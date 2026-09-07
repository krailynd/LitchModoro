package com.krailynd.pomodoro.config;

import com.krailynd.pomodoro.core.AdaptiveBreakPolicy;
import com.krailynd.pomodoro.core.SessionConfig;

public record SessionSettings(int totalMinutes, Integer focusMinutes, Integer breakMinutes) {

    public static SessionSettings preset(int totalMinutes) {
        return new SessionSettings(totalMinutes, null, null);
    }

    public static SessionSettings custom(int totalMinutes, int focusMinutes, int breakMinutes) {
        return new SessionSettings(totalMinutes, focusMinutes, breakMinutes);
    }

    public boolean isCustom() {
        return focusMinutes != null && breakMinutes != null;
    }

    public SessionConfig toConfig(AdaptiveBreakPolicy policy) {
        if (isCustom()) {
            return policy.planCustom(totalMinutes, focusMinutes, breakMinutes, 0, 0);
        }
        return policy.plan(totalMinutes);
    }
}
