package com.welty.novello.coca;

import com.orbanova.common.misc.Logger;
import com.welty.ggf.GgfGame;
import com.welty.ggf.GgfMatch;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.MutableGame;
import org.apache.commons.compress.compressors.CompressorException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RandGameMvSource implements MvSource {
    private static final Logger log = Logger.logger(RandGameMvSource.class);

    /**
     * Select s8rXX, non-anti, games from the file  ~/dev/novello/Othello/latest.223270.bz2
     */
    public static void main(String[] args) throws IOException, CompressorException {
        final List<MeValue> mvs = new RandGameMvSource().getMvs();
        System.out.format("%,d total positions", mvs.size());

        int nPrinted = 0;
        for (MeValue mv : mvs) {
            if (Math.abs(mv.value) > 2000 && mv.nEmpty() < 3) {
                System.out.println(mv);
                nPrinted++;
            }
            if (nPrinted > 100) {
                break;
            }
        }
    }

    @Override public List<MeValue> getMvs() throws IOException {
        final String filename = "Othello.latest.223270.bz2";
        log.info("Selecting games from " + filename);
        int nSelected = 0;
        int nSelectedSynchro = 0;
        int nSelectedKomi = 0;

        int nProcessed = 0;

        final List<MeValue> meValues = new ArrayList<>();

        try {
            for (GgfMatch match : GgfMatch.readFromFile(Paths.get(filename))) {
                final GgfGame game0 = match.getGames().get(0);

                if (game0.getBoardSize() == 8 && !game0.isAnti() && game0.blackRating > 2000 && game0.whiteRating > 2000) {
                    nSelected++;
                    if (game0.isSynchro()) {
                        nSelectedSynchro++;
                    }
                    if (game0.isKomi()) {
                        nSelectedKomi++;
                    }
                    final MutableGame mg0 = MutableGame.ofGgf(game0.toString());
                    meValues.addAll(mg0.calcPositionValues());
                    if (match.getGames().size() > 1) {
                        final GgfGame game1 = match.getGames().get(1);
                        final MutableGame mg1 = MutableGame.ofGgf(game1.toString());
                        meValues.addAll(mg1.calcPositionValues());
                    }
                }
                nProcessed++;
                if (nProcessed % 1000 == 0) {
                    System.out.print(".");
                    if (nProcessed % 50000 == 0) {
                        System.out.println(nProcessed / 1000 + "k");
                    }
                }
            }
        } catch (CompressorException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
        System.out.println("nSelected = " + nSelected + ", nSelectedSynchro = " + nSelectedSynchro + ", nKomi = " + nSelectedKomi);
        return meValues;
    }
}
