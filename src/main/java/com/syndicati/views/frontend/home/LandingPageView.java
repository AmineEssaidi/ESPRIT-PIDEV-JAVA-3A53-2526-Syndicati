package com.syndicati.views.frontend.home;

import javafx.scene.layout.*;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ScrollPane;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.syndicati.components.shared.DynamicHeader;
import com.syndicati.components.shared.DynamicFooter;
import com.syndicati.components.home.HomeContent;
 
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.views.backend.dashboard.DashboardView;
import com.syndicati.views.frontend.profile.ProfileView;

/**
 * Landing Page View - Main container with dynamic island header and footer
 */
public class LandingPageView implements ViewInterface {
    
    private final StackPane root;
    private final DynamicHeader header;
    private final DynamicFooter footer;
    private final Runnable accentRefreshListener;
    private VBox mainContent; 
    private VBox darkPanel; 
    private HomeContent homeContent;
    private VBox contentRow; 
    private String currentPageName = "home";
    private final java.util.Map<String, javafx.scene.Node> pageCache = new java.util.HashMap<>();
    private String lastPageName = "home";
    private final java.util.concurrent.atomic.AtomicLong navigationVersion = new java.util.concurrent.atomic.AtomicLong();
    private com.syndicati.views.frontend.auth.LoadingCinematicView activeLoading;
    
    public LandingPageView() {
        this("home");
    }

    public LandingPageView(String initialPage) {
        this.currentPageName = initialPage != null ? initialPage : "home";
        this.root = new StackPane();
        this.header = new DynamicHeader();
        this.footer = new DynamicFooter();
        this.accentRefreshListener = () -> {
            applyThemeStyling();
            refreshVisiblePageForAccent();
            header.refreshTheme();
            footer.refreshTheme();
        };
        
        setupLayout();
        ThemeManager.getInstance().addAccentChangeListener(accentRefreshListener);
        header.setMainContainer(root);
        header.setBackgroundUpdateCallback(() -> {
            applyThemeStyling();
            footer.refreshTheme();
        });
        
        // Start building the UI in small slices to prevent freezing
        loadProgressively();
    }
    
    private void loadProgressively() {
        // Step 1: Initialize Header (Fast)
        javafx.application.Platform.runLater(() -> {
            if (header.getRoot() != null) {
                // Ensure header is in the right place
            }
        });

        // Step 2: Initialize HomeContent (Heavy)
        javafx.application.Platform.runLater(() -> {
            homeContent = new HomeContent();
            if (mainContent != null && homeContent.getRoot() != null) {
                mainContent.getChildren().add(homeContent.getRoot());
                // If we are NOT on home page, hide the home content immediately
                if (!"home".equalsIgnoreCase(currentPageName)) {
                    homeContent.getRoot().setVisible(false);
                    homeContent.getRoot().setManaged(false);
                }
            }
        });

        // Step 3: Initialize Footer (Medium)
        javafx.application.Platform.runLater(() -> {
            if (footer.getRoot() != null && contentRow != null) {
                // Footer is usually at the bottom of the pageWrapper scroll content
            }
        });
    }
    
