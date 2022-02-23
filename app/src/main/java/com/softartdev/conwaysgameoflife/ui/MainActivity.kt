package com.softartdev.conwaysgameoflife.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.softartdev.conwaysgameoflife.MainService
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.databinding.ActivityMainBinding
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var bound: Boolean
        get() = MainServiceConnection.bound
        set(value) {
            MainServiceConnection.bound = value
        }
    private val iCellState: ICellState = CellState.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mainCellLayout.setOnCellClickListener { x, y ->
            val inverted = iCellState.invertLifeGeneration(x, y) ?: return@setOnCellClickListener
            repaint(inverted)
        }
        updateStartButtonText()
        binding.mainStartButton.setOnClickListener {
            iCellState.toggleGoNextGeneration()
            updateStartButtonText()
        }
        binding.mainStepButton.setOnClickListener {
            val processed = iCellState.processNextGeneration() ?: return@setOnClickListener
            repaint(processed)
        }
        binding.mainRandomButton.setOnClickListener {
            val randomized = iCellState.randomizeLifeGeneration() ?: return@setOnClickListener
            repaint(randomized)
        }
        binding.mainCleanButton.setOnClickListener {
            val cleaned = iCellState.cleanLifeGeneration() ?: return@setOnClickListener
            repaint(cleaned)
        }
        startService(Intent(this, MainService::class.java))
    }

    override fun onStart() {
        super.onStart()
        if (!bound) {
            MainServiceConnection.mainActivity = this
            bindService(Intent(this, MainService::class.java), MainServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            MainServiceConnection.mainActivity = null
            unbindService(MainServiceConnection)
            bound = false
        }
    }

    fun updateStartButtonText() {
        val toggle = iCellState.isGoNextGeneration ?: return
        binding.mainStartButton.text = if (toggle) getString(R.string.stop) else getString(R.string.start)
    }

    fun repaint(generation: Array<BooleanArray>) {
        binding.mainStepsTextView.text = getString(R.string.steps, iCellState.countGeneration)
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