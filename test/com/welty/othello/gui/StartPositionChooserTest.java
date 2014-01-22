package com.welty.othello.gui;

import com.welty.novello.core.Position;
import junit.framework.TestCase;

public class StartPositionChooserTest extends TestCase {
    public void testAlternate() {
        assertEquals(Position.ALTERNATE_START_POSITION, StartPositionChooser.next("Alternate"));
    }

    public void testXot() throws Exception {
        final Position xot1 = StartPositionChooser.next("XOT");
        assertEquals(52, xot1.nEmpty());
        final Position xot2 = StartPositionChooser.next("XOT");
        assertFalse(xot1.equals(xot2));
    }
}
