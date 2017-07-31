/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.commons.text.CaseFolder;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.collect.*;

/**
 * Provide a declension system for a germanic language.  Generally, there are
 *
 * four cases: Nominative, Accusative, Dative, Genetive
 * Three genders: Masculine, Feminine, Neuter
 * articles are auto-derived
 *
 * To "simplify" matters,
 *
 * @author stamm
 */
abstract class GermanicDeclension extends ArticledDeclension {
    private static final Logger logger = Logger.getLogger(GermanicDeclension.class.getName());

    private final List<GermanicNounForm> entityForms;
    private final List<GermanicNounForm> fieldForms;
    private final List<GermanicAdjectiveForm> adjectiveForms;
    private final List<GermanicArticleForm> articleForms;
    private final EnumMap<LanguageArticle,NounFormMap<GermanicNounForm>> nounFormMap;
    private final EnumMap<LanguageArticle,ModifierFormMap<GermanicAdjectiveForm>> adjectiveFormMap;
    private final ModifierFormMap<GermanicArticleForm> articleFormMap;

    public GermanicDeclension(HumanLanguage language) {
    	super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<GermanicNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<GermanicNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguageArticle article : getRequiredNounArticles()) {
                    GermanicNounForm form = new GermanicNounForm(this, number, caseType, article, ordinal++);
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

        ImmutableList.Builder<GermanicAdjectiveForm> adjBuilder = ImmutableList.builder();
        int adjOrdinal = 0;
        for (LanguageStartsWith startsWith : getRequiredStartsWith()) {
            for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
                for (LanguageGender gender : getRequiredGenders()) {
                    for (LanguageArticle article : getRequiredAdjectiveArticles()) {
                        for (LanguageCase caseType : getRequiredCases()) {
                            adjBuilder.add(new GermanicAdjectiveForm(this, number, gender, article, caseType, startsWith, adjOrdinal++));
                        }
                    }
                }
            }
        }
        this.adjectiveForms = adjBuilder.build();
        this.adjectiveFormMap = ModifierFormMap.getArticleSpecificMap(this.adjectiveForms);

