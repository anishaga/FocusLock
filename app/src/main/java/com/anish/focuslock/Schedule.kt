package com.anish.focuslock

import java.time.LocalTime

object Schedule {
    private val morningBlockStart = LocalTime.of(9, 0)
    private val lunchStart = LocalTime.of(13, 30)
    private val lunchEnd = LocalTime.of(15, 30)
    private val eveningBlockEnd = LocalTime.of(21, 0)

    fun isBlocked(now: LocalTime = LocalTime.now()): Boolean {
        val morningBlocked = !now.isBefore(morningBlockStart) && now.isBefore(lunchStart)
        val eveningBlocked = !now.isBefore(lunchEnd) && now.isBefore(eveningBlockEnd)
        return morningBlocked || eveningBlocked
    }

    fun nextAllowedText(now: LocalTime = LocalTime.now()): String =
        when {
            now.isBefore(lunchStart) -> "Allowed again at 1:30 PM"
            now.isBefore(eveningBlockEnd) -> "Allowed again at 9:00 PM"
            else -> "Allowed now"
        }
}
