/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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

    private AdjectiveRefTag(String name, NounRefTag nounTag, TermRefTag nextTerm, boolean isCapital,
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