    private void setupLayout() {
        root.setStyle(rootBaseStyle());
        root.setBackground(null);

        darkPanel = new VBox();
        darkPanel.setSpacing(0);
        darkPanel.setPadding(new Insets(0));
        darkPanel.setAlignment(Pos.TOP_LEFT);
        
        ThemeManager themeManager = ThemeManager.getInstance();
        
        String imagePath = getClass().getResource("/images/login_bg_3.jpg") != null 
            ? getClass().getResource("/images/login_bg_3.jpg").toExternalForm() 
            : "";
        root.setStyle(
            "-fx-background-image: url('" + imagePath + "');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center center;" +
            themeManager.getScrollbarVariableStyle()
        );
        
        String backgroundColor = themeManager.isDarkMode()
            ? "rgba(15,15,20,0.75)"
            : "rgba(245,248,255,0.75)";
            
        darkPanel.setStyle(
            "-fx-background-color: " + backgroundColor + ";" +
            "-fx-border-color: " + (themeManager.isDarkMode() ? themeManager.toRgba(themeManager.getAccentHex(), 0.26) : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 0;"
        );

        StackPane.setAlignment(darkPanel, Pos.TOP_LEFT);
        StackPane.setMargin(darkPanel, new Insets(0));
        darkPanel.maxWidthProperty().bind(root.widthProperty());
        darkPanel.prefWidthProperty().bind(darkPanel.maxWidthProperty());
        darkPanel.maxHeightProperty().bind(root.heightProperty());
        darkPanel.prefHeightProperty().bind(darkPanel.maxHeightProperty());

        mainContent = new VBox();
        mainContent.setSpacing(16);
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.setPadding(new Insets(102, 10, 10, 10));
        mainContent.setCache(true);
        mainContent.setCacheHint(javafx.scene.CacheHint.QUALITY);

        // REMOVED: immediate creation of homeContent. 
        // It is now handled in loadProgressively()

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;" + themeManager.getScrollbarVariableStyle());

        VBox pageWrapper = new VBox();
        pageWrapper.setFillWidth(true);
        pageWrapper.maxWidthProperty().bind(scrollPane.widthProperty());
        pageWrapper.getChildren().add(mainContent);

        VBox footerContainer = new VBox();
        footerContainer.setAlignment(Pos.CENTER);
        footerContainer.setPadding(new Insets(20, 0, 24, 0));
        footerContainer.getChildren().add(footer.getRoot());
        pageWrapper.getChildren().add(footerContainer);

        scrollPane.setContent(pageWrapper);

        HBox windowBar = createWindowBar();
        windowBar.setPickOnBounds(true);
        windowBar.setMouseTransparent(false);
        
        contentRow = new VBox();
        contentRow.setSpacing(6);
        contentRow.setAlignment(Pos.TOP_LEFT);
        contentRow.setPadding(new Insets(48, 12, 12, 12));
        contentRow.setFillWidth(true);
        StackPane floatingHeaderLayer = new StackPane(scrollPane, header.getRoot());
        floatingHeaderLayer.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(header.getRoot(), Pos.TOP_CENTER);
        StackPane.setMargin(header.getRoot(), new Insets(0, 0, 0, 0));

        contentRow.getChildren().add(floatingHeaderLayer);
        VBox.setVgrow(floatingHeaderLayer, Priority.ALWAYS);
        VBox.setVgrow(contentRow, Priority.ALWAYS);
        
        darkPanel.getChildren().add(contentRow);
        root.getChildren().add(darkPanel);
        root.getChildren().add(windowBar);
        StackPane.setAlignment(windowBar, Pos.TOP_LEFT);

        root.addEventFilter(ActionEvent.ACTION, e -> NavigationManager.getInstance().awardInteractionXp(1));
    }
    
    public StackPane getRoot() { return root; }
    public VBox getMainContent() { return mainContent; }
    public DynamicHeader getHeader() { return header; }
    public String getCurrentPageName() { return currentPageName; }
    
    public void navigateToProfile() {
        navigateToPage("profile");
    }
    
    public void navigateToHome() {
        long navToken = navigationVersion.incrementAndGet();
        showLoadingTransition("home", () -> {
            if (navToken != navigationVersion.get()) return;
            currentPageName = "home";
            if (!darkPanel.getChildren().contains(contentRow)) {
                darkPanel.getChildren().clear();
                darkPanel.getChildren().add(contentRow);
            }

            // Dispose the previous non-home page (navigateTo("home") was only hiding nodes before).
            if (lastPageName != null
                && !lastPageName.isBlank()
                && !"home".equalsIgnoreCase(lastPageName)
                && !"dashboard".equalsIgnoreCase(lastPageName)) {
                try {
                    System.out.println("[Dispose] LandingPageView.navigateToHome disposing " + lastPageName);
                    // Remove all non-home pages from the scene graph and clear cache.
                    removeNonHomePageNodes();
                    pageCache.clear();
                    NavigationManager.getInstance().disposeView(lastPageName);
                } catch (Exception ignored) {}
            }
            
            // Hide all cached pages
            for (javafx.scene.Node node : mainContent.getChildren()) {
                node.setVisible(false);
                node.setManaged(false);
            }
            
            if (homeContent == null) {
                homeContent = new HomeContent();
                mainContent.getChildren().add(homeContent.getRoot());
            }
            
            homeContent.getRoot().setVisible(true);
            homeContent.getRoot().setManaged(true);

            lastPageName = "home";
        });
    }
    
    public void navigateToDashboard() {
        enterDashboardMode();
    }

    public void enterDashboardMode() {
        long navToken = navigationVersion.incrementAndGet();
        showLoadingTransition("Admin Dashboard", () -> {
            if (navToken != navigationVersion.get()) return;
            currentPageName = "dashboard";

            // Dispose previous non-dashboard view before entering dashboard mode.
            if (lastPageName != null
                && !lastPageName.isBlank()
                && !"dashboard".equalsIgnoreCase(lastPageName)
                && !"home".equalsIgnoreCase(lastPageName)) {
                try {
                    System.out.println("[Dispose] LandingPageView.enterDashboardMode disposing " + lastPageName);
                    removeNonHomePageNodes();
                    pageCache.clear();
                    NavigationManager.getInstance().disposeView(lastPageName);
                } catch (Exception ignored) {}
            }

            DashboardView dv = NavigationManager.getInstance().getDashboardView();
            dv.setExitCallback(this::exitDashboardMode);
            HBox adminRoot = dv.getRoot();
            adminRoot.setMaxWidth(Double.MAX_VALUE);
            adminRoot.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(adminRoot, Priority.ALWAYS);
            darkPanel.getChildren().clear();
            darkPanel.getChildren().add(adminRoot);

            lastPageName = "dashboard";
        });
    }

    public void exitDashboardMode() {
        darkPanel.getChildren().clear();
        darkPanel.getChildren().add(contentRow);
        navigateToHome();
    }
    
    public void navigateToPage(String pageName) {
        if (pageName == null) return;
        long navToken = navigationVersion.incrementAndGet();
        
        showLoadingTransition(pageName, () -> {
            if (navToken != navigationVersion.get()) return;
            String normalizedPage = pageName.toLowerCase();
            currentPageName = normalizedPage;

            if ("home".equalsIgnoreCase(pageName)) {
                navigateToHome();
                return;
            }
            if ("dashboard".equalsIgnoreCase(pageName)) {
                enterDashboardMode();
                return;
            }

            if (!darkPanel.getChildren().contains(contentRow)) {
                darkPanel.getChildren().clear();
                darkPanel.getChildren().add(contentRow);
            }

            // Sweep expired DB cache entries to prevent memory stacking across navigation.
            try {
                com.syndicati.services.DatabaseService.getInstance().sweepExpiredCache();
            } catch (Exception ignored) {}
            // Trim image cache to avoid accumulating decoded image buffers across view hops.
            try {
                com.syndicati.utils.image.ImageLoaderUtil.trimCache(12);
            } catch (Exception ignored) {}

            // Dispose the previous page so its Node tree can be garbage collected.
            // This prevents memory stacking when navigating between heavy views.
            if (lastPageName != null
                && !lastPageName.isBlank()
                && !"home".equalsIgnoreCase(lastPageName)
                && !"dashboard".equalsIgnoreCase(lastPageName)
                && !lastPageName.equalsIgnoreCase(normalizedPage)) {
                // Remove all non-home pages from the scene graph.
                removeNonHomePageNodes();
                pageCache.clear();
                NavigationManager.getInstance().disposeView(lastPageName);
                try { com.syndicati.utils.perf.MemoryPressureUtil.onViewDisposed(); } catch (Exception ignored) {}
            }

            // NO-CACHE MODE: always dispose and recreate the target view so RAM stays low.
            // Exception: Profile is warm-started during SessionRecovery; disposing it here breaks preloading.
            if (!"profile".equalsIgnoreCase(normalizedPage)) {
                NavigationManager.getInstance().disposeView(normalizedPage);
            }
            ViewInterface view = NavigationManager.getInstance().getView(normalizedPage);
            javafx.scene.Node targetPage = view != null ? view.getRoot() : null;

            // Hide home content when leaving home.
            if (homeContent != null && homeContent.getRoot() != null) {
                boolean isHome = "home".equalsIgnoreCase(normalizedPage);
                homeContent.getRoot().setVisible(isHome);
                homeContent.getRoot().setManaged(isHome);
            }

            if (targetPage != null) {
                targetPage.setVisible(true);
                targetPage.setManaged(true);
                mainContent.getChildren().add(targetPage);

                // Trigger async data load for everything except Profile (which is built in bg)
                if (!(view instanceof ProfileView)) {
                    Thread.startVirtualThread(() -> {
                        if (navToken == navigationVersion.get()) {
                            view.loadDataAsync();
                        }
                    });
                }
            }

            lastPageName = normalizedPage;
            try { com.syndicati.utils.perf.MemoryPressureUtil.onNavigationSwap(); } catch (Exception ignored) {}
        });
    }

    private void showLoadingTransition(String viewName, Runnable midAction) {
        if (activeLoading != null) {
            try {
                root.getChildren().remove(activeLoading.getRoot());
                activeLoading.cleanup();
            } catch (Exception ignored) {}
            activeLoading = null;
        }
        com.syndicati.views.frontend.auth.LoadingCinematicView loading = new com.syndicati.views.frontend.auth.LoadingCinematicView(viewName);
        activeLoading = loading;
        root.getChildren().add(loading.getRoot());
        loading.getRoot().toFront();
        
        loading.play(() -> {
            root.getChildren().remove(loading.getRoot());
            if (activeLoading == loading) {
                activeLoading = null;
            }
            loading.cleanup();
        });

        // Small delay to ensure the loading screen is rendered before the UI swap potentially lags the FX thread
        javafx.animation.PauseTransition shortDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
        shortDelay.setOnFinished(e -> midAction.run());
        shortDelay.play();
    }

    private void removeNonHomePageNodes() {
        if (mainContent == null) return;
        for (javafx.scene.Node node : new java.util.ArrayList<>(mainContent.getChildren())) {
            if (homeContent != null && node == homeContent.getRoot()) continue;
            mainContent.getChildren().remove(node);
            try { com.syndicati.utils.perf.NodeTreeDisposer.dispose(node); } catch (Exception ignored) {}
        }
    }

    private void applyThemeStyling() {
        ThemeManager themeManager = ThemeManager.getInstance();
        String imagePath = getClass().getResource("/images/login_bg_3.jpg") != null 
            ? getClass().getResource("/images/login_bg_3.jpg").toExternalForm() 
            : "";
        root.setStyle(
            "-fx-background-image: url('" + imagePath + "');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center center;" +
            themeManager.getScrollbarVariableStyle()
        );
        
        if (darkPanel != null) {
            String backgroundColor = themeManager.isDarkMode()
                ? "rgba(15,15,20,0.75)"
                : "rgba(245,248,255,0.75)";
                
            darkPanel.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                "-fx-border-color: " + (themeManager.isDarkMode() ? themeManager.toRgba(themeManager.getAccentHex(), 0.26) : "rgba(15,23,42,0.14)") + ";" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 0;"
            );
        }
    }

