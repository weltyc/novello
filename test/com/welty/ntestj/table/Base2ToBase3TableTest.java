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

import static com.welty.ntestj.table.Base2ToBase3Table.base2ToBase3;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 3, 2009
 * Time: 10:38:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class Base2ToBase3TableTest extends TestCase {
    public void testBase2ToBase3() {
        assertEquals(0, base2ToBase3((short) 0));
        assertEquals(1, base2ToBase3((short) 1));
        assertEquals(3, base2ToBase3((short) 2));

        assertEquals(0, base2ToBase3(0, 0));
        assertEquals(1, base2ToBase3(0, 1));
        assertEquals(2, base2ToBase3(1, 0));
        assertEquals(3, base2ToBase3(0, 2));
        assertEquals(4, base2ToBase3(0, 3));
        assertEquals(5, base2ToBase3(1, 2));
        assertEquals(6, base2ToBase3(2, 0));
    }
}
