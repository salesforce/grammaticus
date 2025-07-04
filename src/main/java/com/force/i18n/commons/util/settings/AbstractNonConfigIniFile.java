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

package com.force.i18n.commons.util.settings;

import java.util.*;
import java.util.Map.Entry;

/**
 * Simple implementation of the type conversion for an IniFile.
 *
 * @author koliver
 */
public abstract class AbstractNonConfigIniFile implements NonConfigIniFile {

    @Override
    public List<String> getList(String section, String param) {
        Map<String, Object> sectionMap = getSection(section);
        if (sectionMap == null)
            return null;
        return getParamList(section, param);
    }

    /**
     * Gets a List of all values whose parameters start with <code>baseParam_x</code>,
     * where x is number starting from 0.
     * This returns null in the case there are no elements.
     */
    private List<String> getParamList(String section, String baseParam) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; true; ++index) {
            String param = baseParam + "_" + index;
            String result = getString(section, param, null);
            if (result == null) {
                break;
            } else {
                list.add(result);
            }
        }
        return list.size() > 0 ? list : null;
    }

    @Override
    public List<String> getList(String section, String param, List<String> ifNull) {
        List<String> list = getList(section, param);
        if (list == null)
            return ifNull;
        return list;
    }

    @Override
    public boolean getBoolean(String section, String param) {
        return "1".equals(getString(section, param));
    }

    @Override
    public boolean getBoolean(String section, String param, boolean ifNull) {
        return getString(section, param, ifNull ? "1" : "0").equals("1");
    }

    @Override
    public float getFloat(String section, String param) {
        Object val = get(section, param);
        if (val instanceof String ) {
            try {
                return Float.parseFloat(getString(section, param));
            } catch (NumberFormatException x) {
                throw new RuntimeException("Section: " + section + ", Param: " + param, x);
            }
        } else if (val instanceof Float) {
            return (Float)val;
        }
        throw new RuntimeException("param " + param + " not found in section " + section);
    }

    @Override
    public float getFloat(String section, String param, float ifNull) {
        Object val = get(section, param);
        if (val == null)
            return ifNull;
        return getFloat(section, param);
    }

    @Override
    public int getInt(String section, String param) {
        Object val = get(section, param);
        if (val instanceof String ) {
            try {
                return Integer.parseInt(getString(section, param));
            } catch (NumberFormatException x) {
                throw new RuntimeException("Section: " + section + ", Param: " + param, x);
            }
        } else if (val instanceof Integer) {
            return (Integer)val;
        }
        throw new RuntimeException("param " + param + " not found in section " + section);
    }

    @Override
    public int getInt(String section, String param, int ifNull) {
        Object val = get(section, param);
        if (val == null)
            return ifNull;
        return getInt(section, param);
    }

    @Override
    public String getString(String section, String param) {
        return (String)get(section, param);
    }

    @Override
    public String getCensoredString(String section, String param, String ifNull) {
        String value = getString(section, param, null);
        if(value == null) return ifNull;

        return SettingsUtil.censorValue(section, param, value);
    }

    @Override
    public String getStringThrow(String section, String param) {
        return (String)get(section, param);
    }

    @Override
    public String getString(String section, String param, String ifNull) {
        return (String)get(section, param, ifNull);
    }

    @Override
    public abstract Set<Entry<String, Map<String, Object>>> entrySet();
}
