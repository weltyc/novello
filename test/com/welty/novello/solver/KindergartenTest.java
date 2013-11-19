package com.welty.novello.solver;

@SuppressWarnings("OctalInteger")
public class KindergartenTest extends BitBoardTestCase {
    /**
     * Test KindergartenEastWest class
     */
    public void testEastWest() {
        assertBitBoardEquals(0x04, KindergartenEastWest.flips(1, 0xF8, 0x04));
        assertBitBoardEquals(0x04, KindergartenEastWest.flips(1, -8, 0x04));
        assertBitBoardEquals(0, KindergartenEastWest.flips(1, -4, 0x02));
        assertBitBoardEquals(0x02, KindergartenEastWest.flips(0, -4, 0x02));
        assertBitBoardEquals("Top row", 0x0200000000000000L, KindergartenEastWest.flips(56, -4L << 56, 2L << 56));

        // positions from solver test case "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*O*O...."
        assertBitBoardEquals(0, KindergartenEastWest.flips(3, 0xFFFFFFFFFFFFFF50L, 0xA0));
        assertBitBoardEquals(0x10, KindergartenEastWest.flips(3, 0xA0, 0xFFFFFFFFFFFFFF50L));
        assertBitBoardEquals(0x38, KindergartenEastWest.flips(2, 0xFFFFFFFFFFFFFF40L, 0xB8));
        assertBitBoardEquals(0x7C, KindergartenEastWest.flips(1, 0x80, 0xFFFFFFFFFFFFFF7CL));
        assertBitBoardEquals(0, KindergartenEastWest.flips(0, 0xFFFFFFFFFFFFFF00L, 0xFE));
        assertBitBoardEquals(0, KindergartenEastWest.flips(0, 0xFE, 0xFFFFFFFFFFFFFF00L));
    }

    /**
     * Test KindergartenUpDown class
     */
    public void testUpDown() {
        assertBitBoardEquals(0x010000, KindergartenUpDown.flips(010, 0x0101010101000000L, 0x010000));
        assertBitBoardEquals(0, KindergartenUpDown.flips(0, 0x0101010101000000L, 0x010000));
        assertBitBoardEquals("From top right corner", 1L << 060, KindergartenUpDown.flips(050, 1L << 070, 1L << 060));
        assertBitBoardEquals("From top left corner", 1L << 067, KindergartenUpDown.flips(057, 1L << 077, 1L << 067));
    }

    /**
     * Test FlipperULDR class
     */
    public void testUpLeftDownRight() {
        assertBitBoardEquals(1L << 022, new FlipperULDR(011).flips(1L << 033, 1L << 022));
        assertBitBoardEquals(0, new FlipperULDR(0).flips(1L << 033, 1L << 022));
        assertBitBoardEquals("From top left corner", 1L << 066, new FlipperULDR(055).flips(1L << 077, 1L << 066));
        assertBitBoardEquals("No wraparound to the left", 0, new FlipperULDR(006).flips(1L << 030, 1L << 017));
        assertBitBoardEquals("No wraparound to the right", 0, new FlipperULDR(071).flips(1L << 047, 1L << 060));
    }

    /**
     * Test FlipperURDL class
     */
    public void testURDL() {
        assertBitBoardEquals(1L << 025, new FlipperURDL(016).flips(1L << 034, 1L << 025));
        assertBitBoardEquals(0, new FlipperURDL(0).flips(1L << 034, 1L << 025));
        assertBitBoardEquals("From top right corner", 1L << 061, new FlipperURDL(052).flips(1L << 070, 1L << 061));
        assertBitBoardEquals("No wraparound to the right", 0, new FlipperURDL(001).flips(1L << 017, 1L << 010));
        assertBitBoardEquals("No wraparound to the left", 0, new FlipperURDL(076).flips(1L << 060, 1L << 067));
    }
}
