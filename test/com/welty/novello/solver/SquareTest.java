package com.welty.novello.solver;


/**
 */
public class SquareTest extends BitBoardTestCase {
    /**
     * Test that calcFlips() works
     */
    public void testCalcFlips() {
        testCalcFlips("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO..", 0, 0);
        testCalcFlips("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*..", 1, 0x4);
        testCalcFlips("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*..", 0, 0);
    }

    private void testCalcFlips(String boardString, int sq, int expectedFlips) {
        final BitBoard bitBoard = new BitBoard(boardString, false);
        final Square square = Square.of(sq);
        long flips = square.calcFlips(bitBoard.white, bitBoard.black);
        assertBitBoardEquals(expectedFlips, flips);
    }

    public void testFillRight() {
        assertBitBoardEquals(0x080C, BitBoardUtils.fillRight(0x0808, 0x04));
        assertBitBoardEquals("no wrap", 0x0F00, BitBoardUtils.fillRight(0x0800, 0x07F0));
    }

    public void testCalcFlipsRight() {
        // OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*..
        final long placement = 2;
        final long enemy = 4;
        final long mover = -8;

        final long placementLeft = BitBoardUtils.fillLeft(placement, enemy);
        assertBitBoardEquals(0x6, placementLeft);
        final long moverRight = BitBoardUtils.fillRight(mover, enemy);
        assertBitBoardEquals(mover+enemy, moverRight);
        final long flips = placementLeft & moverRight;
        assertBitBoardEquals(0x04, flips);
    }

    public void testCalcFlipsUpLeft() {
        final long placement = 0x100;
        final long enemy = 0x04020000;
        final long mover = 0x0800000000L;
        System.out.println(new BitBoard(mover, enemy, true));
        assertBitBoardEquals(0x04020100, BitBoardUtils.fillUpLeft(placement, enemy));
        assertBitBoardEquals(0x0804020000L, BitBoardUtils.fillDownRight(mover, enemy));
    }

    public void testCalcFlipsSamplePosition() {
        final String boardText = "OOOOOOO*OO******O*O*O**OO**O**OOO*O**O*OO***O*OOO*****O.OOOOOOOO";
        BitBoard bb = new BitBoard(boardText, true);
        final long flips = Square.H7.calcFlips(bb.black, bb.white);
        assertBitBoardEquals(0x0000010105030200L, flips);
    }

    public void testFillUpLeft() {
        assertBitBoardEquals("no wrap", BitBoardUtils.BFile + BitBoardUtils.AFile-0x80, BitBoardUtils.fillUpLeft(BitBoardUtils.BFile, BitBoardUtils.AFile + BitBoardUtils.HFile));
    }
}

