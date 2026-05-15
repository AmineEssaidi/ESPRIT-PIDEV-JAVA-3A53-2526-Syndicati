package com.syndicati.components.shared;

import com.syndicati.services.ai.AgentService;
import com.syndicati.services.user.messaging.MessagingService;
import com.syndicati.services.user.relationship.UserRelationshipService;
import com.syndicati.services.user.user.UserService;
import com.syndicati.services.user.profile.ProfileService;
import com.syndicati.services.user.messaging.socket.MessagingSocketClient;
import com.syndicati.services.user.messaging.socket.SocketPayload;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.models.user.Conversation;
import com.syndicati.models.user.Message;
import com.syndicati.models.user.User;
import com.syndicati.models.user.Participant;
import com.syndicati.models.user.Profile;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.input.KeyCode;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.File;

/**
 * Global Floating Action Buttons - Horizon Premium Styling.
 */
public class FloatingActionButtons {

    private static VBox container;
    private static VBox agentPopup;
    private static VBox msgPopup;
    private static Integer msgPopupUserId;

    public static void attachTo(StackPane root) {
        if (root == null) return;
        
        if (container == null) {
            createUI();
        }

        Integer currentUserId = currentSessionUserId();
        if ((msgPopupUserId == null && currentUserId != null) || (msgPopupUserId != null && !msgPopupUserId.equals(currentUserId))) {
            if (msgPopup != null && msgPopup.getParent() != null) {
                ((Pane) msgPopup.getParent()).getChildren().remove(msgPopup);
            }
            MessagingSocketClient.getInstance().disconnect();
            msgPopup = createMessagePopupShell();
            msgPopupUserId = currentUserId;
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
        msgPopupUserId = currentSessionUserId();

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
        return new MessagingHub().getPanel();
    }

    private static Integer currentSessionUserId() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null ? user.getIdUser() : null;
    }

    private static class MessagingHub {
        private final VBox panel;
        private final StackPane viewContainer;
        private final VBox listContainer;
        private final VBox chatContainer;
        private final VBox selectionContainer;
        private final Text titleText;
        private final Button btnBack;
        
        private final MessagingService messagingService = MessagingService.getInstance();
        private final UserRelationshipService relationshipService = new UserRelationshipService();
        private final UserService userService = new UserService();
        private final ProfileService profileService = new ProfileService();
        private final User currentUser = SessionManager.getInstance().getCurrentUser();
        
        private Conversation currentConversation;
        private Integer activeRecipientId;
        private Timeline pollTimeline;

        public MessagingHub() {
            panel = new VBox();
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
            header.setAlignment(Pos.CENTER_LEFT);
            header.setSpacing(12);
            header.setPadding(new Insets(20, 24, 20, 24));
            
            btnBack = new Button("←");
            btnBack.getStyleClass().add("horizon-close");
            btnBack.setVisible(false);
            btnBack.setOnAction(e -> switchView("list"));

            titleText = new Text("💬 Messaging");
            titleText.getStyleClass().add("horizon-title");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button close = new Button("×");
            close.getStyleClass().add("horizon-close");
            close.setOnAction(e -> {
                stopPolling();
                panel.setVisible(false);
            });
            
            header.getChildren().addAll(btnBack, titleText, spacer, close);

            viewContainer = new StackPane();
            VBox.setVgrow(viewContainer, Priority.ALWAYS);

            listContainer = createListView();
            chatContainer = createChatView();
            selectionContainer = createSelectionView();

            viewContainer.getChildren().addAll(listContainer, chatContainer, selectionContainer);
            panel.getChildren().addAll(header, viewContainer);

            switchView("list");
            
            // Real-time socket integration
            initSocket();
        }

