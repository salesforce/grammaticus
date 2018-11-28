/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.settings;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;


/**
 * Responsible for providing String deduping services to the lib module. If a different String interning solution needs
 * to be used, only this class needs to change instead of all the callees of the services.
 *
 * @author btoal
 */
public class IniFileUtil {

    private static final Interner<String> INTERNER = Interners.newWeakInterner();

    /**
     * For the given {@link String}, return reference to equal String.  Useful for {@link String} deduping.
     *
     * @param str the string to intern
     * @return {@link String}
     */
    public static String intern(final String str) {
        if (str == null) {
            return null;
        }

        return INTERNER.intern(str);
    }

    /**
     * If a given {@link Object} is actually a {@link String}, attempt to dedupe it.
     *
     * @param o the object to intern if it is a string
     * @return the given object, possibly interned into a String using the WeakStringInterner
     */
    public static Object intern(final Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            return intern((String) o);
        }

        return o;
    }
}