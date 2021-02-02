/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.util.*;

import org.junit.Assert;

import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ModifierFormMap;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.NounFormMap;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

/**
 * Unit test for LanguageDeclension functionality (no need to parse)
 * @author stamm
 */
public class LanguageDeclensionImplsTest extends TestCase {
    public LanguageDeclensionImplsTest(String name) {
        super(name);
    }

    /**
     * Validate the invariant the form = declension.get*Form(form.param1, form.param2, etc)
     */
    public void testAllGrammaticalForms() {
        for (HumanLanguage lang : LanguageProviderFactory.get().getAll()) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(lang);

            for (AdjectiveForm form : declension.getAdjectiveForms()) {
                assertEquals("Couldn't get results for form " + form + " in " + lang, form, declension.getAdjectiveForm(form.getStartsWith(), form.getGender(), form.getNumber(), form.getCase(), form.getArticle(), form.getPossessive()));
            }
            if (declension.hasArticle()) {
                for (ArticleForm form : declension.getArticleForms()) {
                    assertEquals("Couldn't get results for form " + form + " in " + lang, form, declension.getArticleForm(form.getStartsWith(), form.getGender(), form.getNumber(), form.getCase()));
                }
            }
            for (NounForm form : declension.getAllNounForms()) {
                assertEquals("Couldn't get results for form " + form + " in " + lang, form, declension.getExactNounForm(form.getNumber(), form.getCase(), form.getPossessive(), form.getArticle()));
            }
        }
    }

    /**
     * Validate invariants for declensions (such as defaults must be contained in the set of possible values)
     */
    public void testDeclensionInvariants() {
        for (HumanLanguage lang : LanguageProviderFactory.get().getAll()) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(lang);

            if (declension.hasGender()) {
                assertTrue("Default gender isn't contained in the set of genders for " + lang, declension.getRequiredGenders().contains(declension.getDefaultGender()));
            } else {
                assertNull("Required Genders should be null for " + lang, declension.getRequiredGenders());
            }

            if (declension.hasStartsWith() || declension.hasEndsWith()) {
                assertTrue("Default starts with isn't contained in the set of startsWiths for " + lang, declension.getRequiredStartsWith().contains(declension.getDefaultStartsWith()));
                assertTrue("Required startsWith should be greater than 1 for " + lang, declension.getRequiredStartsWith().size() > 1);  // None
            } else {
                assertEquals("Required startsWith should be 1 for " + lang, 1, declension.getRequiredStartsWith().size());  // None
            }

            if (declension.getAllowedCases() != null) {
                assertTrue("Default case isn't contained in the allowed set of cases for " + lang, declension.getAllowedCases().contains(declension.getDefaultCase()));
                assertTrue("Required cases aren't in the set of allowed cases", declension.getAllowedCases().containsAll(declension.getRequiredCases()));
            	assertEquals("Declension shouldn't mark allowed cases for " + lang, declension.hasAllowedCases(), declension.getAllowedCases().size() > 1);
            } else {
            	assertFalse("Declension should have no cases for " + lang, declension.hasAllowedCases());
            }

            if (declension.hasPossessive() || declension.hasPossessiveInAdjective()) {
                assertTrue("Default possessive isn't contained in the set of possessives for " + lang, declension.getRequiredPossessive().contains(declension.getDefaultPossessive()));
                assertTrue("Required possessive should be greater than 1 for " + lang, declension.getRequiredPossessive().size() > 1);  // None
            } else {
                assertEquals("Required possessive should be 1 for " + lang, 1, declension.getRequiredPossessive().size());  // None
            }

            if (declension.hasArticle() || declension.hasArticleInNounForm()) {
                assertTrue("Default article isn't contained in the set of articles for " + lang, declension.getAllowedArticleTypes().contains(declension.getDefaultArticle()));
                assertTrue("hasArticle doesn't match number of article types for " + lang, declension.getAllowedArticleTypes().size() > 1);
            } else {
                assertEquals("Required articles should be empty for " + lang, 0, declension.getAllowedArticleTypes().size());            	
            }
            
            if (declension.hasStartsWith() && declension.hasEndsWith()) {
            	Assert.fail("You cannot have a language with both starts with and ends with phoneme modifier changes at this time.");
            }

        }
    }

    /**
     * Validate that if the declension says something matters for the form, that the form for the noun includes that.
     *
     * You can't do the same for modifiers, due to some language not having modifier forms (i.e. Turkish)
     * Also, this test might fail for certain languages due to certain modifiers applying to only certain forms
     */
    public void testNounFormsMatchDeclensionFlags() {
        final Set<String> LANGS_TO_IGNORE_CASE_TEST = ImmutableSet.of(LanguageConstants.BULGARIAN, LanguageConstants.MACEDONIAN,  // Bulgarian case is only in modifier
        		LanguageConstants.ARABIC
               ); // Case is autoderived in arabic


        for (HumanLanguage lang : LanguageProviderFactory.get().getAll()) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(lang);

            // Make sure if it says there's a plural, the standard noun form is plural
            if (declension.hasPlural()) {
                assertEquals("Declension says there are plurals, but no noun form for it: " + lang, LanguageNumber.PLURAL, declension.getNounForm(LanguageNumber.PLURAL, LanguageArticle.ZERO).getNumber());
            }

            // Make sure articles are there if there are articles
            if (declension.hasArticleInNounForm()) {
                if (declension.getAllowedArticleTypes().contains(LanguageArticle.DEFINITE)) {
                    assertEquals("Declension says there are articles, but no noun form for it: " + lang,
                            LanguageArticle.DEFINITE, declension.getNounForm(LanguageNumber.SINGULAR, LanguageArticle.DEFINITE).getArticle());
                } else {
                    assertEquals("Declension says there are articles, but no noun form for it: " + lang,
                            LanguageArticle.INDEFINITE, declension.getNounForm(LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE).getArticle());
                }
            }

            if (declension.hasPossessive()) {
                Iterator<LanguagePossessive> i = declension.getRequiredPossessive().iterator();
                i.next();  // Ignore the first one
                LanguagePossessive secondPoss = i.next();
                assertEquals("Declension says there are possessive, but no noun form for the second allowed possessive: " + lang, secondPoss,
                        declension.getApproximateNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, secondPoss, LanguageArticle.ZERO).getPossessive());
            }


            if (!LANGS_TO_IGNORE_CASE_TEST.contains(lang.getLocale().getLanguage())
                    && declension.getAllowedCases() != null && declension.getAllowedCases().size() > 1) {
                Iterator<LanguageCase> i = declension.getAllowedCases().iterator();
                i.next();  // Ignore the first one
                LanguageCase secondCase = i.next();
                assertEquals("Declension says there are cases, but no noun form for the second allowed case: " + lang, secondCase, declension.getNounForm(LanguageNumber.SINGULAR, secondCase).getCase());
            }
        }
    }


    /**
     * Test implemention of ModifierFormMap for the entire domain of Adjective and ArticleForms
     */
    public void testModifierFormMap() {
        for (HumanLanguage lang : LanguageProviderFactory.get().getAll()) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(lang);

            if (declension.hasPossessiveInAdjective() && declension.hasArticle()) {
                continue;  // Ignore ARABIC since it doesn't use modifier form map.
            }

            EnumMap<LanguageArticle,ModifierFormMap<AdjectiveForm>> forms = ModifierFormMap.getArticleSpecificMap(declension.getAdjectiveForms());

            for (AdjectiveForm form : declension.getAdjectiveForms()) {
                assertEquals("Couldn't get results for form " + form, form, forms.get(form.getArticle()).getForm(form.getStartsWith(), form.getGender(), form.getNumber(), form.getCase()));
            }

            if (declension.hasArticle()) {
                ModifierFormMap<ArticleForm> articleForms = new ModifierFormMap<ArticleForm>(declension.getArticleForms());

                for (ArticleForm form : declension.getArticleForms()) {
                    assertEquals("Couldn't get results for form " + form, form, articleForms.getForm(form.getStartsWith(), form.getGender(), form.getNumber(), form.getCase()));
                }

            }

        }
    }

    /**
     * Test implemention of NounFormMap for the entire domain of NounForms
     */
    public void testNounFormMap() {
        for (HumanLanguage lang : LanguageProviderFactory.get().getAll()) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(lang);

            if (declension.hasPossessive() && declension.hasArticleInNounForm()) {
                continue;  // Ignore ARABIC since it doesn't use nounforms
            }

            if (declension.hasArticleInNounForm()) {
                EnumMap<LanguageArticle,NounFormMap<NounForm>> forms = NounFormMap.getArticleSpecificMap(declension.getAllNounForms());


                for (NounForm form : declension.getAllNounForms()) {
                    assertEquals("Couldn't get results for form " + form, form, forms.get(form.getArticle()).getForm(form.getNumber(), form.getCase()));
                }
            } else if (declension.hasPossessive()){
                EnumMap<LanguagePossessive,NounFormMap<NounForm>> forms = NounFormMap.getPossessiveSpecificMap(declension.getAllNounForms());


                for (NounForm form : declension.getAllNounForms()) {
                    assertEquals("Couldn't get results for form " + form, form, forms.get(form.getPossessive()).getForm(form.getNumber(), form.getCase()));
                }
            } else {
                NounFormMap<NounForm> nounForms = new NounFormMap<NounForm>(declension.getAllNounForms());

                for (NounForm form : declension.getAllNounForms()) {
                    assertEquals("Couldn't get results for form " + form, form, nounForms.getForm(form.getNumber(), form.getCase()));
                }
            }
        }
    }

}
