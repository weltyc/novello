package com.welty.novello.ntest;

import com.welty.novello.core.Props;
import com.welty.novello.selfplay.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.selfplay.Player;
import com.welty.novello.core.Position;
import com.welty.novello.core.BitBoardUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * An instance of the NTest othello program
 */
public class NTest implements Player {
    private final PrintWriter out;
    private final BufferedReader in;
    private int ping = 0;
    private final int depth;
    private final boolean debug;

    public NTest(int depth, boolean debug) {
        try {
            this.depth = depth;
            this.debug = debug;
            final String exe = Props.getInstance().get("ntest");
            final File ntestDir = new File(exe).getParentFile();
            final Process process = new ProcessBuilder(exe, "x").directory(ntestDir).redirectErrorStream(true).start();
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

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int flags) {
        try {
            pingPong();
            println("set game " + new MutableGame(board, "me", "you", "here").toGgf());
            println("go");
            String line;
            while (null != (line = readLine())) {
                if (line.startsWith("===")) {
                    final String[] parts = line.substring(4).split("/");
                    final int sq = BitBoardUtils.textToSq(parts[0]);
                    final int eval;
                    if (parts.length < 2 || parts[1].isEmpty()) {
                        eval = 0;
                    } else {
                        eval = (int) Math.round(Double.parseDouble(parts[1]) * 100);
                    }
                    return new MoveScore(sq, eval);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Ntest connection failed");
    }

    @Override public String toString() {
        return "NTest:" + depth;
    }
}
