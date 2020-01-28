/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.SimpleDeclension.SimpleNoun;
import com.force.i18n.grammar.impl.SimpleDeclension.SimpleNounForm;
import com.google.common.collect.ImmutableList;

/**
 * For platform languages that aren't going to be supported, this class provides some simple mechanisms for
 * obtaining a bare minimum of values.
 * @author stamm
 */
abstract class UnsupportedLanguageDeclension extends ArticledDeclension {
	// All the forms you can request
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(SimpleNounForm.SINGULAR);
    // Only the simplest of adjectives and articles
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = SimpleDeclension.ADJECTIVE_FORMS;
    static final List<? extends ArticleForm> ARTICLE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);

    public UnsupportedLanguageDeclension(HumanLanguage language) {
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
        return true;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(LanguageGender.FEMININE, LanguageGender.MASCULINE);
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.FEMININE;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimpleArticledPluralNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
    }

    @Override
    protected Article createArticle(String name, LanguageArticle articleType) {
        return new SimpleArticle(this, name, articleType);
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        return number == LanguageNumber.PLURAL ? PluralNounForm.PLURAL : PluralNounForm.SINGULAR;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return ARTICLE_FORMS;
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        return "";
    }

    /**
     * Abstract class representing the insular Celtic languages (Welsh, Irish, Gaelic)
     */
    static class CelticDeclension extends UnsupportedLanguageDeclension {
        public CelticDeclension(HumanLanguage language) {
			super(language);
		}

		@Override
        public boolean hasGender() {
            return true;
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(LanguageGender.MASCULINE, LanguageGender.FEMININE);
        }
        @Override
        public LanguagePosition getDefaultAdjectivePosition() {
            return LanguagePosition.POST;
        }
    }

    /**
     * Irish has been simplified from Old Irish and Celtic, but still has a case system
     * and a special way of handling the definite article for things that start with an S or Z sound (like italian)
     * 
     * Note: we don't support the special form for nouns when used with the number ending in two (2 láimh vs 3 lámha) 
     * taking the lenited singular noun.  Google translate as of 2017 does the same thing when using the number instead of (Dhá).
     */
    static class IrishDeclension extends CelticDeclension {
        public IrishDeclension(HumanLanguage language) {
			super(language);
		}

        static final List<? extends NounForm> GA_ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(IrishNounForm.class));
        public static enum IrishNounForm implements NounForm {
            SINGULAR(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE),
            SINGULAR_GEN(LanguageNumber.SINGULAR, LanguageCase.GENITIVE),
            PLURAL(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE),
            PLURAL_GEN(LanguageNumber.PLURAL, LanguageCase.GENITIVE),
            ;

            private final LanguageNumber number;
            private final LanguageCase caseType;
            private IrishNounForm(LanguageNumber number, LanguageCase caseType) {
                this.number = number;
                this.caseType = caseType;
            }

            @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
            @Override public LanguageCase getCase() { return this.caseType; }
            @Override public LanguageNumber getNumber() {return this.number;}
            @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
            @Override
            public String getKey() {
                return getNumber().getDbValue() + "-" + getCase().getDbValue();
            }
        }

        public static final class IrishNoun extends LegacyArticledNoun {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;
			private String singular;
            private String plural;
            private String singular_gen;
            private String plural_gen;

            IrishNoun(IrishDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender,String access,  boolean isStandardField, boolean isCopiedFromDefault) {
                super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
            }

            @Override
            public void makeSkinny() {
            }

            @Override
            public Map<? extends NounForm, String> getAllDefinedValues() {
                return enumMapFilterNulls(IrishNounForm.SINGULAR, singular, IrishNounForm.PLURAL, plural, IrishNounForm.SINGULAR_GEN, singular_gen,
                        IrishNounForm.PLURAL_GEN, plural_gen);
            }
            @Override
            public String getDefaultString(boolean isPlural) {
                return isPlural ? (plural != null ? plural : singular) : singular;
            }
            @Override
            public String getExactString(NounForm form) {
                assert form instanceof IrishNounForm : "Trying to trick a leprechaun out of his gold with a non-irish noun form? " + form;
                return form.getCase() == LanguageCase.GENITIVE ? form.getNumber() == LanguageNumber.PLURAL ? plural_gen : singular_gen
                        : form.getNumber() == LanguageNumber.PLURAL ? plural : singular;
            }
            @Override
            public void setString(String value, NounForm form) {
                if (form.getCase() == LanguageCase.GENITIVE) {
                    if (form.getNumber().isPlural()) {
                        this.plural_gen = intern(value);
                    } else {
                        this.singular_gen = intern(value);
                    }
                } else {
                    if (form.getNumber().isPlural()) {
                        this.plural = intern(value);
                    } else {
                        this.singular = intern(value);
                    }
                }
            }
            @Override
            protected boolean validateValues(String name, LanguageCase _case) {
                if (this.singular == null) {
                    return false;
                }
                // Default the values for entity nouns, but not for others to make rename fields more specific.
                if (getNounType() == NounType.ENTITY) {
                    if (this.plural == null)
                     {
                        this.plural = this.singular;  // Default plural to singular.
                    }
                    // Default the singular/plural definitions to start
                    if (this.singular_gen == null) {
                        this.singular_gen = this.singular;
                    }
                    if (this.plural_gen == null) {
                        this.plural_gen = this.plural;
                    }
                }
                return true;
            }
        }


        @Override
        public List< ? extends NounForm> getAllNounForms() {
            return GA_ALL_FORMS;
        }

        @Override
        public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
            return _case == LanguageCase.GENITIVE ?
                    (number == LanguageNumber.PLURAL ? IrishNounForm.PLURAL_GEN : IrishNounForm.SINGULAR_GEN) :
                    number == LanguageNumber.PLURAL ? IrishNounForm.PLURAL : IrishNounForm.SINGULAR;
        }

        @Override
        protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
                LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            return new IrishNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
        }

        /*
        @Override
        public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
            return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL, LanguageStartsWith.SPECIAL);  // Special = 's'
        }
        */
        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.GENITIVE);
        }
    }

    /**
     * Maltese is a language that is like Arabic, but differently complicated.  Furthermore, details about its grammar
     * are harder to come by.  But it is an official EU language
     */
    static class MalteseDeclension extends UnsupportedLanguageDeclension {
        public MalteseDeclension(HumanLanguage language) {
			super(language);
		}

		@Override
        public LanguagePosition getDefaultAdjectivePosition() {
            return LanguagePosition.POST;
        }
    }


    /**
     * Basque is a language isolate that uses agglutination to form most of the words.  Our grammar
     * engine is not designed to handle it.
     */
    static class BasqueDeclension extends UnsupportedLanguageDeclension {
        public BasqueDeclension(HumanLanguage language) {
			super(language);
		}

		// Unsupported language where plurals are formed using complicated aggutinations.
        @Override
        public List< ? extends NounForm> getAllNounForms() {
            return Collections.singletonList(SimpleNounForm.SINGULAR);
        }

        @Override
        public boolean hasPlural() {
            return false;  // Basque has an "unmarked" form, to which you add singular or plural endings.
        }


        @Override
        protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            return new SimpleNoun(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
        }

        @Override
        public boolean hasGender() {
            return false;
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return null;
        }

        @Override
        public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
            return SimpleNounForm.SINGULAR;
        }
    }
    
    /**
     * Persian is an Iranian language that has a lot of arabic words added to it, where the grammar of how 
     * adjectives and plurals are made are based on the source of the word. 
     * 
     * We're going with a simplified system where we track single/plural, nom/accusative, and assume adjectives
     * are unmodified.  There are circumstances with animate and human nouns that break this.  Hence, unsupported.
     */
    static class PersianDeclension extends LanguageDeclension {
        public PersianDeclension(HumanLanguage language) {
			super(language);
		}

        static final List<? extends NounForm> FA_ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PersianNounForm.class));
        static final List<? extends NounForm> FA_SING_FORMS = ImmutableList.of(PersianNounForm.SINGULAR);
        public static enum PersianNounForm implements NounForm {
            SINGULAR(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE),
            SINGULAR_ACC(LanguageNumber.SINGULAR, LanguageCase.ACCUSATIVE),
            PLURAL(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE),
            PLURAL_ACC(LanguageNumber.PLURAL, LanguageCase.ACCUSATIVE),
            ;

            private final LanguageNumber number;
            private final LanguageCase caseType;
            private PersianNounForm(LanguageNumber number, LanguageCase caseType) {
                this.number = number;
                this.caseType = caseType;
            }

            @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
            @Override public LanguageCase getCase() { return this.caseType; }
            @Override public LanguageNumber getNumber() {return this.number;}
            @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
            @Override
            public String getKey() {
                return getNumber().getDbValue() + "-" + getCase().getDbValue();
            }
        }

        public static final class PersianNoun extends Noun {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;
			private String singular;
            private String plural;
            private String singular_acc;
            private String plural_acc;

            PersianNoun(PersianDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender,String access,  boolean isStandardField, boolean isCopiedFromDefault) {
                super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
            }

            @Override
            public void makeSkinny() {
            }

            @Override
            public Map<? extends NounForm, String> getAllDefinedValues() {
                return enumMapFilterNulls(PersianNounForm.SINGULAR, singular, PersianNounForm.PLURAL, plural, PersianNounForm.SINGULAR_ACC, singular_acc,
                        PersianNounForm.PLURAL_ACC, plural_acc);
            }
            @Override
            public String getDefaultString(boolean isPlural) {
                return isPlural ? (plural != null ? plural : singular) : singular;
            }
            
            

			@Override
			public String getString(NounForm form) {
                assert form instanceof PersianNounForm : "Persian only: " + form;
                return form.getCase() == LanguageCase.ACCUSATIVE ? form.getNumber() == LanguageNumber.PLURAL ? plural_acc : singular_acc
                        : form.getNumber() == LanguageNumber.PLURAL ? plural : singular;
            }
            @Override
            public void setString(String value, NounForm form) {
                if (form.getCase() == LanguageCase.ACCUSATIVE) {
                    if (form.getNumber().isPlural()) {
                        this.plural_acc = intern(value);
                    } else {
                        this.singular_acc = intern(value);
                    }
                } else {
                    if (form.getNumber().isPlural()) {
                        this.plural = intern(value);
                    } else {
                        this.singular = intern(value);
                    }
                }
            }
            @Override
            protected boolean validateValues(String name, LanguageCase _case) {
                if (this.singular == null) {
                    return false;
                }
                // Default the values for entity nouns, but not for others to make rename fields more specific.
                if (getNounType() == NounType.ENTITY) {
                    if (this.plural == null)
                     {
                        this.plural = this.singular;  // Default plural to singular.
                    }
                    // Default the singular/plural definitions to start
                    if (this.singular_acc == null) {
                        this.singular_acc = this.singular;
                    }
                    if (this.plural_acc == null) {
                        this.plural_acc = this.plural;
                    }
                }
                return true;
            }
        }


        @Override
        public List< ? extends NounForm> getAllNounForms() {
            return FA_ALL_FORMS;
        }

        @Override
        public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
            return _case == LanguageCase.ACCUSATIVE ?
                    (number == LanguageNumber.PLURAL ? PersianNounForm.PLURAL_ACC : PersianNounForm.SINGULAR_ACC) :
                    number == LanguageNumber.PLURAL ? PersianNounForm.PLURAL : PersianNounForm.SINGULAR;
        }

        @Override
        protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
                LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            return new PersianNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE);
        }
        
		@Override
		public boolean hasStartsWith() {
			return false;
		}

        @Override
        public boolean hasGender() {
            return false;
        }

		@Override
		public Collection<? extends NounForm> getEntityForms() {
			return getAllNounForms();
		}

		@Override
		public Collection<? extends NounForm> getFieldForms() {
			return FA_SING_FORMS;
		}

		@Override
		public Collection<? extends NounForm> getOtherForms() {
			return FA_SING_FORMS;
		}

		@Override
		public List<? extends AdjectiveForm> getAdjectiveForms() {
			return SimpleDeclension.ADJECTIVE_FORMS;
		}

		@Override
		protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
			return new SimpleAdjective(this, name);
		}
    }

}
