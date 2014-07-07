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

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 3, 2009
 * Time: 10:56:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class Base3ToBase2TableTest extends TestCase {
    public void testConversion() {
        assertEquals(0, Base3ToBase2Table.base3ToBase2(0));
        assertEquals(1, Base3ToBase2Table.base3ToBase2(1));
    }
}
