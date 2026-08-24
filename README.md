# Focus Lock v1.1

A deliberately small Android prototype.

## Schedule

- 09:00 → 13:30 BLOCKED
- 13:30 → 15:30 ALLOWED
- 15:30 → 21:00 BLOCKED
- 21:00 → 09:00 ALLOWED

## Safety boundaries

This app deliberately does NOT use VPNService, Device Admin, root, screen-lock APIs, firewall/hosts changes, or boot automation.

The only special capability is the Accessibility Service that you explicitly enable in Android Settings.

Emergency exit:
1. Android Settings → Accessibility → Focus Lock → turn the service OFF.
2. Or uninstall Focus Lock.

This app cannot itself lock the phone.

## Chrome detection

The service first looks for Chrome's URL-bar node (`com.android.chrome:id/url_bar`) and only accepts exact X/Twitter domains. A conservative fallback handles Chrome versions that expose URL text differently.

Accessibility behavior varies by Android/Chrome/device. If URL detection does not work on your phone, do not add stronger system permissions merely to force it; adjust the Chrome-specific detector instead.

## Build

Open the project in IntelliJ IDEA or Android Studio with a configured Android SDK.

Build:

    gradlew.bat assembleDebug

APK:

    app/build/outputs/apk/debug/app-debug.apk
