package com.anish.focuslock

import android.content.Context

object UnlockManager {
    private const val PREFS = "focus_lock"
    private const val OVERRIDE_UNTIL = "override_until"

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

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
