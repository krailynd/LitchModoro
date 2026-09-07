package com.krailynd.pomodoro;

import com.krailynd.pomodoro.config.SessionSettings;
import com.krailynd.pomodoro.config.SettingsStore;
import com.krailynd.pomodoro.core.AdaptiveBreakPolicy;
import com.krailynd.pomodoro.core.SessionConfig;
import com.krailynd.pomodoro.core.SessionPlan;
import com.krailynd.pomodoro.core.TimerEngine;
import com.krailynd.pomodoro.ui.MainController;
import com.krailynd.pomodoro.ui.MainWindow;
import com.krailynd.pomodoro.ui.PresetPanel;
import javafx.application.Application;
import javafx.stage.Stage;

public final class MainApp extends Application {

    private final AdaptiveBreakPolicy policy = new AdaptiveBreakPolicy();
    private final SettingsStore settingsStore = new SettingsStore();

    private MainWindow window;
    private MainController controller;
    private PresetPanel presetPanel;
    private SessionSettings currentSettings;

    @Override
    public void start(Stage primaryStage) {
        SessionSettings initial = settingsStore.load();

        window = new MainWindow(primaryStage);
        presetPanel = new PresetPanel(settings -> {
            settingsStore.save(settings);
            rebuildSession(settings);
        });
        presetPanel.setVisible(false);
        window.addOverlay(presetPanel);

        rebuildSession(initial);
        window.show();
    }

    private void rebuildSession(SessionSettings settings) {
        currentSettings = settings;
        if (controller != null) {
            controller.shutdown();
        }
        SessionConfig config = settings.toConfig(policy);
        TimerEngine engine = new TimerEngine(SessionPlan.fromConfig(config));
        controller = new MainController(engine);
        controller.setOnSettings(() -> presetPanel.open(currentSettings));
        window.setContent(controller.view());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
