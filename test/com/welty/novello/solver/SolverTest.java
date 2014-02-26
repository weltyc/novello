package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.othello.api.AbortCheck;
import junit.framework.TestCase;

import static com.welty.novello.core.BitBoardUtils.reflection;

/**
 */
public class SolverTest extends TestCase {
    private static final SolverTestCase[] testCases = {
// some trivial positions:
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO..", 62, 64),
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*.", 64),
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*O..", 64),
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*OO.", 56),
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*O...", 63, 64),
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*O*O....", 49, 50),
            new SolverTestCase("**********************************************************O*O...", -64),
            new SolverTestCase("***********************************************************O*...", -63, -64),
            new SolverTestCase("************************************************************O*..", -64),
            new SolverTestCase("*************************************************************O*.", -58),
            new SolverTestCase("***********************************************O*******O*****O*.", -54),
            new SolverTestCase("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*OOOOOOO*OOOO***.", 64),

// added by me:
            new SolverTestCase("OOOOOOO*OO***OO.O*O*O*O.O**O****O*O**O*OOO****OOO.****O..******O", -10),

// Harder positions with 12 empties
            new SolverTestCase("..OOOOO.*.OO*O..**O**OOO*O***OOO**O**OOO***O*OOO..**O*O....****O", -12),
            new SolverTestCase("O**O.*...OO*OOO.OOOO*OOOOOO*O***OOOO*O*.OOOOO***..OO***..OOOOO..", -6),
            new SolverTestCase("..O.**.*O.O*****OOOO****O*OOOO**O***OO*OOO*****OO.O****O......*O", 28),
            new SolverTestCase("*OOOOOO..*OOOOO..***OOOO.*****OO.****OOO.*****OO..****O...*****O", -8),
            new SolverTestCase("*.O*O....*OOOO.*******************O********OO****.OOOO....O.****", -4),
            new SolverTestCase("..*O*..*.**OOOOO***O**O**OO***O**OOO*O***.OOOO***.OOO*.*...O***.", -2),
            new SolverTestCase("...*OO**..***OO*.*****OO**O****.***O****.***OO**..******..****O*", 12),
            new SolverTestCase("..****O*..***O**.**OO***OOOO****.OO*****.OO**O**.**OOO...*OOOOO.", -12),
            new SolverTestCase("..*.....OO**.*..OOO******OOO**OO**O**OOO***OOOOO*O**O*..*OOOOOOO", 4),
            new SolverTestCase("*.O.*...**.**O.O******OO*****OOO*OOOOOOO*O*OOOO.**OOOO.*..OOOOO.", -22),
            new SolverTestCase("*..*....*.****.***OOOO******O****O*O*****O*O*O***OOOOO..*OOOOO..", -12),
            new SolverTestCase("O***.OO.O****O.*O**OO*.*O**OO*O*OO*OOOO.OO*OOOOOO*OOOO....OO..O.", 8),
            new SolverTestCase("*******...OOOO.******OO*.*O*OOO****OOOO*.***OOO*..OO*OO...OOOOO.", 4),
            new SolverTestCase("*.O.*...**.**O.O******OO*****OOO*OOOOOOO*O*OOOO.**OOOO.*..OOOOO.", -22),
            new SolverTestCase("*******...OOOO.******OO*.*O*OOO****OOOO*.***OOO*..OO*OO...OOOOO.", 4),
            new SolverTestCase("*..*..O.*****O.**O*OOO***O**O****O*O*****O***O******OO..*.*.OO..", -12),
            new SolverTestCase("*..*....*.****.***OOOO******O****O*O*****O*O*O***OOOOO..*OOOOO..", -12),
            new SolverTestCase("...****...****.O*****OO.O*O*OOO.O****OOO******OO.******O.OOOOOO.", 20),
            new SolverTestCase("..***.....***O.OOOOO*OOO*OO****OOO*OO**O.*OO*O*O*.OO***O..OOOO*O", -8),
            new SolverTestCase("O..****.*******.O*O*OO**OO**O***OO**O***O.OOOO**..OOOO.*..OOOO..", 8),
            new SolverTestCase("..*****.OO*****..OO****O**OOO**O**OOO**O*****O*OO.OOOOOO.....O.O", 4),
            new SolverTestCase(".***....O.****.OO*******O**O****O*O*OO**OO****O*O.***OOO...*OO.O", 2),
            new SolverTestCase("..OOOOO.**OOOO.****OOO***O**O*O**O**O*****O******.OOOO......OOO.", -12),
            new SolverTestCase("O..***..******..O*O*O***O**OO***O*O*O***O**OOO**.*OOOO.*..OOOO..", 8),
            new SolverTestCase(".*******..OO*O*.OOOOO*OO.O*O*OO*.*O**O**.O******...*****..OOOOOO", 0),
            new SolverTestCase("..*****O..****OO..***O*O..**O**O.O*OO**OOO**OOOOOO****.*******..", 6),
            new SolverTestCase(".OOOOO...OO****.OOOO****OOOO*O*.OOOOOOO.O*OOOOOO***OOO..OOOO..O.", -6),
            new SolverTestCase("*.O.*...**.***..***OOOOO*****OOO*O**OO*O**OO*OOO*OOOOOOOO....O*O", -2),
            new SolverTestCase("...****...****.O*****OO.O*O*OOO.O****OOO******OO.******O.OOOOOO.", 20),
            new SolverTestCase(".O****.O..****.O.O*OOO*O.**OOOOO.**OOO*O.*O*O*OO.****OO.O*OOOOO.", 14),
            new SolverTestCase("..O**O.O..OOOOO*OOOOOO**OOO*O*O*OO*O*OO*O***O***O.******..**....", 8),
            new SolverTestCase("..*****..**********O****O***O********O**.***OOO..****OOO....**O.", 12),
            new SolverTestCase("..O..*OO...O****..OO***O******OO****O*OO*O**OOOO**OOOOO.*****.O.", -12),
            new SolverTestCase(".OOOOO..*.O**O.O.**OO*OOOO**OO*.OOO*OO**OO*O*O**..**O**...******", 14),
            new SolverTestCase("....O.....OOOO.OOOOO****OOO*O***OOO*OO**OO*OO*O*.******O.OOOOOOO", -10),
            new SolverTestCase(".OO.*.....OO**..O**OO***O***OO**O*****O*O****O**.***O*.*.*******", 12),
            new SolverTestCase("..O****OO.**OOOOO***OOOOO**OO*OOOOOOOOOOO.O**OOOO.O..OOO...***..", 0),
            new SolverTestCase(".O****.O..****.O.O*OOO*O.**OOOOO.**OOO*O.*O*O*OO.****OO.O*OOOOO.", 14),
            new SolverTestCase("..****..O.*OO...OO**OO..O**O*OOOO***OOOOO****O*O.**O***O.******O", -8),
            new SolverTestCase(".**.O....***OO.*O*O**O**OO*OO***OO*OOO*.OOO**OOO.OOOOOOO..*OOOO.", -8),
            new SolverTestCase(".....O....OOOOOO**O***O*.*OOOOO****OO*O***O**O**********O*****..", 2, 4),
            new SolverTestCase("O..*OO..*OO********O********O***..*OOO*...OO*OOO.OOOOOOO.OOOOOO.", -2),
            new SolverTestCase("OOOOO*O...OOOOO.**O*****.**OO***********.*OOO****.OOOO....OOOOO.", 18),
            new SolverTestCase("..O.O*..OO*OOO..OO**O*O.O**O**O.O***O*OOOO*OOO*OO******O*.**..*O", 8),
            new SolverTestCase("..O**O..OOO*****OO******OOO***O*OOO*OOOO.O**O*.O.****O...*.OOOO.", -6),
            new SolverTestCase("..O***.....*.*OOOO**O*OOOO**OOOO.O*OO*OO.OO**OO*.OOOOOO*.OOOOOOO", -10),
            new SolverTestCase("*.OOOOO.**OOOO*.*O*OO***.*O**O**.O*O****O.*O***O...OO**O..*O.O*O", -2),
            new SolverTestCase("..*OOO....****...O*O**.OOO*OOOOOOO**O*OOOO***O*OOOO****O..O****O", -6),
            new SolverTestCase("******...*O********OO**O**O***OO.*O***O..*O*O*O...OO*O*...OOOOO*", 2),
            new SolverTestCase("O.O.....O.OOO*..OOOO*OO**O***OO**O*OO*O***OO*****OO*OO.*OOOOOO..", 2),
            new SolverTestCase("....*.O*..******..******..****O.OO***OOOO****OOO***OOOOO*OOOOOOO", 0),
            new SolverTestCase(".******.*.****.***OOOO****OO*OO***O*O*O*.O*OOO**O.OOOO.*...O*O..", 10),
            new SolverTestCase(".OOOOOO...O**O..O.*OO*O..***O*OO***O**OO**O****O.O*****O..O****O", -6),
            new SolverTestCase("..O**O...OOOOO****O*OOO****O**O*******O******O**..****.*..****..", -8),
            new SolverTestCase("..O**O..OOO*****OO******OOO***O*OOO*OOOO.O**O*.O.****O...*.OOOO.", -6),
            new SolverTestCase("*..OOO...*OOOOOO*O****OO*O***O*O*******O*******O*..****O...O..*O", 4),
            new SolverTestCase(".*.OOO....****...OO***.OOOOO*OOOOOO*O*OOOO***O*OOOO****O..O****O", -6),
            new SolverTestCase(".*****...OOOO***OOO*O**.*O*OO**O**O*O**O******OO...***OO....O**O", 18),
            new SolverTestCase("..****.*..*O***.********************O***O*******..****...OOOOOO.", 12),
            new SolverTestCase(".O*O.....***O*..*O*O*O...OOOOOO*OOOO*O**OOOOO****O*O****.*OOOOO*", 0),
            new SolverTestCase(".OOOOO....O**O..*****OO.***O*OO.***OO*OO****O*OO.O*****O..*****O", 2),
            new SolverTestCase("OOO****O*******OO*O*OOOO.O*OOO*OOOOOO*OO*.***OOO...**OO......*O.", -2),
            new SolverTestCase(".*.OOOO...***O...OO*O*.OOOOO***.OOO*O***OO***O**OOO*****..O****O", -6),
            new SolverTestCase("..*.*.O.O.****.*.***O*****O**O***OOO*OO**OOOO*O**.OOOO**..OOOO.*", 0),
            new SolverTestCase(".OOOOO...****O.**O*OOO***OO*O****O*O***.OOO*O*.O.OO**O...OOOOOO.", -4),
            new SolverTestCase("..***O...*****..*OO*******OO*****O**OOOO**O*OOOO*.*O*OO..*.OOOO.", 6),
            new SolverTestCase("..*****.O.**O*..O**O*OO.****OOO.*OOOOOO.*OOOOOOO*.OOOOO..*OOOOOO", -6),
            new SolverTestCase("..O.O*....OOO*.O**OOO*OO*OOO*OOO***OO*OOOOO*OO*O..OO****..OOOOOO", -14),
            new SolverTestCase("..******O.****.*OO*O*OO*O*O*OOO*OO**O*O*OOOOOO**...O.*O*....**OO", -22),
            new SolverTestCase("...*O....**OO*.*OO*OO***OOO*O***.OO******OO***O*..OOOOO*.*OOO***", -8),
            new SolverTestCase("..O*OOO*O****OO*O***O*O*OOOO*OO*O*O*O*O*OO*OOO**O..*OO.*.....O..", -10),
            new SolverTestCase("..**O..O..*OOO**OO**OOO*O**OOOOOO*OOOOOOOOOOOOO.O*OOO...OOOOOO..", 18),
            new SolverTestCase("..OOOOO..***OO.O***OOOOO***OOO*O**O*OOOO*O***OOO******..*.*...*.", 14),
            new SolverTestCase(".******...**O*..OO*O*OO*OOO*OOOOOOOO**O*.OOO**O..OO*OOO...*OOOO*", -4),
            new SolverTestCase(".***..OO*.*O*O**O*O*OO**O**OOO*OO*OO*O..OOOOOOO.O*OOO...OOOOOO..", 18),
            new SolverTestCase("..OOOO....OOOO*.**OOO**O*OOO*O*OOOOOOO*****O*O*O.***OO..*OOOOO..", 6),
            new SolverTestCase(".*******.**O.O*.O**OO*O*O****O.OO*O**OO.O**OO*..O******..OOOOO..", -1, -2),
            new SolverTestCase("O***O*...****O..O*OOOOO*OO*OOO**OOOOO*O*OOO*OO**..O*OO.*..OOOO..", 2),
            new SolverTestCase("..O**...**O***..***OOO*O**O********O*O**.***O***OO*O***..OO**O..", -2),
            new SolverTestCase("*******..*OOO*.O.O*****O.OO**O*O.OOOO**O..OO*O*O...O***O.OOOOOOO", 2),
            new SolverTestCase("*.O..O.*.*OOOOOO.**O**OO*****O*OO*OOOOOO..*****O..*****O..*****O", -2),
            new SolverTestCase(".*****..*.****O.**O**O*.*OOO*****OO*O**O*O*****.***OOO..**...OOO", -2),
            new SolverTestCase("OOOOOOO..O***O.O.OO****O.O*O***OOOOO*OOO*.OO*O*O..OO***O...O*.*O", 12),
            new SolverTestCase(".***OOO..**OOOOOO*OOOOOOOO***OOOO*O***OOOO**O***O.***.....**O...", -12),
            new SolverTestCase("OOOOOOO..O***O.O.OO****O.O*O***OOOOO*OOO*.OO*O*O..OO***O...O*.*O", 12),
            new SolverTestCase("..O.O*..O.OOOO...O**OO**.*O***O*O****OO*OOO*****O.*O****.*****O*", -2),
            new SolverTestCase("..OO....O.OOO...OOO**O**OO****O*O*OO**OO**OOO****.*OOO**.****OO*", 2),
            new SolverTestCase(".OOOOO..*.O**O..*O*O**O.***O*OOO****OOOO****OO.*.*****O...OOOOOO", -2),
            new SolverTestCase("...O**..**.O***.****OOOO*****OOO**O*OOOO.O*O*OOOO.****...OOOOOOO", 4),
            new SolverTestCase("..OOOO.***OOOO*.***OO*OOO****O*******OO.***O*O.O.**O.O...***.OOO", -12),
            new SolverTestCase("O.O****O.O*****.**O**O*..*OO*O*O***O***OO******O..*****....OOOO.", 6),
            new SolverTestCase("..**.*....****.*OOOOOO**.*O*O*****************O**.OOOOO*..*OOOO*", 6),
            new SolverTestCase("..OOO*..*.OO**..****O******O******O*OO**.*O*O*OOO.****O...O****O", 6),
            new SolverTestCase("*****O..*OO*.O..***OOO..**OOOOO.*OO*O*O.*O**OO*OO.**O**..OO****O", -25, -26),
            new SolverTestCase("..*****.O.****.*O*******O*****O*O*O*OOO.*.OOOOO..*OOOO...*******", -25, -26),
            new SolverTestCase("O.O****O.O*****.**O**O*..*OO*O*O***O***OO******O..*****....OOOO.", 6),
            new SolverTestCase("..O****O..*OOOOOO*O*O*OO***OOO*OO.O*O*OO.O*O*OOO..**OOO..****O..", 2),
            new SolverTestCase("..****....****..OOOOO*O*OOOOOO**OOO*OO**OO*O*O*.O******O...****O", 33, 34),
            new SolverTestCase(".******.OOOO*O..OOO*O*O.O*OOOO*.O*******OOOOOOO.O.O*OO....O*OOOO", -20),
            new SolverTestCase(".OOOOOO...****..*.**O***OO**O**.*O**O**.*OO****.OOOO*O*..*******", -4),

    };

