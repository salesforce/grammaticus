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
