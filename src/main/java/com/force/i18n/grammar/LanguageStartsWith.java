/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

/**
 * Represents the different forms of starting phonemes associated with an noun
 * TODO: Change to LanguagePhonemeType, since in Korean, it isn't startswith, it's endswith.
 * 
 * @author stamm
 */
public enum LanguageStartsWith {
    CONSONANT ("c", "Consonant"),
    VOWEL ("v", "Vowel"),
    SPECIAL ("s", "Special"),
    ;
	
	public static final char JSON_ATTR_NAME = 's'; 

    private final String dbValue;
    private final String apiValue;
    private LanguageStartsWith(String dbValue, String apiValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
    }
    public String getDbValue() { return this.dbValue; }
    public String getApiValue() { return this.apiValue; }
    public boolean isDefault() { return false; }
    public static LanguageStartsWith fromDbValue(String dbValue) {
        if (dbValue == null) return null;
        for (LanguageStartsWith e : values()) {
            if (e.getDbValue().equals(dbValue)) return e;
        }
        return null;
    }

    public static LanguageStartsWith fromApiValue(String apiValue) {
        for (LanguageStartsWith e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }
}