    /**
     * Run through all Solver Test Cases.
     */
    public static void testSolveValues() {
        final Solver solver = new Solver();
        testSolveValues(solver);
    }

    static void testSolveValues(Solver solver) {
        for (SolverTestCase testCase : testCases) {
            // Do the test with the board reflected in various directions for two reasons:
            // 1. We get an additional test case for free
            // 2. Timings are heavily influenced by move ordering, and this balances out variations in move ordering.
            for (int i = 0; i < 8; i++) {
                final long w = reflection(testCase.white, i);
                final long b = reflection(testCase.black, i);
                executeTestCase(solver, w, b, testCase.expectedValue());
            }
        }
    }

    // node counts from the C version of ntest
    private static final int[] nodeCounts = {
            0, 1, 3, 1, 4, 5, 4, 4, 3, 1, 1, 1, 57, 18673, 8254, 18439, 80473, 8620, 18727, 29909, 24296, 30310, 16674, 39365, 18697, 7945,
            16674, 7945, 17076, 39365, 7032, 26328, 38431, 23909, 16353, 26725, 28943, 8083, 39726, 19540, 11599, 7032, 81698, 10663,
            60011, 28655, 19788, 18344, 23022, 10058, 81698, 7169, 15194, 39028, 50263, 18465, 30561, 12405, 39135, 17048, 17089, 33376,
            37468, 10970, 5919, 18990, 20599, 12405, 13806, 35093, 25131, 5732, 23492, 14282, 27689, 22700, 6534, 14019, 17239, 10061,
            17219, 16125, 36920, 57214, 28906, 21459, 16406, 30825, 32698, 26872, 14008, 57691, 7129, 21837, 58173, 12869, 25904, 12869,
            20945, 4608, 17326, 23432, 40882, 63573, 23144, 11398, 17145, 12974, 63573, 22845, 36582, 30204, 67596
    };

