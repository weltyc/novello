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
