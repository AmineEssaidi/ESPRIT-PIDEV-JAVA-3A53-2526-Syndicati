package com.syndicati;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.text.Font;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import com.syndicati.utils.security.AccessControlService;
import com.syndicati.controllers.log.ActivityLogController;
import com.syndicati.views.frontend.home.AdminDestinationChoiceView;
import com.syndicati.views.frontend.home.LandingPageView;
import com.syndicati.views.frontend.login.LoginView;
import com.syndicati.services.analytics.AnomalyScoringScheduler;
import com.syndicati.services.observability.LangfuseRuntimeService;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.services.DiscordRPCService;
import com.syndicati.services.observability.LogAIWorkerService;
import com.syndicati.services.user.messaging.socket.MessagingSocketServer;
import com.syndicati.services.InsightFaceService;
import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.controllers.forum.PublicationController;
import com.syndicati.controllers.evenement.EvenementController;
import com.syndicati.services.forum.SentimentAnalysisService;

/**
 * Main JavaFX Application - Syndicati desktop client
 */
public class MainApplication extends Application {
    private static final String GLOBAL_SCROLLBAR_CSS = "/styles/app-scrollbar.css";
    
    private static MainApplication instance;
    private Stage primaryStage;
    private LandingPageView landingPageView;
    private LoginView loginView;
    private boolean isLoggedIn = false;
    private javafx.animation.Timeline loginChecker; // Keep reference to stop it later
    private String boldFontFamily = "Clash Grotesk"; // default name in case load resolves differently
    private String lightFontFamily = "Clash Grotesk"; // default name in case load resolves differently
    private boolean windowChromeListenerInstalled = false;
    private final ActivityLogController activityLogController = new ActivityLogController();
    private final AnomalyScoringScheduler anomalyScoringScheduler = new AnomalyScoringScheduler();
    private final LangfuseRuntimeService langfuseRuntimeService = LangfuseRuntimeService.getInstance();
    
