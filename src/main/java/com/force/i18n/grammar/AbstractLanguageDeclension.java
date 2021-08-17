/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.offline.PluralRulesJsImpl;

/**
 * Represents the Declensions (or Noun Forms and their uses) associated with
 * a language.  This also contains information about the specifics of
 * noun usage in a language, such as whether "starts with" is imports.
 *
 * Each language (in general) will get its own declension.These classes should be treated like enums.
 *
 * @author stamm
 */
public abstract class AbstractLanguageDeclension implements LanguageDeclension {
	private final HumanLanguage language;

	public AbstractLanguageDeclension(HumanLanguage language) {
		this.language = language;
	}

    @Override
    public final HumanLanguage getLanguage() {
    	return this.language;
    }

    @Override
    public abstract List< ? extends NounForm> getAllNounForms();

    @Override
    public abstract Collection<? extends NounForm> getEntityForms();

    @Override
    public abstract Collection<? extends NounForm> getFieldForms();

    @Override
    public abstract Collection<? extends NounForm> getOtherForms();

    @Override
    public abstract List<? extends AdjectiveForm> getAdjectiveForms();

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        throw new UnsupportedOperationException("You can only ask for article forms of a language with articles");
    }

    @Override
    public Noun createNoun(String name, NounType type, String entityName, LanguageStartsWith startsWith,
            LanguageGender gender, Map<? extends NounForm, String> forms) {
        Noun noun = createNoun(name, null, type, entityName, startsWith, gender, null, true, false);
        if (forms != null) {
            for (Map.Entry<? extends NounForm, String> entry : forms.entrySet()) {
                noun.setString(intern(entry.getValue()), entry.getKey());
            }
        }
        return noun;
    }

    @Override
    public Noun createNoun(String name, NounType type, String entityName, ResultSet rs) throws SQLException {
        String starts = rs.getString("STARTS_WITH");
        String gen = rs.getString("GENDER");
        return createNoun(name, type, entityName, LanguageStartsWith.fromDbValue(starts),
                LanguageGender.fromDbValue(gen), Collections.<NounForm, String> emptyMap());
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasEndsWith() {
    	return false;
    }

    @Override
    public boolean hasStartsWithInAdjective() {
        return hasStartsWith(); // Default to hasStartsWith
        // NOTE: Before 164, English had this set to false.  This was incorrect.
        // There are at least *four* labels in english that are of the form <Entity entity="0" article="a"/> which require
        // the specification of "starts with vowel" for english objects.
    }

    @Override
    public boolean hasAutoDerivedStartsWith() {
        return false;
    }

    @Override
    public boolean hasPlural() {
        return true;  // Default to true because most languages have it
    }

    @Override
    public Set<LanguageNumber> getAllowedNumbers() {
    	return hasPlural() ? LanguageNumber.PLURAL_SET : LanguageNumber.SINGULAR_SET;
    }


    @Override
    public boolean hasPossessive() {
        return false;  // Default to false because few languages have it
    }


    @Override
    public boolean hasPossessiveInAdjective() {
        return false;
    }

    @Override
    public boolean hasArticle() {
        return false;
    }

    @Override
    public boolean hasArticleInNounForm() {
        return false;
    }

    @Override
    public boolean isArticleInNounFormAutoDerived() {
        return false;
    }

    @Override
    public boolean hasRequiredCases() {
        return getRequiredCases().size() > 1;
    }

    @Override
    public boolean hasAllowedCases() {
        return getAllowedCases().size() > 1;
    }

    @Override
    public boolean hasCapitalization() {
        return true;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return null;  // Default
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE);  // Nominative is usually required
    }

    @Override
    public EnumSet<LanguageCase> getAllowedCases() {
        return getRequiredCases();
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT);  // Only generally care about consonant.
    }

    @Override
    public EnumSet<LanguagePossessive> getRequiredPossessive() {
        return EnumSet.of(LanguagePossessive.NONE);  // Doesn't matter much.
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.NEUTER;
    }

    @Override
    public LanguageStartsWith getDefaultStartsWith() {
        return LanguageStartsWith.CONSONANT;
    }

    @Override
    public LanguagePosition getDefaultAdjectivePosition() {
        return LanguagePosition.PRE;
    }

    @Override
    public LanguageCase getDefaultCase() {
        return LanguageCase.NOMINATIVE;
    }

    @Override
    public LanguageArticle getDefaultArticle() {
        return LanguageArticle.ZERO;
    }

    @Override
    public LanguagePossessive getDefaultPossessive() {
        return LanguagePossessive.NONE;
    }

    @Override
    public boolean isInflected() {
        return true;
    }

    @Override
    public boolean moveNounInflectionToFirstModifier() {
        return false;
    }

    @Override
    public boolean hasSubjectGenderInVerbConjugation() {
        return false;
    }

    @Override
    public boolean hasClassifiers() {
        return false;
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        for (NounForm nf : getAllNounForms()) {
            if (nf.getNumber() == number && nf.getCase() == _case && nf.getArticle() == article && nf.getPossessive() == possessive) {
                return nf;
            }
        }
        return null;  // Invalid form
    }

    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return Collections.<LanguageArticle>emptySet();
    }

    @Override
    public boolean shouldInferNounDefArticleFromParticle() {
        return false;
    }

    @Override
    public NounForm getApproximateNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        NounForm baseForm = getExactNounForm(number, _case, possessive, article);

        // RECURSION: Try to use the "Legacy" article support (i.e. there are articles, but no support for them.
        if (baseForm == null && !hasArticleInNounForm() && article != getDefaultArticle()) {
             baseForm = getApproximateNounForm(number, _case, possessive, getDefaultArticle());
             if (baseForm != null) {
                 // TODO: the declension having legacy articles should be explicit.  Using hasArticle()'s weird
                 return hasArticle() ? new LegacyArticledNounForm(baseForm, article) : baseForm;
             }
        }

        LanguagePossessive possesiveToTry = hasPossessive() ? possessive : getDefaultPossessive();
        LanguageNumber numberToTry = hasPlural() ? number : LanguageNumber.SINGULAR;
        LanguageArticle articleToTry = hasArticleInNounForm() ? article : getDefaultArticle();
        LanguageCase caseToTry = hasRequiredCases() ? _case : getDefaultCase();

        // Try to find a form based on if you don't have a value yet.
        if (baseForm == null && !hasPossessive() && possessive != getDefaultPossessive()) {
            baseForm = getExactNounForm(numberToTry, caseToTry, getDefaultPossessive(), articleToTry);
        }
        // Article form is next to drop in non-articled languages
        if (baseForm == null && !hasArticle() && article != getDefaultArticle()) {
            baseForm = getExactNounForm(numberToTry, caseToTry, possesiveToTry, articleToTry);
        }
        // Now case
        if (baseForm == null && !hasRequiredCases() && _case != getDefaultCase()) {
            baseForm = getExactNounForm(numberToTry, getDefaultCase(), possesiveToTry, articleToTry);
        }
        // OK, lets try to fall back based
        if (baseForm == null && hasPossessive() && possessive != getDefaultPossessive()) {
            baseForm = getExactNounForm(numberToTry, caseToTry, getDefaultPossessive(), articleToTry);
        }
        // Article form is first to drop
        if (baseForm == null && hasArticleInNounForm() && article != getDefaultArticle()) {
            baseForm = getExactNounForm(numberToTry, caseToTry, possesiveToTry, getDefaultArticle());
        }
        // Now case
        if (baseForm == null && hasRequiredCases() && _case != getDefaultCase()) {
            baseForm = getExactNounForm(numberToTry, getDefaultCase(), possesiveToTry, articleToTry);
        }
        if (baseForm == null && number != LanguageNumber.PLURAL) {
            baseForm = getExactNounForm(numberToTry, caseToTry, possesiveToTry, articleToTry);
        }
        // If you're looking for dual, default to plural if we don't have it.
        if (baseForm == null && number == LanguageNumber.DUAL) {
            baseForm = getExactNounForm(LanguageNumber.PLURAL, caseToTry, possesiveToTry, articleToTry);
        }

        // OK, you asked for something that wasn't supported.
        if (baseForm == null) {
            assert false : "Programmer error, you asked for an illegal noun form";
            baseForm = getAllNounForms().iterator().next();
        }

        return baseForm;
    }

    // convenience method
    @Override
    public final NounForm getNounForm(LanguageNumber number, LanguageCase caseType) {
        return getApproximateNounForm(number, caseType, LanguagePossessive.NONE, LanguageArticle.ZERO);
    }

    // Convenience method as well
    @Override
    public final NounForm getNounForm(LanguageNumber number, LanguageArticle articleType) {
        return getApproximateNounForm(number, getDefaultCase(), LanguagePossessive.NONE, articleType);
    }

    // Convenience method for retrieving an equivalent nounForm from this declension
    @Override
    public NounForm getNounForm(NounForm nf) {
        return getExactNounForm(nf.getNumber(), nf.getCase(), nf.getPossessive(), nf.getArticle());
    }

    @Override
    public int getMaxDistanceForModifiers() {
    	return isInflected() ? 0 : 5;
    }

    // Convenience method for retrieving an equivalent AdjectiveForm from this declension
    @Override
    public final AdjectiveForm getAdjectiveForm(AdjectiveForm af) {
        return getAdjectiveForm(af.getStartsWith(), af.getGender(), af.getNumber(), af.getCase(), af.getArticle(), af.getPossessive());
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        for (AdjectiveForm af : getAdjectiveForms()) {
            if (af.getNumber() == number && af.getCase() == _case && af.getArticle() == article && af.getGender() == gender && af.getStartsWith() == startsWith && af.getPossessive() == possessive) {
                return af;
            }
        }
        return null;  // Invalid form
    }

    @Override
    public AdjectiveForm getApproximateAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        AdjectiveForm baseForm = getAdjectiveForm(startsWith, gender, number, _case, article, possessive);
        if (baseForm != null)
         {
            return baseForm;  // Assume success
        }

        LanguageStartsWith startsWithToTry = hasStartsWith() ? startsWith : getDefaultStartsWith();
        LanguageGender genderToTry = hasGender() ? gender : getDefaultGender();
        LanguageNumber numberToTry = hasPlural() ? number : LanguageNumber.SINGULAR;
        LanguageArticle articleToTry = (hasArticle() || hasArticleInNounForm()) && getAllowedArticleTypes().contains(article) ? article : getDefaultArticle();
        LanguageCase caseToTry = hasRequiredCases() ? _case : getDefaultCase();
        LanguagePossessive possessiveToTry = hasPossessiveInAdjective() ? possessive : getDefaultPossessive();


        // First try to drop unsupported values.
        // Drop possessive first.
        if (!hasPossessiveInAdjective() && possessive != getDefaultPossessive()) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, numberToTry, caseToTry, articleToTry, getDefaultPossessive());
        }

        // Next drop article
        if (baseForm == null && !hasArticle() && article != getDefaultArticle()) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, numberToTry, caseToTry, getDefaultArticle(), possessiveToTry);
        }
        // Now starts with
        if (baseForm == null && !hasStartsWith() && startsWith != getDefaultStartsWith()) {
            baseForm = getAdjectiveForm(getDefaultStartsWith(), genderToTry, numberToTry, caseToTry, articleToTry, possessiveToTry);
        }
        // Next drop gender
        if (baseForm == null && !hasGender() && gender != getDefaultGender()) {
            baseForm = getAdjectiveForm(startsWithToTry, getDefaultGender(), numberToTry, caseToTry, articleToTry, possessiveToTry);
        }
        // Next drop case
        if (baseForm == null && !hasRequiredCases() && _case != getDefaultCase()) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, numberToTry, getDefaultCase(), articleToTry, possessiveToTry);
        }
        // Now, drop supported values
        // Possessive, article, starts with, gender, case, plural
        if (baseForm == null && hasPossessive() && possessive != getDefaultPossessive()) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, numberToTry, caseToTry, articleToTry, getDefaultPossessive());
        }

        // Now article
        if (baseForm == null && hasArticle() && article != getDefaultArticle()) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, numberToTry, caseToTry, getDefaultArticle(), possessiveToTry);
        }
        // Now starts with
        if (baseForm == null && hasStartsWith() && startsWith != getDefaultStartsWith()) {
            baseForm = getAdjectiveForm(getDefaultStartsWith(), genderToTry, numberToTry, caseToTry, articleToTry, possessiveToTry);
        }
        // Next drop gender
        if (baseForm == null && hasGender() && gender != getDefaultGender()) {
            baseForm = getAdjectiveForm(startsWithToTry, getDefaultGender(), numberToTry, caseToTry, articleToTry, possessiveToTry);
        }
        // Next drop case
        if (baseForm == null && hasRequiredCases() && _case != getDefaultCase()) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, numberToTry, getDefaultCase(), articleToTry, possessiveToTry);
        }
        // Next drop case
        if (baseForm == null && number != LanguageNumber.SINGULAR) {
            baseForm = getAdjectiveForm(startsWithToTry, genderToTry, LanguageNumber.SINGULAR, caseToTry, articleToTry, possessiveToTry);
        }
        // OK, you asked for something that wasn't supported.
        if (baseForm == null) {
            assert false : "Programmer error, you asked for an illegal adjective form";
            baseForm = getAdjectiveForms().iterator().next();
        }
        return baseForm;
    }

    @Override
    public ArticleForm getApproximateArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case) {
        // Just try to get the article without the starts with.
        ArticleForm baseForm = getArticleForm(startsWith, gender, number, _case);
        if (baseForm == null && startsWith != getDefaultStartsWith()) {
            baseForm = getArticleForm(getDefaultStartsWith(), gender, number, _case);
        }
        if (baseForm == null) {
            assert false : "Programmer error, you asked for an illegal adjective form: " + startsWith + ":" + gender + ":" + number + ":" + _case;
            baseForm = getArticleForms().iterator().next();
        }
        return baseForm;
    }

    // Convenience method for retrieving an equivalent ArticleForm from this declension
    @Override
    public final ArticleForm getArticleForm(ArticleForm af) {
        return getArticleForm(af.getStartsWith(), af.getGender(), af.getNumber(), af.getCase());
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case) {
        for (ArticleForm af : getArticleForms()) {
            if ((!hasPlural() || af.getNumber() == number) && af.getCase() == _case && (!hasGender() || af.getGender() == gender)
                    && (!hasStartsWith() || af.getStartsWith() == startsWith)) {
                return af;
            }
        }
        return null;  // Invalid form
    }

    @Override
    public boolean shouldLowercaseEntityInCompoundNouns() {
        return false;
    }

    @Override
    public String formLowercaseNounForm(String s, NounForm form) {
        return hasCapitalization() ? getLanguage().toFoldedCase(s) : s;
    }

    @Override public String toString() { return getClass().getSimpleName(); }


    @Override
    public final LanguagePluralRules getPluralRules() {
    	return LanguageProviderFactory.get().getPluralRules(getLanguage());
    }

    /**
     * SIMPLE FORMS:
     *
     * Often times, complex declension have simple forms of one thing or another.
     * This is the "simple" form types for which there is only one possible declension
     */
    public static enum SimpleModifierForm implements AdjectiveForm, ArticleForm {
        SINGULAR
        ;
        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageNumber getNumber() {return LanguageNumber.SINGULAR;}
        @Override public LanguageGender getGender() {return LanguageGender.NEUTER;}
        @Override public LanguageStartsWith getStartsWith() {return LanguageStartsWith.CONSONANT;}
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }

        @Override
        public String getKey() {
            return "0";
        }
    }

    /**
     * Represents a simple adjective with a single form.
     */
    public static class SimpleAdjective extends Adjective {
        private static final long serialVersionUID = 1L;
        private static final Logger logger = Logger.getLogger(SimpleAdjective.class.getName());
        private String value;

        public SimpleAdjective(LanguageDeclension declension, String name) {
            super(declension, name, declension.getDefaultAdjectivePosition());
        }

        @Override
        public Map< ? extends AdjectiveForm, String> getAllValues() {
            return Collections.singletonMap(SimpleModifierForm.SINGULAR, value);
        }

        @Override
        public String getString(AdjectiveForm form) {
            assert form instanceof SimpleModifierForm : "Why are you asking for some random adjective form.  Really?";
            return value;
        }

        @Override
        protected void setString(AdjectiveForm form, String value) {
            assert form instanceof SimpleModifierForm : "Why are you asking for some random adjective form.  Really?";
            this.value = intern(value);
        }

        @Override
        public boolean validate(String name) {
            if (this.value == null) {
                logger.info("###\tError: The adjective " + name + " has no value");
                return false;
            }
            return true;
        }
    }

    /**
     * Represents a simple adjective for use in languages that have "startsWith" for articles and modifiers.
     * This is used in hungarian and english.
     */
    public static class SimpleAdjectiveWithStartsWith extends SimpleAdjective {
        private static final long serialVersionUID = 1L;

        private final LanguageStartsWith startsWith;

        public SimpleAdjectiveWithStartsWith(LanguageDeclension declension, String name, LanguageStartsWith startsWith) {
            super(declension, name);
            this.startsWith = startsWith;
        }
        @Override
        public LanguageStartsWith getStartsWith() {
            return startsWith;
        }
    }

    /**
     * Simple noun form with singular and plurals
     * @author stamm
     */
    public static enum PluralNounForm implements NounForm {
        SINGULAR(LanguageNumber.SINGULAR),
        PLURAL(LanguageNumber.PLURAL),
        ;

        private final LanguageNumber number;
        private PluralNounForm(LanguageNumber number) {
            this.number = number;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override
        public String getKey() {
            return getNumber().getDbValue();
        }
    }

    /**
     * Represents a simple noun with a singular and plural form.
     * Pretty much the same as an EnglishNoun, without the legacy article bits
     * See IndonesianNounForm for more info
     */
    public static class SimplePluralNoun extends SimplePluralNounWithGender {
        private static final long serialVersionUID = 1L;
        private static final Logger logger = Logger.getLogger(SimplePluralNoun.class.getName());

        public SimplePluralNoun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected boolean validateGender(String name) {
            if (getGender() != LanguageGender.NEUTER) {
                logger.info(VALIDATION_WARNING_HEADER + name + " must be neuter");
            }
            return super.validateGender(name);  // Let it go
        }

        @Override
        public Noun clone() {
            SimplePluralNoun noun = (SimplePluralNoun) super.clone();
            return noun;
        }
    }

    /**
     * Represents a simple noun with a singular and plural form, and a gender.
     */
    public static class SimplePluralNounWithGender extends Noun {
        private static final long serialVersionUID = 1L;
        private static final Logger logger = Logger.getLogger(SimplePluralNounWithGender.class.getName());

        private String singular;
        private String plural;

        public SimplePluralNounWithGender(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public void makeSkinny() {
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            // TODO: All the values, or just the interesting ones?  The world may never know.
            return enumMapFilterNulls(PluralNounForm.SINGULAR, singular, PluralNounForm.PLURAL, plural);
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            return isPlural ?  (plural != null ? plural : singular): singular;
        }

        @Override
        public String getString(NounForm form) {
            assert form instanceof PluralNounForm : "Why are you asking for some random noun form.  Really?";
            return getDefaultString(form.getNumber() == LanguageNumber.PLURAL);
        }

        @Override
        public void setString(String value, NounForm form) {
            value = intern(value);
            if (form.getNumber().isPlural()) {
                this.plural = value;
                if (value != null && value.equals(this.singular))
                 {
                    this.singular = value; // Keep one reference for serialization
                }
            } else {
                this.singular = value;
                if (value != null && value.equals(this.plural))
                 {
                    this.plural = value; // Keep one reference for serialization
                }
            }
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            if (this.singular == null) {
                logger.info("###\tError: The noun " + name + " has no singular form for: "  + getDeclension().getLanguage());
                return false;
            }
            return true;
        }

        @Override
        public Noun clone() {
            SimplePluralNounWithGender noun = (SimplePluralNounWithGender) super.clone();
            return noun;
        }
    }

    /**
     * @return a new EnumMap with the given keys and values, only including an entry if the value and key are not null
     * This is used instead of ImmutableMap, because that doesn't allow null values
     * @param <K> the enum type
     * @param <V> the key type
     * @param k1 firstKey
     * @param v1 firstValue
     * @param k2 secondKey
     * @param v2 secondValue
     */
    protected static <K extends Enum<K>,V> EnumMap<K,V> enumMapFilterNulls(K k1, V v1, K k2, V v2) {
        return enumMapFilterNulls(k1, v1, k2, v2, null, null, null, null);
    }

    /**
     * @return a new EnumMap with the given keys and values, only including an entry if the value and key are not null
     * This is used instead of ImmutableMap, because that doesn't allow null values
     * @param <K> the enum type
     * @param <V> the key type
     * @param k1 firstKey
     * @param v1 firstValue
     * @param k2 secondKey
     * @param v2 secondValue
     * @param k3 thirdKey
     * @param v3 thirdValue
     */
    protected static <K extends Enum<K>,V> EnumMap<K,V> enumMapFilterNulls(K k1, V v1, K k2, V v2, K k3, V v3) {
        return enumMapFilterNulls(k1, v1, k2, v2, k3, v3, null, null);
    }

    /**
     * @return a new EnumMap with the given keys and values, only including an entry if the value and key are not null
     * This is used instead of ImmutableMap, because that doesn't allow null values
     * @param <K> the enum type
     * @param <V> the key type
     * @param k1 firstKey
     * @param v1 firstValue
     * @param k2 secondKey
     * @param v2 secondValue
     * @param k3 thirdKey
     * @param v3 thirdValue
     * @param k4 fourthKey
     * @param v4 fourthValue
     */
    protected static <K extends Enum<K>,V> EnumMap<K,V> enumMapFilterNulls(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        if (k1 == null) {
            throw new NullPointerException("You must pass in a valid enum for the first argument");
        }
        @SuppressWarnings("unchecked") // getClass returns <?> incorrectly
        EnumMap<K,V> result = new EnumMap<>((Class<K>)k1.getClass());
        if (v1 != null) {
            result.put(k1,v1);
        }
        if (k2 != null && v2 != null) {
            result.put(k2,v2);
        }
        if (k3 != null && v3 != null) {
            result.put(k3,v3);
        }
        if (k4 != null && v4 != null) {
            result.put(k4,v4);
        }
        return result;
    }

    /**
     * Allow the declensions to override the behavior of grammaticus.js
     * @param a the thing to append
     * @param instance the name of the instance variable that has the current engine (for language-specific overrides
     * @throws IOException if there's an issue with the appendable
     */
    @Override
    public void writeJsonOverrides(Appendable a, String instance) throws IOException {
        a.append(instance + ".locale='" + getLanguage().getHttpLanguageCode() + "';");
        String pluralRules = PluralRulesJsImpl.getSelectFunctionOverride(getLanguage().getLocale());
        if (pluralRules != null) {
            a.append(instance+".getPluralCategory = " + pluralRules+";");
        }
        if (hasGender() || hasStartsWith() || hasEndsWith()) {
            a.append(instance + ".getModifierForm = function(termType, termForm, nounForm, noun, nextTerm){");
            if (hasGender()) {
                a.append("var gen=noun.g;");
            }
            if (hasStartsWith() || hasEndsWith()) {
                a.append("var sw=nextTerm.s;"); // The next term is the relevant one for starts with
            }
            if (hasArticle()) {
                a.append(" if (termType=='d') {return ");
                getArticleForms().get(0).appendJsFormReplacement(a, "termForm", "gen", "sw");
                a.append(";}");
            }
            a.append(" return ");
            getAdjectiveForms().get(0).appendJsFormReplacement(a, "termForm", "gen", "sw");
            a.append(";};");
        }
        if (hasClassifiers()) {
            a.append(instance + ".getDefaultCounterWord = function() { return '")
                    .append(((WithClassifiers)this).getDefaultClassifier()).append("';};");
        }
    }
}
