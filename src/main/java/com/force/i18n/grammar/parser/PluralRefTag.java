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

import java.util.*;

import com.force.i18n.PluralCategory;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.LanguageDictionary;
import com.google.common.collect.Sets;

/**
 * Plural Tag reference implementation.
 * Constructed through LabelHandler, and used by LabelInfo.
 * <p>
 * example:
 * <pre>
 * &lt;plural num="0"&gt;&lt;when val="..."/&gt;...&lt;/plural&gt;
 * </pre>
 * @author stamm
 */
public class PluralRefTag extends RefTag {
    private static final long serialVersionUID = -1L;

    private final int val;
    private final Map<PluralCategory, Object> when;
    private final Object ifDefault;

    public PluralRefTag(int val, Map<PluralCategory, Object> when, Object ifDefault) {
        this.val = val;
        this.when = when;
        this.ifDefault = ifDefault != null ? ifDefault : "";
    }

    @Override
    public String getKey() {
        return "Plural" + val + when;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), val, when, ifDefault);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return Objects.equals(val, ((PluralRefTag)obj).val)
                && Objects.equals(when, ((PluralRefTag)obj).when)
                && Objects.equals(ifDefault, ((PluralRefTag)obj).ifDefault);
    }

    Object getData(LanguageDictionary dictionary, Object[] vals) {
        if (vals == null || vals.length <= val) {
            return ifDefault;
        }
        Object toTest = vals[val];
        Number num;
        if (toTest == null) {
            return ifDefault;
        }
        if (toTest instanceof Number) {
            num = (Number) toTest;
        } else {
            try {
                num = Double.parseDouble(String.valueOf(toTest));
            } catch (NumberFormatException ex) {
                return ifDefault;
            }
        }
        // Zero isn't returned for plural rules in CLDR, but you might want it anyway.
        PluralCategory category = (num.doubleValue() == 0.0 && when.containsKey(PluralCategory.ZERO))
                ? PluralCategory.ZERO
                : dictionary.getDeclension().getPluralRules().getPluralCategory(num);
        Object val = when.get(category);
        return val != null ? val : ifDefault;
    }

    @Override
    public String toString(LanguageDictionary dictionary, boolean overrideForms, Object[] vals,
            Renameable... entities) {
        // Don't format for message format, since it'll do it again later.
        return dictionary.format(getData(dictionary, vals), entities, vals, overrideForms, false);
    }

    @Override
    public Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary) {
        Set<GrammaticalTerm> whens = RefTag.getTermsFromLabels(dictionary, when.values());
        return ifDefault != null ? Sets.union(whens, RefTag.getTermsFromLabelValue(dictionary, ifDefault)) : whens;
    }

    @Override
    public String toJson(LanguageDictionary dictionary, List<?> list) {
        StringBuilder def = new StringBuilder();
        RefTag.appendJsonLabelValueNoThrow(dictionary, def, ifDefault, null);
        StringBuilder forms = new StringBuilder();
        when.entrySet().stream().forEach(e -> {
            forms.append(",\"" + e.getKey().getCldrCategory() + "\":");
            RefTag.appendJsonLabelValueNoThrow(dictionary, forms, e.getValue(), null);
        });
        String formsVal = forms.length() > 2 ? forms.substring(1) : "";
        return "{\"t\":\"p\",\"i\":" + val + ",\"def\":" + def.toString() + ",\"v\":{" + formsVal + "}}";
    }
}
