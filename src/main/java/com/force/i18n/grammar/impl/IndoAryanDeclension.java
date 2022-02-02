/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.AbstractLanguageDeclension;
import com.force.i18n.grammar.Adjective;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageCase;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageGender;
import com.force.i18n.grammar.LanguageNumber;
import com.force.i18n.grammar.LanguagePosition;
import com.force.i18n.grammar.LanguagePossessive;
import com.force.i18n.grammar.LanguageStartsWith;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.NounForm;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexAdjective;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexAdjectiveForm;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNoun;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNounForm;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ModifierFormMap;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.NounFormMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Implementation of most Indo-Aryan languages, those with more than 2 cases
 * (unlike Hindustani) and without articles (IndoAryan)
 *
 * As pointed out in the wiki article, there's a popularity in studying German
 * among Marathi native speakers, due to the similarities in the grammar.  Hence
 * the implementation is a lot like an article-less German.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Indo-Aryan_languages">Wikipedia: Indo-Aryan languages</a>
 * @see <a href="https://en.wikipedia.org/wiki/Gujarati_grammar#Nouns">Wikipedia: Gujarati Nouns</a>
 * @see <a href="https://en.wikipedia.org/wiki/Marathi_grammar#Nominals">Wikipedia: Marathi Nouns</a>
 * @author stamm
 * @since 1.1
 */
abstract class IndoAryanDeclension extends AbstractLanguageDeclension {
    private static final Logger logger = Logger.getLogger(IndoAryanDeclension.class.getName());

    private final List<IndoAryanNounForm> entityForms;
    private final List<IndoAryanNounForm> fieldForms;
    private final List<IndoAryanAdjectiveForm> adjectiveForms;
    private final NounFormMap<IndoAryanNounForm> nounFormMap;
    private final ModifierFormMap<IndoAryanAdjectiveForm> adjectiveFormMap;

    public IndoAryanDeclension(HumanLanguage language) {
        super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<IndoAryanNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<IndoAryanNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                IndoAryanNounForm form = new IndoAryanNounForm(this, number, caseType, ordinal++);
                entityBuilder.add(form);
                if (caseType == LanguageCase.NOMINATIVE) {
                    fieldBuilder.add(form); // Only plurals count for the fields
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
        this.nounFormMap = new NounFormMap<>(this.entityForms);

        ImmutableList.Builder<IndoAryanAdjectiveForm> adjBuilder = ImmutableList.builder();
        int adjOrdinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageGender gender : getRequiredGenders()) {
                for (LanguageCase caseType : getRequiredCases()) {
                    adjBuilder.add(new IndoAryanAdjectiveForm(this, number, gender, caseType, adjOrdinal++));
                }
            }
        }
        this.adjectiveForms = adjBuilder.build();
        this.adjectiveFormMap = new ModifierFormMap<>(this.adjectiveForms);
    }


    static class IndoAryanNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;
        private final LanguageCase caseType;
        private final LanguageNumber number;

        public IndoAryanNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE;}
        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue();
        }
        @Override
        public String toString() {
            return "IndoAryanNF:"+getKey();
        }
    }

    static class IndoAryanAdjectiveForm extends ComplexAdjectiveForm {
        private static final long serialVersionUID = 1L;
        private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageGender gender;

        public IndoAryanAdjectiveForm(LanguageDeclension declension, LanguageNumber number, LanguageGender gender, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.gender = gender;
            this.caseType = caseType;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return LanguageStartsWith.CONSONANT; }
        @Override public LanguageGender getGender() {  return this.gender; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override
        public String getKey() {
            return getGender().getDbValue() + "-" + getNumber().getDbValue() + "-" + getCase().getDbValue();
        }
    }


    /**
     * Represents an IndoAryan noun.
     * See IndoAryanNounForm for more info
     */
    public static class IndoAryanNoun extends ComplexNoun<IndoAryanNounForm> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        IndoAryanNoun(IndoAryanDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        @Override
        protected final Class<IndoAryanNounForm> getFormClass() {
            return IndoAryanNounForm.class;
        }

        @Override
        protected boolean validateGender(String name) {
            if (!getDeclension().getRequiredGenders().contains(getGender())) {
                logger.info(VALIDATION_WARNING_HEADER + name + " invalid gender");
                setGender(getDeclension().getDefaultGender());
            }
            return true;
        }
    }

    /**
     * Represents an Indo-Aryan adjective
     */
    public static class IndoAryanAdjective extends ComplexAdjective<IndoAryanAdjectiveForm> {
        private static final long serialVersionUID = 1L;
        IndoAryanAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }


        @Override
        protected final Class<IndoAryanAdjectiveForm> getFormClass() {
            return IndoAryanAdjectiveForm.class;
        }


        @Override
        public boolean validate(String name) {
            defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
            return true;
        }
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new IndoAryanAdjective(this, name, position);
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new IndoAryanNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return this.adjectiveForms;
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return this.entityForms;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return this.entityForms;
    }

    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return this.fieldForms;
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return Collections.singleton(fieldForms.get(0));  // Only need "singular" for other forms
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        return this.adjectiveFormMap.getForm(startsWith, gender, number, _case);
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
            LanguageArticle article) {
        if (possessive != LanguagePossessive.NONE) return null;
        return this.nounFormMap.getForm(number, _case);
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.MASCULINE;
    }

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }
    
    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);
    }

    
    static final class GujaratiDeclension extends IndoAryanDeclension {
        public GujaratiDeclension(HumanLanguage language) {
            super(language);
        }
        
        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.OBJECTIVE, LanguageCase.LOCATIVE);  // TODO: Locative is questionable.
        }
    }

    static final class MarathiDeclension extends IndoAryanDeclension {
        public MarathiDeclension(HumanLanguage language) {
            super(language);
        }
        
        // https://en.wikipedia.org/wiki/Marathi_grammar#Nominals
        // This needs more study.  In the meantime, I'm making it too many
        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE, LanguageCase.INSTRUMENTAL, LanguageCase.DATIVE, LanguageCase.ABLATIVE, LanguageCase.GENITIVE, LanguageCase.LOCATIVE); 
        }
    }
    
    // https://en.wikipedia.org/wiki/Punjabi_grammar
    // Nominitive = direct
    // Accusative = oblique
    static final class PunjabiDeclension extends IndoAryanDeclension {
        public PunjabiDeclension(HumanLanguage language) {
            super(language);
        }
        
        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE, LanguageCase.INSTRUMENTAL, LanguageCase.ABLATIVE, LanguageCase.VOCATIVE); 
        }
    }

}
