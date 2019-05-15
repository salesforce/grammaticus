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
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;

/**
 * The declension of nouns for Hebrew
 *
 * Hebrew has only the definite article, but the grammar engine doesn't really need to know about it because
 * the article is never inflected and nothing really cares about it.  However, it can appear in the middle
 * of nouns, so the definite version is auto-derived unless otherwise specified.
 *
 * @author stamm
 */
class HebrewDeclension extends SemiticDeclension {
    public HebrewDeclension(HumanLanguage language) {
		super(language);
	}

	private static final Logger logger = Logger.getLogger(HebrewDeclension.class.getName());

    public static enum HebrewNounForm implements NounForm {
        SINGULAR(LanguageNumber.SINGULAR, LanguageArticle.ZERO),
        PLURAL(LanguageNumber.PLURAL, LanguageArticle.ZERO),
        SINGULAR_DEF(LanguageNumber.SINGULAR, LanguageArticle.DEFINITE),
        PLURAL_DEF(LanguageNumber.PLURAL, LanguageArticle.DEFINITE),
        ;
        private final LanguageNumber number;
        private final LanguageArticle article;
        HebrewNounForm(LanguageNumber number, LanguageArticle article) {
            this.number = number;
            this.article = article;
        }
        @Override public LanguageArticle getArticle() { return article; }
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getArticle().getDbValue();
        }
    }

    /**
     * Adjective form for languages that don't care about "starts with"
     */
    public static enum HebrewModifierForm implements AdjectiveForm {
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE),
        PLURAL_MASCULINE(LanguageNumber.PLURAL, LanguageGender.MASCULINE),
        PLURAL_FEMININE(LanguageNumber.PLURAL, LanguageGender.FEMININE),
        SINGULAR_MASCULINE_DEF(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageArticle.DEFINITE),
        SINGULAR_FEMININE_DEF(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageArticle.DEFINITE),
        PLURAL_MASCULINE_DEF(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageArticle.DEFINITE),
        PLURAL_FEMININE_DEF(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageArticle.DEFINITE),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private final LanguageArticle article;
        private HebrewModifierForm(LanguageNumber number, LanguageGender gender) {
            this(number, gender, LanguageArticle.ZERO);
        }
        private HebrewModifierForm(LanguageNumber number, LanguageGender gender, LanguageArticle article) {
            this.number = number;
            this.gender = gender;
            this.article = article;
        }

        @Override public LanguageArticle getArticle() { return this.article;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguageGender getGender() {return this.gender;}
        @Override public LanguageStartsWith getStartsWith() { return LanguageStartsWith.CONSONANT; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
		@Override
		public String getKey() {
			return getGender().getDbValue() + "-" + getArticle().getDbValue() + "-" + getNumber().getDbValue();
		}
		@Override
		public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
				throws IOException {
			a.append(genderVar+"+"+termFormVar+".substr(1)");
		}
    }

    private static final String DEFAULT_DEFINITE_PREFIX = "\u05d4";  // ה

    public static final class HebrewNoun extends LegacyArticledNoun {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private String singular;
        private String plural;
        private String singular_def;
        private String plural_def;

        HebrewNoun(HebrewDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender,String access,  boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public void makeSkinny() {
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return enumMapFilterNulls(HebrewNounForm.SINGULAR, singular, HebrewNounForm.PLURAL, plural, HebrewNounForm.SINGULAR_DEF, singular_def,
                    HebrewNounForm.PLURAL_DEF, plural_def);
        }
        @Override
        public String getDefaultString(boolean isPlural) {
            return isPlural ? (plural != null ? plural : singular) : singular;
        }
        @Override
        public String getExactString(NounForm form) {
            assert form instanceof HebrewNounForm : "It's not kosher to pass in a non-hebrew noun " + form;
            return form.getArticle() == LanguageArticle.DEFINITE ? form.getNumber() == LanguageNumber.PLURAL ? plural_def : singular_def
                    : form.getNumber() == LanguageNumber.PLURAL ? plural : singular;
        }
        @Override
        public void setString(String value, NounForm form) {
            if (form.getArticle() == LanguageArticle.DEFINITE) {
                if (form.getNumber().isPlural()) {
                    this.plural_def = intern(value);
                } else {
                    this.singular_def = intern(value);
                }
            } else {
                if (form.getNumber().isPlural()) {
                    this.plural = intern(value);
                } else {
                    this.singular = intern(value);
                }
            }
        }
        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            if (this.singular == null) {
                logger.info("###\tError: The noun " + name + " has no singular form");
                return false;
            }
            // Default the values for entity nouns, but not for others to make rename fields more specific.
            if (getNounType() == NounType.ENTITY) {
                if (this.plural == null)
                 {
                    this.plural = this.singular;  // Default plural to singular.
                }
                // Default the singular/plural definitions to start
                if (this.singular_def == null) {
                    this.singular_def = DEFAULT_DEFINITE_PREFIX + this.singular;
                }
                if (this.plural_def == null) {
                    this.plural_def = DEFAULT_DEFINITE_PREFIX + this.plural;
                }
            }
            return true;
        }
    }

    protected static class HebrewAdjective extends Adjective {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		// The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<HebrewModifierForm,String> values = new EnumMap<HebrewModifierForm,String>(HebrewModifierForm.class);
        HebrewAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }
        @Override
        public Map< ? extends AdjectiveForm, String> getAllValues() {
            return values;
        }
        @Override
        public String getString(AdjectiveForm form) {
            return values.get(form);
        }
        @Override
        protected void setString(AdjectiveForm form, String value) {
            assert form instanceof HebrewModifierForm : "Enough of this mishegas, ask only for hebrew";
            values.put((HebrewModifierForm)form, intern(value));
        }
        @Override
        protected String deriveDefaultString(AdjectiveForm form, String value, AdjectiveForm baseFormed) {
            if (form.getArticle() == LanguageArticle.DEFINITE && baseFormed.getArticle() != LanguageArticle.DEFINITE) {
                return DEFAULT_DEFINITE_PREFIX + value;
            }
            return value;
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, REQUIRED_ADJECTIVE_FORMS);
        }
    }

    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(HebrewNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(HebrewNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = ImmutableList.copyOf(EnumSet.allOf(HebrewModifierForm.class));
    // All the forms that have to be specified in the File
    static final Set<? extends AdjectiveForm> REQUIRED_ADJECTIVE_FORMS = EnumSet.of(HebrewModifierForm.PLURAL_FEMININE, HebrewModifierForm.PLURAL_MASCULINE, HebrewModifierForm.SINGULAR_FEMININE, HebrewModifierForm.SINGULAR_MASCULINE);

    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new HebrewAdjective(this, name, position);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
            LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new HebrewNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return ADJECTIVE_FORMS;
    }

    @Override
    protected String getDefiniteArticlePrefix(LanguageStartsWith startsWith) {
        return DEFAULT_DEFINITE_PREFIX;
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return OTHER_FORMS;
    }
}
