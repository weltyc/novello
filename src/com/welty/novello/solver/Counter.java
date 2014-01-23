package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Counts;
import com.welty.novello.core.Square;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.eval.Mpc;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Like an Eval, but keeps track of node counts.
 * <p/>
 * Unlike an Eval, this is not thread-safe. So create one for each thread.
 * <p/>
 * This does not implement the Eval interface for two reasons:
 * 1. Evals are assumed to be thread-safe, and this isn't.
 * 2. To avoid vtbl lookups for speed.
 */
public class Counter {
    private final @NotNull Eval eval;
    private long nEvals;
    private long nFlips;
    final @NotNull Mpc mpcs;

    /**
     * Compile with capture = true to enable position capturing
     * <p/>
     * This will overwrite the existing file (so, specifically, set capture = false after capturing
     * or the next run will delete the capture file).
     * <p/>
     * To get an equal number of odd and even positions in the endgame, run a SelfPlaySet
     * using players with options NS (no extended depth searches in the endgame, no solver).
     */
    private static final boolean capture = false;
    private static final int CAPTURE_FREQUENCY = 200;
    private static final Random captureRand = new Random();
    public static final Path capturePath = Paths.get("captured2.me");
    private static final DataOutputStream captureOut;
    private static int nextCapture = nextCaptureDelta();
    private static final int[] counts = new int[64];
    private static final int MAX_COUNT = 200_000; //max # of positions to capture at each empty

    private static int nextCaptureDelta() {
        return captureRand.nextInt(CAPTURE_FREQUENCY) + 1;
    }

    static {
        // open capture file and register shutdown hook to close it.

        if (capture) {
            try {
                final OutputStream out = Files.newOutputStream(capturePath);
                captureOut = new DataOutputStream(out);

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override public void run() {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // I guess we'll notice when we try to use the file and it is corrupt.
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            captureOut = null;
        }
    }

    public Counter(@NotNull Eval eval) {
        this.eval = eval;
        if (eval instanceof CoefficientEval) {
            final Mpc mpc = ((CoefficientEval) eval).mpc;
            if (mpc == null) {
                mpcs = Mpc.DEFAULT;
            } else {
                mpcs = mpc;
            }
        } else {
            mpcs = Mpc.DEFAULT;
        }
    }

    public long nFlips() {
        return nFlips;
    }

    public long calcFlips(Square square, long mover, long enemy) {
        nFlips++;
        return square.calcFlips(mover, enemy);
    }

    public int eval(long mover, long enemy) {
        nEvals++;
        if (capture && nEvals == nextCapture) {
            final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
            if (counts[nEmpty] < MAX_COUNT) {
                counts[nEmpty]++;
                try {
                    captureOut.writeLong(mover);
                    captureOut.writeLong(enemy);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            nextCapture += nextCaptureDelta();
        }
        return eval.eval(mover, enemy);
    }

    public long nEvals() {
        return nEvals;
    }

    public @NotNull Counts getNodeStats() {
        return new Counts(nFlips, nEvals);
    }
}