    @Override
    public void start(Stage primaryStage) {
        System.err.println("==========================================");
        System.err.println("!!! SYNDICATI APPLICATION STARTING !!!");
        System.err.println("==========================================");
        instance = this;
        this.primaryStage = primaryStage;
        
        // 1. Load custom fonts
        loadCustomFonts();
        
        // 2. Configure Stage (CRITICAL: initStyle MUST be called before show())
        primaryStage.setTitle("Syndicati");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setResizable(true);
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/app_logo/syndicati.png"), 256, 256, true, true));
        primaryStage.setMinWidth(1500);
        primaryStage.setMinHeight(800);

        // 3. Initialize Views
        loginView = new LoginView();
        loginView.setOnLoginSuccess(this::navigateToLandingPage);
        
        com.syndicati.views.frontend.auth.IntroCinematicView introView = new com.syndicati.views.frontend.auth.IntroCinematicView();
        
        // 4. Create Scene
        Scene scene = new Scene(introView.getRoot(), 1500, 800);
        scene.setFill(Color.BLACK);
        applyGlobalStyles(scene);
        if (scene.getRoot() != null) {
            appendRootStyle(scene.getRoot(), "-fx-font-family: '" + lightFontFamily + "';");
        }

        // 5. Setup Managers
        ThemeManager.getInstance().setScene(scene);
        installWindowChromeListener();

        // 6. Show and Play Intro
        primaryStage.setScene(scene);
        primaryStage.show();
        centerStageOnScreen(primaryStage);
        applyRoundedShape(scene);
        addResizeHandlers(primaryStage, scene);
        introView.play();

        // 7. Background Tasks Coordination
        // One-time sync to generate fr.json and ar.json if they don't exist
        Thread.startVirtualThread(() -> {
            com.syndicati.utils.localization.LocalizationManager.getInstance().syncTranslations("fr", "ar");
        });
        
        java.util.concurrent.CompletableFuture<Boolean> sessionCheckFuture = new java.util.concurrent.CompletableFuture<>();
        java.util.concurrent.atomic.AtomicReference<com.syndicati.models.user.User> recoveryUserRef = new java.util.concurrent.atomic.AtomicReference<>();

        introView.setOnFinished(() -> {
            sessionCheckFuture.thenAccept(hasSession -> {
                javafx.application.Platform.runLater(() -> {
                    if (hasSession) {
                        com.syndicati.models.user.User recoveryUser = recoveryUserRef.get();
                        com.syndicati.views.frontend.auth.SessionRecoveryView recoveryView = new com.syndicati.views.frontend.auth.SessionRecoveryView();
                        
                        // Destination Choice Callbacks
                        recoveryView.setOnGoHome(() -> {
                            javafx.application.Platform.runLater(() -> {
                                // 1. Fade out current view
                                FadeTransition ft = new FadeTransition(Duration.millis(800), recoveryView.getRoot());
                                ft.setToValue(0);
                                ScaleTransition st = new ScaleTransition(Duration.millis(800), recoveryView.getRoot());
                                st.setToX(0.95); st.setToY(0.95);
                                
                                ParallelTransition pt = new ParallelTransition(ft, st);
                                pt.setOnFinished(evt -> {
                                    double w = primaryStage.getWidth();
                                    double h = primaryStage.getHeight();
                                    double x = primaryStage.getX();
                                    double y = primaryStage.getY();
                                    boolean max = primaryStage.isMaximized();
                                    
                                    showLandingPage(w, h, x, y, max, false);
                                    
                                    // 2. Fade in new view
                                    Node newRoot = primaryStage.getScene().getRoot();
                                    newRoot.setOpacity(0);
                                    newRoot.setScaleX(1.05); newRoot.setScaleY(1.05);
                                    
                                    FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), newRoot);
                                    fadeIn.setToValue(1.0);
                                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(1000), newRoot);
                                    scaleIn.setToX(1.0); scaleIn.setToY(1.0);
                                    
                                    new ParallelTransition(fadeIn, scaleIn).play();
                                    recoveryView.cleanup();
                                });
                                pt.play();
                            });
                        });
                        
                        recoveryView.setOnGoDashboard(() -> {
                            javafx.application.Platform.runLater(() -> {
                                // 1. Fade out current view
                                FadeTransition ft = new FadeTransition(Duration.millis(800), recoveryView.getRoot());
                                ft.setToValue(0);
                                ScaleTransition st = new ScaleTransition(Duration.millis(800), recoveryView.getRoot());
                                st.setToX(0.95); st.setToY(0.95);
                                
                                ParallelTransition pt = new ParallelTransition(ft, st);
                                pt.setOnFinished(evt -> {
                                    double w = primaryStage.getWidth();
                                    double h = primaryStage.getHeight();
                                    double x = primaryStage.getX();
                                    double y = primaryStage.getY();
                                    boolean max = primaryStage.isMaximized();
                                    
                                    showLandingPage(w, h, x, y, max, true);
                                    
                                    // 2. Fade in new view
                                    Node newRoot = primaryStage.getScene().getRoot();
                                    newRoot.setOpacity(0);
                                    newRoot.setScaleX(1.05); newRoot.setScaleY(1.05);
                                    
                                    FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), newRoot);
                                    fadeIn.setToValue(1.0);
                                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(1000), newRoot);
                                    scaleIn.setToX(1.0); scaleIn.setToY(1.0);
                                    
                                    new ParallelTransition(fadeIn, scaleIn).play();
                                    recoveryView.cleanup();
                                });
                                pt.play();
                            });
                        });

                        primaryStage.getScene().setRoot(recoveryView.getRoot());
                        
                        Thread.startVirtualThread(() -> {
                            try {
                                com.syndicati.utils.session.SessionManager sm = com.syndicati.utils.session.SessionManager.getInstance();
                                recoveryView.setProgress(0.4, "Restoring profile...");
                                sm.setCurrentProfile(new com.syndicati.controllers.user.profile.ProfileController().profileByUserId(recoveryUser.getIdUser()).orElse(null));
                                
                                recoveryView.setProgress(0.8, "Connecting...");
                                com.syndicati.controllers.user.relationship.UserRelationshipController rc = new com.syndicati.controllers.user.relationship.UserRelationshipController();
                                sm.setCircleData(rc.findFriends(recoveryUser, 24), rc.findPendingRequestsFor(recoveryUser), rc.countFriends(recoveryUser), rc.countPendingRequests(recoveryUser));
                                
                                // CRITICAL: Set user and update UI before finalizing progress
                                sm.setCurrentUser(recoveryUser);
                                // Refresh navigation managers with the existing user
                                com.syndicati.utils.navigation.NavigationManager nm = com.syndicati.utils.navigation.NavigationManager.getInstance();
                                nm.warmup();
                                
                                // Give warmup a moment to fetch profile data from DB
                                Thread.sleep(1000);
                                
                                recoveryView.updateUser(recoveryUser);
                                
                                // CRITICAL: One-time pre-load of heavy views during session recovery
                                nm.initializeHeavyViews();
                                
                                recoveryView.setProgress(0.8, com.syndicati.utils.localization.LocalizationManager.getInstance().get("warming_workspace"));
                                Thread.sleep(800);
                                recoveryView.setProgress(1.0, com.syndicati.utils.localization.LocalizationManager.getInstance().get("ready_to_enter"));
                            } catch (Exception e) {
                                javafx.application.Platform.runLater(() -> primaryStage.getScene().setRoot(loginView.getRoot()));
                            }
                        });
                    } else {
                        primaryStage.getScene().setRoot(loginView.getRoot());
                    }
                });
            });
        });

        // 8. Parallel Initialization
        Thread.startVirtualThread(() -> {
            try {
                com.syndicati.utils.database.ConnectionManager.getInstance().startMonitoring();
                langfuseRuntimeService.start();
                anomalyScoringScheduler.start();

                String savedUserId = com.syndicati.utils.shared.AppPreferences.getLocal("LOCAL_LOGGED_IN_USER_ID", null);
                if (savedUserId != null && !savedUserId.trim().isEmpty()) {
                    int uid = Integer.parseInt(savedUserId.trim());
                    new com.syndicati.models.user.data.UserRepository().findById(uid).ifPresent(u -> {
                        recoveryUserRef.set(u);
                        sessionCheckFuture.complete(true);
                    });
                }
                if (!sessionCheckFuture.isDone()) sessionCheckFuture.complete(false);

                LogAIWorkerService.getInstance();
                InsightFaceService.getInstance().initialize();
                DiscordRPCService.getInstance().initialize();
                MessagingSocketServer.getInstance().start();
                SentimentAnalysisService.startMicroservice();
                
                activityLogController.logPageView("app_startup", "Application Startup", java.util.Map.of(
                    "source", "main_application",
                    "langfuse_enabled", String.valueOf(langfuseRuntimeService.isEnabled())
                ));
            } catch (Exception e) {
                sessionCheckFuture.complete(false);
            }
        });

        // 9. Shutdown Hook
        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(() -> {
            LogAIWorkerService.getInstance().stopWorker();
            com.syndicati.services.ai.AgentService.shutdown();
            SentimentAnalysisService.stopMicroservice();
            com.syndicati.services.mail.AsyncMailerService.shutdown();
            com.syndicati.utils.database.ConnectionManager.getInstance().shutdown();
            com.syndicati.services.DatabaseService.getInstance().shutdown();
            langfuseRuntimeService.stop();
            anomalyScoringScheduler.stop();
            DiscordRPCService.getInstance().shutdown();
        }));

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Prevent immediate close
            
            System.out.println("[SHUTDOWN] Starting cinematic shutdown...");
            
            // Log shutdown before stopping services so tracer is still live.
            activityLogController.logPageView("app_shutdown", "Application Shutdown Cinematic", java.util.Map.of(
                "source", "close_request"
            ));

            com.syndicati.views.frontend.auth.ShutdownCinematicView shutdownView = new com.syndicati.views.frontend.auth.ShutdownCinematicView();
            primaryStage.getScene().setRoot(shutdownView.getRoot());
            
            shutdownView.setOnFinished(() -> {
                System.out.println("[SHUTDOWN] Finalizing service cleanup...");
                
                LogAIWorkerService.getInstance().stopWorker();
                com.syndicati.services.ai.AgentService.getInstance().stopPythonWorker();
                SentimentAnalysisService.stopMicroservice();
                com.syndicati.services.mail.AsyncMailerService.shutdown();
                
                com.syndicati.utils.database.ConnectionManager.getInstance().shutdown();
                langfuseRuntimeService.stop();
                anomalyScoringScheduler.stop();
                DiscordRPCService.getInstance().shutdown();
                
                System.out.println("[SUCCESS] All services stopped. Exiting.");
                
                // Final exit
                javafx.application.Platform.exit();
                System.exit(0);
            });
            
            shutdownView.play();
        });
        
        // Final UI performance hints
        primaryStage.getScene().getRoot().setCache(true);
        primaryStage.getScene().getRoot().setCacheHint(javafx.scene.CacheHint.SPEED);
        
        System.out.println("[SUCCESS] Syndicati started!");
        System.out.println("[INFO] Login page loaded - use admin/admin to login");
        
        // Login success is now event-driven from LoginView for immediate navigation.
    }
    
    private void setupLoginMonitoring() {
        // Stop any existing login checker first
        if (loginChecker != null) {
            loginChecker.stop();
        }
        
        // Create a timeline to check for successful login
        loginChecker = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> {
                if (loginView != null && loginView.isLoginSuccessful()) {
                    navigateToLandingPage();
                }
            })
        );
        loginChecker.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        loginChecker.play();
    }
    

    private void showLoadingOverlayAndRun(Runnable action) {
        javafx.scene.Node rootNode = primaryStage.getScene().getRoot();
        if (rootNode instanceof javafx.scene.layout.StackPane) {
            javafx.scene.layout.StackPane root = (javafx.scene.layout.StackPane) rootNode;
            
            javafx.scene.layout.VBox overlay = new javafx.scene.layout.VBox(16);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
            
            javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
            spinner.setStyle("-fx-progress-color: " + ThemeManager.getInstance().getAccentHex() + ";");
            spinner.setMaxSize(40, 40);
            
            javafx.scene.text.Text text = new javafx.scene.text.Text("Preparing Workspace...");
            text.setFont(javafx.scene.text.Font.font(boldFontFamily, javafx.scene.text.FontWeight.BOLD, 16));
            text.setFill(javafx.scene.paint.Color.WHITE);
            
            overlay.getChildren().addAll(spinner, text);
            overlay.setOpacity(0.0);
            
            root.getChildren().add(overlay);
            
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), overlay);
            ft.setToValue(1.0);
            ft.setOnFinished(e -> {
                // Short pause to ensure JavaFX renders the overlay before blocking the thread
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
                pt.setOnFinished(evt -> action.run());
                pt.play();
            });
            ft.play();
        } else {
            action.run();
        }
    }

    private void navigateToLandingPage() {
        if (isLoggedIn) return; // Prevent multiple navigations

        isLoggedIn = true;
        System.out.println("[INFO] Login successful. Navigating to landing page...");

        // Store current window size and position before switching
        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();
        double currentX = primaryStage.getX();
        double currentY = primaryStage.getY();
        boolean wasMaximized = primaryStage.isMaximized();

        // STOP the login monitoring animation FIRST
        if (loginChecker != null) {
            loginChecker.stop();
            loginChecker = null;
        }

        showLoadingOverlayAndRun(() -> {
            // Use the new Unified Recovery View for a premium transition
            com.syndicati.views.frontend.auth.SessionRecoveryView recoveryView = new com.syndicati.views.frontend.auth.SessionRecoveryView();
            
            // IMMEDIATELY start warm-starting the LandingPageView as soon as the recovery screen appears.
            javafx.application.Platform.runLater(() -> {
                if (landingPageView == null) {
                    System.out.println("[INFO] Immediate warm-start of LandingPageView initiated...");
                    landingPageView = new com.syndicati.views.frontend.home.LandingPageView();
                    
                    // PRE-INITIALIZE Forum and Events views during the idle intro period
                    // This moves their heavy UI construction off the navigation path.
                    com.syndicati.utils.navigation.NavigationManager nm = com.syndicati.utils.navigation.NavigationManager.getInstance();
                    com.syndicati.interfaces.ViewInterface forumView = nm.getView("forum");
                    com.syndicati.interfaces.ViewInterface eventsView = nm.getView("events");
                    
                    // Trigger early data hydration so they are ready immediately
                    if (forumView != null) forumView.loadDataAsync();
                    if (eventsView != null) eventsView.loadDataAsync();
                }
            });

            recoveryView.setOnGoHome(() -> {
                if (landingPageView == null) {
                    // Fallback if click happens before warmup finished
                    landingPageView = new com.syndicati.views.frontend.home.LandingPageView();
                }
                javafx.application.Platform.runLater(() -> {
                    showLandingPage(currentWidth, currentHeight, currentX, currentY, wasMaximized, false);
                    recoveryView.cleanup();
                });
            });
            
            recoveryView.setOnGoDashboard(() -> {
                if (landingPageView == null) {
                    landingPageView = new com.syndicati.views.frontend.home.LandingPageView();
                }
                javafx.application.Platform.runLater(() -> {
                    showLandingPage(currentWidth, currentHeight, currentX, currentY, wasMaximized, true);
                    recoveryView.cleanup();
                });
            });

            primaryStage.getScene().setRoot(recoveryView.getRoot());
            
            // Background sync (ensure profile and metadata are warm in memory)
            Thread.startVirtualThread(() -> {
                try {
                    User user = SessionManager.getInstance().getCurrentUser();
                    com.syndicati.utils.session.SessionManager sm = com.syndicati.utils.session.SessionManager.getInstance();
                    
                    recoveryView.setProgress(0.2, "Syncing identity...");
                    
                    // 1. Fetch Profile (Critical)
                    if (sm.getCurrentProfile() == null) {
                        sm.setCurrentProfile(new com.syndicati.controllers.user.profile.ProfileController().profileByUserId(user.getIdUser()).orElse(null));
                    }
                    
                    recoveryView.setProgress(0.5, com.syndicati.utils.localization.LocalizationManager.getInstance().get("warming_workspace"));
                    
                    // 2. Pre-load Service Data (Forum & Events)
                    // This populates the repository caches during the intro sequence
                    PublicationController pubController = new PublicationController();
                    EvenementController eventController = new EvenementController();
                    
                    recoveryView.setProgress(0.7, com.syndicati.utils.localization.LocalizationManager.getInstance().get("syncing_community"));
                    pubController.publicationsByCategory("General");
                    pubController.publicationsByCategory("Announcement");
                    
                    recoveryView.setProgress(0.85, com.syndicati.utils.localization.LocalizationManager.getInstance().get("fetching_events"));
                    eventController.evenements();
                    
                    // 3. Refresh local cache for Dashboard data
                    com.syndicati.services.DatabaseService.getInstance().getCache("warmup"); 
                    
                    recoveryView.updateUser(user);
                    recoveryView.setProgress(1.0, com.syndicati.utils.localization.LocalizationManager.getInstance().get("ready_to_enter"));
                } catch (Exception e) {
                    System.err.println("[ERROR] Preloading failed: " + e.getMessage());
                    javafx.application.Platform.runLater(() -> showLandingPage(currentWidth, currentHeight, currentX, currentY, wasMaximized, false));
                }
            });
        });
        System.out.println("[OK] Successfully initiated navigation to landing page.");
    }


    private void showLandingPage(
        double currentWidth,
        double currentHeight,
        double currentX,
        double currentY,
        boolean wasMaximized,
        boolean goToDashboard
    ) {
        if (loginView != null) {
            loginView.cleanup();
            loginView = null;
        }

        if (landingPageView != null) {
            landingPageView.cleanup();
            landingPageView = null;
        }

        // If already warm, we just use the instance, otherwise create it
        if (landingPageView == null) {
            landingPageView = new com.syndicati.views.frontend.home.LandingPageView();
        }

        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setViews(landingPageView);

        StackPane globalRoot = new StackPane(landingPageView.getRoot());
        Scene scene = new Scene(globalRoot);
        com.syndicati.components.shared.FloatingActionButtons.attachTo(globalRoot);
        
        scene.setFill(Color.BLACK); // Keep non-transparent app background
        scene.getStylesheets().clear(); // Clear any inherited styles
        applyGlobalStyles(scene);
        if (landingPageView.getRoot() != null) {
            appendRootStyle(landingPageView.getRoot(), "-fx-font-family: '" + lightFontFamily + "';");
        }

        primaryStage.setMinWidth(1500);
        primaryStage.setMinHeight(800);

        ThemeManager.getInstance().setScene(scene);
        primaryStage.setScene(scene);

        if (wasMaximized) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setWidth(currentWidth);
            primaryStage.setHeight(currentHeight);
            primaryStage.setX(currentX);
            primaryStage.setY(currentY);
        }

        primaryStage.setTitle("Syndicati - Dashboard");

        addResizeHandlers(primaryStage, scene);
        applyRoundedShape(scene);
        primaryStage.show();

        activityLogController.logPageView(goToDashboard ? "admin_dashboard" : "landing_dashboard", "Dashboard", java.util.Map.of(
            "source", "scene_switch",
            "dashboard_mode", goToDashboard ? "admin" : "community"
        ));

        // Let the scene render once before initializing heavy subcomponents
        javafx.application.Platform.runLater(() -> {
            landingPageView.getRoot().layout();
            primaryStage.sizeToScene();
            
            // Execute heavy dashboard/home loading
            if (goToDashboard) {
                landingPageView.enterDashboardMode();
                DiscordRPCService.getInstance().updatePresence("Admin Dashboard", "Managing Syndicati");
            } else {
                landingPageView.navigateToHome();
                DiscordRPCService.getInstance().updatePresence("Community Portal", "Browsing Home");
            }
            NavigationManager.getInstance().warmup();
        });
    }

    private void loadCustomFonts() {
        try {
            // Load ClashGrotesk-Bold for titles
            java.io.InputStream boldStream = MainApplication.class.getResourceAsStream("/ClashGrotesk-Bold.otf");
            if (boldStream != null) {
                Font boldFont = Font.loadFont(boldStream, 14);
                if (boldFont != null) {
                    boldFontFamily = boldFont.getFamily();
                    System.out.println("[INFO] Loaded bold font: " + boldFont.getName() + " (family: " + boldFontFamily + ")");
                } else {
                    System.out.println("[WARN] Failed to load ClashGrotesk-Bold.otf font - using default family name.");
                }
                boldStream.close();
            } else {
                System.out.println("[WARN] ClashGrotesk-Bold.otf not found on classpath.");
            }
            
            // Load Archivo-Regular for body text (primary)
            java.io.InputStream regularPrimaryStream = MainApplication.class.getResourceAsStream("/Archivo-Regular.ttf");
            if (regularPrimaryStream != null) {
                Font regularPrimaryFont = Font.loadFont(regularPrimaryStream, 14);
                if (regularPrimaryFont != null) {
                    lightFontFamily = regularPrimaryFont.getFamily();
                    System.out.println("[INFO] Loaded body font (regular): " + regularPrimaryFont.getName() + " (family: " + lightFontFamily + ")");
                } else {
                    System.out.println("[WARN] Failed to load Archivo-Regular.ttf font - trying Archivo-Light.");
                }
                regularPrimaryStream.close();
            } else {
                System.out.println("[WARN] Archivo-Regular.ttf not found on classpath - trying Archivo-Light.");
            }

            // Fallback: Archivo-Regular
            if ("Clash Grotesk".equals(lightFontFamily)) {
                java.io.InputStream archivoRegularStream = MainApplication.class.getResourceAsStream("/Archivo-Regular.ttf");
                if (archivoRegularStream != null) {
                    Font archivoRegular = Font.loadFont(archivoRegularStream, 14);
                    if (archivoRegular != null) {
                        lightFontFamily = archivoRegular.getFamily();
                        System.out.println("[INFO] Loaded body font (regular): " + archivoRegular.getName() + " (family: " + lightFontFamily + ")");
                    } else {
                        System.out.println("[WARN] Failed to load Archivo-Regular.ttf font - trying Archivo-Light.");
                    }
                    archivoRegularStream.close();
                } else {
                    System.out.println("[WARN] Archivo-Regular.ttf not found on classpath - trying Archivo-Light.");
                }
            }

            // Fallback: Archivo-Light
            if ("Clash Grotesk".equals(lightFontFamily)) {
                java.io.InputStream archivoLightStream = MainApplication.class.getResourceAsStream("/Archivo-Light.ttf");
                if (archivoLightStream != null) {
                    Font archivoLight = Font.loadFont(archivoLightStream, 14);
                    if (archivoLight != null) {
                        lightFontFamily = archivoLight.getFamily();
                        System.out.println("[INFO] Loaded body font (light): " + archivoLight.getName() + " (family: " + lightFontFamily + ")");
                    } else {
                        System.out.println("[WARN] Failed to load Archivo-Light.ttf font - trying Clash fallback.");
                    }
                    archivoLightStream.close();
                } else {
                    System.out.println("[WARN] Archivo-Light.ttf not found on classpath - trying Clash fallback.");
                }
            }

            // Fallback: ClashGrotesk-Regular
            if ("Clash Grotesk".equals(lightFontFamily)) {
                java.io.InputStream regularStream = MainApplication.class.getResourceAsStream("/ClashGrotesk-Regular.ttf");
                if (regularStream != null) {
                    Font regularFont = Font.loadFont(regularStream, 14);
                    if (regularFont != null) {
                        lightFontFamily = regularFont.getFamily();
                        System.out.println("[INFO] Loaded regular font: " + regularFont.getName() + " (family: " + lightFontFamily + ")");
                    } else {
                        System.out.println("[WARN] Failed to load ClashGrotesk-Regular.ttf font - trying light font.");
                    }
                    regularStream.close();
                } else {
                    System.out.println("[WARN] ClashGrotesk-Regular.ttf not found on classpath - trying light font.");
                }
            }

            // Final fallback: old ClashGrotesk-Light
            if ("Clash Grotesk".equals(lightFontFamily)) {
                java.io.InputStream lightStream = MainApplication.class.getResourceAsStream("/ClashGrotesk-Light.otf");
                if (lightStream != null) {
                    Font lightFont = Font.loadFont(lightStream, 14);
                    if (lightFont != null) {
                        lightFontFamily = lightFont.getFamily();
                        System.out.println("[INFO] Loaded light fallback font: " + lightFont.getName() + " (family: " + lightFontFamily + ")");
                    } else {
                        System.out.println("[WARN] Failed to load ClashGrotesk-Light.otf font - using default family name.");
                    }
                    lightStream.close();
                } else {
                    System.out.println("[WARN] ClashGrotesk-Light.otf not found on classpath.");
                }
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Error loading custom fonts: " + ex.getMessage());
        }
    }
    
    public String getBoldFontFamily() {
        return boldFontFamily;
    }
    
    public String getLightFontFamily() {
        return lightFontFamily;
    }
    
    public void logout() {
        System.out.println("[INFO] Logging out - returning to login page...");
                // Clear user session
        com.syndicati.utils.session.SessionManager.getInstance().clear();
                // Store current window size and position before switching
        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();
        double currentX = primaryStage.getX();
        double currentY = primaryStage.getY();
        boolean wasMaximized = primaryStage.isMaximized();
        
        // Reset login state
        isLoggedIn = false;
        
        // Create new login view
        loginView = new LoginView();
        loginView.setOnLoginSuccess(this::navigateToLandingPage);
        
        // Explicitly set the scene size to the current window size to prevent shrinking
        Scene scene = new Scene(loginView.getRoot(), currentWidth, currentHeight);
        scene.setFill(Color.BLACK); 
        scene.getStylesheets().clear(); 
        applyGlobalStyles(scene);
        
        // Enforce minimum window size (consistent with start())
        primaryStage.setMinWidth(1500);
        primaryStage.setMinHeight(800);
        
        ThemeManager.getInstance().setScene(scene);
        primaryStage.setScene(scene);
        
        // Re-apply rounded corners for the transparent stage
        applyRoundedShape(scene);
        addResizeHandlers(primaryStage, scene);
        
        // Restore window size and position
        if (wasMaximized) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setWidth(Math.max(1500, currentWidth));
            primaryStage.setHeight(Math.max(800, currentHeight));
            primaryStage.setX(currentX);
            primaryStage.setY(currentY);
        }
        
        primaryStage.setTitle("Syndicati - Login");
        
        // Add corner resize functionality for login page
        addResizeHandlers(primaryStage, scene);
        
        // Reapply rounded window clip for the login scene after logout
        applyRoundedShape(scene);
        
        // Show stage
        primaryStage.show();

        activityLogController.logPageView("login_page", "Login Page", java.util.Map.of(
            "source", "logout"
        ));
        DiscordRPCService.getInstance().updatePresence("Authentication", "Signing Out");
        
        // Avoid forcing additional size/layout passes here; media-backed backgrounds
        // are initialized asynchronously and can be disrupted by immediate re-scaling.
        
        // Clean up landing page view
        if (landingPageView != null) {
            landingPageView.cleanup();
            landingPageView = null;
        }
        
        System.out.println("[OK] Successfully returned to login page.");
    }
    
    public static MainApplication getInstance() {
        return instance;
    }

    private void appendRootStyle(javafx.scene.Parent root, String styleChunk) {
        String existing = root.getStyle();
        if (existing == null) {
            existing = "";
        }
        root.setStyle(existing + styleChunk);
    }

    private void applyGlobalStyles(Scene scene) {
        java.net.URL cssUrl = MainApplication.class.getResource(GLOBAL_SCROLLBAR_CSS);
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        } else {
            System.out.println("[WARN] Global stylesheet not found: " + GLOBAL_SCROLLBAR_CSS);
        }
    }
    
    private void centerStageOnScreen(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
    
    private void applyRoundedShape(Scene scene) {
        updateWindowClip(scene, primaryStage != null && primaryStage.isMaximized());
    }

    private void installWindowChromeListener() {
        if (windowChromeListenerInstalled || primaryStage == null) {
            return;
        }

        primaryStage.maximizedProperty().addListener((observable, oldValue, maximized) -> {
            updateWindowClip(primaryStage.getScene(), maximized);
        });
        windowChromeListenerInstalled = true;
    }

    private void updateWindowClip(Scene scene, boolean maximized) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        if (maximized) {
            scene.getRoot().setClip(null);
            return;
        }

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.widthProperty().bind(scene.widthProperty());
        clip.heightProperty().bind(scene.heightProperty());
        scene.getRoot().setClip(clip);
    }
    
    private void addResizeHandlers(Stage stage, Scene scene) {
        final double RESIZE_MARGIN = 8; // Pixel margin for resize detection
        final double WINDOW_BAR_HEIGHT = 40; // Height of custom window bar to exclude from resize
        final double[] resizeStartX = {0};
        final double[] resizeStartY = {0};
        final double[] resizeStartWidth = {0};
        final double[] resizeStartHeight = {0};
        final String[] resizeDirection = {""};
        
        scene.setOnMouseMoved(e -> {
            if (stage.isMaximized()) return;
            
            double mouseX = e.getSceneX();
            double mouseY = e.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();
            
            // Skip if mouse is over window bar (top 40px center area) to allow dragging
            if (mouseY < WINDOW_BAR_HEIGHT && mouseX > RESIZE_MARGIN && mouseX < width - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
                return;
            }
            
            // Detect which edge/corner
            boolean left = mouseX < RESIZE_MARGIN;
            boolean right = mouseX > width - RESIZE_MARGIN;
            boolean top = mouseY < RESIZE_MARGIN;
            boolean bottom = mouseY > height - RESIZE_MARGIN;
            
            // Set cursor based on position
            if (top && left) {
                scene.setCursor(javafx.scene.Cursor.NW_RESIZE);
            } else if (top && right) {
                scene.setCursor(javafx.scene.Cursor.NE_RESIZE);
            } else if (bottom && left) {
                scene.setCursor(javafx.scene.Cursor.SW_RESIZE);
            } else if (bottom && right) {
                scene.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else if (top) {
                scene.setCursor(javafx.scene.Cursor.N_RESIZE);
            } else if (bottom) {
                scene.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else if (left) {
                scene.setCursor(javafx.scene.Cursor.W_RESIZE);
            } else if (right) {
                scene.setCursor(javafx.scene.Cursor.E_RESIZE);
            } else {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
        
        scene.setOnMousePressed(e -> {
            if (stage.isMaximized()) return;
            
            double mouseX = e.getSceneX();
            double mouseY = e.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();
            
            // Skip if mouse is over window bar (top 40px center area)
            if (mouseY < WINDOW_BAR_HEIGHT && mouseX > RESIZE_MARGIN && mouseX < width - RESIZE_MARGIN) {
                resizeDirection[0] = "";
                return;
            }
            
            resizeStartX[0] = e.getScreenX();
            resizeStartY[0] = e.getScreenY();
            resizeStartWidth[0] = stage.getWidth();
            resizeStartHeight[0] = stage.getHeight();
            
            // Detect which edge/corner
            boolean left = mouseX < RESIZE_MARGIN;
            boolean right = mouseX > width - RESIZE_MARGIN;
            boolean top = mouseY < RESIZE_MARGIN;
            boolean bottom = mouseY > height - RESIZE_MARGIN;
            
            if (top && left) {
                resizeDirection[0] = "NW";
            } else if (top && right) {
                resizeDirection[0] = "NE";
            } else if (bottom && left) {
                resizeDirection[0] = "SW";
            } else if (bottom && right) {
                resizeDirection[0] = "SE";
            } else if (top) {
                resizeDirection[0] = "N";
            } else if (bottom) {
                resizeDirection[0] = "S";
            } else if (left) {
                resizeDirection[0] = "W";
            } else if (right) {
                resizeDirection[0] = "E";
            } else {
                resizeDirection[0] = "";
            }
        });
        
        scene.setOnMouseDragged(e -> {
            if (stage.isMaximized() || resizeDirection[0].isEmpty()) return;
            
            double deltaX = e.getScreenX() - resizeStartX[0];
            double deltaY = e.getScreenY() - resizeStartY[0];
            
            String dir = resizeDirection[0];
            
            // Handle horizontal resizing
            if (dir.contains("W")) {
                double newWidth = resizeStartWidth[0] - deltaX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setX(e.getScreenX());
                    stage.setWidth(newWidth);
                }
            } else if (dir.contains("E")) {
                double newWidth = resizeStartWidth[0] + deltaX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setWidth(newWidth);
                }
            }
            
            // Handle vertical resizing
            if (dir.contains("N")) {
                double newHeight = resizeStartHeight[0] - deltaY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setY(e.getScreenY());
                    stage.setHeight(newHeight);
                }
            } else if (dir.contains("S")) {
                double newHeight = resizeStartHeight[0] + deltaY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setHeight(newHeight);
                }
            }
        });
    }
    
    /**
     * Instantly refreshes the entire application UI.
     * Used for language switching (RTL) and global theme updates.
     */
    public void refreshAppUI() {
        if (landingPageView == null) return;
        
        String savedPage = landingPageView.getCurrentPageName();

        javafx.application.Platform.runLater(() -> {
            // CRITICAL: Clear cached views in NavigationManager so they are re-created with new translations
            // We pass FALSE to skip re-instantiating the heavy ProfileView to prevent UI freezes
            com.syndicati.utils.navigation.NavigationManager nm = com.syndicati.utils.navigation.NavigationManager.getInstance();
            nm.clearAllViews(false);
            
            // Re-instantiate the landing page to pick up new translations and orientation
            landingPageView = new com.syndicati.views.frontend.home.LandingPageView(savedPage);
            
            // Update the NavigationManager to use the new view instance
            nm.setViews(landingPageView);
            
            // Re-trigger warmup to ensure profile data is fresh and heavy views are pre-loaded
            nm.warmup();
            
            javafx.scene.Scene newScene = new javafx.scene.Scene(landingPageView.getRoot(), 1500, 800);
            newScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            
            // Re-apply the premium scrollbar styling to the new scene
            newScene.getStylesheets().add(getClass().getResource("/styles/app-scrollbar.css").toExternalForm());
            
            // Apply RTL if needed
            com.syndicati.utils.localization.LocalizationManager.getInstance().applyOrientation(landingPageView.getRoot());
            
            primaryStage.setScene(newScene);
            applyRoundedShape(newScene);
            addResizeHandlers(primaryStage, newScene);
            
            // Restore the page we were on
            landingPageView.navigateToPage(savedPage);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}


