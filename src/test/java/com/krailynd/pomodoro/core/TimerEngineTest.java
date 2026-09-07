package com.krailynd.pomodoro.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TimerEngineTest {

    private static final class RecordingListener implements TimerListener {
        final List<SegmentType> segmentStarts = new ArrayList<>();
        final List<Integer> ticks = new ArrayList<>();
        int finishCount;

        @Override
        public void onTick(int secondsRemainingInSegment) {
            ticks.add(secondsRemainingInSegment);
        }

        @Override
        public void onSegmentStart(Segment segment) {
            segmentStarts.add(segment.type());
        }

        @Override
        public void onFinish() {
            finishCount++;
        }
    }

    private TimerEngine smallEngine(RecordingListener listener) {
        SessionConfig config = SessionConfig.builder(3)
                .focusMinutes(1)
                .shortBreakMinutes(1)
                .build();
        SessionPlan plan = SessionPlan.fromConfig(config);
        assertEquals(3, plan.segmentCount());
        TimerEngine engine = new TimerEngine(plan);
        engine.addListener(listener);
        return engine;
    }

    @Test
    void fullRunTransitionsFocusBreakFocusThenFinish() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        engine.start();
        assertEquals(TimerState.FOCUS, engine.state());
        assertEquals(60, engine.secondsRemainingInSegment());

        for (int i = 0; i < 180; i++) {
            engine.tick();
        }

        assertEquals(TimerState.FINISHED, engine.state());
        assertEquals(1, listener.finishCount);
        assertEquals(List.of(SegmentType.FOCUS, SegmentType.BREAK, SegmentType.FOCUS), listener.segmentStarts);
        assertEquals(179, listener.ticks.size());
    }

    @Test
    void multiSecondTickCrossesSegmentBoundaries() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        engine.start();
        engine.tick(60);
        assertEquals(TimerState.BREAK, engine.state());
        assertEquals(60, engine.secondsRemainingInSegment());

        engine.tick(61);
        assertEquals(TimerState.FOCUS, engine.state());
        assertEquals(59, engine.secondsRemainingInSegment());

        engine.tick(59);
        assertEquals(TimerState.FINISHED, engine.state());
        assertEquals(1, listener.finishCount);
    }

    @Test
    void pauseFreezesRemainingTimeAndResumeContinues() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        engine.start();
        engine.tick(20);
        assertEquals(40, engine.secondsRemainingInSegment());

        engine.pause();
        assertEquals(TimerState.PAUSED, engine.state());
        engine.tick(10);
        engine.tick(10);
        assertEquals(40, engine.secondsRemainingInSegment());

        engine.resume();
        assertEquals(TimerState.FOCUS, engine.state());
        engine.tick();
        assertEquals(39, engine.secondsRemainingInSegment());
    }

    @Test
    void skipSegmentAdvancesToNextSegment() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        engine.start();
        engine.skipSegment();
        assertEquals(TimerState.BREAK, engine.state());
        assertEquals(60, engine.secondsRemainingInSegment());

        engine.skipSegment();
        assertEquals(TimerState.FOCUS, engine.state());

        engine.skipSegment();
        assertEquals(TimerState.FINISHED, engine.state());
        assertEquals(1, listener.finishCount);
    }

    @Test
    void skipWhilePausedUnpausesIntoNextSegment() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        engine.start();
        engine.pause();
        engine.skipSegment();
        assertEquals(TimerState.BREAK, engine.state());
        assertEquals(60, engine.secondsRemainingInSegment());
    }

    @Test
    void stopResetsToIdle() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        engine.start();
        engine.tick(10);
        engine.stop();
        assertEquals(TimerState.IDLE, engine.state());
        assertEquals(0, engine.secondsRemainingInSegment());
        assertNull(engine.currentSegment());

        engine.start();
        assertEquals(TimerState.FOCUS, engine.state());
        assertEquals(60, engine.secondsRemainingInSegment());
    }

    @Test
    void rejectsInvalidUsage() {
        RecordingListener listener = new RecordingListener();
        TimerEngine engine = smallEngine(listener);

        assertThrows(IllegalStateException.class, () -> {
            engine.start();
            engine.start();
        });
        assertThrows(IllegalArgumentException.class, () -> engine.tick(0));
        assertThrows(IllegalArgumentException.class, () -> new TimerEngine(null));
    }

    @Test
    void longBreakSegmentMapsToLongBreakState() {
        SessionConfig config = SessionConfig.builder(4)
                .focusMinutes(1)
                .shortBreakMinutes(1)
                .longBreakMinutes(1)
                .cyclesBeforeLongBreak(1)
                .build();
        TimerEngine engine = new TimerEngine(SessionPlan.fromConfig(config));
        engine.start();
        engine.tick(60);
        assertEquals(TimerState.LONG_BREAK, engine.state());
    }
}
