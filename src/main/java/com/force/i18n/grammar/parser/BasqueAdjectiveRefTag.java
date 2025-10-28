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

import com.force.i18n.HumanLanguage;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageCase;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.LanguageNumber;
import com.force.i18n.grammar.ModifierForm;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.NounForm;
import com.force.i18n.grammar.NounModifier;
import com.force.i18n.grammar.impl.BasqueDeclension;

/**
 * Basque-specific adjective tag that prefers explicit forms but can synthesize a surface from the base adjective when
 * an explicit form is not provided. Agreement (number, case, article) is taken only from overrides on this tag;
 * otherwise the declension defaults are used.
 *
 * If an explicit value equals the base form under default attributes, it is treated as redundant and the surface is
 * synthesized instead.
 */
class BasqueAdjectiveRefTag extends AdjectiveRefTag {
    private static final long serialVersionUID = 1L;

    protected BasqueAdjectiveRefTag(String name, NounRefTag nounTag, TermRefTag nextTerm, boolean isCapital, TermAttributes overrides) {
        super(name, nounTag, nextTerm, isCapital, overrides);
    }

    // Keep the size of serialized maps down by reusing tags, and isolate pooling from the superclass.
    // Note: equality/hashCode for ref tags do not include the concrete class, so sharing the superclass pool could
    // return an AdjectiveRefTag for a BasqueAdjectiveRefTag key (and vice versa), losing Basque-specific behavior.
    //Maintain a separate pool and override unique() to preserve type.
    protected static final ConcurrentUniquefy<AdjectiveRefTag> tagMap = new ConcurrentUniquefy<>();

    static AdjectiveRefTag getAdjectiveRefTag(String name, NounRefTag entityTag, TermRefTag nextTerm, boolean isCapital, TermAttributes overrides) {
        return tagMap.unique(new BasqueAdjectiveRefTag(name, entityTag, nextTerm, isCapital, overrides));
    }

    @Override
    protected ModifierForm getForm(LanguageDictionary formatter, NounForm associatedNounForm, Noun noun, GrammaticalTerm nextTerm) {
        // For Basque adjectives, derive the form strictly from the adjective's own overrides
        // (authors explicitly provide case/plural/article on the adjective tag),
        // falling back to declension defaults when not provided.
        LanguageDeclension decl = formatter.getDeclension();
        LanguageNumber num = getDeclensionOverrides().getNumber() != null ? getDeclensionOverrides().getNumber() : LanguageNumber.SINGULAR;
        LanguageCase kase = getDeclensionOverrides().getCase() != null ? getDeclensionOverrides().getCase() : decl.getDefaultCase();
        LanguageArticle article = getDeclensionOverrides().getArticle() != null ? getDeclensionOverrides().getArticle() : decl.getDefaultArticle();
        return decl.getAdjectiveForm(decl.getDefaultStartsWith(), decl.getDefaultGender(), num, kase, article, decl.getDefaultPossessive());
    }

    @Override
    public String toString(LanguageDictionary formatter, boolean overrideForms, Object[] vals, Renameable... entities) {
        NounModifier modifier = resolveModifier(formatter);
        if (modifier == null && overrideForms) return "";
        if (getAssociatedNounRef() == null) return "";

        AdjContext ctx = buildAdjContext(formatter, overrideForms, entities);
        RequestedAttrs attrs = resolveRequestedAttrs(formatter);

        String explicit = resolveExplicitOrNull(ctx.modifier, ctx.adjForm, getName(), formatter, attrs);
        if (explicit != null) return applyCasing(formatter.getLanguage(), isCapital(), explicit);

        String base = ctx.modifier != null ? ctx.modifier.getDefaultValue() : null;
        if (base == null) return "";

        String synthesized = synthesizeFromBase(formatter, base, attrs);
        return applyCasing(formatter.getLanguage(), isCapital(), synthesized);
    }

