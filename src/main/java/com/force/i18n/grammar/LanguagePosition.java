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
 * Represents the different positions of modifiers with respect to the noun.
 * This allows better tracking of which noun the adjective is modifying
 *
 * @author stamm
 */
public enum LanguagePosition {
    PRE("b", "Pre"),   // b = before
    POST("a", "Post")  // a = after
    ;

	public static final char JSON_ATTR_NAME = 'l';  // l for location, since p is posessive

    private final String dbValue;
    private final String apiValue;
    private LanguagePosition(String dbValue, String apiValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
    }
    public String getDbValue() { return this.dbValue; }
    public String getApiValue() { return this.apiValue; }
    public boolean isDefault() { return false; }
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
