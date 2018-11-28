/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.grammar.LanguageCase.*;
import static com.force.i18n.grammar.LanguageGender.*;
import static com.force.i18n.grammar.LanguageNumber.SINGULAR;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.collect.*;

/**
 * Superclass of slavic languages; i.e. there are more than 7 noun forms, and no articles
 * In this case, we have a "generic" and non-enum based NounForm and AdjectiveForm
 *
 * @author stamm
 */
abstract class SlavicDeclension extends LanguageDeclension {
    private static final Logger logger = Logger.getLogger(SlavicDeclension.class.getName());

    private final List<SlavicNounForm> nounForms;
    private final Multimap<LanguageCase, SlavicNounForm> nounFormsByCase;
    private final NounFormMap<SlavicNounForm> nounFormMap;
    private final List<SlavicAdjectiveForm> adjectiveForms;
    private final ModifierFormMap<SlavicAdjectiveForm> adjectiveFormMap;

    public SlavicDeclension(HumanLanguage language) {
        super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<SlavicNounForm> nounBuilder = ImmutableList.builder();
        ImmutableMultimap.Builder<LanguageCase, SlavicNounForm> byCaseBuilder = ImmutableMultimap.builder();
        int ordinal = 0;
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageCase caseType : getRequiredCases()) {
                SlavicNounForm form = new SlavicNounForm(this, number, caseType, ordinal++);
                byCaseBuilder.put(caseType, form);
                nounBuilder.add(form);
            }
        }
        this.nounForms = nounBuilder.build();
        this.nounFormsByCase = byCaseBuilder.build();
        this.nounFormMap = new NounFormMap<SlavicNounForm>(this.nounForms);

        ImmutableList.Builder<SlavicAdjectiveForm> adjBuilder = ImmutableList.builder();
        int adjOrdinal = 0;
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageGender gender : (hasGender() ? getRequiredGenders() : ImmutableSet.of(getDefaultGender()))) {
                if (gender == LanguageGender.ANIMATE_MASCULINE)
                    continue; //we'll handle it separately, since it only needs a few cases
                for (LanguageCase caseType : getRequiredCases()) {
                    adjBuilder.add(new SlavicAdjectiveForm(this, number, gender, caseType, adjOrdinal++));
                }
            }
        }

        if (getMasculineAnimateForms() != null) {
            //add animate masculine forms, if any
            for (Map.Entry<LanguageNumber, EnumSet<LanguageCase>> entry : getMasculineAnimateForms().entrySet()) {
                for (LanguageCase caseType : entry.getValue()) {
                    adjBuilder.add(new SlavicAdjectiveForm(this, entry.getKey(), LanguageGender.ANIMATE_MASCULINE, caseType, adjOrdinal++));
                }
            }
        }

        this.adjectiveForms = adjBuilder.build();
        this.adjectiveFormMap = new ModifierFormMap<SlavicAdjectiveForm>(this.adjectiveForms);
    }


    static class SlavicNounForm extends ComplexNounForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final LanguageCase caseType;
        private final LanguageNumber number;

        public SlavicNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
        }

        @Override
        public LanguageArticle getArticle() {
            return LanguageArticle.ZERO;
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
            return LanguagePossessive.NONE;
        }

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue();
        }

        @Override
        public String toString() {
            return "SlavicNF:" + getKey();
        }
    }

    static class SlavicAdjectiveForm extends ComplexAdjectiveForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageGender gender;

        public SlavicAdjectiveForm(LanguageDeclension declension, LanguageNumber number, LanguageGender gender, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.gender = gender;
            this.caseType = caseType;
        }

        @Override
        public LanguageArticle getArticle() {
            return LanguageArticle.ZERO;
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
            return LanguagePossessive.NONE;
        }
    }

    /**
     * Represents an Slavic noun.
     * See SlavicNounForm for more info
     */
    public static class SlavicNoun extends ComplexNoun<SlavicNounForm> {
        private static final long serialVersionUID = 1L;

        SlavicNoun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected Class<SlavicNounForm> getFormClass() {
            return SlavicNounForm.class;
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        @Override
        protected boolean validateGender(String name) {
            if (getDeclension().hasGender() && !getDeclension().getRequiredGenders().contains(getGender())) {
                logger.info(VALIDATION_WARNING_HEADER + name + " invalid gender");
                setGender(getDeclension().getDefaultGender());
            }
            return true;
        }
    }

    /**
     * Represents an english adjective
     */
    public static class SlavicAdjective extends ComplexAdjective<SlavicAdjectiveForm> {
        private static final long serialVersionUID = 1L;

        SlavicAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        protected Class<SlavicAdjectiveForm> getFormClass() {
            return SlavicAdjectiveForm.class;
        }

    }


    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SlavicAdjective(this, name, position);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
                              LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SlavicNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }


    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return this.adjectiveForms;
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return this.nounForms;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return this.nounForms;
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return this.nounFormsByCase.get(NOMINATIVE);
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        assert nounForms.get(0).getCase() == NOMINATIVE && nounForms.get(0).getNumber() == SINGULAR : "Invalid case map";
        return Collections.singleton(nounForms.get(0));
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                                          LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        if (article != LanguageArticle.ZERO) return null;
        return this.adjectiveFormMap.getForm(startsWith, gender, number, _case);
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
                                     LanguageArticle article) {
        if (article != LanguageArticle.ZERO || possessive != LanguagePossessive.NONE) return null;
        return this.nounFormMap.getForm(number, _case);
    }


    @Override
    public boolean hasArticle() {
        return false;
    }

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(NEUTER, FEMININE, MASCULINE);
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    /**
     * A few slavic adjective forms use the gender masculine animate.
     *
     * @return map of number to list of cases that use that gender
     */
    public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
        return null;
    }

    static final EnumSet<LanguageCase> WEST_SLAVIC_CASES = EnumSet.of(NOMINATIVE, ACCUSATIVE, DATIVE, GENITIVE, INSTRUMENTAL, LOCATIVE, VOCATIVE);
    static final EnumSet<LanguageCase> WEST_SLAVIC_CASES_NO_VOC = EnumSet.of(NOMINATIVE, ACCUSATIVE, DATIVE, GENITIVE, INSTRUMENTAL, LOCATIVE);
    static final EnumSet<LanguageGender> WEST_SLAVIC_GENDERS = EnumSet.of(NEUTER, MASCULINE, FEMININE, ANIMATE_MASCULINE);

    static class CzechDeclension extends SlavicDeclension {
        public CzechDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return WEST_SLAVIC_CASES;
        }

        @Override
        public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
            return ImmutableMap.of(
                    LanguageNumber.PLURAL, EnumSet.of(NOMINATIVE, VOCATIVE),
                    LanguageNumber.SINGULAR, EnumSet.of(ACCUSATIVE));
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return WEST_SLAVIC_GENDERS;
        }
    }

    static class PolishDeclension extends SlavicDeclension {
        public PolishDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return WEST_SLAVIC_CASES;
        }

        @Override
        public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
            return ImmutableMap.of(
                    LanguageNumber.PLURAL, EnumSet.of(NOMINATIVE, ACCUSATIVE),
                    LanguageNumber.SINGULAR, EnumSet.of(ACCUSATIVE));
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return WEST_SLAVIC_GENDERS;
        }
    }


    static class RussianDeclension extends SlavicDeclension {
        public RussianDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(NOMINATIVE, ACCUSATIVE, DATIVE, GENITIVE, INSTRUMENTAL, PREPOSITIONAL);
        }

        @Override
        public boolean shouldLowercaseEntityInCompoundNouns() {
            return true;
        }

        @Override
        public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
            return ImmutableMap.of(LanguageNumber.PLURAL, EnumSet.of(ACCUSATIVE),
                    LanguageNumber.SINGULAR, EnumSet.of(ACCUSATIVE));
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return WEST_SLAVIC_GENDERS;
        }
    }

    static class UkrainianDeclension extends SlavicDeclension {
        public UkrainianDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return WEST_SLAVIC_CASES;
        }

        @Override
        public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
            return ImmutableMap.of(
                    LanguageNumber.PLURAL, EnumSet.of(ACCUSATIVE),
                    LanguageNumber.SINGULAR, EnumSet.of(ACCUSATIVE));
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return WEST_SLAVIC_GENDERS;
        }
    }


    static class SlovakianDeclension extends SlavicDeclension {
        public SlovakianDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return WEST_SLAVIC_CASES_NO_VOC;
        }

        @Override
        public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
            return ImmutableMap.of(
                    LanguageNumber.PLURAL, EnumSet.of(NOMINATIVE, ACCUSATIVE),
                    LanguageNumber.SINGULAR, EnumSet.of(ACCUSATIVE));
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return WEST_SLAVIC_GENDERS;
        }
    }

    static class SlovenianDeclension extends SlavicDeclension {
        public SlovenianDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return WEST_SLAVIC_CASES;
        }

        @Override
        public Map<LanguageNumber, EnumSet<LanguageCase>> getMasculineAnimateForms() {
            return ImmutableMap.of(LanguageNumber.SINGULAR, EnumSet.of(ACCUSATIVE));
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return WEST_SLAVIC_GENDERS;
        }
    }

    /**
     * SerboCroatian-like languages. (serbian, Croation, Bosnian, Montenegran
     *
     * @author stamm
     */
    static class VariantSerboCroatianDeclension extends SlavicDeclension {
        public VariantSerboCroatianDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return WEST_SLAVIC_CASES_NO_VOC;
        }
    }


    /**
     * Georgian is not Slavic (it's Kartvelian, close enough to isolate), but shares enough in common
     * with Slavic languages through Sprachbunding to be close enough.
     *
     * @author stamm
     */
    static class GeorgianDeclension extends SlavicDeclension {
        public GeorgianDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ERGATIVE, LanguageCase.DATIVE,
                    LanguageCase.GENITIVE, LanguageCase.INSTRUMENTAL, LanguageCase.ADVERBIAL);
        }

        @Override
        public boolean hasGender() {
            return false;
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return null;  // No gender in georgian.
        }
    }
}
