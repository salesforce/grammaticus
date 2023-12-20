/*
 * Copyright (c) 2023, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import com.force.i18n.grammar.Noun.NounType;

import com.force.i18n.grammar.impl.SimpleDeclension.SimpleNounWithClassifier;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;

/**
 * Declension for Chuj Mayan language. General rules as following,
 *
 */

class ChujDeclension extends ArticledDeclension {
    
    // All the forms you can request
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);
    
    public ChujDeclension(HumanLanguage language) {
        super(language);
    }
    
    //TODO Might need to introduce ChujModifierForm enum and replace SimpleModifierForm throught ChujDeclension
    /*
    public enum ChujModifierForm {
        //TODO Complete this enum        
    }*/
    
    protected static class ChujArticle extends Article {
        private static final long serialVersionUID = 1L;

        //The "keys" here are StartsWith, Gender, and Plurality(number)
        EnumMap<SimpleModifierForm, String> values = new EnumMap<>( SimpleModifierForm.class);

        ChujArticle(ChujDeclension declension, String name, LanguageArticle articleType) {
            super(declension, name, articleType);
        }

        @Override
        public Map<? extends ArticleForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(ArticleForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(ArticleForm form, String value) {
            assert form instanceof SimpleModifierForm : "It's not a supported article form for Chuj.";
            values.put((SimpleModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return true; //TODO
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }
    
    static class ChujNoun extends SimpleNounWithClassifier {
        private static final long serialVersionUID = 1L;

        public ChujNoun(LanguageGender gender, LanguageDeclension declension, String name, String pluralAlias, NounType type,
                String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField,
                boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopiedFromDefault);
            setGender(gender);
            //TODO validateGender
        }

    }
    
    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        //TODO Verify
        throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
    }
    
    @Override
    public List< ? extends ArticleForm> getArticleForms() {
        //TODO Verify
        return Collections.singletonList(SimpleModifierForm.SINGULAR);
    }
    
    @Override
    public final boolean hasClassifiers() {
        return true;
    }
    
    /*
    @Override
    public final boolean hasArticle() {
        return true;
    }*/
    
    @Override
    public final boolean hasStartsWith() {
        //TODO Verify       
        return false;
    }
    
    @Override
    public final boolean hasGender() {
        return true;
    }
        
    @Override
    public LanguageGender getDefaultGender() {
        //TODO Verify
        return LanguageGender.MAYAN_FEMALE;
    }
    
    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new ChujArticle(this, name, articleType);
    }
    
    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return null; //TODO return new ChujAdjective(this, name, startsWith, position);
    }
    
    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new ChujNoun(null, //TODO What can be passed instead of null as Gender?
                this, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopied);
    }
    
    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ADJECTIVE_FORMS;
    }
    
    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return OTHER_FORMS;
    }
    
    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return getAllNounForms();
    }
    
    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }
    
    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return ALL_FORMS;
    }
}