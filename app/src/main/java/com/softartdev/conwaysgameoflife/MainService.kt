package com.softartdev.conwaysgameoflife

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState
import java.util.*

class MainService : Service() {

    val iCellState: ICellState = CellState.getInstance()
    var uiRepaint: ((Array<BooleanArray>) -> Unit)? = null
    private val mainBinder = MainBinder()

    override fun onCreate() {
        super.onCreate()
        val timerTask: TimerTask = object : TimerTask() {
            val uiHandler = Handler()
            override fun run() {
                if (iCellState.isGoNextGeneration) {
                    val processed = iCellState.processNextGeneration()
                    uiHandler.post { uiRepaint?.invoke(processed) }
                }
            }
        }
        iCellState.scheduleTimer(timerTask)
    }

    override fun onBind(intent: Intent): IBinder = mainBinder

    override fun onDestroy() {
        super.onDestroy()
        iCellState.cancelTimer()
    }

    inner class MainBinder : Binder() {
        val service: MainService get() = this@MainService
    }
}
