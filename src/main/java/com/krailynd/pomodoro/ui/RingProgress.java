package com.krailynd.pomodoro.ui;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public final class RingProgress extends javafx.scene.layout.Region {

    private static final double STROKE_WIDTH = 10.0;
    private static final double LERP_FACTOR = 0.15;
    private static final double SNAP_EPSILON = 0.002;
    private static final Color TRACK_COLOR = Color.web("#E0E0E0");
    private static final Color FOCUS_COLOR = Color.web("#3E8E5A");
    private static final Color REST_COLOR = Color.web("#9DB8A8");

    private final Canvas canvas = new Canvas();
    private double progress = 1.0;
    private double targetProgress = 1.0;
    private boolean restTone;

    public RingProgress() {
        getChildren().add(canvas);
        setPrefSize(260, 260);
        setMinSize(120, 120);
        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double value) {
        targetProgress = clamp(value);
        if (!Animations.ENABLED) {
            progress = targetProgress;
            redraw();
        }
    }

    public void snapProgress(double value) {
        targetProgress = clamp(value);
        progress = targetProgress;
        redraw();
    }

    public void setRestTone(boolean rest) {
        if (restTone != rest) {
            restTone = rest;
            redraw();
        }
    }

    public void updateFrame() {
        double diff = targetProgress - progress;
        if (Math.abs(diff) <= SNAP_EPSILON) {
            if (progress != targetProgress) {
                progress = targetProgress;
                redraw();
            }
            return;
        }
        progress += diff * LERP_FACTOR;
        redraw();
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        Insets insets = getInsets();
        double x = insets.getLeft();
        double y = insets.getTop();
        double cw = Math.max(0, w - insets.getLeft() - insets.getRight());
        double ch = Math.max(0, h - insets.getTop() - insets.getBottom());
        if (canvas.getWidth() != cw || canvas.getHeight() != ch) {
            canvas.setWidth(cw);
            canvas.setHeight(ch);
        }
        canvas.setLayoutX(x);
        canvas.setLayoutY(y);
        redraw();
    }

    private void redraw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        double padding = STROKE_WIDTH;
        double size = Math.min(w, h) - padding * 2;
        if (size <= 0) {
            return;
        }
        double x = (w - size) / 2;
        double y = (h - size) / 2;

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(STROKE_WIDTH);

        gc.setStroke(TRACK_COLOR);
        gc.strokeArc(x, y, size, size, 0, 360, javafx.scene.shape.ArcType.OPEN);

        if (progress > 0) {
            gc.setStroke(restTone ? REST_COLOR : FOCUS_COLOR);
            gc.strokeArc(x, y, size, size, 90, -360 * progress, javafx.scene.shape.ArcType.OPEN);
        }
    }
}
