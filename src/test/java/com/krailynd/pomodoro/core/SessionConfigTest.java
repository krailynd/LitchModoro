package com.krailynd.pomodoro.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SessionConfigTest {

    @Test
    void buildsValidConfigWithDefaults() {
        SessionConfig config = SessionConfig.builder(60).build();
        assertEquals(60, config.totalMinutes());
        assertEquals(25, config.focusMinutes());
        assertEquals(5, config.shortBreakMinutes());
    }

    @Test
    void rejectsZeroTotal() {
        assertThrows(IllegalArgumentException.class, () -> SessionConfig.builder(0).build());
    }

    @Test
    void rejectsNegativeFocus() {
        SessionConfig.Builder builder = SessionConfig.builder(60).focusMinutes(-1);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void rejectsZeroShortBreak() {
        SessionConfig.Builder builder = SessionConfig.builder(60).shortBreakMinutes(0);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void rejectsFocusPlusBreakExceedingTotal() {
        SessionConfig.Builder builder = SessionConfig.builder(30).focusMinutes(25).shortBreakMinutes(10);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void rejectsLongBreakWithoutCycleCount() {
        SessionConfig.Builder builder = SessionConfig.builder(120).longBreakMinutes(15).cyclesBeforeLongBreak(0);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void rejectsCycleCountWithoutLongBreak() {
        SessionConfig.Builder builder = SessionConfig.builder(120).longBreakMinutes(0).cyclesBeforeLongBreak(4);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void rejectsNegativeCyclesBeforeLongBreak() {
        SessionConfig.Builder builder = SessionConfig.builder(120).cyclesBeforeLongBreak(-2);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void isImmutableValueObject() {
        SessionConfig a = SessionConfig.builder(60).build();
        SessionConfig b = SessionConfig.builder(60).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("total=60"));
    }
}
