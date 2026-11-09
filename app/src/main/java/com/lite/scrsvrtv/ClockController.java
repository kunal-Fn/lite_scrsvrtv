package com.lite.scrsvrtv;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextClock;

import java.util.Calendar;
import java.util.Random;

/**
 * Drives the clock layout: applies the user options, dims at night and moves
 * the clock to a new spot every minute so a TV panel never gets burn-in.
 */
final class ClockController {
    private static final long MOVE_EVERY_MS = 60_000L;

    private final View root;
    private final View box;
    private final TextClock time;
    private final TextClock date;
    private final Prefs prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            moveRandomly();
            applyNightDim();
            handler.postDelayed(this, MOVE_EVERY_MS);
        }
    };

    ClockController(View root, Prefs prefs) {
        this.root = root;
        this.prefs = prefs;
        box = root.findViewById(R.id.clock_box);
        time = root.findViewById(R.id.clock_time);
        date = root.findViewById(R.id.clock_date);
        applyPrefs();
    }

    private void applyPrefs() {
        String fmt24 = prefs.showSeconds() ? "HH:mm:ss" : "HH:mm";
        String fmt12 = prefs.showSeconds() ? "h:mm:ss" : "h:mm";
        // TextClock picks 24h/12h from the system; force the user's choice.
        if (prefs.use24h()) {
            time.setFormat24Hour(fmt24);
            time.setFormat12Hour(fmt24);
        } else {
            time.setFormat24Hour(fmt12);
            time.setFormat12Hour(fmt12);
        }
        time.setTextSize(prefs.showSeconds() ? 120 : 160);

        int idx = prefs.colorIndex();
        if (idx < 0 || idx >= Prefs.COLORS.length) idx = 0;
        int color = Prefs.COLORS[idx];
        time.setTextColor(color);
        date.setTextColor(color);
        date.setVisibility(prefs.showDate() ? View.VISIBLE : View.GONE);
    }

    private void applyNightDim() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean night = hour >= 22 || hour < 6;
        box.setAlpha(prefs.nightDim() && night ? 0.35f : 0.85f);
    }

    private void moveRandomly() {
        int freeX = root.getWidth() - box.getWidth();
        int freeY = root.getHeight() - box.getHeight();
        if (freeX <= 0 || freeY <= 0) return;
        box.animate()
                .translationX(random.nextInt(freeX + 1))
                .translationY(random.nextInt(freeY + 1))
                .setDuration(1500)
                .start();
    }

    void start() {
        applyNightDim();
        // Center first, once the views have a size.
        root.post(new Runnable() {
            @Override public void run() {
                box.setTranslationX((root.getWidth() - box.getWidth()) / 2f);
                box.setTranslationY((root.getHeight() - box.getHeight()) / 2f);
            }
        });
        handler.postDelayed(tick, MOVE_EVERY_MS);
    }

    void stop() {
        handler.removeCallbacks(tick);
    }
}
