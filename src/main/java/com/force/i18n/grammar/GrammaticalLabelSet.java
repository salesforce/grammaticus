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
import java.util.Collection;
import java.util.Set;

import com.force.i18n.*;
import com.force.i18n.settings.*;


/**
 * Represents a label set that has a "grammar" to it, as opposed to a "normal" labelfile
 * @author stamm
 */
public interface GrammaticalLabelSet extends LabelSet {

    /**
     * @param reference the label to look up
     * @return the value contained in this label set at the given reference, or null if none was found
     */
    Object get(LabelReference reference);

    /**
     * @param section the section to search
     * @param param the key inside that section
     * @param allowLabelException if an exception should be thrown instead of null returned for a bad label
     * @return  the value in the label set for the given section and param, throwing an exception depending on the boolean value
     * @throws ParameterNotFoundException if the parameter is missing
     * @throws SettingsSectionNotFoundException if the section is undefined
     */
    Object get(String section, String param, boolean allowLabelException) throws ParameterNotFoundException, SettingsSectionNotFoundException;

    /**
     * @param reference the label to look up
     * @return the label contained in this label set for the given reference.
     */
    String getString(LabelReference reference);

    String getString(String section, Renameable[] entities, String param);

    String getString(String section, Renameable[] entities, String param, String ifNull) ;

    String getString(String section, String param, boolean forMessageFormat);

    String getStringThrow(String section, String param, boolean forMessageFormat);

    String getString(String section, Renameable[] entities, String param, boolean forMessageFormat);

    /**
     * @return the label and replace the renameable entities and values
	 * NOTE: this doesn't actually replace the message format.
	 * @param section the section of the label
	 * @param param the param of the label
	 * @param entities the renamable entities for replace
	 * @param vals the {n} values to replace
     */
    String getString(String section, String param, Renameable[] entities, Object... vals);

    /**
     * @return the label and replace the values and optionally double the single quotes
	 * @param section the section of the label
	 * @param param the param of the label
	 * @param forMessageFormat whether this will be put into message format and the single quotes need to be doubled
	 * @param vals the {n} values to replace
     */
    String getString(String section, String param, boolean forMessageFormat, Object... vals);

    /**
     * @return the label and replace the renameable entities and values and optionally double the single quotes
	 * @param section the section of the label
	 * @param param the param of the label
	 * @param entities the renamable entities for replace
	 * @param forMessageFormat whether this will be put into message format and the single quotes need to be doubled
	 * @param vals the {n} values to replace
     */
    String getString(String section, String param, Renameable[] entities, boolean forMessageFormat, Object... vals);


    /**
     * @return the dictionary associated with this label set
     */
    LanguageDictionary getDictionary();

    // Overriding visibility
    PropertyFileData getPropertyFileData();

    /**
     * @return the set of section names that are marked "public" in the label files
     */
    Set<String> getPublicSectionNames();

    /**
     * Test to see if the parameter contains *solely* a grammatical term, and if it does, return it, otherwise null
     * @param section the section to search
     * @param param the parameter to search.
     * @return the grammatical term that is the only value contained for the given label, or null if the label is anything else
     */
    GrammaticalTerm getGrammaticalTerm(String section, String param);

    /**
     * Write the entire label set to the given appendable as a Map from section.key to label
     * @param appendable the appendable to write to
     * @param keysToInclude optional set of section names or section.key names that restrict which labels to include
     * @param termsInUse if provided and non null, the set of used terms by all the given labels will be added to it;
     * @throws IOException if there's an error writing to appendable
     */
    void writeJson(Appendable appendable, Collection<String> keysToInclude, Set<GrammaticalTerm> termsInUse) throws IOException;

    /**
     * For a set of labels, determine which Grammatical Terms are in use.  This allows downloading
     * only a subset of nouns to the client.
     * @param keysToInclude a non-optional set of String that correspond to labels that are in use
     * @return a collection of grammatical terms that are referenced in the label.
     */
    Collection<? extends GrammaticalTerm> getUsedTerms(Collection<String> keysToInclude);

    /**
     * An interface that is a composite of two GrammaticalLabelSets, a fallback
     * set for any values that are missing from an overlay set
     */
    public interface GrammaticalLabelSetComposite extends GrammaticalLabelSet{
        /**
         * @return the "overlay" label set on top of the fallback set
         */
        GrammaticalLabelSet getOverlay();

        /**
         * @return the "fallback" label set to provide values if they are missing
         * from the overlay
         */
       GrammaticalLabelSet getFallback();


    }
}
