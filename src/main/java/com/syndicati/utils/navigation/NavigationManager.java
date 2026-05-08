package com.syndicati.utils.navigation;
import com.syndicati.interfaces.ViewInterface;

import javafx.scene.layout.Pane;
import com.syndicati.views.frontend.home.LandingPageView;
import com.syndicati.views.frontend.services.ServicesView;
import com.syndicati.views.frontend.about.AboutView;
import com.syndicati.views.frontend.profile.ProfileView;
import com.syndicati.views.backend.dashboard.DashboardView;
import com.syndicati.views.frontend.services.ServiceDetailView;
import com.syndicati.views.frontend.about.AboutDetailView;
import com.syndicati.views.frontend.settings.SettingsView;
import com.syndicati.views.frontend.services.ResidencePageView;
import com.syndicati.views.frontend.services.ForumPageView;
import com.syndicati.views.frontend.services.SyndicatPageView;
import com.syndicati.views.frontend.services.EvenementPageView;
import com.syndicati.utils.security.AccessControlService;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.controllers.log.ActivityLogController;
import javafx.scene.control.Alert;

/**
 * Navigation Manager - Handles page navigation and sub-menu management
 */
public class NavigationManager {
    
    private static NavigationManager instance;
    private LandingPageView landingPageView;
    private ServicesView servicesView;
    private AboutView aboutView;
    private ProfileView profileView;
    private DashboardView dashboardView;
    private ServiceDetailView serviceDetailView;
    private AboutDetailView aboutDetailView;
    private SettingsView settingsView;
    private ResidencePageView residenceView;
    private ForumPageView forumView;
    private SyndicatPageView syndicatView;
    private EvenementPageView evenementView;
    private final ActivityLogController activityLogController = new ActivityLogController();
    
    private NavigationManager() {}
    
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    public void setViews(LandingPageView landingPageView) {
        this.landingPageView = landingPageView;
    }

    public void clearAllViews(boolean includeHeavy) {
        this.servicesView = null;
        this.aboutView = null;
        if (includeHeavy) {
            this.profileView = null;
            this.dashboardView = null;
        }
        this.serviceDetailView = null;
        this.aboutDetailView = null;
        this.settingsView = null;
        this.residenceView = null;
        this.forumView = null;
        this.syndicatView = null;
        this.evenementView = null;
    }

    /**
     * One-time initialization of heavy views like ProfileView.
     * Called during session recovery to prevent hangs later.
     */
    public void initializeHeavyViews() {
        javafx.application.Platform.runLater(() -> {
            if (profileView == null) {
                System.out.println("[NavigationManager] Initializing heavy ProfileView...");
                profileView = new com.syndicati.views.frontend.profile.ProfileView();
            }
        });
    }

    public void warmup() {
        // Silent background warmup for data and heavy views.
        Thread.startVirtualThread(() -> {
            try {
                com.syndicati.models.user.User user = SessionManager.getInstance().getCurrentUser();
                if (user == null) return;

                // 1. Refresh data silently if not fresh
                SessionManager sm = SessionManager.getInstance();
                if (!sm.isProfileFresh()) {
                    new com.syndicati.controllers.user.profile.ProfileController()
                        .findOneByUserId(user.getIdUser())
                        .ifPresent(sm::setCurrentProfile);
                }
                
                if (!sm.isStandingFresh()) {
                    new com.syndicati.models.user.data.UserStandingRepository()
                        .findByUserId(user.getIdUser())
                        .ifPresent(sm::setCurrentStanding);
                }

                if (!sm.isCircleCacheFresh()) {
                    var rc = new com.syndicati.controllers.user.relationship.UserRelationshipController();
                    sm.setCircleData(
                        rc.findFriends(user, 24),
                        rc.findPendingRequestsFor(user),
                        rc.countFriends(user),
                        rc.countPendingRequests(user)
                    );
                }
            } catch (Exception ignored) {}
        });
    }

