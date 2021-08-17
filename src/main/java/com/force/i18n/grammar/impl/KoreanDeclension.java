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
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.LanguageDeclension.WithClassifiers;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.Noun.WithClassifier;
import com.force.i18n.grammar.impl.SimpleDeclension.SimpleNounForm;
import com.google.common.collect.ImmutableList;

/**
 * Korean is a language that's generally only inflected for the "endswith" part of the phrase, and nothing else.
 *
 * @author stamm
 * @since 0.5.0
 */
public class KoreanDeclension extends AbstractLanguageDeclension implements WithClassifiers {

	private static final Logger logger = Logger.getLogger(KoreanDeclension.class.getName());

	public KoreanDeclension(HumanLanguage language) {
		super(language);
        assert language.getLocale().getLanguage().equals("ko") : "Initializing a language that isn't korean";
	}

    /**
     * Korean particles differ based on whether the previous noun ended with a vowel or a consonant.
     * We use "adjective" for this to be simpler.
     */
    public static enum KoreanAdjectiveForm implements AdjectiveForm {
        PREV_CONSONANT(LanguageStartsWith.CONSONANT),
        PREV_VOWEL(LanguageStartsWith.VOWEL),
        PREV_FLAP(LanguageStartsWith.SPECIAL),  // ㄹ should be treated like a consonant, except with the instrumental case particle
        ;

        private final LanguageStartsWith prevEndsWith;
        private KoreanAdjectiveForm(LanguageStartsWith prevEndsWith) {
            this.prevEndsWith = prevEndsWith;
        }

        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageGender getGender() { return LanguageGender.NEUTER; }
        @Override public LanguageNumber getNumber() { return LanguageNumber.SINGULAR; }
        @Override public LanguageStartsWith getStartsWith() {return this.prevEndsWith; }
		@Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
		@Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        static KoreanAdjectiveForm getForm(ModifierForm form) {
        	switch (form.getStartsWith()) {
        	case VOWEL: return PREV_VOWEL;
        	case SPECIAL: return PREV_FLAP;
        	case CONSONANT: return PREV_CONSONANT;
        	}
        	return PREV_CONSONANT;
        }

		@Override
		public String getKey() {
			return prevEndsWith.getDbValue();
		}
		@Override
		public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
				throws IOException {
			a.append(startsWithVar);  // The noun form is the startsWithVar.
		}
    }

    /**
     * Represents an english adjective
     */
    public static class KoreanAdjective extends Adjective {
        private String prevEndsWithConsonant;
        private String prevEndsWithVowel;
        private String prevEndsWithSpecial;  // Ends with ㄹ
        private static final long serialVersionUID = -1L;

        KoreanAdjective(KoreanDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        public Map<? extends AdjectiveForm, String> getAllValues() {
            return enumMapFilterNulls(KoreanAdjectiveForm.PREV_VOWEL, prevEndsWithVowel,
            		KoreanAdjectiveForm.PREV_CONSONANT, prevEndsWithConsonant,
            		KoreanAdjectiveForm.PREV_FLAP, prevEndsWithSpecial);
        }

        @Override
        public String getString(AdjectiveForm form) {
            switch (KoreanAdjectiveForm.getForm(form)) {
            case PREV_FLAP:  return prevEndsWithSpecial;
            case PREV_VOWEL:  return prevEndsWithVowel;
            default:
            case PREV_CONSONANT:  return prevEndsWithConsonant;
            }
        }

        @Override
        protected void setString(AdjectiveForm form, String value) {
            switch (KoreanAdjectiveForm.getForm(form)) {
            case PREV_FLAP: this.prevEndsWithSpecial = intern(value);  break;
            case PREV_VOWEL: this.prevEndsWithVowel = intern(value);  break;
            default:
            case PREV_CONSONANT:  this.prevEndsWithConsonant = intern(value);
            }
        }

        @Override
        public boolean validate(String name) {
            if (this.prevEndsWithConsonant == null) {
                logger.info("###\tError: The adjective " + name + " has no form");
                return false;
            }
            if (this.prevEndsWithVowel == null) {
                this.prevEndsWithVowel = this.prevEndsWithConsonant;
            }
            if (this.prevEndsWithSpecial == null) {
                this.prevEndsWithSpecial = this.prevEndsWithConsonant;
            }
            return true;
        }
    }

