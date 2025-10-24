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
    BENEFACTIVE("be", "Benefactive"),
    ;

	public static final char JSON_ATTR_NAME = 'c';

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
