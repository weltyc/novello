package com.welty.novello.ntest;

import com.orbanova.common.misc.Require;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.selfplay.SyncEngine;
import com.welty.othello.core.ProcessLogger;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gui.ExternalEngineManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * An instance of the NTest othello program.
 * <p/>
 * This class is not thread-safe.
 */
public class NBoardSyncEngine implements SyncEngine {
    private final String program;
    private int ping = 0;
    private final ProcessLogger processLogger;
    private int lastDepth = -1;

    public NBoardSyncEngine(@NotNull ExternalEngineManager.Xei xei, boolean debug) {
        this.program = xei.name;
        try {
            processLogger = xei.createProcess(debug);
            pingPong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void println(String s) {
        processLogger.println(s);
    }

    private String readLine() throws IOException {
        return processLogger.readLine();
    }

    /**
     * Send a ping and wait for the matching pong.
     * <p/>
     * In practical terms this discards all input coming from the exe until the matching pong.
     * It synchronizes this and the exe.
     *
     * @throws IOException
     */
    private void pingPong() throws IOException {
        ping++;
        println("ping " + ping);
        String line;
        while (null != (line = readLine())) {
            if (line.equals("pong " + ping)) {
                return;
            }
        }
    }

    @NotNull @Override public MoveScore calcMove(@NotNull MutableGame game, int maxDepth) {
        Require.geq(maxDepth, "max depth", 0);
        try {
            pingPong();
            println("set game " + game.toGgf());
            if (maxDepth != lastDepth) {
                println("set depth " + maxDepth);
                lastDepth = maxDepth;
            }
            println("go");
            String line;
            while (null != (line = readLine())) {
                if (line.startsWith("===")) {
                    return parseMoveScore(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("NBoard connection to " + program + " failed");
    }

    @NotNull @Override public MoveScore calcMove(@NotNull Board board, @Nullable OsClock clock, int maxDepth) {
        Require.geq(maxDepth, "max depth", 0);
        try {
            pingPong();
            if (clock == null) {
                // player can take as long as it wants. 1 billion millis (~2 weeks) should be long enough.
                clock = OsClock.LONG;

            }
            println("set game " + new MutableGame(board, "me", "you", "here", clock, clock).toGgf());
            if (maxDepth != lastDepth) {
                println("set depth " + maxDepth);
                lastDepth = maxDepth;
            }
            println("go");
            String line;
            while (null != (line = readLine())) {
                if (line.startsWith("===")) {
                    return parseMoveScore(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("NBoard connection to " + program + " failed");
    }

    @Override public void clear() {
        println("clear");
    }

    static MoveScore parseMoveScore(String line) {
        final String[] parts = line.substring(4).split("\\s+|/");
        final int sq = BitBoardUtils.textToSq(parts[0]);
        final int eval;
        if (parts.length < 2 || parts[1].isEmpty()) {
            eval = 0;
        } else {
            eval = (int) Math.round(Double.parseDouble(parts[1]) * 100);
        }
        return new MoveScore(sq, eval);
    }

    @Override public String toString() {
        return program;
    }
}
