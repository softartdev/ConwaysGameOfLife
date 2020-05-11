package com.softartdev.conwaysgameoflife.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.softartdev.conwaysgameoflife.MainService
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.model.ICellState
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var iCellState: ICellState? = null
    private var bound = false

    private val serviceConnection by lazy { object : ServiceConnection {
        private lateinit var mainService: MainService
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected")
            val mainBinder = service as? MainService.MainBinder ?: return
            mainService = mainBinder.service
            iCellState = mainService.iCellState
            mainService.uiRepaint = this@MainActivity::repaint
            mainService.uiRepaint?.invoke(mainService.iCellState.lifeGeneration)
            bound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("onServiceDisconnected")
            mainService.uiRepaint = null
            iCellState = null
            bound = false
        }
    } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_cell_layout.setOnCellClickListener { x, y ->
            val inverted = iCellState?.invertLifeGeneration(x, y) ?: return@setOnCellClickListener
            repaint(inverted)
        }
        updateStartButtonText()
        main_start_button.setOnClickListener {
            iCellState?.toggleGoNextGeneration()
            updateStartButtonText()
        }
        main_step_button.setOnClickListener {
            val processed = iCellState?.processNextGeneration() ?: return@setOnClickListener
            repaint(processed)
        }
        main_random_button.setOnClickListener {
            val randomized = iCellState?.randomizeLifeGeneration() ?: return@setOnClickListener
            repaint(randomized)
        }
        main_clean_button.setOnClickListener {
            val cleaned = iCellState?.cleanLifeGeneration() ?: return@setOnClickListener
            repaint(cleaned)
        }
        startService(Intent(this, MainService::class.java))
    }

    override fun onStart() {
        super.onStart()
        if (!bound) {
            bindService(Intent(this, MainService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(serviceConnection)
        }
    }

    private fun updateStartButtonText() {
        val toggle = iCellState?.isGoNextGeneration ?: return
        main_start_button.text = if (toggle) getString(R.string.stop) else getString(R.string.start)
    }

    private fun repaint(generation: Array<BooleanArray>) {
        main_steps_text_view.text = getString(R.string.steps, iCellState?.countGeneration)
        main_cell_layout.repaint(generation)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_rules -> {
            AlertDialog.Builder(this)
                    .setTitle(R.string.rules_title)
                    .setMessage(R.string.rules_text)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}