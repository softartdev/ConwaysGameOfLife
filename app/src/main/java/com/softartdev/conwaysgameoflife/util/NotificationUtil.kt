package com.softartdev.conwaysgameoflife.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.ui.MainActivity

private const val NOTIFICATION_ID = 0

fun NotificationManagerCompat.createNotificationChannel(
        applicationContext: Context
): NotificationChannel? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channelId = applicationContext.getString(R.string.notification_channel_id)
    getNotificationChannel(channelId) ?: NotificationChannel(
            channelId,
            applicationContext.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
    ).apply {
        setShowBadge(false)
    }.also(::createNotificationChannel)
} else null

fun NotificationManagerCompat.sendNotification(messageBody: String, applicationContext: Context) {
    val channelId = applicationContext.getString(R.string.notification_channel_id)
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)//TODO change it
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)//TODO use steps string
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    notify(NOTIFICATION_ID, notificationBuilder.build())
}