    private String rootBaseStyle() {
        ThemeManager tm = ThemeManager.getInstance();
        return "-fx-background-color: transparent; -fx-background: transparent;" + tm.getScrollbarVariableStyle();
    }

    private void refreshVisiblePageForAccent() {
        NavigationManager navigation = NavigationManager.getInstance();
        navigation.rebuildThemeSensitiveViews();

        if ("home".equals(currentPageName)) {
            homeContent = new HomeContent();
            mainContent.getChildren().setAll(homeContent.getRoot());
            return;
        }

        if ("dashboard".equals(currentPageName)) {
            DashboardView dv = navigation.getDashboardView();
            dv.setExitCallback(this::exitDashboardMode);
            HBox adminRoot = dv.getRoot();
            adminRoot.setMaxWidth(Double.MAX_VALUE);
            adminRoot.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(adminRoot, Priority.ALWAYS);
            darkPanel.getChildren().setAll(adminRoot);
            return;
        }

        // For cached pages, we just refresh their root
        ViewInterface currentView = navigation.getView(currentPageName);
        if (currentView != null && mainContent != null) {
             // Redundant for this optimization phase but ensures theme sync
             navigateToPage(currentPageName);
        }
    }
    
    public void cleanup() {
        navigationVersion.incrementAndGet();
        ThemeManager.getInstance().removeAccentChangeListener(accentRefreshListener);
        if (header != null) header.cleanup();
        if (footer != null) footer.cleanup();
        if (activeLoading != null) {
            try { activeLoading.cleanup(); } catch (Exception ignored) {}
            activeLoading = null;
        }
        removeNonHomePageNodes();

        // Dispose cached pages/views to release memory.
        try {
            for (String k : new java.util.ArrayList<>(pageCache.keySet())) {
                NavigationManager.getInstance().disposeView(k);
            }
            pageCache.clear();
        } catch (Exception ignored) {}
    }

