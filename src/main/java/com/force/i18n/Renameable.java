/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import com.force.i18n.grammar.Noun;

/**
 * Interface for objects that are "Renameable", i.e. the can be referenced
 * by the &lt;Entity&gt; tag in the xml files.
 *
 * @author stamm
 */
public interface Renameable {
    public static final String ENTITY_NAME = "entity";
    public static final String ENTITY_NAME_PREFIX = ENTITY_NAME + "_";
    public static final String ENTITY_NAME_FIELD = ENTITY_NAME + "_namefield";

    /**
     * @return the unique DB identifier of this object.
     */
    String getName();

    /**
     * @return the (localized) label of this object.
     */
    String getLabel();

    /**
     * @return the (localized) label of this object in plural.  This is a little sleazy
     */
    String getLabelPlural();

    /**
     * @return <tt>True</tt> if the object has a "standard" label (i.e. isn't custom)
     */
    boolean hasStandardLabel();

    /**
     * @param language the language to return
     * @param field the key for the standard field on this renameable entity to return
     * @return the label for the given standard field
     */
    String getStandardFieldLabel(HumanLanguage language, StandardField field);

    /**
     * @param labelKey the label from the grammar files
     * @return for the given labelKey, return the DB key that should be used to retreive it.
     * I.e., if labelKey is "Entity" and this is "Account", it should return "Account".
     */
    String getEntitySpecificDbLabelKey(String labelKey);
    
    
    /**
     * @param labelKey the DB-specific key that may or may not be a field
     * @return the field for the labelKey if appropriate, or null if it does not exist.
     */
    StandardField getRenameableFieldForKey(String labelKey);

    /**
     * @return the "standard" noun in the given language.  If you want the renamed version, you should
     * use RenamingProvider.get() or the equivalent
     * @param language the language for the noun.
     */
    Noun getStandardNoun(HumanLanguage language);
    
    /**
     * Marker interface for StandardFields.  It's assumed it will be implemented by an enum.
     * @author stamm
     */
    public interface StandardField {
        String name();
    }
}
