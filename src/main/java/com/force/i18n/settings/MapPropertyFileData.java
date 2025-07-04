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

package com.force.i18n.settings;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

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
public class MapPropertyFileData implements PropertyFileData {
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