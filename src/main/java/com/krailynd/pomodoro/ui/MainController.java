package com.krailynd.pomodoro.ui;

import com.krailynd.pomodoro.core.Segment;
import com.krailynd.pomodoro.core.SegmentType;
import com.krailynd.pomodoro.core.TimerEngine;
import com.krailynd.pomodoro.core.TimerListener;
import com.krailynd.pomodoro.core.TimerState;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public final class MainController implements TimerListener {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;
    private static final String IDLE_HINT = "Pick your study time";
    private static final String FINISHED_MESSAGE = "Session complete \u00B7 well done";

    private final TimerEngine engine;

    private final Label segmentLabel = new Label(IDLE_HINT);
    private final Label timeLabel = new Label("--:--");
    private final RingProgress ring = new RingProgress();
    private final Button startPauseButton = new Button("Start");
    private final Button skipButton = new Button("Skip");
    private final Button stopButton = new Button("Stop");
    private final Button settingsButton = new Button("\u2699");
    private final VBox view;
    private Runnable onSettings = () -> {
    };

    private int currentSegmentDurationSeconds;
    private long lastTickNanos;
    private double accumulatedSeconds;

    private final AnimationTimer ticker = new AnimationTimer() {
        @Override
        public void handle(long now) {
            ring.updateFrame();
            if (!isEngineRunning()) {
                lastTickNanos = now;
                accumulatedSeconds = 0;
                return;
            }
            if (lastTickNanos == 0) {
                lastTickNanos = now;
                return;
            }
            accumulatedSeconds += (now - lastTickNanos) / NANOS_PER_SECOND;
            lastTickNanos = now;
            int wholeSeconds = (int) accumulatedSeconds;
            if (wholeSeconds >= 1) {
                accumulatedSeconds -= wholeSeconds;
                engine.tick(wholeSeconds);
            }
        }
    };

    public MainController(TimerEngine engine) {
        this.engine = engine;
        this.engine.addListener(this);

        segmentLabel.getStyleClass().add("segment-label");
        timeLabel.getStyleClass().add("time-label");

        StackPane ringStack = new StackPane(ring, timeLabel);
        ringStack.getStyleClass().add("ring-stack");

        startPauseButton.getStyleClass().addAll("control-button", "primary-button");
        skipButton.getStyleClass().add("control-button");
        stopButton.getStyleClass().add("control-button");
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setOnAction(e -> onSettings.run());

        Animations.softHover(startPauseButton, Color.web("#3E8E5A"), Color.web("#357A4E"));
        Animations.softHover(skipButton, Color.TRANSPARENT, Color.web("#EEEEEE"));
        Animations.softHover(stopButton, Color.TRANSPARENT, Color.web("#EEEEEE"));

        startPauseButton.setOnAction(e -> onStartPause());
        skipButton.setOnAction(e -> {
            engine.skipSegment();
            refreshControls();
        });
        stopButton.setOnAction(e -> {
            engine.stop();
            showIdle();
        });

        HBox controls = new HBox(12, startPauseButton, skipButton, stopButton);
        controls.setAlignment(Pos.CENTER);
        controls.getStyleClass().add("controls");

        HBox topRow = new HBox(settingsButton);
        topRow.setAlignment(Pos.CENTER_RIGHT);

        view = new VBox(16, topRow, segmentLabel, ringStack, controls);
        view.setAlignment(Pos.CENTER);
        view.getStyleClass().add("main-screen");

        showIdle();
        Animations.entrance(view);
        ticker.start();
    }

    public VBox view() {
        return view;
    }

    public void setOnSettings(Runnable onSettings) {
        this.onSettings = onSettings != null ? onSettings : () -> {
        };
    }

    public void shutdown() {
        ticker.stop();
        engine.removeListener(this);
        engine.stop();
    }

    private void onStartPause() {
        TimerState state = engine.state();
        switch (state) {
            case IDLE -> engine.start();
            case FINISHED -> {
                onSettings.run();
                return;
            }
            case PAUSED -> engine.resume();
            default -> engine.pause();
        }
        refreshControls();
    }

    private boolean isEngineRunning() {
        TimerState state = engine.state();
        return state == TimerState.FOCUS || state == TimerState.BREAK || state == TimerState.LONG_BREAK;
    }

    private void showIdle() {
        Segment first = engine.plan().segments().isEmpty() ? null : engine.plan().segments().get(0);
        currentSegmentDurationSeconds = first != null ? first.durationSeconds() : 0;
        Animations.crossFadeText(segmentLabel, IDLE_HINT);
        timeLabel.setText(formatTime(currentSegmentDurationSeconds));
        ring.setRestTone(false);
        ring.snapProgress(1.0);
        refreshControls();
    }

    private void refreshControls() {
        TimerState state = engine.state();
        boolean finished = state == TimerState.FINISHED;
        boolean active = state != TimerState.IDLE && !finished;
        skipButton.setDisable(!active);
        stopButton.setDisable(!active);
        skipButton.setVisible(!finished);
        skipButton.setManaged(!finished);
        stopButton.setVisible(!finished);
        stopButton.setManaged(!finished);
        if (finished) {
            startPauseButton.setText("New session");
            return;
        }
        startPauseButton.setText(isEngineRunning() ? "Pause" : "Start");
    }

    private static String segmentName(SegmentType type) {
        return switch (type) {
            case FOCUS -> "Focus";
            case BREAK -> "Breathe";
            case LONG_BREAK -> "Stretch";
        };
    }

    private static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onTick(int secondsRemainingInSegment) {
        Platform.runLater(() -> {
            timeLabel.setText(formatTime(secondsRemainingInSegment));
            if (currentSegmentDurationSeconds > 0) {
                ring.setProgress(secondsRemainingInSegment / (double) currentSegmentDurationSeconds);
            }
        });
    }

    @Override
    public void onSegmentStart(Segment segment) {
        Platform.runLater(() -> {
            currentSegmentDurationSeconds = segment.durationSeconds();
            Animations.crossFadeText(segmentLabel, segmentName(segment.type()));
            timeLabel.setText(formatTime(segment.durationSeconds()));
            ring.setRestTone(segment.type() != SegmentType.FOCUS);
            ring.snapProgress(1.0);
            Animations.pulse(ring);
            refreshControls();
        });
    }

    @Override
    public void onFinish() {
        Platform.runLater(() -> {
            Animations.crossFadeText(segmentLabel, FINISHED_MESSAGE);
            timeLabel.setText("00:00");
            ring.snapProgress(0.0);
            refreshControls();
        });
    }
}
