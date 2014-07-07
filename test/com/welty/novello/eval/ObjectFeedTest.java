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

package com.welty.novello.eval;

import com.orbanova.common.ramfs.RamFileSystem;
import com.welty.novello.core.ObjectFeed;
import junit.framework.TestCase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ObjectFeedTest extends TestCase {
    private static final ObjectFeed.Deserializer<String> utfDeserializer = new ObjectFeed.Deserializer<String>() {
        @Override public String read(DataInputStream in) throws IOException {
            return in.readUTF();
        }
    };

    public void testSimple() throws IOException {
        final RamFileSystem fs = new RamFileSystem();
        final Path path = fs.getPath("test");
        final DataOutputStream out = new DataOutputStream(Files.newOutputStream(path));
        out.writeUTF("Foo");
        out.writeUTF("Bar");
        out.close();

        final ObjectFeed<String> feed = new ObjectFeed<>(path, utfDeserializer);
        final List<String> expected = Arrays.asList("Foo", "Bar");
        assertEquals(expected, feed.asList());
        assertEquals("assert reader was closed", 0, (int)Files.getAttribute(path, "ramfs:readers"));
    }
}