        private void initSocket() {
            if (currentUser == null) return;
            
            MessagingSocketClient client = MessagingSocketClient.getInstance();
            if (!client.isConnected()) {
                client.connect(currentUser.getIdUser());
            }
            
            client.addMessageListener(payload -> {
                if (payload.getType() == SocketPayload.Type.MESSAGE) {
                    Platform.runLater(() -> {
                        // If we are looking at the conversation list, refresh it
                        if (listContainer.isVisible()) {
                            loadConversations();
                        }
                        
                        // If we are in the chat view and it's the current conversation, refresh messages
                        if (chatContainer.isVisible() && currentConversation != null && 
                            payload.getConversationId() == currentConversation.getId()) {
                            refreshMessages();
                        }
                    });
                }
            });
        }

        public VBox getPanel() { return panel; }

        private void switchView(String viewName) {
            listContainer.setVisible("list".equals(viewName));
            chatContainer.setVisible("chat".equals(viewName));
            selectionContainer.setVisible("selection".equals(viewName));
            
            btnBack.setVisible(!"list".equals(viewName));
            if ("list".equals(viewName)) {
                titleText.setText("💬 Messaging");
                stopPolling();
                loadConversations();
            } else if ("selection".equals(viewName)) {
                titleText.setText("👤 Select Friend");
                stopPolling();
                loadFriends();
            }
        }

        private VBox createListView() {
            VBox view = new VBox(20);
            view.setPadding(new Insets(20));

            HBox actions = new HBox(10);
            Button btnNew = new Button("👤 Message");
            btnNew.getStyleClass().add("horizon-send");
            btnNew.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.1); -fx-font-size: 12px; -fx-padding: 8px 12px;");
            btnNew.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnNew, Priority.ALWAYS);
            btnNew.setOnAction(e -> switchView("selection"));

