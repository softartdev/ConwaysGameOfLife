package com.softartdev.conwaysgameoflife

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState
import com.softartdev.conwaysgameoflife.ui.MainActivity
import timber.log.Timber


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
        val runnable: Runnable = object : Runnable {
            val uiHandler = Handler(Looper.myLooper()!!)
            override fun run() {
                Timber.d("Process generation step: %s", iCellState.countGeneration)//FIXME await on idle
                if (iCellState.isGoNextGeneration) {
                    val processed = iCellState.processNextGeneration()
                    if (serviceRunningInForeground) {
                        val notification = createNotification(applicationContext, iCellState.countGeneration, notificationBuilder)
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    } else {
                        uiHandler.post { uiRepaint?.invoke(processed) }
                    }
                } else notificationManager.cancelAll()
            }
        }
        iCellState.scheduleTimer(runnable)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        val cancelFromNotification = intent.getBooleanExtra(EXTRA_CANCEL_FROM_NOTIFICATION, false)
        if (cancelFromNotification) {
            Timber.d("cancel from notification")
            iCellState.toggleGoNextGeneration()
            stopSelf()
        }
        return START_NOT_STICKY
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
        if (iCellState.isGoNextGeneration) {
            val notification = createNotification(applicationContext, iCellState.countGeneration, notificationBuilder)
            startForeground(NOTIFICATION_ID, notification)
        }
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
        private const val EXTRA_CANCEL_FROM_NOTIFICATION = "extra_cancel_from_notification"

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
            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = PendingIntent.FLAG_IMMUTABLE or flags
            }
            val contentPendingIntent = PendingIntent.getActivity(applicationContext, 0, contentIntent, flags)
            val cancelIntent = Intent(applicationContext, MainService::class.java)
                .putExtra(EXTRA_CANCEL_FROM_NOTIFICATION, true)
            val cancelPendingIntent = PendingIntent.getService(applicationContext, 0, cancelIntent, flags)
            val cancelTitle = applicationContext.getString(R.string.stop)
            val cancelAction = NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, cancelTitle, cancelPendingIntent)
            return NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_twotone_grid_on_24)
                .setContentTitle(applicationContext.getString(R.string.notification_title))
                .setContentIntent(contentPendingIntent)
                .addAction(cancelAction)
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
