package com.softartdev.conwaysgameoflife

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
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
                        updateNotificationSafely(applicationContext, iCellState, notificationBuilder, notificationManager)
                    } else {
                        uiHandler.post { uiRepaint?.invoke(processed) }
                    }
                } else notificationManager.cancelAll()
            }
        }
        iCellState.setRunnable(runnable)
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
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        serviceRunningInForeground = false
        return mainBinder
    }

    override fun onRebind(intent: Intent?) {
        Timber.d("onRebind")
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        serviceRunningInForeground = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")
        if (iCellState.isGoNextGeneration) {
            val notification = createNotification(applicationContext, iCellState.countGeneration, notificationBuilder)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType: Int = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
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
            val flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
        ): Notification = notificationBuilder
            .setContentText(applicationContext.getString(R.string.steps, stepCount))
            .build()

        private fun updateNotificationSafely(
            applicationContext: Context,
            iCellState: ICellState,
            notificationBuilder: NotificationCompat.Builder,
            notificationManager: NotificationManagerCompat,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Timber.w("No notification permission, cannot update notification")
                return
            }
            val notification = createNotification(applicationContext, iCellState.countGeneration, notificationBuilder)
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
}
