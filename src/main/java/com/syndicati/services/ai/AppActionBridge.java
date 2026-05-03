package com.syndicati.services.ai;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Window;
import com.syndicati.utils.navigation.NavigationManager;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * DesktopPage - A Playwright-inspired API for native JavaFX automation.
 */
public class AppActionBridge {

    public static class DesktopPage {
        public void navigate(String route) {
            Platform.runLater(() -> NavigationManager.getInstance().navigateTo(resolveRoute(route)));
        }

        private String resolveRoute(String route) {
            if (route == null) return "home";
            switch (route.toLowerCase().trim()) {
                case "syndicat":
                case "syndicats":
                case "syndic":
                    return "services/syndicat";
                case "forum":
                case "forums":
                    return "services/forum";
                case "residence":
                case "residences":
                    return "services/residence";
                case "evenement":
                case "evenements":
                case "event":
                case "events":
                    return "services/evenement";
                default:
                    return route;
            }
        }

        public void click(String selector) {
            Platform.runLater(() -> {
                Optional<Node> target = findNode(selector, "CLICK");
                target.ifPresent(node -> {
                    highlight(node);
                    // Use a small delay for visual feedback before firing
                    new Timeline(new KeyFrame(Duration.millis(400), e -> {
                        Platform.runLater(() -> {
                            if (node instanceof Button) ((Button) node).fire();
                            else if (node instanceof Hyperlink) ((Hyperlink) node).fire();
                            else {
                                // Fallback: simulate a generic mouse click for Panes, Labels, etc.
                                node.fireEvent(new javafx.scene.input.MouseEvent(
                                    javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                                    javafx.scene.input.MouseButton.PRIMARY, 1,
                                    false, false, false, false, true, false, false, true, false, false, null
                                ));
                            }
                        });
                    })).play();
                });
            });
        }

        public void fill(String selector, String value) {
            Platform.runLater(() -> {
                Optional<Node> target = findNode(selector, "FILL");
                target.ifPresent(node -> {
                    Node inputNode = node;
                    // Smart lookup: if we found a Label but wanted to FILL, look for a sibling input
                    if (!(node instanceof TextInputControl) && node.getParent() instanceof javafx.scene.Parent) {
                        for (Node sibling : ((javafx.scene.Parent)node.getParent()).getChildrenUnmodifiable()) {
                            if (sibling instanceof TextInputControl) {
                                inputNode = sibling;
                                break;
                            }
                        }
                    }

                    if (inputNode instanceof TextInputControl) {
                        highlight(inputNode);
                        inputNode.requestFocus();
                        simulateTyping((TextInputControl) inputNode, value);
                    }
                });
            });
        }

        private void highlight(Node node) {
            DropShadow glow = new DropShadow(30, Color.web("#7850ff"));
            node.setEffect(glow);
            Timeline blink = new Timeline(
                new KeyFrame(Duration.ZERO, e -> glow.setRadius(30)),
                new KeyFrame(Duration.millis(300), e -> glow.setRadius(10)),
                new KeyFrame(Duration.millis(600), e -> glow.setRadius(30)),
                new KeyFrame(Duration.seconds(1.2), e -> node.setEffect(null))
            );
            blink.play();
        }

        private void simulateTyping(TextInputControl input, String text) {
            input.clear();
            final StringBuilder sb = new StringBuilder();
            Timeline typing = new Timeline();
            for (int i = 0; i < text.length(); i++) {
                final int index = i;
                typing.getKeyFrames().add(new KeyFrame(Duration.millis(35 * (i + 1)), e -> {
                    sb.append(text.charAt(index));
                    input.setText(sb.toString());
                    input.positionCaret(sb.length());
                }));
            }
            typing.play();
        }

        private Optional<Node> findNode(String query, String actionType) {
            if (query == null || query.isEmpty()) return Optional.empty();
            String q = query.toLowerCase().trim();
            
            for (Window window : Window.getWindows()) {
                if (window.getScene() != null) {
                    Optional<Node> found = search(window.getScene().getRoot(), q, actionType);
                    if (found.isPresent()) return found;
                }
            }
            return Optional.empty();
        }

        private Optional<Node> search(Node node, String query, String actionType) {
            if (node == null || !node.isVisible()) return Optional.empty();
            
            // Skip the Agent popup itself to prevent self-interaction
            if (node.getStyleClass().contains("horizon-panel")) return Optional.empty();

            // 1. Try Synthetic ID match (unnamed_hashCode)
            if (query.startsWith("unnamed_")) {
                try {
                    int targetHash = Integer.parseInt(query.substring(8));
                    if (node.hashCode() == targetHash) return Optional.of(node);
                } catch (Exception ignored) {}
            }

            // 2. Try ID match first (high precision)
            String id = node.getId();
            if (id != null && id.toLowerCase().contains(query)) {
                return Optional.of(node);
            }

            // 3. Try Text match (Labeled elements)
            if (node instanceof Labeled) {
                String text = ((Labeled) node).getText();
                if (text != null && text.toLowerCase().contains(query)) {
                    return Optional.of(node);
                }
            }

            // 4. Try Prompt text for inputs
            if (node instanceof TextInputControl) {
                String prompt = ((TextInputControl) node).getPromptText();
                if (prompt != null && prompt.toLowerCase().contains(query)) {
                    return Optional.of(node);
                }
            }

            // Recursive search
            if (node instanceof javafx.scene.Parent) {
                for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                    Optional<Node> found = search(child, query, actionType);
                    if (found.isPresent()) return found;
                }
            }
            return Optional.empty();
        }
    }

    private static final DesktopPage page = new DesktopPage();

    public static void executeAction(String type, String target, String value) {
        switch (type.toUpperCase()) {
            case "NAVIGATE": page.navigate(target); break;
            case "CLICK": page.click(target); break;
            case "FILL": page.fill(target, value); break;
        }
    }
}
