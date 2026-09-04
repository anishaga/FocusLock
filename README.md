# Focus Lock v1.5

A deliberately small Android prototype.

## Schedule

- 04:00 → 07:00 BLOCKED
- 07:00 → 09:00 ALLOWED
- 09:00 → 13:30 BLOCKED
- 13:30 → 15:30 ALLOWED
- 15:30 → 21:00 BLOCKED
- 21:00 → 04:00 ALLOWED

(This now matches `Schedule.kt` exactly, including the early-morning block
that this file previously omitted.)

## Safety boundaries

This app deliberately does NOT use VPNService, Device Admin, root, screen-lock APIs, firewall/hosts changes, or boot automation.

The only special capability is the Accessibility Service that you explicitly enable in Android Settings, scoped (`android:packageNames`) to only ever receive events from Chrome.

Emergency exit:
1. Android Settings → Accessibility → Focus Lock → turn the service OFF.
2. Or uninstall Focus Lock.

This app cannot itself lock the phone.

Note: some banking/finance apps warn about, or refuse to run while, *any* accessibility service is enabled anywhere on the device — this is a fraud-prevention check on their side, unrelated to what a given service actually does, and there's no way for this app to opt out of that check. Disable the service before using those apps, or set up Android's built-in Accessibility Shortcut (Settings → Accessibility → Focus Lock → Shortcut) for a one-tap/volume-key toggle instead of navigating here each time.

## Chrome detection

The service looks for Chrome's URL-bar node (`com.android.chrome:id/url_bar`) and only accepts exact X/Twitter domains, matched by parsing the host out of the address-bar text (handles bare domains, `http`/`https`, and a `www.` prefix).

There is intentionally no fallback that inspects the rest of the page. An earlier version scanned the whole page for text resembling a blocked domain whenever the URL-bar node wasn't found, which caused false positives — a Chrome "Most visited" shortcut tile for x.com, or a Google search result captioned "twitter.com", could trigger a block on an unrelated page. If this resource ID ever stops matching on a given Chrome build, the app fails open (doesn't block) rather than blocking everything. If you notice X slipping through undetected on a particular device/Chrome version, that's the thing to fix — not by scanning more of the page, but by finding what changed about the URL-bar node on that build.

## Leaving a blocked tab open in the background

If a tab is left open on X (backgrounded rather than closed or navigated away from), Chrome can resume directly into that same tab next time it's opened — including after "Leave" — with no chance to reach Chrome's own tab switcher to close it before the lock screen reappears. `UnlockManager.startLeaveGrace()` gives a 10-second window after Leave/back during which the service won't re-block, specifically so there's time to close or switch away from that tab inside Chrome. It never touches the schedule or override state, only delays re-detection.

## Build

Open the project in IntelliJ IDEA or Android Studio with a configured Android SDK.

Build:

    gradlew.bat assembleDebug

APK:

    app/build/outputs/apk/debug/app-debug.apk

## Launcher icon

Adaptive icon (`res/mipmap-anydpi-v26/`), vector-only (no raster/PNG assets, so it scales cleanly to any density) - a simple padlock mark on the app's ink color, plus a monochrome variant for Android 13+ themed icons. Since minSdk is 26 (the same version adaptive icons were introduced in), there's no legacy fallback icon to maintain.


