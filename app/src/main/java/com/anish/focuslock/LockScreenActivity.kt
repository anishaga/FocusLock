package com.anish.focuslock

import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LockScreenActivity : AppCompatActivity() {

    private lateinit var countdown: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        countdown = findViewById(R.id.countdownText)

        findViewById<Button>(R.id.leaveButton).setOnClickListener {
            finishAndRemoveTask()
        }

        findViewById<Button>(R.id.emergencyButton).setOnClickListener {
            showEmergencyUnlock()
        }

        updateMessage()
    }

    override fun onResume() {
        super.onResume()
        if (!Schedule.isBlocked() || UnlockManager.isOverrideActive(this)) {
            finishAndRemoveTask()
        } else {
            updateMessage()
        }
    }

    private fun updateMessage() {
        countdown.text = "X is blocked during your focus hours.\n\n${Schedule.nextAllowedText()}"
    }

    private fun showEmergencyUnlock() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 10, 48, 0)
        }

        val explanation = TextView(this).apply {
            text = "This is intentionally difficult. Type the sentence below manually if access is genuinely urgent."
            textSize = 15f
        }

        val phrase = TextView(this).apply {
            text = "\n${UnlockManager.PHRASE}\n"
            textIsSelectable = false
        }

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            isSingleLine = false
            hint = "Type the phrase manually"
            textIsSelectable = false
            customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
                override fun onDestroyActionMode(mode: ActionMode?) = Unit
            }
        }

        container.addView(explanation)
        container.addView(phrase)
        container.addView(input)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Emergency unlock")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = false

            input.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    button.isEnabled = s?.toString()?.trim() == UnlockManager.PHRASE
                }
                override fun afterTextChanged(s: android.text.Editable?) = Unit
            })

            button.setOnClickListener {
                dialog.dismiss()
                startCountdown()
            }
        }

        dialog.show()
    }

    private fun startCountdown() {
        countdown.text = "Emergency unlock armed.\n\nWait 30 seconds..."
        object : CountDownTimer(30_000L, 1_000L) {
            override fun onTick(ms: Long) {
                countdown.text = "Emergency unlock armed.\n\nWait ${ms / 1000 + 1} seconds..."
            }

            override fun onFinish() {
                AlertDialog.Builder(this@LockScreenActivity)
                    .setTitle("Are you absolutely sure?")
                    .setMessage("This will allow X for 15 minutes. Your normal schedule will resume afterwards.")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes, unlock 15 minutes") { _, _ ->
                        UnlockManager.grant15Minutes(this@LockScreenActivity)
                        finishAndRemoveTask()
                    }
                    .show()
            }
        }.start()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        finishAndRemoveTask()
    }
}
