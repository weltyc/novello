package com.welty.novello.core;

import com.welty.novello.eval.CoefficientCalculator;
import com.welty.othello.gdk.OsClock;
import junit.framework.TestCase;

import java.util.List;

/**
 */

public class MutableGameTest extends TestCase {

    public void testUpdates() {
        final Position startPosition = Position.START_POSITION;
        final OsClock clock = new OsClock("5:00");
        final MutableGame game = new MutableGame(startPosition, "Boris", "William", "VistaNova", clock, clock);
        assertEquals(Position.START_POSITION, game.getStartPosition());
        assertEquals(Position.START_POSITION, game.getLastPosition());
        final String ggf = game.toGgf();
        assertTrue(ggf.contains("BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]"));
        assertTrue(ggf.contains("PC[VistaNova]"));
        assertTrue(ggf.contains("TY[8r]"));
        assertTrue(ggf.contains("RE[?]"));
        assertTrue(ggf.contains("PB[Boris]"));
        assertTrue(ggf.contains("TI[5:00]"));
        assertTrue(ggf.startsWith("(;GM[Othello]"));
        assertTrue(ggf.endsWith(";)"));

        game.play("F5");
        final Position nextPosition = new Position(0x000000081C000000L, 0x0000001000000000L, false);
        assertEquals(Position.START_POSITION, game.getStartPosition());
        assertEquals(nextPosition, game.getLastPosition());
        assertTrue(game.toGgf().contains("BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]"));
        assertTrue(game.toGgf().contains("B[F5]"));

        game.play("D6//1.03");
        assertTrue(game.toGgf().contains("W[D6//1.03]"));

        //
        // we're going to cheat and assume black passes here, to test passing code and to make the game shorter.
        //

        game.play("PASS");
        game.play("F4");
        game.play("PASS");
        game.play("F6");
        String ggf4 = game.toGgf();
        assertTrue(ggf4.contains("B[PA]W[F4]B[PA]W[F6]"));
        assertFalse("no final pass", game.toGgf().contains("B[PA]W[F4]B[PA]W[F6]B["));

        final List<MeValue> pvs = game.calcPositionValues();
        assertEquals(4, pvs.size());
        final boolean[] blackToMoves = {true, false, false, false};
        final int expected = BitBoardUtils.WINNER_GETS_EMPTIES ? 64 : 8;
        for (int i = 0; i < 4; i++) {
            final MeValue pv = pvs.get(i);
            assertEquals(60 - i, pv.nEmpty());
            final boolean blackToMove = blackToMoves[i];
            final int netScore = blackToMove ? -expected : expected;
            assertEquals(netScore * CoefficientCalculator.DISK_VALUE, pv.value);
        }
        assertEquals(pvs.get(0).mover, startPosition.mover());
        assertEquals(pvs.get(0).enemy, startPosition.enemy());
    }

    public void testParseUnequalTimes() {
        final String unequalClockText = "TB[05:00//02:00]TW[01:00//01:00]";
        final String unequal = ggf.replace("TI[05:00//02:00]", unequalClockText);
        final MutableGame game = MutableGame.ofGgf(unequal);
        assertEquals("5:00", game.getStartState().blackClock.toString());
        assertEquals("1:00//1:00", game.getStartState().whiteClock.toString());
        final String out = game.toGgf();
        assertTrue(out.contains("TB[5:00]"));
        assertTrue(out.contains("TW[1:00//1:00]"));
    }

    private static final String ggf = "(;GM[Othello]PC[GGS/os]DT[2003.12.15_13:24:03.MST]PB[Saio1200]PW[Saio3000]RB[2197.01]RW[2199.72]TI[05:00//02:00]TY[8]RE[+0.000]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[d3//0.01]W[c5//0.01]B[e6//0.01]W[d2//0.01]B[c6//0.01]W[d6//0.01]B[b5//0.01]W[f5//0.01]B[e7//0.01]W[f6//0.01]B[f4//0.01]W[f3//0.01]B[g4//0.01]W[d7//0.01]B[g3//0.01]W[g5//0.01]B[h6//0.01]W[h5//0.01]B[h4//0.01]W[e8//0.01]B[c7//0.01]W[h3//0.01]B[c3//0.01]W[h7//0.01]B[e3//0.01]W[b6//0.01]B[g6//5.42]W[f7//0.01]B[d8//0.01]W[c2//0.01]B[d1//0.01]W[c4//0.01]B[b4//0.01]W[a5//0.01]B[f8//0.01]W[f2//0.01]B[e2//0.01]W[a4//17.38]B[a3//19.10]W[b3//0.01]B[f1//4.90]W[g7//0.01]B[b7//0.01]W[c8//0.01]B[a6//0.01]W[a7//0.01]B[c1//0.01]W[b2//0.01]B[a8//0.01]W[b8//0.01]B[a2//0.01]W[e1//0.01]B[h8//0.01]W[g8//0.01]B[h2//0.01]W[g1//0.01]B[h1//3.80]W[g2//0.01]B[pass]W[a1//0.01]B[b1//0.01];)";

    public void testOfGgf() {
        final MutableGame game = MutableGame.ofGgf(ggf);
        assertEquals("Saio1200", game.blackName);
        assertEquals("Saio3000", game.whiteName);
        assertEquals("GGS/os", game.place);
        assertEquals("-------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *", game.getStartPosition().positionString());
        assertEquals(0, game.getLastPosition().nEmpty());
        assertEquals(0, game.getLastPosition().terminalScore());
        assertEquals(5 * 60., game.getStartState().whiteClock.tCurrent);
        assertEquals(5 * 60., game.getStartState().blackClock.tCurrent);
    }

    public void testCalcPositionAt() {
        final MutableGame game = MutableGame.ofGgf(ggf);
        assertEquals("-------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *", game.calcPositionAt(60).positionString());
        assertEquals("-------- -------- ---*---- ---**--- ---*O--- -------- -------- -------- O", game.calcPositionAt(59).positionString());
        assertEquals(game.getLastPosition(), game.calcPositionAt(0));
    }

    public void testOfVong() {
        final String text = "-WZebra   +00 d16      EML=4B:TJ,532+$\"%*-K>F#?S6][\\^UN!Z7/RYOIGW19@80AQHXP_V'&. ()";
        final MutableGame game = MutableGame.ofVong(text);
        assertEquals("WZebra", game.blackName);
        assertEquals("d16", game.whiteName);
        assertEquals(0, game.netScore());
        assertEquals(0, game.getLastPosition().nEmpty());

        System.out.println(game.toGgf());
    }

    public void testGetStateAfter() {
        final MutableGame game = MutableGame.ofGgf(ggf);
        assertEquals(game.getStartState(), game.getStateAfter(0));
    }

}
