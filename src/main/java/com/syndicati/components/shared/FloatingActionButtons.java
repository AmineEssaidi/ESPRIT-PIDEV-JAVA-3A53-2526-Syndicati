package com.syndicati.components.shared;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import com.syndicati.services.ai.AgentService;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.input.KeyCode;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.shape.Circle;

/**
 * Global Floating Action Buttons - Horizon Premium Styling.
 */
public class FloatingActionButtons {

    private static VBox container;
    private static VBox agentPopup;
    private static VBox msgPopup;

    public static void attachTo(StackPane root) {
        if (root == null) return;
        
        if (container == null) {
            createUI();
        }

        if (container.getParent() != null) ((Pane) container.getParent()).getChildren().remove(container);
        if (agentPopup.getParent() != null) ((Pane) agentPopup.getParent()).getChildren().remove(agentPopup);
        if (msgPopup.getParent() != null) ((Pane) msgPopup.getParent()).getChildren().remove(msgPopup);

        root.getChildren().addAll(agentPopup, msgPopup, container);
    }

    private static void createUI() {
        container = new VBox(15);
        container.setAlignment(Pos.BOTTOM_RIGHT);
        container.setPadding(new Insets(30));
        container.setPickOnBounds(false);

        Button btnAgent = createHorizonButton("🤖", "rgba(10, 10, 10, 0.8)");
        Button btnMessages = createHorizonButton("💬", "rgba(10, 10, 10, 0.8)");

        agentPopup = createAgentPopupShell();
        msgPopup = createMessagePopupShell();

        btnAgent.setOnAction(e -> {
            agentPopup.setVisible(!agentPopup.isVisible());
            msgPopup.setVisible(false);
            if (agentPopup.isVisible()) agentPopup.toFront();
        });

        btnMessages.setOnAction(e -> {
            msgPopup.setVisible(!msgPopup.isVisible());
            agentPopup.setVisible(false);
            if (msgPopup.isVisible()) msgPopup.toFront();
        });

        container.getChildren().addAll(btnMessages, btnAgent);
    }

    private static Button createHorizonButton(String icon, String color) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-background-color: " + color + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 32px;" +
                    "-fx-background-radius: 50%;" +
                    "-fx-min-width: 64;" +
                    "-fx-min-height: 64;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                    "-fx-border-radius: 50%;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;");
        
