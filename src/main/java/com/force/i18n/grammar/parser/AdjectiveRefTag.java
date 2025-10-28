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

import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;

/**
 * Represents a fully qualified adjective reference.
 *
 * @author stamm
 */
class AdjectiveRefTag extends ModifierRefTag {
    private static final long serialVersionUID = 173630704208474804L;

    // Keep the size of serialized maps down to a minimum by reusing tags.
    protected static final ConcurrentUniquefy<AdjectiveRefTag> tagMap = new ConcurrentUniquefy<>();

    protected AdjectiveRefTag(String name, NounRefTag nounTag, TermRefTag nextTerm, boolean isCapital,
            TermAttributes overrides) {
        super(name, nounTag, nextTerm, isCapital, overrides);
    }

    @Override
    protected TermType getType() {
        return TermType.Adjective;
    }

    static AdjectiveRefTag getAdjectiveRefTag(String name, NounRefTag entityTag, TermRefTag nextTerm, boolean isCapital,
            TermAttributes overrides) {
        assert name != null;
        return tagMap.unique(new AdjectiveRefTag(name, entityTag, nextTerm, isCapital, overrides));
    }

    public ModifierRefTag fixupModifier(NounRefTag nounTag, TermRefTag nextTermRef, NounForm nounForm) {
        return getAdjectiveRefTag(getName(), nounTag, nextTermRef, isCapital(),
                getDeclensionOverrides().overrideFromNounForm(nounForm));
    }

    @Override
    AdjectiveRefTag getNewModifierRef(NounRefTag entity, TermRefTag nextTerm, LanguageArticle article) {
        // if the term already has an "article" attribute, don't override with the given article param.
        TermAttributes override = getDeclensionOverrides().getArticle() == null
                ? getDeclensionOverrides().overrideArticle(article) : getDeclensionOverrides();
        return getAdjectiveRefTag(getName(), entity, nextTerm, isCapital(), override);
    }

    @Override
    protected ModifierForm getForm(LanguageDictionary formatter, NounForm associatedNounForm, Noun noun,
            GrammaticalTerm nextTerm) {
        return getDeclensionOverrides().getAdjectiveForm(formatter.getDeclension(), noun, associatedNounForm, nextTerm);
    }

    @Override
    protected NounModifier resolveModifier(LanguageDictionary dictionary) {
        return dictionary.getAdjective(getName());
    }

    @Override
    AdjectiveRefTag unique() {
        return tagMap.unique(this);
    }
}