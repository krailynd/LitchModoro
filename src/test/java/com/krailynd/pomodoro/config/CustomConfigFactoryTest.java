package com.krailynd.pomodoro.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CustomConfigFactoryTest {

    @Test
    void validInputsProduceConfig() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("180", "45", "10");
        CustomConfigFactory.Success success = assertInstanceOf(CustomConfigFactory.Success.class, result);
        assertEquals(180, success.config().totalMinutes());
        assertEquals(45, success.config().focusMinutes());
        assertEquals(10, success.config().shortBreakMinutes());
    }

    @Test
    void trimsWhitespaceAroundNumbers() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs(" 120 ", " 25", "5 ");
        assertInstanceOf(CustomConfigFactory.Success.class, result);
    }

    @Test
    void nonNumericTotalFailsWithField() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("abc", "25", "5");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertEquals("total", failure.field());
        assertEquals("Total minutes must be a whole number", failure.error());
    }

    @Test
    void blankFocusFails() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("120", " ", "5");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertEquals("focus", failure.field());
    }

    @Test
    void totalBelowMinimumFails() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("4", "2", "1");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertEquals("total", failure.field());
        assertTrue(failure.error().contains("between "
                + CustomConfigFactory.MIN_TOTAL_MINUTES + " and " + CustomConfigFactory.MAX_TOTAL_MINUTES));
    }

    @Test
    void totalAboveMaximumFails() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("721", "25", "5");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertEquals("total", failure.field());
    }

    @Test
    void focusAboveMaximumFails() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("720", "241", "5");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertEquals("focus", failure.field());
    }

    @Test
    void breakAboveMaximumFails() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("120", "25", "61");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertEquals("break", failure.field());
    }

    @Test
    void focusPlusBreakBeyondTotalFailsWithoutField() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("30", "25", "10");
        CustomConfigFactory.Failure failure = assertInstanceOf(CustomConfigFactory.Failure.class, result);
        assertNull(failure.field());
        assertTrue(failure.error().contains("focusMinutes + shortBreakMinutes"));
    }

    @Test
    void decimalInputFails() {
        CustomConfigFactory.Result result = CustomConfigFactory.fromInputs("120.5", "25", "5");
        assertInstanceOf(CustomConfigFactory.Failure.class, result);
    }
}
