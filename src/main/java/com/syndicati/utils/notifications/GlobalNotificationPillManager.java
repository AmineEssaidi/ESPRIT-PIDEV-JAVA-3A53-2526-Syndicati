package com.syndicati.utils.notifications;

import com.syndicati.utils.theme.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Global pill-style notification presenter used by both view and controller flows.
 */
public final class GlobalNotificationPillManager {

    private static final Duration DISPLAY_DURATION = Duration.seconds(2.8);
    private static final Deque<Request> QUEUE = new ArrayDeque<>();

    private static Popup popup;
    private static PauseTransition hideDelay;
    private static boolean showing;

    private GlobalNotificationPillManager() {
    }

    public static void created(String subject, String detail) {
        show(subject + " created", detail, Kind.SUCCESS);
    }

    public static void updated(String subject, String detail) {
        show(subject + " updated", detail, Kind.INFO);
    }

    public static void deleted(String subject, String detail) {
        show(subject + " deleted", detail, Kind.WARNING);
    }

    public static void success(String title, String detail) {
        show(title, detail, Kind.SUCCESS);
    }

    public static void info(String title, String detail) {
        show(title, detail, Kind.INFO);
    }

    public static void warning(String title, String detail) {
        show(title, detail, Kind.WARNING);
    }

    public static void error(String title, String detail) {
        show(title, detail, Kind.ERROR);
    }

    public static void expandedError(String title, String detail, String... hints) {
        showExpanded(title, buildExpandedMessage(detail, hints), Kind.ERROR);
    }

    public static void validationIssue(String title, String detail, String... hints) {
        showExpanded(title, buildExpandedMessage(detail, hints), Kind.WARNING);
    }

    public static void moderationBlocked(String subject, String detail, List<String> categories) {
        String label = subject == null || subject.isBlank() ? "Content" : subject;
        String reason = null;
        if (categories != null) {
            for (String category : categories) {
                if (category != null && !category.isBlank()) {
                    reason = category.trim();
                    break;
                }
            }
        }

        String compactDetail = detail != null && !detail.isBlank()
            ? detail.trim()
            : "Blocked by the moderation filter.";

        if (reason != null) {
            compactDetail = compactDetail + " Reason: " + reason;
        }

        show(label + " blocked", compactDetail, Kind.ERROR);
    }

    private static String buildExpandedMessage(String detail, String... hints) {
        StringBuilder expanded = new StringBuilder();
        if (detail != null && !detail.isBlank()) {
            expanded.append(detail);
        }
        if (hints != null && hints.length > 0) {
            if (expanded.length() > 0) {
                expanded.append("\n\n");
            }
            expanded.append("What to check:");
            for (String hint : hints) {
                if (hint != null && !hint.isBlank()) {
                    expanded.append("\n- ").append(hint);
                }
            }
        }
        return expanded.toString();
    }

    public static void show(String title, String detail, Kind kind) {
        Request request = new Request(title, detail, kind);
        if (Platform.isFxApplicationThread()) {
            enqueue(request);
        } else {
            Platform.runLater(() -> enqueue(request));
        }
    }

    public static void showExpanded(String title, String detail, Kind kind) {
        Request request = new Request(title, detail, kind, true);
        if (Platform.isFxApplicationThread()) {
            enqueue(request);
        } else {
            Platform.runLater(() -> enqueue(request));
        }
    }

    private static void enqueue(Request request) {
        QUEUE.addLast(request);
        if (!showing) {
            showNext();
        }
    }

    private static void showNext() {
        if (showing) {
            return;
        }

        Request request = QUEUE.pollFirst();
        if (request == null) {
            return;
        }

        Scene scene = ThemeManager.getInstance().getScene();
        if (scene == null || scene.getRoot() == null) {
            showing = false;
            return;
        }

        Node anchor = scene.getRoot();
        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds == null) {
            showing = false;
            return;
        }

        showing = true;
        Popup currentPopup = ensurePopup();
        currentPopup.getContent().setAll(buildContent(request));

        javafx.scene.Node content = currentPopup.getContent().get(0);
        content.applyCss();
        content.autosize();
        double width = content.prefWidth(-1);
        double height = content.prefHeight(width);
        double x = bounds.getMinX() + Math.max(16, (bounds.getWidth() - width) / 2.0);
        double y = bounds.getMaxY() - height - 36;

