package com.softartdev.conwaysgameoflife.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.softartdev.conwaysgameoflife.model.CellState.LIFE_SIZE;
import static org.junit.Assert.*;

public class CellStateTest {

    private ICellState iCellState;

    @Before
    public void setUp() {
        iCellState = CellState.getInstance();
    }

    @After
    public void tearDown() {
        iCellState.cleanLifeGeneration();
        iCellState = null;
    }

    @Test
    public void flickerSemaphore() {
        boolean[][] first = new boolean[LIFE_SIZE][LIFE_SIZE];
        first[1][2] = true;
        first[2][2] = true;
        first[3][2] = true;
        iCellState.invertLifeGeneration(1, 2);
        iCellState.invertLifeGeneration(2, 2);
        boolean[][] act = iCellState.invertLifeGeneration(3, 2);
        assertArrayEquals(first, act);

        boolean[][] second = new boolean[LIFE_SIZE][LIFE_SIZE];
        second[2][1] = true;
        second[2][2] = true;
        second[2][3] = true;
        act = iCellState.processNextGeneration();
        assertArrayEquals(second, act);

        act = iCellState.processNextGeneration();
        assertArrayEquals(first, act);
        act = iCellState.processNextGeneration();
        assertArrayEquals(second, act);
    }
}