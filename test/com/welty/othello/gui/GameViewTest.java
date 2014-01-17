package com.welty.othello.gui;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import junit.framework.TestCase;
import org.mockito.Mockito;

/**
 */
public class GameViewTest extends TestCase {
    final GameView gameView = new GameView();

    @Override protected void setUp() throws Exception {
        gameView.setGameGgf("(;GM[Othello]PC[Rose]PB[NTest:2]PW[b1:3]RE[-64]TI[0]TY[8r]BO[8 -------- -------- -----*-- ---***-- ---**O-- ----**-- -------- -------- O]W[D7/3.85]B[D6/-2.82/0.001]W[C5/4.02]B[C4/-3.87/0.001]W[C7/6.18/0.001]B[G5/-4.55]W[C3/5.03/0.001]B[C6/-2.31]W[E7/8.90/0.001]B[B5/-4.29]W[F7/8.10]B[D3/-5.66/0.001]W[B6/9.18/0.002]B[B3/-6.79]W[G6/12.71/0.001]B[D8/-9.15]W[C8/15.34/0.001]B[E8/-8.62]W[F8/22.57/0.001]B[H6/-12.65]W[A4/23.53/0.002]B[A5/-17.59/0.001]W[A6/33.79/0.001]B[A7/-23.86]W[A8/32.80/0.002]B[B8/-33.98]W[B7/45.62/0.001]B[G7/-39.87]W[B4/46.99/0.001]B[G8]W[H8/56.38]B[PASS]W[C2/64.09]B[B2/-57.44/0.001]W[D2/66.27]B[A2/-62.39]W[A3/65.64/0.001]B[C1/-59.42]W[E3/64.11/0.001]B[E2/-66.55]W[F1/68.21/0.001]B[D1/-65.51]W[A1/71.77/0.001]B[PASS]W[B1/73.14]B[PASS]W[E1/67.18]B[PASS]W[F2/68.59/0.001]B[G2]W[H1/70.47]B[PASS]W[H7/64.00]B[PASS]W[G4/64.00]B[PASS]W[H5/64.00];)");
    }

    public void testGetPosition() throws Exception {
        assertEquals("-------- -------- -----*-- ---***-- ---**O-- ----**-- -------- -------- O", gameView.getPosition().positionString(" "));
    }

    public void testNMoves() {
        assertEquals(57, gameView.nMoves());
    }

    public void testFirst() {
        gameView.setIPosition(0);
        gameView.first();
        assertEquals(0, gameView.getIPosition());

        gameView.setIPosition(1);
        gameView.first();
        assertEquals(0, gameView.getIPosition());

        gameView.setIPosition(2);
        gameView.first();
        assertEquals(0, gameView.getIPosition());
    }

    public void testPrev() throws Exception {
        gameView.setIPosition(0);
        gameView.prev();
        assertEquals(0, gameView.getIPosition());

        gameView.setIPosition(1);
        gameView.prev();
        assertEquals(0, gameView.getIPosition());

        gameView.setIPosition(2);
        gameView.prev();
        assertEquals(1, gameView.getIPosition());
    }

    public void testNext() throws Exception {
        final int n = gameView.nMoves();

        gameView.setIPosition(n);
        gameView.next();
        assertEquals(n, gameView.getIPosition());

        gameView.setIPosition(n - 1);
        gameView.next();
        assertEquals(n, gameView.getIPosition());

        gameView.setIPosition(n - 2);
        gameView.next();
        assertEquals(n - 1, gameView.getIPosition());
    }

    public void testLast() throws Exception {
        final int n = gameView.nMoves();

        gameView.setIPosition(n);
        gameView.last();
        assertEquals(n, gameView.getIPosition());

        gameView.setIPosition(n - 1);
        gameView.last();
        assertEquals(n, gameView.getIPosition());

        gameView.setIPosition(n - 2);
        gameView.last();
        assertEquals(n, gameView.getIPosition());
    }

