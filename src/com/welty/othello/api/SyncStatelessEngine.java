package com.welty.othello.api;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.solver.SearchAbortedException;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.*;
import org.jetbrains.annotations.NotNull;

/**
 * A StatelessEngine that responds on the same thread it receives data on
 * <p/>
 * Threading: This implementation assumes all incoming calls will be from the same thread.
 */
public class SyncStatelessEngine implements StatelessEngine {
    private final EvalSyncEngine evalSyncEngine;
    private final String name;
    private final @NotNull ResponseHandler responseHandler;

    /**
     * The next request queued up for the engine, or null if no request is queued up
     */
    private final RequestQueue requests = new RequestQueue();

    /**
     * Checker to see if the search should be aborted.
     * <p/>
     * This implementation aborts if there are new requests in the request queue.
     */
    private final AbortCheck abortCheck = new AbortCheck() {
        @Override public boolean shouldAbort() {
            return requests.hasRequest();
        }

        @Override public boolean abortNextRound() {
            return shouldAbort();
        }
    };

    public static final boolean debug = true;
    private String status = "";

    public SyncStatelessEngine(String name, Eval eval, String options, @NotNull ResponseHandler responseHandler) {
        this.name = name;
        this.responseHandler = responseHandler;
        evalSyncEngine = new EvalSyncEngine(eval, options);
        new Thread(new Runner(), getName()).start();
    }

    @Override public void terminate() {
    }

    @Override public void learn(PingPong pingPong, final NBoardState state) {
        final int pong = pingPong.next();
        requests.add(new Runnable() {
            @Override public void run() {
                evalSyncEngine.learn(state.getGame(), state.getMaxMidgameDepth(), abortCheck, new EngineListener(pong));
            }
        });
    }

    @Override public void analyze(PingPong pingPong, final NBoardState state) {
        final int pong = pingPong.next();
        requests.add(new Runnable() {
            @Override public void run() {
                evalSyncEngine.analyze(state.getGame(), state.getMaxMidgameDepth(), abortCheck, new EngineListener(pong));
            }
        });
    }

    @Override public void requestHints(final PingPong pingPong, final NBoardState state, final int nMoves) {
        if (debug) {
            System.out.println("> hint " + nMoves + " from " + state.getGame().getPos().board);
        }
        final int pong = pingPong.next();

        requests.add(new Runnable() {
            @Override public void run() {
                final COsBoard board = state.getGame().getPos().board;
                final Position position = Position.of(board);
                // calcMove() can't handle a pass. So we handle it right here.
                if (position.hasLegalMove()) {
                    try {
                        evalSyncEngine.calcHints(position, state.getMaxMidgameDepth(), nMoves, abortCheck, new EngineListener(pong));
                    } catch (SearchAbortedException e) {
                        // We're done because there's a new request. Move on to the next request.
                    }
                } else {
                    final OsMoveListItem mli = OsMoveListItem.PASS;
                    final String pv = mli.move.toString();
                    final float eval = Float.NaN;
                    final HintResponse response = new HintResponse(pong, false, pv, new Value(eval), 0, new Depth(state.getMaxMidgameDepth()), "");
                    if (debug) {
                        System.out.println("< " + response);
                    }
                    responseHandler.handle(response);
                }
            }
        });

    }

    @Override public void requestMove(PingPong pingPong, final NBoardState state) {
        final int pong = pingPong.next();
        requests.add(new Runnable() {
            @Override public void run() {
                final OsMoveListItem mli = calcMli(state, pong);
                responseHandler.handle(new MoveResponse(pong, mli));
            }
        });
    }

    @NotNull @Override public String getName() {
        return name;
    }

    public synchronized void setStatus(@NotNull String status) {
        this.status = status;
        responseHandler.handle(new StatusChangedResponse(status));
    }

    @NotNull @Override public synchronized String getStatus() {
        return status;
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
    @NotNull OsMoveListItem calcMli(NBoardState state, int pong) {
        final COsPosition pos = state.getGame().getPos();
        final COsBoard board = pos.board;
        final Position position = Position.of(board);
        // calcMove() can't handle a pass. So we handle it right here.
        if (position.hasLegalMove()) {
            final long t0 = System.currentTimeMillis();
            final MoveScore moveScore = evalSyncEngine.calcMove(position, pos.getCurrentClock(), state.getMaxMidgameDepth(), abortCheck, new EngineListener(pong));
            return moveScore.toMli(System.currentTimeMillis() - t0);
        } else {
            return OsMoveListItem.PASS;
        }
    }

    /**
     * Runs requests from the queue
     */
    private class Runner implements Runnable {
        @Override public void run() {
            while (true) {
                try {
                    final Runnable take = requests.take();
                    setStatus("Thinking...");
                    take.run();
                    setStatus("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class EngineListener implements EvalSyncEngine.Listener {
        private final int pong;

        public EngineListener(int pong) {
            this.pong = pong;
        }

        @Override public void updateStatus(String status) {
            setStatus(status);
        }

        @Override public void updateNodeStats(long nodeCount, long millis) {
            responseHandler.handle(new NodeStatsResponse(pong, nodeCount, millis * 0.001));
        }

        @Override public void hint(MoveScore moveScore, Depth depth) {
            responseHandler.handle(moveScore.toHintResponse(pong, depth));
        }

        @Override public void analysis(int moveNumber, double eval) {
            responseHandler.handle(new AnalysisResponse(pong, moveNumber, eval));
        }
    }
}
