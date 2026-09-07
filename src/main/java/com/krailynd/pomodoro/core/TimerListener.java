package com.krailynd.pomodoro.core;

public interface TimerListener {

    default void onTick(int secondsRemainingInSegment) {
    }

    default void onSegmentStart(Segment segment) {
    }

    default void onFinish() {
    }
}
