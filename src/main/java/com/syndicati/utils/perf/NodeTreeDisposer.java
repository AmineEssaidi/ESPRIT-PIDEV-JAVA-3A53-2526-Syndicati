package com.syndicati.utils.perf;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;

/**
 * Best-effort JavaFX node cleanup for views that are removed from navigation.
 */
public final class NodeTreeDisposer {
    private NodeTreeDisposer() {}

    public static void dispose(Node node) {
        if (node == null) {
            return;
        }

        try {
            if (node instanceof Parent parent) {
                for (Node child : java.util.List.copyOf(parent.getChildrenUnmodifiable())) {
                    dispose(child);
                }
            }
        } catch (Exception ignored) {}

        try {
            if (node instanceof ImageView imageView) {
                imageView.setImage(null);
            } else if (node instanceof WebView webView) {
                webView.getEngine().load("about:blank");
            } else if (node instanceof MediaView mediaView) {
                mediaView.setMediaPlayer(null);
            } else if (node instanceof TextInputControl input) {
                input.clear();
            } else if (node instanceof ComboBoxBase<?> combo) {
                combo.setValue(null);
            } else if (node instanceof ButtonBase button) {
                button.setOnAction(null);
            }
        } catch (Exception ignored) {}

        try { node.setOnMouseClicked(null); } catch (Exception ignored) {}
        try { node.setOnMouseEntered(null); } catch (Exception ignored) {}
        try { node.setOnMouseExited(null); } catch (Exception ignored) {}
        try { node.setOnMouseMoved(null); } catch (Exception ignored) {}
        try { node.setOnMousePressed(null); } catch (Exception ignored) {}
        try { node.setOnMouseReleased(null); } catch (Exception ignored) {}
        try { node.setOnMouseDragged(null); } catch (Exception ignored) {}
        try { node.setOnKeyPressed(null); } catch (Exception ignored) {}
        try { node.setOnKeyReleased(null); } catch (Exception ignored) {}
        try { node.getProperties().clear(); } catch (Exception ignored) {}

        try {
            if (node instanceof Pane pane) {
                pane.getChildren().clear();
            }
        } catch (Exception ignored) {}
    }
}
