package com.krailynd.pomodoro.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.krailynd.pomodoro.core.AdaptiveBreakPolicy;
import com.krailynd.pomodoro.core.SessionConfig;
import com.krailynd.pomodoro.core.SessionPlan;
import org.junit.jupiter.api.Test;

class PlanPreviewTest {

    private final AdaptiveBreakPolicy policy = new AdaptiveBreakPolicy();

    private String previewFor(int totalMinutes) {
        SessionConfig config = policy.plan(totalMinutes);
        return PlanPreview.describe(config, SessionPlan.fromConfig(config));
    }

    @Test
    void oneHourPreset() {
        assertEquals("1h · 2 × 25 min focus · 5 min breaks", previewFor(60));
    }

    @Test
    void twoHourPresetIncludesLongBreak() {
        assertEquals("2h · 4 × 25 min focus · 5 min breaks · 15 min long break every 4 cycles",
                previewFor(120));
    }

    @Test
    void fourHourPresetUsesDeepFocus() {
        assertEquals("4h · 4 × 50 min focus · 10 min breaks · 20 min long break every 2 cycles",
                previewFor(240));
    }

    @Test
    void customConfigWithoutLongBreak() {
        SessionConfig config = policy.planCustom(180, 45, 10, 0, 0);
        assertEquals("3h · 3 × 45 min focus · 10 min breaks",
                PlanPreview.describe(config, SessionPlan.fromConfig(config)));
    }

    @Test
    void nonWholeHourTotalFormatsMinutes() {
        SessionConfig config = policy.planCustom(90, 25, 5, 0, 0);
        assertEquals("1h 30min · 3 × 25 min focus · 5 min breaks",
                PlanPreview.describe(config, SessionPlan.fromConfig(config)));
    }

    @Test
    void subHourTotalFormatsAsMinutesOnly() {
        assertEquals("30 min · 1 × 25 min focus · 5 min breaks", previewFor(30));
    }
}
