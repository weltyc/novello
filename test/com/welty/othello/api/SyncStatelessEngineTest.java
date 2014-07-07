/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.othello.api;

import com.welty.novello.eval.DiskEval;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.ResponseHandler;
import junit.framework.TestCase;
import org.mockito.Mockito;

/**
 */
public class SyncStatelessEngineTest extends TestCase {
    public void testGetPass() {
        final SyncStatelessEngine engine = new SyncStatelessEngine("test", new DiskEval(), "", Mockito.mock(ResponseHandler.class));

        // a game where the next move is a pass
        COsGame game = new COsGame("(;GM[Othello]PC[GGS/os]DT[2009.07.02_09:24:22.MDT]PB[triple0]PW[Saio3000]RB[2173.03]RW[2175.52]TI[10:00//02:00]TY[8]RE[+0.000]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[f5]W[f6]B[e6]W[f4]B[e3]W[c5]B[c4]W[d6]B[c6]W[b5]B[d7]W[e7]B[b6]W[c8]B[f7]W[f8]B[a6]W[b3]B[c3]W[f3]B[g3]W[e2]B[g4]W[g5]B[f2]W[f1]B[c7]W[h4]B[h6]W[h5]B[g2]W[a4]B[b4]W[a5]B[h2]W[a3]B[h3]W[a7]B[e8]W[d8]B[g6]W[c2]B[b2]W[d2]B[a2]W[a1]B[d3]W[h1]B[b7]W[g7]B[c1]W[e1]B[b1]W[d1]B[h8]W[h7]B[g8]W[a8]B[b8];)");
        final OsMoveListItem mli = engine.calcMli(new NBoardState(game, 1, 0), 0);
        assertEquals(OsMoveListItem.PASS, mli);
    }
}