    public void testChangeFiring() throws Exception {
        {
            final GameView.ChangeListener listener = Mockito.mock(GameView.ChangeListener.class);
            gameView.addChangeListener(listener);
            gameView.setIPosition(3);
            Mockito.verify(listener).gameViewChanged();
        }
        {
            final GameView.ChangeListener listener = Mockito.mock(GameView.ChangeListener.class);
            gameView.addChangeListener(listener);
            gameView.next();
            Mockito.verify(listener).gameViewChanged();
        }
        {
            final GameView.ChangeListener listener = Mockito.mock(GameView.ChangeListener.class);
            gameView.addChangeListener(listener);
            gameView.last();
            Mockito.verify(listener).gameViewChanged();
        }
        {
            gameView.setIPosition(3);
            final GameView.ChangeListener listener = Mockito.mock(GameView.ChangeListener.class);
            gameView.addChangeListener(listener);
            gameView.prev();
            Mockito.verify(listener).gameViewChanged();
        }
        {
            gameView.setIPosition(3);
            final GameView.ChangeListener listener = Mockito.mock(GameView.ChangeListener.class);
            gameView.addChangeListener(listener);
            gameView.first();
            Mockito.verify(listener).gameViewChanged();
        }
        {
            final GameView.ChangeListener listener = Mockito.mock(GameView.ChangeListener.class);
            gameView.addChangeListener(listener);
            gameView.setGameGgf("(;GM[Othello]PC[Rose]PB[NTest:2]PW[b1:3]RE[-64]TI[0]TY[8r]BO[8 -------- -------- -----*-- ---***-- ---**O-- ----**-- -------- -------- O]W[D7/3.85]B[D6/-2.82/0.001]W[C5/4.02];)");
            Mockito.verify(listener).gameViewChanged();
        }
    }

    public void testResetPositionOnGameChange() {
        gameView.last();
        gameView.setGameGgf("(;GM[Othello]PC[Rose]PB[NTest:2]PW[b1:3]RE[-64]TI[0]TY[8r]BO[8 -------- -------- -----*-- ---***-- ---**O-- ----**-- -------- -------- O]W[D7/3.85]B[D6/-2.82/0.001]W[C5/4.02];)");
        assertEquals(0, gameView.getIPosition());
    }

    public void testNewGame() {
        gameView.last();
        gameView.newGame(null, null);
        assertEquals(Position.START_POSITION, gameView.getStartPosition());
        assertEquals(0, gameView.getIPosition());
        assertEquals(Position.START_POSITION, gameView.getPosition());
        assertEquals(0, gameView.nMoves());
    }

    final int f5 = BitBoardUtils.textToSq("F5");

    public void testBlackEngineMoveRequestResponse() {
        GameView gameView = new GameView();

        final Engine mock = Mockito.mock(Engine.class);
        final String engineName = "Mock Engine";
        Mockito.when(mock.getName()).thenReturn(engineName);

        gameView.newGame(mock, null);
        assertEquals(engineName, gameView.getBlackName());
        Mockito.verify(mock).requestMove(gameView, Position.START_POSITION, 1);

        gameView.engineMove(new MoveScore(f5, 0), -13);
        assertEquals("wrong ping, ignore", Position.START_POSITION, gameView.getPosition());
        gameView.engineMove(new MoveScore(f5, 0), 1);
        assertEquals("right ping, position should be updated.", Position.START_POSITION.play(f5), gameView.getPosition());
    }

    private static final String mockEngineName = "Mock Engine";

    public void testWhiteEngineMoveRequestResponse() {
        GameView gameView = new GameView();

        final Engine mock = getMockEngine();

        gameView.newGame(null, mock);
        assertEquals(mockEngineName, gameView.getWhiteName());
        verifyNoRequest(mock);

        gameView.boardClick(f5);
        final Position position = Position.START_POSITION.play(f5);
        Mockito.verify(mock).requestMove(gameView, position, 2);
    }

    private Engine getMockEngine() {
        final Engine mock = Mockito.mock(Engine.class);
        Mockito.when(mock.getName()).thenReturn(mockEngineName);
        return mock;
    }

    public void testDontRequestMoveAtEndOfGame() {
        GameView gameView = new GameView();

        final Engine mock = Mockito.mock(Engine.class);
        final String engineName = "Mock Engine";
        Mockito.when(mock.getName()).thenReturn(engineName);

        final Position pos = new Position(-1L, 0, true);
        gameView.newGame(mock, mock, pos);
        verifyNoRequest(mock);
    }

    private static void verifyNoRequest(Engine mock) {
        Mockito.verify(mock, Mockito.never()).requestMove(Mockito.any(GameView.class), Mockito.any(Position.class), Mockito.anyInt());
    }

    public void testHumanMove() {
        final GameView gameView = new GameView();
        final Engine mock = Mockito.mock(Engine.class);
        final String engineName = "Mock Engine";
        Mockito.when(mock.getName()).thenReturn(engineName);

        gameView.newGame(null, mock);

        gameView.boardClick(f5);
        assertEquals("Human to move, update position", Position.START_POSITION.play(f5), gameView.getPosition());

        gameView.boardClick(BitBoardUtils.textToSq("D6"));
        assertEquals("Computer to move, don't update position", Position.START_POSITION.play(f5), gameView.getPosition());
    }
}
