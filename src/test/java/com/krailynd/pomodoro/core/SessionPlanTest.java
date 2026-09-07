package com.krailynd.pomodoro.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SessionPlanTest {

    @Test
    void totalDurationNeverExceedsSessionLengthAcrossPolicies() {
        AdaptiveBreakPolicy policy = new AdaptiveBreakPolicy();
        for (int minutes : new int[]{30, 45, 60, 90, 120, 180, 240, 300, 360, 420, 480}) {
            SessionPlan plan = SessionPlan.fromConfig(policy.plan(minutes));
            assertTrue(plan.totalSeconds() <= minutes * 60,
                    "plan for " + minutes + "min exceeded the session length");
            assertTrue(plan.totalSeconds() > 0);
        }
    }

    @Test
    void oneHourClassicFillsExactly() {
        SessionPlan plan = SessionPlan.fromConfig(new AdaptiveBreakPolicy().plan(60));
        assertEquals(3600, plan.totalSeconds());
        assertEquals(SegmentType.FOCUS, plan.segments().get(0).type());
        assertEquals(SegmentType.BREAK, plan.segments().get(1).type());
    }

    @Test
    void segmentsAlternateFocusAndBreak() {
        SessionPlan plan = SessionPlan.fromConfig(new AdaptiveBreakPolicy().plan(120));
        for (int i = 0; i < plan.segmentCount(); i++) {
            SegmentType type = plan.segments().get(i).type();
            if (i % 2 == 0) {
                assertEquals(SegmentType.FOCUS, type, "even index must be FOCUS");
            } else {
                assertTrue(type == SegmentType.BREAK || type == SegmentType.LONG_BREAK,
                        "odd index must be a break");
            }
        }
    }

    @Test
    void longBreakReplacesShortBreakAtConfiguredCycle() {
        SessionConfig config = SessionConfig.builder(120)
                .focusMinutes(25)
                .shortBreakMinutes(5)
                .longBreakMinutes(15)
                .cyclesBeforeLongBreak(2)
                .build();
        SessionPlan plan = SessionPlan.fromConfig(config);
        assertEquals(SegmentType.LONG_BREAK, plan.segments().get(3).type());
        assertEquals(15 * 60, plan.segments().get(3).durationSeconds());
    }

    @Test
    void leftoverTimeBecomesTruncatedFocusSegment() {
        SessionConfig config = SessionConfig.builder(40).focusMinutes(25).shortBreakMinutes(5).build();
        SessionPlan plan = SessionPlan.fromConfig(config);
        Segment last = plan.segments().get(plan.segmentCount() - 1);
        assertEquals(SegmentType.FOCUS, last.type());
        assertEquals(10 * 60, last.durationSeconds());
        assertEquals(2400, plan.totalSeconds());
    }

    @Test
    void segmentsListIsUnmodifiable() {
        SessionPlan plan = SessionPlan.fromConfig(new AdaptiveBreakPolicy().plan(60));
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> plan.segments().add(new Segment(SegmentType.FOCUS, 60)));
    }
}
