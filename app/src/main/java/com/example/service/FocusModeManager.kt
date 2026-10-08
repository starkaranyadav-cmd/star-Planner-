package com.example.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

class FocusModeManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val TAG = "FocusModeManager"
    }

    /**
     * Checks whether this app is granted Do Not Disturb (Notification Policy) access.
     */
    fun hasDndPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    /**
     * Intent to open the System Do Not Disturb access settings page.
     */
    fun getDndSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    /**
     * Activates Do Not Disturb (silences calls, SMS, and notifications).
     * Uses INTERRUPTION_FILTER_ALARMS so user schedule alarms still ring, or INTERRUPTION_FILTER_NONE.
     */
    fun setFocusModeDnd(enabled: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasDndPermission()) {
                Log.w(TAG, "Notification policy access not granted!")
                return false
            }
            try {
                if (enabled) {
                    // Mute all calls, SMS, notifications; only explicit alarms pass through
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                    Log.d(TAG, "Focus Mode DND enabled: Interruption filter set to ALARMS")
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    Log.d(TAG, "Focus Mode DND disabled: Interruption filter restored to ALL")
                }
                return true
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to toggle DND interruption filter", e)
                return false
            }
        }
        return true
    }
}
