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

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @author stamm
 *
 */
class FrenchDeclension extends RomanceDeclension {
    public FrenchDeclension(HumanLanguage language) {
		super(language);
	}

	/**
     * Adjective form for languages that don't care about "starts with"
     */
    public static enum FrenchModifierForm implements AdjectiveForm, ArticleForm {
        // TODO: are all of these necessary?
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.CONSONANT),
        PLURAL_MASCULINE(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT),
        PLURAL_FEMININE(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.CONSONANT),
        SINGULAR_MASCULINE_V(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.VOWEL),
        SINGULAR_FEMININE_V(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.VOWEL),
        PLURAL_MASCULINE_V(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.VOWEL),
        PLURAL_FEMININE_V(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.VOWEL),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private final LanguageStartsWith startsWith;
        private FrenchModifierForm(LanguageNumber number, LanguageGender gender, LanguageStartsWith startsWith) {
            this.number = number;
            this.gender = gender;
            this.startsWith = startsWith;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguageGender getGender() {return this.gender;}
        @Override public LanguageStartsWith getStartsWith() { return startsWith; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
		@Override
		public String getKey() {
			return getNumber().getDbValue() + "-" + getGender().getDbValue() + "-" + getStartsWith().getDbValue();
		}
		@Override
		public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
				throws IOException {
			a.append(termFormVar+".substr(0,2)+"+genderVar+"+'-'+"+startsWithVar);
		}
    }

    protected static class FrenchAdjective extends Adjective {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<FrenchModifierForm, String> values = new EnumMap<>(FrenchModifierForm.class);
        private final LanguageStartsWith startsWith;

        FrenchAdjective(LanguageDeclension declension, String name, LanguageStartsWith startsWith, LanguagePosition position) {
            super(declension, name, position);
            this.startsWith = startsWith;
        }

        @Override
        public LanguageStartsWith getStartsWith() {
            return this.startsWith;
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
            assert form instanceof FrenchModifierForm : "The french do not like their language sullied with foreign words";
            values.put((FrenchModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(FrenchModifierForm.SINGULAR_FEMININE));
        }

        protected Object readResolve() {
            for (Map.Entry<FrenchModifierForm, String> e : this.values.entrySet()) {
                this.values.put(e.getKey(), intern(e.getValue()));
            }
            return this;
        }
    }

    protected static class FrenchArticle extends Article {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<FrenchModifierForm, String> values = new EnumMap<>(FrenchModifierForm.class);

        FrenchArticle(FrenchDeclension declension, String name, LanguageArticle articleType) {
            super(declension, name, articleType);
        }

        @Override
        public Map< ? extends ArticleForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(ArticleForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(ArticleForm form, String value) {
            assert form instanceof FrenchModifierForm : "The french do not like their language sullied with foreign words";
            values.put((FrenchModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(FrenchModifierForm.SINGULAR_FEMININE));
        }

        protected Object readResolve() {
            for (Map.Entry<FrenchModifierForm, String> e : this.values.entrySet()) {
                this.values.put(e.getKey(), intern(e.getValue()));
            }
            return this;
        }
    }

    @Override
    public boolean hasStartsWith() {
        return true;
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new FrenchAdjective(this, name, startsWith, position);
    }

    private static final List<FrenchModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(FrenchModifierForm.class));

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public List< ? extends ArticleForm> getArticleForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return getAllNounForms();  // You need to specify all of them.
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL);
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        if (_case != LanguageCase.NOMINATIVE || article != LanguageArticle.ZERO) return null;
        // Optimize for local cases
        switch (startsWith) {
        case CONSONANT:
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ? FrenchModifierForm.PLURAL_MASCULINE : FrenchModifierForm.SINGULAR_MASCULINE)
                    : (number.isPlural() ? FrenchModifierForm.PLURAL_FEMININE : FrenchModifierForm.SINGULAR_FEMININE);
        case VOWEL:
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ? FrenchModifierForm.PLURAL_MASCULINE_V : FrenchModifierForm.SINGULAR_MASCULINE_V)
                    : (number.isPlural() ? FrenchModifierForm.PLURAL_FEMININE_V : FrenchModifierForm.SINGULAR_FEMININE_V);
        case SPECIAL:
        }
///CLOVER:OFF
        return null;
///CLOVER:ON
    }

    private static final EnumMap<FrenchModifierForm, String> INDEFINITE_ARTICLE =
        new EnumMap<>(ImmutableMap.<FrenchModifierForm,String>builder()
                .put(FrenchModifierForm.SINGULAR_FEMININE, "une ")
                .put(FrenchModifierForm.SINGULAR_MASCULINE, "un ")
                .put(FrenchModifierForm.PLURAL_FEMININE, "des ")
                .put(FrenchModifierForm.PLURAL_MASCULINE, "des ")
                .put(FrenchModifierForm.SINGULAR_FEMININE_V, "une ")
                .put(FrenchModifierForm.SINGULAR_MASCULINE_V, "un ")
                .put(FrenchModifierForm.PLURAL_FEMININE_V, "des ")
                .put(FrenchModifierForm.PLURAL_MASCULINE_V, "des ").build());

    private static final EnumMap<FrenchModifierForm, String> DEFINITE_ARTICLE =
        new EnumMap<>(ImmutableMap.<FrenchModifierForm,String>builder()
                .put(FrenchModifierForm.SINGULAR_FEMININE, "la ")
                .put(FrenchModifierForm.SINGULAR_MASCULINE, "le ")
                .put(FrenchModifierForm.PLURAL_FEMININE, "les ")
                .put(FrenchModifierForm.PLURAL_MASCULINE, "les ")
                .put(FrenchModifierForm.SINGULAR_FEMININE_V, "l'")
                .put(FrenchModifierForm.SINGULAR_MASCULINE_V, "l'")
                .put(FrenchModifierForm.PLURAL_FEMININE_V, "les ")
                .put(FrenchModifierForm.PLURAL_MASCULINE_V, "les ").build());

    @Override
    protected Map< ? extends ArticleForm, String> getDefiniteArticles() {
        return DEFINITE_ARTICLE;
    }

    @Override
    protected Map< ? extends ArticleForm, String> getIndefiniteArticles() {
        return INDEFINITE_ARTICLE;
    }

    /**
     * The Romansh declension is very similar in structure to french
     * @author stamm
     */
    static class RomanshDeclension extends FrenchDeclension {
        public RomanshDeclension(HumanLanguage language) {
			super(language);
		}

		private static final EnumMap<FrenchModifierForm, String> RM_INDEFINITE_ARTICLE =
            new EnumMap<>(ImmutableMap.<FrenchModifierForm,String>builder()
                    .put(FrenchModifierForm.SINGULAR_FEMININE, "ina ")
                    .put(FrenchModifierForm.SINGULAR_MASCULINE, "in ")
                    .put(FrenchModifierForm.PLURAL_FEMININE, "")
                    .put(FrenchModifierForm.PLURAL_MASCULINE, "")
                    .put(FrenchModifierForm.SINGULAR_FEMININE_V, "in'")
                    .put(FrenchModifierForm.SINGULAR_MASCULINE_V, "in'")
                    .put(FrenchModifierForm.PLURAL_FEMININE_V, "")
                    .put(FrenchModifierForm.PLURAL_MASCULINE_V, "").build());

        private static final EnumMap<FrenchModifierForm, String> RM_DEFINITE_ARTICLE =
            new EnumMap<>(ImmutableMap.<FrenchModifierForm,String>builder()
                    .put(FrenchModifierForm.SINGULAR_FEMININE, "la ")
                    .put(FrenchModifierForm.SINGULAR_MASCULINE, "il ")
                    .put(FrenchModifierForm.PLURAL_FEMININE, "las ")
                    .put(FrenchModifierForm.PLURAL_MASCULINE, "ils ")
                    .put(FrenchModifierForm.SINGULAR_FEMININE_V, "l'")
                    .put(FrenchModifierForm.SINGULAR_MASCULINE_V, "l'")
                    .put(FrenchModifierForm.PLURAL_FEMININE_V, "las ")
                    .put(FrenchModifierForm.PLURAL_MASCULINE_V, "ils ").build());

        @Override
        protected Map<? extends ArticleForm, String> getDefiniteArticles() {
            return RM_DEFINITE_ARTICLE;
        }

        @Override
        protected Map<? extends ArticleForm, String> getIndefiniteArticles() {
            return RM_INDEFINITE_ARTICLE;
        }
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new FrenchArticle(this, name, articleType);
    }
}
