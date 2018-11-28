/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents possession associated with a noun form
 *
 * @author stamm
 */
public enum LanguagePossessive {
    NONE("n", "None"),
    FIRST("f", "FirstPerson"),
    SECOND("s", "SecondPerson"),
    FIRST_PLURAL("F", "FirstPersonPlural", "fpl"),
    SECOND_PLURAL("S", "SecondPersonPlural", "spl"),
    ;

    private static final Map<String, LanguagePossessive> dbValueMap = new HashMap<String, LanguagePossessive>(64);

    static {
        for (LanguagePossessive poss : values()) {
            dbValueMap.put(poss.getDbValue(), poss);
        }
    }

    private final String dbValue;
    private final String apiValue;
    private final String labelValue;

    private LanguagePossessive(String dbValue, String apiValue) {
        this(dbValue, apiValue, null);
    }

    private LanguagePossessive(String dbValue, String apiValue, String labelValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
        this.labelValue = labelValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public String getApiValue() {
        return this.apiValue;
    }

    public String getLabelValue() {
        return this.labelValue == null ? getDbValue() : this.labelValue;
    }

    public boolean isDefault() {
        return this == NONE;
    }

    public static LanguagePossessive fromDbValue(String dbValue) {
        return dbValueMap.get(dbValue);
    }

    public static LanguagePossessive fromLabelValue(String dbValue) {
        if (dbValue == null) return null;
        return dbValueMap.get(dbValue);
    }

    public static LanguagePossessive fromApiValue(String apiValue) {
        for (LanguagePossessive e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }
}
