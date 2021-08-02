/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.parser;

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
        return "{\"t\":\"c\",\"an\":"+associatedNounIndex.intValue()+"}";
    }

    @Override
    public Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary) {
        return Collections.emptySet();
    }
}
