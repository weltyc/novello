package com.welty.othello.api;

import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsBoard;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

public class SyncPingEngine extends PingEngine {
    private final EvalSyncEngine evalSyncEngine;
    private COsGame game;

    public SyncPingEngine(Eval eval, int maxDepth, String options) {
        evalSyncEngine = new EvalSyncEngine(eval, maxDepth, options);
    }

    @Override public void terminate() {
    }

    @Override public synchronized void setGame(int ping, COsGame game) {
        game = new COsGame(game);
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
        final OsBoard board = game.GetPos().board;
        final Position position = Position.of(board);
        evalSyncEngine.calcMove(position);
    }

    @Override public void requestMove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull @Override public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull @Override public String getStatus() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void ping(int ping) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
