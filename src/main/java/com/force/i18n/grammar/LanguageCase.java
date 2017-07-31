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
 * Represents the Grammatical case of a grammatical term
 * @author yoikawa,stamm
 */
public enum LanguageCase {
    NOMINATIVE ("n", "Nominative"),
    ACCUSATIVE ("a", "Accusative"),
    GENITIVE ("g", "Genitive"),
    DATIVE ("d", "Dative"),
    INESSIVE ("ines", "Inessive"),
    ELATIVE ("el", "Elative"),
    ILLATIVE ("il", "Illative"),
    ADESSIVE ("ad", "Adessive"),
    ABLATIVE ("abl", "Ablative"),
    ALLATIVE ("al", "Allative"),
    ESSIVE ("es", "Essive"),
    TRANSLATIVE ("tra", "Translative"),
    PARTITIVE ("par", "Partitive"),
    OBJECTIVE ("o", "Objective"),
    SUBJECTIVE ("s", "Subjective"),
    INSTRUMENTAL ("in", "Instrumental"),
    PREPOSITIONAL ("pr", "Prepositional"),
    LOCATIVE ("l", "Locative"),
    VOCATIVE ("v", "Vocative"),
    SUBLATIVE ("sub", "Sublative"),
    SUPERESSIVE ("sup", "Superessive"),
    DELATIVE ("del", "Delative"),
    CAUSALFINAL ("cf", "Causalfinal"),
    ESSIVEFORMAL ("ef", "Essiveformal"),
    TERMINATIVE ("t", "Termanative"),  // Note, the API name is Termanative, which is a typo
    DISTRIBUTIVE ("di", "Distributive"),
    ERGATIVE ("er", "Ergative"),
    ADVERBIAL ("adv", "Adverbial"),
    ABESSIVE("abe", "Abessive"),
    COMITATIVE("com", "Comitative"),
    ;

    private static final Map<String,LanguageCase> dbValueMap = new HashMap<String,LanguageCase>(64);
    static {
        for (LanguageCase caseType : values()) {
            dbValueMap.put(caseType.getDbValue(), caseType);
        }
    }

    private final String dbValue;
    private final String apiValue;
    private LanguageCase(String dbValue, String apiValue) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
    }
    public String getDbValue() { return this.dbValue; }
    public String getApiValue() { return this.apiValue; }
    public boolean isDefault() { return this == NOMINATIVE; }

    public static LanguageCase fromDbValue(String dbValue) {
        if (dbValue == null) return null;
        return dbValueMap.get(dbValue);
    }

    public static LanguageCase fromApiValue(String apiValue) {
        for (LanguageCase e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }
}
