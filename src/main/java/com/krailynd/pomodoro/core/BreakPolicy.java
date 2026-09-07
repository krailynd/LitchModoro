package com.krailynd.pomodoro.core;

public interface BreakPolicy {

    SessionConfig plan(int totalMinutes);
}
