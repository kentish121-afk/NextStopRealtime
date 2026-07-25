package com.example.nextstoprealtime.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.nextstoprealtime.MainActivity
import com.example.nextstoprealtime.R

object NotificationHelper {

    const val CHANNEL_ID_ARRIVALS = "bus_arrivals"
    const val CHANNEL_NAME_ARRIVALS = "Bus arrival alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ARRIVALS,
                CHANNEL_NAME_ARRIVALS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders when your bus is about to arrive"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showArrivalReminder(
        context: Context,
        notificationId: Int,
        lineName: String,
        destination: String,
        stopName: String,
        minutesAway: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (minutesAway <= 0) {
            "$lineName is due now"
        } else {
            "$lineName in $minutesAway min"
        }

        val text = "$destination • $stopName"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ARRIVALS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with custom icon later
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }
}
