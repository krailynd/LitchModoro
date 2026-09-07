package com.krailynd.pomodoro.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AdaptiveBreakPolicyTest {

    private final AdaptiveBreakPolicy policy = new AdaptiveBreakPolicy();

    @Test
    void oneHourUsesClassicPomodoroWithoutLongBreak() {
        SessionConfig config = policy.plan(60);
        assertEquals(25, config.focusMinutes());
        assertEquals(5, config.shortBreakMinutes());
        assertFalse(config.hasLongBreak());
    }

    @Test
    void twoHoursAddsFifteenMinuteLongBreakEveryFourCycles() {
        SessionConfig config = policy.plan(120);
        assertEquals(25, config.focusMinutes());
        assertEquals(5, config.shortBreakMinutes());
        assertEquals(15, config.longBreakMinutes());
        assertEquals(4, config.cyclesBeforeLongBreak());
    }

    @Test
    void fourHoursUsesDeepCyclesWithTwentyMinuteLongBreakAtMidpoint() {
        SessionConfig config = policy.plan(240);
        assertEquals(50, config.focusMinutes());
        assertEquals(10, config.shortBreakMinutes());
        assertEquals(20, config.longBreakMinutes());
        assertEquals(2, config.cyclesBeforeLongBreak());

        SessionPlan plan = SessionPlan.fromConfig(config);
        assertEquals(SegmentType.LONG_BREAK, plan.segments().get(3).type());
        assertEquals(20 * 60, plan.segments().get(3).durationSeconds());
        assertEquals(1, plan.segments().stream().filter(s -> s.type() == SegmentType.LONG_BREAK).count());
    }

    @Test
    void threeHoursUsesDeepCyclesWithoutLongBreak() {
        SessionConfig config = policy.plan(180);
        assertEquals(50, config.focusMinutes());
        assertEquals(10, config.shortBreakMinutes());
        assertFalse(config.hasLongBreak());
    }

    @Test
    void sixHoursUsesUltradianCyclesWithoutLongBreak() {
        SessionConfig config = policy.plan(360);
        assertEquals(90, config.focusMinutes());
        assertEquals(15, config.shortBreakMinutes());
        assertFalse(config.hasLongBreak());
    }

    @Test
    void eightHoursUsesExtendedUltradianWithMidpointLongBreak() {
        SessionConfig config = policy.plan(480);
        assertEquals(90, config.focusMinutes());
        assertEquals(20, config.shortBreakMinutes());
        assertEquals(45, config.longBreakMinutes());
        assertEquals(2, config.cyclesBeforeLongBreak());

        SessionPlan plan = SessionPlan.fromConfig(config);
        assertEquals(SegmentType.LONG_BREAK, plan.segments().get(3).type());
        assertEquals(45 * 60, plan.segments().get(3).durationSeconds());
    }

    @Test
    void sevenHoursAlsoUsesExtendedUltradian() {
        SessionConfig config = policy.plan(420);
        assertEquals(90, config.focusMinutes());
        assertEquals(20, config.shortBreakMinutes());
        assertEquals(45, config.longBreakMinutes());
        assertTrue(config.hasLongBreak());
    }

    @Test
    void customOverrideIsSupported() {
        SessionConfig config = policy.planCustom(90, 40, 8, 0, 0);
        assertEquals(90, config.totalMinutes());
        assertEquals(40, config.focusMinutes());
        assertEquals(8, config.shortBreakMinutes());
        assertFalse(config.hasLongBreak());
    }

    @Test
    void rejectsNonPositiveInput() {
        assertThrows(IllegalArgumentException.class, () -> policy.plan(0));
        assertThrows(IllegalArgumentException.class, () -> policy.plan(-30));
    }
}
