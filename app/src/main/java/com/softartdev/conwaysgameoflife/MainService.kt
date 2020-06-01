package com.softartdev.conwaysgameoflife

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState
import com.softartdev.conwaysgameoflife.util.createNotificationChannel
import com.softartdev.conwaysgameoflife.util.sendNotification
import timber.log.Timber
import java.util.*


class MainService : Service() {

    val iCellState: ICellState = CellState.getInstance()
    var uiRepaint: ((Array<BooleanArray>) -> Unit)? = null
    private val mainBinder = MainBinder()

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
                    uiHandler.post { uiRepaint?.invoke(processed) }
                    val message = getString(R.string.steps, iCellState.countGeneration)
                    notificationManager.sendNotification(message, applicationContext)
                } else notificationManager.cancelAll()
            }
        }
        iCellState.scheduleTimer(timerTask)
        //TODO start foreground
    }

    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")
        return mainBinder
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
