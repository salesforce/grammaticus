/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.settings;

import java.util.*;
import java.util.Map.Entry;

/**
 * Simple implementation of an IniFile.
 * (Moved from tests)
 *
 * @author koliver
 * @see #set(String, String, Object)
 */
public class SimpleNonConfigIniFile extends AbstractNonConfigIniFile {

    // Map of section name to a HashMap of parameters -> value.
    private final Map<String, Map<String, Object>> sections = new HashMap<String, Map<String, Object>>(16);

    @Override
    public Object get(String section, String param) {
        Map<String, Object> sectionMap = this.sections.get(section);
        if (sectionMap == null) return null;
        return sectionMap.get(param);
    }

    @Override
    public Object get(String section, String param, Object ifNull) {
        Object value = get(section, param);
        if (value == null) return ifNull;
        return value;
    }

    @Override
    public Map<String, Object> getSection(String section) {
        return this.sections.get(section);
    }

    public void set(final String section, final String paramName, final Object value) {
        if (section == null)
            throw new IllegalArgumentException("section can't be null");
        if (paramName == null)
            throw new IllegalArgumentException("paramName can't be null");
        if (value == null)
            throw new IllegalArgumentException("value can't be null");
        Map<String, Object> sectionMap = getOrCreate(section);

        sectionMap.put(paramName, value);
    }

    private Map<String, Object> getOrCreate(final String section) {
        Map<String, Object> sectionMap = this.sections.get(section);
        if (sectionMap == null) {
            sectionMap = new HashMap<String, Object>(16);
            this.sections.put(section, sectionMap);
        }
        return sectionMap;
    }

    public void set(String section, String paramName, int intValue) {
        set(section, paramName, Integer.toString(intValue));
    }

    public void set(String section, String paramName, boolean booleanValue) {
        set(section, paramName, booleanValue ? "1" : "0");
    }

    // add a bunch of params at once
    public void addSection(String sectionName, Map<String, Object> section) {
        for (Entry<String, Object> entry : section.entrySet()) {
            set(sectionName, entry.getKey(), entry.getValue());
        }
    }

    public void addList(String sectionName, String prefix, List<? extends Object> values) {
        Map<String, Object> section = getOrCreate(sectionName);
        int i = 0;
        for (Object entry : values) {
            section.put(prefix + "_" + i++, entry);
        }
    }

    /**
     * Removes the value at the given section/paramName.
     *
     * @return the old value at that configuration.
     * Will be null if there is nothing specified in the configs.
     */
    public Object remove(String section, String paramName) {
        Map<String, Object> sectionMap = this.sections.get(section);
        if (sectionMap == null) return null;

        Object oldValue = sectionMap.remove(paramName);
        return oldValue;
    }

    @Override
    public Set<Entry<String, Map<String, Object>>> entrySet() {
        return this.sections.entrySet();
    }
}
