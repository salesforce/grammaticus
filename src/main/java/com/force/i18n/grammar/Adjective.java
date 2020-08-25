/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
/**
 * An adjective or other noun modifier as stored in a LanguageDictionary.
 *
 * TODO: Adjectives should be able to declaratively take a "case" that overrides the default case in the parser
 * @author yoikawa,stamm
 */
public abstract class Adjective extends NounModifier {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Adjective.class.getName());
    private final LanguagePosition position;

    /**
     * @return all the forms of the adjective
     */
    @Override
    public abstract Map<? extends AdjectiveForm, String> getAllValues();

    @Deprecated
    protected Adjective(LanguageDeclension declension, String name) {
        this(declension, name, declension.getDefaultAdjectivePosition());
    }

    protected Adjective(LanguageDeclension declension, String name, LanguagePosition position) {
        super(declension, name);
        this.position = position;
    }

    /**
     * @return the value of this adjective for the given form
     * @param form the get the string from
     */
    protected abstract String getString(AdjectiveForm form);

    @Override
    public final String getString(ModifierForm form) {
        return getString((AdjectiveForm)form);
    }

    @Override
    public String getDefaultValue() {
        return getString(getDeclension().getAdjectiveForm(getDeclension().getDefaultStartsWith(),
               getDeclension().getDefaultGender(), LanguageNumber.SINGULAR,
               getDeclension().getDefaultCase(), getDeclension().getDefaultArticle(), getDeclension().getDefaultPossessive()));
    }

    /**
     * @return the position of this noun modifier WRT the noun
     */
    @Override
    public LanguagePosition getPosition() {
        return this.position;
    }

    // Convenience methods
    public String getString(LanguageNumber number, LanguageGender gender, LanguageStartsWith startsWith) {
        return getString(getDeclension().getApproximateAdjectiveForm(startsWith, gender, number, getDeclension().getDefaultCase(), getDeclension().getDefaultArticle(), getDeclension().getDefaultPossessive()));
    }

    public String getString(LanguageNumber number, LanguageArticle article, LanguageGender gender, LanguageStartsWith startsWith) {
        return getString(getDeclension().getApproximateAdjectiveForm(startsWith, gender, number, getDeclension().getDefaultCase(), article, getDeclension().getDefaultPossessive()));
    }

    public String getString(LanguageNumber number, LanguageCase caseType, LanguageGender gender, LanguageStartsWith startsWith) {
        return getString(getDeclension().getApproximateAdjectiveForm(startsWith, gender, number, caseType, getDeclension().getDefaultArticle(), getDeclension().getDefaultPossessive()));
    }

    /**
     * Set the value of one of the forms of this adjective.
     * @param form the form to set
     * @param value the value to set
     */
    protected abstract void setString(AdjectiveForm form, String value);

    /**
     * @return the appropriate default string for the value, providing the ability for the subclass to munge with the original version
     * @param form the adjective form
     * @param value the base form that value comes from
     * @param baseForm the base from that will be assigned
     */
    protected String deriveDefaultString(AdjectiveForm form, String value, AdjectiveForm baseForm) {
        return value;
    }

    @Override
    public boolean validate(String name) {
        // Default validation to only requiring the most "generic" version of the adjective
        return defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, getDeclension().getDefaultGender(),
                LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
    }

    /**
     * A "simple" method for validating required terms and validating that they exist.
     * This generally depends on the declensions "return all forms"
     * TODO: This should compare the difference between specified forms and
     * choose the most similar (ie. the smallest Hamming distance)
     * @param name the name of the key for logging
     * @param requiredForms the set of required forms for adjectives in this language
     * @return <tt>true</tt> if the forms provided are valid.  Also fills in missing forms if necessary
     */
    public boolean defaultValidate(String name, Set<? extends AdjectiveForm> requiredForms) {
        for (AdjectiveForm form : getDeclension().getAdjectiveForms()) {
            if (getString(form) == null) {
                if (requiredForms.contains(form)) {
                    logger.fine("###\tError: The adjective " + name + " is missing required " + form + " form");
                    // TODO: uncomment the return false below once we actually handle validation
                    // Presently, the return value is simply ignored
                    // return false;
                }

                String s = null;
                AdjectiveForm baseForm = null;
                // Article form is first to drop
                if ((getDeclension().hasArticle() || getDeclension().hasArticleInNounForm()) && form.getArticle() != getDeclension().getDefaultArticle()) {
                     baseForm = getDeclension().getAdjectiveForm(form.getStartsWith(), form.getGender(), form.getNumber(), form.getCase(), getDeclension().getDefaultArticle(), form.getPossessive());
                     s = getString(baseForm);
                }

                // Next try starts with
                 if (s == null && getDeclension().hasStartsWith() && form.getStartsWith() != getDeclension().getDefaultStartsWith()) {
                     baseForm = getDeclension().getAdjectiveForm(getDeclension().getDefaultStartsWith(), form.getGender(), form.getNumber(), form.getCase(), form.getArticle(), form.getPossessive());
                     s = getString(baseForm);
                 }


                 // Now case (case drops before gender because that's how german wants it, i.e. that_acc should be placed into nominative)
                 // TODO: When german is fixed, this can go back so that gender is dropped before case.
                 if (s == null && getDeclension().hasAllowedCases() && form.getCase() != getDeclension().getDefaultCase()) {
                     baseForm = getDeclension().getAdjectiveForm(form.getStartsWith(), form.getGender(), form.getNumber(), getDeclension().getDefaultCase(), form.getArticle(), form.getPossessive());
                     s = getString(baseForm);
                 }


                 // Posssesive form needs to drop after cases due to the way Arabic works.  If this moves around, you'll have to revalidate AutoDerivedDeclensionTest
                 if (s == null && getDeclension().hasPossessiveInAdjective() && form.getPossessive() != getDeclension().getDefaultPossessive()) {
                     baseForm = getDeclension().getAdjectiveForm(form.getStartsWith(), form.getGender(), form.getNumber(), form.getCase(), form.getArticle(), getDeclension().getDefaultPossessive());
                     s = getString(baseForm);
                 }

                 // Now gender
                if (s == null && getDeclension().hasGender() && form.getGender() != getDeclension().getDefaultGender()) {
                    baseForm = getDeclension().getAdjectiveForm(form.getStartsWith(), getDeclension().getDefaultGender(), form.getNumber(), form.getCase(), form.getArticle(), form.getPossessive());
                    s = getString(baseForm);
                }

                // Now number; singular is the default for all languages
                if (s == null && getDeclension().hasPlural() && form.getNumber() != LanguageNumber.SINGULAR) {
                	LanguageNumber numberToTry = form.getNumber() == LanguageNumber.PLURAL ? LanguageNumber.SINGULAR :  LanguageNumber.PLURAL;  // If we're plural try singular, otherwise, like for dual, try plural
                    baseForm = getDeclension().getAdjectiveForm(form.getStartsWith(), form.getGender(), numberToTry, getDeclension().getDefaultCase(), form.getArticle(), form.getPossessive());
                    s = getString(baseForm);
                }

                if (s == null) {
                    // There wasn't a specified value with just 1 difference (ie. 1 Hamming distance),
                    // so default to the absolute default value
                    s = getDefaultValue();
                    if (s == null) {
                        logger.fine("###\tError: The adjective " + name + " has no " + form + " form and no default could be found");
                        return false;
                    } else {
                        logger.fine("###\tERROR: The adjective " + name + " has no obvious default for " + form + "form");
                    }
                }

                setString(form, intern(deriveDefaultString(form, s, baseForm)));
            }
        }
        return true;
    }



    @Override protected TermType getTermType() { return TermType.Adjective; }

    @Override
    public String toString() {
        return "Adj-" + getDeclension().getLanguage().getLocale() + "-'" + getAllValues().get(getDeclension().getAdjectiveForms().iterator().next()) + "'";
    }
}
