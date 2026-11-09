package com.lite.scrsvrtv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

/** Shows the clock full screen so you can see it without waiting for idle. */
public class PreviewActivity extends Activity {
    private ClockController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.clock_view);
        View root = findViewById(R.id.clock_root);
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        controller = new ClockController(root, new Prefs(this));
    }

    @Override protected void onResume() { super.onResume(); controller.start(); }
    @Override protected void onPause() { controller.stop(); super.onPause(); }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();   // like the real screensaver: any key closes it
        return true;
    }
}
