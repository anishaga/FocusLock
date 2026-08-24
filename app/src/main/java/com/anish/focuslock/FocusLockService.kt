package com.anish.focuslock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusLockService : AccessibilityService() {

    companion object {
        private const val CHROME = "com.android.chrome"
        private const val MIN_RELAUNCH_INTERVAL_MS = 1500L
    }

    private var lastLockAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() != CHROME) return
        if (!Schedule.isBlocked()) return
        if (UnlockManager.isOverrideActive(this)) return

        val now = System.currentTimeMillis()
        if (now - lastLockAt < MIN_RELAUNCH_INTERVAL_MS) return

        val root = rootInActiveWindow ?: return
        if (!ChromeUrlDetector.isBlockedUrlVisible(root)) return

        lastLockAt = now
        startActivity(
            Intent(this, LockScreenActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    override fun onInterrupt() = Unit
}
