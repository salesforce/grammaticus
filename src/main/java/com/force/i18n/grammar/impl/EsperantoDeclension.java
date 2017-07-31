/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;

/**
 * Fake language used for internal translation.  It appears as a standard language, and reuses whatever code it can.
 * @author stamm
 */
public class EsperantoDeclension extends ArticledDeclension {
	// All the forms you can request
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);
    static final List<? extends ArticleForm> ARTICLE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);

    public EsperantoDeclension(HumanLanguage language) {
		super(language);
	}

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return OTHER_FORMS;
    }

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return ADJECTIVE_FORMS;
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return ARTICLE_FORMS;
    }

    @Override
    public boolean hasGender() {
        return false;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    protected Article createArticle(String name, LanguageArticle articleType) {
        return new SimpleArticle(this, name, articleType);
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        switch (articleType) {
        case DEFINITE: return "La";
        default: return null;
        }
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase case1) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase case1, LanguageArticle article, LanguagePossessive possessive) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimpleArticledPluralNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
    }

}
