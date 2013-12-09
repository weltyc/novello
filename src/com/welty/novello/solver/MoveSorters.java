package com.welty.novello.solver;

/**
 * A MoveSorter for each depth
 * <p/>
 * Having a separate MoveSorter for each depth lets us sort moves while doing another search to score moves
 * for a deeper search.
 */
class MoveSorters {
    private final MoveSorter[] sorters = new MoveSorter[64];

    MoveSorters() {
        for (int i = 0; i < sorters.length; i++) {
            sorters[i] = new MoveSorter();
        }
    }

    public MoveSorter get(int nEmpties) {
        return sorters[nEmpties];
    }
}
