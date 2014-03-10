package com.welty.novello.solver;

import com.orbanova.common.feed.Feed;
import com.orbanova.common.feed.Feeds;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Board;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class SampleGames {
    /**
     * @return 12 games (Saio vs Saio) that form Ntest's standard speed test
     */

    public static List<MutableGame> saioGames() {
        final ArrayList<MutableGame> games = new ArrayList<>();

        final Feed<String> gameStrings = Feeds.ofLines(SampleGames.class, "TestGames.ggf");
        for (String line : gameStrings) {
            final int start = line.indexOf("(");
            final String gameString = line.substring(start);
            games.add(MutableGame.ofGgf(gameString));
        }

        return games;
    }

    /**
     * @return 100 games (Zebra vs a very old ntest) that form Novello's standard speed test.
     */
    public static List<MutableGame> vongGames() {
        final ArrayList<MutableGame> games = new ArrayList<>();

        final Feed<String> gameStrings = Feeds.ofLines(SampleGames.class, "vong.txt");
        for (String line : gameStrings) {
            games.add(MutableGame.ofVong(line));
        }

        return games;
    }

    /**
     * Selects positions from the vongGames.
     *
     * Positions are selected if they have the correct number of empties and the player to move
     * has a legal move.
     *
     * If a position occurs in more than one game, only one copy is returned. Duplicates are discarded.
     *
     * The 'value' part of the position is the net game score, in disks, to the mover.
     *
     * @param nEmpty number of empties for test positions
     * @return distinct positions from vongGames() at that number of empties.
     */
    public static List<MeValue> vongPositions(int nEmpty) {
        // it would be nice to keep these in the original order to track back to the original game.
        List<MeValue> pvs = new ArrayList<>();
        final Set<Board> seen = new HashSet<>();

        for (MutableGame game : vongGames()) {
            final Board board = game.calcBoardAt(nEmpty);
            if (board !=null && !seen.contains(board)) {
                seen.add(board);
                final int value = board.blackToMove? game.netScore() : -game.netScore();
                final MeValue pv = new MeValue(board.mover(), board.enemy(), value);
                pvs.add(pv);
            }
        }
        return pvs;
    }
}
