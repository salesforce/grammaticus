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

import java.util.Arrays;

import com.google.common.annotations.Beta;

/**
 * Abstract implementation of an IntSet.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author stamm
 */
@Beta
public abstract class AbstractIntSet implements IntSet {

    public AbstractIntSet() {
        super();
    }

    /**
     * Returns an array containing all of the elements in this collection.  If
     * the collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the
     * same order.  The returned array will be "safe" in that no references to
     * it are maintained by the collection.  (In other words, this method must
     * allocate a new array even if the collection is backed by an Array).
     * The caller is thus free to modify the returned array.<p>
     *
     * This implementation allocates the array to be returned, and iterates
     * over the elements in the collection, storing each object reference in
     * the next consecutive element of the array, starting with element 0.
     *
     * @return an array containing all of the elements in this collection.
     */
    @Override
    public int[] toArray() {
        int[] result = new int[size()];
        IntIterator e = iterator();
        for (int i = 0; e.hasNext(); i++) {
            result[i] = e.next();
        }
        Arrays.sort(result);
        return result;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(int o) {
        IntIterator e = iterator();
        while (e.hasNext())
            if (o == e.next())
                return true;
        return false;
    }

    @Override
    public boolean containsAll(IntSet c) {
        IntIterator e = c.iterator();
        while (e.hasNext())
            if (!contains(e.next()))
                return false;
        return true;
    }

    @Override
    public boolean add(int o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof IntSet))
            return false;
        IntSet c = (IntSet)o;
        if (c.size() != size())
            return false;
        try {
            return containsAll(c);
        }
        catch (ClassCastException unused) {
            return false;
        }
        catch (NullPointerException unused) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        IntIterator i = iterator();
        while (i.hasNext()) {
            h += i.next();
        }
        return h;
    }
}
