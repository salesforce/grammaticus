/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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
	 * NOTE: this doesn't actually replace the message format.
     */
    String getString(String section, String param, Renameable[] entities, Object... vals);

    /**
	 * NOTE: this doesn't actually replace the message format.
     */
    String getString(String section, String param, boolean forMessageFormat, Object... vals);

    /**
	 * NOTE: this doesn't actually replace the message format.
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
     * @param keys: optional set of section names or section.key names that restrict which labels to include
     * @param termsInUse: if provided and non null, the set of used terms by all the given labels will be added to it;
     * @throws IOException
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
