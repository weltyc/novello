package com.welty.novello.book;

import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SearchDepths;
import com.welty.novello.solver.Counter;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.thor.DatabaseData;
import com.welty.othello.thor.PrintingProgressTracker;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 */
public class BookGenerator {

    public static void main(String[] args) throws IOException {
        final int midgameDepth = 25;
        System.out.println("Search depths: \n" + SearchDepths.maxes(midgameDepth));

        if (args.length > 0) {
            return;
        }

        // add all WOC games to book.
        final DatabaseData dd = new DatabaseData();
        dd.loadFromDirectory(new File("C:/dev/othNew/thor"), new PrintingProgressTracker("games"));
//        printStatistics(dd);
        final int n = dd.NGames();

        int nBotGames = 0;
        int nEdaxGames = 0;

        // create book from WOC games
        final Book book = Book.load();

        for (int i=0; i<n; i++) {
            final COsGame game = dd.GameFromIndex(i);
            final int year = dd.getGameYear(i);

            if (game.sPlace.equals("Championnat du Monde")) {
                book.add(MutableGame.of(game));
            }
            if (isStrongBot(game.getBlackPlayer().name) && isStrongBot(game.getWhitePlayer().name)) {
                book.add(MutableGame.of(game));
                nBotGames++;
            }

            if (year == 2009 && game.getBlackPlayer().name.equals("???") && game.getWhitePlayer().name.equals("???") && game.sPlace.equals("Diverses parties")) {
                nEdaxGames++;
                // about 200k edax games. Taking every 25th one results in about 8k games to add to book.
                if (nEdaxGames % 25 == 0) {
                    book.add(MutableGame.of(game));
                }
            }
        }

        System.out.println("Size at 21 empty = " + book.sizeAtEmpty(21));
        System.out.println("Size at 26 empty = " + book.sizeAtEmpty(26));
        System.out.println("# bot games = " + nBotGames);

        book.negamax(new MultithreadedAdder(book, midgameDepth), true, Book.getBookLocation());

    }

    private static final String[] strongBots = "ntest,edax,saio,zebra,yohoho,cruel+".split(",");

    private static boolean isStrongBot(String name) {
        final String lower = name.toLowerCase();
        for (String bot : strongBots) {
            if (lower.startsWith(bot)) {
                return true;
            }
        }
        return false;
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

    private static class Searcher {
        private final MidgameSearcher mid;
        private final Solver end;

        Searcher(Book book) {
            final Eval eval = Players.currentEval();
            final Counter counter = new Counter(eval);
            final MidgameSearcher.Options options = new MidgameSearcher.Options("");
            mid = new MidgameSearcher(counter, options, book);
            end = new Solver(eval, options, book);
        }
    }

    private static class MultithreadedAdder implements Adder {
        @Nullable private final Book book;
        private final int midgameDepth;
        private final int solveDepth;
        private final ThreadLocal<Searcher> searchers = new ThreadLocal<Searcher>() {
            @Override protected Searcher initialValue() {
                return new Searcher(book);
            }
        };

        private MultithreadedAdder(@Nullable Book book, int midgameDepth) {
            this.book = book;
            this.midgameDepth = midgameDepth;
            this.solveDepth = SearchDepths.calcSolveDepth(midgameDepth);
        }

        @Override public MoveScore calcDeviation(Board board, long moves) {
//            System.out.print("d");
            return searchers.get().mid.calcMoveIterative(board, moves, midgameDepth);
        }

        @Override public MoveScore solve(Board board) {
//            System.out.print("s");
            return searchers.get().end.getMoveScore(board.mover(), board.enemy());
        }

        @Override public int solveDepth() {
            return solveDepth;
        }
    }
}
