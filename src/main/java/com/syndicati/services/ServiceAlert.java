package com.syndicati.services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

public class ServiceAlert {

    public static boolean showConfirmation(String title, String header, String content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("");
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        confirm.initStyle(StageStyle.UNDECORATED);

        DialogPane pane = confirm.getDialogPane();
        pane.setStyle("-fx-background-color: #1e1e2e; -fx-font-family: 'Segoe UI';");
        pane.lookup(".header-panel").setStyle("-fx-background-color: #2a2a3d; -fx-padding: 12px 16px;");
        pane.lookup(".header-panel .label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
        pane.lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
        pane.lookupAll(".button").forEach(n -> n.setStyle(
                "-fx-background-color: #3d3d5c; -fx-text-fill: #ffffff; -fx-font-size: 13px;" +
                        "-fx-padding: 6px 16px; -fx-background-radius: 6px; -fx-cursor: hand;"
        ));

        confirm.setOnShown(evt -> {
            Stage stage = (Stage) pane.getScene().getWindow();
            stage.getScene().setFill(Color.TRANSPARENT);

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Button closeBtn = new Button("✕");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 2px 8px;");
            closeBtn.setOnAction(e -> stage.close());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox titleBar = new HBox(titleLabel, spacer, closeBtn);
            titleBar.setAlignment(Pos.CENTER_LEFT);
            titleBar.setPadding(new Insets(8, 12, 8, 16));
            titleBar.setStyle("-fx-background-color: #2a2a3d;");
            titleBar.setPrefWidth(pane.getWidth());

            final double[] drag = new double[2];
            titleBar.setOnMousePressed(e -> { drag[0] = e.getSceneX(); drag[1] = e.getSceneY(); });
            titleBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - drag[0]); stage.setY(e.getScreenY() - drag[1]); });

            pane.setHeader(titleBar);
        });

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showError(String title, String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("");
        error.setHeaderText(message);
        error.setContentText("");
        error.initStyle(StageStyle.UNDECORATED);

        DialogPane pane = error.getDialogPane();
        pane.setStyle("-fx-background-color: #1e1e2e; -fx-font-family: 'Segoe UI';");
        pane.lookup(".header-panel").setStyle("-fx-background-color: #2a2a3d; -fx-padding: 12px 16px;");
        pane.lookup(".header-panel .label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
        pane.lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
        pane.lookupAll(".button").forEach(n -> n.setStyle(
                "-fx-background-color: #3d3d5c; -fx-text-fill: #ffffff; -fx-font-size: 13px;" +
                        "-fx-padding: 6px 16px; -fx-background-radius: 6px; -fx-cursor: hand;"
        ));

        error.setOnShown(evt -> {
            Stage stage = (Stage) pane.getScene().getWindow();
            stage.getScene().setFill(Color.TRANSPARENT);

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Button closeBtn = new Button("✕");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 2px 8px;");
            closeBtn.setOnAction(e -> stage.close());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox titleBar = new HBox(titleLabel, spacer, closeBtn);
            titleBar.setAlignment(Pos.CENTER_LEFT);
            titleBar.setPadding(new Insets(8, 12, 8, 16));
            titleBar.setStyle("-fx-background-color: #2a2a3d;");
            titleBar.setPrefWidth(pane.getWidth());

            final double[] drag = new double[2];
            titleBar.setOnMousePressed(e -> { drag[0] = e.getSceneX(); drag[1] = e.getSceneY(); });
            titleBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - drag[0]); stage.setY(e.getScreenY() - drag[1]); });

            pane.setHeader(titleBar);

            pane.lookupAll(".label").forEach(n ->
                    n.setStyle(n.getStyle() + "; -fx-text-fill: #ffffff;")
            );
        });

        error.showAndWait();
    }
}
