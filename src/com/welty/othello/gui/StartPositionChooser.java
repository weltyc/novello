package com.welty.othello.gui;

import com.orbanova.common.feed.Feeds;
import com.welty.novello.core.Position;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class StartPositionChooser {
    private static final XotChooser xotChooser = new XotChooser();

    public static Position next(String startPositionType) {
        final Position startPosition;
        switch (startPositionType) {
            case "Standard":
                startPosition = Position.START_POSITION;
                break;
            case "Alternate":
                startPosition = Position.ALTERNATE_START_POSITION;
                break;
            case "XOT":
                startPosition = xotChooser.next();
                break;
            case "F5":
                startPosition = Position.START_POSITION.play("F5");
                break;
            default:
                throw new RuntimeException("Unknown start position type : " + startPositionType);
        }
        return startPosition;
    }

    private static class XotChooser {
        private final List<String> xots;
        private int lastIndex = 0;

        private XotChooser() {
            final InputStream in = XotChooser.class.getResourceAsStream("xot-large.txt");
            xots = Feeds.ofLines(in).asList();
            Collections.shuffle(xots);
        }

        public synchronized Position next() {
            lastIndex++;
            if (lastIndex >= xots.size()) {
                lastIndex = 0;
            }

            return Position.START_POSITION.playLine(xots.get(lastIndex));
        }
    }
}