        currentPopup.show(anchor, x, y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(160), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(160), content);
        slideIn.setFromY(12);
        slideIn.setToY(0);
        fadeIn.play();
        slideIn.play();

        if (hideDelay != null) {
            hideDelay.stop();
        }
        hideDelay = new PauseTransition(DISPLAY_DURATION);
        hideDelay.setOnFinished(e -> hideCurrent());
        hideDelay.play();
    }

    private static void hideCurrent() {
        if (popup == null || popup.getContent().isEmpty()) {
            showing = false;
            showNext();
            return;
        }

        javafx.scene.Node content = popup.getContent().get(0);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), content);
        fadeOut.setFromValue(content.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            popup.hide();
            popup.getContent().clear();
            showing = false;
            showNext();
        });
        fadeOut.play();
    }

    private static Popup ensurePopup() {
        if (popup == null) {
            popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(false);
            popup.setHideOnEscape(false);
        }
        return popup;
    }

    private static StackPane buildContent(Request request) {
        String tone = request.kind.tone;
        String border = request.kind.border;
        Label icon = new Label(request.kind.icon);
        icon.setTextFill(Color.web(tone));
        icon.setStyle("-fx-font-size: 16px; -fx-font-weight: 900;");

        VBox textBlock = new VBox(4);
        textBlock.setFillWidth(true);
        textBlock.setMaxWidth(380);
        Label title = new Label(request.title);
        title.setTextFill(Color.web("#f8fafc"));
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-line-spacing: 1px;");
        title.setWrapText(true);
        title.setMaxWidth(380);
        Label detail = new Label(request.detail);
        detail.setWrapText(true);
        detail.setTextFill(Color.web("rgba(226,232,240,0.85)"));
        detail.setStyle("-fx-font-size: 12px; -fx-line-spacing: 2px;");
        detail.setMaxWidth(380);
        textBlock.getChildren().addAll(title, detail);

        if (request.expanded) {
            Label hint = new Label("Moderation details available in the content log.");
            hint.setWrapText(true);
            hint.setMaxWidth(380);
            hint.setTextFill(Color.web("rgba(255,255,255,0.60)"));
            hint.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");
            textBlock.getChildren().add(hint);
        }

        Button close = new Button("\u2715");
        close.setOnAction(e -> hideCurrent());
        close.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-text-fill: rgba(255,255,255,0.82);" +
            "-fx-background-radius: 999px;" +
            "-fx-border-radius: 999px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-cursor: hand;"
        );

        HBox header = new HBox(12, icon, textBlock, close);
        HBox.setHgrow(textBlock, javafx.scene.layout.Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(0, header);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setMinWidth(380);
        card.setPrefWidth(request.expanded ? 520 : 460);
        card.setMaxWidth(request.expanded ? 560 : 500);
        card.setStyle(
            "-fx-background-color: rgba(10,10,15,0.96);" +
            "-fx-background-radius: 999px;" +
            "-fx-border-radius: 999px;" +
            "-fx-border-width: 1px;" +
            "-fx-border-color: " + border + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.40), 24, 0.18, 0, 10);"
        );

        HBox shell = new HBox(12, card);
        shell.setAlignment(Pos.CENTER_LEFT);
        shell.setStyle("-fx-background-color: transparent;");

        StackPane wrapper = new StackPane(shell);
        wrapper.setStyle("-fx-text-background-color: #f8fafc;");
        wrapper.setMaxWidth(request.expanded ? 560 : 500);
        wrapper.setOpacity(0);
        return wrapper;
    }

    private enum Kind {
        SUCCESS("\u2713", ThemeManager.getInstance().getAccentHex(), "rgba(255,255,255,0.12)"),
        INFO("\u2139", ThemeManager.getInstance().getAccentHex(), "rgba(255,255,255,0.12)"),
        WARNING("\u26a0", "#f59e0b", "rgba(245,158,11,0.25)"),
        ERROR("\u2715", "#ef4444", "rgba(239,68,68,0.25)");

        private final String icon;
        private final String tone;
        private final String border;

        Kind(String icon, String tone, String border) {
            this.icon = icon;
            this.tone = tone;
            this.border = border;
        }
    }

    private record Request(String title, String detail, Kind kind, boolean expanded) {
        private Request(String title, String detail, Kind kind) {
            this(title, detail, kind, false);
        }
    }
}