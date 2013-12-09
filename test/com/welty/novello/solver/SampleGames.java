package com.welty.novello.solver;

import com.orbanova.common.feed.Feed;
import com.orbanova.common.feed.Feeds;
import com.welty.novello.core.MutableGame;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SampleGames {
    public static List<MutableGame> games() {
        final ArrayList<MutableGame> games = new ArrayList<>();

        final Feed<String> gameStrings = Feeds.ofLines(SampleGames.class, "TestGames.ggf");
        for (String line : gameStrings) {
            final int start = line.indexOf("(");
            final String gameString = line.substring(start);
            games.add(MutableGame.ofGgf(gameString));
        }

        return games;
    }
}
