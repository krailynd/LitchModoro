package com.krailynd.pomodoro.core;

public record Segment(SegmentType type, int durationSeconds) {

    public Segment {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be positive, got " + durationSeconds);
        }
    }
}