    private HBox createWindowBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(8);
        bar.setPadding(new Insets(4, 16, 4, 16));
        bar.setPrefHeight(40);
        bar.setMinHeight(40);
        bar.setMaxHeight(40);
        bar.setStyle("-fx-background-color: transparent;");
        bar.setPickOnBounds(false);
        bar.setMouseTransparent(false);

        Region dragRegion = new Region();
        HBox.setHgrow(dragRegion, Priority.ALWAYS);
        dragRegion.setMinHeight(40);
        dragRegion.setStyle("-fx-cursor: move;");

        javafx.scene.control.Button btnMin = new javafx.scene.control.Button("");
        javafx.scene.control.Button btnMax = new javafx.scene.control.Button("");
        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("");

        styleMacOSButton(btnMin, "#febc2e", "-");
        styleMacOSButton(btnMax, "#28c840", "+");
        styleMacOSButton(btnClose, "#ff5f57", "x");

        HBox buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getChildren().addAll(btnMin, btnMax, btnClose);

        btnMin.setOnAction(e -> ((javafx.stage.Stage) root.getScene().getWindow()).setIconified(true));
        btnMax.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });
        btnClose.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.fireEvent(new javafx.stage.WindowEvent(stage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        final double[] dragOffset = new double[2];
        dragRegion.setOnMousePressed(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            dragOffset[0] = e.getScreenX() - stage.getX();
            dragOffset[1] = e.getScreenY() - stage.getY();
        });
        dragRegion.setOnMouseDragged(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - dragOffset[0]);
                stage.setY(e.getScreenY() - dragOffset[1]);
            }
        });

        bar.getChildren().addAll(dragRegion, buttonContainer);
        return bar;
    }

    private void styleMacOSButton(javafx.scene.control.Button btn, String color, String symbol) {
        btn.setMinSize(12, 12);
        btn.setPrefSize(12, 12);
        btn.setMaxSize(12, 12);
        btn.setStyle("-fx-background-color: "+color+"; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: transparent;");
        btn.setOnMouseEntered(e -> {
            btn.setText(symbol);
            btn.setStyle("-fx-background-color: "+color+"; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: rgba(0,0,0,0.6); -fx-font-size: 8;");
        });
        btn.setOnMouseExited(e -> {
            btn.setText("");
            btn.setStyle("-fx-background-color: "+color+"; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: transparent;");
        });
    }
}
