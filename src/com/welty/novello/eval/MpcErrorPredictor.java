package com.welty.novello.eval;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MpcErrorPredictor {
    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("coefficients/c/1s/mpc.txt");
        final ArrayList<int[]>[] sliceData = Mpc.readSliceData(path);

        System.out.println("shallow,delta,me,mse");
        for (int shallow = 0; shallow < 8; shallow++) {
            for (int deep = shallow + 2; deep <= 11; deep+=2) {
                double cutSse = 0;
                double cutSe = 0;
                double cuts = 0;

                for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
                    if (sliceData[nEmpty].isEmpty()) {
                        continue;
                    }
                    if (deep <= nEmpty) {
                        final Mpc.Cutter cutter = new Mpc.Cutter(sliceData[nEmpty], deep, shallow);

                        final double pred = (80-nEmpty)/14.;
                        final double err = cutter.getSd()/pred - 1;
                        cuts++;
                        cutSe += err;
                        cutSse += err * err;
//                        System.out.println(nEmpty + "," + shallow + "," + (deep - shallow) + "," + cutter.getSd());
                    }
                }
                System.out.format("%2d,%2d,%5.2f,%5.2f\n", shallow, deep - shallow, cutSe/cuts, cutSse/cuts);
            }
        }
    }
}
