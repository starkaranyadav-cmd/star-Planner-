package com.example.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DISMISS_ALARM = "com.example.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.ACTION_SNOOZE_ALARM"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_ACTIVITY_TITLE = "extra_activity_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 101)
        val title = intent.getStringExtra(EXTRA_ACTIVITY_TITLE) ?: "Routine Alarm"

        when (intent.action) {
            ACTION_DISMISS_ALARM -> {
                Log.d("AlarmActionReceiver", "Dismissing alarm for ID: $notifId")
                NotificationHelper.stopActiveAlarmSound()
                notificationManager.cancel(notifId)
            }
            ACTION_SNOOZE_ALARM -> {
                Log.d("AlarmActionReceiver", "Snoozing alarm for ID: $notifId")
                NotificationHelper.stopActiveAlarmSound()
                notificationManager.cancel(notifId)
                // Schedule snooze notification in 5 minutes
                NotificationHelper(context).scheduleSnooze(title, 5)
            }
        }
    }
}
