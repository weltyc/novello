package com.welty.ntestj;

/**
 * Ntest's endgame search depth calculation
 */
public class Heights {
    /**
     * Heights at which NTest would do a WLD probable solve
     */
    final int[] hWLD = new int[6];

    /**
     * Percent solved for each hWLD; 100% = exact solution
     */
    private static final int[] nSolvePct = {50, 60, 70, 91, 99, 100};
    private final int hMidgame;

    public Heights(int hMidgame) {
        this.hMidgame = hMidgame;
        int i;
        // cIncrease = ln(time to depth D+1/time to depth D)
        final double[] cIncrease = {0.738, 0.601, 0.549, 0.495, 0.438, 0.400};
        final double cIncreaseMidgame = 0.500;

        // times are roughly equal at depth 8
        for (i = 0; i <= 5; i++)
            hWLD[i] = (int) (15 + (hMidgame - 6) * cIncreaseMidgame / cIncrease[i]);

        // increase solve height since we won't have to calculate a deviation
        // check that heights are still in order
        hWLD[0]++;
        for (i = 1; i <= 5; i++) {
            if (hWLD[i] < hWLD[0])
                hWLD[i] = hWLD[0];
        }

        // above ~26 empties, time to reach probable solve levels 5 and 4 are dominated
        //	by the midgame search, so they take about the same amount of time. Therefore
        //	set heights equal
        if (hWLD[5] > 26) {
            if (hWLD[4] >= 26)
                hWLD[5] = hWLD[4];
            else
                hWLD[5] = 26;
        }
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("---- Heights ----\n");
        sb.append("midgame : ").append(hMidgame).append("\n");
        for (int i = 0; i <= 5; i++)
            sb.append(String.format("%3d %% WLD: %d\n", nSolvePct[i], hWLD[i]));
        sb.append("novello depths: full solve at <= ")
                .append(getFullWidthHeight())
                .append(", probable solve at <= ")
                .append(getProbableSolveHeight())
                .append("\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * @return greatest nEmpty at which the search should do a full-width 100% search
     */
    public int getFullWidthHeight() {
        return hWLD[5] - 4;
    }

    /**
     * @return greatest nEmpty at which the search should do a probable WLD search
     */
    public int getProbableSolveHeight() {
        return hWLD[1];
    }
}
