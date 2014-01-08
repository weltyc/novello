package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;
import junit.framework.TestCase;

public class Corner2x5TermTest extends TestCase {
    public void testLeftInstance() {
        assertEquals(0, Corner2x5Term.leftInstance(0,0,0,0));
        assertEquals(3 + 243*3, Corner2x5Term.leftInstance(0x80,0x80,0x80,0x80));
        assertEquals(7 + 243*9*7, Corner2x5Term.leftInstance(0x80,0x40,0x20,0x10));
    }

    public void testInstance0() {
        final Corner2x5Term term = new Corner2x5Term(0); // 0 = left, row 0&1
        final long mover = 0b00010010_00110100;
        final long enemy = 0b01000101_10001000;
        final int moverPattern = 0b01000_01100;
        final int enemyPattern = 0b00010_10001;
        final int expected = Base3.base2ToBase3(moverPattern, enemyPattern);
        assertEquals(expected, term.instance(mover, enemy, 0, 0));
    }

    public void testInstance1() {
        final Corner2x5Term term = new Corner2x5Term(1); // 1 = right, row 0&1
        final long mover = 0b00010010_00110100;
        final long enemy = 0b01000101_10001000;
        final int moverPattern = 0b10010_10100;
        final int enemyPattern = 0b00101_01000;
        final int expected = Base3.base2ToBase3(moverPattern, enemyPattern);
        assertEquals(expected, term.instance(mover, enemy, 0, 0));
    }

    public void testInstance2() {
        final Corner2x5Term term = new Corner2x5Term(2); // 2 = left, row 7&6
        final long mover = 0b00010010_00110100L<<48;
        final long enemy = 0b01000101_10001000L<<48;
        final int moverPattern = 0b01100_01000;
        final int enemyPattern = 0b10001_00010;
        final int expected = Base3.base2ToBase3(moverPattern, enemyPattern);
        assertEquals(expected, term.instance(mover, enemy, 0, 0));
    }

    public void testInstance3() {
        final Corner2x5Term term = new Corner2x5Term(3); // 3 = right, row 7&6
        final long mover = 0b00010010_00110100L<<48;
        final long enemy = 0b01000101_10001000L<<48;
        final int moverPattern = 0b10100_10010;
        final int enemyPattern = 0b01000_00101;
        final int expected = Base3.base2ToBase3(moverPattern, enemyPattern);
        assertEquals(expected, term.instance(mover, enemy, 0, 0));
    }

    public void testInstance4And5() {
        final long mover = BitBoardUtils.reflection(0b00010010_00110100, 7);
        final long enemy = BitBoardUtils.reflection(0b01000101_10001000, 7);

        final Corner2x5Term leftTerm = new Corner2x5Term(4); // 4 = left, col 0&1
        final int expectedLeft = Base3.base2ToBase3(0b01000_01100, 0b00010_10001);
        assertEquals(expectedLeft, leftTerm.instance(mover, enemy, 0, 0));

        final Corner2x5Term rightTerm = new Corner2x5Term(5); // 5 = right, col 0&1
        final int expectedRight = Base3.base2ToBase3(0b10010_10100, 0b00101_01000);
        assertEquals(expectedRight, rightTerm.instance(mover, enemy, 0, 0));
    }

    public void testInstance6() {
        final Corner2x5Term term = new Corner2x5Term(6); // 6 = left, col 7&6
        final long moverR = 0b00010010_00110100;
        final long enemyR = 0b01000101_10001000;
        final long mover = BitBoardUtils.reflection(moverR, 5);
        final long enemy = BitBoardUtils.reflection(enemyR, 5);
        final int moverPattern = 0b01000_01100;
        final int enemyPattern = 0b00010_10001;
        final int expected = Base3.base2ToBase3(moverPattern, enemyPattern);
        assertEquals(expected, term.instance(mover, enemy, 0, 0));
    }
}