        ImmutableList.Builder<GermanicArticleForm> artBuilder = ImmutableList.builder();
        int artOrdinal = 0;
        for (LanguageStartsWith startsWith : getRequiredStartsWith()) {
            for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
                for (LanguageGender gender : getRequiredGenders()) {
                    for (LanguageCase caseType : getRequiredCases()) {
                        artBuilder.add(new GermanicArticleForm(this, number, gender, caseType, startsWith, artOrdinal++));
                    }
                }
            }
        }
        this.articleForms = artBuilder.build();
        this.articleFormMap = new ModifierFormMap<GermanicArticleForm>(this.articleForms);
    }


    static class GermanicNounForm extends ComplexNounForm {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguageArticle article;

        public GermanicNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguageArticle article, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.article = article;
        }

        @Override public LanguageArticle getArticle() { return this.article; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE;}
        @Override
        public String getKey() {
            if (((GermanicDeclension)getDeclension()).getRequiredNounArticles().size() > 1) {
                return getNumber().getDbValue() + "-" + getCase().getDbValue() + getArticle().getDbValue();
            } else {
                return getNumber().getDbValue() + "-" + getCase().getDbValue();
            }
        }
        @Override
        public String toString() {
            return "GermanicNF:"+getKey();
        }
    }

    static class GermanicAdjectiveForm extends ComplexAdjectiveForm {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final LanguageNumber number;
        private final LanguageArticle article;
        private final LanguageCase caseType;
        private final LanguageGender gender;
        private final LanguageStartsWith startsWith;

        public GermanicAdjectiveForm(LanguageDeclension declension, LanguageNumber number, LanguageGender gender, LanguageArticle article, LanguageCase caseType, LanguageStartsWith startsWith, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.article = article;
            this.gender = gender;
            this.caseType = caseType;
            this.startsWith = startsWith;
        }

        @Override public LanguageArticle getArticle() { return this.article; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return this.startsWith; }
        @Override public LanguageGender getGender() {  return this.gender; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
    }

    static class GermanicArticleForm extends ComplexArticleForm {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageGender gender;
        private final LanguageStartsWith startsWith;

        public GermanicArticleForm(LanguageDeclension declension, LanguageNumber number, LanguageGender gender, LanguageCase caseType, LanguageStartsWith startsWith, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.gender = gender;
            this.caseType = caseType;
            this.startsWith = startsWith;
        }

        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return this.startsWith; }
        @Override public LanguageGender getGender() {  return this.gender; }
    }

    /**
     * Represents an Germanic noun.
     * See GermanicNounForm for more info
     */
    public static class GermanicNoun extends ComplexArticledNoun<GermanicNounForm> {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
        GermanicNoun(GermanicDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        @Override
		protected final Class<GermanicNounForm> getFormClass() {
        	return GermanicNounForm.class;
		}

		@Override
        protected boolean validateGender(String name) {
            if (!getDeclension().getRequiredGenders().contains(getGender())) {
                logger.info(VALIDATION_WARNING_HEADER + name + " invalid gender");
                setGender(getDeclension().getDefaultGender());
            }
            return true;
        }

        @Override
        protected String appendArticleToBase(String base, String article, NounForm form) {
            if (article == null) return base;
            return article + " " + base;  // Nouns in germanic language remain in uppercase and have a space
        }
    }

    /**
     * Represents an english adjective
     */
    public static class GermanicAdjective extends ComplexAdjective<GermanicAdjectiveForm> {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
        GermanicAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }


        @Override
		protected final Class<GermanicAdjectiveForm> getFormClass() {
        	return GermanicAdjectiveForm.class;
		}


		@Override
        public boolean validate(String name) {
            defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
            return true;
        }
    }

    /**
     * Represents an english adjective
     */
    public static class GermanicArticle extends Article {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Map<GermanicArticleForm, String> values = new HashMap<GermanicArticleForm,String>();

        GermanicArticle(ArticledDeclension declension, String name, LanguageArticle articleType) {
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
            values.put((GermanicArticleForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            defaultValidate(name, ImmutableSet.of(getDeclension().getArticleForm(LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE)));
            return true;
        }
    }
    /**
     * @return the set of articles that the adjectives need to define in the label files.
     * Generally, this would be the zero and definite article for adjective agreement rules
     */
    protected abstract EnumSet<LanguageArticle> getRequiredAdjectiveArticles();


    private static final EnumSet<LanguageArticle> ZERO_ARTICLES = EnumSet.of(LanguageArticle.ZERO);
    static final EnumSet<LanguageArticle> ZERO_AND_DEFARTICLES = EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE);
    static final EnumSet<LanguageArticle> ALL_ARTICLES = EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE, LanguageArticle.INDEFINITE);
    /**
     * NOTE: Only define this for languages where the article is irregular and a suffix (such as Dutch)
     * @return the set of articles that the adjectives need to define in the label files
     */
    protected EnumSet<LanguageArticle> getRequiredNounArticles() {
        return ZERO_ARTICLES;
    }

    @Override
    public
    final boolean hasArticleInNounForm() {
        return getRequiredNounArticles().size() > 1;
    }


    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new GermanicAdjective(this, name, position);
    }

    @Override
    protected Article createArticle(String name, LanguageArticle articleType) {
        return new GermanicArticle(this, name, articleType);
    }


    /* (non-Javadoc)
     * @see i18n.grammar.LanguageDeclension#createNoun(i18n.grammar.Noun.NounType, i18n.grammar.LanguageStartsWith, i18n.grammar.LanguageGender, java.lang.String, boolean)
     */
    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new GermanicNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
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
    public List<? extends ArticleForm> getArticleForms() {
        return this.articleForms;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        ModifierFormMap<? extends AdjectiveForm> formMap = this.adjectiveFormMap.get(article);
        return formMap == null ? null : formMap.getForm(startsWith, gender, number, _case);
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case) {
        return this.articleFormMap.getForm(startsWith, gender, number, _case);
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
            LanguageArticle article) {
        if (possessive != LanguagePossessive.NONE) return null;
        NounFormMap<? extends NounForm> formMap = this.nounFormMap.get(article);
        return formMap == null ? null : formMap.getForm(number, _case);
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.NEUTER;
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
    public boolean shouldLowercaseEntityInCompoundNouns() {
        return true;
    }


    static class GermanDeclension extends GermanicDeclension {
        public GermanDeclension(HumanLanguage language) {
            super(language);
            assert language.getLocale().getLanguage().equals("de") : "Initializing a variant german declension for non-german";
        } 
        
        private static final Map<LanguageCase, ImmutableMap<LanguageNumber, ImmutableMap<LanguageGender,String>>> DEFINITE_ARTICLE =
            ImmutableMap.of(
                   LanguageCase.NOMINATIVE,
                            ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                    LanguageGender.NEUTER, "Das",
                                    LanguageGender.FEMININE, "Die",
                                    LanguageGender.MASCULINE, "Der"
                                    ), LanguageNumber.PLURAL, ImmutableMap.of(
                                    LanguageGender.NEUTER, "Die",LanguageGender.FEMININE, "Die",LanguageGender.MASCULINE, "Die"
                                    )),
                    LanguageCase.ACCUSATIVE,
                            ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                    LanguageGender.NEUTER, "Das",
                                    LanguageGender.FEMININE, "Die",
                                    LanguageGender.MASCULINE, "Den"
                                    ), LanguageNumber.PLURAL, ImmutableMap.of(
                                    LanguageGender.NEUTER, "Die", LanguageGender.FEMININE, "Die", LanguageGender.MASCULINE, "Die"
                                    )),
                   LanguageCase.GENITIVE,
                            ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                    LanguageGender.NEUTER, "Des",
                                    LanguageGender.FEMININE, "Der",
                                    LanguageGender.MASCULINE, "Des"
                                    ), LanguageNumber.PLURAL, ImmutableMap.of(
                                    LanguageGender.NEUTER, "Der",LanguageGender.FEMININE, "Der", LanguageGender.MASCULINE, "Der"
                                    )),
                   LanguageCase.DATIVE,
                           ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                   LanguageGender.NEUTER, "Dem",
                                   LanguageGender.FEMININE, "Der",
                                   LanguageGender.MASCULINE, "Dem"
                                   ), LanguageNumber.PLURAL, ImmutableMap.of(
                                   LanguageGender.NEUTER, "Den", LanguageGender.FEMININE, "Den", LanguageGender.MASCULINE, "Den"
                                   )));

        private static final Map<LanguageCase, ImmutableMap<LanguageGender, String>> INDEFINITE_ARTICLE =
            ImmutableMap.of(
                   LanguageCase.NOMINATIVE,
                            ImmutableMap.of(
                                    LanguageGender.NEUTER, "Ein",
                                    LanguageGender.FEMININE, "Eine",
                                    LanguageGender.MASCULINE, "Ein"
                                    ),
                    LanguageCase.ACCUSATIVE,
                            ImmutableMap.of(
                                    LanguageGender.NEUTER, "Ein",
                                    LanguageGender.FEMININE, "Eine",
                                    LanguageGender.MASCULINE, "Einen"
                                    ),
                   LanguageCase.GENITIVE,
                            ImmutableMap.of(
                                    LanguageGender.NEUTER, "Eines",
                                    LanguageGender.FEMININE, "Einer",
                                    LanguageGender.MASCULINE, "Eines"
                                    ),
                   LanguageCase.DATIVE,
                          ImmutableMap.of(
                                   LanguageGender.NEUTER, "Einem",
                                   LanguageGender.FEMININE, "Einer",
                                   LanguageGender.MASCULINE, "Einem"
                                   ));

        @Override
        protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            switch (articleType) {
            case DEFINITE:
                return DEFINITE_ARTICLE.get(form.getCase()).get(form.getNumber()).get(form.getGender());
            case INDEFINITE:
                if (form.getNumber() == LanguageNumber.PLURAL) return null;
                return INDEFINITE_ARTICLE.get(form.getCase()).get(form.getGender());
            default:
                return null;
            }
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE, LanguageCase.GENITIVE, LanguageCase.DATIVE);
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);
        }

        @Override
        public boolean shouldLowercaseEntityInCompoundNouns() {
            return false;
        }

        @Override
        public String formLowercaseNounForm(String s, NounForm form) {
            if (s == null || s.length() == 0) return s;
            // German capitalizes nouns even with an article, which differs from most languages
            if (form.getArticle() != LanguageArticle.ZERO) {
                return CaseFolder.toFoldedCase(s.substring(0, 1)) + s.substring(1);
            } else {
                return s;
            }
        }
    }

    static class SwedishDeclension extends GermanicDeclension {
        public SwedishDeclension(HumanLanguage language) {
			super(language);
	        assert language.getLocale().getLanguage().equals("sv") : "Initializing a language that isn't swedish";
		}

		public static final LanguageGender EUTER = LanguageGender.FEMININE;

        @Override
        protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        protected EnumSet<LanguageArticle> getRequiredNounArticles() {
            return ALL_ARTICLES;
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, EUTER);  // Feminine is "EUTER"
        }
    }

    static class DutchDeclension extends GermanicDeclension {
        public DutchDeclension(HumanLanguage language) {
			super(language);
	        assert language.getLocale().getLanguage().equals("nl") : "Initializing a language that isn't dutch";
		}

		@Override
        protected final EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        public final EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, LanguageGender.COMMON);  // Feminine is "COMMON"
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            switch (articleType) {
            case DEFINITE:
                if (form.getNumber() == LanguageNumber.SINGULAR && form.getGender() == LanguageGender.NEUTER) return "Het";
                return "De";
            case INDEFINITE:
                if (form.getNumber() == LanguageNumber.PLURAL) return null;
                return "Een";
            default:
                return null;
            }
        }
    }

    static class DanishDeclension extends GermanicDeclension {
        public DanishDeclension(HumanLanguage language) {
			super(language);
	        assert language.getLocale().getLanguage().equals("da") : "Initializing a language that isn't danish";
		}

		@Override
        protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, LanguageGender.COMMON);  // Feminine is "COMMON"
        }

        @Override
        protected EnumSet<LanguageArticle> getRequiredNounArticles() {
            return ALL_ARTICLES;
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
        }

    }

    static class NorwegianDeclension extends GermanicDeclension {
        public NorwegianDeclension(HumanLanguage language) {
			super(language);
		}

		@Override
        protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);  // Feminine is "COMMON"
        }

        @Override
        protected EnumSet<LanguageArticle> getRequiredNounArticles() {
            return ALL_ARTICLES;
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
        }

        @Override
        public boolean shouldInferNounDefArticleFromParticle() {
            return true;
        }
    }

    static class IcelandicDeclension extends GermanicDeclension {
        public IcelandicDeclension(HumanLanguage language) {
			super(language);
		}

		@Override
        protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);  // Feminine is "COMMON"
        }


        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE, LanguageCase.GENITIVE, LanguageCase.DATIVE);
        }

        @Override
        protected EnumSet<LanguageArticle> getRequiredNounArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
        }

        @Override
        public boolean shouldInferNounDefArticleFromParticle() {
            return true;
        }
    }

    static class LuxembourgishDeclension extends GermanicDeclension {
        public LuxembourgishDeclension(HumanLanguage language) {
			super(language);
		}

		private static final Map<LanguageCase, ImmutableMap<LanguageNumber, ImmutableMap<LanguageGender,String>>> DEFINITE_ARTICLE =
            ImmutableMap.of(
                   LanguageCase.NOMINATIVE,
                            ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                    LanguageGender.NEUTER, "D'",
                                    LanguageGender.FEMININE, "D'",
                                    LanguageGender.MASCULINE, "Den"
                                    ), LanguageNumber.PLURAL, ImmutableMap.of(
                                    LanguageGender.NEUTER, "D'",LanguageGender.FEMININE, "D'",LanguageGender.MASCULINE, "D'"
                                    )),
                   LanguageCase.DATIVE,
                           ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                   LanguageGender.NEUTER, "Dem",
                                   LanguageGender.FEMININE, "Der",
                                   LanguageGender.MASCULINE, "Dem"
                                   ), LanguageNumber.PLURAL, ImmutableMap.of(
                                   LanguageGender.NEUTER, "Den", LanguageGender.FEMININE, "Den", LanguageGender.MASCULINE, "Den"
                                   )));

        private static final Map<LanguageCase, ImmutableMap<LanguageGender, String>> INDEFINITE_ARTICLE =
            ImmutableMap.of(
                   LanguageCase.NOMINATIVE,
                            ImmutableMap.of(
                                    LanguageGender.NEUTER, "En",
                                    LanguageGender.FEMININE, "Eng",
                                    LanguageGender.MASCULINE, "En"
                                    ),
                   LanguageCase.DATIVE,
                          ImmutableMap.of(
                                   LanguageGender.NEUTER, "Engem",
                                   LanguageGender.FEMININE, "Enger",
                                   LanguageGender.MASCULINE, "Engem"
                                   ));

        @Override
        protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
            return ZERO_AND_DEFARTICLES;
        }

        @Override
        protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            switch (articleType) {
            case DEFINITE:
                return DEFINITE_ARTICLE.get(form.getCase()).get(form.getNumber()).get(form.getGender());
            case INDEFINITE:
                if (form.getNumber() == LanguageNumber.PLURAL) return null;
                return INDEFINITE_ARTICLE.get(form.getCase()).get(form.getGender());
            default:
                return null;
            }
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.DATIVE);
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);
        }
    }
}
