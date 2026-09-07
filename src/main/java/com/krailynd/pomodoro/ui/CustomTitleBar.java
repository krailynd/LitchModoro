package com.krailynd.pomodoro.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public final class CustomTitleBar extends HBox {

    private double dragOffsetX;
    private double dragOffsetY;

    public CustomTitleBar(Stage stage) {
        getStyleClass().add("title-bar");
        setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LITCHMODORO");
        title.getStyleClass().add("title-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeButton = new Button("—");
        minimizeButton.getStyleClass().addAll("title-bar-button", "minimize-button");
        minimizeButton.setOnAction(e -> stage.setIconified(true));

        Button closeButton = new Button("×");
        closeButton.getStyleClass().addAll("title-bar-button", "close-button");
        closeButton.setOnAction(e -> stage.close());

        getChildren().addAll(title, spacer, minimizeButton, closeButton);

        setOnMousePressed(e -> {
            if (e.getTarget() instanceof Button) {
                return;
            }
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        setOnMouseDragged(e -> {
            if (e.getTarget() instanceof Button) {
                return;
            }
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });
    }
}
