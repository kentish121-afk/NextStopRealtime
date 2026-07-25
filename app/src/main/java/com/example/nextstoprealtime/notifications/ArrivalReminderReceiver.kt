package com.example.nextstoprealtime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ArrivalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(ArrivalReminderScheduler.EXTRA_NOTIFICATION_ID, 0)
        val lineName = intent.getStringExtra(ArrivalReminderScheduler.EXTRA_LINE_NAME) ?: "Bus"
        val destination = intent.getStringExtra(ArrivalReminderScheduler.EXTRA_DESTINATION) ?: ""
        val stopName = intent.getStringExtra(ArrivalReminderScheduler.EXTRA_STOP_NAME) ?: ""
        val minutesAway = intent.getIntExtra(ArrivalReminderScheduler.EXTRA_MINUTES_AWAY, 5)

        NotificationHelper.showArrivalReminder(
            context = context,
            notificationId = notificationId,
            lineName = lineName,
            destination = destination,
            stopName = stopName,
            minutesAway = minutesAway
        )
    }
}
