package com.example.ayo.data;

public class Pit {

    private int seeds;

    public Pit(int initialSeedCount) {

        if (initialSeedCount < 4)
            throw new IllegalArgumentException("Initial seeds in a Pit must be at least 4");

        seeds = initialSeedCount;
    }

    public int getSeedsCount() {
        return seeds;
    }

    public void incrementSeedCount() {
        seeds += 1;
    }

    public void clear() {
        seeds = 0;
    }
}
