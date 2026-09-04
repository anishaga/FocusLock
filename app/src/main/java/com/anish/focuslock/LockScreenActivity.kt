package com.anish.focuslock

import android.os.Bundle
import android.os.CountDownTimer
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LockScreenActivity : AppCompatActivity() {

    private lateinit var countdown: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        countdown = findViewById(R.id.countdownText)

        findViewById<MaterialButton>(R.id.leaveButton).setOnClickListener {
            UnlockManager.startLeaveGrace(this)
            finishAndRemoveTask()
        }

        findViewById<MaterialButton>(R.id.emergencyButton).setOnClickListener {
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
        // Same information as before (README/README-documented behavior
        // unchanged): just Schedule.nextAllowedText(), unmodified function.
        countdown.text = Schedule.nextAllowedText()
    }

    private fun showEmergencyUnlock() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_emergency_unlock, null)
        val phraseText = view.findViewById<TextView>(R.id.emergencyPhraseText)
        val input = view.findViewById<EditText>(R.id.emergencyInput)

        phraseText.text = UnlockManager.PHRASE

        // Unchanged from the original: disables the text-selection action
        // mode (copy/paste/select-all) so the phrase must be typed by hand,
        // not pasted in.
        input.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            override fun onDestroyActionMode(mode: ActionMode?) = Unit
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.emergency_title)
            .setView(view)
            .setNegativeButton(R.string.emergency_cancel, null)
            .setPositiveButton(R.string.emergency_continue, null)
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
        countdown.text = getString(R.string.emergency_armed_format, 30)
        object : CountDownTimer(30_000L, 1_000L) {
            override fun onTick(ms: Long) {
                countdown.text = getString(R.string.emergency_armed_format, (ms / 1000 + 1).toInt())
            }

            override fun onFinish() {
                AlertDialog.Builder(this@LockScreenActivity)
                    .setTitle(R.string.emergency_confirm_title)
                    .setMessage(R.string.emergency_confirm_message)
                    .setNegativeButton(R.string.emergency_confirm_no, null)
                    .setPositiveButton(R.string.emergency_confirm_yes) { _, _ ->
                        UnlockManager.grant15Minutes(this@LockScreenActivity)
                        finishAndRemoveTask()
                    }
                    .show()
            }
        }.start()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        UnlockManager.startLeaveGrace(this)
        finishAndRemoveTask()
    }
}
