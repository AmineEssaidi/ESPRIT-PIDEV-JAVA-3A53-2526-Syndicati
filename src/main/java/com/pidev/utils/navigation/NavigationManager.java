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
            default:
                return landingPageView.getRoot();
        }
    }
    
    public void navigateTo(String pageName) {
        System.out.println("Navigating to: " + pageName);
        // Navigate to the specified page by updating the LandingPageView content
        if (landingPageView != null) {
            if ("profile".equals(pageName)) {
                landingPageView.navigateToProfile();
            } else if ("home".equals(pageName)) {
                landingPageView.navigateToHome();
            } else if ("dashboard".equals(pageName)) {
                landingPageView.navigateToDashboard();
            } else if ("settings".equals(pageName)) {
                landingPageView.navigateToSettings();
            }
        }
    }
}