        DropShadow ds = new DropShadow(35, Color.rgb(0, 0, 0, 0.4));
        ds.setOffsetY(15);
        btn.setEffect(ds);
        
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-scale-x: 1.1; -fx-scale-y: 1.1; -fx-border-color: rgba(255, 255, 255, 0.3);"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-scale-x: 1.1; -fx-scale-y: 1.1; -fx-border-color: rgba(255, 255, 255, 0.3);", "")));
        
        return btn;
    }

    private static VBox createAgentPopupShell() {
        VBox panel = new VBox();
        panel.getStylesheets().add(FloatingActionButtons.class.getResource("/styles/agent-popup.css").toExternalForm());
        panel.getStyleClass().add("horizon-panel");
        panel.setPrefSize(400, 600);
        panel.setMaxSize(400, 600);
        panel.setVisible(false);
        StackPane.setAlignment(panel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(panel, new Insets(0, 30, 110, 0));

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("horizon-header");
        Text title = new Text("🤖 Syndicati Agent");
        title.getStyleClass().add("horizon-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("×");
        close.getStyleClass().add("horizon-close");
        close.setOnAction(e -> panel.setVisible(false));
        header.getChildren().addAll(title, spacer, close);

        // Tabs
        HBox tabs = new HBox();
        tabs.getStyleClass().add("horizon-tabs");
        Button tabChat = new Button("Chat");
        Button tabAgent = new Button("Agent");
        tabChat.getStyleClass().addAll("horizon-tab", "active");
        tabAgent.getStyleClass().addAll("horizon-tab");
        HBox.setHgrow(tabChat, Priority.ALWAYS);
        HBox.setHgrow(tabAgent, Priority.ALWAYS);
        tabChat.setMaxWidth(Double.MAX_VALUE);
        tabAgent.setMaxWidth(Double.MAX_VALUE);
        tabs.getChildren().addAll(tabChat, tabAgent);

        VBox chatContent = new VBox(15);
        chatContent.getStyleClass().add("horizon-content");
        VBox agentContent = new VBox(15);
        agentContent.getStyleClass().add("horizon-content");
        agentContent.setVisible(false);
        agentContent.setManaged(false);

        tabChat.setOnAction(e -> {
            tabChat.getStyleClass().add("active");
            tabAgent.getStyleClass().remove("active");
            chatContent.setVisible(true);
            chatContent.setManaged(true);
            agentContent.setVisible(false);
            agentContent.setManaged(false);
        });

        tabAgent.setOnAction(e -> {
            tabChat.getStyleClass().remove("active");
            tabAgent.getStyleClass().add("active");
            chatContent.setVisible(false);
            chatContent.setManaged(false);
            agentContent.setVisible(true);
            agentContent.setManaged(true);
        });

        // Chat View
        VBox chatMessages = new VBox(16);
        chatMessages.setPadding(new Insets(10, 0, 10, 0));
        ScrollPane chatScroll = new ScrollPane(chatMessages);
        chatScroll.getStyleClass().add("horizon-scroll");
        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        // Welcome Message
        addMessage(chatMessages, "Agent", "Hello! I'm your Syndicati Smart Assistant. Ask me anything about the app, or I can research topics for you on the web. 🚀", false);

        HBox chatInputRow = new HBox(10);
        chatInputRow.getStyleClass().add("horizon-input-row");
        chatInputRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField chatInput = new TextField();
        chatInput.setPromptText("Ask anything or research...");
        chatInput.getStyleClass().add("horizon-input");
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        
        Button btnChatSend = new Button("Search");
        btnChatSend.getStyleClass().add("horizon-send");
        chatInputRow.getChildren().addAll(chatInput, btnChatSend);
        
        Runnable sendMessage = () -> {
            String txt = chatInput.getText().trim();
            if (txt.isEmpty()) return;
            
            addMessage(chatMessages, "User", txt, true);
            chatInput.clear();
            
            // Add a temporary "Thinking..." bubble
            VBox thinkingBox = new VBox();
            thinkingBox.getStyleClass().add("msg-assistant");
            Label thinkingLabel = new Label("Gemini is researching...");
            thinkingLabel.getStyleClass().add("msg-text");
            thinkingLabel.setStyle("-fx-opacity: 0.5; -fx-font-style: italic;");
            thinkingBox.getChildren().add(thinkingLabel);
            chatMessages.getChildren().add(thinkingBox);
            
            // Auto-scroll to bottom
            Platform.runLater(() -> chatScroll.setVvalue(1.0));

            AgentService.getInstance().chatAsync(txt).thenAccept(r -> Platform.runLater(() -> {
                chatMessages.getChildren().remove(thinkingBox);
                addMessage(chatMessages, "Agent", r, false);
                Platform.runLater(() -> chatScroll.setVvalue(1.0));
            }));
        };
        btnChatSend.setOnAction(e -> sendMessage.run());
        chatInput.setOnAction(e -> sendMessage.run());
        chatContent.getChildren().addAll(chatScroll, chatInputRow);

        // Agent View (Takeover)
        agentContent.setAlignment(Pos.TOP_LEFT);
        Text takeoverTitle = new Text("Your Objective");
        takeoverTitle.getStyleClass().add("horizon-label");
        
        TextArea agentGoal = new TextArea();
        agentGoal.setPromptText("e.g. I want to file a new reclamation...");
        agentGoal.getStyleClass().add("horizon-textarea");
        agentGoal.setPrefHeight(120);
        
        Button btnRun = new Button("Execute Action");
        btnRun.getStyleClass().add("horizon-go-btn");
        btnRun.setMaxWidth(Double.MAX_VALUE);

        // Advanced Status Area
        VBox statusBox = new VBox(12);
        statusBox.getStyleClass().add("horizon-status-container");
        statusBox.setPadding(new Insets(15));
        statusBox.setVisible(false);
        statusBox.setManaged(false);

        HBox scanningRow = new HBox(10);
        scanningRow.setAlignment(Pos.CENTER_LEFT);
        Circle pulse = new Circle(5, Color.web("#7850ff"));
        pulse.setEffect(new DropShadow(10, Color.web("#7850ff")));
        Label lblScanning = new Label("Syndicati Vision: Contextualizing...");
        lblScanning.getStyleClass().add("horizon-status-text");
        scanningRow.getChildren().addAll(pulse, lblScanning);

        // Animation for the pulse
        Timeline pulseAnim = new Timeline(
            new KeyFrame(Duration.ZERO, e -> pulse.setOpacity(1)),
            new KeyFrame(Duration.seconds(0.75), e -> pulse.setOpacity(0.3)),
            new KeyFrame(Duration.seconds(1.5), e -> pulse.setOpacity(1))
        );
        pulseAnim.setCycleCount(Timeline.INDEFINITE);

        VBox logContainer = new VBox(5);
        ScrollPane logScroll = new ScrollPane(logContainer);
        logScroll.getStylesheets().add(FloatingActionButtons.class.getResource("/styles/agent-popup.css").toExternalForm());
        logScroll.getStyleClass().add("horizon-log-scroll");
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(200);
        VBox.setVgrow(logScroll, Priority.ALWAYS);

        statusBox.getChildren().addAll(scanningRow, logScroll);
        
        Runnable runAgent = () -> {
            String cmd = agentGoal.getText();
            if (cmd.isEmpty()) return;
            
            statusBox.setVisible(true);
            statusBox.setManaged(true);
            logContainer.getChildren().clear();
            pulseAnim.play();
            
            addLog(logContainer, "🎯 Objective: " + cmd, "#ffffff");
            addLog(logContainer, "🔍 Scanning application state...", "#7850ff");
            
            AgentService.getInstance().takeoverAsync(cmd).thenAccept(r -> Platform.runLater(() -> {
                pulseAnim.stop();
                addLog(logContainer, "✨ Task execution finished.", "#22c55e");
                addLog(logContainer, "📝 Final Response: " + r, "#ffffff");
            }));
        };

        btnRun.setOnAction(e -> runAgent.run());
        agentGoal.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (e.isControlDown()) {
                    agentGoal.appendText("\n");
                } else {
                    runAgent.run();
                    e.consume();
                }
            }
        });

        agentContent.getChildren().addAll(takeoverTitle, agentGoal, btnRun, statusBox);

        StackPane contents = new StackPane(chatContent, agentContent);
        VBox.setVgrow(contents, Priority.ALWAYS);
        panel.getChildren().addAll(header, tabs, contents);
        return panel;
    }

    private static void addMessage(VBox container, String sender, String text, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(5, 0, 5, 0));
        
        VBox msgBox = new VBox(5);
        msgBox.getStyleClass().add(isUser ? "msg-user" : "msg-assistant");
        msgBox.setMaxWidth(300); // Prevent bubbles from being too wide
        
        Label lblText = new Label(text);
        lblText.getStyleClass().add("msg-text");
        lblText.setWrapText(true);
        lblText.setMaxWidth(280);
        
        msgBox.getChildren().add(lblText);
        wrapper.getChildren().add(msgBox);
        container.getChildren().add(wrapper);
    }

    private static void addLog(VBox container, String text, String colorHex) {
        Label log = new Label(text);
        log.getStyleClass().add("horizon-log-entry");
        log.setStyle("-fx-text-fill: " + colorHex + ";");
        log.setWrapText(true);
        container.getChildren().add(log);
        Platform.runLater(() -> {
            if (container.getParent() instanceof ScrollPane) {
                ((ScrollPane) container.getParent()).setVvalue(1.0);
            }
        });
    }

    private static VBox createMessagePopupShell() {
        VBox p = new VBox();
        p.getStylesheets().add(FloatingActionButtons.class.getResource("/styles/agent-popup.css").toExternalForm());
        p.getStyleClass().add("horizon-panel");
        p.setPrefSize(440, 680);
        p.setVisible(false);
        StackPane.setAlignment(p, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(p, new Insets(0, 30, 110, 0));
        
        HBox header = new HBox();
        header.getStyleClass().add("horizon-header");
        Text title = new Text("💬 Messaging");
        title.getStyleClass().add("horizon-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("×");
        close.getStyleClass().add("horizon-close");
        close.setOnAction(e -> p.setVisible(false));
        header.getChildren().addAll(title, spacer, close);
        
        p.getChildren().addAll(header, new Label("Messaging service is syncing...") {{ setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-padding: 40;"); }});
        return p;
    }
}
