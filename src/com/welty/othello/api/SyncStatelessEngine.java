package com.welty.othello.api;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.gdk.OsBoard;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.HintResponse;
import com.welty.othello.protocol.MoveResponse;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A StatelessEngine that responds on the same thread it receives data on
 * <p/>
 * Threading: This implementation assumes all incoming calls will be from the same thread.
 */
public class SyncStatelessEngine implements StatelessEngine {
    private final EvalSyncEngine evalSyncEngine;
    private final ResponseHandler responseHandler;

    private static final boolean debug = true;

    public SyncStatelessEngine(Eval eval, String options, ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        evalSyncEngine = new EvalSyncEngine(eval, options);
    }

    @Override public void terminate() {
    }

    @Override public void learn(PingPong pingPong, SearchState state) {
    }

    @Override public void requestHints(PingPong pingPong, SearchState state, int nMoves) {
        if (debug) {
            System.out.println("> hint " + nMoves + " from " + state.getGame().getPos().board);
        }
        // todo return hints for nMoves moves rather than 1
        final int pong = pingPong.next();

        final OsMoveListItem mli = calcMli(state);
        final String pv = mli.move.toString();
        final float eval = (float) mli.getEval();
        final HintResponse response = new HintResponse(pong, false, pv, "" + eval, 0, "" + state.getMaxDepth(), "");
        if (debug) {
            System.out.println("< " + response);
        }
        responseHandler.handle(response);
    }

    @Override public void requestMove(PingPong pingPong, SearchState state) {
        final int pong = pingPong.next();
        final OsMoveListItem mli = calcMli(state);
        final MoveResponse response = new MoveResponse(pong, mli);
        responseHandler.handle(response);
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

    /**
     * Calculate MoveListItem.
     * <p/>
     * This routine must handle passes (and it does so by immediately passing, without
     * contacting the engine).
     *
     * @param state position and engine options
     * @return MoveListItem
     */
    @NotNull OsMoveListItem calcMli(SearchState state) {
        final OsBoard board = state.getGame().getPos().board;
        final Position position = Position.of(board);
        // calcMove() can't handle a pass. So we handle it right here.
        if (position.hasLegalMove()) {
            final MoveScore moveScore = evalSyncEngine.calcMove(position, state.getMaxDepth());
            return moveScore.toMli();
        } else {
            return OsMoveListItem.PASS;
        }
    }
}
