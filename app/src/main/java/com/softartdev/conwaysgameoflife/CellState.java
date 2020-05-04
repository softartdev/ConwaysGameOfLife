package com.softartdev.conwaysgameoflife;

public class CellState {
    private static CellState INSTANCE;

    private CellState() {
    }

    public static synchronized CellState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CellState();
        }
        return INSTANCE;
    }
}
