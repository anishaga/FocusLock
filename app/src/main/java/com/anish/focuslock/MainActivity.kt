package com.anish.focuslock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.testButton).setOnClickListener {
            startActivity(Intent(this, LockScreenActivity::class.java))
        }

        findViewById<Button>(R.id.clearOverrideButton).setOnClickListener {
            UnlockManager.clear(this)
            updateStatus()
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        statusText.text = when {
            UnlockManager.isOverrideActive(this) -> "🟡 Emergency override is active for up to 15 minutes."
            Schedule.isBlocked() -> "🔴 Currently blocked\n\n${Schedule.nextAllowedText()}"
            else -> "🟢 Currently allowed"
        }
    }
}
