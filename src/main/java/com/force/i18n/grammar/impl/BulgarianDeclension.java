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
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;

/**
 * Bulgarian is a slavic declension that has been affected the most by the
 * Balkan Sprachbund, making it rather non-slavic.  The case distinction is fairly degenerate,
 * so that the customer doesn't actually have to specify it.
 * <p>
 * The *big* issue is that in the masculine, there is a distinction between whether the noun phrase
 * is the subject of the sentence or in some other part.  The translator will have to specify
 * case="o" for any noun/adjective that is not the subject of a sentence.
 * <p>
 * Gender: Masculine, Feminine and Neuter
 * Number: Singular and plural (and dual, but let's not talk about that, ok?)
 * Cases: Nominative (subjective) vs Objective (that's not how they think).   So we're not actually having cases.
 * Articles:
 * There is no indefinite article
 * <p>
 * There are six forms
 * The singular masculine Long form (noun is subject of sentence)
 * The singular masculine Short form (noun is any other part of sentence)
 * The feminine form - та
 * The neuter form - то
 * The plural form - те
 * The neuter plural form exception- ending in A/-Я/-TA - та
 * <p>
 * Definiteness in a noun / adjective phrase is expressed on the first word in the phrase.
 * <p>
 * The red car
 * red(def ending) car
 * the car is red
 * car (def eding) is red
 * <p>
 * names.xml will have to specify the following forms
 *
 * <adjective name="Thin">
 * <value gender="m" plural="n">слаб</value>
 * <value gender="m" plural="n" article="the">слабият</value>
 * <value gender="f" plural="n">слаба</value>
 * <value gender="n" plural="n">слабо</value>
 * <value gender="n" plural="y">слаби</value>
 * </adjective>
 * <p>
 * The rest of the forms will be autoderived:  NOTE: The plural must have the gender omitted or marked as neuter
 * <p>
 * Note: Macedonian is similar to Bulgarian (it's a little more complicated, but it's
 * another Balkan Sprachbund language)
 *
 * @author stamm
 */
class BulgarianDeclension extends LanguageDeclension {
    public BulgarianDeclension(HumanLanguage language) {
        super(language);
    }

    private static final Logger logger = Logger.getLogger(BulgarianDeclension.class.getName());

    /**
     * Adjective form for languages that don't care about "starts with"
     */
    public static enum BulgarianModifierForm implements AdjectiveForm {
        // TODO: We're not going to have to ask the translator for all of these forms.
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE),
        SINGULAR_NEUTER(LanguageNumber.SINGULAR, LanguageGender.NEUTER),
        PLURAL_NEUTER(LanguageNumber.PLURAL, LanguageGender.NEUTER),
        SINGULAR_MASCULINE_DEF(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageArticle.DEFINITE),
        SINGULAR_MASCULINE_OBJ_DEF(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageArticle.DEFINITE, LanguageCase.OBJECTIVE),
        SINGULAR_FEMININE_DEF(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageArticle.DEFINITE),
        SINGULAR_NEUTER_DEF(LanguageNumber.SINGULAR, LanguageGender.NEUTER, LanguageArticle.DEFINITE),
        PLURAL_NEUTER_DEF(LanguageNumber.PLURAL, LanguageGender.NEUTER, LanguageArticle.DEFINITE),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private final LanguageArticle article;
        private final LanguageCase caseType;

        private BulgarianModifierForm(LanguageNumber number, LanguageGender gender) {
            this(number, gender, LanguageArticle.ZERO);
        }

        private BulgarianModifierForm(LanguageNumber number, LanguageGender gender, LanguageArticle article) {
            this(number, gender, article, LanguageCase.NOMINATIVE);
        }

        private BulgarianModifierForm(LanguageNumber number, LanguageGender gender, LanguageArticle article, LanguageCase caseType) {
            this.number = number;
            this.gender = gender;
            this.article = article;
            this.caseType = caseType;
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
        public LanguageGender getGender() {
            return this.gender;
        }

        @Override
        public LanguageStartsWith getStartsWith() {
            return LanguageStartsWith.CONSONANT;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return LanguagePossessive.NONE;
        }
    }

    public static enum BulgarianNounForm implements NounForm {
        SINGULAR(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE),
        PLURAL(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE),
        SINGULAR_DEF(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.DEFINITE),
        SINGULAR_OBJ_DEF(LanguageNumber.SINGULAR, LanguageCase.OBJECTIVE, LanguageArticle.DEFINITE),  // Only used in the masculine.
        PLURAL_DEF(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguageArticle.DEFINITE),
        ;