    /**
     * Korean Noun's only have one value, but we want to have a place to calculate whether the noun endswith a vowel or a consonant
     * @author stamm
     */
    protected static class KoreanNoun extends Noun implements WithClassifier {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		String value;
		String classifier;
        protected KoreanNoun(KoreanDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public Map< ? extends NounForm, String> getAllDefinedValues() {
            return Collections.singletonMap(SimpleNounForm.SINGULAR, value);
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            return value;
        }

        @Override
        public final String getString(NounForm form) {
        	return this.value;
        }

        @Override
        public void setString(String value, NounForm form) {
            this.value = value;
            // Calculate endwith
            LanguageStartsWith endsWith = LanguageStartsWith.CONSONANT;  // Assume consonant
            if (value != null) {
                String trimmed = TextUtil.trim(this.value);
                // See https://en.wikipedia.org/wiki/Korean_language_and_computers#Hangul_in_Unicode
                char lastChar = trimmed.charAt(trimmed.length()-1);
                if (lastChar >= 0xAC00 && lastChar <= 0xD7A3) {
                    // Hangul composition = [{(initial) × 588} + {(medial) × 28} + (final)] + 44
                    // If the final character is 0, then it ends with a vowel, as all the finals are consonants
                    int offset = lastChar - 0xAC00;
                    int finJaeum = offset % 28;
                    if (finJaeum == 0) {
                        endsWith = LanguageStartsWith.VOWEL;
                    } else if (finJaeum == 8){
                        endsWith = LanguageStartsWith.SPECIAL; // Ends with ㄹ
                    }
                } else if (lastChar >= 0x1161 && lastChar <= 0x11A2) {
                    // If decomposed, the  the vowels are all in one area
                    // https://en.wikipedia.org/wiki/Hangul_Jamo_(Unicode_block)
                    endsWith = LanguageStartsWith.VOWEL;
                } else if (lastChar == 0x11AF) {
                    endsWith = LanguageStartsWith.SPECIAL;  // Decomposed ㄹ
                }
            }
            setStartsWith(endsWith);
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            if (this.value == null) {
                logger.info("###\tError: The noun " + name + " has no value");
                return false;
            }
            return true;
        }

        @Override
        public Noun clone() {
            KoreanNoun noun = (KoreanNoun) super.clone();
            return noun;
        }

        @Override
        public void makeSkinny() {
        }

        @Override
        public void setClassifier(String classifier) {
            this.classifier = classifier;
        }

        @Override
        public String getClassifier() {
            return this.classifier;
        }
    }

    // All the forms you can request
    private static final List<? extends NounForm> ALL_FORMS = Collections.singletonList(SimpleNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = ImmutableList.copyOf(EnumSet.of(KoreanAdjectiveForm.PREV_CONSONANT, KoreanAdjectiveForm.PREV_VOWEL, KoreanAdjectiveForm.PREV_FLAP));

    @Override public List<? extends NounForm> getAllNounForms() { return ALL_FORMS;  }

    @Override public Collection<? extends NounForm> getEntityForms() { return getAllNounForms();  }

    @Override public Collection<? extends NounForm> getFieldForms() { return getAllNounForms(); }

    @Override public Collection<? extends NounForm> getOtherForms() { return getAllNounForms(); }

    @Override public List< ? extends AdjectiveForm> getAdjectiveForms() { return ADJECTIVE_FORMS; }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
    	// It isn't starts with, it's end with here.
        return new KoreanAdjective(this, name, position);
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
    	switch (startsWith) {
    	case VOWEL: return KoreanAdjectiveForm.PREV_VOWEL;
    	case SPECIAL: return KoreanAdjectiveForm.PREV_FLAP;
    	default:
    	}
    	return KoreanAdjectiveForm.PREV_CONSONANT;
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        return SimpleNounForm.SINGULAR;
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
	public boolean hasEndsWith() {
    	return true;
	}

    @Override
    public boolean hasPlural() {
        return false;
    }

    @Override
    public boolean hasCapitalization() {
        return false;
    }

	@Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL, LanguageStartsWith.SPECIAL);  // Only generally care about consonant.
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new KoreanNoun(this, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopied);
    }

    @Override
    public String getDefaultClassifier() {
        return SimpleDeclension.getDefaultClassifier(getLanguage());
    }
}
