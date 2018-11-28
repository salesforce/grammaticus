/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;

/**
 * An implementation of declension of a language that doesn't use different forms for nouns.
 * <p>
 * You can think of this as "simple", but english is fairly simple.  This means "one noun form ever"
 * <p>
 * http://en.wikipedia.org/wiki/Isolating_language
 */
class SimpleDeclension extends LanguageDeclension {
    private static final Logger logger = Logger.getLogger(SimpleDeclension.class.getName());

    public SimpleDeclension(HumanLanguage language) {
        super(language);
    }

    // Nice classes that can be reused for languages with little or no inflection
    public static enum SimpleNounForm implements NounForm {
        SINGULAR;

        @Override
        public LanguageArticle getArticle() {
            return LanguageArticle.ZERO;
        }

        @Override
        public LanguageCase getCase() {
            return LanguageCase.NOMINATIVE;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return LanguagePossessive.NONE;
        }

        @Override
        public LanguageNumber getNumber() {
            return LanguageNumber.SINGULAR;
        }

        @Override
        public String getKey() {
            return "s";
        }
    }

    /**
     * Represents an simple noun with no inflection at all.
     *
     * @author stamm
     */
    public static class SimpleNoun extends Noun {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String value;

        SimpleNoun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public void makeSkinny() {
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            // TODO: Should this return "all" of them, or just the real ones?
            return Collections.singletonMap(SimpleNounForm.SINGULAR, value);
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            return value;
        }

        @Override
        public String getString(NounForm form) {
            assert form instanceof SimpleNounForm : "Why are you asking for some random noun form.  Really?";
            return value;
        }

        @Override
        protected void setString(String value, NounForm form) {
            assert form instanceof SimpleNounForm : "Why are you asking for some random noun form.  Really?";
            this.value = intern(value);
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
///CLOVER:OFF
            if (this.value == null) {
                logger.info("###\tError: The noun " + name + " has no value");
                return false;
            }
///CLOVER:ON
            return true;
        }
    }

    // All the forms you can request
    private static final List<? extends NounForm> ALL_FORMS = Collections.singletonList(SimpleNounForm.SINGULAR);
    private static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return ALL_FORMS;
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ADJECTIVE_FORMS;
    }

    @Override
    public boolean hasArticle() {
        return false;
    }

    @Override
    public boolean hasGender() {
        return false;
    }

    // no vowel
    @Override
    public boolean hasStartsWith() {
        return false;
    }

    // no plural
    @Override
    public boolean hasPlural() {
        return false;
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        return SimpleNounForm.SINGULAR;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                                          LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimpleNoun(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
    }

    // Asian simple languages do not have mixed case letters
    @Override
    public boolean hasCapitalization() {
        return false;
    }


    @Override
    public final boolean isInflected() {
        return false;  // Simple declensions have no inflection in nouns/adjectives at all
    }
}
