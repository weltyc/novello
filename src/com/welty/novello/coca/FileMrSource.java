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

package com.welty.novello.coca;

import com.welty.novello.core.MinimalReflection;
import com.welty.novello.core.ObjectFeed;
import com.welty.novello.solver.Counter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class FileMrSource implements MrSource {
    private final Path path;

    public FileMrSource(Path filePath) {
        this.path = filePath;
        if (!Files.exists(path)) {
            throw new RuntimeException("File doesn't exist: " + path);
        }
    }

    @Override public Set<MinimalReflection> getMrs() throws IOException {
        return new ObjectFeed<>(path, MinimalReflection.deserializer).asSet();
    }

    public static void main(String[] args) throws IOException {
        final Path filePath = Counter.capturePath;
        System.out.println("Stats for File " + filePath);
        final FileMrSource source = new FileMrSource(filePath);
        final Set<MinimalReflection> mrs = source.getMrs();
        System.out.format("# mrs : %,2d\n\n", mrs.size());
        final int[] counts = new int[60];
        for (MinimalReflection mr : mrs) {
            counts[mr.nEmpty()]++;
        }
        System.out.println("#empty  #mrs");
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] != 0) {
                System.out.format("%2d     %,10d\n", i, counts[i]);
            }
        }
    }
}
