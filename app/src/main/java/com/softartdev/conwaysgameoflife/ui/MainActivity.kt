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
import com.softartdev.conwaysgameoflife.databinding.ActivityMainBinding
import com.softartdev.conwaysgameoflife.model.ICellState
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var iCellState: ICellState? = null
    private var bound = false

    private val serviceConnection by lazy { object : ServiceConnection {
        private lateinit var mainService: MainService
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected")
            val mainBinder = service as? MainService.MainBinder ?: return
            mainService = mainBinder.service
            iCellState = mainService.iCellState
            updateStartButtonText()
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mainCellLayout.setOnCellClickListener { x, y ->
            val inverted = iCellState?.invertLifeGeneration(x, y) ?: return@setOnCellClickListener
            repaint(inverted)
        }
        updateStartButtonText()
        binding.mainStartButton.setOnClickListener {
            iCellState?.toggleGoNextGeneration()
            updateStartButtonText()
        }
        binding.mainStepButton.setOnClickListener {
            val processed = iCellState?.processNextGeneration() ?: return@setOnClickListener
            repaint(processed)
        }
        binding.mainRandomButton.setOnClickListener {
            val randomized = iCellState?.randomizeLifeGeneration() ?: return@setOnClickListener
            repaint(randomized)
        }
        binding.mainCleanButton.setOnClickListener {
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
            bound = false
        }
    }

    private fun updateStartButtonText() {
        val toggle = iCellState?.isGoNextGeneration ?: return
        binding.mainStartButton.text = if (toggle) getString(R.string.stop) else getString(R.string.start)
    }

    private fun repaint(generation: Array<BooleanArray>) {
        binding.mainStepsTextView.text = getString(R.string.steps, iCellState?.countGeneration)
        binding.mainCellLayout.repaint(generation)
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