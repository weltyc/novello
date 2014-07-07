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

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 28, 2009
 * Time: 9:57:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class CEvaluatorInfoTest extends TestCase {
    public void testEqualsHashCode() {
        final CEvaluatorInfo a = new CEvaluatorInfo('J', 'A');
        final CEvaluatorInfo b = new CEvaluatorInfo('J', 'A');
        final CEvaluatorInfo c = new CEvaluatorInfo('K', 'A');
        final CEvaluatorInfo d = new CEvaluatorInfo('J', 'B');

        assertEquals(a, a);
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
        assertFalse(a.hashCode() == c.hashCode());
        assertFalse(a.equals(d));
        assertFalse(a.hashCode() == d.hashCode());
    }
}
