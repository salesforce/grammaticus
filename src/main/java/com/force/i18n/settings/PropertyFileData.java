/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;


/**
 * Stores our configuration (and label) data. Data is referenced by 2-level keys:
 * section name and then parameter name within that section.
 *
 * @author koliver
 */
public interface PropertyFileData extends Serializable {

    /**
     * @return an unmodifiable map of all the values for a particular sectionName.
     * If, that section does not exist, will return <code>null</code>.
     * @param sectionName the section name
     */
    Map<String, Object> getSection(String sectionName);

    /**
     * @return an unmodifiable view of the data.
     */
    Set<Entry<String, Map<String, Object>>> entrySet();

    /**
     * Adds a value to the data.
     * @return the old value for this section/parameter key, or null if no value existed.
     * @param sectionName the section name
     * @param paramName the param name
     * @param value the value to set for that section/parameter
     */
    Object put(String sectionName, String paramName, Object value);

    /**
     * Removes the value for a section/parameter key,
     * @return the old value for this section/parameter key, or null if no value existed.
     * @param sectionName the section name
     * @param paramName the param name
     */
    Object remove(String sectionName, String paramName);

    /**
     * Removes all properties for the given section name.
     * @param sectionName the section name
     */
    void removeSection(String sectionName);

    /**
     * @return an unmodifiable set of section names in the data.
     */
    Set<String> getSectionNames();


    /**
     * @return an unmodifiable map of section names that are "namespaced"
     * in that they are publicly accessible and versioned.  The keys of the namespaces
     * are all "lowercase", unlike normal section names, which are the associated values
     */
    Set<String> getPublicSectionNames();

    /**
     * The set of "public" sections.
     * @param section the section to mark as "public"
     */
    void setSectionAsPublic(String section);

    /**
     * @return whether or not the section exists.
     * @param sectionName the section to mark as "public"
     * @see #contains(String, String)
     */
    boolean containsSection(String sectionName);

    /**
     * @return whether or not the property exists.
     * @see #containsSection(String)
     * @param sectionName the section name
     * @param paramName the param name
     */
    boolean contains(String sectionName, String paramName);

    /**
     * Gets the locale associated with this data if any
     *
     * @return the locale associated with this property data.
     *      Some implementations may not require specifying this, and thus
     *      may throw an exception if called.
     */
    Locale getLocale();

    /**
     * If this implementation supports sharing of section and parameter names,
     * then this should allow a non-shared representation to "hop" onto the sharing bandwagon.
     * <p>
     * This is not always supported by implementations. If the implementation doesn't
     * support it, it should be considered a no-op.
     *
     * @see SharedKeyMapPropertyFileData
     * @param seedKeyMap the seed key map to share keys with
     */
    void shareKeys(SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap);

    /**
     * @param sectionName the section name
     * @param paramName the parameter name
     * @return a raw label value as {@code Object} for the a particular {@code sectionName} and {@code paramName} or
     * {@code null} if the {@code sectionName} or {@code paramName} does not exist.
     */
    default Object get(String sectionName, String paramName) {
        Map<String, Object> section = getSection(sectionName);
        return section == null ? null : section.get(paramName);
    }
}
