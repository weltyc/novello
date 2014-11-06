package com.welty.novello.book;

import com.orbanova.common.misc.Utils;
import com.welty.novello.core.*;
import com.welty.novello.solver.SampleGames;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static com.welty.novello.book.Book.NodeType.*;

/**
 */
public class BookTest extends TestCase {

    private static final MutableGame gameWithPass = MutableGame.ofGgf("(;GM[Othello]PC[NBoard]DT[2014-10-26 18:33:23 GMT]PB[Chris]PW[Chris]RE[?]TI[1:00]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5//2.439]W[D6]B[C7]W[F3]B[C3]W[D3]B[D2]W[C2]B[B2]W[C4]B[G2]W[C1]B[B3]W[E3]B[E2]W[D1]B[C5]W[B1]B[PA]W[A1]B[A2]W[E1]B[PA]W[A3];)");

    public void testImport() {
        final MutableGame game = SampleGames.vongGames().get(0);
        Book book = new Book().add(game);
        for (int nEmpty = 0; nEmpty <= 60; nEmpty++) {
            final int expectedCount = nEmpty >= book.minDepth() ? 1 : 0;
            assertEquals("book size at " + nEmpty, expectedCount, book.sizeAtEmpty(nEmpty));

            List<MinimalReflection> mrs = book.getMrs(nEmpty);
            assertEquals(expectedCount, mrs.size());
            for (MinimalReflection mr : mrs) {
                Book.Data data = book.getData(mr);
                assertEquals(UBRANCH, data.getNodeType());
                assertEquals(-1, data.getBestUnplayedSq());
            }
        }
    }

    public void testImportWithPass() {
        // this game has a pass and ends in a non-terminal position with 38 empties.
        Book book = new Book().add(gameWithPass);
        for (int nEmpty = 0; nEmpty <= 60; nEmpty++) {
            final int expectedCount = nEmpty >= 38 ? 1 : 0;
            assertEquals("book size at " + nEmpty, expectedCount, book.sizeAtEmpty(nEmpty));
        }

        // check loading a pass position
        final Board passPosition = new Board("-OOO----\n-*OO*-*-\n-*OO**--\n--***---\n--****--\n---*----\n--*-----\n--------\n", true);
        assertEquals(UBRANCH, book.getData(passPosition).getNodeType());
        assertEquals(UBRANCH, book.getData(passPosition.pass()).getNodeType());
    }

    public void testNotInBookWithPass() {
        Book book = new Book();
        final Board passPosition = new Board("-OOO----\n-*OO*-*-\n-*OO**--\n--***---\n--****--\n---*----\n--*-----\n--------\n", true);
        assertEquals(null, book.getData(passPosition));
    }

    public void testNotAddedToBookTerminalPosition() {
        Book book = new Book();
        final Board terminalPosition = new Board("--------\n--------\n--------\n--***---\n--****--\n--------\n--------\n--------\n", true);
        assertEquals(new Book.Data(SOLVED, 64, 0), book.getData(terminalPosition));
        assertEquals(new Book.Data(SOLVED, -64, 0), book.getData(terminalPosition.pass()));
    }

    public void testSearchForBestUnplayedMove() {
        Book book = new Book();
        book.addUnevaluatedPos(Board.START_BOARD);
        book.addUnevaluatedPos(Board.START_BOARD.play("F5"));
        final int move = book.searchForBestUnplayedMove(new TestAdder(), Board.START_BOARD);
        assertEquals(-2, move);
    }

    public void testOverwrites() {
        Book book = new Book();
        Board b = Board.START_BOARD;

        // overwrite null is possible.
        book.addUnevaluatedPos(b);
        assertEquals(new Book.Data(UBRANCH, 0, -1), book.getData(b));

        // overwrite UBRANCH is ignored.
        final int F5 = BitBoardUtils.textToSq("F5");
        book.putPos(b, 10, UBRANCH, F5);
        book.addUnevaluatedPos(b);
        assertEquals(new Book.Data(UBRANCH, 10, F5), book.getData(b));

        // overwrite SOLVED is ignored.
        book.putPos(b, 3, SOLVED);
        book.addUnevaluatedPos(b);
        assertEquals(new Book.Data(SOLVED, 3, -1), book.getData(b));

        // overwrite ULEAF is possible.
        book.putPos(b, 3, ULEAF);
        book.addUnevaluatedPos(b);
        assertEquals(new Book.Data(UBRANCH, 0, -1), book.getData(b));
    }

    public void testNegamaxSimple() {
        testNegamax(SampleGames.vongGames().get(0));
    }

