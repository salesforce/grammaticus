/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.commons.util.collection;

import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author fiaz.hossain
 * @since 1.1
 */
public class LruCacheTest extends TestCase {

    public LruCacheTest(String name) {
        super(name);
    }

    public void testConstructors() throws Exception {
        LruCache<String, String> cache = new LruCache<String, String>(5);
        runAll(cache);
        
        cache = new LruCache<String, String>(5, 1);
        runAll(cache);        
    }

    private void runAll(LruCache<String, String> cache) throws Exception {        
        Assert.assertEquals(5, cache.getCapacity());
        cache.put("one", "1");
        cache.put("two", "2");
        cache.put("three", "3");
        cache.put("four", "4");
        cache.put("five", "5");
        Assert.assertTrue("1".endsWith(cache.get("one")));
        cache.put("six", "6");
        Assert.assertTrue("6".endsWith(cache.get("six")));
        // Since we accessed 1 earlier it shoudl stay
        Assert.assertTrue("1".endsWith(cache.get("one")));
        // Instead 2 was the oldest and it should go
        Assert.assertNull(cache.get("two"));
    }
}
