/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * A standard implementation of {@link PropertyFileData}
 * that is a HashMap of section name to HashMaps of parameter name to values.
 * <p>
 * If there is overlap between the section or parameter keys with, say another
 * language's version, you should consider using the {@link SharedKeyMapPropertyFileData}
 * implementation.
 *
 * @author koliver
 * @since 146
 */
public class MapPropertyFileData implements PropertyFileData, Serializable {
    private static final long serialVersionUID = 1L;

    protected final Locale locale;
    protected final Map<String, Map<String, Object>> data;
    protected final Set<String> publicSections;

    /**
     * Use this constructor if you are uninterested in locale specific data.
     * Means that {@link #getLocale()} will throw an exception if called.
     */
    public MapPropertyFileData() {
        this(null);
    }

    /**
     * @param locale the locale of this property file data
     * Only use this constructor if you are dealing with
     *      data that is specific to a locale. The locale id should correspond
     *      to a value that came from the global.locale table.
     */
    public MapPropertyFileData(Locale locale) {
        this.locale = locale;
        this.data = new HashMap<String, Map<String, Object>>();
        this.publicSections = new HashSet<String>();
    }

    /**
     * @throws IllegalStateException if this is called when constructed without a localeId specified.
     */
    @Override
    public Locale getLocale() {
        if (this.locale == null) {
            throw new IllegalStateException("Cannot call getLocale() on a map constructed without a locale");
        }
        return this.locale;
    }

    @Override
    public Map<String, Object> getSection(String sectionName) {
        if (!this.data.containsKey(sectionName)) {
            return null;
        }

        return Collections.unmodifiableMap(this.data.get(sectionName));
    }

    @Override
    public Set<Entry<String, Map<String, Object>>> entrySet() {
        return Collections.unmodifiableSet(this.data.entrySet());
    }

    @Override
    public Set<String> getSectionNames() {
        return Collections.unmodifiableSet(this.data.keySet());
    }

    @Override
    public Set<String> getPublicSectionNames() {
        return Collections.unmodifiableSet(publicSections);
    }

    @Override
    public void removeSection(String sectionName) {
        this.data.remove(sectionName);
    }

    @Override
    public Object remove(String sectionName, String paramName) {
        Map<String, Object> section = this.data.get(sectionName);
        if (section == null) {
            return null;
        }

        return section.remove(paramName);
    }

    @Override
    public boolean containsSection(String sectionName) {
        return this.data.containsKey(sectionName);
    }

    @Override
    public boolean contains(String sectionName, String paramName) {
        Map<String, Object> section = this.data.get(sectionName);
        if (section == null) {
            return false;
        }
        return section.containsKey(paramName);
    }

    @Override
    public Object put(String sectionName, String paramName, Object value) {
        Map<String, Object> section = this.data.get(sectionName);
        if (section == null) {
            section = new HashMap<String, Object>();
            this.data.put(sectionName, section);
        }
        return section.put(paramName, intern(value));
    }

    @Override
    public void setSectionAsPublic(String section) {
        if (!section.equals(section.toLowerCase())) {
            throw new RuntimeException("Public sections must have a lowercase name");
        }
        this.publicSections.add(section);
    }

    /**
     * No-op.
     */
    @Override
    public void shareKeys(SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap) {
    }

}