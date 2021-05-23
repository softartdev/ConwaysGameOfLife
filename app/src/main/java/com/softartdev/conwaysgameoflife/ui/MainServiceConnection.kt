package com.softartdev.conwaysgameoflife.ui

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.softartdev.conwaysgameoflife.MainService
import com.softartdev.conwaysgameoflife.model.ICellState

object MainServiceConnection : ServiceConnection {

    var mainActivity: MainActivity? = null
        set(value) {
            if (value == null) {
                mainService.uiRepaint = null
            }
            field = value
        }

    var iCellState: ICellState? = null
    var bound = false

    private lateinit var mainService: MainService

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val mainBinder = service as? MainService.MainBinder ?: return
        mainService = mainBinder.service
        iCellState = mainService.iCellState
        mainActivity?.updateStartButtonText()
        mainActivity?.let { mainService.uiRepaint = it::repaint }
        mainService.uiRepaint?.invoke(mainService.iCellState.lifeGeneration)
        bound = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mainService.uiRepaint = null
        iCellState = null
        bound = false
    }
}