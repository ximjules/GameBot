package com.gamebot.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gamebot.app.MainActivity
import com.gamebot.app.R

object NotificationHelper {

    private const val CHANNEL_ID = "gamebot_channel"
    private const val CHANNEL_NAME = "GAMEBOT Obavijesti"
    private const val CHANNEL_DESC = "Obavijesti za gaming događaje i podsjetnike"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showEventNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 1
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Nema dozvole za notifikacije: ${e.message}")
        }
    }

    fun showGameReminderNotification(context: Context, gameName: String) {
        showEventNotification(
            context = context,
            title = "Vrijeme za igranje! 🎮",
            message = "Nastavi igrati $gameName",
            notificationId = 2
        )
    }

    fun showNewEventNotification(context: Context, eventName: String, time: String) {
        showEventNotification(
            context = context,
            title = "Novi događaj: $eventName 🎯",
            message = "Počinje u $time — ne zaboravi!",
            notificationId = 3
        )
    }
}