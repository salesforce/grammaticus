/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.grammar.LanguageCase.*;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The declension of nouns for Amharic.  Not fully supported.
 *
 * Amharic has 3 cases (Nom, Acc, &amp; Gen), M/F Gender, plural, and definitiveness.
 * It has bound possessive forms differening based on case, gender, politeness, etc.  
 * So we're not going to support it directly.  Definitiveness moves to prepositional
 * adjectives depending.
 *
 * Case endings could be derived, but it would be a fair amount of work to automatically
 * derive the vowel changing endings from the Ethiopic bloc.  
 *
 * @author stamm
 * @since 1.2
 */
class AmharicDeclension extends SemiticDeclension {
    private final List<AmharicNounForm> nounForms;
    private final List<AmharicNounForm> entityForms;
    private final List<AmharicAdjectiveForm> adjectiveForms;

    public AmharicDeclension(HumanLanguage language) {
    	super(language);
        assert language.getLocale().getLanguage().equals("am") : "Initializing a variant Amharic declension for non-Amharic";

        // Generate the different forms from subclass methods
        ImmutableList.Builder<AmharicNounForm> nounBuilder = ImmutableList.builder();
        ImmutableList.Builder<AmharicNounForm> entityNounBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getAllowedCases()) {
                for (LanguagePossessive possessive : getRequiredPossessive()) {
                    for (LanguageArticle article : getAllowedArticleTypes()) {
                        AmharicNounForm form = new AmharicNounForm(this, number, caseType, possessive, article, ordinal++);
                        if (caseType == LanguageCase.NOMINATIVE) entityNounBuilder.add(form);  // TODO: Which forms *must* be specified?
                        nounBuilder.add(form);
                    }
                }
            }
        }
        this.nounForms = nounBuilder.build();
        this.entityForms = entityNounBuilder.build();

        ordinal=0;
        ImmutableList.Builder<AmharicAdjectiveForm> adjBuilder = ImmutableList.builder();
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageGender gender : getRequiredGenders()) {
                for (LanguageCase caseType : getRequiredCases()) {
                    for (LanguageArticle article : getAllowedArticleTypes()) {
                        for (LanguagePossessive possessive : getRequiredPossessive()) {
                            AmharicAdjectiveForm form = new AmharicAdjectiveForm(this, gender, number, caseType, article, possessive, ordinal++);
                            adjBuilder.add(form);
                        }
                    }
                }
            }
        }
        this.adjectiveForms = adjBuilder.build();
    }


    /**
     * Amharic nouns are inflected for case, number, possessive, and article.  Everything that is
     */
    static class AmharicNounForm extends ComplexNounForm {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguagePossessive possesive;
        private final LanguageArticle article;

        public AmharicNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguagePossessive possesive, LanguageArticle article, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.possesive = possesive;
            this.article = article;
        }

        @Override public LanguageArticle getArticle() { return this.article; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguagePossessive getPossessive() { return possesive;}
    }

    /**
     * Amharic nouns are inflected for case, number, gender, and article.  Oh my.
     */
    static class AmharicAdjectiveForm extends ComplexAdjectiveForm {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final LanguageGender gender;
        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguageArticle article;
        private final LanguagePossessive possessive;

        public AmharicAdjectiveForm(LanguageDeclension declension, LanguageGender gender, LanguageNumber number, LanguageCase caseType,  LanguageArticle article, LanguagePossessive possessive, int ordinal) {
            super(declension, ordinal);
            this.gender = gender;
            this.number = number;
            this.caseType = caseType;
            this.article = article;
            this.possessive = possessive;
        }

        @Override public LanguageArticle getArticle() { return this.article; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return LanguageStartsWith.CONSONANT; }
        @Override public LanguageGender getGender() {  return this.gender; }
        @Override public LanguagePossessive getPossessive() { return possessive; }
		@Override
		public String getKey() {
			return getGender().getDbValue() + "-" + getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getArticle().getDbValue() + "-" + getPossessive().getDbValue();
		}
    }

    public static final class AmharicNoun extends ComplexArticledNoun<AmharicNounForm> {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private static final Logger logger = Logger.getLogger(AmharicNoun.class.getName());
        AmharicNoun(AmharicDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender,  String access, boolean isStandardField, boolean isCopied) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopied);
        }

        @Override
		protected final Class<AmharicNounForm> getFormClass() {
        	return AmharicNounForm.class;
		}


		@Override
        public String getExactString(NounForm form) {
            return super.getExactString(form);
        }


        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            Collection<? extends NounForm> requiredForms = getDeclension().getFieldForms();
            // Default the definitive case if necessary.
            for (NounForm form : getDeclension().getAllNounForms()) {
                if (form.getCase() == LanguageCase.ACCUSATIVE) continue;  // Ignore the accusative, since it's autoderived
                String value = getExactString(form);
                if (value == null) {
                    if (getNounType() == NounType.ENTITY) {
                    	// Only do the "defaulting" on entities because the "entity"
                    	// value in sfdcnames.xml usually only specifies 2 forms
                    	String val = getCloseButNoCigarString(form);
                    	if (val == null) {
                    		logger.info("###\tError: The noun " + name + " has no " + form
                    					+ " form and no default could be found");
                    		return false;
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


    protected static class AmharicAdjective extends ComplexAdjective<AmharicAdjectiveForm> {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		// The "keys" here are StartsWith, Gender, and Plurality
        AmharicAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
		protected final Class<AmharicAdjectiveForm> getFormClass() {
        	return AmharicAdjectiveForm.class;
		}

		@Override
        protected String deriveDefaultString(AdjectiveForm form, String value, AdjectiveForm baseForm) {
			// TODO SLT: Derive adjectives where we can
			return super.deriveDefaultString(form, value, baseForm);
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.FEMININE, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
        }
    }


    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new AmharicAdjective(this, name, position);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
            LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new AmharicNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    static final EnumSet<LanguagePossessive> REQUIRED_POSESSIVES = EnumSet.of(LanguagePossessive.NONE, LanguagePossessive.FIRST, LanguagePossessive.SECOND);
    static final EnumSet<LanguageCase> ALLOWED_CASES = EnumSet.of(NOMINATIVE, ACCUSATIVE, GENITIVE); 
    static final EnumSet<LanguageCase> REQUIRED_CASES = EnumSet.of(NOMINATIVE);  // Only do nomitive.

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
    public List< ? extends NounForm> getAllNounForms() {
        return nounForms;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return entityForms;
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return nounForms;
    }

	@Override
	protected String getDefiniteArticlePrefix(LanguageStartsWith startsWith) {
		// Amharic, unlike the related Tigrinya (እታ), doesn't have a definite article prefix,
		// but it has a suffix which changes the last character of the word or adds one depending on 
		// gender and case.  
		// A boy (ልጅ).  The boy (ልጁ).  
		// A dog (ውሻ).  The dog (ውሻው).
		// When full support is required
		return "";
	}
    
}
