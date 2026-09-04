package com.anish.focuslock

import android.content.Context

object UnlockManager {
    private const val PREFS = "focus_lock"
    private const val OVERRIDE_UNTIL = "override_until"
    private const val LEAVE_GRACE_UNTIL = "leave_grace_until"
    private const val LEAVE_GRACE_MS = 10 * 1000L

    const val PHRASE =
        "I am sure this is extremely urgent and I consciously choose to break my focus rule"

    fun isOverrideActive(context: Context): Boolean =
        System.currentTimeMillis() < prefs(context).getLong(OVERRIDE_UNTIL, 0L)

    fun grant15Minutes(context: Context) {
        prefs(context).edit()
            .putLong(OVERRIDE_UNTIL, System.currentTimeMillis() + 15 * 60 * 1000L)
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(OVERRIDE_UNTIL).apply()
    }

    /**
     * Brief window after the user dismisses the lock screen (Leave, or
     * back) during which the service won't re-block, even if a blocked
     * page is still what's on screen. Without this, if Chrome resumes
     * directly into a tab that was left open on X (backgrounded rather
     * than closed/navigated away from) instead of the New Tab page, there
     * was no way to reach Chrome's own tab switcher to close it - every
     * attempt to open Chrome just re-showed the lock screen instantly.
     * This never bypasses the schedule or override - it only delays
     * re-detection for a few seconds so the user has a real chance to fix
     * their own Chrome tab state.
     */
    fun startLeaveGrace(context: Context) {
        prefs(context).edit()
            .putLong(LEAVE_GRACE_UNTIL, System.currentTimeMillis() + LEAVE_GRACE_MS)
            .apply()
    }

    fun isInLeaveGrace(context: Context): Boolean =
        System.currentTimeMillis() < prefs(context).getLong(LEAVE_GRACE_UNTIL, 0L)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
