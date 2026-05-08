package com.syndicati.services.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Minimal in-process event bus for incremental UI updates.
 *
 * Views can subscribe to updates and refresh only affected sections.
 */
public final class DataUpdateBus {

    public enum Topic {
        FORUM,
        EVENTS,
        RESIDENCE,
        RECLAMATION,
        PROFILE
    }

    private static final DataUpdateBus INSTANCE = new DataUpdateBus();

    public static DataUpdateBus getInstance() {
        return INSTANCE;
    }

    private final Map<Topic, List<Consumer<Topic>>> listeners = new ConcurrentHashMap<>();

    private DataUpdateBus() {}

    public AutoCloseable subscribe(Topic topic, Consumer<Topic> listener) {
        listeners.computeIfAbsent(topic, t -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> unsubscribe(topic, listener);
    }

    public void unsubscribe(Topic topic, Consumer<Topic> listener) {
        List<Consumer<Topic>> ls = listeners.get(topic);
        if (ls != null) {
            ls.remove(listener);
        }
    }

    public void publish(Topic topic) {
        List<Consumer<Topic>> ls = listeners.get(topic);
        if (ls == null || ls.isEmpty()) return;
        for (Consumer<Topic> l : ls) {
            try {
                l.accept(topic);
            } catch (Exception ignored) {}
        }
    }
}

