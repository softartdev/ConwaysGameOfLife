package com.softartdev.conwaysgameoflife.model;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CellState {
    private static CellState INSTANCE;
    public static final int LIFE_SIZE = 10;
    private static final int SHOW_DELAY = 500;
    private Timer timer;
    private boolean[][] lifeGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    private boolean[][] nextGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    private int countGeneration = 0;
    private volatile boolean goNextGeneration = false;
    private Random random = new Random();

    private CellState() {
    }

    public void scheduleTimer(TimerTask timerTask) {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(timerTask, 0L, SHOW_DELAY);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public boolean toggleGoNextGeneration() {
        boolean go = goNextGeneration;
        synchronized (this) {
            goNextGeneration = !go;
        }
        return go;
    }

    public boolean[][] invertLifeGeneration(int dx, int dy) {
        synchronized(this) {
            lifeGeneration[dx][dy] = !lifeGeneration[dx][dy];
        }
        return lifeGeneration;
    }

    public boolean[][] processNextGeneration() {
        processOfLife();
        return nextGeneration;
    }

    public boolean[][] randomizeLifeGeneration() {
        synchronized (this) {
            countGeneration = 1;
            for (int x = 0; x < LIFE_SIZE; x++) {
                for (int y = 0; y < LIFE_SIZE; y++) {
                    lifeGeneration[x][y] = random.nextBoolean();
                }
            }
        }
        return lifeGeneration;
    }

    public boolean[][] cleanLifeGeneration() {
        synchronized (this) {
            for (int x = 0; x < LIFE_SIZE; x++) {
                Arrays.fill(lifeGeneration[x], false);
            }
        }
        return lifeGeneration;
    }

    public boolean isGoNextGeneration() {
        return goNextGeneration;
    }

    public int getCountGeneration() {
        return countGeneration;
    }

    // the main process of life
    private synchronized void processOfLife() {
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

    // count the number of neighbors
    private synchronized int countNeighbors(int x, int y) {
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

    public static synchronized CellState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CellState();
        }
        return INSTANCE;
    }
}
