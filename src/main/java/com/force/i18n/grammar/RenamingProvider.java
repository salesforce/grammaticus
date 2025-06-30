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

import com.force.i18n.HumanLanguage;
import com.force.i18n.Renameable;

/**
 * Interface for something that provides nouns that are renamed for the given request
 *
 * @author stamm
 */
public interface RenamingProvider {
    /**
     * Get a renamed "standard" noun
     * @param language the language
     * @param key the key for the "custom" entity.
     * @return a noun for the given renameable, if it is renamed
     */
    Noun getRenamedNoun(HumanLanguage language, String key);

    /**
     * Get the "original" noun for a non standard key, in case there is some other "standard" label for it
     * @param language the language
     * @param key the key for the "custom" entity.
     * @return a noun for the given renameable with the "original" values
     */
    Noun getPackagedNoun(HumanLanguage language, String key);


    /**
     * Get a renamed noun for the given renameable, or the standard noun if there isn't
     * a renamed noun for that object.  This is a convenience method for callers so that
     * they don't have to do the fallthrough logic themselves
     * @param language the language
     * @param key the key for the "custom" entity.
     * @return the noun for the given renameable, renamed if applicable
     */
    Noun getNoun(HumanLanguage language, Renameable key);


    /**
     * @return true if the noun identified by "key" is renamed in the given language
     * @param language the language to test
     * @param key the name of the noun to check
     */
    boolean isRenamed(HumanLanguage language, String key);

    /**
     * @return true if the noun identified by "key" is "custom"
     * @param key he name of the noun to check
     */
    boolean isCustomKey(String key);


    /**
     * @return {@code true} if renaming should be checked for standard nouns (i.e. not)
     */
    boolean useRenamedNouns();

    /**
     * Grammar engine switched in 164
     * @return {@code true} if API version is less than 164
     */
    boolean supportOldGrammarEngine();

    /**
     * @return the version to use for the name of labels.
     *
     * This allows the strings of a noun to be versioned in a way that, if the
     * noun isn't overridden, will provide a different value based on the
     * version
     */
    default double getLabelVersion() {
    	return 0.0;
    }

    /**
     * @return {@code true} if the current User Context's Perm and Prefs allow them to see the Middle Name in
     *         calculated full people's names
     */
    // TODO - Not the best class name or place to put this method but this is the best we can do for now.
    boolean displayMiddleNameInCalculatedPersonName();

    /**
     * @return {@code true} if the current User Context's Perm and Prefs allow them to see the Suffix in calculated
     *         full people's names
     */
    // TODO - Not the best class name or place to put this method but this is the best we can do for now.
    boolean displaySuffixInCalculatedPersonName();


}
