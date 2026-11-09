package com.lite.scrsvrtv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Remote-friendly settings screen (D-pad: up/down to move, OK to change).
 * Built in code to keep the app small and dependency free.
 */
public class SettingsActivity extends Activity {
    private Prefs prefs;
    private TextView status;
    private Button idleBtn, h24Btn, secBtn, dateBtn, colorBtn, nightBtn, backlightBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(48);
        list.setPadding(pad, dp(32), pad, dp(32));

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        title.setTextColor(0xFFFFFFFF);
        list.addView(title);

        idleBtn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) {
                int cur = prefs.idleMinutes(), next = Prefs.IDLE_MINUTES[0];
                for (int i = 0; i < Prefs.IDLE_MINUTES.length; i++) {
                    if (Prefs.IDLE_MINUTES[i] == cur) {
                        next = Prefs.IDLE_MINUTES[(i + 1) % Prefs.IDLE_MINUTES.length];
                        break;
                    }
                }
                prefs.setIdleMinutes(next);
                refresh();
            }
        });
        addButton(list, "Apply idle time to TV", new View.OnClickListener() {
            @Override public void onClick(View v) { applyIdleTime(); }
        });
        addButton(list, "Set as TV screensaver", new View.OnClickListener() {
            @Override public void onClick(View v) { setAsScreensaver(); }
        });
        h24Btn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) { prefs.setUse24h(!prefs.use24h()); refresh(); }
        });
        secBtn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) { prefs.setShowSeconds(!prefs.showSeconds()); refresh(); }
        });
        dateBtn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) { prefs.setShowDate(!prefs.showDate()); refresh(); }
        });
        colorBtn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) {
                prefs.setColorIndex((prefs.colorIndex() + 1) % Prefs.COLORS.length);
                refresh();
            }
        });
        nightBtn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) { prefs.setNightDim(!prefs.nightDim()); refresh(); }
        });
        backlightBtn = addButton(list, new View.OnClickListener() {
            @Override public void onClick(View v) { prefs.setLowBacklight(!prefs.lowBacklight()); refresh(); }
        });
        addButton(list, "Preview clock", new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, PreviewActivity.class));
            }
        });

        status = new TextView(this);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        status.setTextColor(0xFF9E9E9E);
        status.setPadding(0, dp(20), 0, 0);
        list.addView(status);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF000000);
        scroll.addView(list);
        setContentView(scroll);
        idleBtn.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();   // permission may have changed in system settings
    }

    // ---------- actions ----------

    /** Writes the "Start after" time of the screensaver (Settings.System.SCREEN_OFF_TIMEOUT). */
    private void applyIdleTime() {
        if (!canWriteSettings()) {
            try {
                Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
                toast("Allow \"Modify system settings\", then come back and press Apply again");
            } catch (ActivityNotFoundException e) {
                toast("This TV has no permission screen. Use the adb command shown below.");
            }
            refresh();
            return;
        }
        try {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, prefs.idleMinutes() * 60_000);
            toast("Screensaver will start after " + prefs.idleMinutes() + " min idle");
        } catch (SecurityException e) {
            toast("Not allowed: " + e.getMessage());
        }
        refresh();
    }

    /** Selects this clock as the active screensaver (needs WRITE_SECURE_SETTINGS via adb). */
    private void setAsScreensaver() {
        ContentResolver cr = getContentResolver();
        try {
            Settings.Secure.putString(cr, "screensaver_components", dreamComponent());
            Settings.Secure.putInt(cr, "screensaver_enabled", 1);
            Settings.Secure.putInt(cr, "screensaver_activate_on_sleep", 1);
            toast("Lite Digital Clock is now the screensaver");
        } catch (SecurityException e) {
            // No secure permission: let the user pick it in system settings.
            try {
                startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS));
                toast("Choose \"Lite Digital Clock\" as the screensaver");
            } catch (ActivityNotFoundException e2) {
                try {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                } catch (ActivityNotFoundException ignored) { }
                toast("Settings > Device Preferences > Screen saver > Lite Digital Clock");
            }
        }
        refresh();
    }

    // ---------- UI ----------

    private void refresh() {
        idleBtn.setText("Start after idle:  " + prefs.idleMinutes() + " min");
        h24Btn.setText("Time format:  " + (prefs.use24h() ? "24 hour" : "12 hour"));
        secBtn.setText("Show seconds:  " + onOff(prefs.showSeconds()));
        dateBtn.setText("Show date:  " + onOff(prefs.showDate()));
        int ci = prefs.colorIndex();
        if (ci < 0 || ci >= Prefs.COLOR_NAMES.length) ci = 0;
        colorBtn.setText("Clock colour:  " + Prefs.COLOR_NAMES[ci]);
        nightBtn.setText("Extra dim at night (22:00-06:00):  " + onOff(prefs.nightDim()));
        backlightBtn.setText("Low backlight while running:  " + onOff(prefs.lowBacklight()));

        ContentResolver cr = getContentResolver();
        int curMs = Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, -1);
        String curDream = Settings.Secure.getString(cr, "screensaver_components");
        boolean active = curDream != null && curDream.contains(ClockDreamService.class.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("TV now: screensaver starts after ")
          .append(curMs > 0 ? (curMs / 60_000.0f) + " min" : "unknown")
          .append("  |  clock selected: ").append(active ? "yes" : "no").append('\n');
        if (!canWriteSettings()) {
            sb.append("\nIdle-time permission missing. From a PC run:\n")
              .append("  adb shell appops set ").append(getPackageName()).append(" WRITE_SETTINGS allow\n");
        }
        if (!active) {
            sb.append("\nTo select the clock from the app, run once:\n")
              .append("  adb shell pm grant ").append(getPackageName())
              .append(" android.permission.WRITE_SECURE_SETTINGS\n")
              .append("or pick it in Settings > Device Preferences > Screen saver.\n");
        }
        status.setText(sb.toString());
    }

    private Button addButton(LinearLayout parent, View.OnClickListener l) {
        return addButton(parent, "", l);
    }

    private Button addButton(LinearLayout parent, String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setFocusable(true);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        parent.addView(b, lp);
        return b;
    }

    private boolean canWriteSettings() {
        return Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(this);
    }

    private String dreamComponent() {
        return new ComponentName(this, ClockDreamService.class).flattenToString();
    }

    private static String onOff(boolean b) { return b ? "On" : "Off"; }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
