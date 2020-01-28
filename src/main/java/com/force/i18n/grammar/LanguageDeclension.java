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
public abstract class LanguageDeclension {
	private final HumanLanguage language;

	public LanguageDeclension(HumanLanguage language) {
		this.language = language;
	}

    /**
     * @return the language of this declension
     */
    public final HumanLanguage getLanguage() {
    	return this.language;
    }

    /**
     * @return all the forms that are associated with nouns in a list
     */
    public abstract List< ? extends NounForm> getAllNounForms();

    /**
     * @return the set of noun forms for entities (which need all the information for adjective modification)
     * For entities, we need with-article forms, but validation method
     * will fix those values automatically. See Noun.fixValueWithArticle
     */
    public abstract Collection<? extends NounForm> getEntityForms();

    /**
     * @return the forms that can be associated with "fields", where full declension isn't needed
     */
    public abstract Collection<? extends NounForm> getFieldForms();

    /**
     * @return the forms that can be associated with adjectives and other modifiers
     */
    public abstract Collection<? extends NounForm> getOtherForms();


    /**
     * @return all of the various forms of adjectives available in the dictionary
     */
    public abstract List<? extends AdjectiveForm> getAdjectiveForms();

   /**a
    * @return all of the various forms of articles available in the dictionary
    */
    public List<? extends ArticleForm> getArticleForms() {
        throw new UnsupportedOperationException("You can only ask for article forms of a language with articles");
    }

