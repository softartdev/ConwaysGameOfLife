package com.softartdev.conwaysgameoflife.ui

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.model.CellState
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val cellState by lazy { CellState.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repaint(cellState.lifeGeneration)
        main_cell_layout.setOnCellClickListener { x, y ->
            val inverted = cellState.invertLifeGeneration(x, y)
            repaint(inverted)
        }
        updateStartButtonText()
        main_start_button.setOnClickListener {
            cellState.toggleGoNextGeneration()
            updateStartButtonText()
        }
        main_step_button.setOnClickListener {
            val processed = cellState.processNextGeneration()
            repaint(processed)
        }
        main_random_button.setOnClickListener {
            val randomized = cellState.randomizeLifeGeneration()
            repaint(randomized)
        }
        main_clean_button.setOnClickListener {
            val cleaned = cellState.cleanLifeGeneration()
            repaint(cleaned)
        }
        val uiHandler = Handler()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                if (cellState.isGoNextGeneration) {
                    val processed = cellState.processNextGeneration()
                    uiHandler.post { repaint(processed) }
                }
            }
        }
        cellState.scheduleTimer(timerTask)
    }

    private fun updateStartButtonText() {
        val toggle = cellState.isGoNextGeneration
        main_start_button.text = if (toggle) getString(R.string.stop) else getString(R.string.start)
    }

    private fun repaint(generation: Array<BooleanArray>) {
        main_steps_text_view.text = getString(R.string.steps, cellState.countGeneration)
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

    override fun onDestroy() {
        super.onDestroy()
        cellState.cancelTimer()
    }
}