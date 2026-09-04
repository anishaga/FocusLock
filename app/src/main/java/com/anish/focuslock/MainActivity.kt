package com.anish.focuslock

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var statusCard: MaterialCardView
    private lateinit var statusIcon: TextView
    private lateinit var statusHeadline: TextView
    private lateinit var statusSubtext: TextView
    private lateinit var timelineBar: LinearLayout
    private lateinit var scheduleList: LinearLayout
    private lateinit var accessibilityDot: View
    private lateinit var accessibilityStatusText: TextView

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusCard = findViewById(R.id.statusCard)
        statusIcon = findViewById(R.id.statusIcon)
        statusHeadline = findViewById(R.id.statusHeadline)
        statusSubtext = findViewById(R.id.statusSubtext)
        timelineBar = findViewById(R.id.timelineBar)
        scheduleList = findViewById(R.id.scheduleList)
        accessibilityDot = findViewById(R.id.accessibilityDot)
        accessibilityStatusText = findViewById(R.id.accessibilityStatusText)

        findViewById<MaterialButton>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<MaterialButton>(R.id.testButton).setOnClickListener {
            startActivity(Intent(this, LockScreenActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.clearOverrideButton).setOnClickListener {
            UnlockManager.clear(this)
            updateStatus()
        }

        buildTimelineBar()
        buildScheduleList()
        updateStatus()
        updateAccessibilityStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        updateAccessibilityStatus()
    }

    private fun updateStatus() {
        when {
            UnlockManager.isOverrideActive(this) -> {
                statusCard.setCardBackgroundColor(colorOf(R.color.focus_override_container))
                statusIcon.text = "\uD83D\uDFE1" // 🟡
                statusHeadline.text = getString(R.string.status_override_headline)
                statusSubtext.text = getString(R.string.override_subtext)
            }
            Schedule.isBlocked() -> {
                statusCard.setCardBackgroundColor(colorOf(R.color.focus_blocked_container))
                statusIcon.text = "\uD83D\uDD34" // 🔴
                statusHeadline.text = getString(R.string.status_blocked_headline)
                statusSubtext.text = Schedule.nextAllowedText()
            }
            else -> {
                statusCard.setCardBackgroundColor(colorOf(R.color.focus_allowed_container))
                statusIcon.text = "\uD83D\uDFE2" // 🟢
                statusHeadline.text = getString(R.string.status_allowed_headline)
                statusSubtext.text = nextBlockSubtext()
            }
        }
    }

    /**
     * Only used for the "Allowed" subtext caption. This never decides
     * blocked/allowed itself - that stays entirely driven by
     * Schedule.isBlocked() and UnlockManager.isOverrideActive() above,
     * unchanged from the original app. If anything here were ever wrong,
     * the worst case is a wrong caption, never a wrong block/allow decision.
     */
    private fun nextBlockSubtext(): String {
        val nowMinute = LocalTime.now().let { it.hour * 60 + it.minute }
        val current = Schedule.blockWindows()
            .firstOrNull { nowMinute >= it.startMinute && nowMinute < it.endMinute }
        val nextStart = current?.takeIf { !it.blocked }?.endMinute?.rem(24 * 60)
        return if (nextStart != null) {
            getString(R.string.next_block_format, formatMinuteOfDay(nextStart))
        } else {
            Schedule.nextAllowedText()
        }
    }

    private fun formatMinuteOfDay(minute: Int): String {
        val m = minute % (24 * 60)
        return LocalTime.of(m / 60, m % 60).format(timeFormatter)
    }

    private fun buildTimelineBar() {
        timelineBar.removeAllViews()
        Schedule.blockWindows().forEach { window ->
            val segment = View(this)
            segment.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                window.durationMinutes.toFloat()
            )
            segment.setBackgroundColor(
                colorOf(if (window.blocked) R.color.focus_blocked else R.color.focus_allowed)
            )
            timelineBar.addView(segment)
        }
    }

    private fun buildScheduleList() {
        scheduleList.removeAllViews()
        val inflater = LayoutInflater.from(this)
        Schedule.blockWindows().forEach { window ->
            val row = inflater.inflate(R.layout.item_schedule_row, scheduleList, false)
            val dot = row.findViewById<View>(R.id.rowDot)
            val range = row.findViewById<TextView>(R.id.rowRange)
            val status = row.findViewById<TextView>(R.id.rowStatus)

            val color = colorOf(if (window.blocked) R.color.focus_blocked else R.color.focus_allowed)
            // mutate() first so tinting this row's dot never affects other
            // views inflated from the same drawable resource.
            dot.background?.mutate()?.setTint(color)
            range.text = "${formatMinuteOfDay(window.startMinute)} \u2013 ${formatMinuteOfDay(window.endMinute)}"
            status.text = getString(
                if (window.blocked) R.string.schedule_blocked_label else R.string.schedule_allowed_label
            )
            status.setTextColor(color)

            scheduleList.addView(row)
        }
    }

    private fun updateAccessibilityStatus() {
        val enabled = isAccessibilityServiceEnabled()
        accessibilityDot.background?.mutate()?.setTint(
            colorOf(if (enabled) R.color.focus_allowed else R.color.focus_blocked)
        )
        accessibilityStatusText.text = getString(
            if (enabled) R.string.accessibility_service_on else R.string.accessibility_service_off
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        try {
            val expected = ComponentName(this, FocusLockService::class.java)
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(enabledServices)
            while (splitter.hasNext()) {
                val componentName = ComponentName.unflattenFromString(splitter.next())
                if (componentName == expected) return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    private fun colorOf(resId: Int): Int = ContextCompat.getColor(this, resId)
}
