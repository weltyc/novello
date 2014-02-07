package com.welty.othello.api;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsBoard;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * A StatelessEngine that responds on the same thread it receives data on
 * <p/>
 * Threading: This implementation assumes all incoming calls will be from the same thread.
 */
public class SyncStatelessEngine extends StatelessEngine {
    private final EvalSyncEngine evalSyncEngine;

    public SyncStatelessEngine(Eval eval, String options) {
        evalSyncEngine = new EvalSyncEngine(eval, options);
    }

    @Override public void terminate() {
    }

    @Override public void learn(PingPong pingPong, SearchState state) {
    }

    @Override public void requestHints(PingPong pingPong, SearchState state, int nMoves) {
        // todo return hints for nMoves moves rather than 1
        final int pong = pingPong.next();

        final MoveScore moveScore = calcMove(state);
        final String pv = BitBoardUtils.sqToText(moveScore.sq);
        final CMove move = new CMove((byte) moveScore.sq);
        final String eval = "" + moveScore.score;
        fireHint(pong, false, pv, move, eval, 0, "2", "");
    }

    @Override public void requestMove(PingPong pingPong, SearchState state) {
        final int pong = pingPong.next();
        final MoveScore moveScore = calcMove(state);
        fireEngineMove(pong, new OsMoveListItem(moveScore.toString()));
    }

    @NotNull @Override public String getName() {
        return evalSyncEngine.toString();
    }

    @NotNull @Override public String getStatus() {
        return "";
    }

    @Override public boolean isReady() {
        return true;
    }

    private MoveScore calcMove(SearchState state) {
        final OsBoard board = state.getGame().GetPos().board;
        final Position position = Position.of(board);
        return evalSyncEngine.calcMove(position, state.getMaxDepth());
    }
}
