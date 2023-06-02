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
 * Declension for Catalan language. General rules as following,
 *
 * Noun forms : singular &amp; plural
 *       Case : n/a
 *     Gender : masculine, feminine
 *    Article : definite &amp; indefinite; Defined as below,
 *        ---&gt; definite :
 *             Masculine: singular: el (l’), plural: els
 *             Feminine: singular: la (l’) plural: les
 *        ---&gt; indefinite :
 *             Masculine: singular: un, plural: uns
 *             Feminine: singular: una, plural: unes
 *Starts with : vowel(includes H), Consonant
 *       Misc : adjectives are only infected in gender and plurality (no case!)
 *
 * @author pu.chen
 *
 */
class CatalanDeclension extends RomanceDeclension {
    public CatalanDeclension(HumanLanguage language) {
        super(language);
    }

    /**
     * Adjective for Catalan. It takes care of gender, plurality(number) and startwith.
     */
    public enum CatalanModifierForm implements AdjectiveForm, ArticleForm {
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.CONSONANT),
        PLURAL_MASCULINE(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT),
        PLURAL_FEMININE(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.CONSONANT),

        SINGULAR_MASCULINE_VOWEL(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.VOWEL),
        SINGULAR_FEMININE_VOWEL(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.VOWEL),
        PLURAL_MASCULINE_VOWEL(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.VOWEL),
        PLURAL_FEMININE_VOWEL(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.VOWEL),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private final LanguageStartsWith startsWith;

        private CatalanModifierForm(LanguageNumber number, LanguageGender gender, LanguageStartsWith startsWith) {
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
            return getNumber().getDbValue() + "-" + getStartsWith().getDbValue() + "-" + getGender().getDbValue();
        }

        @Override
        public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
                throws IOException {
            a.append(termFormVar + ".substr(0,2)+" + genderVar + "+'-'+" + startsWithVar);
        }
    }

    protected static class CatalanAdjective extends Adjective {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality(number)
        EnumMap<CatalanModifierForm, String> values = new EnumMap<>(CatalanModifierForm.class);
        private final LanguageStartsWith startsWith;

        CatalanAdjective(LanguageDeclension declension, String name, LanguageStartsWith startsWith, LanguagePosition position) {
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
            assert form instanceof CatalanModifierForm : "It's not a supported adjective form for Catalan.";
            values.put((CatalanModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(CatalanModifierForm.SINGULAR_FEMININE));
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }

    protected static class CatalanArticle extends Article {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality(number)
        EnumMap<CatalanModifierForm, String> values = new EnumMap<>(CatalanModifierForm.class);

        CatalanArticle(CatalanDeclension declension, String name, LanguageArticle articleType) {
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
            assert form instanceof CatalanModifierForm : "It's not a supported article form for Catalan.";
            values.put((CatalanModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(CatalanModifierForm.SINGULAR_FEMININE));
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }

    @Override
    public boolean hasStartsWith() {
        return true;
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new CatalanAdjective(this, name, startsWith, position);
    }

    private static final List<CatalanModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(CatalanModifierForm.class));

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
        return getAllNounForms();
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL);
    }

    /*
     * Adjectives are only infected in gender and plurality (no case!)
     */
    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        if (_case != LanguageCase.NOMINATIVE || article != LanguageArticle.ZERO) {
            return null;
        }

        switch (startsWith) {
        case CONSONANT:
            return gender == LanguageGender.MASCULINE ?
                    (number.isPlural() ? CatalanModifierForm.PLURAL_MASCULINE : CatalanModifierForm.SINGULAR_MASCULINE)
                    : (number.isPlural() ? CatalanModifierForm.PLURAL_FEMININE : CatalanModifierForm.SINGULAR_FEMININE);
        case VOWEL:
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ?
                    CatalanModifierForm.PLURAL_MASCULINE_VOWEL : CatalanModifierForm.SINGULAR_MASCULINE_VOWEL)
                    : (number.isPlural() ? CatalanModifierForm.PLURAL_FEMININE_VOWEL : CatalanModifierForm.SINGULAR_FEMININE_VOWEL);
        case SPECIAL: // n/a for Catalan.
        }

        return null;
    }

    /*  Article : definite & indefinite
     *        ---> definite :
     *             Masculine: singular: el (l’), plural: els
     *             Feminine: singular: la (l’) plural: les
     *        ---> indefinite :
     *             Masculine: singular: un, plural: uns
     *             Feminine: singular: una, plural: unes
     */
    private static final EnumMap<CatalanModifierForm, String> DEFINITE_ARTICLE =
            new EnumMap<CatalanModifierForm, String>(ImmutableMap.<CatalanModifierForm,String>builder()
                    .put(CatalanModifierForm.SINGULAR_FEMININE, "la ")
                    .put(CatalanModifierForm.SINGULAR_MASCULINE, "el ")
                    .put(CatalanModifierForm.PLURAL_FEMININE, "les ")
                    .put(CatalanModifierForm.PLURAL_MASCULINE, "els ")

                    .put(CatalanModifierForm.SINGULAR_FEMININE_VOWEL, "l'")
                    .put(CatalanModifierForm.SINGULAR_MASCULINE_VOWEL, "l'")
                    .put(CatalanModifierForm.PLURAL_FEMININE_VOWEL, "les ")
                    .put(CatalanModifierForm.PLURAL_MASCULINE_VOWEL, "els ").build());

    private static final EnumMap<CatalanModifierForm, String> INDEFINITE_ARTICLE =
        new EnumMap<CatalanModifierForm, String>(ImmutableMap.<CatalanModifierForm,String>builder()
                .put(CatalanModifierForm.SINGULAR_FEMININE, "una ")
                .put(CatalanModifierForm.SINGULAR_MASCULINE, "un ")
                .put(CatalanModifierForm.PLURAL_FEMININE, "unes ")
                .put(CatalanModifierForm.PLURAL_MASCULINE, "uns ")

                .put(CatalanModifierForm.SINGULAR_FEMININE_VOWEL, "una ")
                .put(CatalanModifierForm.SINGULAR_MASCULINE_VOWEL, "un ")
                .put(CatalanModifierForm.PLURAL_FEMININE_VOWEL, "unes ")
                .put(CatalanModifierForm.PLURAL_MASCULINE_VOWEL, "uns ").build());


    @Override
    protected Map< ? extends ArticleForm, String> getDefiniteArticles() {
        return DEFINITE_ARTICLE;
    }

    @Override
    protected Map< ? extends ArticleForm, String> getIndefiniteArticles() {
        return INDEFINITE_ARTICLE;
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new CatalanArticle(this, name, articleType);
    }
}
