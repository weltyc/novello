package com.welty.novello.ntest;

import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.SyncEngine;
import com.welty.othello.core.ProcessLogger;
import com.welty.othello.gui.ExternalEngineManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * An instance of the NTest othello program
 */
public class NBoardSyncEngine implements SyncEngine {
    private final String program;
    private int ping = 0;
    private final ProcessLogger processLogger;

    private static final Logger log = Logger.logger(NBoardSyncEngine.class, Logger.Level.DEBUG);


    public NBoardSyncEngine(String program, boolean debug) {
        this(ExternalEngineManager.getXei(program), debug);
    }

    private NBoardSyncEngine(ExternalEngineManager.Xei xei, boolean debug) {
        this.program = xei.name;
        try {
            log.debug("wd  : " + xei.wd);
            log.debug("cmd : " + xei.cmd);
            final String[] processArgs = xei.cmd.split("\\s+");
            final Process process = new ProcessBuilder(processArgs).directory(new File(xei.wd)).redirectErrorStream(true).start();
            processLogger = new ProcessLogger(process, debug);
            processLogger.readLine();
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

    @Override public MoveScore calcMove(@NotNull Position board, int maxDepth) {
        try {
            pingPong();
            println("set game " + new MutableGame(board, "me", "you", "here").toGgf());
            Require.geq(maxDepth, "max depth", 0);
            println("set depth " + maxDepth);
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
        // The NBoard protocol does not have this command
        // todo add it
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
