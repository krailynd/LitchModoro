package com.krailynd.pomodoro.core;

public final class AdaptiveBreakPolicy implements BreakPolicy {

    private static final int CLASSIC_FOCUS = 25;
    private static final int CLASSIC_BREAK = 5;
    private static final int MEDIUM_LONG_BREAK = 15;
    private static final int MEDIUM_LONG_BREAK_EVERY = 4;
    private static final int DEEP_FOCUS = 50;
    private static final int DEEP_BREAK = 10;
    private static final int DEEP_LONG_BREAK = 20;
    private static final int ULTRADIAN_FOCUS = 90;
    private static final int ULTRADIAN_BREAK = 15;
    private static final int ULTRADIAN_LONG_BREAK = 45;
    private static final int ULTRADIAN_EXTENDED_BREAK = 20;

    @Override
    public SessionConfig plan(int totalMinutes) {
        if (totalMinutes <= 0) {
            throw new IllegalArgumentException("totalMinutes must be positive, got " + totalMinutes);
        }
        if (totalMinutes <= 60) {
            return SessionConfig.builder(totalMinutes)
                    .focusMinutes(CLASSIC_FOCUS)
                    .shortBreakMinutes(CLASSIC_BREAK)
                    .build();
        }
        if (totalMinutes <= 120) {
            return SessionConfig.builder(totalMinutes)
                    .focusMinutes(CLASSIC_FOCUS)
                    .shortBreakMinutes(CLASSIC_BREAK)
                    .longBreakMinutes(MEDIUM_LONG_BREAK)
                    .cyclesBeforeLongBreak(MEDIUM_LONG_BREAK_EVERY)
                    .build();
        }
        if (totalMinutes <= 240) {
            SessionConfig.Builder builder = SessionConfig.builder(totalMinutes)
                    .focusMinutes(DEEP_FOCUS)
                    .shortBreakMinutes(DEEP_BREAK);
            if (totalMinutes == 240) {
                int totalCycles = totalMinutes / (DEEP_FOCUS + DEEP_BREAK);
                builder.longBreakMinutes(DEEP_LONG_BREAK)
                        .cyclesBeforeLongBreak(Math.max(1, totalCycles / 2));
            }
            return builder.build();
        }
        if (totalMinutes < 420) {
            return SessionConfig.builder(totalMinutes)
                    .focusMinutes(ULTRADIAN_FOCUS)
                    .shortBreakMinutes(ULTRADIAN_BREAK)
                    .build();
        }
        int cycleMinutes = ULTRADIAN_FOCUS + ULTRADIAN_EXTENDED_BREAK;
        int totalCycles = (int) Math.round(totalMinutes / (double) cycleMinutes);
        return SessionConfig.builder(totalMinutes)
                .focusMinutes(ULTRADIAN_FOCUS)
                .shortBreakMinutes(ULTRADIAN_EXTENDED_BREAK)
                .longBreakMinutes(ULTRADIAN_LONG_BREAK)
                .cyclesBeforeLongBreak(Math.max(1, totalCycles / 2))
                .build();
    }

    public SessionConfig planCustom(int totalMinutes,
                                    int focusMinutes,
                                    int shortBreakMinutes,
                                    int longBreakMinutes,
                                    int cyclesBeforeLongBreak) {
        return SessionConfig.builder(totalMinutes)
                .focusMinutes(focusMinutes)
                .shortBreakMinutes(shortBreakMinutes)
                .longBreakMinutes(longBreakMinutes)
                .cyclesBeforeLongBreak(cyclesBeforeLongBreak)
                .build();
    }
}
