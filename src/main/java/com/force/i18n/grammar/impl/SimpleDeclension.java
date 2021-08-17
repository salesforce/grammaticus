/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageConstants;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;
/**
 * An implementation of declension of a language that doesn't use different forms for nouns.
 *
 * You can think of this as "simple", but english is fairly simple.  This means "one noun form ever"
 *
 * http://en.wikipedia.org/wiki/Isolating_language
 */
class SimpleDeclension extends AbstractLanguageDeclension {
    private static final Logger logger = Logger.getLogger(SimpleDeclension.class.getName());

    public SimpleDeclension(HumanLanguage language) {
    	super(language);
    }

    // Nice classes that can be reused for languages with little or no inflection
    public static enum SimpleNounForm implements NounForm {
        SINGULAR
        ;

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override public LanguageNumber getNumber() {return LanguageNumber.SINGULAR;}
        @Override
        public String getKey() {
            return "s";
        }
    }

    /**
     * Represents an simple noun with no inflection at all.
     * @author stamm
     */
    public static class SimpleNoun extends Noun {
        private static final long serialVersionUID = 1L;
        protected String value;

        SimpleNoun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            this(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, access, isStandardField, isCopiedFromDefault);
        }

        SimpleNoun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
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
    static final List<? extends NounForm> ALL_FORMS = Collections.singletonList(SimpleNounForm.SINGULAR);
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);

    @Override
    public List< ? extends NounForm> getAllNounForms() {
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
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
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
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
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

    @Override
    public void writeJsonOverrides(Appendable a, String instance) throws IOException {
        super.writeJsonOverrides(a, instance);
        if (!this.hasCapitalization()) a.append(instance).append(".dont_capitalize=true;");
    }

    /**
     * Use this for an uninflected language, but with classifier words
     * @author stamm
     * @since 0.6.0
     */
    static class SimpleDeclensionWithClassifiers extends SimpleDeclension implements LanguageDeclension.WithClassifiers {
        private final String defaultClassifier;

        public SimpleDeclensionWithClassifiers(HumanLanguage language) {
            this(language, getDefaultClassifier(language));
        }


        public SimpleDeclensionWithClassifiers(HumanLanguage language, String defaultClassifier) {
            super(language);
            this.defaultClassifier = defaultClassifier;
        }

        @Override
        public final boolean hasClassifiers() {
            return true;
        }

        @Override
        public String getDefaultClassifier() {
            return defaultClassifier;
        }

        @Override
        public Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
                LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField,
                boolean isCopied) {
            return new SimpleNounWithClassifier(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
        }
    }

    /**
     * Vietnamese declension that is similar to SimpleDeclention, but has plural form and classifiers.
     *
     * @author yoikawa
     * @since 138
     */
    static class VietnameseDeclension extends SimpleDeclensionWithClassifiers {
        static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));

        public VietnameseDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public boolean hasCapitalization() {
            return true;
        }

        @Override
        public boolean hasPlural() {
            return true;
        }

        @Override
        public List<? extends NounForm> getAllNounForms() { return ALL_FORMS; }

        @Override
        public Collection<? extends NounForm> getEntityForms() { return ALL_FORMS; }

        @Override
        public Collection<? extends NounForm> getFieldForms() { return ALL_FORMS; }

        @Override
        public Collection<? extends NounForm> getOtherForms() { return ALL_FORMS; }

        @Override
        public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
                LanguageArticle article) {
            return LanguageNumber.SINGULAR == number ? PluralNounForm.SINGULAR : PluralNounForm.PLURAL;
        }

        @Override
        public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            return new PluralNounWithClassifier(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
        }

    }

    static String getDefaultClassifier(HumanLanguage language) {
        switch (language.getLocale().getLanguage()) {
        case LanguageConstants.JAPANESE:
            return "つ"; // つ
        case LanguageConstants.CHINESE:
            switch (language.getLocaleString()) {
            case LanguageConstants.CHINESE_TW:
            case LanguageConstants.CHINESE_HK:
                return "個";  // Traditional: 個
            }
            return "个";  // Simplified: 个
        case LanguageConstants.KOREAN:
            return "개";  // 개

        // These languages have classifiers, but aren't supported in grammaticus yet.
        case LanguageConstants.VIETNAMESE:
            return "cái";  // cái: This isn't super generic
        case LanguageConstants.BENGALI:
            return "\u099f\u09be";  // টা
        case LanguageConstants.MALAY:
        case LanguageConstants.INDONESIAN:
            return "buah";  // Often found in compound with 'se-'
        }
        return "";
    }

    /**
     * Use this for uninflected nouns, but with classifier words
     * @author stamm
     * @since 0.6.0
     */
    static class SimpleNounWithClassifier extends SimpleNoun implements Noun.WithClassifier {
        private static final long serialVersionUID = 1L;
        private String classifier;

        public SimpleNounWithClassifier(LanguageDeclension declension, String name, String pluralAlias, NounType type,
                String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField,
                boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopiedFromDefault);
        }

        public SimpleNounWithClassifier(LanguageDeclension declension, String name, String pluralAlias, NounType type,
                String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, access, isStandardField, isCopiedFromDefault);
        }


        @Override
        public String getClassifier() {
            return this.classifier;
        }

        @Override
        public void setClassifier(String classifier) {
            this.classifier = classifier;
        }
    }

    static class PluralNounWithClassifier extends SimpleNounWithClassifier {
        private static final long serialVersionUID = 1L;
        private String plural;

        public PluralNounWithClassifier(LanguageDeclension declension, String name, String pluralAlias, NounType type,
                String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return enumMapFilterNulls(PluralNounForm.SINGULAR, value, PluralNounForm.PLURAL, plural);
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            return isPlural && plural != null ? plural : value;
        }

        @Override
        public String getString(NounForm form) {
            assert form instanceof PluralNounForm;
            return getDefaultString(form.getNumber().isPlural());
        }

        @Override
        protected void setString(String v, NounForm form) {
            assert form instanceof PluralNounForm;
            v = intern(v);
            if (form.getNumber().isPlural()) {
                this.plural = v;
                if (v != null && v.equals(this.value)) {
                    this.value = v; // Keep one reference for serialization
                }
            } else {
                this.value = v;
                if (v != null && v.equals(this.plural)) {
                    this.plural = v; // Keep one reference for serialization
                }
            }
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            if (this.value == null) {
                logger.info("###\tError: The noun " + name + " has no singular form");
                return false;
            }
            return true;
        }
    }
}
