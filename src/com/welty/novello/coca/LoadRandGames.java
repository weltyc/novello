package com.welty.novello.coca;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.feed.Handlers;
import com.orbanova.common.feed.NullableMapper;
import com.welty.novello.core.Match;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;

public class LoadRandGames {
    /**
     * Select s8rXX, non-anti, games from the file  ~/dev/novello/Othello/latest.223270.bz2
     */
    public static void main(String[] args) throws IOException, CompressorException {
        final String filename = "Othello.latest.223270.bz2";

        try (final BufferedReader in = getBufferedReaderForBZ2File(filename)) {
            Feeds.ofLines(in)
                    .map(new NullableMapper<String, Match>() {
                        @Override public Match y(String x) {
                            try {
                                return Match.of(x);
                            } catch (RuntimeException e) {
                                return null;
                            }
                        }
                    })
                    .each(Handlers.OUT);
        }
    }


    public static BufferedReader getBufferedReaderForBZ2File(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));

        return br2;
    }
}
