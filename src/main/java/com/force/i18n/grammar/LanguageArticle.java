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
 * Represents the article (i.e. the type of reference) associated with a noun form.
 * @author stamm
 */
public enum LanguageArticle {
    ZERO ("n", "None"),
    INDEFINITE ("i", "A", "a"),
    DEFINITE ("d", "The", "the"),
    PARTITIVE ("p", "Mass", "mass"),
    ;

	public static final char JSON_ATTR_NAME = 'd';  // A is for adjective, D is for definitiveness

    private static final Map<String,LanguageArticle> dbValueMap = new HashMap<String,LanguageArticle>(8);
    private static final Map<String,LanguageArticle> labelValueMap = new HashMap<String,LanguageArticle>(16);
    static {
        for (LanguageArticle article : values()) {
            dbValueMap.put(article.getDbValue(), article);
            if (article.getLabelValue() != null) {
                // Performance optimization: Place both "The" and "the" in the map to avoid doing toLowerCase on anything
                labelValueMap.put(article.getApiValue(), article);  // The apivalue is the capitalized version of the original one.
                labelValueMap.put(article.getLabelValue(), article);
            }
        }
    }

    private final String dbValue;
    private final String apiValue;
    private final String labelValue;
    private LanguageArticle(String dbValue, String apiValue) {
        this(dbValue, apiValue, null);
    }
    private LanguageArticle(String dbValue, String apiValue, String labelValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
        this.labelValue = labelValue;
    }
    public String getDbValue() { return this.dbValue; }
    public String getApiValue() { return this.apiValue; }
    public String getLabelValue() {return this.labelValue; }
    public boolean isDefault() { return this == ZERO; }
    public static LanguageArticle fromDbValue(String dbValue) {
        return dbValueMap.get(dbValue);
    }

    public static LanguageArticle fromLabelValue(String labelValue) {
        if (labelValue == null) return null;
        return labelValueMap.get(labelValue);
    }

    public static LanguageArticle fromApiValue(String apiValue) {
        for (LanguageArticle e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }
 }
