package com.example.ayo.data;

import org.jetbrains.annotations.NotNull;

public class Board {

    private final Pit[] pits;

    private static final int DEFAULT_NUM_OF_PITS = 12;
    private static final int DEFAULT_PITS_INITIAL_SEEDS_COUNT = 4;


    public Board() {

        this(DEFAULT_NUM_OF_PITS, DEFAULT_PITS_INITIAL_SEEDS_COUNT);
    }

    public Board(int pitsInitialSeedCount) {

        this(DEFAULT_NUM_OF_PITS, pitsInitialSeedCount);
    }

    public Board(int numOfPits, int pitsInitialSeedCount) {

        if (numOfPits < 12)
            throw new IllegalArgumentException("pits in a Board must be at least 12");

        if (numOfPits % 2 != 0)
            throw new IllegalArgumentException("pits in a Board must be even");


        pits = new Pit[numOfPits];

        int i = 0;
        do {
            pits[i] = new Pit(pitsInitialSeedCount);
            i++;
        } while (i < numOfPits);
    }


    public @NotNull Pit getPit(int pitNumber) {

        if (pitNumber < 0)
            pitNumber = 0;
        else
            pitNumber = pitNumber % pits.length;

        return pits[pitNumber];
    }

    public int getPitsCount() {

        return pits.length;
    }
}
