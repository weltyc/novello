package com.welty.ggf;

import junit.framework.TestCase;

public class GgfMatchTest extends TestCase {
    public void test1() {
        GgfMatch m = GgfMatch.of("1 (;GM[Othello]PC[GGS/os]DT[2010.10.17_05:23:05.MDT]PB[hercule]PW[scorp+]RB[2082.33]RW[1669.67]TI[10:00//02:00]TY[8]RE[+6.000]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[D3//3.95]W[c5/-18.40/0.10]B[E6//2.30]W[d2/-2.42/0.04]B[C3//2.48]W[e3/0.46]B[F3//2.03]W[f5/2.46]B[F6//2.91]W[c2/0.46]B[F4//2.61]W[c4/-10.46]B[D6//4.02]W[g4/-10.49]B[G3//3.60]W[c6/16.49]B[B5//2.50]W[e2/0.49]B[B4//3.08]W[g5/-4.49]B[F2//3.18]W[h3/-4.56]B[H6//2.90]W[e7/-4.56]B[F7//3.07]W[c7/-4.56]B[D1//3.68]W[b6/-2.56]B[E8//3.37]W[d7/-2.56]B[G6//14.23]W[h5/0.30/0.01]B[C1//21.32]W[e1/0.76/0.01]B[C8//5.11]W[a3/-1.36]B[A4//3.36]W[a5/-1.43]B[D8//3.72]W[b7/-3.46]B[H4//3.05]W[b3/-3.42]B[H2//3.28]W[b1/-0.25]B[B2//3.29]W[h7/-6.00/0.02]B[H8//2.95]W[g2/-6.00]B[G1//3.37]W[g8/-6.00]B[F8//2.83]W[g7/-6.00]B[A7//3.00]W[a8/-6.00]B[B8//2.26]W[a6/-6.00]B[A2//2.76]W[a1/-6.00]B[H1//2.54]W[f1];)");
        assertEquals(1, m.getGames().size());
        assertEquals("hercule", m.getGames().get(0).getBlackPlayer().name);
    }

    public void test2() {
        GgfMatch m = GgfMatch.of("2 (;GM[Othello]PC[GGS/os]DT[2010.10.17_07:21:46.MDT]PB[Saio7000]PW[Saio3001]RB[2216.27]RW[2210.66]TI[05:00//02:00]TY[s8]RE[+0.000]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[e6/0.62/0.01]W[f4//0.01]B[d3/0.63/0.01]W[c4/0.61/0.01]B[c3/0.64/0.01]W[d6/0.62/0.01]B[f6/0.64/0.01]W[c6/0.64/0.01]B[f5/0.68/0.01]W[g5/0.65/0.01]B[g4/0.71/0.01]W[f7/0.71/0.01]B[h6/0.76/0.01]W[g6/0.75/0.01]B[e7/0.78/0.01]W[f8/0.82/0.01]B[d8/0.83/0.01]W[e2/0.78/0.01]B[c5/0.84/0.01]W[b5/0.81/0.01]B[e3/0.84/0.01]W[h5/0.88/0.01]B[h4/0.89/0.01]W[f3/0.92/0.01]B[h7/0.95/0.01]W[f2/0.97/0.01]B[d2/0.97/0.01]W[b3/0.96/0.01]B[c2/0.97/0.01]W[c7/0.95/0.01]B[d7/0.97/0.01]W[e8/0.98/0.01]B[g8/0.99/0.01]W[e1//0.01]B[c1//0.01]W[g3//37.31]B[g1//0.01]W[f1//0.01]B[d1//0.01]W[g2//0.01]B[a4//0.01]W[b7//0.01]B[b4//0.01]W[a5//0.01]B[c8//0.01]W[a3//0.01]B[h1//0.01]W[b6//0.01]B[a6//0.01]W[b2//0.01]B[a1//0.01]W[b8//0.01]B[a8//0.01]W[a7//0.01]B[a2//0.01]W[b1//0.01]B[h2//0.01]W[h3//0.01]B[pass]W[h8//0.01]B[pass]W[g7//0.01];)(;GM[Othello]PC[GGS/os]DT[2010.10.17_07:21:46.MDT]PB[Saio3001]PW[Saio7000]RB[2210.66]RW[2216.27]TI[05:00//02:00]TY[s8]RE[+0.000]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[d3//0.01]W[c5/0.63/0.01]B[e6/0.61/0.01]W[f5/0.64/0.01]B[f6/0.61/0.01]W[e3/0.65/0.01]B[c3/0.61/0.01]W[d2/0.62/0.01]B[c4/0.60/0.01]W[b5/0.65/0.01]B[f4/0.65/0.01]W[d6/0.70/0.01]B[f3/0.72/0.01]W[b4/0.76/0.01]B[c7/0.75/0.01]W[e2/0.78/0.01]B[c6/0.78/0.01]W[f2/0.80/0.01]B[f1/0.80/0.01]W[g3/0.82/0.01]B[d1/0.84/0.01]W[e1/0.86/0.01]B[c1/0.85/0.01]W[c2/0.85/0.01]B[g4/0.85/0.01]W[d7/0.86/0.01]B[b6/0.87/0.01]W[g6/0.91/0.01]B[g5/0.92/0.01]W[e7/0.96/0.01]B[a5/0.96/0.01]W[f7/0.98/0.01]B[h6/0.99/0.01]W[a4//0.01]B[a3//0.01]W[a6//6.41]B[a7//0.01]W[b3//0.01]B[c8//4.69]W[d8//0.01]B[a2//0.01]W[h4//0.01]B[h2//0.01]W[h3//0.01]B[h5//0.01]W[b8//0.01]B[g2//0.01]W[b2//0.01]B[g8//0.01]W[f8//0.01]B[g7//0.01]W[h8//0.01]B[e8//0.01]W[h7//0.01]B[a8//0.01]W[h1//0.01]B[g1//0.01]W[b7//0.01]B[b1//0.01]W[a1//0.01];)");
        assertEquals(2, m.getGames().size());
        assertEquals("Saio7000", m.getGames().get(0).getBlackPlayer().name);
        assertEquals("Saio3001", m.getGames().get(1).getBlackPlayer().name);
    }

    // commented out because it takes a long time and the underlying file might not always be available.
    // any time this fails, create a new test in GgfMatch or GgfGame.
//    public void testToString() throws IOException, CompressorException {
//        final String filename = "Othello.latest.223270.bz2";
//        final Path path = Paths.get(filename);
//
//        try (BufferedReader in = GgfMatch.getBufferedReaderForBZ2File(path)) {
//            String line;
//            while (null != (line = in.readLine())) {
//                final GgfMatch match = GgfMatch.of(line);
//                if (!line.equals(match.toString())) {
//                    System.out.println(line);
//                    assertEquals(line.replace(']', '\n'), match.toString().replace(']', '\n'));
//                }
//            }
//        }
//    }
}