    /**
     * Compare node counts to ntest's
     */
    public static void testNodeCounts() {
        System.out.format("%9s %9s%n", "Ntest", "this");
        System.out.println("--------- ---------");
        int ntest = 0;
        int novello = 0;
        final Solver solver = new Solver();
        long oldNFlips = 0;
        for (int j = 0; j < testCases.length; j++) {
            final SolverTestCase testCase = testCases[j];
            solver.clear(testCase.nEmpty());
            executeTestCase(solver, testCase.white, testCase.black, testCase.expectedValue());
            final int nt = nodeCounts[j];
            final long no = solver.getCounts().nFlips - oldNFlips;
            oldNFlips = solver.getCounts().nFlips;
            System.out.format("%,9d %,9d %s%n", nt, no, nt > 6 * no ? "<-----" : "");
            ntest += nt;
            novello += no;
        }
        System.out.println("--------- ---------");
        System.out.format("%,9d %,9d%n", ntest, novello);
    }

    private static void executeTestCase(Solver solver, long white, long black, int expected) {
        final int nEmpties = BitBoardUtils.nEmpty(white, black);
        solver.clear(nEmpties); // we use this for benchmarking. Don't cheat!
        int actual = solver.solve(white, black);
        if (expected != actual) {
            final Position position = new Position(black, white, false);
            System.out.println(position.positionString());
            assertEquals(position.toString(), expected, actual);
        }
    }