    /**
     * @return Return the noun implementation for this language
     * @param name the key to the noun
     * @param pluralAlias the alias to use in the XML for the plural version of this noun for readability
     * @param type NounType (entity/field)
     * @param entityName grouping for which entity this noun is in for display
     * @param startsWith startsWith/endsWith soung
     * @param gender linguistic gender
     * @param access a string to evaluate if the noun isn't available to all users
     * @param isStandardField if it's a field (i.e don't need the whole set of declensions)
     * @param isCopied whether this is copied from another dictionary (i.e. inherited from English)
     */
    protected abstract Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied);

    /**
     * Accessor for creating custom nouns simply based on all of the forms.  Should be used by the "renaming provider"
     * @param name the key to the noun
     * @param type NounType (entity/field)
     * @param entityName grouping for which entity this noun is in for display
     * @param startsWith startsWith/endsWith soung
     * @param gender linguistic gender
     * @param forms the forms of the noun by noun form
     * @return Return the noun implementation for this language
     */
    public Noun createNoun(String name, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, Map<? extends NounForm,String> forms) {
        Noun noun = createNoun(name, null, type, entityName, startsWith, gender, null, true, false);
        if (forms != null) {
            for (Map.Entry<? extends NounForm, String> entry : forms.entrySet()) {
                noun.setString(intern(entry.getValue()), entry.getKey());
            }
        }
        return noun;
    }

    /**
     * Helper method for creating a noun from the database
     * @param name the name of the noun
     * @param type the noun type
     * @param entityName the name of the standard entity this noun is associated with, or null if it's custom or irrelevant
     * @param rs a cursor against the label_data or custom_entity_translation tables
     * @return Return the noun implementation for this language
     * @throws SQLException if there is a database error
     */
    public Noun createNoun(String name, NounType type, String entityName, ResultSet rs) throws SQLException {
        String starts = rs.getString("STARTS_WITH");
        String gen = rs.getString("GENDER");
        return createNoun(name, type, entityName, LanguageStartsWith.fromDbValue(starts), LanguageGender.fromDbValue(gen), Collections.<NounForm,String>emptyMap());
    }

    /**
     * @param name the key for the adjective
     * @param startsWith startsWith/endsWith sound
     * @param position whether the adjective is a pre or postposition
     * @return a language specific implementation for modifiers/adjectives in this language
     */
    protected abstract Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position);

    /**
     * @param name the key for the article
     * @param articleType the type of article
     * @return a language specific implementation for modifiers/adjectives in this language
     */
    protected Article createArticle(String name, LanguageArticle articleType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Convenient method to tell that the language has gender.<br>
     * In English, it is false. Subclass must override this method to return the correct value. Note that subclass may
     * also need to override <CODE> int[] getRequiredGenders</CODE>.
     *
     * @return true if the language has concept of gender
     */
    public abstract boolean hasGender();

    /**
     * convenient method to tell that the language has to know the noun, which starts with vowel (includes soft-sounds
     * like Hour). <br>
     * In English, it is true. Subclass must override this method to return the correct value. Note that subclass may
     * also need to override <CODE> Element createStartsWithElement(String, Noun, boolean)</CODE> too.
     *
     * @return true if the language modifier(article/adjective) have different value if it starts with vowel.
     */
    public abstract boolean hasStartsWith();

    /**
     * convenient method to tell that the language has to know the noun, which ends with a vowel or a noun.  Used in particles
     *
     * @return true if the language modifier(article/adjective) have different value the previous phoneme ends with a vowel.
     */
    public boolean hasEndsWith() {
    	return false;
    }

    /**
     * @return true if adjectives are dependent on whether the noun starts with a vowel. If false, custom objects do not
     *         need to collect the startsWith information on their names, since we only use their names with simple
     *         adjectives and we do not compose sentences with them.
     */
    public boolean hasStartsWithInAdjective() {
        return hasStartsWith(); // Default to hasStartsWith
        // NOTE: Before 164, English had this set to false.  This was incorrect.
        // There are at least *four* labels in english that are of the form <Entity entity="0" article="a"/> which require
        // the specification of "starts with vowel" for english objects.
    }

    /**
     * Certain languages have strict rules for determining the starts with of an adjective, such as greek.
     * If this is enabled, the display for "hasStartsWith" in the UI will be suppressed.
     * @return whether startswith is autoderivable from a given form.
     */
    public boolean hasAutoDerivedStartsWith() {
        return false;
    }

    /**
     * convenient method to tell that the language has plural form In English, it is true. Subclass must override this
     * method to return the correct value.
     *
     * @return true if the language has concept of number (singular/plural)
     */
    public boolean hasPlural() {
        return true;  // Default to true because most languages have it
    }

    /**
     * @return the set of supported LanguageNumbers for this declension.
     */
    public Set<LanguageNumber> getAllowedNumbers() {
    	return hasPlural() ? LanguageNumber.PLURAL_SET : LanguageNumber.SINGULAR_SET;
    }


    /**
     * convenient method to tell that the language has possessive form.
     * In English, it is false. Subclass must override this method to return
     * the correct value.
     * @return true if the language has concept of possessive (first/second/third person possessive)
     */
    public boolean hasPossessive() {
        return false;  // Default to false because few languages have it
    }


    /**
     * @return <tt>true</tt> if there are different adjective forms based on the possessive
     */
    public boolean hasPossessiveInAdjective() {
        return false;
    }

    /**
     * @return <tt>true</tt> if the language has a different form based on article
     * NOTE: If you override this, you pretty much have to extend from ArticledDeclension.
     */
    public boolean hasArticle() {
        return false;
    }

    /**
     * @return <tt>true</tt> if there are different noun forms based on the article (i.e. the article isn't a modifier, but is an irregular suffix)
     */
    public boolean hasArticleInNounForm() {
        return false;
    }

    /**
     * @return whether the article form for a noun is autoderived
     */
    public boolean isArticleInNounFormAutoDerived() {
        return false;
    }


    /**
     * @return <tt>true</tt> if the language has different form based on cases
     */
    public boolean hasRequiredCases() {
        return getRequiredCases().size() > 1;
    }


    /**
     * @return <tt>true</tt> if the language has different form based on cases
     */
    public boolean hasAllowedCases() {
        return getAllowedCases().size() > 1;
    }

    /**
     * @return <tt>true</tt> if the language has a distinction between capital and lowercase letters.
     * If false, most of the lowercasing is ignored globally.
     */
    public boolean hasCapitalization() {
        return true;
    }

    /**
     * @return set of NEUTER, FEMININE or MASCULINE, null if nothing apply
     */
    public EnumSet<LanguageGender> getRequiredGenders() {
        return null;  // Default
    }

    /**
     * @return set of NOMINATIVE, ACCUSATIVE, GENITIVE or DATIVE
     */
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE);  // Nominative is usually required
    }

    /**
     * @return the set of allowed cases for a noun/adjective; which differs from the required cases
     * in that the "allowed" cases may include auto-derived forms.
     */
    public EnumSet<LanguageCase> getAllowedCases() {
        return getRequiredCases();
    }

    /**
     * @return set of which "starts with" values are "required"
     */
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT);  // Only generally care about consonant.
    }

    /**
     * @return array of values of POSSESSIVE_NONE, POSSESSIVE_FIRST, POSSESSIVE_SECOND or POSSESSIVE_THIRD
     */
    public EnumSet<LanguagePossessive> getRequiredPossessive() {
        return EnumSet.of(LanguagePossessive.NONE);  // Doesn't matter much.
    }

    /**
     * @return default gender value for this language
     */
    public LanguageGender getDefaultGender() {
        return LanguageGender.NEUTER;
    }

    /**
     * @return default value for this language
     */
    public LanguageStartsWith getDefaultStartsWith() {
        return LanguageStartsWith.CONSONANT;
    }

    /**
     * @return the default position of adjectives that needs to be overridden
     * in the language files
     */
    public LanguagePosition getDefaultAdjectivePosition() {
        return LanguagePosition.PRE;
    }

    /**
     * @return default value for this language
     */
    public LanguageCase getDefaultCase() {
        return LanguageCase.NOMINATIVE;
    }

    /**
     * @return default value for this language
     */
    public LanguageArticle getDefaultArticle() {
        return LanguageArticle.ZERO;
    }

    /**
     * @return default value for this language
     */
    public LanguagePossessive getDefaultPossessive() {
        return LanguagePossessive.NONE;
    }

    /**
     * @return whether or not this language has an inflected declension (i.e. is not a simple declension)
     * Generally isolating languages are non-inflected
     */
    public boolean isInflected() {
        return true;
    }

    /**
     * Label file processing should change so that any modifier on a noun should move
     * from the noun to the first modifier, if one exists.  For bulgarian.
     * @return whether this language needs special processing of modifiers in noun phrases
     */
    public boolean moveNounInflectionToFirstModifier() {
        return false;
    }

    /**
     * This method is used to determine whether to display a "warning" in rename tabs about there being gramatical issues
     * around renaming gender
     * @return whether verbs are inflected differently based on the gender of the subject
     */
    public boolean hasSubjectGenderInVerbConjugation() {
        return false;
    }

    /**
     * Determine whether the noun can have a "classifier" associated with it when used for counting numbers of things.
     * You can use a special tag called &lt;Counter/&gt; or &lt;Classifier/&gt; that will be associated with the noun.
     * This allows the customer to rename the noun *and* change the classifier when doing counting so you it will appear
     * correct, especially when the counter word needs to match the type of noun.
     *
     * It's better to implement the WithClassifiers interface than this directly.
     * @return whether or not this language uses classifiers.
     */
    public boolean hasClassifiers() {
        return false;
    }

    /**
     * @return Return the appropriate noun form for this language based on the form parameters provided
     * @param number the linguistic number
     * @param _case the linguistic case
     * @param possessive the possessive type
     * @param article the associated article 
     */
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article) {
        for (NounForm nf : getAllNounForms()) {
            if (nf.getNumber() == number && nf.getCase() == _case && nf.getArticle() == article && nf.getPossessive() == possessive) {
                return nf;
            }
        }
        return null;  // Invalid form
    }

    /**
     * @return The set of articles allowed in the language.  By default is empty
     */
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return Collections.<LanguageArticle>emptySet();
    }

    /**
     * For languages where the noun has a different form based on the article (Nordic), should the
     * article="the" on the noun be inferred from the existence of the &lt;The&gt; particle?
     *
     * NOTE: THIS ONLY WORKS FOR THE DEFINITE ARTICLE
     * @return whether article="the" should be inferred from a &lt;The&gt; particle
     */
    public boolean shouldInferNounDefArticleFromParticle() {
        return false;
    }


    /**
     * @return Return the appropriate noun form for this language based on the form parameters provided.
     * Simple languages should reimplement this to provide a quicker and more direct response
     * @param number the linguistic number
     * @param _case the linguistic case
     * @param possessive the possessive type
     * @param article the associated article 
     */
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
    public final NounForm getNounForm(LanguageNumber number, LanguageCase caseType) {
        return getApproximateNounForm(number, caseType, LanguagePossessive.NONE, LanguageArticle.ZERO);
    }

    // Convenience method as well
    public final NounForm getNounForm(LanguageNumber number, LanguageArticle articleType) {
        return getApproximateNounForm(number, getDefaultCase(), LanguagePossessive.NONE, articleType);
    }

    // Convenience method for retrieving an equivalent nounForm from this declension
    public NounForm getNounForm(NounForm nf) {
        return getExactNounForm(nf.getNumber(), nf.getCase(), nf.getPossessive(), nf.getArticle());
    }

    /**
     * @return the maximum distance to look for modifiers associated with the noun
     * For languages without spaces between works, this should be 0
     */
    public int getMaxDistanceForModifiers() {
    	return isInflected() ? 0 : 5;
    }

    // Convenience method for retrieving an equivalent AdjectiveForm from this declension
    public AdjectiveForm getAdjectiveForm(AdjectiveForm af) {
        return getAdjectiveForm(af.getStartsWith(), af.getGender(), af.getNumber(), af.getCase(), af.getArticle(), af.getPossessive());
    }

    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        for (AdjectiveForm af : getAdjectiveForms()) {
            if (af.getNumber() == number && af.getCase() == _case && af.getArticle() == article && af.getGender() == gender && af.getStartsWith() == startsWith && af.getPossessive() == possessive) {
                return af;
            }
        }
        return null;  // Invalid form
    }

    /**
     * @param startsWith the startsWith/endsWith of the adjective
     * @param gender the linguistic gender of the adjective
     * @param number the linguistic number
     * @param _case the linguistic case
     * @param possessive the possessive type
     * @param article the associated article 
     * @return Return the appropriate noun form for this language based on the form parameters provided.
     * Simple languages should reimplement this to provide a quicker and more direct response
     */
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
    public ArticleForm getArticleForm(ArticleForm af) {
        return getArticleForm(af.getStartsWith(), af.getGender(), af.getNumber(), af.getCase());
    }

    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case) {
        for (ArticleForm af : getArticleForms()) {
            if ((!hasPlural() || af.getNumber() == number) && af.getCase() == _case && (!hasGender() || af.getGender() == gender)
                    && (!hasStartsWith() || af.getStartsWith() == startsWith)) {
                return af;
            }
        }
        return null;  // Invalid form
    }

    /**
     * @return true if compound nouns should, generally, be lowercased in compound nouns.
     * This is used to determine defaults on the renaming screens.
     */
    public boolean shouldLowercaseEntityInCompoundNouns() {
        return false;
    }

    /**
     * @param s the string to lowercase
     * @param form the form of the noun to lowercase
     * @return the lowercase form of a noun that should be used if a non-capitalized version
     * of the noun was asked for in a Label Ref.  This is used in german to deal with capitalization
     * of "Ein Account" vs "ein Account", where the Account should generally always be capitalized.
     */
    public String formLowercaseNounForm(String s, NounForm form) {
        return hasCapitalization() ? getLanguage().toFoldedCase(s) : s;
    }

    @Override public String toString() { return getClass().getSimpleName(); }


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
        /**
		 *
		 */
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
        /**
		 *
		 */
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
        /**
		 *
		 */
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
        /**
         *
         */
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
                logger.info("###\tError: The noun " + name + " has no singular form");
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
				a.append("var sw=nextTerm.s;");  // The next term is the relevant one for starts with
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
    	    a.append(instance + ".getDefaultCounterWord = function() { return '").append(((WithClassifiers)this).getDefaultClassifier()).append("';};");
    	}
    }

    /**
     * Implement this interface on a declension that has classifier words.
     * @see <a href="https://en.wikipedia.org/wiki/Classifier_(linguistics)">Wikpedia: Classifier</a>
     * @see <a href="https://en.wikipedia.org/wiki/Korean_count_word">Wikpedia: Korean Counter Word</a>
     * @see <a href="https://en.wikipedia.org/wiki/Japanese_counter_word">Wikpedia: Japanese Counter Word</a>
     *
     * @author stamm
     * @since 0.6.0
     */
    public interface WithClassifiers {
        /**
         * Determine whether the noun can have a "classifier" associated with it when used for counting numbers of things.
         * You can use a special tag called &lt;Counter/&gt; or &lt;Classifier/&gt; that will be associated with the noun.
         * This allows the customer to rename the noun *and* change the classifier when doing counting so you it will appear
         * correct, especially when the counter word needs to match the type of noun.
         * @return whether or not this language uses classifiers.
         */
        default boolean hasClassifiers() {
            return true;
        }

        /**
         * @return the default Counter Word for this language.
         * The typical defaults for this are words like Chinese (个/個), Japanese (つ) and in Korean (개).
         */
        String getDefaultClassifier();
    }

}
