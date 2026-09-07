package com.krailynd.pomodoro.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TimerEngine {

    private final SessionPlan plan;
    private final List<TimerListener> listeners = new ArrayList<>();

    private TimerState state = TimerState.IDLE;
    private TimerState pausedFrom;
    private int segmentIndex;
    private int remainingSeconds;

    public TimerEngine(SessionPlan plan) {
        if (plan == null || plan.isEmpty()) {
            throw new IllegalArgumentException("plan must not be null or empty");
        }
        this.plan = plan;
    }

    public void addListener(TimerListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(TimerListener listener) {
        listeners.remove(listener);
    }

    public synchronized void start() {
        if (state != TimerState.IDLE && state != TimerState.FINISHED) {
            throw new IllegalStateException("can only start from IDLE or FINISHED, current state: " + state);
        }
        segmentIndex = 0;
        beginSegment();
    }

    public synchronized void tick() {
        tick(1);
    }

    public synchronized void tick(int elapsedSeconds) {
        if (elapsedSeconds <= 0) {
            throw new IllegalArgumentException("elapsedSeconds must be positive, got " + elapsedSeconds);
        }
        if (!isRunning()) {
            return;
        }
        remainingSeconds -= elapsedSeconds;
        while (remainingSeconds <= 0 && state != TimerState.FINISHED) {
            int overflow = -remainingSeconds;
            if (segmentIndex >= plan.segmentCount() - 1) {
                remainingSeconds = 0;
                state = TimerState.FINISHED;
                fireFinish();
            } else {
                segmentIndex++;
                beginSegment();
                remainingSeconds -= overflow;
            }
        }
        if (isRunning()) {
            fireTick(remainingSeconds);
        }
    }

    public synchronized void pause() {
        if (isRunning()) {
            pausedFrom = state;
            state = TimerState.PAUSED;
        }
    }

    public synchronized void resume() {
        if (state == TimerState.PAUSED) {
            state = pausedFrom;
            pausedFrom = null;
        }
    }

    public synchronized void skipSegment() {
        if (isRunning() || state == TimerState.PAUSED) {
            pausedFrom = null;
            if (segmentIndex >= plan.segmentCount() - 1) {
                remainingSeconds = 0;
                state = TimerState.FINISHED;
                fireFinish();
            } else {
                segmentIndex++;
                beginSegment();
            }
        }
    }

    public synchronized void stop() {
        state = TimerState.IDLE;
        pausedFrom = null;
        segmentIndex = 0;
        remainingSeconds = 0;
    }

    public synchronized TimerState state() {
        return state;
    }

    public synchronized int secondsRemainingInSegment() {
        return remainingSeconds;
    }

    public synchronized int currentSegmentIndex() {
        return segmentIndex;
    }

    public synchronized Segment currentSegment() {
        if (state == TimerState.IDLE || state == TimerState.FINISHED) {
            return null;
        }
        return plan.segments().get(segmentIndex);
    }

    public SessionPlan plan() {
        return plan;
    }

    private boolean isRunning() {
        return state == TimerState.FOCUS || state == TimerState.BREAK || state == TimerState.LONG_BREAK;
    }

    private void beginSegment() {
        Segment segment = plan.segments().get(segmentIndex);
        remainingSeconds = segment.durationSeconds();
        state = switch (segment.type()) {
            case FOCUS -> TimerState.FOCUS;
            case BREAK -> TimerState.BREAK;
            case LONG_BREAK -> TimerState.LONG_BREAK;
        };
        fireSegmentStart(segment);
    }

    private void fireTick(int secondsRemaining) {
        for (TimerListener listener : listeners) {
            listener.onTick(secondsRemaining);
        }
    }

    private void fireSegmentStart(Segment segment) {
        for (TimerListener listener : listeners) {
            listener.onSegmentStart(segment);
        }
    }

    private void fireFinish() {
        for (TimerListener listener : listeners) {
            listener.onFinish();
        }
    }
}
