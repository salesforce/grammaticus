/*
 * Copyright (c) 2025, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.force.i18n.commons.util.collection;

import java.lang.reflect.Field;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author s.hansma
 * @since 1.1
 */
public class ExpandableArrayTest extends TestCase {

    private ExpandableArray<Object> array;

    public ExpandableArrayTest(String arg0) {
        super(arg0);
    }

    public void testGet() {
        this.array.set(3, Integer.valueOf(3));
        assertEquals(Integer.valueOf(3), this.array.get(3));
        this.array.set(99, Integer.valueOf(99));
        assertEquals(Integer.valueOf(99), this.array.get(99));

        assertEquals(null, this.array.get(54));
        assertEquals(null, this.array.get(108));

        // negative test -- should throw ArrayIndexOutOfBoundsException
        try {
            this.array.get(-1);
            // should have thrown
            fail();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // good
        }
    }

    public void testSet() {
        // we tested the positive cases of set in testGet

        // negative test -- should throw ArrayIndexOutOfBoundsException
        try {
            this.array.set(-1, null);
            // should have thrown
            fail();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // good
        }
    }

    public void testSize() {
        assertEquals(0, this.array.size());
        this.array.set(3, new Object());
        assertEquals(4, this.array.size());
        this.array.set(99, new Object());
        assertEquals(100, this.array.size());
        this.array.set(99, null);
        assertEquals(4, this.array.size());
        this.array.set(3, null);
        assertEquals(0, this.array.size());
    }

    public void testClear() {
        this.array.set(1, Integer.valueOf(1));
        this.array.clear();
        assertEquals(0, this.array.size());
        assertNull(this.array.get(1));
    }