            Button btnGroup = new Button("👥 Group");
            btnGroup.getStyleClass().add("horizon-go-btn");
            btnGroup.setStyle("-fx-font-size: 12px; -fx-padding: 8px 12px;");
            btnGroup.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnGroup, Priority.ALWAYS);

            actions.getChildren().addAll(btnNew, btnGroup);

            VBox scrollContent = new VBox(10);
            ScrollPane scroll = new ScrollPane(scrollContent);
            scroll.getStyleClass().add("horizon-scroll");
            scroll.setFitToWidth(true);
            VBox.setVgrow(scroll, Priority.ALWAYS);

            view.getChildren().addAll(actions, scroll);
            return view;
        }

        private void loadConversations() {
            if (currentUser == null) return;
            VBox scrollContent = (VBox) ((ScrollPane) listContainer.getChildren().get(1)).getContent();
            scrollContent.getChildren().clear();
            
            List<Conversation> convs = messagingService.findUserConversations(currentUser.getIdUser());
            if (convs.isEmpty()) {
                Label empty = new Label("No active conversations.");
                empty.getStyleClass().add("conv-last");
                empty.setPadding(new Insets(40));
                scrollContent.getChildren().add(empty);
                return;
            }

            for (Conversation c : convs) {
                HBox item = new HBox(12);
                item.getStyleClass().add("conv-item");
                item.setAlignment(Pos.CENTER_LEFT);
                
                String name = c.getName();
                String avatarUrl = null;
                
                if (!c.isGroup()) {
                    List<Participant> parts = messagingService.findConversationParticipants(c.getId());
                    for (Participant p : parts) {
                        if (p.getUserId() != currentUser.getIdUser()) {
                            Optional<User> u = userService.findById(p.getUserId());
                            if (u.isPresent()) {
                                name = u.get().getFirstName() + " " + u.get().getLastName();
                                avatarUrl = profileService.findOneByUserId(u.get().getIdUser()).map(Profile::getAvatar).orElse(null);
                            }
                            break;
                        }
                    }
                }

                StackPane avatarFrame = createAvatarFrame(avatarUrl, c.isGroup());
                
                VBox info = new VBox(2);
                Label lblName = new Label(name);
                lblName.getStyleClass().add("conv-name");
                
                Optional<Message> last = messagingService.findLastMessage(c.getId());
                Label lblLast = new Label(last.map(m -> (m.getSenderId() == currentUser.getIdUser() ? "You: " : "") + m.getContent()).orElse("New secure channel"));
                lblLast.getStyleClass().add("conv-last");
                lblLast.setMaxWidth(280);
                
                info.getChildren().addAll(lblName, lblLast);
                item.getChildren().addAll(avatarFrame, info);
                
                String finalName = name;
                item.setOnMouseClicked(e -> openConversation(c, finalName));
                scrollContent.getChildren().add(item);
            }
        }

        private StackPane createAvatarFrame(String url, boolean isGroup) {
            StackPane frame = new StackPane();
            frame.getStyleClass().add("conv-avatar");
            
            if (isGroup) {
                Label icon = new Label("👥");
                icon.setStyle("-fx-font-size: 20px;");
                frame.getChildren().add(icon);
            } else if (url != null && !url.isEmpty()) {
                try {
                    // Convert relative path to absolute file URL for JavaFX
                    File file = new File(url);
                    String fileUrl = file.toURI().toString();
                    
                    ImageView img = new ImageView(new Image(fileUrl, true));
                    img.setFitWidth(48);
                    img.setFitHeight(48);
                    Circle clip = new Circle(24, 24, 24);
                    img.setClip(clip);
                    frame.getChildren().add(img);
                } catch (Exception e) {
                    Label lbl = new Label("👤");
                    lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.2);");
                    frame.getChildren().add(lbl);
                }
            } else {
                Label lbl = new Label("👤");
                lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.2);");
                frame.getChildren().add(lbl);
            }
            return frame;
        }

        private VBox createChatView() {
            VBox view = new VBox(15);
            view.setPadding(new Insets(0, 20, 20, 20));

            VBox messagesBox = new VBox(14);
            ScrollPane scroll = new ScrollPane(messagesBox);
            scroll.getStyleClass().add("horizon-scroll");
            scroll.setFitToWidth(true);
            VBox.setVgrow(scroll, Priority.ALWAYS);

            HBox inputArea = new HBox(10);
            inputArea.getStyleClass().add("horizon-input-row");
            TextField input = new TextField();
            input.getStyleClass().add("horizon-input");
            input.setPromptText("Type a message...");
            HBox.setHgrow(input, Priority.ALWAYS);
            
            Button btnSend = new Button("▶");
            btnSend.getStyleClass().add("horizon-send");
            
            inputArea.getChildren().addAll(input, btnSend);
            
            Runnable sendAction = () -> {
                String txt = input.getText().trim();
                if (txt.isEmpty()) return;
                
                int resultId = messagingService.sendMessage(currentUser.getIdUser(), txt, 
                    currentConversation != null ? currentConversation.getId() : null, 
                    activeRecipientId);
                
                if (resultId != -1) {
                    input.clear();
                    if (currentConversation == null) {
                        // We started a new conversation
                        currentConversation = new Conversation();
                        currentConversation.setId(resultId);
                        activeRecipientId = null;
                    }
                    refreshMessages();
                }
            };
            
            btnSend.setOnAction(e -> sendAction.run());
            input.setOnAction(e -> sendAction.run());

            view.getChildren().addAll(scroll, inputArea);
            return view;
        }

        private void openConversation(Conversation c, String name) {
            currentConversation = c;
            activeRecipientId = null;
            titleText.setText(name);
            switchView("chat");
            refreshMessages();
            startPolling();
        }

        private void refreshMessages() {
            if (currentConversation == null) return;
            VBox messagesBox = (VBox) ((ScrollPane) chatContainer.getChildren().get(0)).getContent();
            List<Message> msgs = messagingService.findConversationMessages(currentConversation.getId());
            
            Platform.runLater(() -> {
                messagesBox.getChildren().clear();
                for (Message m : msgs) {
                    boolean isMine = m.getSenderId() == currentUser.getIdUser();
                    HBox row = new HBox(8);
                    row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    
                    // Add small avatar for others in group chats or all chats
                    if (!isMine) {
                        String senderAvatar = profileService.findOneByUserId(m.getSenderId()).map(Profile::getAvatar).orElse(null);
                        StackPane smallAvatar = createAvatarFrame(senderAvatar, false);
                        smallAvatar.setPrefSize(32, 32);
                        smallAvatar.setMinSize(32, 32);
                        smallAvatar.setMaxSize(32, 32);
                        // Resize image inside
                        if (!smallAvatar.getChildren().isEmpty() && smallAvatar.getChildren().get(0) instanceof ImageView) {
                            ImageView iv = (ImageView) smallAvatar.getChildren().get(0);
                            iv.setFitWidth(32);
                            iv.setFitHeight(32);
                            ((Circle) iv.getClip()).setRadius(16);
                            ((Circle) iv.getClip()).setCenterX(16);
                            ((Circle) iv.getClip()).setCenterY(16);
                        }
                        row.getChildren().add(smallAvatar);
                    }

                    VBox bubble = new VBox();
                    bubble.getStyleClass().add(isMine ? "msg-mine" : "msg-theirs");
                    bubble.setMaxWidth(280);
                    
                    Label content = new Label(m.getContent());
                    content.getStyleClass().add("msg-text");
                    content.setWrapText(true);
                    
                    bubble.getChildren().add(content);
                    row.getChildren().add(bubble);
                    messagesBox.getChildren().add(row);
                }
                ((ScrollPane) chatContainer.getChildren().get(0)).setVvalue(1.0);
            });
        }

        private VBox createSelectionView() {
            VBox view = new VBox(15);
            view.setPadding(new Insets(20));

            TextField search = new TextField();
            search.getStyleClass().add("horizon-input");
            search.setPromptText("Search friends...");
            search.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 12; -fx-background-radius: 12;");

            VBox scrollContent = new VBox(10);
            ScrollPane scroll = new ScrollPane(scrollContent);
            scroll.getStyleClass().add("horizon-scroll");
            scroll.setFitToWidth(true);
            VBox.setVgrow(scroll, Priority.ALWAYS);

            view.getChildren().addAll(search, scroll);
            return view;
        }

        private void loadFriends() {
            VBox scrollContent = (VBox) ((ScrollPane) selectionContainer.getChildren().get(1)).getContent();
            scrollContent.getChildren().clear();
            
            List<User> friends = relationshipService.findFriends(currentUser, 0);
            for (User f : friends) {
                HBox card = new HBox(12);
                card.getStyleClass().add("friend-card");
                card.setAlignment(Pos.CENTER_LEFT);
                
                StackPane avatar = createAvatarFrame(profileService.findOneByUserId(f.getIdUser()).map(Profile::getAvatar).orElse(null), false);
                Label name = new Label(f.getFirstName() + " " + f.getLastName());
                name.getStyleClass().add("conv-name");
                
                card.getChildren().addAll(avatar, name);
                card.setOnMouseClicked(e -> {
                    currentConversation = null;
                    activeRecipientId = f.getIdUser();
                    titleText.setText(f.getFirstName() + " " + f.getLastName());
                    switchView("chat");
                    ((VBox) ((ScrollPane) chatContainer.getChildren().get(0)).getContent()).getChildren().clear();
                    startPolling();
                });
                scrollContent.getChildren().add(card);
            }
        }

        private void startPolling() {
            stopPolling();
            pollTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                if (!panel.isVisible() || !chatContainer.isVisible()) {
                    return;
                }
                if (currentConversation != null) {
                    refreshMessages();
                } else {
                    loadConversations();
                }
            }));
            pollTimeline.setCycleCount(Timeline.INDEFINITE);
            pollTimeline.play();
        }

        private void stopPolling() {
            if (pollTimeline != null) {
                pollTimeline.stop();
                pollTimeline = null;
            }
        }
    }
}
