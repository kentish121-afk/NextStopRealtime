package com.example.nextstoprealtime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Placeholder for re-scheduling reminders after device reboot.
 * In a future iteration you can persist scheduled reminders (SharedPreferences / Room)
 * and restore them here.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: load persisted reminders and re-schedule them with ArrivalReminderScheduler
            NotificationHelper.createChannels(context)
        }
    }
}
