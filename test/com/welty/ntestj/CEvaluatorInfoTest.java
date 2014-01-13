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
