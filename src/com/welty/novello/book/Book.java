package com.welty.novello.book;

import com.orbanova.common.gui.ProgressUpdater;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Utils;
import com.welty.novello.core.*;
import com.welty.othello.gdk.COsGame;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * An Othello opening book.
 *
 * A description of the book is in `notes/spec/book.md`
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

    /**
     * Determine the os-specific location to store the book.
     *
     * @return the File where the book should be stored, or null if no location to store the book could be found.
     */
    public static @Nullable File getBookLocation() {
        return bookLocation;
    }

    public static final @Nullable File bookLocation = createBookLocation();

    private static @Nullable File createBookLocation() {
        final File file = calcBookLocation();
        if (file == null) {
            return null;
        }
        final File dir = file.getParentFile();
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir.isDirectory() ? file : null;
    }

    private static @Nullable File calcBookLocation() {
        String loc = System.getenv("APPDATA");
        if (isADirectory(loc)) {
            // windows
            return new File(loc, "Welty/NBoard/book.nbb");
        } else {
            // not windows
            loc = System.getProperty("user.home");
            if (notADirectory(loc)) {
                throw new IllegalStateException("Unable to find book directory");
            }
            String macLoc = loc + "/Local Settings/ApplicationData";
            if (isADirectory(macLoc)) {
                // mac
                return new File(macLoc, "NBoard/book.nbb");
            } else {

                // linux
                return new File(loc, "/.nboard/book.nbb");
            }
        }
    }

    private static boolean isADirectory(String loc) {
        return !notADirectory(loc);
    }

    private static boolean notADirectory(String loc) {
        return loc == null || loc.isEmpty() || !new File(loc).isDirectory();
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
    synchronized void addUnevaluatedPos(Board pos) {
        if (pos.nEmpty() >= minDepth() && pos.hasLegalMove()) {
            final Data data = getData(pos);
            if (data == null || data.getNodeType() == NodeType.ULEAF) {
                MinimalReflection mr = pos.minimalReflection();
                final Data value = new Data();
                put(mr, value);
            }
        }
    }

    private Data put(MinimalReflection mr, Data value) {
        if (mr.calcMoves() == 0) {
            throw new IllegalArgumentException("Should only call this when there's a legal move.");
        }
        if (mr.nEmpty() > 60) {
            throw new IllegalArgumentException("Can't add to book with > 60 empty");
        }
        return entries.put(mr, value);
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
    synchronized void putPos(Board board, int score, NodeType nodeType, int bestUnplayedSq) {
        if (!board.hasLegalMove()) {
            board = board.pass();
            score = -score;
            if (!board.hasLegalMove()) {
                throw new IllegalArgumentException("can't put terminal position into book");
            }
        }
        final Data existingData = getData(board);
        if (existingData != null) {
            final NodeType ent = existingData.getNodeType();
            switch (ent) {
                case SOLVED:
                    // shouldn't need to update a SOLVED node, but it might happen in multithreaded book generation
                    // if multiple positions have this as successor.
                    //
                    // If trying to update a SOLVED node with an unsolved node, just throw away the unsolved node.
                    if (nodeType == NodeType.SOLVED) {
                        if (existingData.score != score) {
                            throw new IllegalStateException("Bug in solver, had multiple values (" + existingData.score + ", " + score + " for\n" + board);
                        }
                    }
                    return;
                case UBRANCH:
                    if (nodeType == NodeType.ULEAF) {
                        throw new IllegalArgumentException("Can't update a UBRANCH node with a ULEAF node.");
                    }
                    break;
            }
        }
        put(board.minimalReflection(), new Data(nodeType, score, bestUnplayedSq));
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
    public synchronized int sizeAtEmpty(int nEmpty) {
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
    public synchronized List<MinimalReflection> getMrs(int nEmpty) {
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
    public synchronized Data getData(Board board) {
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
     * @param adder     midgame and endgame searcher
     * @param printLog  if true, print log messages to console
     */
    public void negamax(Adder adder, boolean printLog) {
        negamax(adder, printLog, null);
    }

    /**
     * Negamax the book, as described in book.md
     *
     * @param adder     midgame and endgame searcher
     * @param printLog  if true, print log messages to console
     * @param writeFile location to write book. Book is written after each empty is completed.
     */
    public void negamax(Adder adder, boolean printLog, File writeFile) {
        final long tStart = System.currentTimeMillis();

        final int nThreads = Runtime.getRuntime().availableProcessors();
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        final int solveDepth = Math.max(minDepth(), adder.solveDepth());
        System.out.println("negamax adding to book. nThreads=" + nThreads + "; minDepth=" + minDepth());

        for (int nEmpty = minDepth(); nEmpty <= 60; nEmpty++) {
            final long t0 = System.currentTimeMillis();
            final List<MinimalReflection> mrs = getMrs(nEmpty);
            try (ProgressUpdater progress = new ProgressUpdater("Adding all games to book at  at " + nEmpty + " empties", mrs.size())) {
                progress.setAutoNote("positions");
                List<Future> futures = new ArrayList<>();

                for (MinimalReflection mr : mrs) {
                    final boolean isSolve = nEmpty <= solveDepth;
                    final ValueTask task = new ValueTask(adder, mr, isSolve, progress);
                    final Future<?> future = executorService.submit(task);
                    futures.add(future);
                }
                for (Future future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        // shouldn't happen because we never interrupt the thread.
                        throw new RuntimeException("Shouldn't happen");
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (printLog) {
                final long dt = System.currentTimeMillis() - t0;
                System.out.println();
                final String message = String.format("%2d empties complete in %,.1f s", nEmpty, dt * 0.001);
                log.info(message);
            }
            if (writeFile != null) {
                try {
                    writeToFile(writeFile);
                    log.info(nEmpty + " written to " + writeFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // don't interrupt the darn thing and you won't get this exception.
            e.printStackTrace();
        }

        final long dt = System.currentTimeMillis() - tStart;
        log.info(String.format("Negamax complete in %,.1f s", dt * 0.001));
    }

    /**
     * Add a game to book and add all deviations.
     *
     * This is like "negamax" except only the positions in the game are valued.
     *
     * @param game
     * @param adder
     */
    public void learn(COsGame game, Adder adder) {
        add(game);
        negamax(adder, false);
    }
    /**
     * Load a book from the default location.
     *
     * @return the loaded Book, or a new Book if the default location contained no book.
     */
    public static Book load() {
        final File bookFile = Book.getBookLocation();
        if (bookFile!=null) {
            try (InputStream in = new BufferedInputStream(new FileInputStream(bookFile))) {
                return new Book(in);
            } catch(FileNotFoundException e) {
                System.out.println("No book at " + bookFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Book();
    }

    private final class ValueTask implements Runnable {
        private final Adder adder;
        private final MinimalReflection mr;
        private final boolean isSolve;
        private final ProgressUpdater progress;

        ValueTask(Adder adder, MinimalReflection mr, boolean isSolve, ProgressUpdater progress) {
            this.adder = adder;
            this.mr = mr;
            this.isSolve = isSolve;
            this.progress = progress;
        }

        @Override public void run() {
            if (isSolve) {
                valueUsingSolve(adder, mr.toBoard());
            } else {
                valueUsingMidgame(adder, mr.toBoard());
            }
            progress.update();
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

    synchronized int bestSubScore(Board board) {
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
     * Value the book: replace the score of each UBranch node with the score of its best subnode.
     */
    synchronized void value() {
        for (int nEmpty = minDepth(); nEmpty <= 60; nEmpty++) {
            for (MinimalReflection mr : getMrs(nEmpty)) {
                final Data data = getData(mr);
                if (data.getNodeType() == NodeType.UBRANCH) {
                    final Board board = mr.toBoard();
                    int score = bestSubScore(board);
                    if (score == -65) {
                        // no subnodes have been played.
                        // Use a value of 0 as our best guess.
                        score = 0;
                    }
                    put(mr, new Data(NodeType.UBRANCH, score, data.bestUnplayedSq));
                }
            }
        }
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
            putPos(board, score, NodeType.SOLVED);
            if (board.nEmpty() > minDepth()) {
                final Board subPos = board.play(moveScore.sq);
                if (subPos.calcPass() != 2) {
                    putPos(subPos, -score, NodeType.SOLVED);
                }
            }
        }
    }

    private static int centidisksToScore(MoveScore moveScore) {
        return Math.round(0.01f * moveScore.centidisks);
    }

    /**
     * Read a book from a stream and close the stream.
     *
     * @param inStream stream to read from
     * @throws IOException if the operation can't be completed.
     */
    public Book(InputStream inStream) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(inStream)) {
            zis.getNextEntry();
            try (DataInputStream in = new DataInputStream(zis)) {

                final int version = in.readInt();
                if (version != 2) {
                    throw new IOException("Invalid version number : " + version);
                }

                try {
                    // completes at EOF by throwing an EOFException
                    //noinspection InfiniteLoopStatement
                    for (; ; ) {
                        Set<MinimalReflection> written = new HashSet<>();
                        readTree2(in, written);
                    }
                } catch (EOFException e) {
                    // expected at end of file
                }

                value();
            }
        }
    }

    /**
     * Write this book to the path.
     * <p/>
     * Writes to a temp file and then renames it. This reduces the risk of losing the old file if the computer
     * loses power during a write.
     *
     * @param file path to write to
     */
    public void writeToFile(File file) throws IOException {
        final Path tempFile = File.createTempFile("nbb", "nbb").toPath();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
            write(out);
        }
        final Path dest = file.toPath();
        Files.move(tempFile, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Write this Book to a stream and close the stream.
     *
     * @param outStream stream to write to
     * @throws IOException if the operation can't be completed.
     */
    public void write(OutputStream outStream) throws IOException {
        try (final ZipOutputStream zos = new ZipOutputStream(outStream)) {
            zos.putNextEntry(new ZipEntry("A"));
            try (DataOutputStream out = new DataOutputStream(zos)) {

                // version number
                out.writeInt(2);

                // write trees from most empty to least.
                Set<MinimalReflection> written = new HashSet<>();
                for (int nEmpty = 60; nEmpty >= 0; nEmpty--) {
                    final List<MinimalReflection> mrs = getMrs(nEmpty);
                    for (MinimalReflection mr : mrs) {
                        if (!written.contains(mr)) {
                            writeTree2(out, mr, written);
                        }
                    }
                }
            }
        }
    }

    /**
     * Write this mr and all unwritten successors to out. Update written.
     */
    private void writeTree2(DataOutputStream out, MinimalReflection mr, Set<MinimalReflection> written) throws IOException {
        out.writeLong(mr.mover);
        out.writeLong(mr.enemy);
//        System.out.println(" Write Tree root\n " + mr);
        writeSubtree2(out, mr, written);
    }

    private void readTree2(DataInputStream in, Set<MinimalReflection> written) throws IOException {
        long mover = in.readLong();
        long enemy = in.readLong();
        final MinimalReflection mr = new MinimalReflection(mover, enemy);
//        System.out.println(" Read Tree root\n " + mr);
        readSubtree2(in, mr, written);
    }

    private void writeSubtree2(DataOutputStream out, MinimalReflection mr, Set<MinimalReflection> written) throws IOException {
        final Data data = getData(mr);
        data.write3(out);
        written.add(mr);

        if (mr.nEmpty() < minDepth()) {
            return;
        }

        // calculate subnodes that need to be written, in order
        long moves = mr.calcMoves();

        int moveId = -1;
        int prevMoveId = 0;

        while (moves != 0) {
            moveId++;
            int sq = Long.numberOfTrailingZeros(moves);
            moves &= moves - 1;
            Board sub = mr.toBoard().play(sq);
            if (!sub.hasLegalMove()) {
                sub = sub.pass();
                if (!sub.hasLegalMove()) {
                    continue;
                }
            }
            final MinimalReflection subMr = sub.toMr();
            if (!written.contains(subMr) && this.getData(subMr) != null) {
//                System.out.println("from " + mr.nEmpty() + ", writing the move " + BitBoardUtils.sqToText(sq));
                out.writeByte(moveId - prevMoveId);
                prevMoveId = moveId;
                writeSubtree2(out, subMr, written);
            }
        }
//        System.out.println("from " + mr.nEmpty() + ", completed the subtree");
        out.writeByte(-1);

        // write the best unplayed sq. If >=0, it is written as the index of the sq in the unplayed sqs list.
        if (data.nodeType == NodeType.UBRANCH) {
            int bestUnplayedSq = calcUBranchDeviationIndex(mr, data);
            out.writeByte(bestUnplayedSq);
        }
    }

    private int calcUBranchDeviationIndex(MinimalReflection mr, Data data) {
        int bestUnplayedSq = data.getBestUnplayedSq();
        if (bestUnplayedSq >= 0) {
            Data sub = getData(mr.toBoard().play(bestUnplayedSq));
            if (sub == null || sub.nodeType != NodeType.ULEAF) {
                bestUnplayedSq = -1;
            } else {
                bestUnplayedSq = calcDeviationIndex(mr, bestUnplayedSq);
            }
        }
        return bestUnplayedSq;
    }

    private int calcDeviationIndex(MinimalReflection mr, int bestUnplayedSq) {
        final List<Successor> successors = getSuccessors(mr.toBoard());
        int iMove = 0;
        for (Successor s : successors) {
            if (s.nodeType == NodeType.ULEAF) {
                if (s.sq == bestUnplayedSq) {
                    return iMove;
                }
                iMove++;
            }
        }
        throw new IllegalStateException("can't find " + BitBoardUtils.sqToText(bestUnplayedSq) + " in board\n" + mr);
    }

    private int calcDeviationSq(MinimalReflection mr, int iMove) {
        final List<Successor> successors = getSuccessors(mr.toBoard());
        for (Successor s : successors) {
            if (s.nodeType == NodeType.ULEAF) {
                if (iMove == 0) {
                    return s.sq;
                }
                iMove--;
            }
        }
        throw new IllegalStateException("not enough ULEAF moves in board\n" + mr);
    }

    private void readSubtree2(DataInputStream in, MinimalReflection mr, Set<MinimalReflection> written) throws IOException {
        Data data = readNodeData2(in, mr, written);

        if (mr.nEmpty() < minDepth()) {
            return;
        }

        int prevMoveId = 0;
        int moveIdDelta;
        while (-1 != (moveIdDelta = in.readByte())) {
            prevMoveId += moveIdDelta;
            int sq = sqFromMoveId(mr.toBoard(), prevMoveId);
//            System.out.println("from " + mr.nEmpty() + ", reading the move " + BitBoardUtils.sqToText(sq));
            Board sub = mr.toBoard().play(sq);
            if (!sub.hasLegalMove()) {
                sub = sub.pass();
                if (!sub.hasLegalMove()) {
                    throw new IllegalStateException("Program bug");
                }
            }
            final MinimalReflection subMr = sub.toMr();
            readSubtree2(in, subMr, written);
        }

        if (data.getNodeType() == NodeType.UBRANCH) {
            // bestUnplayedSq is -1. replace it with the correct value:
            int bestUnplayedSq = in.readByte();
            if (bestUnplayedSq >= 0) {
                bestUnplayedSq = calcDeviationSq(mr, bestUnplayedSq);
            }
            put(mr, new Data(NodeType.UBRANCH, data.getScore(), bestUnplayedSq));
        }
//        System.out.println("from " + mr.nEmpty() + ", completed the subtree");
    }

    private static int sqFromMoveId(Board board, int moveId) {
        long moves = board.calcMoves();
        while (moves != 0) {
            if (moveId == 0) {
                int sq = Long.numberOfTrailingZeros(moves);
                return sq;
            } else {
                moves &= moves - 1;
                moveId--;
            }
        }
        throw new IllegalArgumentException("Illegal moveId " + moveId + " for board " + board);
    }

    private Data readNodeData2(DataInputStream in, MinimalReflection mr, Set<MinimalReflection> written) throws IOException {
        final Data data = Data.read3(in);
        put(mr, data);
        written.add(mr);
        return data;
    }

    /**
     * Calculate the list of successors for a board position.
     *
     * @param board root position
     * @return list of all successors in this Book
     */
    public List<Successor> getSuccessors(Board board) {
        long moves = board.calcMoves();
        if (moves == 0) {
            throw new IllegalArgumentException("Board must have legal moves, but was " + board);
        }
        List<Successor> successors = new ArrayList<>();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moves &= moves - 1;
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

        public void write3(DataOutputStream out) throws IOException {
            out.writeByte(nodeType.ordinal());
            if (nodeType != NodeType.UBRANCH) {
                out.writeByte(score);
            }
        }

        public static Data read3(DataInputStream in) throws IOException {
            final NodeType type = NodeType.values()[in.readByte()];
            final int score;
            final int bestUnplayedSq;
            if (type == NodeType.UBRANCH) {
                score = 0;
                bestUnplayedSq = -1; // replace this after reading the rest of the tree.
            } else {
                score = in.readByte();
                bestUnplayedSq = -1;
            }
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
