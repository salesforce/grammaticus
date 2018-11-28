/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.grammar.LanguageCase.ACCUSATIVE;
import static com.force.i18n.grammar.LanguageCase.NOMINATIVE;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The declension of nouns and adjectives for Arabic
 * <p>
 * Arabic has 3 forms of possession, 3 cases (Nom, Acc, & Gen), Gender, plural, and definitiveness.
 * <p>
 * This leads to 36 forms of nouns and adjectives (since definitiveness applies only to nouns, Gender only to adjectives).
 * <p>
 * It can be viewed as a form of Arabic with cases
 * Arabic has only the definite article, but the grammar engine doesn't really need to know about it because
 * the article is never inflected and nothing really cares about it.  However, it can appear in the middle
 * of nouns, so the definite version is auto-derived unless otherwise specified. *
 *
 * @author stamm
 */
class ArabicDeclension extends SemiticDeclension {
    private final List<ArabicNounForm> nounForms;
    private final List<ArabicNounForm> entityForms;
    private final List<ArabicAdjectiveForm> adjectiveForms;


    private static final String DEFAULT_DEFINITE_PREFIX = "\u0627\u0644";  // ال
    private static final String FINAL_ALIF = "\u0627";  // ا   What is added to indefinite nouns

    public ArabicDeclension(HumanLanguage language) {
        super(language);
        assert language.getLocale().getLanguage().equals("ar") : "Initializing a variant Arabic declension for non-arabic";

        // Generate the different forms from subclass methods
        ImmutableList.Builder<ArabicNounForm> nounBuilder = ImmutableList.builder();
        ImmutableList.Builder<ArabicNounForm> entityNounBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguagePossessive possessive : getRequiredPossessive()) {
                    for (LanguageArticle article : getAllowedArticleTypes()) {
                        ArabicNounForm form = new ArabicNounForm(this, number, caseType, possessive, article, ordinal++);
                        if (caseType == LanguageCase.NOMINATIVE)
                            entityNounBuilder.add(form);  // TODO: Which forms *must* be specified?
                        nounBuilder.add(form);
                    }
                }
            }
        }
        this.nounForms = nounBuilder.build();
        this.entityForms = entityNounBuilder.build();

        ordinal = 0;
        ImmutableList.Builder<ArabicAdjectiveForm> adjBuilder = ImmutableList.builder();
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageGender gender : getRequiredGenders()) {
                for (LanguageCase caseType : getRequiredCases()) {
                    for (LanguageArticle article : getAllowedArticleTypes()) {
                        for (LanguagePossessive possessive : getRequiredPossessive()) {
                            ArabicAdjectiveForm form = new ArabicAdjectiveForm(this, gender, number, caseType, article, possessive, ordinal++);
                            adjBuilder.add(form);
                        }
                    }
                }
            }
        }
        this.adjectiveForms = adjBuilder.build();
    }


    /**
     * Turkish nouns are inflected for case, number, possessive, and article.  Everything that is
     */
    static class ArabicNounForm extends ComplexNounForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguagePossessive possesive;
        private final LanguageArticle article;

        public ArabicNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguagePossessive possesive, LanguageArticle article, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.possesive = possesive;
            this.article = article;
        }

        @Override
        public LanguageArticle getArticle() {
            return this.article;
        }

        @Override
        public LanguageCase getCase() {
            return this.caseType;
        }

        @Override
        public LanguageNumber getNumber() {
            return this.number;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return possesive;
        }
    }

    /**
     * Arabic nouns are inflected for case, number, gender, and article.  Oh my.
     */
    static class ArabicAdjectiveForm extends ComplexAdjectiveForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final LanguageGender gender;
        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguageArticle article;
        private final LanguagePossessive possessive;

        public ArabicAdjectiveForm(LanguageDeclension declension, LanguageGender gender, LanguageNumber number, LanguageCase caseType, LanguageArticle article, LanguagePossessive possessive, int ordinal) {
            super(declension, ordinal);
            this.gender = gender;
            this.number = number;
            this.caseType = caseType;
            this.article = article;
            this.possessive = possessive;
        }

        @Override
        public LanguageArticle getArticle() {
            return this.article;
        }

        @Override
        public LanguageCase getCase() {
            return this.caseType;
        }

        @Override
        public LanguageNumber getNumber() {
            return this.number;
        }

        @Override
        public LanguageStartsWith getStartsWith() {
            return LanguageStartsWith.CONSONANT;
        }

        @Override
        public LanguageGender getGender() {
            return this.gender;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return possessive;
        }
    }

    /**
     * Add a final alif to mark this noun/adjective as indefinite accusative in MSA orthography.
     *
     * @param str the arabic string passed in
     * @return that noun/adjective marked as accusative
     */
    static String addAlifForAccusative(String str) {
        if (Strings.isNullOrEmpty(str)) return str;
        char end = str.charAt(str.length() - 1);

        // TODO: Verify from the translators if the alif marker is necessary
        if (Boolean.TRUE) return str;

        //private static final String INDEF_ACC_EXCEPTION_LIST = "\u0621";  //  ءة   If noun ends with anything except this, then add final alif in accusative by default
        switch (end) {
            case '\u0629':
            case '\u0621':
                return str;
            default:
                return str + FINAL_ALIF;
        }
    }

    public static final class ArabicNoun extends ComplexArticledNoun<ArabicNounForm> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private static final Logger logger = Logger.getLogger(ArabicNoun.class.getName());

        ArabicNoun(ArabicDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopied);
        }

        @Override
        protected final Class<ArabicNounForm> getFormClass() {
            return ArabicNounForm.class;
        }


        @Override
        public String getExactString(NounForm form) {
            // Autoderive the accusative.
            if (form.getCase() == LanguageCase.ACCUSATIVE) {
                // Get the nominative form
                NounForm nomForm = getDeclension().getExactNounForm(form.getNumber(), LanguageCase.NOMINATIVE, form.getPossessive(), form.getArticle());
                String value = super.getExactString(nomForm);
                if (form.getArticle() == LanguageArticle.DEFINITE) return value;  // Definite articles never take alif.
                // Indefinite accusative form takes a final alif to mark it as accusative.
                return addAlifForAccusative(value);
            }
            return super.getExactString(form);
        }


        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            Collection<? extends NounForm> requiredForms = getDeclension().getFieldForms();
            // Default the definitive case if necessary.
            for (NounForm form : getDeclension().getAllNounForms()) {
                if (form.getCase() == LanguageCase.ACCUSATIVE)
                    continue;  // Ignore the accusative, since it's autoderived
                String value = getExactString(form);
                if (value == null) {
                    if (getNounType() == NounType.ENTITY) {
                        // Only magically add endings and derive missing forms
                        // for Entities, and not for compound nouns
                        if (form.getArticle() == LanguageArticle.DEFINITE) {
                            // Derive the definite from the non-article form
                            NounForm indForm = getDeclension().getExactNounForm(form.getNumber(),
                                    form.getCase(), form.getPossessive(), LanguageArticle.ZERO);
                            String indValue = super.getExactString(indForm);
                            if (indValue != null) {
                                setString(DEFAULT_DEFINITE_PREFIX + indValue, form);
                            }
                        } else {
                            // Only do the "defaulting" on entities because the "entity"
                            // value in sfdcnames.xml usually only specifies 2 forms
                            String val = getCloseButNoCigarString(form);
                            if (val == null) {
                                logger.info("###\tError: The noun " + name + " has no " + form
                                        + " form and no default could be found");
                                return false;
                            }
                            setString(val, form);
                        }
                    } else if (requiredForms.contains(form)) {
                        // TODO SLT: This logic seems faulty.  Why'd we bother
                        logger.finest("###\tError: The noun " + name + " has no " + form + " form");
                        return false;
                    }
                }

            }
            return true;
        }
    }


    protected static class ArabicAdjective extends ComplexAdjective<ArabicAdjectiveForm> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        ArabicAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        protected final Class<ArabicAdjectiveForm> getFormClass() {
            return ArabicAdjectiveForm.class;
        }

        @Override
        protected String deriveDefaultString(AdjectiveForm form, String value, AdjectiveForm baseFormed) {
            if (form.getPossessive() != LanguagePossessive.NONE) {
                return value; // Already has the right prefix
            }

            String prefix = "";

            if (form.getArticle() == LanguageArticle.DEFINITE && baseFormed.getArticle() != LanguageArticle.DEFINITE) {
                prefix = DEFAULT_DEFINITE_PREFIX;
            }

            // Automatically derive the accusative from the nominative
            if (form.getCase() == LanguageCase.ACCUSATIVE) {
                // Here's where we get the "real" string.
                if (value == null || value.length() == 0) return value;
                if (form.getArticle() == LanguageArticle.DEFINITE)
                    return prefix + value;  // Definite articles never take alif.
                return addAlifForAccusative(prefix + value);
            }

            return prefix + value;
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.FEMININE, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
        }
    }


    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new ArabicAdjective(this, name, position);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
                              LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new ArabicNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    protected String getDefiniteArticlePrefix(LanguageStartsWith startsWith) {
        return DEFAULT_DEFINITE_PREFIX;
    }


    static final EnumSet<LanguagePossessive> REQUIRED_POSESSIVES = EnumSet.of(LanguagePossessive.NONE, LanguagePossessive.FIRST, LanguagePossessive.SECOND);
    static final EnumSet<LanguageCase> ALLOWED_CASES = EnumSet.of(NOMINATIVE, ACCUSATIVE);  // Arabic has genitive, but it's the same as the nominative in nearly all cases
    static final EnumSet<LanguageCase> REQUIRED_CASES = EnumSet.of(NOMINATIVE);  // Arabic has genitive, but it's the same as the nominative in nearly all cases


    @Override
    public EnumSet<LanguagePossessive> getRequiredPossessive() {
        return REQUIRED_POSESSIVES;
    }


    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return REQUIRED_CASES;
    }

    @Override
    public EnumSet<LanguageCase> getAllowedCases() {
        return ALLOWED_CASES;
    }

    @Override
    public boolean hasPossessive() {
        return true;
    }

    @Override
    public boolean hasPossessiveInAdjective() {
        return true;
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return adjectiveForms;
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return nounForms;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return entityForms;
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return nounForms;
    }

}
