/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;

/**
 * Represents the declension of Hindi or Urdu, Indo-Iranian languages which
 * have case, gender, number, and complicated post-positions that attach to the noun
 * aggutinatively.
 *
 * We support two of each kind of value to support 4 noun and 8 adjective forms.
 *
 * NOTE: We are reusing Nominative and Objective to represent Direct and Oblique,
 *
 * @author stamm
 */
class HindiUrduDeclension extends LanguageDeclension {
    private static final LanguageCase DIRECT_CASE = LanguageCase.NOMINATIVE;
    private static final LanguageCase OBLIQUE_CASE = LanguageCase.OBJECTIVE;

    private static EnumSet<LanguageGender> GENDER_TYPES = EnumSet.of(LanguageGender.FEMININE, LanguageGender.MASCULINE);

    HindiUrduDeclension(HumanLanguage language) {
    	super(language);
    }

    /**
     * Adjective form for languages that don't care about "starts with"
     */
    public static enum HindiUrduModifierForm implements AdjectiveForm {
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, DIRECT_CASE),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE, DIRECT_CASE),
        PLURAL_MASCULINE(LanguageNumber.PLURAL, LanguageGender.MASCULINE, DIRECT_CASE),
        PLURAL_FEMININE(LanguageNumber.PLURAL, LanguageGender.FEMININE, DIRECT_CASE),
        SINGULAR_MASCULINE_O(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, OBLIQUE_CASE),
        SINGULAR_FEMININE_O(LanguageNumber.SINGULAR, LanguageGender.FEMININE, OBLIQUE_CASE),
        PLURAL_MASCULINE_O(LanguageNumber.PLURAL, LanguageGender.MASCULINE, OBLIQUE_CASE),
        PLURAL_FEMININE_O(LanguageNumber.PLURAL, LanguageGender.FEMININE, OBLIQUE_CASE),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private final LanguageCase caseType;
        private HindiUrduModifierForm(LanguageNumber number, LanguageGender gender, LanguageCase caseType) {
            this.number = number;
            this.gender = gender;
            this.caseType = caseType;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return this.caseType; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguageGender getGender() {return this.gender;}
        @Override public LanguageStartsWith getStartsWith() { return LanguageStartsWith.CONSONANT; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
    }

    public static enum HindiUrduNounForm implements NounForm {
        SINGULAR(LanguageNumber.SINGULAR, DIRECT_CASE),
        SINGULAR_OBL(LanguageNumber.SINGULAR, OBLIQUE_CASE),
        PLURAL(LanguageNumber.PLURAL, DIRECT_CASE),
        PLURAL_OBL(LanguageNumber.PLURAL, OBLIQUE_CASE),
        ;

        private final LanguageNumber number;
        private final LanguageCase caseType;
        private HindiUrduNounForm(LanguageNumber number, LanguageCase caseType) {
            this.number = number;
            this.caseType = caseType;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return this.caseType; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue();
        }
    }

    public static final class HindiUrduNoun extends Noun {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private String singular;
        private String plural;
        private String singular_obl;
        private String plural_obl;

        HindiUrduNoun(HindiUrduDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender,String access,  boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public void makeSkinny() {
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return enumMapFilterNulls(HindiUrduNounForm.SINGULAR, singular, HindiUrduNounForm.PLURAL, plural, HindiUrduNounForm.SINGULAR_OBL, singular_obl,
                    HindiUrduNounForm.PLURAL_OBL, plural_obl);
        }
        @Override
        public String getDefaultString(boolean isPlural) {
            return isPlural ? (plural != null ? plural : singular) : singular;
        }
        @Override
        public String getString(NounForm form) {
            assert form instanceof HindiUrduNounForm : "Why not hindustani for " + form;
            return form.getCase() == OBLIQUE_CASE ? form.getNumber() == LanguageNumber.PLURAL ? plural_obl : singular_obl
                    : form.getNumber() == LanguageNumber.PLURAL ? plural : singular;
        }
        @Override
        public void setString(String value, NounForm form) {
            if (form.getCase() == OBLIQUE_CASE) {
                if (form.getNumber().isPlural()) {
                    this.plural_obl = intern(value);
                } else {
                    this.singular_obl = intern(value);
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
                return false;
            }
            // Default the values for entity nouns, but not for others to make rename fields more specific.
            if (getNounType() == NounType.ENTITY) {
                if (this.plural == null)
                 {
                    this.plural = this.singular;  // Default plural to singular.
                }
                // Default the singular/plural definitions to start
                if (this.singular_obl == null) {
                    this.singular_obl = this.singular;
                }
                if (this.plural_obl == null) {
                    this.plural_obl = this.plural;
                }
            }
            return true;
        }
    }

    static class HindiUrduAdjective extends Adjective {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		EnumMap<HindiUrduModifierForm,String> values = new EnumMap<HindiUrduModifierForm,String>(HindiUrduModifierForm.class);
        HindiUrduAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
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
            assert form instanceof HindiUrduModifierForm : "Why not Hindustani?";
            values.put((HindiUrduModifierForm)form, intern(value));
        }
        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(HindiUrduModifierForm.SINGULAR_FEMININE));
        }
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.FEMININE;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return getAllNounForms();
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return getAllNounForms();
    }

    /**
     * construct new <CODE>HindiUrduNoun</CODE>.
     */
    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new HindiUrduNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new HindiUrduAdjective(this, name, position);
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return GENDER_TYPES;
    }

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.OBJECTIVE);  // It's really Direct and Oblique...
    }

    private static final List<HindiUrduNounForm> ALL_NOUN_FORMS = ImmutableList.copyOf(EnumSet.allOf(HindiUrduNounForm.class));
    private static final List<HindiUrduModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(HindiUrduModifierForm.class));

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return ALL_NOUN_FORMS;
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
            LanguageArticle article) {
        if (possessive != LanguagePossessive.NONE || article != LanguageArticle.ZERO) {
            return null;
        }
        return _case == OBLIQUE_CASE ? (number.isPlural() ? HindiUrduNounForm.PLURAL_OBL : HindiUrduNounForm.SINGULAR_OBL)
                : (number.isPlural() ? HindiUrduNounForm.PLURAL : HindiUrduNounForm.SINGULAR);
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        // Optimize for local cases
        if (startsWith != LanguageStartsWith.CONSONANT || article != LanguageArticle.ZERO) {
            return null;
        }
        if (_case == OBLIQUE_CASE) {
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ? HindiUrduModifierForm.PLURAL_MASCULINE_O : HindiUrduModifierForm.SINGULAR_MASCULINE_O)
                    : (number.isPlural() ? HindiUrduModifierForm.PLURAL_FEMININE_O : HindiUrduModifierForm.SINGULAR_FEMININE_O);
        }
        return gender == LanguageGender.MASCULINE ? (number.isPlural() ? HindiUrduModifierForm.PLURAL_MASCULINE : HindiUrduModifierForm.SINGULAR_MASCULINE)
                : (number.isPlural() ? HindiUrduModifierForm.PLURAL_FEMININE : HindiUrduModifierForm.SINGULAR_FEMININE);
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }
}
