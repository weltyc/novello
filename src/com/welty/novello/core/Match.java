package com.welty.novello.core;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Match (1 or 2 Games from the same start position
 */
public class Match {
    private final List<MutableGame> games;

    private Match(List<MutableGame> mutableGames) {
        this.games = Collections.unmodifiableList(new ArrayList<>(mutableGames));
    }

    /**
     * Get the games from the match
     * @return the games
     */
    public List<MutableGame> getGames() {
        return games;
    }

    /**
     * Construct a Match from a ggf text representation
     *
     * @param ggf ggf text
     * @return Match
     */
    public static Match of(String ggf) {
        final String[] parts = ggf.split("\\s+", 2);
        final int nGames = Integer.parseInt(parts[0]);
        final String ggfTexts = parts[1];

        final List<MutableGame> mutableGames = new ArrayList<>(2);
        for (int loc = 0;;) {
            final int end = ggfTexts.indexOf(')', loc);
            if (end<0) {
                break;
            }
            final String ggfText = ggfTexts.substring(loc, end+1);
            mutableGames.add(MutableGame.ofGgf(ggfText));
            loc = end+1;
        }
        Require.eq(nGames, "# games", mutableGames.size());
        return new Match(mutableGames);
    }

    @Override public String toString() {
        return games.size() + " " + Feeds.of(games).join(" ");
    }
}
