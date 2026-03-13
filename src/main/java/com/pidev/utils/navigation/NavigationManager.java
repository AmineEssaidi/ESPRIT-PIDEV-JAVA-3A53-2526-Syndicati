package com.pidev.utils.navigation;

import javafx.scene.layout.Pane;
import com.pidev.views.home.LandingPageView;
import com.pidev.views.services.ServicesView;
import com.pidev.views.about.AboutView;
import com.pidev.views.profile.ProfileView;
import com.pidev.views.dashboard.DashboardView;
import com.pidev.views.services.ServiceDetailView;
import com.pidev.views.about.AboutDetailView;
import com.pidev.views.settings.SettingsView;
import com.pidev.views.services.ResidencePageView;
import com.pidev.views.services.ForumPageView;
import com.pidev.views.services.SyndicatPageView;
import com.pidev.views.services.EvenementPageView;

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
    
    private NavigationManager() {}
    
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    public void setViews(LandingPageView landingPageView) {
        this.landingPageView = landingPageView;
        this.servicesView = new ServicesView();
        this.aboutView = new AboutView();
        this.profileView = new ProfileView();
        this.dashboardView = new DashboardView();
        this.serviceDetailView = new ServiceDetailView();
        this.aboutDetailView = new AboutDetailView();
        this.settingsView = new SettingsView();
        this.residenceView = new ResidencePageView();
        this.forumView = new ForumPageView();
        this.syndicatView = new SyndicatPageView();
        this.evenementView = new EvenementPageView();
    }
    
    public Pane getPage(String pageName) {
        switch (pageName.toLowerCase()) {
            case "home":
                return landingPageView.getRoot();
            case "services":
                return servicesView.getRoot();
            case "about":
                return aboutView.getRoot();
            case "profile":
                return profileView.getRoot();
            case "dashboard":
                return dashboardView.getRoot();
            case "service-detail":
                return serviceDetailView.getRoot();
            case "about-detail":
                return aboutDetailView.getRoot();
            case "settings":
                return settingsView.getRoot();
            case "services/residence":
                return residenceView.getRoot();
            case "services/forum":
                return forumView.getRoot();
            case "services/syndicat":
                return syndicatView.getRoot();
            case "services/evenement":
                return evenementView.getRoot();
            default:
                return landingPageView.getRoot();
        }
    }
    
    public void navigateTo(String pageName) {
        System.out.println("Navigating to: " + pageName);
        if (landingPageView != null) {
            if ("home".equals(pageName)) {
                landingPageView.navigateToHome();
            } else {
                landingPageView.navigateToPage(pageName);
            }
        }
    }

    /** Exposes the dashboard view so LandingPageView can set its exit callback. */
    public DashboardView getDashboardView() {
        return dashboardView;
    }
}
