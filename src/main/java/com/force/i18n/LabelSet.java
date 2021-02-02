/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * An interface to a label set.  Provides some helper methods
 * to provide debugging access to the original parsed files, such as
 * {@link #getLastModified()} and {@link #getLabelSectionToFilename()}
 *
 * @author stamm
 */
public interface LabelSet extends SharedLabelSet, Serializable {
    /**
     * @return the mapping from label section to file name, if it exists, otherwise null
     */
    Map<String, String> getLabelSectionToFilename();

    /**
     * @return the set of all section names available in this label set
     */
    Set<String> sectionNames();

    /**
     * @return the values inside the section setified.
     * @param sectionName name of the section
     * @throws com.force.i18n.settings.SettingsSectionNotFoundException if the section does not exist.
     */
    Set<String> getParams(String sectionName);

    /**
     * @return the values inside the section setified.
     * @param sectionName the name of the section
     * @param ifNull the value to return for the parameters if there is no section with that name
     */
    Set<String> getParams(String sectionName, Set<String> ifNull);

    /**
     * @param sectionName name of the section
     * @param paramName key for the parameter
     * @return {@code true} if the label set contains the given parameter
     */
    boolean containsParam(String sectionName, String paramName);

    /**
     * @return {@code true} if the label set contains a label with the
     * given sectionName.  This should return in quickly in O(1),
     * unlike sectionNames()
     * @param sectionName the key of the section
     */
    boolean containsSection(String sectionName);

    /**
     * @return the timestamp of the last modified label file
     */
    public long getLastModified();


}
