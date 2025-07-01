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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the gender or noun class of an *object* associated with a noun.
 *
 * Generally, the cloud
 * @author stamm
 */
public enum LanguageGender {
    NEUTER ("n", "Neuter"),
    FEMININE ("f", "Feminine", "c", "e"),   // Dutch = "c", Swedish = "e"
    MASCULINE ("m", "Masculine"),
    ANIMATE_MASCULINE ("a", "AnimateMasculine"), //West Slavic

    // Bantu Noun Classes.  Swahili noun classes available as aliases
    // In isiXhosa & isiZulu, class I and Ia, while different for nouns, are the same for adjectives, so are unmarked.
    // Note: In other Bantu languages, like Tswana, the numbering is different (The plurals are part of the class, not a separate one)
    // That may cause confusion
    CLASS_I ("1", "ClassI", "M-wa", "I"), // Class I/II M-Wa
    CLASS_III("3", "ClassIII", "M-mi", "III"), // Class III/IV M-Mi
    CLASS_V("5", "ClassV", "Ma", "V"), // Class V/VI Ma
    CLASS_VII("7", "ClassVII", "Ki-vi", "VII"), // Class VII/VIII Ki-vi
    CLASS_IX("9", "ClassIX", "N", "IX"), // Class IX/X N
    CLASS_XI("U", "ClassXI", "XI"), // Class XI/XII U Class.
    CLASS_XIV("B", "ClassXIV", "XIV"), // Class XIV (Zulu/Xhosa ubu-)
    CLASS_XV("S", "ClassXV", "XV"), // Class XV (Zulu/Xhosa uku-)
    CLASS_XVI("P", "ClassXVI", "Pa", "XVI"), // Class XVI Pa Class
    CLASS_XVII("K", "ClassXVII", "Ku", "XVII"), // Class XVII Ku Class
    CLASS_XVIII("M", "ClassXVIII", "Mu", "XVIII"), // Class XVIII Mu Class
    ;

	public static final char JSON_ATTR_NAME = 'g';

    private static final Map<String,LanguageGender> dbValueMap = new HashMap<String,LanguageGender>(8);
    private static final Map<String,LanguageGender> labelValueMap = new HashMap<String,LanguageGender>(16);
    static {
        for (LanguageGender gender : values()) {
            dbValueMap.put(gender.getDbValue(), gender);
            labelValueMap.put(gender.getDbValue(), gender);
            if (gender.aliases != null) {
                for (String alias : gender.aliases) {
                    LanguageGender old = labelValueMap.put(alias, gender);
                    assert old == null;
                }
            }
        }
    }

    private final String dbValue;
    private final String apiValue;
    private final String[] aliases;
    private LanguageGender(String dbValue, String apiValue, String... aliases) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
        this.aliases = aliases;
    }
    public String getDbValue() { return this.dbValue; }
    public String getApiValue() { return this.apiValue; }
    public boolean isDefault() { return false; }
    public static LanguageGender fromDbValue(String dbValue) {
        return dbValueMap.get(dbValue);
    }
    public List<String> getAliases() {
        return Collections.unmodifiableList(Arrays.asList(this.aliases));
    }

    public static LanguageGender fromLabelValue(String labelValue) {
        if (labelValue == null) return null;
        return labelValueMap.get(labelValue);
    }

    public static LanguageGender fromApiValue(String apiValue) {
        for (LanguageGender e : values()) {
            if (e.getApiValue().equals(apiValue)) return e;
        }
        return null;
    }

    /*
     * Alias definitions
     */
    /**
     * Swedish uses EUTER as a gender, which isn't feminine, but we store it as such
     */
    public static final LanguageGender EUTER = FEMININE;
    /**
     * Dutch has "de" and "het" words, het is netuer and "de" is either masculine or feminine; but we use feminine.
     */
    public static final LanguageGender COMMON = FEMININE;
}
