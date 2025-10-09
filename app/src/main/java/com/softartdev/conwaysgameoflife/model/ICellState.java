package com.softartdev.conwaysgameoflife.model;

import java.util.TimerTask;

public interface ICellState {
    int getPeriod();
    void setTimerTask(TimerTask timerTask);
    void updatePeriod(int period);
    void cancelTimer();
    void resumeTimer();
    boolean[][] getLifeGeneration();
    boolean toggleGoNextGeneration();
    boolean[][] invertLifeGeneration(int dx, int dy);
    boolean[][] processNextGeneration();
    boolean[][] randomizeLifeGeneration();
    boolean[][] cleanLifeGeneration();
    boolean isGoNextGeneration();
    int getCountGeneration();
}
