package com.softartdev.conwaysgameoflife.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.softartdev.conwaysgameoflife.model.CellState.LIFE_SIZE;
import static org.junit.Assert.*;

public class CellStateTest {

    private CellState cellState;

    @Before
    public void setUp() {
        cellState = CellState.getInstance();
    }

    @After
    public void tearDown() {
        cellState.cleanLifeGeneration();
        cellState = null;
    }

    @Test
    public void flickerSemaphore() {
        boolean[][] first = new boolean[LIFE_SIZE][LIFE_SIZE];
        first[1][2] = true;
        first[2][2] = true;
        first[3][2] = true;
        cellState.invertLifeGeneration(1, 2);
        cellState.invertLifeGeneration(2, 2);
        boolean[][] act = cellState.invertLifeGeneration(3, 2);
        assertArrayEquals(first, act);

        boolean[][] second = new boolean[LIFE_SIZE][LIFE_SIZE];
        second[2][1] = true;
        second[2][2] = true;
        second[2][3] = true;
        act = cellState.processNextGeneration();
        assertArrayEquals(second, act);

        act = cellState.processNextGeneration();
        assertArrayEquals(first, act);
        act = cellState.processNextGeneration();
        assertArrayEquals(second, act);
    }
}