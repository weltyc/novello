package com.welty.novello.core;

import com.welty.othello.gdk.OsMoveListItem;
import junit.framework.TestCase;

public class MoveScoreTest extends TestCase {
    public void testToMli() throws Exception {
        final MoveScore a7 = new MoveScore("A7", 123);
        final OsMoveListItem mli = a7.toMli(0);
        assertEquals("A7/1.23", mli.toString());
    }
}
