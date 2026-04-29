package com.syndicati.services.ai;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Inspects the current JavaFX scene to provide a structural map for the AI Agent.
 */
public class AppInspector {

    public static JsonObject inspectCurrentScene() {
        JsonObject result = new JsonObject();
        List<Window> windows = Window.getWindows();
        
        for (Window window : windows) {
            if (window.getScene() != null) {
                result.add("elements", inspectNode(window.getScene().getRoot()));
            }
        }
        return result;
    }

    private static JsonArray inspectNode(Node node) {
        JsonArray elements = new JsonArray();
        traverse(node, elements);
        return elements;
    }

    private static void traverse(Node node, JsonArray elements) {
        if (node == null) return;

        // Skip the Agent popup itself to avoid self-reference
        if (node.getStyleClass().contains("horizon-panel")) return;

        if (node instanceof Button || node instanceof TextField || node instanceof Label || node instanceof Hyperlink || node instanceof TextArea || node instanceof ImageView || (node instanceof Pane && node.getId() != null)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", node.getClass().getSimpleName());
            String id = node.getId() != null ? node.getId() : "unnamed_" + node.hashCode();
            obj.addProperty("id", id);
            
            String text = "";
            if (node instanceof Labeled) {
                text = sanitizeText(((Labeled) node).getText());
            } else if (node instanceof TextInputControl) {
                text = sanitizeText(((TextInputControl) node).getPromptText());
                if (text == null || text.isEmpty()) {
                    text = sanitizeText(((TextInputControl) node).getText());
                }
            }
            
            if (text != null && !text.isEmpty()) {
                obj.addProperty("text", text);
            }

            // Capture Parent Context (to help AI understand groupings)
            if (node.getParent() != null && node.getParent().getId() != null) {
                obj.addProperty("parent_id", node.getParent().getId());
            }
            
            elements.add(obj);
        }

        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                traverse(child, elements);
            }
        }
    }

    private static String sanitizeText(String text) {
        if (text == null) return "";
        // Remove non-ASCII characters (emojis, icons, etc)
        return text.replaceAll("[^\\x00-\\x7F]", "").trim();
    }
}
