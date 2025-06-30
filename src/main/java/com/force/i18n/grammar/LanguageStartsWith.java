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
