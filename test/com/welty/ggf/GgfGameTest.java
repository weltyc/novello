package com.welty.ggf;

import junit.framework.TestCase;

public class GgfGameTest extends TestCase {
    private static final String ggf = "(;GM[Othello]PC[GGS/os]DT[2003.12.15_13:24:03.MST]PB[Saio1200]PW[Saio3000]" +
            "RB[2197.01]RW[2199.72]TI[05:00//02:00]TY[8]RE[+0.000]" +
            "BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]" +
            "B[d3//0.01]W[c5//0.01]B[e6//0.01]W[d2//0.01]B[c6//0.01]W[d6//0.01]B[b5//0.01]W[f5//0.01]B[e7//0.01]W[f6//0.01]B[f4//0.01]W[f3//0.01]B[g4//0.01]W[d7//0.01]B[g3//0.01]W[g5//0.01]B[h6//0.01]W[h5//0.01]B[h4//0.01]W[e8//0.01]B[c7//0.01]W[h3//0.01]B[c3//0.01]W[h7//0.01]B[e3//0.01]W[b6//0.01]B[g6//5.42]W[f7//0.01]B[d8//0.01]W[c2//0.01]B[d1//0.01]W[c4//0.01]B[b4//0.01]W[a5//0.01]B[f8//0.01]W[f2//0.01]B[e2//0.01]W[a4//17.38]B[a3//19.10]W[b3//0.01]B[f1//4.90]W[g7//0.01]B[b7//0.01]W[c8//0.01]B[a6//0.01]W[a7//0.01]B[c1//0.01]W[b2//0.01]B[a8//0.01]W[b8//0.01]B[a2//0.01]W[e1//0.01]B[h8//0.01]W[g8//0.01]B[h2//0.01]W[g1//0.01]B[h1//3.80]W[g2//0.01]B[pass]W[a1//0.01]B[b1//0.01];)";

    public void testParseGame() {
        final GgfGame game = GgfGame.of(ggf);
        assertEquals("Othello", game.game);
        assertEquals("GGS/os", game.place);
        assertEquals("2003.12.15_13:24:03.MST", game.date);
        assertEquals("Saio1200", game.blackPlayer);
        assertEquals("Saio3000", game.whitePlayer);
        assertEquals(2197.01, game.blackRating, .01);
        assertEquals(2199.72, game.whiteRating, .01);
        assertEquals("05:00//02:00", game.getBlackTimeString());
        assertEquals("05:00//02:00", game.getWhiteTimeString());
        assertEquals("8", game.getTypeString());
        assertEquals("8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *", game.getStartPositionString());
        assertEquals(61, game.moves.size());
    }

    public void testToString() {
        final GgfGame game = GgfGame.of(ggf);
        assertEquals(ggf, game.toString());
    }

    public void testKomi() {
        final String komi = "(;GM[Othello]PC[GGS/os]DT[2010.10.11_09:16:41.MDT]PB[ming1014]PW[ant]RB[1440.64]RW[1176.38]TI[15:00//02:00]TY[8k]KB[e6//0.90]KW[f5/-1.00]KM[-0.500]RE[+8.500]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[e6//0.90]W[f4/-16.47/0.24]B[e3//5.70]W[d6/-37.53/0.02]B[c5//0.70]W[f3/-52.53/0.01]B[c4//0.39]W[f6/-38.53/0.01]B[f5//1.42]W[g4/-39.53/0.01]B[c7//3.00]W[d3/-22.53/0.01]B[e7//0.84]W[c3/-12.53]B[g6//0.72]W[b4/-46.53]B[g5//0.62]W[b6/-42.53]B[a5//1.19]W[c6/-34.66]B[b5//1.42]W[d7/-34.86]B[e8//1.25]W[f7/0.86]B[h3//1.17]W[f8/10.16]B[d8//1.14]W[h4/5.52]B[h5//1.26]W[c8/0.87]B[a3//7.75]W[a4/-0.83]B[g3//1.06]W[a6/-2.32]B[a7//2.78]W[b7/-5.29]B[f2//26.33]W[f1/-2.06]B[c2//2.60]W[e2/-2.01]B[d2//1.72]W[b3/-1.65]B[e1//6.10]W[c1/-4.23]B[g1//1.92]W[h7/-2.06]B[a2//3.62]W[g7/-6.28]B[h6//1.87]W[d1/-16.14]B[b1//2.15]W[h2/-19.03]B[h8//13.15]W[g8/-17.40]B[h1//0.80]W[g2/-14.51]B[b2//16.10]W[a1]B[b8//5.46]W[a8];)";
        final GgfGame game = GgfGame.of(komi);
        assertEquals(komi.replace(']','\n'), game.toString().replace(']', '\n'));
    }

    public void testWhiteStarts() {
        final String ggf = "(;GM[Othello]PC[GGS/os]DT[2011.03.01_07:05:47.MST]PB[Saio7000]PW[SaioQuad]RB[2799.01]RW[2779.77]TI[10:00//02:00]TY[s8r19]RE[+8.000]BO[8 -------- -------- --OOO*-- -O*OO*-- --O*OO-- --O***-- --O*---- -------- O]W[b5/-8.00/91.89]B[e2/8.00/70.29]W[d8/-10.00/58.34]B[g4/8.00/122.71]W[f2/-8.00/51.58]B[d2/8.00/76.60]W[d1/-8.00/104.34]B[c1/8.00/211.08]W[f1/-8.00/66.31]B[e1/8.00/24.96]W[b1/-8.00/49.25]B[c2/8.00/16.50]W[b3/-8.00/36.30]B[a6/8.00/33.59]W[a5/-8.00/0.01]B[b6/8.00/0.01]W[a7/-8.00/0.01]B[e8/8.00/0.01]W[f8/-8.00/0.01]B[a3/8.00/0.01]W[e7/-8.00/0.01]B[b8/8.00/0.01]W[g7/-8.00/0.01]B[h8/8.00/0.01]W[g3/-8.00/0.01]B[f7/8.00/0.01]W[h5/-8.00/0.01]B[b2/8.00/0.01]W[c8/-8.00/0.01]B[g8/8.00/0.01]W[a1/-8.00/0.01]B[a2/8.00/0.01]W[a4/-8.00/0.01]B[g6/8.00/0.01]W[g5/-8.00/0.01]B[h3/8.00/0.01]W[h4/-8.00/0.01]B[b7/8.00/0.01]W[h6/-8.00/0.01]B[h7/8.00/0.01]W[a8/-8.00/0.01]B[h2/8.00/0.01]W[g2/-8.00/0.01]B[g1/8.00/0.01]W[h1/-8.00/0.01];)";
        final GgfGame game = GgfGame.of(ggf);
        assertEquals(ggf.replace(']','\n'), game.toString().replace(']','\n'));
    }

    public void testGetBoardSize() {
        assertEquals(8, GgfGame.of(ggf).getBoardSize());
    }

    public void testIsAnti() {
        assertFalse(GgfGame.of(ggf).isAnti());
        final String anti = ggf.replace("TY[8]", "TY[8a]");
        assertTrue(GgfGame.of(anti).isAnti());
    }
}
