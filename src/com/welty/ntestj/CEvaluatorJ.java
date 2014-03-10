package com.welty.ntestj;

import com.orbanova.common.misc.Require;
import com.welty.c.CBinaryReader;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.eval.Eval;
import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.eval.Feature;
import com.welty.ntestj.table.*;
import gnu.trove.list.array.TShortArrayList;

import java.io.*;

import static com.welty.novello.eval.DiagonalTerm.diagonalInstance;

/**
 * Ntest evaluation function
 */
public class CEvaluatorJ extends Eval {
    public static final int potMobAdd = 0;
    public static final int potMobShift = 1;

    private static CEvaluatorJ instance = null;

    public static CEvaluatorJ getInstance() {
        if (instance == null) {
            instance = new CEvaluatorJ("coefficients/JA", 10);
        }
        return instance;
    }

    // offsetJs for coefficients
    private static final int offsetJR1 = 0, sizeJR1 = 6561,
            offsetJR2 = offsetJR1 + sizeJR1, sizeJR2 = 6561,
            offsetJR3 = offsetJR2 + sizeJR2, sizeJR3 = 6561,
            offsetJR4 = offsetJR3 + sizeJR3, sizeJR4 = 6561,
            offsetJD8 = offsetJR4 + sizeJR4, sizeJD8 = 6561,
            offsetJD7 = offsetJD8 + sizeJD8, sizeJD7 = 2187,
            offsetJD6 = offsetJD7 + sizeJD7, sizeJD6 = 729,
            offsetJD5 = offsetJD6 + sizeJD6, sizeJD5 = 243,
            offsetJTriangle = offsetJD5 + sizeJD5, sizeJTriangle = 9 * 6561,
            offsetJC4 = offsetJTriangle + sizeJTriangle, sizeJC4 = 6561,
            offsetJC5 = offsetJC4 + sizeJC4, sizeJC5 = 6561 * 9,
            offsetJEX = offsetJC5 + sizeJC5, sizeJEX = 6561 * 9,
            offsetJMP = offsetJEX + sizeJEX, sizeJMP = 64,
            offsetJMO = offsetJMP + sizeJMP, sizeJMO = 64,
            offsetJPMP = offsetJMO + sizeJMO, sizeJPMP = 64,
            offsetJPMO = offsetJPMP + sizeJPMP, sizeJPMO = 64,
            offsetJPAR = offsetJPMO + sizeJPMO, sizeJPAR = 2;

    /**
     * @return Coefficients in the array format used by Novello
     *         <p/>
     *         For each slice, the feature indices are in the order given by mapsJ.
     */
    public short[][][] getNovelloCoeffs() {
        final short[][][] novelloCoeffs = new short[60][][];
        for (int nEmpty = 0; nEmpty < 60; nEmpty++) {
            novelloCoeffs[nEmpty] = getSliceCoeffs(nEmpty);
        }
        return novelloCoeffs;
    }


    /**
     * @return slice coefficients as used by novello
     */
    private short[][] getSliceCoeffs(int nEmpty) {
        final EvalStrategy strategyJ = EvalStrategies.strategy("j");
        final int[] ntestCoeffs = pcoeffs[nEmpty][1];
        final short[][] sliceCoeffs = new short[mapsJ.length][];

        int offset = 0;
        for (int iMap = 0; iMap < mapsJ.length; iMap++) {
            final CMap map = mapsJ[iMap];
            final short[] mapOridCoeffs = new short[map.NIDs()];
            final char nNtestConfigs = map.NConfigs();
            final Feature novelloFeature = strategyJ.getFeature(iMap);
            if (novelloFeature.nOrids() != map.NIDs()) {
                throw new IllegalStateException("Different id counts for " + iMap + " : "
                        + novelloFeature + "(" + novelloFeature.nOrids() +
                        ") vs " + map + " (" + (int) map.NIDs() + ")");
            }

            for (char ntestConfig = 0; ntestConfig < nNtestConfigs; ntestConfig++) {
                final int novelloInstance = novelloInstanceFromNtestConfig(ntestConfig, iMap);
                final int novelloOrid = novelloFeature.orid(novelloInstance);


                int ntestCoeff = ntestCoeffs[offset + ntestConfig];
                // if iMap < M1J, the value is in the high 16 bits and the low 16 bits contain packed potential mobility
                // information. Strip the potential mobility information and store all values as shorts.
                final short novelloCoeff = (short) (iMap <= C4J ? (ntestCoeff >> 16) : ntestCoeff);
                mapOridCoeffs[novelloOrid] = novelloCoeff;
            }
            sliceCoeffs[iMap] = mapOridCoeffs;
            offset += map.NConfigs();
        }
        return sliceCoeffs;
    }

