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

package com.force.i18n.grammar;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Representation of map stores any {@link GrammaticalTerm} in LanguageDictionary.
 * Is a subset of Map but some other methods are added.
 * Note that, not all implementation supports all methods. Updating this would not work for isSkinny=true,
 * and also sequential access won't work if the implementation is not in-memory one.
 *
 */
public interface GrammaticalTermMap<T extends GrammaticalTerm> extends Serializable {

    /**
     * Return true is skinny - immutable
     */
    default boolean isSkinny() {
        return false;
    }

    /**
     * Run validation in the values and make it skinny
     */
    default void validate() {
        for(Map.Entry<String, T> e : entrySet()) {
            e.getValue().validate(e.getKey());
            e.getValue().makeSkinny();
        }
    }

    /**
     * make it skinny - in memory or another immutable solution
     * @return
     */
    GrammaticalTermMap<T> makeSkinny();

    /**
     * set of keys
     */
    Set<String> keySet();

    /**
     * true if this key exists
     * @param name
     * @return
     */
    boolean containsKey(String name);

    /**
     * true if empty
     * @return
     */
    boolean isEmpty();

    /**
     * Get term from this map
     * @param name
     * @return
     */
    T get(String name);

    /**
     * entry set - not work if isSkinny=true.
     */
    void put(String k, T v);

    /**
     * entry set - not work if isSkinny=true.
     */
    void putAll(GrammaticalTermMap<T> other);

    /**
     * Get all terms in this map - not work if not in-memory case.
     */

    Collection<T> values();

    /**
     * Get name - term pairs of this map - not work if not in-memory case.
     */
    Set<Map.Entry<String,T>> entrySet();

    /**
     * Write as json of this map - not work if not in-memory case.
     *
     * @param out
     * @param renamingProvider
     * @param dictionary
     * @param termsToInclude
     * @throws IOException
     */
    void writeJson(Appendable out, RenamingProvider renamingProvider, LanguageDictionary dictionary, Collection<String> termsToInclude) throws IOException;
}