package com.softartdev.conwaysgameoflife

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState
import com.softartdev.conwaysgameoflife.ui.MainActivity
import timber.log.Timber
import java.util.*


class MainService : Service() {

    val iCellState: ICellState = CellState.getInstance()
    var uiRepaint: ((Array<BooleanArray>) -> Unit)? = null
    private val mainBinder = MainBinder()

    private var serviceRunningInForeground = false

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(applicationContext)
    }
    private val notificationBuilder: NotificationCompat.Builder by lazy {
        createNotificationBuilder(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        createNotificationChannel(applicationContext, notificationManager)
        val timerTask: TimerTask = object : TimerTask() {
            val uiHandler = Handler()
            override fun run() {
                if (iCellState.isGoNextGeneration) {
                    val processed = iCellState.processNextGeneration()
                    Timber.d("Process generation step: %s", iCellState.countGeneration)
                    if (serviceRunningInForeground) {
                        val notification = createNotification(applicationContext, iCellState.countGeneration, notificationBuilder)
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    } else {
                        uiHandler.post { uiRepaint?.invoke(processed) }
                    }
                } else notificationManager.cancelAll()
            }
        }
        iCellState.scheduleTimer(timerTask)
    }

    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")
        stopForeground(true)
        serviceRunningInForeground = false
        return mainBinder
    }

    override fun onRebind(intent: Intent?) {
        Timber.d("onRebind")
        stopForeground(true)
        serviceRunningInForeground = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")
        val notification = createNotification(applicationContext, iCellState.countGeneration, notificationBuilder)
        startForeground(NOTIFICATION_ID, notification)
        serviceRunningInForeground = true
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        iCellState.cancelTimer()
    }

    inner class MainBinder : Binder() {
        val service: MainService get() = this@MainService
    }

    companion object {
        private const val NOTIFICATION_ID = 12345678

        private fun createNotificationChannel(
                applicationContext: Context,
                notificationManager: NotificationManagerCompat
        ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                    applicationContext.getString(R.string.notification_channel_id),
                    applicationContext.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }.let(notificationManager::createNotificationChannel)
        } else Unit

        private fun createNotificationBuilder(
                applicationContext: Context
        ): NotificationCompat.Builder {
            val channelId = applicationContext.getString(R.string.notification_channel_id)
            val contentIntent = Intent(applicationContext, MainActivity::class.java)
            val contentPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            return NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(R.drawable.ic_twotone_grid_on_24)
                    .setContentTitle(applicationContext.getString(R.string.notification_title))
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
        }

        private fun createNotification(
                applicationContext: Context,
                stepCount: Int,
                notificationBuilder: NotificationCompat.Builder
        ): Notification {
            val contentText = applicationContext.getString(R.string.steps, stepCount)
            return notificationBuilder
                    .setContentText(contentText)
                    .build()
        }
    }
}
