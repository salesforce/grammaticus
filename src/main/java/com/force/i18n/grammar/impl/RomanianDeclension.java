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
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Romanian support implementation of LanguageDeclension.
 *
 * The grammar is similar to that of German except no definite articles and adjectives.
 * Two cases Nominative and Dative required, Accusative is same as Nominative and Gentive is same as
 * Dative, so these are not handled separately.
 * Definite articles are appended to the end of nouns. For each noun we need to have its definite article form
 * specified according to case and number. Indefinite articles are regular(similar to German)
 * Adjectives agree in case, number and gender with the Noun (similar to Czech and Polish).
 *
 * SLT - Romanian is a romance language, while having three genders and two case forms.  It really has no
 * business extending RomanceDeclension.
 *
 * @author shameed,stamm
 */
class RomanianDeclension extends RomanceDeclension {
    private static final Logger logger = Logger.getLogger(RomanianDeclension.class.getName());

    private static EnumSet<LanguageGender> GENDER_TYPES = EnumSet.of(LanguageGender.FEMININE, LanguageGender.MASCULINE, LanguageGender.NEUTER);
    private static ModifierFormMap<RomanianModifierForm> MODIFIER_FORM_MAP = new ModifierFormMap<RomanianModifierForm>(Arrays.asList(RomanianModifierForm.values()));

    private final EnumMap<LanguageArticle,NounFormMap<RomanianNounForm>> nounFormMap;
    private final List<RomanianNounForm> entityForms;
    private final List<RomanianNounForm> fieldForms;


    public RomanianDeclension(HumanLanguage language) {
    	super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<RomanianNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<RomanianNounForm> fieldBuilder = ImmutableList.builder();

        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguageArticle article : getRequiredNounArticles()) {
                    RomanianNounForm form = new RomanianNounForm(this, number, caseType, article, ordinal++);
                    entityBuilder.add(form);
                    if (caseType == LanguageCase.NOMINATIVE && article == LanguageArticle.ZERO) {
                        fieldBuilder.add(form); // Only plurals count for the fields
                    }
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
        this.nounFormMap = NounFormMap.getArticleSpecificMap(this.entityForms);
    }


    public static class RomanianNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;

        private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageArticle article;