    @Override
    AdjectiveRefTag getNewModifierRef(NounRefTag entity, TermRefTag nextTerm, LanguageArticle article) {
        TermAttributes override = getDeclensionOverrides().getArticle() == null ? getDeclensionOverrides().overrideArticle(article) : getDeclensionOverrides();
        return getAdjectiveRefTag(getName(), entity, nextTerm, isCapital(), override);
    }

    @Override
    AdjectiveRefTag unique() {
        return tagMap.unique(this);
    }

    private static final class AdjContext {
        final NounModifier modifier;
        final ModifierForm adjForm;
        AdjContext(NounModifier modifier, ModifierForm adjForm) {
            this.modifier = modifier;
            this.adjForm = adjForm;
        }
    }

    private static final class RequestedAttrs {
        final LanguageArticle article;
        final LanguageCase kase;
        final LanguageNumber number;
        RequestedAttrs(LanguageArticle article, LanguageCase kase, LanguageNumber number) {
            this.article = article;
            this.kase = kase;
            this.number = number;
        }
    }

    private AdjContext buildAdjContext(LanguageDictionary formatter, boolean overrideForms, Renameable... entities) {
        NounModifier modifier = resolveModifier(formatter);
        NounForm nounForm = getAssociatedNounRef().getForm(formatter, overrideForms);
        Noun noun = getAssociatedNounRef().resolveNoun(formatter, entities);
        GrammaticalTerm nextTerm = noun;
        ModifierForm adjForm = getForm(formatter, nounForm, noun, nextTerm);
        return new AdjContext(modifier, adjForm);
    }

    private RequestedAttrs resolveRequestedAttrs(LanguageDictionary formatter) {
        LanguageDeclension decl = formatter.getDeclension();

        LanguageArticle article = getDeclensionOverrides().getArticle();
        if (article == null) article = decl.getDefaultArticle();

        LanguageCase kase = getDeclensionOverrides().getCase() != null ? getDeclensionOverrides().getCase() : decl.getDefaultCase();
        LanguageNumber number = getDeclensionOverrides().getNumber() != null ? getDeclensionOverrides().getNumber() : LanguageNumber.SINGULAR;
        return new RequestedAttrs(article, kase, number);
    }

    private String resolveExplicitOrNull(NounModifier modifier, ModifierForm adjForm, String tagName,
            LanguageDictionary formatter, RequestedAttrs attrs) {
        String explicit = modifier != null ? modifier.getString(adjForm) : null;
        if (explicit != null) {
            String folded = formatter.getLanguage().toFoldedCase(explicit);
            if (folded.equals(tagName)) {
                explicit = null;
            }
        }
        if (explicit == null) return null;

        boolean isDefaultAttrs = attrs.kase == formatter.getDeclension().getDefaultCase()
                && attrs.number == LanguageNumber.SINGULAR
                && attrs.article == formatter.getDeclension().getDefaultArticle();

        String baseDefault = modifier.getDefaultValue();
        boolean explicitIsBase = baseDefault != null
                && formatter.getLanguage().toFoldedCase(baseDefault).equals(formatter.getLanguage().toFoldedCase(explicit));
        if (!explicitIsBase || isDefaultAttrs) {
            return explicit;
        }
        return null;
    }

    private String synthesizeFromBase(LanguageDictionary formatter, String base, RequestedAttrs attrs) {
        BasqueDeclension bd = (BasqueDeclension)formatter.getDeclension();
        NounModifier mod = resolveModifier(formatter);

        int flags = -1;
        if (mod instanceof BasqueDeclension.BasqueAdjective ba) {
            flags = ba.getBasqueStemFlags();
        }
        return flags >= 0
            ? bd.renderSurfaceWithFlags(base, attrs.kase, attrs.number, attrs.article, flags)
            : bd.renderSurface(base, attrs.kase, attrs.number, attrs.article);
    }

    private String applyCasing(HumanLanguage language, boolean isCapital, String value) {
        return isCapital ? value : language.toFoldedCase(value);
    }
}
