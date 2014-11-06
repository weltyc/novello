package com.welty.novello.book;

import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Utils;
import com.welty.novello.core.*;
import com.welty.othello.gdk.COsGame;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

/**
 * An Othello opening book
 */
@EqualsAndHashCode
public class Book {
    private static final Logger log = Logger.logger(Book.class);

    /**
     * Book contents
     */
    private final Map<MinimalReflection, Data> entries = new HashMap<>();

    public Book() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int nEmpty = 60; nEmpty >= 0; nEmpty--) {
            final List<MinimalReflection> mrs = getMrs(nEmpty);
            if (!mrs.isEmpty()) {
                //noinspection StringConcatenationInsideStringBufferAppend
                sb.append("\n=== " + nEmpty + " empty ===\n");

                final boolean blackToMove = !Utils.isOdd(nEmpty);
                for (MinimalReflection mr : mrs) {
                    sb.append(mr.toBoard(blackToMove)).append('\n');
                    sb.append(getData(mr)).append("\n\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Add the game to book without calculating deviations.
     * <p/>
     * call {@link #negamax(Adder, boolean)} to calculate deviations.
     *
     * @param game game to add
     * @return this Book, for chaining.
     */
    public Book add(COsGame game) {
        return add(MutableGame.of(game));
    }

    /**
     * Add the game to book without calculating deviations.
     * <p/>
     * call {@link #negamax(Adder, boolean)} to calculate deviations.
     *
     * @param game game to add
     * @return this Book, for chaining.
     */
    public Book add(MutableGame game) {
        Board pos = game.getStartBoard();
        for (Move8x8 move : game.getMlis()) {
            if (pos.nEmpty() < minDepth()) {
                break;
            }
            addUnevaluatedPos(pos);

            if (move.isPass()) {
                pos = pos.pass();
            } else {
                pos = pos.play(move.getSq());
            }
        }

        addUnevaluatedPos(pos);
        return this;
    }

    /**
     * Add the position as a ULEAF node with value=0 and bestUnplayedSq = -1, unless it has no legal moves or is too shallow.
     *
     * @param pos position to add.
     */
    void addUnevaluatedPos(Board pos) {
        final Data data = getData(pos);
        if (data == null || data.getNodeType() == NodeType.ULEAF) {
            if (pos.nEmpty() >= minDepth() && pos.hasLegalMove()) {
                MinimalReflection mr = pos.minimalReflection();
                entries.put(mr, new Data());
            }
        }
    }

    /**
     * Add the position to book with bestUnplayedSq = -1.
     * <p/>
     * If the position requires a pass, this method passes and stores -score.
     *
     * @param board    board. May require a pass, but may not be a terminal position.
     * @param score    score, in disks
     * @param nodeType type of node
     */
    void putPos(Board board, int score, NodeType nodeType) {
        putPos(board, score, nodeType, -1);
    }

    /**
     * Add the position to book.
     * <p/>
     * If the position requires a pass, this method passes and stores -score.
     *
     * @param board    board. May require a pass, but may not be a terminal position.
     * @param score    score, in disks
     * @param nodeType type of node
     */
    void putPos(Board board, int score, NodeType nodeType, int bestUnplayedSq) {
        if (!board.hasLegalMove()) {
            board = board.pass();
            score = -score;
            if (!board.hasLegalMove()) {
                throw new IllegalArgumentException("can't put terminal position into book");
            }
        }
        entries.put(board.minimalReflection(), new Data(nodeType, score, bestUnplayedSq));
    }

    /**
     * Get minimum depth stored in this book.
     * <p/>
     * Positions with fewer empties are not stored in this Book.
     *
     * @return the minimum depth.
     */
    public int minDepth() {
        return 20;
    }

    /**
     * Get the number of positions with a given number of empty disks
     *
     * @param nEmpty # of empty disks
     * @return # of positions at that # of empties
     */
    public int sizeAtEmpty(int nEmpty) {
        int size = 0;
        for (MinimalReflection board : entries.keySet()) {
            if (board.nEmpty() == nEmpty) {
                size++;
            }
        }
        return size;
    }

    /**
     * Get a list of all minimal reflections of boards available at a given number of empties.
     *
     * @param nEmpty number of empty disks
     * @return list of boards.
     */
    public List<MinimalReflection> getMrs(int nEmpty) {
        List<MinimalReflection> result = new ArrayList<>();
        for (MinimalReflection board : entries.keySet()) {
            if (board.nEmpty() == nEmpty) {
                result.add(board);
            }
        }
        return result;
    }

    /**
     * Get the data corresponding to a Mr.
     * <p/>
     * If the board has no legal moves, this method automatically passes. If neither player has a legal move,
     * this method returns the terminal value as a SOLVED node.
     *
     * @param mr the board's minimal reflection.
     * @return the data, or null if the position is not in book.
     */
    public Data getData(MinimalReflection mr) {
        return getData(mr.toBoard());
    }

    /**
     * Get the data corresponding to a board.
     * <p/>
     * If the board has no legal moves, this method automatically passes. If neither player has a legal move,
     * this method returns the terminal value as a SOLVED node.
     *
     * @param board the board
     * @return the data, or null if the position is not in book.
     */
    public Data getData(Board board) {
        if (board.hasLegalMove()) {
            return entries.get(board.minimalReflection());
        } else {
            Board passed = board.pass();
            if (passed.hasLegalMove()) {
                final Data passData = entries.get(passed.minimalReflection());
                if (passData == null) {
                    return null;
                } else {
                    return new Data(passData.nodeType, -passData.score, passData.bestUnplayedSq);
                }
            } else {
                return new Data(NodeType.SOLVED, board.terminalScoreToMover(), 0);
            }
        }
    }

    /**
     * Negamax the book, as described in book.md
     *
     * @param adder      midgame and endgame searcher
     * @param printLog   if true, print log messages to console
     */
    public void negamax(Adder adder, boolean printLog) {
        final int solveDepth = adder.solveDepth();
        for (int nEmpty = minDepth(); nEmpty <= 60; nEmpty++) {
            final List<MinimalReflection> mrs = getMrs(nEmpty);
            for (MinimalReflection mr : mrs) {
                if (nEmpty <= solveDepth) {
                    valueUsingSolve(adder, mr.toBoard());
                } else {
                    valueUsingMidgame(adder, mr.toBoard());
                }
            }
            if (printLog) {
                System.out.println();
                log.info(nEmpty + " empties complete");
            }
        }
    }

    private void valueUsingMidgame(Adder adder, Board board) {
        final Data data = getData(board);
        if (data.getNodeType() != NodeType.UBRANCH) {
            return;
        }

        final int bestUnplayedSq;
        if (ubranchNeedsMidgameSearch(board, data)) {
            bestUnplayedSq = searchForBestUnplayedMove(adder, board);
        } else {
            bestUnplayedSq = data.bestUnplayedSq;
        }

        final int score = bestSubScore(board);
        putPos(board, score, NodeType.UBRANCH, bestUnplayedSq);
    }

    private int bestSubScore(Board board) {
        long moves = board.calcMoves();
        if (moves == 0) {
            throw new IllegalArgumentException("board must have moves");
        }
        int bestScore = -65;

        while (moves != 0) {
            int sq = Long.numberOfTrailingZeros(moves);
            long mask = 1L << sq;
            moves ^= mask;
            Data subData = getData(board.play(sq));
            if (subData != null) {
                int subScore = -subData.getScore();
                if (subScore > bestScore) {
                    bestScore = subScore;
                }
            }
        }

        return bestScore;
    }


    /**
     * Do a midgame search for the best non-book move and add it to this Book.
     *
     * @return square of the best unplayed move (either an existing ULEAF node or the one added as a result of the
     * search), or -2 if no unplayed move exists.
     */
    int searchForBestUnplayedMove(Adder adder, Board board) {
        long moves = board.calcMoves();
        long nonBook = 0;
        int bestUnplayedMove = -2;
        int bestUnplayedScore = -65;

        while (moves != 0) {
            int sq = Long.numberOfTrailingZeros(moves);
            long mask = 1L << sq;
            moves ^= mask;
            Data subData = getData(board.play(sq));
            if (subData == null) {
                nonBook |= mask;
            } else if (subData.getNodeType() == NodeType.ULEAF) {
                int subScore = -subData.getScore();
                if (subScore > bestUnplayedScore) {
                    bestUnplayedScore = subScore;
                    bestUnplayedMove = sq;
                }
            }
        }

        if (nonBook != 0) {
            final MoveScore moveScore = adder.calcDeviation(board, nonBook);
            final int subScore = centidisksToScore(moveScore);
            if (subScore > bestUnplayedScore) {
                bestUnplayedMove = moveScore.sq;
            }
            putPos(board.play(moveScore.sq), -subScore, NodeType.ULEAF);
        }

        return bestUnplayedMove;
    }

    private boolean ubranchNeedsMidgameSearch(Board board, Data data) {
        final int sq = data.getBestUnplayedSq();
        if (sq == -2) {
            // all moves are in book.
            return false;
        }

        if (sq >= 0) {
            Data subData = getData(board.play(sq));
            if (subData.getNodeType() == NodeType.ULEAF) {
                return false;
            }
        }

        return true;
    }

    private void valueUsingSolve(Adder adder, Board board) {
        final Data data = getData(board);
        if (data.getNodeType() == NodeType.UBRANCH) {
            final MoveScore moveScore = adder.solve(board);
            final int score = centidisksToScore(moveScore);
            entries.put(board.minimalReflection(), new Data(NodeType.SOLVED, score, moveScore.sq));
            if (board.nEmpty() > minDepth()) {
                Board sub = board.play(moveScore.sq);
                if (sub.hasLegalMove()) {
                    entries.put(sub.minimalReflection(), new Data(NodeType.SOLVED, -score, 0));
                }
            }
        }
    }

    private static int centidisksToScore(MoveScore moveScore) {
        return Math.round(0.01f * moveScore.centidisks);
    }

    /**
     * Write this Book to a stream and close the stream.
     *
     * @param outStream stream to write to
     * @throws IOException if the operation can't be completed.
     */
    public void write(OutputStream outStream) throws IOException {
        DataOutputStream out = new DataOutputStream(outStream);

        // version number
        out.writeInt(1);

        // write trees from most empty to least.
        Set<MinimalReflection> written = new HashSet<>();
        for (int nEmpty = 60; nEmpty >= 0; nEmpty--) {
            final List<MinimalReflection> mrs = getMrs(nEmpty);
            for (MinimalReflection mr : mrs) {
                if (!written.contains(mr)) {
                    writeTree(out, mr, written);
                }
            }
        }

        out.close();
    }

    /**
     * Write this mr and all unwritten successors to out. Update written.
     */
    private void writeTree(DataOutputStream out, MinimalReflection mr, Set<MinimalReflection> written) throws IOException {
        out.writeLong(mr.mover);
        out.writeLong(mr.enemy);
        writeNodeData(out, mr, written);

        // test without the compression first.
//
//        // calculate subnodes that need to be written, in order
//        long moves = mr.calcMoves();
//        long writeMask = 0;
//
//        int nMoves = 0;
//        while (moves != 0) {
//            int sq = Long.numberOfTrailingZeros(moves);
//            moves &= moves-1;
//
//            nMoves++;
//        }
    }

    private void writeNodeData(DataOutputStream out, MinimalReflection mr, Set<MinimalReflection> written) throws IOException {
        getData(mr).write(out);
        written.add(mr);
    }

    /**
     * Read a book from a stream and close the stream.
     *
     * @param inStream stream to read from
     * @return newly created book
     * @throws IOException if the operation can't be completed.
     */
    public static Book read(InputStream inStream) throws IOException {
        DataInputStream in = new DataInputStream(inStream);
        final int version = in.readInt();
        if (version != 1) {
            throw new IOException("Invalid version number : " + version);
        }

        final Book book = new Book();
        try {
            // completes at EOF by throwing an EOFException
            //noinspection InfiniteLoopStatement
            for (; ; ) {
                long mover = in.readLong();
                long enemy = in.readLong();
                final MinimalReflection mr = new MinimalReflection(mover, enemy);
                final Data data = Data.read(in);
                book.entries.put(mr, data);
            }
        } catch (EOFException e) {
            // expected at end of file
        }

        in.close();
        return book;
    }

    /**
     * Calculate the list of successors for a board position.
     *
     * @param board root position
     * @return list of all successors in this Book
     */
    public List<Successor> getSuccessors(Board board) {
        long moves = board.calcMoves();
        if (moves==0) {
            throw new IllegalArgumentException("Board must have legal moves, but was " + board);
        }
        List<Successor> successors = new ArrayList<>();
        while (moves!=0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moves &= moves-1;
            Board sub = board.play(sq);
            final Data subData = getData(sub);
            if (subData != null) {
                successors.add(new Successor(sq, -subData.score, subData.getNodeType()));
            }
        }
        return successors;
    }

    public enum NodeType {
        SOLVED, ULEAF, UBRANCH
    }

    public static class Data {
        private final @NotNull NodeType nodeType;
        private final int bestUnplayedSq;
        private final int score;

        Data() {
            this(NodeType.UBRANCH, 0, -1);
        }

        public Data(@NotNull NodeType nodeType, int score, int bestUnplayedSq) {
            this.nodeType = nodeType;
            this.bestUnplayedSq = bestUnplayedSq;
            this.score = score;
        }

        @NotNull public NodeType getNodeType() {
            return nodeType;
        }

        public int getBestUnplayedSq() {
            if (nodeType != NodeType.UBRANCH) {
                throw new IllegalStateException("No square available for node type " + nodeType);
            }
            return bestUnplayedSq;
        }

        /**
         * Get book score
         *
         * @return score, in disks, from mover's point of view.
         */
        public int getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            return nodeType == data.nodeType
                    && score == data.score
                    && ((nodeType != NodeType.UBRANCH) || (bestUnplayedSq == data.bestUnplayedSq));

        }

        @Override
        public int hashCode() {
            int result = nodeType.hashCode();
            result = 31 * result + bestUnplayedSq;
            if (nodeType == NodeType.UBRANCH) {
                result = 31 * result + score;
            }
            return result;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("(").append(nodeType).append(": ").append(score);
            if (nodeType == NodeType.UBRANCH) {
                sb.append(" sq=");
                if (bestUnplayedSq >= 0) {
                    sb.append(BitBoardUtils.sqToText(bestUnplayedSq));
                } else {
                    sb.append(bestUnplayedSq);
                }
            }
            sb.append(")");
            return sb.toString();
        }

        public void write(DataOutputStream out) throws IOException {
            out.writeByte(nodeType.ordinal());
            out.writeByte(score);
            if (nodeType == NodeType.UBRANCH) {
                out.writeByte(bestUnplayedSq);
            }
        }

        public static Data read(DataInputStream in) throws IOException {
            final NodeType type = NodeType.values()[in.readByte()];
            final int score = in.readByte();
            final int bestUnplayedSq = (type == NodeType.UBRANCH) ? in.readByte() : -1;
            return new Data(type, score, bestUnplayedSq);
        }
    }

    @EqualsAndHashCode
    public static class Successor {
        public final int sq;
        public final int score; // score from parent position point of view, in discs.
        public final NodeType nodeType;

        public Successor(int sq, int score, NodeType nodeType) {
            this.sq = sq;
            this.score = score;
            this.nodeType = nodeType;
        }

        @Override public String toString() {
            return BitBoardUtils.sqToText(sq) + " -> " + score;
        }
    }
}