    /**
     * Conversion from ntest triangle trit ordering to novello triangle trit ordering
     * <p/>
     * Ntest ordering:
     * 0 4 3 2
     * 7 1 5
     * 8 6
     * 9
     * <p/>
     * Novello ordering:
     * 0 1 2 3
     * 4 5 6
     * 7 8
     * 9
     */
    static final int[] triangleReorder = {0, 4, 3, 2, 7, 1, 5, 8, 6, 9};
    static final int[] c2x5Reorder = {5, 6, 7, 8, 9, 0, 1, 2, 3, 4};

    public static int novelloInstanceFromNtestConfig(char ntestConfig, int iMap) {
        if (iMap < M1J) {
            // pattern-based map
            final CMap map = mapsJ[iMap];

            final int[] trits = new int[map.size];
            PatternUtils.ConfigToTrits(ntestConfig, map.size, trits);
            for (int i = 0; i < trits.length; i++) {
                trits[i] = (trits[i] + 2) % 3;
            }

            if (iMap == C4J || iMap == C2x5J) {
                final int[] reorder = iMap == C4J ? triangleReorder : c2x5Reorder;
                // novello and ntest have the trits in a different order.
                final int[] novelloTrits = new int[trits.length];
                for (int i = 0; i < trits.length; i++) {
                    novelloTrits[i] = trits[reorder[i]];
                }
                System.arraycopy(novelloTrits, 0, trits, 0, trits.length);
            }
            final int novelloInstance = PatternUtils.TritsToConfig(trits, map.size);
            return novelloInstance;
        } else {
            return ntestConfig;
        }
    }

    // iDebugEval prints out debugging information in the static evaluation routine.
    //	0 - none
    //	1 - final value
    //	2 - board, final value and all components
    private static final int iDebugEval = 0;

    static final int R1J = 0;
    static final int R2J = 1;
    static final int R3J = 2;
    static final int R4J = 3;
    static final int D8J = 4;
    static final int D7J = 5;
    static final int D6J = 6;
    static final int D5J = 7;
    public static final int C4J = 8;
    public static final int C2x4J = 9;
    public static final int C2x5J = 10;
    public static final int CR1XXJ = 11;
    static final int M1J = 12;
    static final int M2J = 13;
    static final int PM1J = 14;
    static final int PM2J = 15;
    static final int PARJ = 16;    // map numbers

    public static final int[] patternToMapJ = {
            // rows & cols
            R1J, R1J, R1J, R1J,
            R2J, R2J, R2J, R2J,
            R3J, R3J, R3J, R3J,
            R4J, R4J, R4J, R4J,
            // diagonals & corners
            D8J, D8J,
            D7J, D7J, D7J, D7J,
            D6J, D6J, D6J, D6J,
            D5J, D5J, D5J, D5J,
            C4J, C4J, C4J, C4J,

            // 2x4, 2x5, edge+2X
            C2x4J, C2x4J, C2x4J, C2x4J, C2x4J, C2x4J, C2x4J, C2x4J,
            C2x5J, C2x5J, C2x5J, C2x5J, C2x5J, C2x5J, C2x5J, C2x5J,
            CR1XXJ, CR1XXJ, CR1XXJ, CR1XXJ,

            // mobility and parity
            M1J, M2J,
            PM1J, PM2J,
            PARJ
    };    // tells which patterns are valued the same.

