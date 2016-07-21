package com.welty.novello.book;

import com.welty.novello.core.MutableGame;
import com.welty.novello.selfplay.SearchDepths;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.thor.DatabaseData;
import com.welty.othello.thor.PrintingProgressTracker;

import java.io.File;
import java.io.IOException;

/**
 * Generates a book from existing games
 */
public class BookGenerator {

    /**
     * Generate a book from existing games using default values
     *
     * Interesting games are selected from the thor database and added to book.
     *
     * @param args must be empty
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            return;
        }

        File bookLocation = Book.getBookLocation();
        System.out.println("Creating book at " + bookLocation);

        final int midgameDepth = 12;
        System.out.println("Search depths: \n" + SearchDepths.maxes(midgameDepth));

        final String thorDbLocation = "/home/chris/dev/mongo/npack/nboard/db/ffo";

        // add all WOC games to book.
        final DatabaseData dd = new DatabaseData();
        dd.loadFromDirectory(new File(thorDbLocation), new PrintingProgressTracker("games"));
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

        book.negamax(new MultithreadedAdder(book, midgameDepth), true, bookLocation);

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
}

