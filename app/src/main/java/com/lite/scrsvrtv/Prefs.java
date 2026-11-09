package com.lite.scrsvrtv;

import android.content.Context;
import android.content.SharedPreferences;

/** All user options, stored in SharedPreferences. */
final class Prefs {
    static final int[] IDLE_MINUTES = {1, 2, 5, 10, 15, 20, 30, 45, 60};

    static final String[] COLOR_NAMES = {"Soft white", "Night red", "Amber", "Cyan", "Green"};
    static final int[] COLORS = {0xFFB0B0B0, 0xFFB71C1C, 0xFFFFA000, 0xFF4FC3F7, 0xFF66BB6A};

    private static final String FILE = "clock_prefs";
    private final SharedPreferences sp;

    Prefs(Context c) {
        sp = c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    int idleMinutes() { return sp.getInt("idle_min", 5); }
    void setIdleMinutes(int v) { sp.edit().putInt("idle_min", v).apply(); }

    boolean use24h() { return sp.getBoolean("h24", true); }
    void setUse24h(boolean v) { sp.edit().putBoolean("h24", v).apply(); }

    boolean showSeconds() { return sp.getBoolean("seconds", false); }
    void setShowSeconds(boolean v) { sp.edit().putBoolean("seconds", v).apply(); }

    boolean showDate() { return sp.getBoolean("date", true); }
    void setShowDate(boolean v) { sp.edit().putBoolean("date", v).apply(); }

    int colorIndex() { return sp.getInt("color", 0); }
    void setColorIndex(int v) { sp.edit().putInt("color", v).apply(); }

    /** Extra dimming of the text between 22:00 and 06:00. */
    boolean nightDim() { return sp.getBoolean("night_dim", true); }
    void setNightDim(boolean v) { sp.edit().putBoolean("night_dim", v).apply(); }

    /** Ask the system to lower the screen backlight while the screensaver runs. */
    boolean lowBacklight() { return sp.getBoolean("low_backlight", true); }
    void setLowBacklight(boolean v) { sp.edit().putBoolean("low_backlight", v).apply(); }
}
