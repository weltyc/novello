package com.welty.ggf;

import com.orbanova.common.feed.Feed;
import com.orbanova.common.feed.Feeds;
import com.orbanova.common.feed.NullableMapper;
import com.orbanova.common.misc.Require;
import com.welty.othello.gdk.COsGame;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Match (1 or 2 Games from the same start position)
 */
public class GgfMatch {
    public static final NullableMapper<String,GgfMatch> OF_STRING = new NullableMapper<String, GgfMatch>() {
        @Override public GgfMatch y(String x) {
            try {
                return GgfMatch.of(x);
            } catch (RuntimeException e) {
                System.out.println("err: " + x);
                return null;
            }
        }
    };
    private final List<COsGame> games;

    private GgfMatch(List<COsGame> games) {
        this.games = Collections.unmodifiableList(new ArrayList<>(games));
    }

    /**
     * Get the games from the match
     *
     * @return the games
     */
    public List<COsGame> getGames() {
        return games;
    }

    /**
     * Construct a Match from a ggf text representation
     *
     * @param ggf ggf text
     * @return Match
     */
    public static GgfMatch of(String ggf) {
        final String[] parts = ggf.split("\\s+", 2);
        final int nGames = Integer.parseInt(parts[0]);
        final String ggfTexts = parts[1];

        final List<COsGame> games = new ArrayList<>(2);
        for (int loc = 0; ; ) {
            final int end = ggfTexts.indexOf(')', loc);
            if (end < 0) {
                break;
            }
            final String ggfText = ggfTexts.substring(loc, end + 1);
            games.add(new COsGame(ggfText));
            loc = end + 1;
        }
        Require.eq(nGames, "# games", games.size());
        return new GgfMatch(games);
    }

    @Override public String toString() {
        return games.size() + " " + Feeds.of(games).join("");
    }

    public static Feed<GgfMatch> readFromFile(Path path) throws IOException, CompressorException {
        final BufferedReader in = GgfMatch.getBufferedReaderForBZ2File(path);
        return Feeds.ofLines(in).map(OF_STRING);
    }

    public static BufferedReader getBufferedReaderForBZ2File(Path fileIn) throws IOException, CompressorException {
        InputStream fin = Files.newInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));

        return br2;
    }
}
