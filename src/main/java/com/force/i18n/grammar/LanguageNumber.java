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

package com.force.i18n.grammar;

import java.util.EnumSet;
import java.util.Set;

import com.force.i18n.PluralCategory;
import com.google.common.collect.ImmutableSet;

/**
 * Represents the number associated with a noun form
 * @author stamm
 */
public enum LanguageNumber {
    SINGULAR (0, "Singular", PluralCategory.ONE),
    PLURAL (1, "Plural", PluralCategory.OTHER),
    DUAL (2, "Dual", PluralCategory.TWO),
    ;

	public static final char JSON_ATTR_NAME = 'q';  // q for quantity (since n is noun)

    private final int dbValue;
    private final String apiValue;
    private final PluralCategory pluralCategory; // CLDR category value
    private LanguageNumber(int dbValue, String apiValue, PluralCategory pluralCategory) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
        this.pluralCategory = pluralCategory;
    }
    public int getIntValue() { return this.dbValue; }
    public String getDbValue() { return String.valueOf(this.dbValue); }
    public String getApiValue() { return this.apiValue; }

    /**
     * @return the CLDR Category value for the given number.
     */
    public PluralCategory getPluralCategory() { return this.pluralCategory; }
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
    	PluralCategory category = PluralCategory.fromCategory(dbValue);
    	if (category != null) {
    		LanguageNumber num = fromPluralCategory(category);
    		if (num != null) return num;
    	}
    	if ("d".equalsIgnoreCase(dbValue) || "2".equals(dbValue)) return DUAL;
    	if ("y".equalsIgnoreCase(dbValue)) return PLURAL;
    	assert "n".equalsIgnoreCase(dbValue) : "Invalid plural value for label: " + dbValue;
    	return SINGULAR;
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
    static LanguageNumber fromPluralCategory(PluralCategory category) {
        for (LanguageNumber e : values()) {
            if (e.getPluralCategory() == category) return e;
        }
        return null;
    }

    public static final Set<LanguageNumber> PLURAL_SET = ImmutableSet.copyOf(EnumSet.of(SINGULAR, PLURAL));
    public static final Set<LanguageNumber> SINGULAR_SET = ImmutableSet.copyOf(EnumSet.of(SINGULAR));
    public static final Set<LanguageNumber> DUAL_SET = ImmutableSet.copyOf(EnumSet.of(SINGULAR, PLURAL, DUAL));

}
