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
import com.google.common.collect.ImmutableMap;

/**
 * Represents a "romantic language'
 * @author yoikawa,stamm
 */
abstract class RomanceDeclension extends ArticledDeclension {
	private static EnumSet<LanguageGender> GENDER_TYPES = EnumSet.of(LanguageGender.FEMININE, LanguageGender.MASCULINE);

    public RomanceDeclension(HumanLanguage language) {
		super(language);
	}

    /**
     * Adjective form for languages that don't care about "starts with"
     */
    public static enum RomanceModifierForm implements AdjectiveForm, ArticleForm {
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE),
        PLURAL_MASCULINE(LanguageNumber.PLURAL, LanguageGender.MASCULINE),
        PLURAL_FEMININE(LanguageNumber.PLURAL, LanguageGender.FEMININE),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private RomanceModifierForm(LanguageNumber number, LanguageGender gender) {
            this.number = number;
            this.gender = gender;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguageGender getGender() {return this.gender;}
        @Override public LanguageStartsWith getStartsWith() { return LanguageStartsWith.CONSONANT; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
		@Override
		public String getKey() {
			return getNumber().getDbValue() + "-" + getGender().getDbValue();
		}

		@Override
		public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
				throws IOException {
			a.append(termFormVar+".substr(0,2)+"+genderVar);
		}
		
    }

    /**
     * <CODE>Noun</CODE> implementation for the most Latin-1 language
     */
    protected static class RomanceNoun extends SimpleArticledPluralNoun {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected RomanceNoun(RomanceDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected boolean validateGender(String name) {
            if (getGender() == LanguageGender.NEUTER) {
                //$$$ this is right, but temporary disabled because we always load English data
                //    first and that data may contain neuter. That displays huge warning
                //    message which is pretty annoying.
                //                logger.info(VALIDATION_WARNING_HEADER + name + " neuter is not expected");
                setGender(getDeclension().getDefaultGender());
            }
            return true; // accept any gender
        }

        @Override
        public void setString(String value, NounForm nid) {
            PluralNounForm rnf;
            if(nid.getArticle() != LanguageArticle.ZERO && RenamingProviderFactory.get().getProvider().supportOldGrammarEngine()) {
                rnf = (nid.getNumber() == LanguageNumber.SINGULAR) ? PluralNounForm.SINGULAR : PluralNounForm.PLURAL;
            } else {
                assert nid instanceof PluralNounForm : "Why are you trying to be romantic without providing the romance?";
                rnf = (PluralNounForm)nid;
            }
            super.setString(intern(value), rnf);
        }
    }

    protected static class RomanceAdjective extends Adjective {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<RomanceModifierForm,String> values = new EnumMap<RomanceModifierForm,String>(RomanceModifierForm.class);

        RomanceAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
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
            assert form instanceof RomanceModifierForm : "Why are you trying to be romantic without providing the romance?";
            values.put((RomanceModifierForm)form, intern(value));
        }


        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(RomanceModifierForm.SINGULAR_FEMININE));
        }
    }

    protected static class RomanceArticle extends Article {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<RomanceModifierForm,String> values = new EnumMap<RomanceModifierForm,String>(RomanceModifierForm.class);

        RomanceArticle(RomanceDeclension declension, String name, LanguageArticle articleType) {
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
            assert form instanceof RomanceModifierForm : "Why are you trying to be romantic without providing the romance?";
            values.put((RomanceModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(RomanceModifierForm.SINGULAR_FEMININE));
        }
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.FEMININE;
    }

    @Override
    public LanguagePosition getDefaultAdjectivePosition() {
        return LanguagePosition.POST;  // Romance languages are generally adjectives after (generally)...
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
     * construct new <CODE>LatinNoun</CODE>.
     */
    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new RomanceNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new RomanceAdjective(this, name, position);
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return GENDER_TYPES;
    }

    @Override
    public boolean shouldLowercaseEntityInCompoundNouns() {
        return true;
    }

    /**
     * @return a map of the indefinite article form for this romance language
     */
    protected abstract Map<? extends ArticleForm, String> getIndefiniteArticles();

    /**
     * @return a map of the definite article form for this romance language
     */
    protected abstract Map<? extends ArticleForm, String> getDefiniteArticles();

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        switch (articleType) {
        case INDEFINITE:
            return getIndefiniteArticles().get(form);
        case DEFINITE:
            return getDefiniteArticles().get(form);
        default:
            return null;
        }
    }

    // override methods for fields/entities. which requires single value
    // at all.
    @Override
    public boolean hasGender() {
        return true;
    }

    private static final List<PluralNounForm> ALL_NOUN_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    private static final List<RomanceModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(RomanceModifierForm.class));

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return ALL_NOUN_FORMS;
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return ALL_MODIFIER_FORMS;
    }


    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
            LanguageArticle article) {
        if (_case != LanguageCase.NOMINATIVE || possessive != LanguagePossessive.NONE || article != LanguageArticle.ZERO) return null;
        return number.isPlural() ? PluralNounForm.PLURAL : PluralNounForm.SINGULAR;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        // Optimize for local cases
        if (_case != LanguageCase.NOMINATIVE || startsWith != LanguageStartsWith.CONSONANT || article != LanguageArticle.ZERO) return null;
        return gender == LanguageGender.MASCULINE ? (number.isPlural() ? RomanceModifierForm.PLURAL_MASCULINE : RomanceModifierForm.SINGULAR_MASCULINE)
                : (number.isPlural() ? RomanceModifierForm.PLURAL_FEMININE : RomanceModifierForm.SINGULAR_FEMININE);
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case) {
        // The adjective forms in romance languages are not inflected by definitiveness
        return (ArticleForm) getAdjectiveForm(startsWith, gender, number, _case, LanguageArticle.ZERO, LanguagePossessive.NONE);
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    static class SpanishDeclension extends RomanceDeclension {
        public SpanishDeclension(HumanLanguage language) {
    		super(language);
    	}

        private static final EnumMap<RomanceModifierForm, String> INDEFINITE_ARTICLE =
            new EnumMap<RomanceModifierForm, String>(ImmutableMap.of(
                    RomanceModifierForm.SINGULAR_FEMININE, "Una ",
                    RomanceModifierForm.SINGULAR_MASCULINE, "Un ",
                    RomanceModifierForm.PLURAL_FEMININE, "Unas ",
                    RomanceModifierForm.PLURAL_MASCULINE, "Unos "
                    ));
        private static final EnumMap<RomanceModifierForm, String> DEFINITE_ARTICLE =
            new EnumMap<RomanceModifierForm, String>(ImmutableMap.of(
                    RomanceModifierForm.SINGULAR_FEMININE, "La ",
                    RomanceModifierForm.SINGULAR_MASCULINE, "El ",
                    RomanceModifierForm.PLURAL_FEMININE, "Las ",
                    RomanceModifierForm.PLURAL_MASCULINE, "Los "
                    ));
        @Override
        protected Map<RomanceModifierForm, String> getDefiniteArticles() {
            return DEFINITE_ARTICLE;
        }
        @Override
        protected Map<RomanceModifierForm, String> getIndefiniteArticles() {
            return INDEFINITE_ARTICLE;
        }
        @Override
        public Article createArticle(String name, LanguageArticle articleType) {
            return new RomanceArticle(this, name, articleType);
        }

    }

    static class PortugueseDeclension extends RomanceDeclension {
        public PortugueseDeclension(HumanLanguage language) {
        	super(language);
            assert language.getLocale().getLanguage().equals("pt") : "Initializing a variant portuguese declension for non-portuguese";
        }

        private static final EnumMap<RomanceModifierForm, String> INDEFINITE_ARTICLE =
            new EnumMap<RomanceModifierForm, String>(ImmutableMap.of(
                    RomanceModifierForm.SINGULAR_FEMININE, "Uma ",
                    RomanceModifierForm.SINGULAR_MASCULINE, "Um ",
                    RomanceModifierForm.PLURAL_FEMININE, "Umas ",
                    RomanceModifierForm.PLURAL_MASCULINE, "Uns "
                    ));
        private static final EnumMap<RomanceModifierForm, String> DEFINITE_ARTICLE =
            new EnumMap<RomanceModifierForm, String>(ImmutableMap.of(
                    RomanceModifierForm.SINGULAR_FEMININE, "A ",
                    RomanceModifierForm.SINGULAR_MASCULINE, "O ",
                    RomanceModifierForm.PLURAL_FEMININE, "As ",
                    RomanceModifierForm.PLURAL_MASCULINE, "Os "
                    ));
        @Override
        protected Map<RomanceModifierForm, String> getDefiniteArticles() {
            return DEFINITE_ARTICLE;
        }
        @Override
        protected Map<RomanceModifierForm, String> getIndefiniteArticles() {
            return INDEFINITE_ARTICLE;
        }
        @Override
        public Article createArticle(String name, LanguageArticle articleType) {
            return new RomanceArticle(this, name, articleType);
        }

    }
}
