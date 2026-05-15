package com.syndicati.services.polling;

import com.syndicati.services.DatabaseService;
import com.syndicati.services.events.DataUpdateBus;
import com.syndicati.utils.database.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.EnumMap;
import java.util.Map;

/**
 * Polling-based DB change detector.
 *
 * Strategy:
 * - Periodically query cheap "watermarks" (MAX(id) and COUNT(*)) for key tables.
 * - When the watermark changes, publish a topic so views can update incrementally.
 *
 * This avoids heavy full refresh loops while still keeping the UI reactive.
 */
public final class DbChangePoller {

    private final DatabaseService db = DatabaseService.getInstance();
    private final DataUpdateBus bus = DataUpdateBus.getInstance();
    private final ConnectionManager cm = ConnectionManager.getInstance();

    private volatile boolean running = false;
    private final Map<DataUpdateBus.Topic, Watermark> last = new EnumMap<>(DataUpdateBus.Topic.class);

    // Default to a conservative interval to avoid starving the connection pool / UI thread.
    private long baseIntervalMs = 15000;
    private long errorBackoffMs = 3000;

    public void start() {
        if (running) return;
        running = true;
        Thread.startVirtualThread(this::loop);
    }

    public void stop() {
        running = false;
    }

    public void setBaseIntervalMs(long ms) {
        this.baseIntervalMs = Math.max(500, ms);
    }

    private void loop() {
        while (running) {
            try {
                if (!cm.isConnected()) {
                    sleepQuiet(1500);
                    continue;
                }

                boolean any = false;
                any |= checkTopic(DataUpdateBus.Topic.FORUM, "publication", "id");
                any |= checkTopic(DataUpdateBus.Topic.FORUM, "commentaire", "id_commentaire");
                any |= checkTopic(DataUpdateBus.Topic.EVENTS, "evenement", "id_event");
                any |= checkTopic(DataUpdateBus.Topic.RESIDENCE, "residence", "id_residence");
                any |= checkTopic(DataUpdateBus.Topic.RESIDENCE, "appartement", "id_app");
                any |= checkTopic(DataUpdateBus.Topic.RECLAMATION, "reclamations", "idreclamations");

                // Coalesce: publish at most once per topic per tick (handled inside checkTopic).
                errorBackoffMs = 3000;
                sleepQuiet(any ? 800 : baseIntervalMs);
            } catch (Exception e) {
                sleepQuiet(errorBackoffMs);
                errorBackoffMs = Math.min(20000, (long) (errorBackoffMs * 1.5));
            }
        }
    }

    private boolean checkTopic(DataUpdateBus.Topic topic, String table, String idCol) {
        Watermark w = readWatermark(table, idCol);
        if (w == null) return false;

        Watermark prev = last.get(topic);
        if (prev == null) {
            last.put(topic, w);
            return false;
        }

        if (!prev.equals(w)) {
            last.put(topic, w);
            bus.publish(topic);
            return true;
        }
        return false;
    }

    private Watermark readWatermark(String table, String idCol) {
        String sql = "SELECT COALESCE(MAX(" + idCol + "), 0) AS max_id, COUNT(*) AS cnt FROM " + table;
        try (Connection c = db.getConnection()) {
            if (c == null) return null;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long maxId = rs.getLong("max_id");
                        long cnt = rs.getLong("cnt");
                        return new Watermark(maxId, cnt);
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private record Watermark(long maxId, long count) {}
}

