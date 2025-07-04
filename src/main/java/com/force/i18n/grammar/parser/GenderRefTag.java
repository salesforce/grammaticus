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

package com.force.i18n.grammar.parser;

import static java.util.Objects.requireNonNull;

import java.util.*;

import com.force.i18n.Renameable;
import com.force.i18n.grammar.*;
import com.google.common.collect.Sets;

/**
 * Represents a gender choice to allow translators to place gendered version of text without needing
 * agreement for use in languages where the verb should match the gender of the noun, such as in
 * Arabic and Hebrew
 * @author stamm
 * @since 1.1
 */
public class GenderRefTag extends AdnominalRefTag {
    private static final long serialVersionUID = 1L;

    private final Map<LanguageGender, Object> when;
    private final Object ifDefault;

    GenderRefTag(NounRefTag nounTag, Map<LanguageGender, Object> when, Object ifDefault) {
        super(nounTag);
        this.when = when;
        this.ifDefault = ifDefault;
    }

    @Override
    public String getKey() {
        return "Gender:" + when;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), when, ifDefault);
    }

    @Override
    public boolean equals(Object obj) {
            return super.equals(obj)
                && Objects.equals(when, ((GenderRefTag)obj).when)
                && Objects.equals(ifDefault, ((GenderRefTag)obj).ifDefault);
    }

    Object getData(LanguageDictionary dictionary, Renameable[] entities) {
        if (getAssociatedNounRef() == null) {
            return ifDefault;
        }
        Noun n = getAssociatedNounRef().resolveNoun(dictionary, entities);
        if (n == null) {
            return ifDefault;
        }
        Object val = when.get(n.getGender());
        return val != null ? val : ifDefault;
    }

    @Override
    public String toString(LanguageDictionary formatter, boolean overrideForms, Object[] vals, Renameable... entities) {
        Object data = getData(formatter, entities);
        if (data == null) {
            return "";
        }

        return formatter.format(getData(formatter, entities), entities, vals, overrideForms, false);
    }

    @Override
    public GenderRefTag cloneWithResolvedNounTag(NounRefTag nounTag) {
        return new GenderRefTag(nounTag, this.when, this.ifDefault);
    }

    @Override
    public Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary) {
        Set<GrammaticalTerm> whens = RefTag.getTermsFromLabels(dictionary, when.values());
        return ifDefault != null ? Sets.union(whens, RefTag.getTermsFromLabelValue(dictionary, ifDefault)) : whens;
    }

    @Override
    public String toJson(LanguageDictionary dictionary, List<?> list) {
        // Get associated noun ref
        Integer associatedNounIndex = null;
        for (int i = 0; i < list.size(); i++) {
            Object term = list.get(i);
            if (term != null && term.equals(getAssociatedNounRef())) {
                associatedNounIndex = i;
            }
        }
        requireNonNull(associatedNounIndex);

        StringBuilder def = new StringBuilder();
        RefTag.appendJsonLabelValueNoThrow(dictionary, def, ifDefault, null);
        StringBuilder forms = new StringBuilder();
        when.entrySet().stream().forEach(e -> {
            forms.append("\"" + e.getKey().getDbValue() + "\":");
            RefTag.appendJsonLabelValueNoThrow(dictionary, forms, e.getValue(), null);
        });
        return "{\"t\":\"g\",\"an\":" + associatedNounIndex.intValue() + ",\"def\":" + def.toString() + ",\"v\":{" + forms.toString() + "}}";
    }
}
