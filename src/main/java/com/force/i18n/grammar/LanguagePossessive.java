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

import java.util.HashMap;
import java.util.Map;


/**
 * Represents possession associated with a noun form
 * @author stamm
 */
public enum LanguagePossessive {
    NONE ("n", "None"),
    FIRST ("f", "FirstPerson"),
    SECOND ("s", "SecondPerson"),
    FIRST_PLURAL ("F", "FirstPersonPlural", "fpl"),
    SECOND_PLURAL ("S", "SecondPersonPlural", "spl"),
    ;

	public static final char JSON_ATTR_NAME = 'p';

    private static final Map<String,LanguagePossessive> dbValueMap = new HashMap<String,LanguagePossessive>(64);
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
    public String getDbValue() { return this.dbValue; }
    public String getApiValue() { return this.apiValue; }
    public String getLabelValue() { return this.labelValue == null ? getDbValue() : this.labelValue; }
    public boolean isDefault() { return this == NONE; }
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
