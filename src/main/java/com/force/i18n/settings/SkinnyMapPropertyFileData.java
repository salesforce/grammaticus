/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Responsible for providing property file data in a space efficient way. By using {@link SkinnyMapPropertyFileData}'s the overhead per
 * map element stored at both levels is reduced to 8 bytes per entry. Most other {@link Map} implementation have higher
 * per entry overheads. The trade of is the cost of get's will go from O(1) to O(logN). So clients that can afford
 * slower gets, and have memory conscious requirements, should consider using this over other {@link PropertyFileData}
 * implementations.
 *
 * Also, instances of {@link SkinnyMapPropertyFileData} will only be truly immutable if the concrete
 * {@link Object} in 'data' is immutable (i.e. String).
 *
 * @author btoal
 */
public final class SkinnyMapPropertyFileData implements PropertyFileData {
    private static final long serialVersionUID = 1L;
    private final Locale locale;
    private final Map<String, Map<String, Object>> data;
    private final Set<String> publicSections;

    /**
     * Creates a new {@link SkinnyMapPropertyFileData} from existing MapPropertyFileData
     *
     * @param m MapPropertyFileData that will be copied into a ImmutableSortedMap.
     */
    public SkinnyMapPropertyFileData(MapPropertyFileData m) {
        this.locale = m.locale;
        this.publicSections = ImmutableSet.copyOf(m.getPublicSectionNames());


        for (Entry<String, Map<String, Object>> e : m.entrySet()) {
            // Convert the parameter HashMap's to ImmutableSortedMap's to reduce memory required.
            m.data.put(e.getKey(), ImmutableSortedMap.copyOf(e.getValue()));
        }
        // Convert the section HashMap to ImmutableSortedMap
        data = ImmutableSortedMap.copyOf(m.data);
    }

    /**
     * Returns section, if exists from property file metadata.
     */
    @Override
    public Map<String, Object> getSection(String sectionName) {
        if (!data.containsKey(sectionName)) {
            return null;
        }
        return data.get(sectionName);
    }

    /**
     * Returns the set sections and corresponding parameters/values.
     */
    @Override
    public Set<Entry<String, Map<String, Object>>> entrySet() {
        return data.entrySet();
    }

    /**
     * returns if a section exists (true) or not (false) in this property data.
     */
    @Override
    public boolean containsSection(String sectionName) {
        return data.containsKey(sectionName);
    }

    /**
     * returns if a parameter exists (true) or not (false) for the specified section.
     */
    @Override
    public boolean contains(String sectionName, String paramName) {
        Map<String, Object> section = this.data.get(sectionName);
        if (section == null) {
            return false;
        }

        return section.containsKey(paramName);
    }

    /**
     * Get's copy of locale.
     */
    @Override
    public Locale getLocale() {
        if (this.locale == null) {
            throw new IllegalStateException("Cannot call getLocale() on a map constructed without a locale");
        }
        return this.locale;
    }

    /**
     * Returns set of section names.
     */
    @Override
    public Set<String> getSectionNames() {
        return data.keySet();
    }

    /**
     * Returns set of public sections.
     */
    @Override
    public Set<String> getPublicSectionNames() {
        return publicSections;
    }

    /**
     * Use of method will always result in {@link IllegalStateException}. This is because the data within
     * {@link SkinnyMapPropertyFileData} should not be modified.
     */
    @Override
    public Object put(String sectionName, String paramName, Object value) {
        throw new IllegalStateException("Parameters can't be added since " + getClass().getSimpleName() + " is not modifiable.");
    }

    /**
     * Use of method will always result in {@link IllegalStateException}. This is because the data within
     * {@link SkinnyMapPropertyFileData} should not be modified.
     */
    @Override
    public Object remove(String sectionName, String paramName) {
        throw new IllegalStateException("Parameters can't be removed since " + getClass().getSimpleName() + " is not modifiable.");
    }

    /**
     * Use of method will always result in {@link IllegalStateException}. This is because the data within
     * {@link SkinnyMapPropertyFileData} should not be modified.
     */
    @Override
    public void removeSection(String sectionName) {
        throw new IllegalStateException("Sections can't be removed since " + getClass().getSimpleName() + " is not modifiable.");
    }

    /**
     * Use of method will always result in {@link IllegalStateException}. This is because the data within
     * {@link SkinnyMapPropertyFileData} should not be modified.
     */
    @Override
    public void setSectionAsPublic(String section) {
        throw new IllegalStateException("Sections can't be set as public since " + getClass().getSimpleName() + " is not modifiable.");
    }

    @Override
    public void shareKeys(SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap) {
        /* No-operation */
    }
}