        private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageArticle article;

        BulgarianNounForm(LanguageNumber number, LanguageCase caseType) {
            this(number, caseType, LanguageArticle.ZERO);
        }

        BulgarianNounForm(LanguageNumber number, LanguageCase caseType, LanguageArticle article) {
            this.number = number;
            this.caseType = caseType;
            this.article = article;
        }

        @Override
        public LanguageArticle getArticle() {
            return article;
        }

        @Override
        public LanguageCase getCase() {
            return this.caseType;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return LanguagePossessive.NONE;
        }

        @Override
        public LanguageNumber getNumber() {
            return this.number;
        }

        @Override
        public String getKey() {
            return getNumber().getDbValue();
        }
    }

    /**
     * Bulgarian Noun formation is complicated, but regular.  Given the singular and plural value, it is
     * relatively straightforward to determine the remaining forms beyond singular and plural
     *
     * @author stamm
     */
    protected static class BulgarianNoun extends Noun {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        String singular;
        String plural;

        protected BulgarianNoun(BulgarianDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            if (plural == null) {
                if (isStandardField()) {
                    logger.finest("Bulgarian noun " + getName() + " has no plural but is a standard field");
                }
                return Collections.singletonMap(BulgarianNounForm.SINGULAR, singular);
            } else {
                return enumMapFilterNulls(BulgarianNounForm.SINGULAR, singular, BulgarianNounForm.PLURAL, plural);
            }
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            return isPlural ? (plural != null ? plural : singular) : singular;
        }

        @Override
        public final String getString(NounForm form) {
            assert form instanceof BulgarianNounForm : "You must provide a Bulgarian noun form for a Bulgarian noun";

            // here's the magic to automagically append the correct ending
            String baseForm = form.getNumber() == LanguageNumber.SINGULAR ? singular : plural;
            if (form.getArticle() == LanguageArticle.DEFINITE) {
                // Here's where we get the "real" string.
                if (baseForm == null || baseForm.length() == 0) {
                    return baseForm;  // TODO: Log this?
                }
                char lastChar = baseForm.charAt(baseForm.length() - 1);
                // If it ends with an й, then you have to strip it off
                boolean baseFormEndsWithI = lastChar == '\u0419' || lastChar == '\u0439';
                boolean baseFormEndsWithA = lastChar == '\u044f' || lastChar == '\u0430';  // ends with A or Я
                boolean baseFormEndsWithO = lastChar == '\u043e';

                if (form.getNumber() == LanguageNumber.SINGULAR) {
                    if (baseFormEndsWithI) {
                        baseForm = baseForm.substring(0, baseForm.length() - 1);
                    }
                    switch (this.getGender()) {
                        case MASCULINE:
                        case ANIMATE_MASCULINE: //fall through
                            if (baseFormEndsWithA) {
                                return baseForm + "\u0442\u0430";  // ТА if ends with A
                            } else if (baseFormEndsWithO) {
                                return baseForm + "\u0442\u043e";  // то if ends with O
                            } else if (form.getCase() == LanguageCase.OBJECTIVE) {
                                return baseForm + (baseFormEndsWithI ? "\u044f" : "\u0430");  // Я if ends with й, А otherwise
                            } else {
                                return baseForm + (baseFormEndsWithI ? "\u044f\u0442" : "\u044a\u0442");  // ЯТ if ends with й, ЪТ otherwise
                            }
                        case FEMININE:
                            return baseForm + "\u0442\u0430";  // -ТА always
                        case NEUTER:
                            return baseForm + "\u0442\u043e";  // -то always
                    }
                } else {
                    return baseForm + (baseFormEndsWithA ? "\u0442\u0430" : "\u0442\u0435");  // -TE except if it ends with A
                }
            }
            return baseForm;
        }

        @Override
        public void setString(String value, NounForm form) {
            if (form.getArticle() == LanguageArticle.ZERO) {
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
                logger.info("###\tError: The noun " + name + " has no singular form");
                return false;
            }
            return true;
        }

        @Override
        public Noun clone() {
            BulgarianNoun noun = (BulgarianNoun) super.clone();
            return noun;
        }

        @Override
        public void makeSkinny() {
        }
    }


