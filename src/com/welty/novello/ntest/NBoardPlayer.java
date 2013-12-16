package com.welty.novello.ntest;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.Props;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.selfplay.Player;
import com.welty.novello.core.Position;
import com.welty.novello.core.BitBoardUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * An instance of the NTest othello program
 */
public class NBoardPlayer implements Player {
    private final PrintWriter out;
    private final BufferedReader in;
    private final String program;
    private int ping = 0;
    private final int depth;
    private final boolean debug;

    private static final Logger log = Logger.logger(NBoardPlayer.class, Logger.Level.DEBUG);

    public NBoardPlayer(String program, int depth, boolean debug) {
        this.program = program;
        try {
            this.depth = depth;
            this.debug = debug;
            final String exe = Props.getInstance().get(program);
            if (exe==null) {
                throw new RuntimeException("Program '" + program + "' is not listed in properties file " + Props.getInstance().getSourceFile());
            }
            final String args = Props.getInstance().get(program+".args");
            if (args==null) {
                throw new RuntimeException("Program '" + program + ".args' is not listed in properties file " + Props.getInstance().getSourceFile());
            }
            final File ntestDir = new File(exe).getParentFile();
            log.debug("exe : " + exe);
            log.debug("wd  : " + ntestDir);
            log.debug("args: '" + args +"'");
            final String[] processArgs = makeArgs(exe, args);
            final Process process = new ProcessBuilder(processArgs).directory(ntestDir).redirectErrorStream(true).start();
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            pingPong();
            println("set depth " + depth);
            println("new");
            println("go");
            pingPong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] makeArgs(String exe, String args) {
        final String[] split = args.split("\\s+");
        final String[] out = new String[1 + split.length];
        out[0] = exe;
        System.arraycopy(split, 0, out, 1, split.length);
        return out;
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

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
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
}
