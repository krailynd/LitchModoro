package com.krailynd.pomodoro.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class MainWindow {

    private static final double WIDTH = 360;
    private static final double HEIGHT = 560;

    private final Stage stage;
    private final BorderPane container;
    private final StackPane root;

    public MainWindow(Stage stage) {
        this.stage = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("LitchModoro");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

        CustomTitleBar titleBar = new CustomTitleBar(stage);

        container = new BorderPane();
        container.setTop(titleBar);
        container.getStyleClass().add("app-container");

        root = new StackPane(container);
        root.getStyleClass().add("window-root");

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
                getClass().getResource("/com/krailynd/pomodoro/ui/theme.css").toExternalForm());

        stage.setScene(scene);
        stage.setMinWidth(300);
        stage.setMinHeight(480);
    }

    public void setContent(Node content) {
        container.setCenter(content);
    }

    public void addOverlay(Node overlay) {
        root.getChildren().add(overlay);
    }

    public void show() {
        stage.show();
    }

    public Stage stage() {
        return stage;
    }
}
