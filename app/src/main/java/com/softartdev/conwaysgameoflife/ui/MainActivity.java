package com.softartdev.conwaysgameoflife.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.softartdev.conwaysgameoflife.R;
import com.softartdev.conwaysgameoflife.model.CellState;

import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView stepsTextView;
    private CellLayout cellLayout;
    private CellState cellState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepsTextView = findViewById(R.id.main_steps_text_view);
        cellLayout = findViewById(R.id.main_cell_layout);
        cellState = CellState.getInstance();
        cellLayout.setOnCellListener((x, y) -> {
            boolean[][] inverted = cellState.invertLifeGeneration(x, y);
            repaint(inverted);
        });
        Button startButton = findViewById(R.id.main_start_button);
        startButton.setOnClickListener(view -> {
            boolean toggle = cellState.toggleGoNextGeneration();
            startButton.setText(toggle ? getString(R.string.start) : getString(R.string.stop));
        });
        Button stepButton = findViewById(R.id.main_step_button);
        stepButton.setOnClickListener(view -> {
            boolean[][] processed = cellState.processNextGeneration();
            repaint(processed);
        });
        Button randomButton = findViewById(R.id.main_random_button);
        randomButton.setOnClickListener(view -> {
            boolean[][] randomized = cellState.randomizeLifeGeneration();
            repaint(randomized);
        });
        Button cleanButton = findViewById(R.id.main_clean_button);
        cleanButton.setOnClickListener(view -> {
            boolean[][] cleaned = cellState.cleanLifeGeneration();
            repaint(cleaned);
        });
        final Handler uiHandler = new Handler();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (cellState.isGoNextGeneration()) {
                    boolean[][] processed = cellState.processNextGeneration();
                    uiHandler.post(() -> repaint(processed));
                }
            }
        };
        cellState.scheduleTimer(timerTask);
    }

    private void repaint(boolean[][] generation) {
        String steps = getString(R.string.steps, cellState.getCountGeneration());
        stepsTextView.setText(steps);
        cellLayout.repaint(generation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_rules) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.rules_title)
                    .setMessage(R.string.rules_text)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cellState.cancelTimer();
    }
}
