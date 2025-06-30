/*
 * Copyright (c) 2025, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.force.i18n.grammar;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.force.i18n.*;
import com.force.i18n.grammar.Noun.NounType;

/**
 * Represents the Declensions (or Noun Forms and their uses) associated with
 * a language.  This also contains information about the specifics of
 * noun usage in a language, such as whether "starts with" is imports.
 *
 * Each language (in general) will get its own declension.These classes should be treated like enums.
 *
 * @see com.force.i18n.grammar.impl.LanguageDeclensionFactory
 * @author stamm
 */
public interface LanguageDeclension {
    /**
     * Returns the language of this declension.
     * <p>
     * Note that this method may return a different value from
     * {@link com.force.i18n.grammar.LanguageDictionary#getLanguage()} because the most of case, the
     * {@code LanguageDeclension} is constructed by language part of the given {@code Locale} and shared across
     * countries and variants. See also {@link com.force.i18n.grammar.impl.LanguageDeclensionFactory}.
     *
     * @return the language of this declension
     */
    HumanLanguage getLanguage();

    /**
     * @return all the forms that are associated with nouns in a list
     */
    List< ? extends NounForm> getAllNounForms();

    /**
     * @return the set of noun forms for entities (which need all the information for adjective modification)
     * For entities, we need with-article forms, but validation method
     * will fix those values automatically. See Noun.fixValueWithArticle
     */
    Collection<? extends NounForm> getEntityForms();

    /**
     * @return the forms that can be associated with "fields", where full declension isn't needed
     */
    Collection<? extends NounForm> getFieldForms();

    /**
     * @return the forms that can be associated with adjectives and other modifiers
     */
    Collection<? extends NounForm> getOtherForms();

        /**
     * @return all of the various forms of adjectives available in the dictionary
     */
    List<? extends AdjectiveForm> getAdjectiveForms();

   /**a
    * @return all of the various forms of articles available in the dictionary
    */
    List<? extends ArticleForm> getArticleForms();

