package com.softartdev.conwaysgameoflife.model;

public interface ICellState {
    int getPeriod();
    void scheduleTimer(Runnable runnable);
    void setRunnable(Runnable runnable);
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
