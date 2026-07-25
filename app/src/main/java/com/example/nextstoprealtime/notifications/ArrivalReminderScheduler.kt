package com.example.nextstoprealtime.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant

object ArrivalReminderScheduler {

    const val EXTRA_NOTIFICATION_ID = "notification_id"
    const val EXTRA_LINE_NAME = "line_name"
    const val EXTRA_DESTINATION = "destination"
    const val EXTRA_STOP_NAME = "stop_name"
    const val EXTRA_MINUTES_AWAY = "minutes_away"

    /**
     * Schedule a reminder [minutesBefore] minutes before the expected departure.
     * Returns true if successfully scheduled.
     */
    fun schedule(
        context: Context,
        notificationId: Int,
        expectedTimeIso: String,
        lineName: String,
        destination: String,
        stopName: String,
        minutesBefore: Int = 5
    ): Boolean {
        return try {
            val expectedInstant = Instant.parse(expectedTimeIso)
            val triggerAt = expectedInstant.minusSeconds(minutesBefore * 60L).toEpochMilli()

            // Don't schedule if the time is already in the past
            if (triggerAt <= System.currentTimeMillis()) return false

            val intent = Intent(context, ArrivalReminderReceiver::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_LINE_NAME, lineName)
                putExtra(EXTRA_DESTINATION, destination)
                putExtra(EXTRA_STOP_NAME, stopName)
                putExtra(EXTRA_MINUTES_AWAY, minutesBefore)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun cancel(context: Context, notificationId: Int) {
        val intent = Intent(context, ArrivalReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