    /**
     * @return Return the noun implementation for this language
     * @param name the key to the noun
     * @param pluralAlias the alias to use in the XML for the plural version of this noun for readability
     * @param type NounType (entity/field)
     * @param entityName grouping for which entity this noun is in for display
     * @param startsWith startsWith/endsWith sound
     * @param gender linguistic gender
     * @param access a string to evaluate if the noun isn't available to all users
     * @param isStandardField if it's a field (i.e don't need the whole set of declensions)
     * @param isCopied whether this is copied from another dictionary (i.e. inherited from English)
     */
    Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied);

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
    Noun createNoun(String name, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, Map<? extends NounForm,String> forms);

    /**
     * Helper method for creating a noun from the database
     * @param name the name of the noun
     * @param type the noun type
     * @param entityName the name of the standard entity this noun is associated with, or null if it's custom or irrelevant
     * @param rs a cursor against the label_data or custom_entity_translation tables
     * @return Return the noun implementation for this language
     * @throws SQLException if there is a database error
     */
    Noun createNoun(String name, NounType type, String entityName, ResultSet rs) throws SQLException;

    /**
     * @param name the key for the adjective
     * @param startsWith startsWith/endsWith sound
     * @param position whether the adjective is a pre or postposition
     * @return a language specific implementation for modifiers/adjectives in this language
     */
    Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position);

    /**
     * @param name the key for the article
     * @param articleType the type of article
     * @return a language specific implementation for modifiers/adjectives in this language
     */
    Article createArticle(String name, LanguageArticle articleType);

    /**
     * Convenient method to tell that the language has gender.<br>
     * In English, it is false. Subclass must override this method to return the correct value. Note that subclass may
     * also need to override <CODE> int[] getRequiredGenders</CODE>.
     *
     * @return true if the language has concept of gender
     */
    boolean hasGender();

    /**
     * convenient method to tell that the language has to know the noun, which starts with vowel (includes soft-sounds
     * like Hour). <br>
     * In English, it is true. Subclass must override this method to return the correct value. Note that subclass may
     * also need to override <CODE> Element createStartsWithElement(String, Noun, boolean)</CODE> too.
     *
     * @return true if the language modifier(article/adjective) have different value if it starts with vowel.
     */
    boolean hasStartsWith();

    /**
     * convenient method to tell that the language has to know the noun, which ends with a vowel or a noun.  Used in particles
     *
     * @return true if the language modifier(article/adjective) have different value the previous phoneme ends with a vowel.
     */
    boolean hasEndsWith();

    /**
     * @return true if adjectives are dependent on whether the noun starts with a vowel. If false, custom objects do not
     *         need to collect the startsWith information on their names, since we only use their names with simple
     *         adjectives and we do not compose sentences with them.
     */
    boolean hasStartsWithInAdjective();

    /**
     * Certain languages have strict rules for determining the starts with of an adjective, such as greek.
     * If this is enabled, the display for "hasStartsWith" in the UI will be suppressed.
     * @return whether startswith is autoderivable from a given form.
     */
    boolean hasAutoDerivedStartsWith();

    /**
     * convenient method to tell that the language has plural form In English, it is true. Subclass must override this
     * method to return the correct value.
     *
     * @return true if the language has concept of number (singular/plural)
     */
    boolean hasPlural();

    /**
     * @return the set of supported LanguageNumbers for this declension.
     */
    Set<LanguageNumber> getAllowedNumbers();

    /**
     * convenient method to tell that the language has possessive form.
     * In English, it is false. Subclass must override this method to return
     * the correct value.
     * @return true if the language has concept of possessive (first/second/third person possessive)
     */
    boolean hasPossessive();

    /**
     * @return {@code true} if there are different adjective forms based on the possessive
     */
    boolean hasPossessiveInAdjective();

    /**
     * @return {@code true} if the language has a different form based on article
     * NOTE: If you override this, you pretty much have to extend from ArticledDeclension.
     */
    boolean hasArticle();

    /**
     * @return {@code true} if there are different noun forms based on the article (i.e. the article isn't a modifier, but is an irregular suffix)
     */
    boolean hasArticleInNounForm();

    /**
     * @return whether the article form for a noun is autoderived
     */
    boolean isArticleInNounFormAutoDerived();


    /**
     * @return {@code true} if the language has different form based on cases
     */
    boolean hasRequiredCases();

    /**
     * @return {@code true} if the language has different form based on cases
     */
    boolean hasAllowedCases();

    /**
     * @return {@code true} if the language has a distinction between capital and lowercase letters.
     * If false, most of the lowercasing is ignored globally.
     */
    boolean hasCapitalization();

    /**
     * @return set of NEUTER, FEMININE or MASCULINE, null if nothing apply
     */
    EnumSet<LanguageGender> getRequiredGenders();

    /**
     * @return set of NOMINATIVE, ACCUSATIVE, GENITIVE or DATIVE
     */
    EnumSet<LanguageCase> getRequiredCases();
    /**
     * @return the set of allowed cases for a noun/adjective; which differs from the required cases
     * in that the "allowed" cases may include auto-derived forms.
     */
    EnumSet<LanguageCase> getAllowedCases();

    /**
     * @return set of which "starts with" values are "required"
     */
    EnumSet<LanguageStartsWith> getRequiredStartsWith();

    /**
     * @return array of values of POSSESSIVE_NONE, POSSESSIVE_FIRST, POSSESSIVE_SECOND or POSSESSIVE_THIRD
     */
    EnumSet<LanguagePossessive> getRequiredPossessive();

    /**
     * @return default gender value for this language
     */
    LanguageGender getDefaultGender();

    /**
     * @return default value for this language
     */
    LanguageStartsWith getDefaultStartsWith();

    /**
     * @return the default position of adjectives that needs to be overridden
     * in the language files
     */
    LanguagePosition getDefaultAdjectivePosition();

    /**
     * @return default value for this language
     */
    LanguageCase getDefaultCase();

    /**
     * @return default value for this language
     */
    LanguageArticle getDefaultArticle();

    /**
     * @return default value for this language
     */
    LanguagePossessive getDefaultPossessive();

    /**
     * @return whether or not this language has an inflected declension (i.e. is not a simple declension)
     * Generally isolating languages are non-inflected
     */
    boolean isInflected();

    /**
     * Label file processing should change so that any modifier on a noun should move
     * from the noun to the first modifier, if one exists.  For bulgarian.
     * @return whether this language needs special processing of modifiers in noun phrases
     */
    boolean moveNounInflectionToFirstModifier();

    /**
     * This method is used to determine whether to display a "warning" in rename tabs about there being gramatical issues
     * around renaming gender
     * @return whether verbs are inflected differently based on the gender of the subject
     */
    boolean hasSubjectGenderInVerbConjugation();

    /**
     * Determine whether the noun can have a "classifier" associated with it when used for counting numbers of things.
     * You can use a special tag called &lt;Counter/&gt; or &lt;Classifier/&gt; that will be associated with the noun.
     * This allows the customer to rename the noun *and* change the classifier when doing counting so you it will appear
     * correct, especially when the counter word needs to match the type of noun.
     *
     * It's better to implement the WithClassifiers interface than this directly.
     * @return whether or not this language uses classifiers.
     */
    boolean hasClassifiers();

    /**
     * @return Return the appropriate noun form for this language based on the form parameters provided
     * @param number the linguistic number
     * @param _case the linguistic case
     * @param possessive the possessive type
     * @param article the associated article
     */
    NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article);

    /**
     * @return The set of articles allowed in the language.  By default is empty
     */
    Set<LanguageArticle> getAllowedArticleTypes();

    /**
     * For languages where the noun has a different form based on the article (Nordic), should the
     * article="the" on the noun be inferred from the existence of the &lt;The&gt; particle?
     *
     * NOTE: THIS ONLY WORKS FOR THE DEFINITE ARTICLE
     * @return whether article="the" should be inferred from a &lt;The&gt; particle
     */
    boolean shouldInferNounDefArticleFromParticle();

    /**
     * @return Return the appropriate noun form for this language based on the form parameters provided.
     * Simple languages should reimplement this to provide a quicker and more direct response
     * @param number the linguistic number
     * @param _case the linguistic case
     * @param possessive the possessive type
     * @param article the associated article
     */
    NounForm getApproximateNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive, LanguageArticle article);

    // convenience method
    default NounForm getNounForm(LanguageNumber number, LanguageCase caseType) {
        return getApproximateNounForm(number, caseType, LanguagePossessive.NONE, LanguageArticle.ZERO);
    }

    // Convenience method as well
    default NounForm getNounForm(LanguageNumber number, LanguageArticle articleType) {
        return getApproximateNounForm(number, getDefaultCase(), LanguagePossessive.NONE, articleType);
    }

    // Convenience method for retrieving an equivalent nounForm from this declension
    default NounForm getNounForm(NounForm nf) {
        return getExactNounForm(nf.getNumber(), nf.getCase(), nf.getPossessive(), nf.getArticle());
    }

    /**
     * @return the maximum distance to look for modifiers associated with the noun
     * For languages without spaces between works, this should be 0
     */
    int getMaxDistanceForModifiers();

    // Convenience method for retrieving an equivalent AdjectiveForm from this declension
    default AdjectiveForm getAdjectiveForm(AdjectiveForm af) {
        return getAdjectiveForm(af.getStartsWith(), af.getGender(), af.getNumber(), af.getCase(), af.getArticle(), af.getPossessive());
    }

    AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case, LanguageArticle article, LanguagePossessive possessive);

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
    AdjectiveForm getApproximateAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case, LanguageArticle article, LanguagePossessive possessive);

    ArticleForm getApproximateArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case);

    // Convenience method for retrieving an equivalent ArticleForm from this declension
    default ArticleForm getArticleForm(ArticleForm af) {
        return getArticleForm(af.getStartsWith(), af.getGender(), af.getNumber(), af.getCase());
    }

    ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case);

    /**
     * @return true if compound nouns should, generally, be lowercased in compound nouns.
     * This is used to determine defaults on the renaming screens.
     */
    boolean shouldLowercaseEntityInCompoundNouns();

    /**
     * @param s the string to lowercase
     * @param form the form of the noun to lowercase
     * @return the lowercase form of a noun that should be used if a non-capitalized version
     * of the noun was asked for in a Label Ref.  This is used in german to deal with capitalization
     * of "Ein Account" vs "ein Account", where the Account should generally always be capitalized.
     */
    String formLowercaseNounForm(String s, NounForm form);

    default LanguagePluralRules getPluralRules() {
    	return LanguageProviderFactory.get().getPluralRules(getLanguage());
    }

    /**
     * Allow the declensions to override the behavior of grammaticus.js
     * @param a the thing to append
     * @param instance the name of the instance variable that has the current engine (for language-specific overrides
     * @throws IOException if there's an issue with the appendable
     */
    void writeJsonOverrides(Appendable a, String instance) throws IOException;

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
