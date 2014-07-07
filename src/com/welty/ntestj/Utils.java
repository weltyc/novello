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

package com.welty.ntestj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 1, 2009
 * Time: 9:34:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static final int N = 8;
    public static final int NN = N * N;

//////////////////////////////////////
// Constants
//////////////////////////////////////

    public final static short kStoneValue = 100;
    public final static short kWipeout = 64 * kStoneValue;
    public final static short kMaxHeuristic = kWipeout - 1;
    public final static short kMaxBonus = 10000;
    public final static short kInfinity = kWipeout + kMaxBonus;

    public final static byte A1 = 000;
    public final static byte B1 = 001;
    public final static byte C1 = 002;
    public final static byte D1 = 003;
    public final static byte E1 = 004;
    public final static byte F1 = 005;
    public final static byte G1 = 006;
    public final static byte H1 = 007;
    public final static byte A2 = 010;
    public final static byte B2 = 011;
    public final static byte C2 = 012;
    public final static byte D2 = 013;
    public final static byte E2 = 014;
    public final static byte F2 = 015;
    public final static byte G2 = 016;
    public final static byte H2 = 017;
    public final static byte A3 = 020;
    public final static byte B3 = 021;
    public final static byte C3 = 022;
    public final static byte D3 = 023;
    public final static byte E3 = 024;
    public final static byte F3 = 025;
    public final static byte G3 = 026;
    public final static byte H3 = 027;
    public final static byte A4 = 030;
    public final static byte B4 = 031;
    public final static byte C4 = 032;
    public final static byte D4 = 033;
    public final static byte E4 = 034;
    public final static byte F4 = 035;
    public final static byte G4 = 036;
    public final static byte H4 = 037;
    public final static byte A5 = 040;
    public final static byte B5 = 041;
    public final static byte C5 = 042;
    public final static byte D5 = 043;
    public final static byte E5 = 044;
    public final static byte F5 = 045;
    public final static byte G5 = 046;
    public final static byte H5 = 047;
    public final static byte A6 = 050;
    public final static byte B6 = 051;
    public final static byte C6 = 052;
    public final static byte D6 = 053;
    public final static byte E6 = 054;
    public final static byte F6 = 055;
    public final static byte G6 = 056;
    public final static byte H6 = 057;
    public final static byte A7 = 060;
    public final static byte B7 = 061;
    public final static byte C7 = 062;
    public final static byte D7 = 063;
    public final static byte E7 = 064;
    public final static byte F7 = 065;
    public final static byte G7 = 066;
    public final static byte H7 = 067;
    public final static byte A8 = 070;
    public final static byte B8 = 071;
    public final static byte C8 = 072;
    public final static byte D8 = 073;
    public final static byte E8 = 074;
    public final static byte F8 = 075;
    public final static byte G8 = 076;
    public final static byte H8 = 077;

///////////////////////////////////////////
// Coefficient type
///////////////////////////////////////////

// typedef i4 TCoeff;


    // square values
    public final static int WHITE = 0;
    public final static int EMPTY = 1;
    public final static int BLACK = 2;

    /**
     * convert from square number to row and column
     */
    public static int Row(int square) {
        if (N == 8)
            return square >> 3;
        else
            return square / N;
    }

    public static int Col(int square) {
        if (N == 8)
            return square & 7;
        else
            return square % N;
    }

    public static char RowText(int square) {
        return (char) (Row(square) + '1');
    }

    public static char ColText(int square) {
        return (char) (Col(square) + 'A');
    }

    public static int Square(int row, int col) {
        if (N == 8)
            return (row << 3) + col;
        else
            return row * N + col;
    }

    static boolean isSet(long bits, int index) {
        return ((bits >>> index) & 1) != 0;
    }

    private static final DecimalFormat df2 = new DecimalFormat("0.00");

    /**
     * @return the string representation of a value, nicely formatted.
     */
    static String StringFromValue(int value) {
        return df2.format(value / (double) kStoneValue);
    }

//! Return the string representation of a value pair, nicely formatted.
//!

    //! If the two values are equal we just output one of them, otherwise we output both separated by commas.

    static String StringFromValuePair(int value1, int value2) {
        if (value1 == value2) {
            return StringFromValue(value1);
        } else {
            return StringFromValue(value1) + "," + StringFromValue(value2);
        }
    }

    /**
     * print a prompt, and repeatedly get lines until a character in answers is pressed on the keyboard
     *
     * @param prompt      prompt which is displayed to the user
     * @param answers     list of allowed responses, must be upper case characters only.
     * @param repeatAfter the prompt is redisplayed every repeatAfter lines of input, but if repeatAfter==0 then it is never redisplayed
     * @param fp          print the prompt to this file (usually STDERR or STDOUT).
     * @return the chosen character
     */
    static char GetAnswer(String prompt, String answers, int repeatAfter, PrintStream fp) {
        int ntimes;

        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader((System.in)));
            while (true) {
                fp.println(prompt);
                for (ntimes = 0; (ntimes < repeatAfter) || repeatAfter == 0; ntimes++) {
                    String input = in.readLine().toUpperCase();
                    for (char c : input.toCharArray()) {
                        if (0 <= answers.indexOf(c)) {
                            return c;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static char ValueToText(int value) {
        switch (value) {
            case WHITE:
                return 'O';
            case EMPTY:
                return '-';
            case BLACK:
                return '*';
            default:
                throw new IllegalArgumentException("unknown square value : " + value);
        }
    }

    public static int TextToValue(char c) {
        switch (Character.toUpperCase(c)) {
            case 'O':
            case '0':
            case 'W':
                return WHITE;
            case '*':
            case 'X':
            case '+':
            case 'B':
                return BLACK;
            case '.':
            case '_':
            case '-':
                return EMPTY;
            default:
                throw new IllegalArgumentException("unknown character : " + c);
        }
    }

}
