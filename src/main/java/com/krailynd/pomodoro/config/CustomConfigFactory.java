package com.krailynd.pomodoro.config;

import com.krailynd.pomodoro.core.AdaptiveBreakPolicy;
import com.krailynd.pomodoro.core.SessionConfig;

public final class CustomConfigFactory {

    public static final int MIN_TOTAL_MINUTES = 5;
    public static final int MAX_TOTAL_MINUTES = 720;
    public static final int MAX_FOCUS_MINUTES = 240;
    public static final int MAX_BREAK_MINUTES = 60;

    private static final String FIELD_TOTAL = "total";
    private static final String FIELD_FOCUS = "focus";
    private static final String FIELD_BREAK = "break";

    private CustomConfigFactory() {
    }

    public sealed interface Result permits Success, Failure {
    }

    public record Success(SessionConfig config) implements Result {
    }

    public record Failure(String field, String error) implements Result {
    }

    public static Result fromInputs(String totalText, String focusText, String breakText) {
        Integer total = parseWholeNumber(totalText);
        if (total == null) {
            return new Failure(FIELD_TOTAL, "Total minutes must be a whole number");
        }
        Integer focus = parseWholeNumber(focusText);
        if (focus == null) {
            return new Failure(FIELD_FOCUS, "Focus minutes must be a whole number");
        }
        Integer breakMinutes = parseWholeNumber(breakText);
        if (breakMinutes == null) {
            return new Failure(FIELD_BREAK, "Break minutes must be a whole number");
        }
        if (total < MIN_TOTAL_MINUTES || total > MAX_TOTAL_MINUTES) {
            return new Failure(FIELD_TOTAL,
                    "Total minutes must be between " + MIN_TOTAL_MINUTES + " and " + MAX_TOTAL_MINUTES);
        }
        if (focus < 1 || focus > MAX_FOCUS_MINUTES) {
            return new Failure(FIELD_FOCUS,
                    "Focus minutes must be between 1 and " + MAX_FOCUS_MINUTES);
        }
        if (breakMinutes < 1 || breakMinutes > MAX_BREAK_MINUTES) {
            return new Failure(FIELD_BREAK,
                    "Break minutes must be between 1 and " + MAX_BREAK_MINUTES);
        }
        try {
            SessionConfig config = new AdaptiveBreakPolicy()
                    .planCustom(total, focus, breakMinutes, 0, 0);
            return new Success(config);
        } catch (IllegalArgumentException invalid) {
            return new Failure(null, invalid.getMessage());
        }
    }

    private static Integer parseWholeNumber(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException notANumber) {
            return null;
        }
    }
}
