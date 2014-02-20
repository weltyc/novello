package com.welty.othello.api;

import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.ResponseParser;
import junit.framework.TestCase;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ParsedEngineTest extends TestCase {
    public void testUpdateState() {
        final NBoardEngine nBoardEngine = Mockito.mock(NBoardEngine.class);
        final ParsedEngine parsedEngine = new ParsedEngine(Mockito.mock(ResponseParser.class), nBoardEngine);
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);

        // New game. must send set game message and depth, but contempt defaults to 0 so don't send
        parsedEngine.updateState(new NBoardState(game, 0, 12, 0));
        verify(nBoardEngine).sendCommand("set game " + game.toString());
        verify(nBoardEngine).sendCommand("set depth 12");
        verifyNoMoreInteractions(nBoardEngine);

        // contempt changed but not board state or depth.
        parsedEngine.updateState(new NBoardState(game, 0, 12, 1));
        verify(nBoardEngine).sendCommand("set contempt 1");
        verifyNoMoreInteractions(nBoardEngine);

        // depth and board changed, need to resend, but contempt didn't so don't send that.
        game.append(new OsMoveListItem("F5//1.2"));
        parsedEngine.updateState(new NBoardState(game, 1, 4, 1));
        verify(nBoardEngine).sendCommand("move F5//1.2");
        verify(nBoardEngine).sendCommand("set depth 4");
        verifyNoMoreInteractions(nBoardEngine);

        // rewind game; a 'move' command won't work so need to resend entire game.
        // depth and contempt unchanged so shouldn't be sent.
        Mockito.reset(nBoardEngine);
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);
        parsedEngine.updateState(new NBoardState(game, 0, 4, 1));
        verify(nBoardEngine).sendCommand("set game " + game.toString());
        verifyNoMoreInteractions(nBoardEngine);
    }
}
