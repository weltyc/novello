package com.welty.novello.ntest;

import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.welty.novello.core.*;
import com.welty.novello.selfplay.SyncEngine;
import com.welty.othello.gui.ExternalEngineManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.prefs.BackingStoreException;

/**
 * An instance of the NTest othello program
 */
public class NBoardSyncEngine implements SyncEngine {
    private final PrintWriter out;
    private final BufferedReader in;
    private final String program;
    private int ping = 0;
    private final int depth;
    private final boolean debug;

    private static final Logger log = Logger.logger(NBoardSyncEngine.class, Logger.Level.DEBUG);


    public NBoardSyncEngine(String program, int depth, boolean debug) {
        this(ExternalEngineManager.getXei(program), depth, debug);
    }

    public NBoardSyncEngine(String program, int depth, boolean debug, String workingDirectory, String command) {
        this(new ExternalEngineManager.Xei(program, workingDirectory, command), depth, debug);
    }

    private NBoardSyncEngine(ExternalEngineManager.Xei xei, int depth, boolean debug) {
        this.program = xei.name;
        try {
            this.depth = depth;
            this.debug = debug;
            log.debug("wd  : " + xei.wd);
            log.debug("cmd : " + xei.cmd);
            final String[] processArgs = xei.cmd.split("\\s+");
            final Process process = new ProcessBuilder(processArgs).directory(new File(xei.wd)).redirectErrorStream(true).start();
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            println("set depth " + depth);
            System.out.println(in.readLine());
            pingPong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean wasWriting = true;

    private void changeState(boolean writing) {
        if (debug && wasWriting != writing) {
            System.out.println();
            wasWriting = writing;
        }
    }

    private void println(String text) {
        out.println(text);
        changeState(true);
        if (debug) {
            System.out.println("> " + text);
        }
    }

    private String readLine() throws IOException {
        final String line = in.readLine();
        if (line != null) {
            changeState(false);
            if (debug) {
                System.out.println("< " + line);
            }
        }
        return line;
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

    @Override public MoveScore calcMove(@NotNull Position board) {
        try {
            pingPong();
            println("set game " + new MutableGame(board, "me", "you", "here").toGgf());
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

    @Override public void setMaxDepth(int maxDepth) {
        Require.geq(maxDepth, "max depth", 0);
        println("set depth " + maxDepth);
        try {
            pingPong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        return program + ":" + depth;
    }

    public static void main(String[] args) throws IOException {
        String[] processArgs = "./mEdax -nboard".split("\\s+");
        final File wd = new File("/Applications/edax/4.3.2/bin");
        final Process process = new ProcessBuilder(processArgs).directory(wd).redirectErrorStream(true).start();
    }
}
