package com.softartdev.conwaysgameoflife.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.softartdev.conwaysgameoflife.R;
import com.softartdev.conwaysgameoflife.model.CellState;

import java.util.TimerTask;

import static com.softartdev.conwaysgameoflife.model.CellState.LIFE_SIZE;

public class MainActivity extends AppCompatActivity {

    private TextView stepsTextView;
    private ImageView[][] cellView = new ImageView[LIFE_SIZE][LIFE_SIZE];

    private CellState cellState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepsTextView = findViewById(R.id.main_steps_text_view);
        cellState = CellState.getInstance();
        for (int y = 0; y < LIFE_SIZE; y++) {
            for (int x = 0; x < LIFE_SIZE; x++) {
                String cellId = "x" + x + "y" + y;
                int resID = getResources().getIdentifier(cellId, "id", "com.softartdev.conwaysgameoflife");
                cellView[x][y] = findViewById(resID);
                final int dx = x;
                final int dy = y;
                cellView[x][y].setOnClickListener(view -> {
                    boolean[][] inverted = cellState.invertLifeGeneration(dx, dy);
                    repaint(inverted);
                });
            }
        }
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
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                int color = generation[x][y] ? android.R.color.black : android.R.color.white;
                cellView[x][y].setImageResource(color);
            }
        }
        String steps = getString(R.string.steps, cellState.getCountGeneration());
        stepsTextView.setText(steps);
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
