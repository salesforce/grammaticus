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

package com.force.i18n;

import java.net.URL;
import java.util.List;


/**
 * Represents a LabelSet that is for a specific language.
 *
 * This allows different root URLs to be used for different languages
 *
 * @author stamm
 */
public interface LanguageLabelSetDescriptor extends LabelSetDescriptor {

    /**
     * @return the language represented by this label set descriptor.
     */
    HumanLanguage getLanguage();

    /**
     * Return a label set descriptor for the other language.  This allows the
     * roots of different languages to be different for different languages
     * @param otherLanguage the other language for which to return the descriptor
     * @return a LabelSetDescriptor suitable for using the other language, or {@code this}
     * if {@code getLanguage() == otherLanguage}
     */
    LanguageLabelSetDescriptor getForOtherLanguage(HumanLanguage otherLanguage);

    /**
     * Interface for grammatical label sets (that is have a dictionary file)
     */
    public interface GrammaticalLabelSetDescriptor extends LanguageLabelSetDescriptor {
        /**
         * @return a string that represents the name of the label set that is suitable
         * for use as a directory in a path for caching.
         *
         */
        String getLabelSetName();

        /**
         * @return the location of the base dictionary file
         * Can be null if no grammatical terms are used for these labels (then fallback can be used)
         */
        URL getDictionaryFile();

        /**
         * @return the location of the overriding dictionary files if applicable
         * @see LabelSetDescriptor#getOverridingFiles()
         */
        List<URL> getOverridingDictionaryFiles();

        /**
         * @return the label set descriptor suitable for use in a language other than this
         * one.  Usually it returns the same {@link #getRootDir()}, but this allows
         * overriding to allow a different root for different languages.
         */
        @Override
        GrammaticalLabelSetDescriptor getForOtherLanguage(HumanLanguage otherLanguage);


    }
}
