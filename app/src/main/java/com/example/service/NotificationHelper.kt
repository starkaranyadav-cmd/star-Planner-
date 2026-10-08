package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "routine_tracker_alarm_channel"
        const val CHANNEL_NAME = "Daily Routine Alarms"
        private const val TAG = "NotificationHelper"

        private var activeToneGenerator: ToneGenerator? = null
        private var toneHandler: Handler? = null
        private var isAlarmSoundActive = false

        fun stopActiveAlarmSound() {
            try {
                isAlarmSoundActive = false
                toneHandler?.removeCallbacksAndMessages(null)
                activeToneGenerator?.stopTone()
                activeToneGenerator?.release()
                activeToneGenerator = null
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping active alarm sound", e)
            }
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows prominent lock-screen and status-bar routine alarms with dismiss & snooze controls"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendRoutineNotification(
        activityName: String,
        description: String,
        notificationId: Int = 101,
        isSnoozed: Boolean = false
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_HIGHLIGHT_ACTIVITY", activityName)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Turn Off Alarm
        val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS_ALARM
            putExtra(AlarmActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(AlarmActionReceiver.EXTRA_ACTIVITY_TITLE, activityName)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 5 Min
        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE_ALARM
            putExtra(AlarmActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(AlarmActionReceiver.EXTRA_ACTIVITY_TITLE, activityName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titlePrefix = if (isSnoozed) "⏰ SNOOZED: " else "⏰ Time for: "

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("$titlePrefix$activityName")
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$activityName\n$description\n\nTap 'Off' to dismiss or 'Snooze' for 5 minutes."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .addAction(
                android.R.drawable.ic_delete,
                "Off Alarm",
                dismissPendingIntent
            )
            .addAction(
                android.R.drawable.ic_lock_idle_alarm,
                "Snooze 5 Min",
                snoozePendingIntent
            )
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing: ${e.message}")
        }
    }

    private fun getToneTypeForName(toneName: String): Int {
        return when (toneName) {
            "Radar Pulse" -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            "Zen Chime" -> ToneGenerator.TONE_PROP_ACK
            "Urgent Warning" -> ToneGenerator.TONE_SUP_ERROR
            "Gentle Bell" -> ToneGenerator.TONE_PROP_BEEP
            else -> ToneGenerator.TONE_PROP_BEEP2 // Double Beep
        }
    }

    fun previewSampleTone(toneName: String, vibrationEnabled: Boolean = true) {
        stopActiveAlarmSound()
        try {
            val sampleGen = ToneGenerator(AudioManager.STREAM_ALARM, 95)
            val toneType = getToneTypeForName(toneName)
            sampleGen.startTone(toneType, 400)
            if (vibrationEnabled) vibrateDevice(250)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    sampleGen.stopTone()
                    sampleGen.release()
                } catch (_: Exception) {}
            }, 600)
        } catch (e: Exception) {
            Log.e(TAG, "Error previewing tone", e)
        }
    }

    fun playAlarmBeep(
        pulsing: Boolean = true,
        toneName: String = "Double Beep",
        vibrationEnabled: Boolean = true
    ) {
        stopActiveAlarmSound()
        isAlarmSoundActive = true
        try {
            activeToneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneHandler = Handler(Looper.getMainLooper())
            val toneType = getToneTypeForName(toneName)

            if (pulsing) {
                var beepCount = 0
                val runnable = object : Runnable {
                    override fun run() {
                        if (!isAlarmSoundActive || beepCount >= 10) {
                            stopActiveAlarmSound()
                            return
                        }
                        try {
                            activeToneGenerator?.startTone(toneType, 350)
                            if (vibrationEnabled) vibrateDevice(350)
                        } catch (e: Exception) {
                            Log.e(TAG, "Tone playback error", e)
                        }
                        beepCount++
                        toneHandler?.postDelayed(this, 1200)
                    }
                }
                toneHandler?.post(runnable)
            } else {
                activeToneGenerator?.startTone(toneType, 500)
                if (vibrationEnabled) vibrateDevice(400)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start tone generator", e)
        }
    }

    fun scheduleSnooze(activityName: String, minutes: Int = 5) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            sendRoutineNotification(
                activityName = activityName,
                description = "Snooze timer expired. Resuming schedule block.",
                notificationId = 888,
                isSnoozed = true
            )
            playAlarmBeep()
        }, (minutes * 60 * 1000).toLong())
    }

    private fun vibrateDevice(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }
}
