package com.lite.scrsvrtv;

import android.service.dreams.DreamService;

/**
 * The screensaver. Android TV starts it by itself after the device has been
 * idle for the "Start after" time (Settings.System.SCREEN_OFF_TIMEOUT).
 * Any remote key press wakes the TV up again.
 */
public class ClockDreamService extends DreamService {
    private ClockController controller;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Prefs prefs = new Prefs(this);
        setInteractive(false);           // any key exits
        setFullscreen(true);
        setScreenBright(!prefs.lowBacklight());
        setContentView(R.layout.clock_view);
        controller = new ClockController(findViewById(R.id.clock_root), prefs);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        if (controller != null) controller.start();
    }

    @Override
    public void onDreamingStopped() {
        if (controller != null) controller.stop();
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        if (controller != null) controller.stop();
        super.onDetachedFromWindow();
    }
}
