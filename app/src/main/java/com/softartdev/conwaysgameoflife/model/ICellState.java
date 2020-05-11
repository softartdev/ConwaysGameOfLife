package com.softartdev.conwaysgameoflife.model;

import java.util.TimerTask;

public interface ICellState {
    void scheduleTimer(TimerTask timerTask);
    void cancelTimer();
    boolean[][] getLifeGeneration();
    boolean toggleGoNextGeneration();
    boolean[][] invertLifeGeneration(int dx, int dy);
    boolean[][] processNextGeneration();
    boolean[][] randomizeLifeGeneration();
    boolean[][] cleanLifeGeneration();
    boolean isGoNextGeneration();
    int getCountGeneration();
}
