# Lite Clock Screensaver (Android TV 11)

A tiny digital clock screensaver for Android TV. Black background, big dim clock,
made for night use. Android starts it by itself when the TV has been idle for the
time you choose.

## How it works

- `ClockDreamService` is a standard Android screensaver (DreamService). The system
  starts it after the idle time and any remote key closes it.
- The clock moves to a new spot every minute so the panel never gets burn-in.
- Between 22:00 and 06:00 the text dims further, and the backlight can be lowered.
- The launcher app (`SettingsActivity`) lets you set the idle time, 12/24 h, seconds,
  date, colour (soft white, night red, amber, cyan, green) and preview the clock.

## Build

Open the folder in Android Studio and run, or:

    ./gradlew assembleDebug
    adb install -r app/build/outputs/apk/debug/app-debug.apk

## One-time setup on the TV

Most Android TV boxes hide the permission screens, so use adb once:

    # let the app change the idle time ("Start after")
    adb shell appops set com.lite.scrsvrtv WRITE_SETTINGS allow
    # let the app select itself as the screensaver
    adb shell pm grant com.lite.scrsvrtv android.permission.WRITE_SECURE_SETTINGS

Then open the app, pick the idle time, press **Apply idle time to TV** and
**Set as TV screensaver**.

Without adb you can still choose it by hand:
Settings > Device Preferences > Screen saver > *Lite Digital Clock*, and set
"Start after" there.

Note: if the TV's "Put device to sleep" time is shorter than the screensaver time,
the TV will sleep before the clock appears. Keep sleep longer (or Never).
