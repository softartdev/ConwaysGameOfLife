package com.softartdev.conwaysgameoflife

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState
import com.softartdev.conwaysgameoflife.util.NOTIFICATION_ID
import com.softartdev.conwaysgameoflife.util.createNotification
import com.softartdev.conwaysgameoflife.util.createNotificationChannel
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

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        notificationManager.createNotificationChannel(applicationContext)
        val timerTask: TimerTask = object : TimerTask() {
            val uiHandler = Handler()
            override fun run() {
                if (iCellState.isGoNextGeneration) {
                    val processed = iCellState.processNextGeneration()
                    Timber.d("Process generation step: %s", iCellState.countGeneration)
                    if (serviceRunningInForeground) {
                        val message = getString(R.string.steps, iCellState.countGeneration)
                        val notification = createNotification(message, applicationContext)
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
        val message = getString(R.string.steps, iCellState.countGeneration)
        val notification = createNotification(message, applicationContext)
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
}