    protected static class BulgarianAdjective extends Adjective {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        // The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<BulgarianModifierForm, String> values = new EnumMap<BulgarianModifierForm, String>(BulgarianModifierForm.class);

        BulgarianAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        public Map<? extends AdjectiveForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(AdjectiveForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(AdjectiveForm form, String value) {
            assert form instanceof BulgarianModifierForm : "The Bulgarian do not like their language sullied with foreign words";
            values.put((BulgarianModifierForm) form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, REQD_MODIFIER_FORMS);
        }

        @Override
        protected String deriveDefaultString(AdjectiveForm form, String value, AdjectiveForm baseFormed) {
            // here's the magic to automagically append the correct ending
            if (form.getArticle() == LanguageArticle.DEFINITE && form.getArticle() != baseFormed.getArticle()) {
                // Here's where we get the "real" string.
                if (value == null || value.length() == 0) {
                    return value;  // TODO: Log this?
                }
                // If it ends with an й, then you have to strip it off
                if (form.getNumber() == LanguageNumber.SINGULAR) {
                    switch (form.getGender()) {
                        case FEMININE:
                            return value + "\u0442\u0430";  // -ТА always
                        case NEUTER:
                            return value + "\u0442\u043e";  // -то always
                        case MASCULINE:  // fall through
                        case ANIMATE_MASCULINE: // fall through
                    }
                } else {
                    return value + "\u0442\u0435";  // always ends with -TE except if it ends with A
                }
            } else if (form == BulgarianModifierForm.SINGULAR_MASCULINE_OBJ_DEF) {
                assert baseFormed == BulgarianModifierForm.SINGULAR_MASCULINE_DEF : "Defaulting from wrong form";
                if (value.endsWith("\u0442")) {
                    // Needs to end with "т"
                    return value.substring(0, value.length() - 1); // Strip off the last T;
                } else {
                    int space = value.indexOf(' ');
                    if (space > 0 && value.charAt(space - 1) == '\u0442') {
                        // Rely on AutoDerivingDeclensionTest to do the right thing for adverbs
                        StringBuilder sb = new StringBuilder(value.length() - 1);  // Strip off the T that's at the end of the first word
                        sb.append(value.substring(0, space - 1));
                        sb.append(value.substring(space));
                        return sb.toString();
                    }
                }
            }
            return value;
        }
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new BulgarianAdjective(this, name, position);
    }

    private static final EnumSet<BulgarianModifierForm> REQD_MODIFIER_FORMS = EnumSet.of(BulgarianModifierForm.SINGULAR_MASCULINE, BulgarianModifierForm.SINGULAR_MASCULINE_DEF,
            BulgarianModifierForm.SINGULAR_FEMININE, BulgarianModifierForm.SINGULAR_NEUTER);
    private static final List<BulgarianModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(BulgarianModifierForm.class));
    private static final List<BulgarianNounForm> ALL_NOUN_FORMS = ImmutableList.copyOf(EnumSet.allOf(BulgarianNounForm.class));
    private static final List<BulgarianNounForm> NORMAL_NOUN_FORMS = ImmutableList.copyOf(EnumSet.of(BulgarianNounForm.SINGULAR, BulgarianNounForm.PLURAL));
    private static final EnumSet<LanguageGender> GENDER_TYPES = EnumSet.of(LanguageGender.FEMININE, LanguageGender.MASCULINE, LanguageGender.NEUTER);
    private static final EnumSet<LanguageCase> ALLOWED_CASE_FORMS = EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.OBJECTIVE);

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return NORMAL_NOUN_FORMS;
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
                              LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new BulgarianNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return GENDER_TYPES;
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return ALL_NOUN_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return NORMAL_NOUN_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return NORMAL_NOUN_FORMS;
    }

    @Override
    public boolean hasArticleInNounForm() {
        return true;
    }

    private static EnumSet<LanguageArticle> ARTICLE_TYPES = EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE);

    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return ARTICLE_TYPES;
    }

    @Override
    public EnumSet<LanguageCase> getAllowedCases() {
        return ALLOWED_CASE_FORMS;
    }

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public boolean isArticleInNounFormAutoDerived() {
        return true;
    }


    // <My/> <Account case="o" article="the"/> must convert into
    // <My case="o" article="the"/> <Account/> for bulgarian
    @Override
    public boolean moveNounInflectionToFirstModifier() {
        return true;
    }
}
