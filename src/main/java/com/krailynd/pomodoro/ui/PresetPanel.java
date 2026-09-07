package com.krailynd.pomodoro.ui;

import com.krailynd.pomodoro.config.CustomConfigFactory;
import com.krailynd.pomodoro.config.PlanPreview;
import com.krailynd.pomodoro.config.SessionSettings;
import com.krailynd.pomodoro.core.AdaptiveBreakPolicy;
import com.krailynd.pomodoro.core.SessionConfig;
import com.krailynd.pomodoro.core.SessionPlan;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class PresetPanel extends StackPane {

    private static final double PANEL_WIDTH = 248;
    private static final int ANIMATION_MILLIS = 220;
    private static final int[] PRESET_MINUTES = {60, 120, 180, 240, 300, 360, 420, 480};

    private final AdaptiveBreakPolicy policy = new AdaptiveBreakPolicy();
    private final Consumer<SessionSettings> onApply;

    private final ToggleGroup presetGroup = new ToggleGroup();
    private final Map<Toggle, Integer> presetMinutesByToggle = new LinkedHashMap<>();
    private Toggle customToggle;

    private final TextField totalField = new TextField();
    private final TextField focusField = new TextField();
    private final TextField breakField = new TextField();
    private final VBox customFields;
    private final Label previewLabel = new Label();
    private final Label errorLabel = new Label();
    private final VBox panel;
    private final Region scrim = new Region();

    private boolean closing;

    public PresetPanel(Consumer<SessionSettings> onApply) {
        this.onApply = onApply;
        getStyleClass().add("preset-overlay");

        scrim.getStyleClass().add("overlay-scrim");
        scrim.setOnMouseClicked(e -> close());

        Label title = new Label("SESSION LENGTH");
        title.getStyleClass().add("preset-panel-title");

        Button closeButton = new Button("×");
        closeButton.getStyleClass().addAll("title-bar-button", "panel-close-button");
        closeButton.setOnAction(e -> close());

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(title, headerSpacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane presetGrid = buildPresetGrid();
        customFields = buildCustomFields();

        Label previewHeader = new Label("PLAN PREVIEW");
        previewHeader.getStyleClass().add("preset-panel-title");
        previewLabel.getStyleClass().add("preset-preview");
        previewLabel.setWrapText(true);

        errorLabel.getStyleClass().add("panel-error");
        errorLabel.setWrapText(true);
        errorLabel.setManaged(false);

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        Button applyButton = new Button("Apply");
        applyButton.getStyleClass().addAll("control-button", "primary-button");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setOnAction(e -> apply());

        panel = new VBox(12, header, presetGrid, customFields, previewHeader, previewLabel,
                errorLabel, bottomSpacer, applyButton);
        panel.getStyleClass().add("preset-panel");
        panel.setMaxWidth(PANEL_WIDTH);
        panel.setMaxHeight(Double.MAX_VALUE);
        StackPane.setAlignment(panel, Pos.CENTER_RIGHT);

        getChildren().addAll(scrim, panel);
        setVisible(false);

        presetGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> onSelectionChanged());
        totalField.textProperty().addListener((obs, oldText, newText) -> updatePreview());
        focusField.textProperty().addListener((obs, oldText, newText) -> updatePreview());
        breakField.textProperty().addListener((obs, oldText, newText) -> updatePreview());
    }

    public void open(SessionSettings current) {
        selectToggleFor(current);
        if (current.isCustom()) {
            totalField.setText(String.valueOf(current.totalMinutes()));
            focusField.setText(String.valueOf(current.focusMinutes()));
            breakField.setText(String.valueOf(current.breakMinutes()));
        }
        updatePreview();
        clearErrors();
        closing = false;
        setVisible(true);
        panel.setTranslateX(PANEL_WIDTH + 16);
        scrim.setOpacity(0.0);
        Animations.fadeTo(scrim, 1.0, ANIMATION_MILLIS);
        if (!Animations.ENABLED) {
            panel.setTranslateX(0);
            return;
        }
        TranslateTransition slide = new TranslateTransition(Duration.millis(ANIMATION_MILLIS), panel);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        slide.play();
    }

    public void close() {
        if (closing || !isVisible()) {
            return;
        }
        closing = true;
        Animations.fadeTo(scrim, 0.0, ANIMATION_MILLIS);
        if (!Animations.ENABLED) {
            panel.setTranslateX(PANEL_WIDTH + 16);
            setVisible(false);
            return;
        }
        TranslateTransition slide = new TranslateTransition(Duration.millis(ANIMATION_MILLIS), panel);
        slide.setToX(PANEL_WIDTH + 16);
        slide.setInterpolator(Interpolator.EASE_IN);
        slide.setOnFinished(e -> setVisible(false));
        slide.play();
    }

    private GridPane buildPresetGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        half.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(half, half);

        int index = 0;
        for (int minutes : PRESET_MINUTES) {
            ToggleButton option = new ToggleButton((minutes / 60) + " h");
            styleOption(option);
            presetMinutesByToggle.put(option, minutes);
            grid.add(option, index % 2, index / 2);
            index++;
        }
        ToggleButton custom = new ToggleButton("Custom");
        styleOption(custom);
        customToggle = custom;
        grid.add(custom, index % 2, index / 2);
        return grid;
    }

    private void styleOption(ToggleButton option) {
        option.getStyleClass().add("preset-option");
        option.setToggleGroup(presetGroup);
        option.setMaxWidth(Double.MAX_VALUE);
    }

    private VBox buildCustomFields() {
        VBox fields = new VBox(8,
                labeledField("Total minutes", totalField),
                labeledField("Focus minutes", focusField),
                labeledField("Break minutes", breakField));
        fields.getStyleClass().add("custom-fields");
        fields.setVisible(false);
        fields.setManaged(false);
        return fields;
    }

    private VBox labeledField(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        field.getStyleClass().add("custom-field");
        VBox box = new VBox(2, label, field);
        VBox.setMargin(box, new Insets(0));
        return box;
    }

    private void selectToggleFor(SessionSettings settings) {
        if (settings.isCustom()) {
            presetGroup.selectToggle(customToggle);
            return;
        }
        for (Map.Entry<Toggle, Integer> entry : presetMinutesByToggle.entrySet()) {
            if (entry.getValue() == settings.totalMinutes()) {
                presetGroup.selectToggle(entry.getKey());
                return;
            }
        }
        presetGroup.selectToggle(presetMinutesByToggle.keySet().iterator().next());
    }

    private void onSelectionChanged() {
        boolean custom = presetGroup.getSelectedToggle() == customToggle;
        customFields.setVisible(custom);
        customFields.setManaged(custom);
        updatePreview();
    }

    private boolean isCustomSelected() {
        return presetGroup.getSelectedToggle() == customToggle;
    }

    private void updatePreview() {
        Toggle selected = presetGroup.getSelectedToggle();
        if (selected == null) {
            previewLabel.setText("");
            return;
        }
        if (!isCustomSelected()) {
            clearErrors();
            int minutes = presetMinutesByToggle.get(selected);
            SessionConfig config = policy.plan(minutes);
            previewLabel.setText(PlanPreview.describe(config, SessionPlan.fromConfig(config)));
            return;
        }
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs(
                totalField.getText(), focusField.getText(), breakField.getText());
        if (result instanceof CustomConfigFactory.Success success) {
            clearErrors();
            SessionConfig config = success.config();
            previewLabel.setText(PlanPreview.describe(config, SessionPlan.fromConfig(config)));
        } else {
            previewLabel.setText("");
        }
    }

    private void apply() {
        if (isCustomSelected()) {
            CustomConfigFactory.Result result = CustomConfigFactory.fromInputs(
                    totalField.getText(), focusField.getText(), breakField.getText());
            if (result instanceof CustomConfigFactory.Failure failure) {
                showError(failure);
                return;
            }
            SessionConfig config = ((CustomConfigFactory.Success) result).config();
            onApply.accept(SessionSettings.custom(
                    config.totalMinutes(), config.focusMinutes(), config.shortBreakMinutes()));
        } else {
            Toggle selected = presetGroup.getSelectedToggle();
            if (selected == null) {
                return;
            }
            onApply.accept(SessionSettings.preset(presetMinutesByToggle.get(selected)));
        }
        close();
    }

    private void showError(CustomConfigFactory.Failure failure) {
        clearErrors();
        errorLabel.setText(failure.error());
        errorLabel.setManaged(true);
        TextField target = switch (failure.field() == null ? "" : failure.field()) {
            case "total" -> totalField;
            case "focus" -> focusField;
            case "break" -> breakField;
            default -> null;
        };
        if (target != null) {
            target.getStyleClass().add("input-error");
        }
    }

    private void clearErrors() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        totalField.getStyleClass().remove("input-error");
        focusField.getStyleClass().remove("input-error");
        breakField.getStyleClass().remove("input-error");
    }
}