    // pattern J descriptions
    public static final CMap[] mapsJ = {
            new CMap(CMap.TIdType.kORID, 8), new CMap(CMap.TIdType.kORID, 8), new CMap(CMap.TIdType.kORID, 8), new CMap(CMap.TIdType.kORID, 8), // rows & cols
            new CMap(CMap.TIdType.kORID, 8), new CMap(CMap.TIdType.kORID, 7), new CMap(CMap.TIdType.kORID, 6), new CMap(CMap.TIdType.kORID, 5), new CMap(CMap.TIdType.kCRID, 10), // diags
            new CMap(CMap.TIdType.kBase3, 8), new CMap(CMap.TIdType.kBase3, 10), new CMap(CMap.TIdType.kORID, 10),        // corner patterns: 2x4, 2x5, edge+2X
            new CMap(CMap.TIdType.kNumber, 64), new CMap(CMap.TIdType.kNumber, 64),                // mobility
            new CMap(CMap.TIdType.kNumber, 64), new CMap(CMap.TIdType.kNumber, 64),                // pot. mobility
            new CMap(CMap.TIdType.kNumber, 2)                                //  parity
    };

    public static final int nMapsJ = mapsJ.length;

    /**
     * pcoeffs[nEmpty][black?1:0] is the coefficient array for the particular nEmpty and color
     */
    public int[][][] pcoeffs = new int[60][2][];

    // pattern J info
    static int[] coeffStartsJ = new int[nMapsJ];
    static int nCoeffsJ;

    static {
        InitJCoeffStarts();
    }

    static void InitJCoeffStarts() {
        int map;

        // calculate nTotalCoeffs and coeffStarts
        for (map = 0, nCoeffsJ = 0; map < nMapsJ; map++) {
            coeffStartsJ[map] = nCoeffsJ;
            nCoeffsJ += mapsJ[map].NConfigs();
        }
    }

