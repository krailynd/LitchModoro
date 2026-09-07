package com.krailynd.pomodoro.config;

import java.util.prefs.Preferences;

public final class SettingsStore {

    private static final String KEY_TOTAL_MINUTES = "totalMinutes";
    private static final String KEY_FOCUS_MINUTES = "focusMinutes";
    private static final String KEY_BREAK_MINUTES = "breakMinutes";
    private static final int DEFAULT_TOTAL_MINUTES = 120;

    private final Preferences preferences;
    private final com.krailynd.pomodoro.core.AdaptiveBreakPolicy policy =
            new com.krailynd.pomodoro.core.AdaptiveBreakPolicy();

    public SettingsStore() {
        this(Preferences.userNodeForPackage(SettingsStore.class));
    }

    SettingsStore(Preferences preferences) {
        this.preferences = preferences;
    }

    public SessionSettings load() {
        int total = preferences.getInt(KEY_TOTAL_MINUTES, DEFAULT_TOTAL_MINUTES);
        int focus = preferences.getInt(KEY_FOCUS_MINUTES, -1);
        int breakMinutes = preferences.getInt(KEY_BREAK_MINUTES, -1);
        SessionSettings settings = focus > 0 && breakMinutes > 0
                ? SessionSettings.custom(total, focus, breakMinutes)
                : SessionSettings.preset(total);
        try {
            settings.toConfig(policy);
            return settings;
        } catch (RuntimeException invalid) {
            return SessionSettings.preset(DEFAULT_TOTAL_MINUTES);
        }
    }

    public void save(SessionSettings settings) {
        preferences.putInt(KEY_TOTAL_MINUTES, settings.totalMinutes());
        if (settings.isCustom()) {
            preferences.putInt(KEY_FOCUS_MINUTES, settings.focusMinutes());
            preferences.putInt(KEY_BREAK_MINUTES, settings.breakMinutes());
        } else {
            preferences.remove(KEY_FOCUS_MINUTES);
            preferences.remove(KEY_BREAK_MINUTES);
        }
    }
}