    public void rebuildThemeSensitiveViews() {
        // Clear cached views; EXCEPT the currently active one to avoid infinite reload loops
        // during data fetching/theme synchronization.
        String active = landingPageView != null ? landingPageView.getCurrentPageName() : "";
        
        this.servicesView = null;
        this.aboutView = null;
        if (!"profile".equals(active)) this.profileView = null;
        this.dashboardView = null;
        this.serviceDetailView = null;
        this.aboutDetailView = null;
        if (!"settings".equals(active)) this.settingsView = null;
        this.residenceView = null;
        this.forumView = null;
        this.syndicatView = null;
        this.evenementView = null;
    }

    private synchronized ServicesView servicesView() {
        if (servicesView == null) servicesView = new ServicesView();
        return servicesView;
    }

    private synchronized AboutView aboutView() {
        if (aboutView == null) aboutView = new AboutView();
        return aboutView;
    }

    private synchronized ProfileView profileView() {
        if (profileView == null) profileView = new ProfileView();
        return profileView;
    }

    private synchronized DashboardView dashboardView() {
        if (dashboardView == null) dashboardView = new DashboardView();
        return dashboardView;
    }

    private synchronized ServiceDetailView serviceDetailView() {
        if (serviceDetailView == null) serviceDetailView = new ServiceDetailView();
        return serviceDetailView;
    }

    private synchronized AboutDetailView aboutDetailView() {
        if (aboutDetailView == null) aboutDetailView = new AboutDetailView();
        return aboutDetailView;
    }

    private synchronized SettingsView settingsView() {
        if (settingsView == null) settingsView = new SettingsView();
        return settingsView;
    }

    private synchronized ResidencePageView residenceView() {
        if (residenceView == null) residenceView = new ResidencePageView();
        return residenceView;
    }

    private synchronized ForumPageView forumView() {
        if (forumView == null) {
            forumView = new ForumPageView();
            forumView.loadDataAsync();
        }
        return forumView;
    }

    private synchronized SyndicatPageView syndicatView() {
        if (syndicatView == null) syndicatView = new SyndicatPageView();
        return syndicatView;
    }

    private synchronized EvenementPageView evenementView() {
        if (evenementView == null) evenementView = new EvenementPageView();
        return evenementView;
    }

    public void awardInteractionXp(int xpDelta) {
        SessionManager.getInstance().awardXp(xpDelta);
    }
    
    public ViewInterface getView(String pageName) {
        String normalized = pageName == null ? "home" : pageName.toLowerCase().trim();
        
        // Handle service aliases
        if (normalized.equals("residence")) normalized = "services/residence";
        if (normalized.equals("forum")) normalized = "services/forum";
        if (normalized.equals("syndicat")) normalized = "services/syndicat";
        if (normalized.equals("evenement")) normalized = "services/evenement";

        switch (normalized) {
            case "services":
                return servicesView();
            case "about":
                return aboutView();
            case "profile":
                return profileView();
            case "dashboard":
                return dashboardView();
            case "service-detail":
                return serviceDetailView();
            case "about-detail":
                return aboutDetailView();
            case "settings":
                return settingsView();
            case "services/residence":
                return residenceView();
            case "services/forum":
                return forumView();
            case "services/syndicat":
                return syndicatView();
            case "services/evenement":
                return evenementView();
            default:
                return null;
        }
    }

