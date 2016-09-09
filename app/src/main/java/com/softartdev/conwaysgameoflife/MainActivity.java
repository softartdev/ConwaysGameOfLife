package com.softartdev.conwaysgameoflife;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    final String NAME_OF_GAME = "Conway's Game of Life";
    final String SAVE_FILE_EXT = ".life";
    final int LIFE_SIZE = 10;
    boolean[][] lifeGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] nextGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    ImageView[][] cellView = new ImageView[LIFE_SIZE][LIFE_SIZE];
    int countGeneration = 0;
    int showDelay = 500;
    volatile boolean goNextGeneration = false;
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonSave = (Button)findViewById(R.id.buttonSave);
        Button buttonLoad = (Button)findViewById(R.id.buttonLoad);
        Button buttonFAQ = (Button)findViewById(R.id.buttonFAQ);
        final Button buttonStart = (Button)findViewById(R.id.buttonStart);
        Button buttonStep = (Button)findViewById(R.id.buttonStep);
        Button buttonRandom = (Button)findViewById(R.id.buttonRandom);
        Button buttonClean = (Button)findViewById(R.id.buttonClean);

        buttonFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, getString(R.string.rules), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.rulesTitle))
                        .setMessage(getString(R.string.rules))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        buttonSave.setVisibility(View.INVISIBLE);
        buttonLoad.setVisibility(View.INVISIBLE);

        for (int y = 0; y < LIFE_SIZE; y++){
            for (int x = 0; x < LIFE_SIZE; x++){
                String cellId = "x" + x + "y" + y;
                int resID = getResources().getIdentifier(cellId, "id", "com.softartdev.conwaysgameoflife");
                cellView[x][y] = (ImageView)findViewById(resID);

                final int dx = x;
                final int dy = y;
                cellView[x][y].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lifeGeneration[dx][dy] = !lifeGeneration[dx][dy];
                        repaint(lifeGeneration);
                    }
                });
            }
        }

        buttonRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countGeneration = 1;
                for (int x = 0; x < LIFE_SIZE; x++) {
                    for (int y = 0; y < LIFE_SIZE; y++) {
                        lifeGeneration[x][y] = random.nextBoolean();
                    }
                }
                repaint(lifeGeneration);
            }
        });

        buttonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processOfLife();
                repaint(nextGeneration);
            }
        });

        buttonClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int x = 0; x < LIFE_SIZE; x++) {
                    Arrays.fill(lifeGeneration[x], false);
                }
                repaint(lifeGeneration);
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStart.setText(goNextGeneration? getString(R.string.start) : getString(R.string.stop));
                goNextGeneration = !goNextGeneration;
            }
        });

        final Handler uiHandler = new Handler();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (goNextGeneration){
                    processOfLife();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            repaint(nextGeneration);
                        }
                    });
                }
            }
        };
        Timer myTimer = new Timer();
        myTimer.schedule(timerTask, 0L, (long) showDelay);

    }

    public void repaint(boolean[][] generation){
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                if (generation[x][y]) {
                    cellView[x][y].setImageResource(android.R.color.black);
                } else {
                    cellView[x][y].setImageResource(android.R.color.white);
                }
            }
        }
        setTitle(NAME_OF_GAME + " : " + countGeneration);
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
        if (lifeGeneration[x][y]) { count--; }
        return count;
    }

    // the main process of life
    void processOfLife() {
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                int count = countNeighbors(x, y);
                nextGeneration[x][y] = lifeGeneration[x][y];
                // if are 3 live neighbors around empty cells - the cell becomes alive
                nextGeneration[x][y] = (count == 3) ? true : nextGeneration[x][y];
                // if cell has less than 2 or greater than 3 neighbors - it will be die
                nextGeneration[x][y] = ((count < 2) || (count > 3)) ? false : nextGeneration[x][y];
            }
        }
        for (int x = 0; x < LIFE_SIZE; x++) {
            System.arraycopy(nextGeneration[x], 0, lifeGeneration[x], 0, LIFE_SIZE);
        }
        countGeneration++;
    }

}
