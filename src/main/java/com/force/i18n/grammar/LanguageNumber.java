/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

/**
 * Represents the number associated with a noun form
 * @author stamm
 */
public enum LanguageNumber {
    SINGULAR (0, "Singular"),
    PLURAL (1, "Plural"),
    ;
    private final int dbValue;
    private final String apiValue;
    private LanguageNumber(int dbValue, String apiValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
    }
    public int getIntValue() { return this.dbValue; }
    public String getDbValue() { return String.valueOf(this.dbValue); }
    public String getApiValue() { return this.apiValue; }
    public boolean isDefault() { return this == SINGULAR; }

    /**
     * @return whether this is "plural" or not.
     */
    public boolean isPlural() { return this == PLURAL; }
    public static LanguageNumber fromDbValue(String dbValue) {
        for (LanguageNumber e : values()) {
            if (e.getDbValue().equals(dbValue)) return e;
        }
        return null;
    }
    public static LanguageNumber fromLabelValue(String dbValue) {
        return "y".equalsIgnoreCase(dbValue) ? PLURAL : SINGULAR;
    }
    public static LanguageNumber fromIntValue(int intValue) {
        for (LanguageNumber e : values()) {
            if (e.getIntValue() == intValue) return e;
        }
        return null;
    }
    public static LanguageNumber fromApiValue(String apiValue) {
        for (LanguageNumber e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }
}
