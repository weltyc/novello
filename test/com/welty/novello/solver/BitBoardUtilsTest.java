package com.welty.novello.solver;

import java.util.Random;

import static com.welty.novello.solver.BitBoardUtils.*;

/**
 */
public class BitBoardUtilsTest extends BitBoardTestCase {
    @SuppressWarnings("OctalInteger")
    public void testReflectDiagonally() throws Exception {
        // note, given an index in octal this just switches row and col.
        testReflectDiagonally(0x01, 0x01);
        testReflectDiagonally(1L<<010, 1L<<001);
        testReflectDiagonally(1L<<007, 1L<<070);
        testReflectDiagonally(1L<<024, 1L<<042);
        testReflectDiagonally(1L<<053, 1L<<035);

    }

    private void testReflectDiagonally(long a, long b) {
        assertBitBoardEquals(a, reflectDiagonally(b));
        assertBitBoardEquals(b, reflectDiagonally(a));
    }

    /**
     * Test BitBoardUtils.calcMoves
     */
    public void testCalcMoves() {
        BitBoard board = new BitBoard("---------------------------O*------*O---------------------------", true);
        testCalcMoves(0x0000102004080000L, board.mover(), board.enemy());
        testCalcMoves(0x0000080420100000L, board.enemy(), board.mover());

        // test to ensure we don't wrap around
        testCalcMoves(0, "-------- O*------ ------*O -------O *------- -------* O------- --------");

        // test diagonals
        testCalcMoves(0x0000240000240000L, "*------* -O----O- -------- -------- -------- -------- -O----O- *------*");

        // Test the maximum number of enemy discs (6)
        testCalcMoves(1L<<56, "*OOOOOO- -------- -------- -------- -------- -------- -------- --------");

        // randomly generated test cases
        testCalcMoves(0xA050200A0C000008L, ".*.OOO*OO...***OOO.O*OOOO*O*.O.*****..**.***OO***O*O**O*OOO*.OO*");
        testCalcMoves(0x04009450001F0830L, "**.*O.O*OO*OO*OO.OO.O..**.O.*..*.**O*.****O.....**OO.OO.O*..***.");

        // generate random test cases
//        final Random random = new Random(1337);
//        generateRandomCalcMovesTest(random);
//        generateRandomCalcMovesTest(random);
    }

    private static void generateRandomCalcMovesTest(Random random) {
        final long empty = random.nextLong()&random.nextLong();
        final long mover = random.nextLong()&~empty;
        final long enemy = ~(mover|empty);

        final BitBoard board = new BitBoard(mover, enemy, true);
        System.out.format("testCalcMoves(0x%016XL, \"%s\");%n", calcMoves(board.mover(), board.enemy()), board.boardString());
    }

    /**
     * Test all 8 reflections of the mobility calc.
     *
     * @param expected Expected black mobilities.
     * @param boardString Board characters. Black to move.
     */
    private static void testCalcMoves(long expected, String boardString) {
        BitBoard board = new BitBoard(boardString, true);
        testCalcMoves(expected, board.mover(), board.enemy());
    }

    private static void testCalcMoves(long expected, long mover, long enemy) {
        for (int r=0; r<8; r++) {
            final long ex = reflection(expected, r);
            final long mv = reflection(mover, r);
            final long en = reflection(enemy, r);

            assertBitBoardEquals("reflection " + r, ex, calcMoves(mv, en));
        }
    }

    public void testAdjacent() {
        assertBitBoardEquals(0x05070E0A0EC040C0L, adjacent(0x0200000400008000L));
    }

    public void testSingletons() {
        assertBitBoardEquals(0x810000000000L, singletons(0x8100C3008181L));
    }
}
