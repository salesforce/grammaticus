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
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;

/**
 * Represents a Malayo-Polyesian language.  Generally non-inflected except for plurals, which
 * are often formed by reduplication.  Sometimes in Maori it's through vowel
 * lengthening.
 *
 * Maori and Somoan have definite and indefinite articles, but are uninflected.
 *
 * @author stamm
 */
 class MalayoPolynesianDeclension extends AbstractLanguageDeclension {
	// All the forms you can request
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);

    public MalayoPolynesianDeclension(HumanLanguage language) {
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
    public boolean hasGender() {
        return false;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimplePluralNoun(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
    }

    /**
     * Hawaiian is a MalayoPolynesian language, but has a definite article that
     * changes based on the next word (ka/ke) vs "nā" plural.  Ke is used with
     * works that start with k, e, a, or o, plus a few exceptional words like
     * table (kepākaukau), song (mele) and eating utensils.  See kūʻēlula.
     *
     * Ka is represented as starts with consonant.  Ke is represented as starts with special.
     *
     * Indefinite article is invariant (he).
     *
     * This is similar to English.
     *
     * @author stamm
     */
    static class HawaiianDeclension extends ArticledDeclension {

        public HawaiianDeclension(HumanLanguage language) {
    		super(language);
    	}

        /**
         * The hawaiian articles are distinguished by whether the next noun starts with k,e,a,o,
         * and some changes for starts with ke vs ka.
         *
         */
        public static enum HawaiianArticleForm implements ArticleForm {
            KA(LanguageNumber.SINGULAR, LanguageStartsWith.CONSONANT),
            KE(LanguageNumber.SINGULAR, LanguageStartsWith.SPECIAL),
            NA(LanguageNumber.PLURAL, LanguageStartsWith.CONSONANT)
            ;

            private final LanguageNumber number;
            private final LanguageStartsWith startsWith;
            private HawaiianArticleForm(LanguageNumber number, LanguageStartsWith startsWith) {
                this.number = number;
                this.startsWith = startsWith;
            }

            @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
            @Override public LanguageGender getGender() { return LanguageGender.NEUTER; }
            @Override public LanguageNumber getNumber() { return this.number; }
            @Override public LanguageStartsWith getStartsWith() {return this.startsWith; }
            // Helper method to convert from "generic" form to enum
            static HawaiianArticleForm getForm(ModifierForm form) {
                return form.getNumber() == LanguageNumber.SINGULAR ?
                        (form.getStartsWith() == LanguageStartsWith.SPECIAL ? KE : KA)
                        : NA;
            }
    		@Override
    		public String getKey() {
    			return getNumber().getDbValue() + "-" + getStartsWith().getDbValue();
    		}

    		@Override
    		public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
    				throws IOException {
    			a.append(termFormVar+".charAt(0)=='"+LanguageNumber.PLURAL.getDbValue()+"'?"+termFormVar+":'"+LanguageNumber.SINGULAR.getDbValue()+"-'+"+startsWithVar);
    		}
        }

        /**
         * Represents an english adjective
         */
        public static class HawaiianArticle extends Article {
            private static final long serialVersionUID = 597093332610194996L; // javac generated value. Mandatory for javac/eclipse compatibility

            private String singular; // We only store one value
            private String singularVowel; // We only store one value
            private String plural; // We only store one value

            HawaiianArticle(HawaiianDeclension declension, String name, LanguageArticle articleType) {
                super(declension, name, articleType);
            }

            @Override
            public Map<? extends ArticleForm, String> getAllValues() {
                return enumMapFilterNulls(HawaiianArticleForm.KA, singular,
                        HawaiianArticleForm.KE, singularVowel,
                        HawaiianArticleForm.NA, plural);
            }

            @Override
            public String getString(ArticleForm form) {
                switch (HawaiianArticleForm.getForm(form)) {
                case NA: return plural;
                case KE: return singularVowel;
                default:
                case KA: return singular;
                }
            }

            @Override
            protected void setString(ArticleForm form, String value) {
                switch (HawaiianArticleForm.getForm(form)) {
                case NA:  this.plural = intern(value);  break;
                case KE: this.singularVowel = intern(value);  break;
                default:
                case KA:  this.singular = intern(value);
                }
            }

            @Override
            public boolean validate(String name) {
                if (this.singular == null) {
                    return false;
                }
                if (this.singularVowel == null) {
                    this.singularVowel = this.singular;
                }
                if (this.plural == null) {
                    this.plural = this.singular;
                }
                return true;
            }

            protected Object readResolve() {
                this.singular = intern(this.singular);
                this.singularVowel = intern(this.singularVowel);
                this.plural = intern(this.plural);
                return this;
            }
        }

        // All the forms you can request
        static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
        // All the forms you can set for "other" forms
        static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
        // All the forms you can set for "other" forms
        static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);
        // All the forms you can set for articles
        static final List<? extends ArticleForm> ARTICLE_FORMS = ImmutableList.copyOf(EnumSet.of(HawaiianArticleForm.KA, HawaiianArticleForm.KE, HawaiianArticleForm.NA));

        @Override public List<? extends NounForm> getAllNounForms() { return ALL_FORMS;  }

        @Override public Collection<? extends NounForm> getEntityForms() { return getAllNounForms();  }

        @Override public Collection<? extends NounForm> getFieldForms() { return getAllNounForms(); }

        @Override public Collection<? extends NounForm> getOtherForms() { return OTHER_FORMS; }

        @Override public List< ? extends AdjectiveForm> getAdjectiveForms() { return ADJECTIVE_FORMS; }

        @Override public List< ? extends ArticleForm> getArticleForms() { return ARTICLE_FORMS; }


        @Override
        public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
            return new SimpleAdjectiveWithStartsWith(this, name, startsWith);
        }

        @Override
        public Article createArticle(String name, LanguageArticle articleType) {
            return new HawaiianArticle(this, name, articleType);
        }

        /**
         * Simplify the code to just return what you want without iterating through the forms
         */
        @Override
        public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                LanguageCase _case) {
            if (number == LanguageNumber.PLURAL) {
                return HawaiianArticleForm.NA;
            }
            return startsWith == LanguageStartsWith.SPECIAL ? HawaiianArticleForm.KE : HawaiianArticleForm.KA;
        }

        @Override
        public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
            return SimpleModifierForm.SINGULAR;
        }

        @Override
        public boolean hasGender() {
            return false;
        }

        @Override
        public boolean hasStartsWith() {
            return true;
        }

        @Override
        public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
            return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.SPECIAL);  // Only generally care about consonant.
        }

        @Override
        public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            return new SimpleArticledPluralNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            HawaiianArticleForm e = HawaiianArticleForm.getForm(form);

            switch (articleType) {
            case DEFINITE:
                switch (e) {
                case NA:  return "Nā ";
                case KE: return "Ke ";
                case KA:  return "Ka ";
                }
                throw new UnsupportedOperationException("Invalid article");
            case INDEFINITE:
                return "He ";
            case ZERO:
                return null;
            default:
                throw new UnsupportedOperationException("Invalid article");
            }
        }
    }
}
