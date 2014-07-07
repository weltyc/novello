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

package com.welty.ntestj.table;

import com.welty.ntestj.PatternUtils;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 8, 2009
 * Time: 6:25:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Configs2x5To2x4Table {
    public final static int[] configs2x5To2x4 = new int[9 * 6561];

    static {
        final int[] reordering = {0, 1, 2, 3, 5, 6, 7, 8};
        // 2x5->2x4 translator
        for (char config = 0; config < 9 * 6561; config++) {

//		ConfigToTrits(config, 10, trits);
//		int value1=TritsToConfig(trits,4);
//		int value2=TritsToConfig(trits+5, 4);
//		configs2x5To2x4[config]=value1+value2*81;
            configs2x5To2x4[config] = PatternUtils.ReorderedConfig(config, 10, reordering);
        }
    }
}
