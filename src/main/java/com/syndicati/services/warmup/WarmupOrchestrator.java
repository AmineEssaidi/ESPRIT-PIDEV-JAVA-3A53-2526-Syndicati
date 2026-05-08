package com.syndicati.services.warmup;

import com.syndicati.controllers.evenement.EvenementController;
import com.syndicati.controllers.forum.PublicationController;
import com.syndicati.controllers.residence.ResidenceController;
import com.syndicati.controllers.syndicat.ReclamationController;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.controllers.user.relationship.UserRelationshipController;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.user.User;
import com.syndicati.models.user.data.UserStandingRepository;
import com.syndicati.services.DatabaseService;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.session.SessionManager;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Coordinates warm-start work during SessionRecoveryView.
 *
 * Goals:
 * - Prefetch critical data into SessionManager and DatabaseService cache.
 * - Pre-initialize heavy views and trigger their async loads so first navigation is instant.
 */
public final class WarmupOrchestrator {

    public static final String CACHE_WARMUP_MARKER = "warmup:completed";

    private final DatabaseService db = DatabaseService.getInstance();
    private final SessionManager sm = SessionManager.getInstance();

    public void runFullWarmup(BiConsumer<Double, String> progress) {
        Objects.requireNonNull(progress, "progress");
        ProgressThrottle reporter = new ProgressThrottle(progress, 120);

        User user = sm.getCurrentUser();
        if (user == null || user.getIdUser() == null) {
            reporter.report(1.0, "No session");
            return;
        }

        long start = System.currentTimeMillis();
        reporter.report(0.05, "Syncing identity...");

        // 1) Identity/profile (critical)
        try {
            if (sm.getCurrentProfile() == null) {
                sm.setCurrentProfile(new ProfileController().profileByUserId(user.getIdUser()).orElse(null));
            }
        } catch (Exception ignored) {}

        // 2) Social graph (often needed across UI)
        reporter.report(0.15, "Syncing relationships...");
        try {
            UserRelationshipController rc = new UserRelationshipController();
            sm.setCircleData(
                rc.findFriends(user, 24),
                rc.findPendingRequestsFor(user),
                rc.countFriends(user),
                rc.countPendingRequests(user)
            );
        } catch (Exception ignored) {}

        // 2.5) Standing / XP state (ProfileView relies on this freshness)
        reporter.report(0.20, "Syncing standing...");
        try {
            if (sm.getCurrentStanding() == null) {
                new UserStandingRepository()
                    .findByUserId(user.getIdUser())
                    .ifPresent(sm::setCurrentStanding);
            }
        } catch (Exception ignored) {}

        // 3) Pre-initialize heavy views (UI construction warmup)
        // IMPORTANT: view construction runs on the JavaFX thread; doing too much at once can freeze video/animations.
        // We schedule view creation in small pulses.
        reporter.report(0.25, "Warming views...");
        try {
            NavigationManager nm = NavigationManager.getInstance();
            // Warm all session caches (profile/standing/circle TTL) with the same path used elsewhere.
            nm.warmup();
            nm.initializeHeavyViews(); // ProfileView (already uses Platform.runLater)
            scheduleViewWarmup(nm);
        } catch (Exception ignored) {}

        // 4) Warm service datasets into repository/db cache
        reporter.report(0.40, "Fetching community...");
        try {
            PublicationController pub = new PublicationController();
            db.putCache("forum:category:General", pub.publicationsByCategory("General"));
            db.putCache("forum:category:Announcement", pub.publicationsByCategory("Announcement"));
        } catch (Exception ignored) {}

        reporter.report(0.55, "Fetching events...");
        try {
            EvenementController ev = new EvenementController();
            db.putCache("events:list", ev.evenements());
        } catch (Exception ignored) {}

        reporter.report(0.70, "Fetching residences...");
        try {
            ResidenceController res = new ResidenceController();
            List<Residence> residences = res.residences();
            db.putCache("residence:list", residences);
            // Warm apartments for each residence (bounded by pool size; still helps first open)
            for (Residence r : residences) {
                if (r != null && r.getIdResidence() != null) {
                    db.putCache("apartments:residence:" + r.getIdResidence(), res.apartmentsByResidence(r.getIdResidence()));
                }
            }
        } catch (Exception ignored) {}

        reporter.report(0.85, "Fetching reclamations...");
        try {
            ReclamationController rc = new ReclamationController();
            db.putCache("reclamation:list", rc.reclamations());
        } catch (Exception ignored) {}

        // 5) Kick async data loads for views that implement them
        reporter.report(0.93, "Finalizing...");
        try {
            NavigationManager nm = NavigationManager.getInstance();
            // Same idea: keep FX thread responsive. Views will start their own background threads.
            Platform.runLater(() -> {
                try {
                    var forum = nm.getView("forum");
                    var events = nm.getView("evenement");
                    var residence = nm.getView("residence");
                    var syndicat = nm.getView("syndicat");
                    var profile = nm.getView("profile");
                    if (forum != null) forum.loadDataAsync();
                    if (events != null) events.loadDataAsync();
                    if (residence != null) residence.loadDataAsync();
                    if (syndicat != null) syndicat.loadDataAsync();
                    if (profile != null) profile.loadDataAsync();
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}

        db.putCache(CACHE_WARMUP_MARKER, Boolean.TRUE);
        reporter.report(1.0, "Ready");

        long elapsed = System.currentTimeMillis() - start;
        db.putCache("warmup:elapsed_ms", elapsed);
    }

    private void scheduleViewWarmup(NavigationManager nm) {
        // Space out heavy view instantiation to avoid blocking video frames.
        // Must be scheduled/played on the JavaFX thread.
        Platform.runLater(() -> {
            Timeline t = new Timeline(
                new KeyFrame(Duration.millis(0), e -> { try { nm.getView("forum"); } catch (Exception ignored) {} }),
                new KeyFrame(Duration.millis(180), e -> { try { nm.getView("evenement"); } catch (Exception ignored) {} }),
                new KeyFrame(Duration.millis(360), e -> { try { nm.getView("residence"); } catch (Exception ignored) {} }),
                new KeyFrame(Duration.millis(540), e -> { try { nm.getView("syndicat"); } catch (Exception ignored) {} }),
                new KeyFrame(Duration.millis(720), e -> { try { nm.getView("dashboard"); } catch (Exception ignored) {} })
            );
            t.play();
        });
    }

    private static final class ProgressThrottle {
        private final BiConsumer<Double, String> sink;
        private final long minIntervalMs;
        private volatile long lastAt = 0L;

        private ProgressThrottle(BiConsumer<Double, String> sink, long minIntervalMs) {
            this.sink = sink;
            this.minIntervalMs = Math.max(0, minIntervalMs);
        }

        private void report(double p, String status) {
            long now = System.currentTimeMillis();
            if (p >= 1.0 || now - lastAt >= minIntervalMs) {
                lastAt = now;
                try { sink.accept(p, status); } catch (Exception ignored) {}
            }
        }
    }
}

