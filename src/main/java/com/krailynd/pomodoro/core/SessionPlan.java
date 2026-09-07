package com.krailynd.pomodoro.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SessionPlan {

    private final List<Segment> segments;
    private final int totalSeconds;

    private SessionPlan(List<Segment> segments) {
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
        this.totalSeconds = segments.stream().mapToInt(Segment::durationSeconds).sum();
    }

    public List<Segment> segments() {
        return segments;
    }

    public int totalSeconds() {
        return totalSeconds;
    }

    public int segmentCount() {
        return segments.size();
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    public static SessionPlan fromConfig(SessionConfig config) {
        List<Segment> segments = new ArrayList<>();
        int remaining = config.totalMinutes() * 60;
        int focusSeconds = config.focusMinutes() * 60;
        int shortBreakSeconds = config.shortBreakMinutes() * 60;
        int longBreakSeconds = config.longBreakMinutes() * 60;
        int cyclesBeforeLongBreak = config.cyclesBeforeLongBreak();
        int focusCount = 0;

        while (remaining > 0) {
            if (focusSeconds > remaining) {
                segments.add(new Segment(SegmentType.FOCUS, remaining));
                remaining = 0;
                break;
            }
            segments.add(new Segment(SegmentType.FOCUS, focusSeconds));
            remaining -= focusSeconds;
            focusCount++;
            if (remaining <= 0) {
                break;
            }

            boolean longBreakDue = longBreakSeconds > 0
                    && cyclesBeforeLongBreak > 0
                    && focusCount % cyclesBeforeLongBreak == 0;
            if (longBreakDue && longBreakSeconds <= remaining) {
                segments.add(new Segment(SegmentType.LONG_BREAK, longBreakSeconds));
                remaining -= longBreakSeconds;
            } else if (shortBreakSeconds <= remaining) {
                segments.add(new Segment(SegmentType.BREAK, shortBreakSeconds));
                remaining -= shortBreakSeconds;
            } else {
                break;
            }
        }
        return new SessionPlan(segments);
    }
}
