package com.welty.novello.ntest;

import com.welty.novello.core.MoveScore;
import junit.framework.TestCase;

public class NBoardPlayerTest extends TestCase {
    public void testParseMoveScore()  {
        assertEquals("Ntest format", new MoveScore("D3", -51), NBoardSyncEngine.parseMoveScore("=== D3/-0.51"));
        assertEquals("Edax format", new MoveScore("D6", 700), NBoardSyncEngine.parseMoveScore("=== d6 7.00 0.0"));
    }
}
