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
