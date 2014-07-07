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

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.feed.Handler;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.MutableGame;
import com.welty.othello.gdk.COsGame;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogbookMvSource implements MvSource {
    @Override public List<MeValue> getMvs() throws IOException {
        final ArrayList<MeValue> meValues = new ArrayList<>();

        Feeds.ofLines(new File("logbook.gam")).each(new Handler<String>() {
            @Override public void handle(@NotNull String s) {
                final String ggfGame = COsGame.ofLogbook(s).toString();
                final MutableGame mg = MutableGame.ofGgf(ggfGame);
                meValues.addAll(mg.calcPositionValues());
            }
        });

        return meValues;
    }
}