    /**
     * Read in evaluator coefficients from an evaluator file
     * <p/>
     * If the file's fParams is 14, coefficients are stored as floats and are in units of stones
     * If the file's fParams is 100, coefficients are stored as chars and are in units of centistones.
     *
     * @throws IllegalArgumentException if error
     */
    CEvaluatorJ(final String fnBase, int nFiles) {
        int map, coeffStart, mover, packedCoeff;
        int nIDs, nConfigs, id, wconfig, cid, wcid;
        int configpm1, configpm2;
        char mapsize;
        short[] shortCoeffs;        //!< for use with converted (packed) coeffs
        int coeff;
        int iSubset, nSubsets, nEmpty;

        // some parameters are set based on the evaluator version number
        final int nSetWidth = 60 / nFiles;
        final char cCoeffSet = fnBase.charAt(fnBase.length() - 1);


        // read in sets
        int nSets = 0;
        for (int iFile = 0; iFile < nFiles; iFile++) {
            final String fn = fnBase + (char) ('a' + (iFile % nFiles)) + ".cof";

            // open file
            final InputStream stream = getClass().getResourceAsStream(fn);
            if (stream == null) {
                throw new IllegalStateException("Input resource " + fn + " can't be found");
            }
            CBinaryReader fp = new CBinaryReader(new BufferedInputStream(stream));

            // read in version and parameter info
            final int iVersion = fp.readInt();
            int fParams = fp.readInt();

            if (iVersion == 1 && fParams == 14) {
                fp = ConvertFile(fp, fn, iVersion);
                fParams = 100;
            }
            if (iVersion != 1 || (fParams != 100)) {
                throw new IllegalArgumentException("error reading from coefficients file " + fnBase);
            }

            // calculate the number of subsets
            nSubsets = 2;

            for (iSubset = 0; iSubset < nSubsets; iSubset++) {
                // allocate memory for the black and white versions of the coefficients
                int[][][] coeffs = new int[60][2][];
                coeffs[nSets][0] = new int[nCoeffsJ];
                coeffs[nSets][1] = new int[nCoeffsJ];

                // put the coefficients in the proper place
                for (map = 0; map < nMapsJ; map++) {

                    // initial calculations
                    nIDs = mapsJ[map].NIDs();
                    nConfigs = mapsJ[map].NConfigs();
                    mapsize = mapsJ[map].size;
                    coeffStart = coeffStartsJ[map];

                    // get raw coefficients from file
                    shortCoeffs = new short[nIDs];
                    for (int i = 0; i < nIDs; i++) {
                        shortCoeffs[i] = fp.readShort();
                    }

                    // convert raw coefficients[id] to shorts[config]
                    for (char config = 0; config < nConfigs; config++) {
                        id = mapsJ[map].ConfigToID(config);

                        coeff = shortCoeffs[id];

                        cid = config + coeffStart;

                        if (map == PARJ) {
                            // odd-even correction, only in Parity coefficient.
                            if (cCoeffSet >= 'A') {
                                if (iFile >= 7)
                                    coeff += (int) (Utils.kStoneValue * .65);
                                else if (iFile == 6)
                                    coeff += (int) (Utils.kStoneValue * .33);
                            }
                        }

                        // coeff value has first 2 bytes=coeff, 3rd byte=potmob1, 4th byte=potmob2
                        if (map < M1J) {    // pattern maps
                            // restrict the coefficient to 2 bytes
                            if (coeff > 0x3FFF)
                                packedCoeff = 0x3FFF;
                            if (coeff < -0x3FFF)
                                packedCoeff = -0x3FFF;
                            else
                                packedCoeff = coeff;

                            // get linear pot mob info
                            if (map <= D5J) {    // straight-line maps
                                // pack coefficient and pot mob together
                                configpm1 = ConfigToPotMobTable.configToPotMob[0][mapsize][config];
                                configpm2 = ConfigToPotMobTable.configToPotMob[1][mapsize][config];
                                packedCoeff = (packedCoeff << 16) | (configpm1 << 8) | (configpm2);
                            } else if (map == C4J) {    // corner triangle maps
                                // pack coefficient and pot mob together
                                configpm1 = ConfigToPotMobTable.configToPotMobTriangle[0][config];
                                configpm2 = ConfigToPotMobTable.configToPotMobTriangle[1][config];
                                packedCoeff = (packedCoeff << 16) | (configpm1 << 8) | (configpm2);
                            }

                            // calculate white config
                            wconfig = nConfigs - 1 - config;
                            wcid = wconfig + coeffStart;
                            coeffs[nSets][0][wcid] = packedCoeff;
                            coeffs[nSets][1][cid] = packedCoeff;
                        } else {        // non-pattern maps
                            coeffs[nSets][0][cid] = coeff;
                            coeffs[nSets][1][cid] = coeff;
                        }
                    }
                }

                // fold 2x4 corner coefficients into 2x5 corner coefficients
                // so we don't ever need to look up the 2x4 coefficients.
                for (mover = 0; mover < 2; mover++) {
                    final int[] coeffTable = coeffs[nSets][mover];
                    int i2x4 = coeffStartsJ[C2x4J];
                    int i2x5 = coeffStartsJ[C2x5J];

                    // fold coefficients in
                    for (int config = 0; config < 9 * 6561; config++) {
                        final int c2x4 = Configs2x5To2x4Table.configs2x5To2x4[config];
                        coeffTable[i2x5 + config] += coeffTable[i2x4 + c2x4];
                    }
                    // zero out 2x4 coefficients
                    for (int config = 0; config < 6561; config++) {
                        coeffTable[i2x4 + config] = 0;
                    }
                }

                // Set the pcoeffs array and the fParameters
                // todo fix this in the C code, lower bound was >= 50-nSetWidth*iFile
                final int upperBound = 59 - nSetWidth * iFile;
                final int lowerBound = upperBound - nSetWidth;
                for (nEmpty = upperBound; nEmpty > lowerBound; nEmpty--) {
                    // if this is a set of the wrong parity, do nothing
                    if ((nEmpty & 1) == iSubset)
                        continue;
                    for (mover = 0; mover < 2; mover++) {
                        Require.geq(nEmpty, "nEmpty", 0);
                        Require.geq(nSets, "nSets", 0);
                        pcoeffs[nEmpty][mover] = coeffs[nSets][mover];
                    }
                }

                nSets++;
            }
            fp.close();
        }
    }

