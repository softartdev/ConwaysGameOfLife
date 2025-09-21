package com.softartdev.conwaysgameoflife.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.softartdev.conwaysgameoflife.MainService
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.databinding.ActivityMainBinding
import com.softartdev.conwaysgameoflife.model.CellState
import com.softartdev.conwaysgameoflife.model.ICellState

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val iCellState: ICellState = CellState.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            return@setOnApplyWindowInsetsListener insets
        }
        binding.mainCellLayout.setOnCellClickListener { x, y ->
            val inverted = iCellState.invertLifeGeneration(x, y) ?: return@setOnCellClickListener
            repaint(inverted)
        }
        updateStartButtonText()
        binding.mainStartButton.setOnClickListener {
            if (checkNotificationPermissionGranted(toggle = true)) toggleGame()
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
        checkNotificationPermissionGranted()
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, MainService::class.java))
        if (!MainServiceConnection.bound) {
            MainServiceConnection.mainActivity = this
            val mainServiceIntent = Intent(this, MainService::class.java)
            bindService(mainServiceIntent, MainServiceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (MainServiceConnection.bound) {
            MainServiceConnection.mainActivity = null
            unbindService(MainServiceConnection)
            MainServiceConnection.bound = false
        }
    }

    private fun checkNotificationPermissionGranted(toggle: Boolean = false): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> true
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS) -> {
                showNotificationPermissionExplanation()
                false
            }
            else -> {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    when {
                        isGranted -> if (toggle) toggleGame()
                        else -> showNotificationPermissionExplanation()
                    }
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
                false
            }
        }
        else -> true
    }

    private fun toggleGame() {
        with(iCellState) { if (toggleGoNextGeneration()) cancelTimer() else resumeTimer() }
        updateStartButtonText()
    }

    fun updateStartButtonText() {
        val textResId: Int = if (iCellState.isGoNextGeneration) R.string.stop else R.string.start
        binding.mainStartButton.text = getString(textResId)
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
        R.id.action_rules -> showRules()
        else -> super.onOptionsItemSelected(item)
    }

    private fun showRules(): Boolean = AlertDialog.Builder(this)
        .setTitle(R.string.rules_title)
        .setMessage(R.string.rules_text)
        .setPositiveButton(android.R.string.ok, null)
        .show() != null

    private fun showNotificationPermissionExplanation(): AlertDialog? = AlertDialog.Builder(this)
        .setTitle(getString(R.string.notification_permission_title))
        .setMessage(getString(R.string.notification_permission_message))
        .setPositiveButton(getString(R.string.open_settings)) { _, _ -> openAppSettings() }
        .setNegativeButton(getString(R.string.cancel)) { _, _ -> /* Do nothing */ }
        .show()

    private fun openAppSettings() {
        val uri = Uri.fromParts("package", packageName, null)
        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        startActivity(appSettingsIntent)
    }
}