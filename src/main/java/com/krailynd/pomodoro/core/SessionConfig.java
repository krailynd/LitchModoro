package com.krailynd.pomodoro.core;

import java.util.Objects;

public final class SessionConfig {

    private final int totalMinutes;
    private final int focusMinutes;
    private final int shortBreakMinutes;
    private final int longBreakMinutes;
    private final int cyclesBeforeLongBreak;

    private SessionConfig(Builder builder) {
        this.totalMinutes = builder.totalMinutes;
        this.focusMinutes = builder.focusMinutes;
        this.shortBreakMinutes = builder.shortBreakMinutes;
        this.longBreakMinutes = builder.longBreakMinutes;
        this.cyclesBeforeLongBreak = builder.cyclesBeforeLongBreak;
        validate();
    }

    private void validate() {
        if (totalMinutes <= 0) {
            throw new IllegalArgumentException("totalMinutes must be positive, got " + totalMinutes);
        }
        if (focusMinutes <= 0) {
            throw new IllegalArgumentException("focusMinutes must be positive, got " + focusMinutes);
        }
        if (shortBreakMinutes <= 0) {
            throw new IllegalArgumentException("shortBreakMinutes must be positive, got " + shortBreakMinutes);
        }
        if (longBreakMinutes < 0) {
            throw new IllegalArgumentException("longBreakMinutes must not be negative, got " + longBreakMinutes);
        }
        if (cyclesBeforeLongBreak < 0) {
            throw new IllegalArgumentException("cyclesBeforeLongBreak must not be negative, got " + cyclesBeforeLongBreak);
        }
        if (longBreakMinutes > 0 && cyclesBeforeLongBreak == 0) {
            throw new IllegalArgumentException("cyclesBeforeLongBreak must be >= 1 when a long break is configured");
        }
        if (longBreakMinutes == 0 && cyclesBeforeLongBreak > 0) {
            throw new IllegalArgumentException("longBreakMinutes must be > 0 when cyclesBeforeLongBreak is set");
        }
        if (focusMinutes + shortBreakMinutes > totalMinutes) {
            throw new IllegalArgumentException(
                    "focusMinutes + shortBreakMinutes must be <= totalMinutes (got "
                            + focusMinutes + " + " + shortBreakMinutes + " > " + totalMinutes + ")");
        }
    }

    public int totalMinutes() {
        return totalMinutes;
    }

    public int focusMinutes() {
        return focusMinutes;
    }

    public int shortBreakMinutes() {
        return shortBreakMinutes;
    }

    public int longBreakMinutes() {
        return longBreakMinutes;
    }

    public int cyclesBeforeLongBreak() {
        return cyclesBeforeLongBreak;
    }

    public boolean hasLongBreak() {
        return longBreakMinutes > 0;
    }

    public static Builder builder(int totalMinutes) {
        return new Builder(totalMinutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SessionConfig other)) {
            return false;
        }
        return totalMinutes == other.totalMinutes
                && focusMinutes == other.focusMinutes
                && shortBreakMinutes == other.shortBreakMinutes
                && longBreakMinutes == other.longBreakMinutes
                && cyclesBeforeLongBreak == other.cyclesBeforeLongBreak;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMinutes, focusMinutes, shortBreakMinutes, longBreakMinutes, cyclesBeforeLongBreak);
    }

    @Override
    public String toString() {
        return "SessionConfig{total=" + totalMinutes
                + ", focus=" + focusMinutes
                + ", shortBreak=" + shortBreakMinutes
                + ", longBreak=" + longBreakMinutes
                + ", cyclesBeforeLongBreak=" + cyclesBeforeLongBreak + "}";
    }

    public static final class Builder {

        private final int totalMinutes;
        private int focusMinutes = 25;
        private int shortBreakMinutes = 5;
        private int longBreakMinutes = 0;
        private int cyclesBeforeLongBreak = 0;

        private Builder(int totalMinutes) {
            this.totalMinutes = totalMinutes;
        }

        public Builder focusMinutes(int value) {
            this.focusMinutes = value;
            return this;
        }

        public Builder shortBreakMinutes(int value) {
            this.shortBreakMinutes = value;
            return this;
        }

        public Builder longBreakMinutes(int value) {
            this.longBreakMinutes = value;
            return this;
        }

        public Builder cyclesBeforeLongBreak(int value) {
            this.cyclesBeforeLongBreak = value;
            return this;
        }

        public SessionConfig build() {
            return new SessionConfig(this);
        }
    }
}
