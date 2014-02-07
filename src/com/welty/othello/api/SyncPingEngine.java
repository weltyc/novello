package com.welty.othello.api;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsBoard;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * A PingEngine that responds on the same thread it receives data on
 * <p/>
 * Threading: This implementation assumes all incoming calls will be from the same thread.
 */
public class SyncPingEngine extends PingEngine {
    private final EvalSyncEngine evalSyncEngine;
    private COsGame game;
    private int lastPong;

    public SyncPingEngine(Eval eval, int maxDepth, String options) {
        evalSyncEngine = new EvalSyncEngine(eval, maxDepth, options);
    }

    @Override public void terminate() {
    }

    @Override public synchronized void setGame(int ping, COsGame game) {
        this.game = new COsGame(game);
        firePong(ping);
    }

    @Override public void learn() {
    }

    @Override public void setContempt(int ping, int contempt) {
    }

    @Override public synchronized void setMaxDepth(int ping, int maxDepth) {
        evalSyncEngine.setMaxDepth(maxDepth);
        firePong(ping);
    }

    @Override public synchronized void sendMove(int ping, OsMoveListItem mli) {
        game.Update(mli);
        firePong(ping);
    }

    @Override public void requestHints(int nMoves) {
        // todo return hints for nMoves moves rather than 1
        final MoveScore moveScore = calcMove();
        final String pv = BitBoardUtils.sqToText(moveScore.sq);
        final CMove move = new CMove((byte) moveScore.sq);
        final String eval = "" + moveScore.score;
        fireHint(lastPong, false, pv, move, eval, 0, "2", "");
    }

    @Override protected void firePong(int pong) {
        super.firePong(pong);
        lastPong = pong;
    }

    @Override public void requestMove() {
        final MoveScore moveScore = calcMove();
        fireEngineMove(lastPong, new OsMoveListItem(moveScore.toString()));

    }

    @NotNull @Override public String getName() {
        return evalSyncEngine.toString();
    }

    @NotNull @Override public String getStatus() {
        return "";
    }

    @Override public void sendPing(int ping) {
        firePong(ping);
    }

    private MoveScore calcMove() {
        final OsBoard board = game.GetPos().board;
        final Position position = Position.of(board);
        return evalSyncEngine.calcMove(position);
    }
}
