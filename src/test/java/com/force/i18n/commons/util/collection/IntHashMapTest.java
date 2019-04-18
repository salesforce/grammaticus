/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.commons.util.collection;

public class IntHashMapTest extends AbstractIntMapTest {
    public IntHashMapTest(String name) {
        super(name);
    }

    @Override
    protected <T> IntMap<T> newInstance() {
        return new IntHashMap<T>();
    }

    @Override
    protected <T> IntMap<T> newInstance(IntMap<? extends T> toClone) {
        return new IntHashMap<T>(toClone);
    }

    @Override
    protected boolean preservesInsertionOrder(IntMap<?> map) {
        return false;
    }
    
    @Override
    protected boolean supportsNegativeIndices() {
        return true;
    }
}
