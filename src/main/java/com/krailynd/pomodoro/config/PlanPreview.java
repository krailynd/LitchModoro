package com.krailynd.pomodoro.config;

import com.krailynd.pomodoro.core.Segment;
import com.krailynd.pomodoro.core.SegmentType;
import com.krailynd.pomodoro.core.SessionConfig;
import com.krailynd.pomodoro.core.SessionPlan;
import java.util.ArrayList;
import java.util.List;

public final class PlanPreview {

    private PlanPreview() {
    }

    public static String describe(SessionConfig config, SessionPlan plan) {
        int fullFocusCount = 0;
        int breakCount = 0;
        int longBreakCount = 0;
        int fullFocusSeconds = config.focusMinutes() * 60;
        for (Segment segment : plan.segments()) {
            if (segment.type() == SegmentType.FOCUS && segment.durationSeconds() == fullFocusSeconds) {
                fullFocusCount++;
            } else if (segment.type() == SegmentType.BREAK) {
                breakCount++;
            } else if (segment.type() == SegmentType.LONG_BREAK) {
                longBreakCount++;
            }
        }

        List<String> parts = new ArrayList<>();
        parts.add(formatTotal(config.totalMinutes()));
        parts.add(fullFocusCount + " × " + config.focusMinutes() + " min focus");
        if (breakCount > 0) {
            parts.add(config.shortBreakMinutes() + " min breaks");
        }
        if (config.hasLongBreak()) {
            parts.add(config.longBreakMinutes() + " min long break every "
                    + config.cyclesBeforeLongBreak() + " cycles");
        }
        return String.join(" · ", parts);
    }

    private static String formatTotal(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours == 0) {
            return totalMinutes + " min";
        }
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "min";
    }
}
