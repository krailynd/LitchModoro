package com.krailynd.pomodoro.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class Animations {

    public static boolean ENABLED = true;

    private Animations() {
    }

    public static void entrance(Node node) {
        if (!ENABLED) {
            node.setOpacity(1.0);
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            return;
        }
        node.setOpacity(0.0);
        node.setScaleX(0.97);
        node.setScaleY(0.97);
        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setToValue(1.0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(350), node);
        scale.setToX(1.0);
        scale.setToY(1.0);
        new ParallelTransition(fade, scale).play();
    }

    public static void crossFadeText(Labeled labeled, String text) {
        if (!ENABLED || labeled.getText().equals(text)) {
            labeled.setText(text);
            labeled.setOpacity(1.0);
            return;
        }
        FadeTransition out = new FadeTransition(Duration.millis(140), labeled);
        out.setToValue(0.0);
        out.setOnFinished(e -> {
            labeled.setText(text);
            FadeTransition in = new FadeTransition(Duration.millis(200), labeled);
            in.setToValue(1.0);
            in.play();
        });
        out.play();
    }

    public static void pulse(Node node) {
        if (!ENABLED) {
            return;
        }
        ScaleTransition pulse = new ScaleTransition(Duration.millis(210), node);
        pulse.setToX(1.04);
        pulse.setToY(1.04);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    public static void fadeTo(Node node, double targetOpacity, double millis) {
        if (!ENABLED) {
            node.setOpacity(targetOpacity);
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(millis), node);
        fade.setToValue(targetOpacity);
        fade.play();
    }

    public static void softHover(Button button, Color normal, Color hover) {
        applyBackground(button, normal);
        button.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            Color from = currentColor(button, normal);
            Color to = isHovered ? hover : normal;
            if (!ENABLED) {
                applyBackground(button, to);
                return;
            }
            Transition tint = new Transition() {
                {
                    setCycleDuration(Duration.millis(160));
                }

                @Override
                protected void interpolate(double frac) {
                    applyBackground(button, from.interpolate(to, frac));
                }
            };
            tint.play();
        });
    }

    private static Color currentColor(Button button, Color fallback) {
        Object stored = button.getProperties().get("softHoverColor");
        return stored instanceof Color color ? color : fallback;
    }

    private static void applyBackground(Button button, Color color) {
        button.getProperties().put("softHoverColor", color);
        button.setStyle("-fx-background-color: " + toCss(color) + ";");
    }

    private static String toCss(Color color) {
        if (color.getOpacity() <= 0.0) {
            return "transparent";
        }
        return String.format("#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }
}
