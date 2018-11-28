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
        return getString(section, param).equals("1");
    }

    @Override
    public boolean getBoolean(String section, String param, boolean ifNull) {
        return getString(section, param, ifNull ? "1" : "0").equals("1");
    }

    @Override
    public float getFloat(String section, String param) {
        Object val = get(section, param);
        if (val instanceof String) {
            try {
                return Float.parseFloat(getString(section, param));
            } catch (NumberFormatException x) {
                throw new RuntimeException("Section: " + section + ", Param: " + param, x);
            }
        } else if (val instanceof Float) {
            return (Float) val;
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
        if (val instanceof String) {
            try {
                return Integer.parseInt(getString(section, param));
            } catch (NumberFormatException x) {
                throw new RuntimeException("Section: " + section + ", Param: " + param, x);
            }
        } else if (val instanceof Integer) {
            return (Integer) val;
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
        return (String) get(section, param);
    }

    @Override
    public String getCensoredString(String section, String param, String ifNull) {
        String value = getString(section, param, null);
        if (value == null) return ifNull;

        return SettingsUtil.censorValue(section, param, value);
    }

    @Override
    public String getStringThrow(String section, String param) {
        return (String) get(section, param);
    }

    @Override
    public String getString(String section, String param, String ifNull) {
        return (String) get(section, param, ifNull);
    }

    @Override
    public abstract Set<Entry<String, Map<String, Object>>> entrySet();
}
