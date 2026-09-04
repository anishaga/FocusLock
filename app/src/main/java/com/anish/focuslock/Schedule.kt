package com.anish.focuslock

import java.time.LocalTime

object Schedule {
    private val earlyMorningBlockStart = LocalTime.of(4, 0)
    private val earlyMorningBlockEnd = LocalTime.of(7, 0)
    private val morningBlockStart = LocalTime.of(9, 0)
    private val lunchStart = LocalTime.of(13, 30)
    private val lunchEnd = LocalTime.of(15, 30)
    private val eveningBlockEnd = LocalTime.of(21, 0)

    fun isBlocked(now: LocalTime = LocalTime.now()): Boolean {
        val earlyMorningBlocked =
            !now.isBefore(earlyMorningBlockStart) && now.isBefore(earlyMorningBlockEnd)

        val morningBlocked =
            !now.isBefore(morningBlockStart) && now.isBefore(lunchStart)

        val eveningBlocked =
            !now.isBefore(lunchEnd) && now.isBefore(eveningBlockEnd)

        return earlyMorningBlocked || morningBlocked || eveningBlocked
    }

    fun nextAllowedText(now: LocalTime = LocalTime.now()): String =
        when {
            now.isBefore(earlyMorningBlockStart) -> "Allowed now"
            now.isBefore(earlyMorningBlockEnd) -> "Allowed again at 7:00 AM"
            now.isBefore(morningBlockStart) -> "Allowed now"
            now.isBefore(lunchStart) -> "Allowed again at 1:30 PM"
            now.isBefore(eveningBlockEnd) -> "Allowed again at 9:00 PM"
            else -> "Allowed now"
        }

    /**
     * One contiguous blocked/allowed segment of the day, for display only.
     */
    data class BlockWindow(val startMinute: Int, val endMinute: Int, val blocked: Boolean) {
        val durationMinutes: Int get() = endMinute - startMinute
    }

    private fun minuteOf(t: LocalTime): Int = t.hour * 60 + t.minute

    /**
     * Purely descriptive breakdown of the full day into contiguous
     * blocked/allowed segments, derived from the exact same boundary times
     * isBlocked() uses above. Used only for display (the schedule list and
     * timeline bar on the home screen) - it is never consulted by
     * isBlocked() itself, so this cannot change actual blocking behavior,
     * and the display can never silently drift out of sync with reality
     * the way a separately hand-typed schedule string could.
     */
    fun blockWindows(): List<BlockWindow> {
        val boundaries = listOf(
            0,
            minuteOf(earlyMorningBlockStart),
            minuteOf(earlyMorningBlockEnd),
            minuteOf(morningBlockStart),
            minuteOf(lunchStart),
            minuteOf(lunchEnd),
            minuteOf(eveningBlockEnd),
            24 * 60
        )
        return (0 until boundaries.size - 1).map { i ->
            val start = boundaries[i]
            val end = boundaries[i + 1]
            val midMinute = (start + end) / 2
            val blocked = isBlocked(LocalTime.of(midMinute / 60, midMinute % 60))
            BlockWindow(start, end, blocked)
        }
    }
}