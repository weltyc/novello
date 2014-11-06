package com.welty.novello.book;

import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SearchDepth;
import com.welty.novello.selfplay.SearchDepths;
import com.welty.novello.solver.Counter;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.SearchAbortedException;
import com.welty.novello.solver.Solver;
import com.welty.othello.api.AbortCheck;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.thor.DatabaseData;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 */
public class BookGenerator {
    public static void main(String[] args) {
        final int midgameDepth = 30;
        System.out.println("Search depths: \n" + SearchDepths.maxes(midgameDepth));

        if (args.length > 0) {
            return;
        }

        // add all WOC games to book.
        final DatabaseData dd = new DatabaseData();
        dd.loadFromThorDirectory(new File("C:/dev/othNew/thor"));
//        printStatistics(dd);
        final int n = dd.NGames();


        // create book from WOC games
        final Book book = new Book();
        for (int i=0; i<n; i++) {
            final COsGame game = dd.GameFromIndex(i);
            final int year = dd.getGameYear(i);

            if (game.sPlace.equals("Championnat du Monde") && year==2008 && game.getBlackPlayer().name.startsWith("E")) {
                book.add(MutableGame.of(game));
            }
        }

        System.out.println("Size at 21 empty = " + book.sizeAtEmpty(21));
        book.negamax(new MyAdder(book, midgameDepth), true);

        System.out.println(book);

        final String bookPath = "C:/dev/mongo/book.nbb";
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(bookPath))) {
            book.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printStatistics(DatabaseData dd) {
        // print statistics about the database
        final int n = dd.NGames();
        System.out.println(n);

        final TObjectIntMap<String> tournaments = new TObjectIntHashMap<>();

        for (int i=0; i< n; i++) {
            final COsGame game = dd.GameFromIndex(i);
            tournaments.adjustOrPutValue(game.sPlace, 1, 1);
        }

        final ArrayList<String> sorted= new ArrayList<>(tournaments.keySet());
        Collections.sort(sorted);
        for (String s : sorted) {
            System.out.format("%4d %s\n", tournaments.get(s), s);
        }
    }

    private static class MyAdder implements Adder {
        private final MidgameSearcher mid;
        private final Solver end;
        private final int midgameDepth;
        private final int solveDepth;

        private MyAdder(@Nullable Book book, int midgameDepth) {
            this.midgameDepth = midgameDepth;
            final Eval eval = Players.currentEval();
            final Counter counter = new Counter(eval);
            final MidgameSearcher.Options options = new MidgameSearcher.Options("");
            mid = new MidgameSearcher(counter, options, book);
            end = new Solver(eval, options, book);

            this.solveDepth = SearchDepths.calcSolveDepth(midgameDepth);
        }

        @Override public MoveScore calcDeviation(Board board, long moves) {
            System.out.print("d");
            final SearchDepth depth = SearchDepths.lastSearchDepth(board.nEmpty(), midgameDepth);
            try {
                return mid.getMoveScore(board, moves, depth.getDepth(), depth.getWidth(), AbortCheck.NEVER);
            } catch (SearchAbortedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override public MoveScore solve(Board board) {
            System.out.print("s");
            return end.getMoveScore(board.mover(), board.enemy());
        }

        @Override public int solveDepth() {
            return solveDepth;
        }
    }
}
