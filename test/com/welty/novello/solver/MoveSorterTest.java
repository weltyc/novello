package com.welty.novello.solver;

/**
 */
public class MoveSorterTest extends BitBoardTestCase {
    /**
     * Test insertion of squares in to the move sorter keeps moves correctly ordered
     */
    public void testInsert() {
        final MoveSorter sorter = new MoveSorter();

        sorter.insert(33, 10, 0x88, 0x404, null);
        assertEquals(1, sorter.size());
        final Move move = sorter.moves[0];
        assertEquals(33, move.sq);
        assertEquals(10, move.score);
        assertEquals(0x88, move.flips);
        assertEquals(0x404, move.enemyMoves);

        sorter.insert(34, 11, 0, 0, null);
        checkSquares(sorter, 34, 33);

        sorter.insert(35, 8, 0, 0, null);
        checkSquares(sorter, 34, 33, 35);

        sorter.insert(36, 9, 0, 0, null);
        checkSquares(sorter, 34, 33, 36, 35);

        // duplicate score at end
        sorter.insert(37, 8, 0, 0, null);
        checkSquares(sorter, 34, 33, 36, 35, 37);

        // duplicate score at start
        sorter.insert(38, 11, 0, 0, null);
        checkSquares(sorter, 34, 38, 33, 36, 35, 37);
    }

    /**
     * Check that the sorter has the given squares in the given order
     */
    private void checkSquares(MoveSorter sorter, int... sqs) {
        assertEquals(sqs.length, sorter.size());
        for (int i=0; i<sqs.length; i++) {
            assertEquals("["+i+"]", sqs[i], sorter.moves[i].sq);
        }
    }
}
