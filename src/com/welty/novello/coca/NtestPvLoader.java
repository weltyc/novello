package com.welty.novello.coca;

import com.orbanova.common.misc.Require;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.Mr;
import com.welty.novello.core.Position;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads an ntest-style .pvs file
 */
public class NtestPvLoader {

    public static final int FIRST_CORRUPT = 4193246;
    public static final int LAST_CORRUPT = 4193286;

    public static final MvSource mvSource = new MvSource() {
        @Override public List<MeValue> getMvs() throws IOException {
            return loadMeValues();
        }

        @Override public String toString() {
            return "ntest pvs";
        }
    } ;

    public static final MrSource mrSource = new MrSource() {
        @Override public Set<Mr> getMrs() throws IOException {
            final HashSet<Mr> mrs = new HashSet<>();
            for (Pv pv : loadPvs()) {
                mrs.add(new Mr(pv.mover, pv.enemy));
            }
            return mrs;
        }
    };

    /**
     * Loads an ntest-style .pvs file
     *
     * @return PVs, as a list
     */
    private static List<MeValue> loadMeValues() throws IOException {
        final List<Pv> pvs = loadPvs();

        final List<MeValue> meValues = new ArrayList<>();
        for (Pv pv : pvs) {
            pv.addTo(meValues);
        }
        return meValues;
    }

    private static List<Pv> loadPvs() throws IOException {
        final List<Pv> pvs = new ArrayList<>();

        final String filename = "captured.pv";
        final Path path = Paths.get(filename);
        final long length = path.toFile().length();
        System.out.format("%,d positions available\n\n", length / 23);

        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            //noinspection InfiniteLoopStatement
            for (int i = 0; true; i++) {
                if (i >= FIRST_CORRUPT && i <= LAST_CORRUPT) {
                    // these positions are known to be corrupt
                    in.skipBytes(23);
                } else {
                    final Pv pv = new Pv(i, in);
                    if (pv.pass != 2) {
                        pvs.add(pv);
                    }
                }
            }
        } catch (EOFException e) {
            // expected
        }
        return pvs;
    }

    private static class Pv {
        long mover;
        long enemy;
        final int pass;
        final int sq;
        final short value;
        private final int i;

        public Pv(int i, DataInputStream in) throws IOException {
            this.i = i;
            mover = in.readLong();
            final long empty = in.readLong();
            enemy = ~(mover | empty);
            value = Short.reverseBytes(in.readShort());
            pass = Integer.reverseBytes(in.readInt());
            sq = in.readUnsignedByte() ^ 070;

            // verify my understanding of the 'pass' flag
            final int expectedPass = expectPass(mover, enemy);
            if (pass != expectedPass) {
                failInternal();
            }

            // if pass flag, need to pass
            if (pass == 1) {
                long temp = mover;
                mover = enemy;
                enemy = temp;
            }

            // verify my understanding of mover and enemy
            if ((empty & 0x1818000000L) != 0) {
                failInternal();
            }

            // verify my understanding of the value.
            if (pass == 2) {
                Require.eq(value, "value", BitBoardUtils.terminalScore(mover, enemy) * 100);
            }

            // verify my understanding of the 'sq' value
            final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
            if (BitBoardUtils.isBitClear(moverMoves, sq)) {
                failInternal();
            }
        }

        private void failInternal() {
            System.out.println(this);
            throw new IllegalStateException();
        }

        /**
         * Calculate the expected value of 'pass'
         */
        private static int expectPass(long mover, long enemy) {
            if (BitBoardUtils.calcMoves(mover, enemy) != 0) {
                return 0;
            }
            if (BitBoardUtils.calcMoves(enemy, mover) != 0) {
                return 1;
            }
            return 2;
        }

        public void addTo(List<MeValue> pvs) {
            if (pass != 2) {
                pvs.add(new MeValue(mover, enemy, value));
                final Position next = new Position(mover, enemy, true).play(sq);
                switch (next.calcPass()) {
                    case 0:
                        pvs.add(new MeValue(next.mover(), next.enemy(), -value));
                        break;
                    case 1:
                        pvs.add(new MeValue(next.enemy(), next.mover(), value));
                        break;
                    case 2:
                        // do nothing, won't be valued.
                }
            }
        }

        @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();

            sb.append(String.format(" Position %,d\n", i));
            sb.append("pass = " + pass + ", sq=" + BitBoardUtils.sqToText(sq) + "\n");
            sb.append("value = " + value + "\n");
            sb.append(new Position(mover, enemy, true));

            return sb.toString();
        }
    }
}
