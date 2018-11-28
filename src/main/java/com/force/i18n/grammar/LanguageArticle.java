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
 * Represents the article (i.e. the type of reference) associated with a noun form.
 *
 * @author stamm
 */
public enum LanguageArticle {
    ZERO("n", "None"),
    INDEFINITE("i", "A", "a"),
    DEFINITE("d", "The", "the"),
    PARTITIVE("p", "Mass", "mass"),
    ;

    private static final Map<String, LanguageArticle> dbValueMap = new HashMap<String, LanguageArticle>(8);
    private static final Map<String, LanguageArticle> labelValueMap = new HashMap<String, LanguageArticle>(16);

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

    public String getDbValue() {
        return this.dbValue;
    }

    public String getApiValue() {
        return this.apiValue;
    }

    public String getLabelValue() {
        return this.labelValue;
    }

    public boolean isDefault() {
        return this == ZERO;
    }

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