    /**
     * Dispose a cached view instance and release references for GC.
     * This calls cleanup() when available and then nulls the cached field.
     */
    public synchronized void disposeView(String pageName) {
        String normalized = pageName == null ? "" : pageName.toLowerCase().trim();
        if (normalized.equals("residence")) normalized = "services/residence";
        if (normalized.equals("forum")) normalized = "services/forum";
        if (normalized.equals("syndicat")) normalized = "services/syndicat";
        if (normalized.equals("evenement")) normalized = "services/evenement";

        System.out.println("[Dispose] Requested disposeView(" + normalized + ")");

        try {
            switch (normalized) {
                case "services":
                    System.out.println("[Dispose] servicesView=" + (servicesView != null));
                    if (servicesView != null) servicesView.cleanup();
                    servicesView = null;
                    break;
                case "about":
                    System.out.println("[Dispose] aboutView=" + (aboutView != null));
                    if (aboutView != null) aboutView.cleanup();
                    aboutView = null;
                    break;
                case "profile":
                    System.out.println("[Dispose] profileView=" + (profileView != null));
                    if (profileView != null) profileView.cleanup();
                    profileView = null;
                    break;
                case "dashboard":
                    System.out.println("[Dispose] dashboardView=" + (dashboardView != null));
                    if (dashboardView != null) dashboardView.cleanup();
                    dashboardView = null;
                    break;
                case "service-detail":
                    System.out.println("[Dispose] serviceDetailView=" + (serviceDetailView != null));
                    if (serviceDetailView != null) serviceDetailView.cleanup();
                    serviceDetailView = null;
                    break;
                case "about-detail":
                    System.out.println("[Dispose] aboutDetailView=" + (aboutDetailView != null));
                    if (aboutDetailView != null) aboutDetailView.cleanup();
                    aboutDetailView = null;
                    break;
                case "settings":
                    System.out.println("[Dispose] settingsView=" + (settingsView != null));
                    if (settingsView != null) settingsView.cleanup();
                    settingsView = null;
                    break;
                case "services/residence":
                    System.out.println("[Dispose] residenceView=" + (residenceView != null));
                    if (residenceView != null) residenceView.cleanup();
                    residenceView = null;
                    break;
                case "services/forum":
                    System.out.println("[Dispose] forumView=" + (forumView != null));
                    if (forumView != null) forumView.cleanup();
                    forumView = null;
                    break;
                case "services/syndicat":
                    System.out.println("[Dispose] syndicatView=" + (syndicatView != null));
                    if (syndicatView != null) syndicatView.cleanup();
                    syndicatView = null;
                    break;
                case "services/evenement":
                    System.out.println("[Dispose] evenementView=" + (evenementView != null));
                    if (evenementView != null) evenementView.cleanup();
                    evenementView = null;
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
            // Ensure references are still dropped even if cleanup fails.
            if (normalized.equals("services/forum")) forumView = null;
            if (normalized.equals("services/evenement")) evenementView = null;
            if (normalized.equals("services/residence")) residenceView = null;
            if (normalized.equals("services/syndicat")) syndicatView = null;
            if (normalized.equals("profile")) profileView = null;
            if (normalized.equals("dashboard")) dashboardView = null;
        }

        // After dropping references, run a best-effort memory trim under pressure.
        try { com.syndicati.utils.perf.MemoryPressureUtil.onViewDisposed(); } catch (Exception ignored) {}
    }

    public Pane getPage(String pageName) {
        String normalized = pageName == null ? "home" : pageName.toLowerCase().trim();
        
        if (normalized.equals("home")) {
            return new com.syndicati.components.home.HomeContent().getRoot();
        }

        ViewInterface view = getView(pageName);
        if (view != null) {
            return (Pane) view.getRoot();
        }
        
        return new com.syndicati.components.home.HomeContent().getRoot();
    }
    
    public void navigateTo(String pageName) {
        System.out.println("Navigating to: " + pageName);
        String normalizedPage = pageName == null ? "home" : pageName.toLowerCase();

        activityLogController.logPageView(normalizedPage, normalizedPage, java.util.Map.of(
            "source", "navigation_manager"
        ));

        if ("profile".equals(normalizedPage) && !AccessControlService.canAccessProfile()) {
            showAccessDenied("Please sign in to view your profile.");
            return;
        }

        if ("dashboard".equals(normalizedPage) && !AccessControlService.canAccessAdminArea()) {
            showAccessDenied("Access denied. You do not have permission to access the admin area.");
            return;
        }

        if (landingPageView != null) {
            if ("home".equals(normalizedPage)) {
                landingPageView.navigateToHome();
            } else {
                landingPageView.navigateToPage(pageName);
            }
        }
    }

    private void showAccessDenied(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Permission Required");
        alert.setContentText(message);
        if (landingPageView != null) {
            Pane root = landingPageView.getRoot();
            if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
                alert.initOwner(root.getScene().getWindow());
            }
        }
        alert.showAndWait();
    }

    /** Exposes the dashboard view so LandingPageView can set its exit callback. */
    public DashboardView getDashboardView() {
        return dashboardView();
    }
}


