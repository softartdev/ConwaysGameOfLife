package com.softartdev.conwaysgameoflife;

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

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final int LIFE_SIZE = 10;
    boolean[][] lifeGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] nextGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    ImageView[][] cellView = new ImageView[LIFE_SIZE][LIFE_SIZE];
    int countGeneration = 0;
    int showDelay = 500;
    volatile boolean goNextGeneration = false;
    Random random = new Random();
    TextView stepsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepsTextView = findViewById(R.id.main_steps_text_view);
        Button startButton = findViewById(R.id.main_start_button);
        Button stepButton = findViewById(R.id.main_step_button);
        Button randomButton = findViewById(R.id.main_random_button);
        Button cleanButton = findViewById(R.id.main_clean_button);
        for (int y = 0; y < LIFE_SIZE; y++) {
            for (int x = 0; x < LIFE_SIZE; x++) {
                String cellId = "x" + x + "y" + y;
                int resID = getResources().getIdentifier(cellId, "id", "com.softartdev.conwaysgameoflife");
                cellView[x][y] = findViewById(resID);
                final int dx = x;
                final int dy = y;
                cellView[x][y].setOnClickListener(view -> {
                    lifeGeneration[dx][dy] = !lifeGeneration[dx][dy];
                    repaint(lifeGeneration);
                });
            }
        }
        randomButton.setOnClickListener(view -> {
            countGeneration = 1;
            for (int x = 0; x < LIFE_SIZE; x++) {
                for (int y = 0; y < LIFE_SIZE; y++) {
                    lifeGeneration[x][y] = random.nextBoolean();
                }
            }
            repaint(lifeGeneration);
        });
        stepButton.setOnClickListener(view -> {
            processOfLife();
            repaint(nextGeneration);
        });
        cleanButton.setOnClickListener(view -> {
            for (int x = 0; x < LIFE_SIZE; x++) {
                Arrays.fill(lifeGeneration[x], false);
            }
            repaint(lifeGeneration);
        });
        startButton.setOnClickListener(view -> {
            startButton.setText(goNextGeneration ? getString(R.string.start) : getString(R.string.stop));
            goNextGeneration = !goNextGeneration;
        });
        final Handler uiHandler = new Handler();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (goNextGeneration) {
                    processOfLife();
                    uiHandler.post(() -> repaint(nextGeneration));
                }
            }
        };
        Timer myTimer = new Timer();
        myTimer.schedule(timerTask, 0L, showDelay);
    }

    public void repaint(boolean[][] generation) {
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                if (generation[x][y]) {
                    cellView[x][y].setImageResource(android.R.color.black);
                } else {
                    cellView[x][y].setImageResource(android.R.color.white);
                }
            }
        }
        String steps = getString(R.string.steps, countGeneration);
        stepsTextView.setText(steps);
    }

    // count the number of neighbors
    int countNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                int nX = x + dx;
                int nY = y + dy;
                nX = (nX < 0) ? LIFE_SIZE - 1 : nX;
                nY = (nY < 0) ? LIFE_SIZE - 1 : nY;
                nX = (nX > LIFE_SIZE - 1) ? 0 : nX;
                nY = (nY > LIFE_SIZE - 1) ? 0 : nY;
                count += (lifeGeneration[nX][nY]) ? 1 : 0;
            }
        }
        if (lifeGeneration[x][y]) {
            count--;
        }
        return count;
    }

    // the main process of life
    void processOfLife() {
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                int count = countNeighbors(x, y);
                nextGeneration[x][y] = lifeGeneration[x][y];
                // if are 3 live neighbors around empty cells - the cell becomes alive
                nextGeneration[x][y] = (count == 3) || nextGeneration[x][y];
                // if cell has less than 2 or greater than 3 neighbors - it will be die
                nextGeneration[x][y] = ((count >= 2) && (count <= 3)) && nextGeneration[x][y];
            }
        }
        for (int x = 0; x < LIFE_SIZE; x++) {
            System.arraycopy(nextGeneration[x], 0, lifeGeneration[x], 0, LIFE_SIZE);
        }
        countGeneration++;
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
}
