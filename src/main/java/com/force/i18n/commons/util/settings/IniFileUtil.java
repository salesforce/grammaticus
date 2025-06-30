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

	private static final Interner<String> INTERNER = Interners.newBuilder().weak().concurrencyLevel(16).build();

    /**
     * For the given {@link String}, return reference to equal String.  Useful for {@link String} deduping.
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