    public void testResizeArray() {
        String[] resized = ExpandableArray.resizeArray(new String[] { "a", "b", "c", "d" }, 3);
        assertTrue(Arrays.deepToString(resized), Arrays.deepEquals(new String[] { "a", "b", "c" }, resized));

        resized = ExpandableArray.resizeArray(new String[] { "a", "b", "c", "d" }, 4);
        assertTrue(Arrays.deepToString(resized), Arrays.deepEquals(new String[] { "a", "b", "c", "d" }, resized));

        resized = ExpandableArray.resizeArray(new String[] { "a", "b", "c", "d" }, 5);
        assertTrue(Arrays.deepToString(resized), Arrays.deepEquals(new String[] { "a", "b", "c", "d", null }, resized));

        resized = ExpandableArray.resizeArray(new String[] { "a", "b", "c", "d" }, 0);
        assertTrue(Arrays.deepToString(resized), Arrays.deepEquals(new String[0], resized));

        resized = ExpandableArray.resizeArray(new String[0], 0);
        assertTrue(Arrays.deepToString(resized), Arrays.deepEquals(new String[0], resized));

        resized = ExpandableArray.resizeArray(new String[0], 2);
        assertTrue(Arrays.deepToString(resized), Arrays.deepEquals(new String[] { null, null }, resized));

        String[] array = new String[] { "a", "b", "c" };
        assertSame(array, ExpandableArray.resizeArray(array, array.length));

        try {
            ExpandableArray.resizeArray(null, 0);
            fail("expected NullPointerException");
        }
        catch (NullPointerException expected) {}

        try {
            ExpandableArray.resizeArray(new String[0], -1);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {}
    }

    public void testSubArray() {
        String[] subArray = ExpandableArray.subArray(new String[] { "a", "b", "c", "d" }, 1, 2);
        assertTrue(Arrays.deepToString(subArray), Arrays.deepEquals(new String[] { "b" }, subArray));

        subArray = ExpandableArray.subArray(new String[] { "a", "b", "c", "d" }, 0, 4);
        assertTrue(Arrays.deepToString(subArray), Arrays.deepEquals(new String[] { "a", "b", "c", "d" }, subArray));

        subArray = ExpandableArray.subArray(new String[] { "a", "b", "c", "d" }, 0, 5);
        assertTrue(Arrays.deepToString(subArray), Arrays
            .deepEquals(new String[] { "a", "b", "c", "d", null }, subArray));

        subArray = ExpandableArray.subArray(new String[] { "a", "b", "c", "d" }, 2, 5);
        assertTrue(Arrays.deepToString(subArray), Arrays.deepEquals(new String[] { "c", "d", null }, subArray));

        subArray = ExpandableArray.subArray(new String[] { "a", "b", "c", "d" }, 2, 2);
        assertTrue(Arrays.deepToString(subArray), Arrays.deepEquals(new String[0], subArray));

        subArray = ExpandableArray.subArray(new String[0], 0, 0);
        assertTrue(Arrays.deepToString(subArray), Arrays.deepEquals(new String[0], subArray));

        subArray = ExpandableArray.subArray(new String[0], 0, 2);
        assertTrue(Arrays.deepToString(subArray), Arrays.deepEquals(new String[] { null, null }, subArray));

        String[] array = new String[] { "a", "b", "c" };
        assertSame(array, ExpandableArray.subArray(array, 0, array.length));

        try {
            ExpandableArray.subArray(null, 0, 0);
            fail("expected NullPointerException");
        }
        catch (NullPointerException expected) {}

        try {
            ExpandableArray.subArray(new String[0], -1, 0);
            fail("expected ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException expected) {}

        try {
            ExpandableArray.subArray(new String[0], 1, 1);
            fail("expected ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException expected) {}

        try {
            ExpandableArray.subArray(new String[] { "a" }, 1, 0);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {}
    }

    public void testToString() {
        ExpandableArray<Integer> array = new ExpandableArray<Integer>();

        array.set(1, 1);
        array.set(5, 5);
        assertEquals("[null, 1, null, null, null, 5]", array.toString());

        array.set(5, null);
        assertEquals("[null, 1]", array.toString());

        array.set(1, null);
        assertEquals("[]", array.toString());
    }

    public void testEquals() {
        inner_testComparisonFunction(new ComparisonAsserter() {
            @Override
            public void assertComparison(int expectedCompareTo, ExpandableArray<Integer> o1, ExpandableArray<Integer> o2) {
                assertEquals(expectedCompareTo == 0, o1.equals(o2));
            }
        });
    }

    public void testCompareTo() {
        inner_testComparisonFunction(new ComparisonAsserter() {
            @Override
            public void assertComparison(int expectedCompareTo, ExpandableArray<Integer> o1, ExpandableArray<Integer> o2) {
                assertEquals(expectedCompareTo, normalize(o1.compareTo(o2)));
            }
        });
    }

    public void testHashCode() {
        inner_testComparisonFunction(new ComparisonAsserter() {
            @Override
            public void assertComparison(int expectedCompareTo, ExpandableArray<Integer> o1, ExpandableArray<Integer> o2) {
                if (expectedCompareTo == 0) {
                    assertEquals(o1.hashCode(), o2.hashCode());
                }
            }
        });
    }

    public void testTrimToSize() throws Exception {
        this.array.set(5, "jive");
        this.array.trimToSize();
        assertEquals("jive", this.array.get(5));
        assertEquals(6, getDataLength());

        this.array.set(5, null);
        this.array.trimToSize();
        assertNull(this.array.get(5));
        assertEquals(0, getDataLength());
    }

    private int getDataLength() throws Exception {
        Field f = this.array.getClass().getDeclaredField("data");
        f.setAccessible(true);
        return ((Object[])f.get(this.array)).length;
    }

    private interface ComparisonAsserter {
        void assertComparison(int expectedCompareTo, ExpandableArray<Integer> o1, ExpandableArray<Integer> o2);
    }

    private static int normalize(int n) {
        return (n == 0) ? 0 : ((n < 0) ? -1 : 1);
    }

    private void inner_testComparisonFunction(ComparisonAsserter asserter) {
        ExpandableArray<Integer> one = new ExpandableArray<Integer>();
        one.set(1, 1);
        one.set(5, 5);

        ExpandableArray<Integer> two = new ExpandableArray<Integer>(one);

        asserter.assertComparison(0, one, two);

        two.set(5, 6);
        asserter.assertComparison(-1, one, two);

        two.set(5, 4);
        asserter.assertComparison(1, one, two);

        two.set(1, 2);
        asserter.assertComparison(-1, one, two);

        two.set(1, null);
        asserter.assertComparison(1, one, two);

        two.clear();
        asserter.assertComparison(1, one, two);

        one.clear();
        asserter.assertComparison(0, one, two);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.array = new ExpandableArray<Object>();
    }

}
