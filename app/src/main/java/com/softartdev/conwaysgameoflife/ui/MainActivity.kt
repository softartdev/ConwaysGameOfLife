package com.softartdev.conwaysgameoflife.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.softartdev.conwaysgameoflife.MainService
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.databinding.ActivityMainBinding
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState
import com.softartdev.conwaysgameoflife.ui.MainServiceConnection.bound

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
            with(iCellState) { if (toggleGoNextGeneration()) cancelTimer() else resumeTimer() }
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
        binding.mainSeekBar.progress = iCellState.period
        binding.mainPeriodTextView.text = getString(R.string.period, binding.mainSeekBar.progress)
        binding.mainSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 0) {
                    onProgressChanged(seekBar, 1, fromUser)
                    return
                }
                binding.mainPeriodTextView.text = getString(R.string.period, progress)
                iCellState.updatePeriod(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, MainService::class.java))
        if (!bound) {
            MainServiceConnection.mainActivity = this
            val mainServiceIntent = Intent(this, MainService::class.java)
            bindService(mainServiceIntent, MainServiceConnection, Context.BIND_AUTO_CREATE)
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
        binding.mainStartButton.text =
            if (iCellState.isGoNextGeneration) getString(R.string.stop) else getString(R.string.start)
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