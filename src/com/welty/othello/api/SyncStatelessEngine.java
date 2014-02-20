package com.welty.othello.api;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.solver.SearchAbortedException;
import com.welty.othello.gdk.COsBoard;
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
    private final ResponseHandler responseHandler;

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
    };

    private static final boolean debug = true;
    private String status = "";

    public SyncStatelessEngine(Eval eval, String options, ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        evalSyncEngine = new EvalSyncEngine(eval, options);
        new Thread(new Runner(), getName()).start();
    }

    @Override public void terminate() {
    }

    @Override public void learn(PingPong pingPong, NBoardState state) {
    }

    @Override public void requestHints(final PingPong pingPong, final NBoardState state, int nMoves) {
        if (debug) {
            System.out.println("> hint " + nMoves + " from " + state.getGame().getPos().board);
        }
        final int pong = pingPong.next();

        requests.add(new Runnable() {
            @Override public void run() {
                // todo return hints for nMoves moves rather than 1

                final COsBoard board = state.getGame().getPos().board;
                final Position position = Position.of(board);
                // calcMove() can't handle a pass. So we handle it right here.
                if (position.hasLegalMove()) {
                    final EvalSyncEngine.Listener engineListener = new EvalSyncEngine.Listener() {
                        @Override public void updateStatus(String status) {
                            setStatus(status);
                        }

                        @Override public void updateNodeStats(long nodeCount, long millis) {
                            responseHandler.handle(new NodeStatsResponse(pong, nodeCount, millis*0.001));
                        }

                        @Override public void hint(MoveScore moveScore, Depth depth) {
                            responseHandler.handle(moveScore.toHintResponse(pong, depth));
                        }
                    };
                    evalSyncEngine.calcHints(position, state.getMaxDepth(), abortCheck, engineListener);
                } else {
                    final OsMoveListItem mli = OsMoveListItem.PASS;
                    final String pv = mli.move.toString();
                    final float eval = Float.NaN;
                    final HintResponse response = new HintResponse(pong, false, pv, new Value(eval), 0, new Depth(state.getMaxDepth()), "");
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
                final OsMoveListItem mli = calcMli(state);
                final MoveResponse response = new MoveResponse(pong, mli);
                responseHandler.handle(response);
            }
        });
    }

    @NotNull @Override public String getName() {
        return evalSyncEngine.toString();
    }

    public synchronized void setStatus(@NotNull String status) {
        this.status = status;
        responseHandler.handle(new StatusChangedResponse());
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
    @NotNull OsMoveListItem calcMli(NBoardState state) {
        final COsBoard board = state.getGame().getPos().board;
        final Position position = Position.of(board);
        // calcMove() can't handle a pass. So we handle it right here.
        if (position.hasLegalMove()) {
            final long t0 = System.currentTimeMillis();
            final MoveScore moveScore = evalSyncEngine.calcMove(position, state.getMaxDepth(), abortCheck);
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
                    try {
                        take.run();
                    } catch (SearchAbortedException e) {
                        // The search was aborted because we have a new task.
                        // continue so we do the new task.
                    }
                    setStatus("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