    /**
     * A board position, with white-to-move, together with the value of the position to white with perfect play.
     * <p/>
     * This is used for testing solvers.
     */
    private static class SolverTestCase extends Position {
        private final int nobodyGetsEmptiesValue;
        private final int winnerTakesEmptiesValue;

        public SolverTestCase(String boardString, int expectedValue) {
            this(boardString, expectedValue, expectedValue);
        }

        /**
         * Create a solver test case: A board and an expected value.
         * <p/>
         *
         * @param boardString   Text of the board. White is to move. See {@link Position} for
         *                      details on the format of the boardString.
         * @param expectedValue number of disks white will win by, given perfect play, with the winner NOT getting empty squares.
         * @throws IllegalArgumentException if the boardString is invalid
         */
        public SolverTestCase(String boardString, int expectedValue, int winnerTakesEmptiesValue) {
            super(boardString, false);
            nobodyGetsEmptiesValue = expectedValue;
            this.winnerTakesEmptiesValue = winnerTakesEmptiesValue;
        }

        public String toString() {
            return super.toString() + "White to play.\nExpected value to white with perfect play: " + nobodyGetsEmptiesValue + '\n';
        }

        public int expectedValue() {
            return BitBoardUtils.WINNER_GETS_EMPTIES ? winnerTakesEmptiesValue : nobodyGetsEmptiesValue;
        }
    }

    public void testSolveWithMove() {
        Position bb = testCases[2];
        final Solver solver = new Solver();
        // switch player to move because mover has no move.
        final MoveScore result = solver.getMoveScore(bb.enemy(), bb.mover());
        assertEquals(1, result.sq);
    }

    public void testSolveWithMove2() {
        Position bb = new Position("********************O*****OO*O****-********O*****O.OOO**--O---**", true);
        final Solver solver = new Solver();
        final MoveScore result = solver.getMoveScore(bb.mover(), bb.enemy());
        // A8, F8, C5 all win by 64.
        //noinspection OctalInteger
        assertTrue(result.sq == 007 || result.sq == 002 || result.sq == 035);
    }

    public void testAborts() {
        Position bb = testCases[20];
        final Solver solver = new Solver();
        try {
            solver.getMoveScore(bb.mover(), bb.enemy(), AbortCheck.ALWAYS, StatsListener.NULL);
            fail("should abort, but didn't");
        } catch (SearchAbortedException e) {
            // expected
        }
    }
}
