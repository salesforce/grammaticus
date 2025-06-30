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


/**
 * Represents a "counter word" for languages with classifiers.
 *
 * @author stamm
 * @since 1.1
 */
public class CounterRefTag extends AdnominalRefTag {
    private static final long serialVersionUID = 1L;

    CounterRefTag(NounRefTag nounTag) {
        super(nounTag);
    }

    @Override
    public String getKey() {
        return "Counter";
    }

    String getDefaultClassifier(LanguageDictionary formatter) {
        LanguageDeclension decl = formatter.getDeclension();
        return (decl instanceof LanguageDeclension.WithClassifiers)
            ? ((LanguageDeclension.WithClassifiers)decl).getDefaultClassifier() : "";
    }

    @Override
    public String toString(LanguageDictionary formatter, boolean overrideForms, Object[] vals, Renameable... entities) {
        if (getAssociatedNounRef() == null) {
            return getDefaultClassifier(formatter);
        }
        Noun n = getAssociatedNounRef().resolveNoun(formatter, entities);
        if (n == null) {
            return getDefaultClassifier(formatter);
        }
        String classifier = n.getClassifier();
        if (classifier != null) {
            return classifier;
        }
        return getDefaultClassifier(formatter);
    }

    @Override
    public CounterRefTag cloneWithResolvedNounTag(NounRefTag nounTag) {
        return new CounterRefTag(nounTag);
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
        return "{\"t\":\"c\",\"an\":" + associatedNounIndex.intValue() + "}";
    }

    @Override
    public Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary) {
        return Collections.emptySet();
    }
}
