/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.net.URL;
import java.util.List;


/**
 * Represents a LabelSet that is for a specific language.
 * <p>
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
     *
     * @param otherLanguage the other language for which to return the descriptor
     * @return a LabelSetDescriptor suitable for using the other language, or <tt>this</tt>
     * if getLanguage() == otherLanguage
     */
    LanguageLabelSetDescriptor getForOtherLanguage(HumanLanguage otherLanguage);

    /**
     * Interface for grammatical label sets (i.e. have a dictionary file)
     */
    public interface GrammaticalLabelSetDescriptor extends LanguageLabelSetDescriptor {
        /**
         * @return a string that represents the name of the label set that is suitable
         * for use as a directory in a path for caching.
         */
        String getLabelSetName();

        /**
         * @return the location of the base dictionary file
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