        private RomanianNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguageArticle article, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.article = article;
        }

        @Override public LanguageArticle getArticle() { return this.article; }
        @Override public LanguageCase getCase() { return caseType; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override public LanguageNumber getNumber() {return this.number;}

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" +  getArticle().getDbValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.number, this.caseType, this.article);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof RomanianNounForm) {
                RomanianNounForm o = this.getClass().cast(other);
                return super.equals(other) && this.number == o.number && this.caseType == o.caseType
                        && this.article == o.article;
            }
            return false;
        }
    }

    public enum RomanianModifierForm implements ArticleForm, AdjectiveForm {
        // Yeah, I could have generated this, but making it an enum wasn't that big a difference
        SINGULAR_N(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageGender.NEUTER),
        SINGULAR_M(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageGender.MASCULINE),
        SINGULAR_F(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageGender.FEMININE),
        PLURAL_N(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguageGender.NEUTER),
        PLURAL_M(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguageGender.MASCULINE),
        PLURAL_F(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguageGender.FEMININE),
        SINGULAR_DAT_N(LanguageNumber.SINGULAR, LanguageCase.DATIVE, LanguageGender.NEUTER),
        SINGULAR_DAT_M(LanguageNumber.SINGULAR, LanguageCase.DATIVE, LanguageGender.MASCULINE),
        SINGULAR_DAT_F(LanguageNumber.SINGULAR, LanguageCase.DATIVE, LanguageGender.FEMININE),
        PLURAL_DAT_N(LanguageNumber.PLURAL, LanguageCase.DATIVE, LanguageGender.NEUTER),
        PLURAL_DAT_M(LanguageNumber.PLURAL, LanguageCase.DATIVE, LanguageGender.MASCULINE),
        PLURAL_DAT_F(LanguageNumber.PLURAL, LanguageCase.DATIVE, LanguageGender.FEMININE),
        ;

        private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageGender gender;
        private RomanianModifierForm(LanguageNumber number, LanguageCase caseType, LanguageGender gender) {
            this.number = number;
            this.caseType = caseType;
            this.gender = gender;
        }

        @Override public LanguageCase getCase() { return caseType; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguageGender getGender() { return this.gender; }
        @Override public LanguageStartsWith getStartsWith() { return LanguageStartsWith.CONSONANT; }
        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }

        @Override
        public String getKey() {
            return getGender().getDbValue() + "-" + getCase().getDbValue() + "-" + getNumber().getDbValue();
        }

        @Override
        public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
                throws IOException {
            a.append(genderVar + "+" + termFormVar + ".substr(1)");
        }
    }

    /**
     * <CODE>Noun</CODE> implementation for the most Latin-1 language
     */
    protected static class RomanianNoun extends ComplexArticledNoun<RomanianNounForm> {
        private static final long serialVersionUID = 1L;

        protected RomanianNoun(RomanianDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected final Class<RomanianNounForm> getFormClass() {
            return RomanianNounForm.class;
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            for (NounForm form : getDeclension().getAllNounForms()) {
                if (getString(form) == null) {
                    // Only do the "defaulting" on entities
                    if (getNounType() == NounType.ENTITY) {
                        String value = getCloseButNoCigarString(form);

                        if (value == null) {
                            logger.info("###\tError: The noun " + name + " has no " + form + " form and no default could be found");
                            return false;
                        }
                        setString(value, form);
                    }
                }
            }
            return true;
        }
    }

    protected static class RomanianAdjective extends Adjective {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<RomanianModifierForm, String> values = new EnumMap<>(RomanianModifierForm.class);

        RomanianAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
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
            assert form instanceof RomanianModifierForm : "The carpathian mountain cry out, why do you disrespect the Romanian's so?";
            values.put((RomanianModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.allOf(RomanianModifierForm.class));
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }

    protected static class RomanianArticle extends Article {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<RomanianModifierForm, String> values = new EnumMap<>(RomanianModifierForm.class);

        RomanianArticle(RomanianDeclension declension, String name, LanguageArticle articleType) {
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
            assert form instanceof RomanianModifierForm : "The carpathian mountain cry out, why do you disrespect the Romanian's so?";
            values.put((RomanianModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(RomanianModifierForm.SINGULAR_F,
                    RomanianModifierForm.SINGULAR_M, RomanianModifierForm.SINGULAR_N,
                    RomanianModifierForm.PLURAL_N, RomanianModifierForm.SINGULAR_DAT_F,
                    RomanianModifierForm.SINGULAR_DAT_M, RomanianModifierForm.SINGULAR_DAT_N,
                    RomanianModifierForm.PLURAL_DAT_N ));
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
            LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new RomanianNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new RomanianAdjective(this, name, position);
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new RomanianArticle(this, name, articleType);
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return this.entityForms;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return this.fieldForms;
    }

    private static final List<RomanianModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(RomanianModifierForm.class));

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.DATIVE);
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return Collections.singleton(fieldForms.get(0));  // Only need "singular" for other forms
    }

    private static final Map<RomanianModifierForm, String> INDEFINITE_ARTICLES =
        ImmutableMap.<RomanianModifierForm, String>builder()
            .put(RomanianModifierForm.SINGULAR_N, "un ")
            .put(RomanianModifierForm.SINGULAR_F, "o ")
            .put(RomanianModifierForm.SINGULAR_M, "un ")
            .put(RomanianModifierForm.PLURAL_N, "ni&#351;te ")
            .put(RomanianModifierForm.PLURAL_F, "ni&#351;te ")
            .put(RomanianModifierForm.PLURAL_M, "ni&#351;te ")
            .put(RomanianModifierForm.SINGULAR_DAT_N, "unui ")
            .put(RomanianModifierForm.SINGULAR_DAT_F, "unei ")
            .put(RomanianModifierForm.SINGULAR_DAT_M, "unui ")
            .put(RomanianModifierForm.PLURAL_DAT_N, "unor ")
            .put(RomanianModifierForm.PLURAL_DAT_F, "unor ")
            .put(RomanianModifierForm.PLURAL_DAT_M, "unor ")
            .build();

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.NEUTER;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return GENDER_TYPES;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        if (article != LanguageArticle.ZERO) return null;
        return MODIFIER_FORM_MAP.getForm(startsWith, gender, number, _case);
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        if (possessive != LanguagePossessive.NONE) return null;
        NounFormMap<? extends NounForm> formMap = this.nounFormMap.get(article);
        return formMap == null ? null : formMap.getForm(number, _case);

    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case) {
        return MODIFIER_FORM_MAP.getForm(startsWith, gender, number, _case);
    }

    @Override
    protected Map< ? extends ArticleForm, String> getIndefiniteArticles() {
        return INDEFINITE_ARTICLES;
    }

    /**
     * Only define this for languages where the article is irregular and a suffix (such as Danish)
     * @return the set of articles that the noun needs to define in the label files
     */
    protected EnumSet<LanguageArticle> getRequiredNounArticles() {
        return EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE);
    }

    @Override
    public final boolean hasArticleInNounForm() {
        return true;
    }

    //Should never be called since we override getDefaultArticleString which was the only method that calls this
    @Override
    protected Map< ? extends ArticleForm, String> getDefiniteArticles() {
        throw new UnsupportedOperationException("No definitive articles in romanian");
    }

    // Romanian has definite articles in the noun form.
    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        switch (articleType) {
        case INDEFINITE:
            return getIndefiniteArticles().get(form);
        default:
            return null;
        }
    }
}
