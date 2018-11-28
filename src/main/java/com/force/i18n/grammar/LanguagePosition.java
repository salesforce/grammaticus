/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

/**
 * Represents the different positions of modifiers with respect to the noun.
 * This allows better tracking of which noun the adjective is modifying
 *
 * @author stamm
 */
public enum LanguagePosition {
    PRE("b", "Pre"),   // b = before
    POST("a", "Post")  // a = after
    ;
    private final String dbValue;
    private final String apiValue;

    private LanguagePosition(String dbValue, String apiValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public String getApiValue() {
        return this.apiValue;
    }

    public boolean isDefault() {
        return false;
    }

    public static LanguagePosition fromDbValue(String dbValue) {
        for (LanguagePosition e : values()) {
            if (e.getDbValue().equals(dbValue)) return e;
        }
        return null;
    }

    public static LanguagePosition fromLabelValue(String labelValue) {
        if (labelValue == null) return null;
        for (LanguagePosition e : values()) {
            if (e.getDbValue().equals(labelValue) || e.getApiValue().equals(labelValue)) return e;
        }
        return null;
    }

    public static LanguagePosition fromApiValue(String apiValue) {
        for (LanguagePosition e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }
}
