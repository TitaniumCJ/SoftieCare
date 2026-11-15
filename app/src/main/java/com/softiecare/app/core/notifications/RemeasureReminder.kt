package com.softiecare.app.core.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.softiecare.app.MainActivity
import com.softiecare.app.R

// Local reminder to measure again after a countermeasure. Works offline (AlarmManager, no network).
object RemeasureReminder {
    const val EXTRA_INDICATOR_ID = "remeasure_indicator_id"
    internal const val EXTRA_INDICATOR_NAME = "remeasure_indicator_name"
    internal const val CHANNEL_ID = "remeasure_reminders"

    // Inexact alarm: needs no special permission; Android may deliver it a few seconds late
    fun schedule(context: Context, indicatorId: String, indicatorName: String, delayMs: Long) {
        val intent = Intent(context, RemeasureReceiver::class.java)
            .putExtra(EXTRA_INDICATOR_ID, indicatorId)
            .putExtra(EXTRA_INDICATOR_NAME, indicatorName)
        val pending = PendingIntent.getBroadcast(
            context,
            indicatorId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarms = context.getSystemService(AlarmManager::class.java)
        alarms.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMs, pending)
    }
}

class RemeasureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(RemeasureReminder.EXTRA_INDICATOR_ID) ?: return
        val name = intent.getStringExtra(RemeasureReminder.EXTRA_INDICATOR_NAME) ?: id

        // Android 13+ requires the user to allow notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                RemeasureReminder.CHANNEL_ID,
                "Re-measure reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        // Tapping the notification opens the guided measurement for this indicator
        val open = Intent(context, MainActivity::class.java)
            .putExtra(RemeasureReminder.EXTRA_INDICATOR_ID, id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val content = PendingIntent.getActivity(
            context,
            id.hashCode(),
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, RemeasureReminder.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to measure again")
            .setContentText("$name: the countermeasure wait is over. Take a new reading.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(content)
            .build()
        manager.notify(id.hashCode(), notification)
    }
}