    /**
     * Convert the file to short format
     * <p/>
     * read in the file (float format) and write it out in short format.
     * reopen the file and read the iVersion and fParams flags.
     * <p/>
     * Port note: the caller will need to set its iVersion and fParams flags, unlike in the C version.
     *
     * @param fp
     * @param fn
     * @return converted data input stream, pointing to the start of the coefficients (just after the version and params have been read)
     */
    static CBinaryReader ConvertFile(CBinaryReader fp, String fn, int iVersion) {
        try {
            // convert float coefficient file to int. Write new coefficients file to disk and reload.
            TShortArrayList newCoeffs = new TShortArrayList();
            float oldCoeff;
            while (fp.available() != 0) {
                oldCoeff = fp.readFloat();
                int coeff = (int) (oldCoeff * Utils.kStoneValue);
                if (coeff > 0x3FFF)
                    coeff = 0x3FFF;
                if (coeff < -0x3FFF)
                    coeff = -0x3FFF;
                newCoeffs.add((short) coeff);
            }
            fp.close();
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fn)));
            out.writeInt(iVersion);
            final int fParams = 100;
            out.writeInt(fParams);
            for (short i : newCoeffs.toArray()) {
                out.writeShort(i);
            }
            out.close();
            fp = new CBinaryReader(new BufferedInputStream(new FileInputStream(fn)));
            Require.eq(fp.readInt(), "version in new file", iVersion);
            Require.eq(fp.readInt(), "params in new file", fParams);
            return fp;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    int ValueJMobs(long mover, long enemy, long moverMoves, long enemyMoves) {
        final int nEmpty_ = BitBoardUtils.nEmpty(mover, enemy);
        final int[][] sliceCoeffs = pcoeffs[nEmpty_];


        int value;
        int[] pcmove = sliceCoeffs[1];  // ntestJ always has black to move

        value = 0;

        if (iDebugEval > 1) {
            System.out.println("----------------------------");
            System.out.println(new Board(mover, enemy, true));
            System.out.format("mover = 0x%x, enemy = 0x%x\n", mover, enemy);
        }


        final long empty = ~(mover | enemy);

        final int configs0 = BitBoardUtils.rowInstance(empty, mover, 0);
        final int configs1 = BitBoardUtils.rowInstance(empty, mover, 1 * 8);
        final int configs2 = BitBoardUtils.rowInstance(empty, mover, 2 * 8);
        final int configs3 = BitBoardUtils.rowInstance(empty, mover, 3 * 8);
        final int configs4 = BitBoardUtils.rowInstance(empty, mover, 4 * 8);
        final int configs5 = BitBoardUtils.rowInstance(empty, mover, 5 * 8);
        final int configs6 = BitBoardUtils.rowInstance(empty, mover, 6 * 8);
        final int configs7 = BitBoardUtils.rowInstance(empty, mover, 7 * 8);

        final int configs19 = BitBoardUtils.colInstance(empty, mover, 0);
        final int configs20 = BitBoardUtils.colInstance(empty, mover, 1);
        final int configs21 = BitBoardUtils.colInstance(empty, mover, 2);
        final int configs22 = BitBoardUtils.colInstance(empty, mover, 3);
        final int configs23 = BitBoardUtils.colInstance(empty, mover, 4);
        final int configs24 = BitBoardUtils.colInstance(empty, mover, 5);
        final int configs25 = BitBoardUtils.colInstance(empty, mover, 6);
        final int configs26 = BitBoardUtils.colInstance(empty, mover, 7);

        final int configs13 = diagonalInstance(empty, mover, 0x8040201008040201L, 56);
        final int configs32 = diagonalInstance(empty, mover, 0x0102040810204080L, 56);

        final int configs12 = diagonalInstance(empty, mover, 0x4020100804020100L, 56);
        final int configs14 = diagonalInstance(empty, mover, 0x0080402010080402L, 57);
        final int configs31 = diagonalInstance(empty, mover, 0x0001020408102040L, 56);
        final int configs33 = diagonalInstance(empty, mover, 0x0204081020408000L, 57);

        final int configs11 = diagonalInstance(empty, mover, 0x2010080402010000L, 56);
        final int configs15 = diagonalInstance(empty, mover, 0x0000804020100804L, 58);
        final int configs30 = diagonalInstance(empty, mover, 0x0000010204081020L, 56);
        final int configs34 = diagonalInstance(empty, mover, 0x0408102040800000L, 58);

        final int configs10 = diagonalInstance(empty, mover, 0x1008040201000000L, 56);
        final int configs16 = diagonalInstance(empty, mover, 0x0000008040201008L, 59);
        final int configs29 = diagonalInstance(empty, mover, 0x0000000102040810L, 56);
        final int configs35 = diagonalInstance(empty, mover, 0x0810204080000000L, 59);

        value += pcmove[offsetJR1 + configs0] + pcmove[offsetJR1 + configs7] + pcmove[offsetJR1 + configs19] + pcmove[offsetJR1 + configs26];
        value += pcmove[offsetJR2 + configs1] + pcmove[offsetJR2 + configs6] + pcmove[offsetJR2 + configs20] + pcmove[offsetJR2 + configs25];
        value += pcmove[offsetJR3 + configs2] + pcmove[offsetJR3 + configs5] + pcmove[offsetJR3 + configs21] + pcmove[offsetJR3 + configs24];
        value += pcmove[offsetJR4 + configs3] + pcmove[offsetJR4 + configs4] + pcmove[offsetJR4 + configs22] + pcmove[offsetJR4 + configs23];

        if (iDebugEval > 1)
            System.out.format("Rows & cols done. Value so far: %d. PotMobs %d/%d\n", value >> 16, (value >>> 8) & 0xFF, (value & 0xFF));

        value += pcmove[offsetJD5 + configs10] + pcmove[offsetJD5 + configs16] + pcmove[offsetJD5 + configs29] + pcmove[offsetJD5 + configs35];

        if (iDebugEval > 1) {
            System.out.println(" Diagonal 5 configs : " + configs10 + ", " + configs16 + ", " + configs29 + ", " + configs35);
            System.out.format("Diagonal 5 done. Value so far: %d. PotMobs %d/%d\n", value >> 16, (value >>> 8) & 0xFF, (value & 0xFF));
        }

        value += pcmove[offsetJD6 + configs11] + pcmove[offsetJD6 + configs15] + pcmove[offsetJD6 + configs30] + pcmove[offsetJD6 + configs34];
        if (iDebugEval > 1) {
            System.out.format("Diagonal 6 done. Value so far: %d. PotMobs %d/%d\n", value >> 16, (value >>> 8) & 0xFF, (value & 0xFF));
        }
        value += pcmove[offsetJD7 + configs12] + pcmove[offsetJD7 + configs14] + pcmove[offsetJD7 + configs31] + pcmove[offsetJD7 + configs33];
        value += pcmove[offsetJD8 + configs13] + pcmove[offsetJD8 + configs32];

        if (iDebugEval > 1)
            System.out.format("Straight lines done. Value so far: %d. PotMobs %d/%d\n", value >> 16, (value >>> 8) & 0xFF, (value & 0xFF));

        // Triangle patterns in corners
        value += valueTrianglePatternsJFromConfigs(pcmove, configs0, configs1, configs2, configs3);
        value += valueTrianglePatternsJFromConfigs(pcmove, configs7, configs6, configs5, configs4);

        if (iDebugEval > 1)
            System.out.format("Triangles done. Value so far: %d.\n", value >> 16);

        // Take apart packed information about pot mobilities
        int nPMO = (value >> 8) & 0xFF;
        int nPMP = value & 0xFF;
        if (iDebugEval > 1)
            System.out.format("Raw pot mobs: %d, %d\n", nPMO, nPMP);
        nPMO = (nPMO + potMobAdd) >> potMobShift;
        nPMP = (nPMP + potMobAdd) >> potMobShift;
        value >>= 16;

        // pot mobility
        value += ConfigValue(pcmove, nPMP, PM1J, offsetJPMP);
        value += ConfigValue(pcmove, nPMO, PM2J, offsetJPMO);

        if (iDebugEval > 1)
            System.out.format("Potential mobility done. Value so far: %d.\n", value);

        // 2x4, 2x5, edge+2X patterns
        value += valueEdgePatternsJFromConfigs(pcmove, configs0, configs1);
        value += valueEdgePatternsJFromConfigs(pcmove, configs7, configs6);
        value += valueEdgePatternsJFromConfigs(pcmove, configs19, configs20);
        value += valueEdgePatternsJFromConfigs(pcmove, configs26, configs25);

        if (iDebugEval > 1)
            System.out.format("Corners done. Value so far: %d.\n", value);

        // mobility
        final int nMovesPlayer = Long.bitCount(moverMoves);
        value += ConfigValue(pcmove, nMovesPlayer, M1J, offsetJMP);
        final int nMovesOpponent = Long.bitCount(enemyMoves);
        value += ConfigValue(pcmove, nMovesOpponent, M2J, offsetJMO);

        if (iDebugEval > 1)
            System.out.format("Mobility done. Value so far: %d.\n", value);

        // parity
        value += ConfigValue(pcmove, nEmpty_ & 1, PARJ, offsetJPAR);

        if (iDebugEval > 0)
            System.out.format("Total Value= %d\n", value);

        return value;
    }

    /**
     * value all the edge patterns.
     *
     * @return the sum of the values.
     */
    static int valueEdgePatternsJFromConfigs(int[] pcmove, int config1, int config2) {
        final int configs2x5 = RowTo2x5Table.getConfigs(config1, config2);
        final int configXX = RowToXXTable.getConfig(config1, config2);
        int value = 0;
        value += ConfigDisplayValue(pcmove, configs2x5 & 0xFFFF, C2x5J, offsetJC5);
        value += ConfigDisplayValue(pcmove, configs2x5 >>> 16, C2x5J, offsetJC5);
        value += ConfigValue(pcmove, configXX, CR1XXJ, offsetJEX);

        // in J-configs, the values are multiplied by 65536
        //_ASSERT((value&0xFFFF)==0);
        return value;
    }

    /**
     * value all the triangle patterns.
     *
     * @return the sum of the values.
     */

    static int valueTrianglePatternsJFromConfigs(int[] pcmove, int config1, int config2, int config3, int config4) {
        final int configsTriangle = RowToTriangleTable.getConfigs(config1, config2, config3, config4);
        int value = 0;
        value += ConfigPMValue(pcmove, configsTriangle & 0xFFFF, C4J, offsetJTriangle);
        value += ConfigPMValue(pcmove, configsTriangle >>> 16, C4J, offsetJTriangle);

        return value;
    }

    static int ConfigValue(final int[] pcmove, int config, int map, int offset) {
        int value = pcmove[config + offset];
        if (iDebugEval > 1)
            System.out.format("Config: %5d, Id: %5d, Value: %4d\n", (int) (char) config, (int) mapsJ[map].ConfigToID((char) config), value);
        return value;
    }

    static int ConfigDisplayValue(final int[] pcmove, int config, int map, int offset) {
        int value = pcmove[config + offset];
        if (iDebugEval > 1) {
            final String patternString = PatternUtils.PrintBase3((char) config, (char) 10);
            final int id = mapsJ[map].ConfigToID((char) config);
            System.out.format("Config: %5d, (%s), Id: %5d, Value: %4d\n", config, patternString, id, value);
        }
        return value;
    }

    static int ConfigPMValue(final int[] pcmove, int config, int map, int offset) {
        int value = pcmove[config + offset];
        if (iDebugEval > 1)
            System.out.format("Config: %5d, Id: %5d, Value: %4d (pms %2d, %2d)\n",
                    (int) (char) config, (int) mapsJ[map].ConfigToID((char) config), value >> 16, (value >> 8) & 0xFF, value & 0xFF);
        return value;
    }

    @Override public int eval(long mover, long enemy) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        return ValueJMobs(mover, enemy, moverMoves, enemyMoves);
    }

    @Override public String toString() {
        return "ntestJ";
    }
}