    private static void testNegamax(MutableGame game) {
        Book book = new Book().add(game);
        Adder adder = new TestAdder();

        book.negamax(adder, false);
//        dump(book);

        int nEmpty = 0;

        for (; nEmpty < book.minDepth(); nEmpty++) {
            final List<MinimalReflection> mrs = book.getMrs(nEmpty);
            assertEquals(0, mrs.size());
        }
        final int solveDepth = adder.solveDepth();

        for (; nEmpty <= solveDepth; nEmpty++) {
            final List<MinimalReflection> mrs = book.getMrs(nEmpty);
            for (MinimalReflection mr : mrs) {
                final Book.Data data = book.getData(mr);
                if (data == null) {
                    fail("data for " + nEmpty + " is not in book");
                }
                switch (data.getNodeType()) {
                    case SOLVED:
                        checkScore(mr, data);
                        break;
                    case ULEAF:
                        // can only occur as a child of a UBRANCH node at depth solveDepth+1
                        assertEquals(solveDepth, nEmpty);
                        break;
                    default:
                        fail("can't have UBRANCH nodes below solve depth");
                }
            }
        }

        for (; nEmpty <= 60; nEmpty++) {
            final List<MinimalReflection> mrs = book.getMrs(nEmpty);
            for (MinimalReflection mr : mrs) {
                final Book.Data data = book.getData(mr);
                switch (data.getNodeType()) {
                    case SOLVED:
                        fail("shouldn't have solved nodes above solve depth");
                    case UBRANCH:
                        checkScore(mr, data);
                        final int bestUnplayedSq = data.getBestUnplayedSq();
                        if (bestUnplayedSq != -2) {
                            assertNotNull(book.getData(mr.toBoard().play(bestUnplayedSq)));
                        }
                        break;
                    case ULEAF:
                        assertEquals(4, data.getScore());
                        break;
                }
            }
        }
    }

    private static void checkScore(MinimalReflection mr, Book.Data data) {
        final int expected = Utils.isOdd(mr.nEmpty()) ? 2 : -2;
        if (expected != data.getScore()) {
            System.out.println("bad position:");
            System.out.println(mr);
        }
        assertEquals(expected, data.getScore());
    }

    /**
     * A Book.Adder that calculates all deviations as -4 discs an all solves as 2 discs to the player with parity.
     */
    public static class TestAdder implements Adder {
        @Override public MoveScore calcDeviation(Board board, long moves) {
            final int sq = Long.numberOfTrailingZeros(moves);
            return new MoveScore(sq, -400);
        }

        @Override public MoveScore solve(Board board) {
            final int sq = Long.numberOfTrailingZeros(board.calcMoves());
            return new MoveScore(sq, Utils.isOdd(board.nEmpty()) ? 200 : -200);
        }

        @Override public int solveDepth() {
            return 24;
        }
    }

    public void testEquals() {
        Book book = new Book();
        Book book2 = new Book().add(gameWithPass);
        assertEquals(book, book);
        assertEquals(book, new Book());
        assertEquals(book2, book2);
        assertFalse(book.equals(book2));
        assertFalse(book2.equals(book));
    }

    public void testReadWrite() {
        testIO(new Book());
        final Book sample = new Book().add(SampleGames.vongGames().get(0));
        testIO(sample);
        sample.add(gameWithPass);
        testIO(sample);
        sample.negamax(new TestAdder(), false);
        testIO(sample);
    }


    private void testIO(Book book) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            book.write(baos);

            InputStream in = new ByteArrayInputStream(baos.toByteArray());
            Book book2 = Book.read(in);
            assertEquals(book, book2);
        } catch (IOException e) {
            // can't happen, because we're using ByteArray streams.
            throw new RuntimeException("can't happen", e);
        }
    }

    public void testDataEquals() {
        final Book.Data a = new Book.Data(UBRANCH, 1, -1);
        final Book.Data b = new Book.Data(SOLVED, 1, -1);
        final Book.Data c = new Book.Data(UBRANCH, 2, -1);
        final Book.Data d = new Book.Data(UBRANCH, 1, -2);

        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(c, c);
        assertEquals(d, d);

        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(d));

        // but bestPlayedSq is ignored for SOLVED and ULEAF nodes.
        assertEquals(new Book.Data(SOLVED, 0, 0), new Book.Data(SOLVED, 0, 2));
        assertEquals(new Book.Data(ULEAF, 0, 0), new Book.Data(ULEAF, 0, 2));
    }

    public void testGetSuccessors() {
        final Book book = sampleBook();

        final List<Book.Successor> successors0 = Arrays.asList(s("E6", -2), s("F5", -2), s("C4", -2), s("D3", -2));
        assertEquals(successors0, book.getSuccessors(Board.START_BOARD));

        final List<Book.Successor> successor1 = Arrays.asList(s("D6", 2), s("F4", -4, ULEAF));
        assertEquals(successor1, book.getSuccessors(Board.START_BOARD.play("F5")));
    }

    public static Book sampleBook() {
        final Book book = new Book().add(SampleGames.vongGames().get(0));
        book.negamax(new TestAdder(), false);
        return book;
    }

    private static Book.Successor s(String move, int score) {
        return s(move, score, UBRANCH);
    }

    private static Book.Successor s(String move, int score, Book.NodeType nodeType) {
        return new Book.Successor(BitBoardUtils.textToSq(move), score, nodeType);
    }
}


