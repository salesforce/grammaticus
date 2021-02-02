/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import static com.force.i18n.grammar.LanguageArticle.DEFINITE;
import static com.force.i18n.grammar.LanguageArticle.INDEFINITE;
import static com.force.i18n.grammar.LanguageCase.*;
import static com.force.i18n.grammar.LanguageGender.*;
import static com.force.i18n.grammar.LanguageNumber.PLURAL;
import static com.force.i18n.grammar.LanguageNumber.SINGULAR;
import static com.force.i18n.grammar.LanguagePossessive.FIRST;
import static com.force.i18n.grammar.LanguagePossessive.SECOND;
import static com.force.i18n.grammar.LanguageStartsWith.CONSONANT;
import static com.force.i18n.grammar.LanguageStartsWith.VOWEL;

import java.io.IOException;
import java.util.Locale;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;
import com.google.common.collect.ImmutableSet;

/**
 * @author stamm
 *
 */
public class LanguageDictionaryParserTest extends BaseGrammaticalLabelTest {
    public LanguageDictionaryParserTest(String name) {
        super(name);
    }


    private void assertNoun(Noun n, String expected, LanguageNumber number) {
        assertNoun(n, expected, number, LanguageArticle.ZERO);
    }

    private void assertNoun(Noun n, String expected, LanguageNumber number, LanguageArticle article) {
        assertNoun(n, expected, n.getDeclension().getNounForm(number, article));
    }

    private void assertNoun(Noun n, String expected, LanguageCase _case, LanguageNumber number) {
        assertNoun(n, expected, _case, number, LanguageArticle.ZERO);
    }

    private void assertNoun(Noun n, String expected, LanguageCase _case, LanguageNumber number, LanguagePossessive possessive) {
        assertNoun(n, expected, _case, number, LanguageArticle.ZERO, possessive);
    }

    private void assertNoun(Noun n, String expected, LanguageCase _case, LanguageNumber number, LanguageArticle article) {
        assertNoun(n, expected, _case, number, article, n.getDeclension().getDefaultPossessive());
    }


    private void assertNoun(Noun n, String expected, LanguageCase _case, LanguageNumber number, LanguageArticle article, LanguagePossessive possessive) {
        assertNoun(n, expected, n.getDeclension().getApproximateNounForm(number, _case, possessive, article));
    }

    private void assertNoun(Noun n, String expected, NounForm nid) {
        assertEquals("Noun form " + nid + " didn't match expected for " + n.getDeclension().getLanguage(), expected, n.getString(nid));
    }

    // Just load all supported languages - Note that this is to validate only each dictionary
    public void testAllDictionary() throws IllegalArgumentException, IOException {
        for (HumanLanguage language : LanguageProviderFactory.get().getAll()) {
            loadDictionary(language);
        }
    }

    // test root dictionary - English
    public void testRootDictionary() throws IllegalArgumentException, IOException {
        // Root (English) has plural and article. Forms with article are auto-generated.
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage(Locale.US));

        assertNotNull(l.getNoun("account", false));
        assertNotNull(l.getNoun("lead", false));
        assertNotNull(l.getNoun("opportunity", false));
        assertNotNull(l.getNoun("entity", false));

        // make sure key is all lowercase, and no plural form
        assertNull(l.getNoun("Account", false));
        assertNull(l.getNoun("Accounts", false));
        assertNull(l.getNoun("accounts", false));

        // "Account": starts with vowel
        Noun n = l.getNoun("account", false);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), VOWEL);
        assertNoun(n, "Account", SINGULAR);
        assertNoun(n, "An account", SINGULAR, INDEFINITE);
        assertNoun(n, "The account", SINGULAR, DEFINITE);
        assertNoun(n, "Accounts", PLURAL);
        assertNoun(n, "Accounts", PLURAL, INDEFINITE);
        assertNoun(n, "The accounts", PLURAL, DEFINITE);

        // "Lead": this is consonant
        n = l.getNoun("lead", false);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Lead", SINGULAR);
        assertNoun(n, "A lead", SINGULAR, INDEFINITE);
        assertNoun(n, "The lead", SINGULAR, DEFINITE);
        assertNoun(n, "Leads", PLURAL);
        assertNoun(n, "Leads", PLURAL, INDEFINITE);
        assertNoun(n, "The leads", PLURAL, DEFINITE);

        // "Account Name": field type compound nown. this has plural form, but no article form.
        n = l.getNoun("account_name", false);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), VOWEL);
        assertNoun(n, "Account Name", SINGULAR);
        assertNoun(n, "Account Name", SINGULAR, INDEFINITE);
        assertNoun(n, "Account Name", SINGULAR, DEFINITE);
        assertNoun(n, "Account Names", PLURAL);
        assertNoun(n, "Account Names", PLURAL, INDEFINITE);
        assertNoun(n, "Account Names", PLURAL, DEFINITE);

        // "Product Currency": single compound nown. this has no plural form nor article form.
        n = l.getNoun("product_currency", false);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Product Currency", SINGULAR);
        assertNoun(n, "Product Currency", SINGULAR, INDEFINITE);
        assertNoun(n, "Product Currency", SINGULAR, DEFINITE);
        assertNoun(n, "Product Currency", PLURAL);
        assertNoun(n, "Product Currency", PLURAL, INDEFINITE);
        assertNoun(n, "Product Currency", PLURAL, DEFINITE);

        // start checking adjectives
        assertNotNull(l.getAdjective("all"));
        assertNotNull(l.getAdjective("my"));
        assertNotNull(l.getAdjective("new"));
        assertNull(l.getAdjective("all_new"));
    }

    // Test French dictionary
    public void testSpanishDictionary() throws IllegalArgumentException, IOException {
        // French has all definition. Just make sure that loader constructs them all right.
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("es"));

        // "Account":
        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), FEMININE);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Cuenta", SINGULAR);
        assertNoun(n, "Una cuenta", SINGULAR, INDEFINITE);
        assertNoun(n, "La cuenta", SINGULAR, DEFINITE);
        assertNoun(n, "Cuentas", PLURAL);
        assertNoun(n, "Unas cuentas", PLURAL, INDEFINITE);
        assertNoun(n, "Las cuentas", PLURAL, DEFINITE);


        // "Contact":
        n = l.getNoun("contact", false);
        assertNotNull(n);
        assertEquals(n.getGender(), MASCULINE);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Contacto", SINGULAR);
        assertNoun(n, "Un contacto", SINGULAR, INDEFINITE);
        assertNoun(n, "El contacto", SINGULAR, DEFINITE);
        assertNoun(n, "Contactos", PLURAL);
        assertNoun(n, "Unos contactos", PLURAL, INDEFINITE);
        assertNoun(n, "Los contactos", PLURAL, DEFINITE);
    }

    // Test French dictionary
    public void testFrenchDictionary() throws IllegalArgumentException, IOException {
        // French has all definition. Just make sure that loader constructs them all right.
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("fr"));

        // "Account": French has all definitions
        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), MASCULINE);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Compte", SINGULAR);
        assertNoun(n, "un compte", SINGULAR, INDEFINITE);
        assertNoun(n, "le compte", SINGULAR, DEFINITE);
        assertNoun(n, "Comptes", PLURAL);
        assertNoun(n, "des comptes", PLURAL, INDEFINITE);
        assertNoun(n, "les comptes", PLURAL, DEFINITE);

        n = l.getNoun("lead", false);
        assertNotNull(n);
        assertEquals(n.getGender(), FEMININE);

        // French adjective has differnt forms by combination of gender, number and start with
        Adjective m = l.getAdjective("new");
        assertNotNull(l);
        assertEquals("Nouveau", m.getString(SINGULAR, MASCULINE, CONSONANT));
        assertEquals("Nouvel", m.getString(SINGULAR, MASCULINE, VOWEL));
        assertEquals("Nouvelle", m.getString(SINGULAR, FEMININE, CONSONANT));
        assertEquals("Nouveaux", m.getString(PLURAL, MASCULINE, CONSONANT));
        assertEquals("Nouveaux", m.getString(PLURAL, MASCULINE, VOWEL));
        assertEquals("Nouvelles", m.getString(PLURAL, FEMININE, CONSONANT));
    }

    // Test German dictionary
    public void testGermanDictionary() throws IllegalArgumentException, IOException {
        // German auto-generates articles. Check them all
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("de"));

        // Account: Masculine
        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), MASCULINE);
        assertEquals(n.getStartsWith(), CONSONANT);

        assertNoun(n, "Account", SINGULAR);
        assertNoun(n, "Ein Account", SINGULAR, INDEFINITE);
        assertNoun(n, "Der Account", SINGULAR, DEFINITE);
        assertNoun(n, "Accounts", PLURAL);
        assertNoun(n, "Accounts", PLURAL, INDEFINITE);
        assertNoun(n, "Die Accounts", PLURAL, DEFINITE);

        assertNoun(n, "Accounts", GENITIVE, SINGULAR);
        assertNoun(n, "Eines Accounts", GENITIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Des Accounts", GENITIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Accounts", GENITIVE, PLURAL);
        assertNoun(n, "Accounts", GENITIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Der Accounts", GENITIVE, PLURAL, DEFINITE);

        assertNoun(n, "Account", DATIVE, SINGULAR);
        assertNoun(n, "Einem Account", DATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Dem Account", DATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Accounts", DATIVE, PLURAL);
        assertNoun(n, "Accounts", DATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Den Accounts", DATIVE, PLURAL, DEFINITE);

        assertNoun(n, "Account", ACCUSATIVE, SINGULAR);
        assertNoun(n, "Einen Account", ACCUSATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Den Account", ACCUSATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Accounts", ACCUSATIVE, PLURAL);
        assertNoun(n, "Accounts", ACCUSATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Die Accounts", ACCUSATIVE, PLURAL, DEFINITE);

        // Document: Neuter
        n = l.getNoun("document", false);
        assertNotNull(n);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Dokument", SINGULAR);
        assertNoun(n, "Ein Dokument", SINGULAR, INDEFINITE);
        assertNoun(n, "Das Dokument", SINGULAR, DEFINITE);
        assertNoun(n, "Dokumente", PLURAL);
        assertNoun(n, "Dokumente", PLURAL, INDEFINITE);
        assertNoun(n, "Die Dokumente", PLURAL, DEFINITE);

        assertNoun(n, "Dokuments", GENITIVE, SINGULAR);
        assertNoun(n, "Eines Dokuments", GENITIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Des Dokuments", GENITIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Dokumente", GENITIVE, PLURAL);
        assertNoun(n, "Dokumente", GENITIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Der Dokumente", GENITIVE, PLURAL, DEFINITE);

        assertNoun(n, "Dokument", DATIVE, SINGULAR);
        assertNoun(n, "Einem Dokument", DATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Dem Dokument", DATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Dokumenten", DATIVE, PLURAL);
        assertNoun(n, "Dokumenten", DATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Den Dokumenten", DATIVE, PLURAL, DEFINITE);

        assertNoun(n, "Dokument", ACCUSATIVE, SINGULAR);
        assertNoun(n, "Ein Dokument", ACCUSATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Das Dokument", ACCUSATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Dokumente", ACCUSATIVE, PLURAL);
        assertNoun(n, "Dokumente", ACCUSATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Die Dokumente", ACCUSATIVE, PLURAL, DEFINITE);

        // Lead: Feminine
        n = l.getNoun("opportunity", false);
        assertNotNull(n);
        assertEquals(n.getGender(), FEMININE);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Opportunity", SINGULAR);
        assertNoun(n, "Eine Opportunity", SINGULAR, INDEFINITE);
        assertNoun(n, "Die Opportunity", SINGULAR, DEFINITE);
        assertNoun(n, "Opportunities", PLURAL);
        assertNoun(n, "Opportunities", PLURAL, INDEFINITE);
        assertNoun(n, "Die Opportunities", PLURAL, DEFINITE);

        assertNoun(n, "Opportunity", GENITIVE, SINGULAR);
        assertNoun(n, "Einer Opportunity", GENITIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Der Opportunity", GENITIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Opportunities", GENITIVE, PLURAL);
        assertNoun(n, "Opportunities", GENITIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Der Opportunities", GENITIVE, PLURAL, DEFINITE);

        assertNoun(n, "Opportunity", DATIVE, SINGULAR);
        assertNoun(n, "Einer Opportunity", DATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Der Opportunity", DATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Opportunities", DATIVE, PLURAL);
        assertNoun(n, "Opportunities", DATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Den Opportunities", DATIVE, PLURAL, DEFINITE);

        assertNoun(n, "Opportunity", ACCUSATIVE, SINGULAR);
        assertNoun(n, "Eine Opportunity", ACCUSATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "Die Opportunity", ACCUSATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "Opportunities", ACCUSATIVE, PLURAL);
        assertNoun(n, "Opportunities", ACCUSATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "Die Opportunities", ACCUSATIVE, PLURAL, DEFINITE);

        // German adjective has differnt forms by combination of gender and number.
        Adjective m = l.getAdjective("new");
        assertNotNull(m);
        assertEquals("Neuer", m.getString(SINGULAR, MASCULINE, CONSONANT));
        assertEquals("Neues", m.getString(SINGULAR, NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(SINGULAR, FEMININE, CONSONANT));
        assertEquals("Neue", m.getString(PLURAL, MASCULINE, CONSONANT));
        assertEquals("Neue", m.getString(PLURAL, NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(PLURAL, FEMININE, CONSONANT));

        // A: is same as none
        assertEquals("Neuer", m.getString(SINGULAR, INDEFINITE, MASCULINE, CONSONANT));
        assertEquals("Neues", m.getString(SINGULAR, INDEFINITE, NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(SINGULAR, INDEFINITE, FEMININE, CONSONANT));
        assertEquals("Neue", m.getString(PLURAL, INDEFINITE, MASCULINE, CONSONANT));
        assertEquals("Neue", m.getString(PLURAL, INDEFINITE, NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(PLURAL, INDEFINITE, FEMININE, CONSONANT));

        // The
        assertEquals("Neue", m.getString(SINGULAR, DEFINITE, MASCULINE, CONSONANT));
        assertEquals("Neue", m.getString(SINGULAR, DEFINITE, NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(SINGULAR, DEFINITE, FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(PLURAL, DEFINITE, MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(PLURAL, DEFINITE, NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(PLURAL, DEFINITE, FEMININE, CONSONANT));

/* $$$ uncomment below when de/sfdcnames.xml are updated
        // check different cases - accusative
        assertEquals("Neuen", m.getString(l.getNounForm(false, ACCUSATIVE), MASCULINE, CONSONANT));
        assertEquals("Neues", m.getString(l.getNounForm(false, ACCUSATIVE), NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(l.getNounForm(false, ACCUSATIVE), FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, ACCUSATIVE), MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, ACCUSATIVE), NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, ACCUSATIVE), FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(false, ACCUSATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), MASCULINE, CONSONANT));
        assertEquals("Neue", m.getString(l.getNounForm(false, ACCUSATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), NEUTER, CONSONANT));
        assertEquals("Neue", m.getString(l.getNounForm(false, ACCUSATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, ACCUSATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, ACCUSATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, ACCUSATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), FEMININE, CONSONANT));

        // check different cases - dative
        assertEquals("neuem", m.getString(l.getNounForm(false, DATIVE), MASCULINE, CONSONANT));
        assertEquals("neuem", m.getString(l.getNounForm(false, DATIVE), NEUTER, CONSONANT));
        assertEquals("neuer", m.getString(l.getNounForm(false, DATIVE), FEMININE, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(true, DATIVE), MASCULINE, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(true, DATIVE), NEUTER, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(true, DATIVE), FEMININE, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(false, DATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), MASCULINE, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(false, DATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), NEUTER, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(false, DATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), FEMININE, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(true, DATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), MASCULINE, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(true, DATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), NEUTER, CONSONANT));
        assertEquals("neuen", m.getString(l.getNounForm(true, DATIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), FEMININE, CONSONANT));

        // check different cases - Genitive
        assertEquals("Neuen", m.getString(l.getNounForm(false, GENITIVE), MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(false, GENITIVE), NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(false, GENITIVE), FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, GENITIVE), MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, GENITIVE), NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, GENITIVE), FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(false, GENITIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(false, GENITIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(false, GENITIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), FEMININE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, GENITIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), MASCULINE, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, GENITIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), NEUTER, CONSONANT));
        assertEquals("Neuen", m.getString(l.getNounForm(true, GENITIVE, LabelDataPossessiveEnum.NONE, LabelDataArticleEnum.THE), FEMININE, CONSONANT));
*/
    }

    // Test Japanese dictionary
    public void testJapaneseDictionary() throws IllegalArgumentException, IOException {
        // Japanese has no plural form, no cases, no articles, and no start with vowel sounds. Nothing
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("ja"));

        // Account: Masculine
        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "\u53d6\u5f15\u5148", SINGULAR);

        // Any other gender, wrong position should return the same value
        assertNoun(n, "\u53d6\u5f15\u5148", SINGULAR);
        assertNoun(n, "\u53d6\u5f15\u5148", SINGULAR, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", SINGULAR, DEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", PLURAL);
        assertNoun(n, "\u53d6\u5f15\u5148", PLURAL, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", PLURAL, DEFINITE);

        assertNoun(n, "\u53d6\u5f15\u5148", GENITIVE, SINGULAR);
        assertNoun(n, "\u53d6\u5f15\u5148", GENITIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", GENITIVE, SINGULAR, DEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", GENITIVE, PLURAL);
        assertNoun(n, "\u53d6\u5f15\u5148", GENITIVE, PLURAL, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", GENITIVE, PLURAL, DEFINITE);

        assertNoun(n, "\u53d6\u5f15\u5148", DATIVE, SINGULAR);
        assertNoun(n, "\u53d6\u5f15\u5148", DATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", DATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", DATIVE, PLURAL);
        assertNoun(n, "\u53d6\u5f15\u5148", DATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", DATIVE, PLURAL, DEFINITE);

        assertNoun(n, "\u53d6\u5f15\u5148", ACCUSATIVE, SINGULAR);
        assertNoun(n, "\u53d6\u5f15\u5148", ACCUSATIVE, SINGULAR, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", ACCUSATIVE, SINGULAR, DEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", ACCUSATIVE, PLURAL);
        assertNoun(n, "\u53d6\u5f15\u5148", ACCUSATIVE, PLURAL, INDEFINITE);
        assertNoun(n, "\u53d6\u5f15\u5148", ACCUSATIVE, PLURAL, DEFINITE);

        // All the same
        Adjective m = l.getAdjective("new");
        assertNotNull(m);
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, MASCULINE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, NEUTER, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, FEMININE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, MASCULINE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, NEUTER, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, FEMININE, CONSONANT));

        assertEquals("\u65b0\u898f", m.getString(SINGULAR, INDEFINITE, MASCULINE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, INDEFINITE, NEUTER, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, INDEFINITE, FEMININE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, INDEFINITE, MASCULINE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, INDEFINITE, NEUTER, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, INDEFINITE, FEMININE, CONSONANT));

        assertEquals("\u65b0\u898f", m.getString(SINGULAR, DEFINITE, MASCULINE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, DEFINITE, NEUTER, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(SINGULAR, DEFINITE, FEMININE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, DEFINITE, MASCULINE, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, DEFINITE, NEUTER, CONSONANT));
        assertEquals("\u65b0\u898f", m.getString(PLURAL, DEFINITE, FEMININE, CONSONANT));
    }

    // Test Korean dictionary
    public void testKoreanDictionary() throws IllegalArgumentException, IOException {
        // Korean has no plural form, no articles, and no start with vowel sounds.
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("ko"));

        // Account: should return all same result
        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);

        String expected = "\uacc4\uc815";

        // Any other gender, wrong position should return the same value
        assertNoun(n, expected, SINGULAR);
        assertNoun(n, expected, SINGULAR, INDEFINITE);
        assertNoun(n, expected, SINGULAR, DEFINITE);
        assertNoun(n, expected, PLURAL);
        assertNoun(n, expected, PLURAL, INDEFINITE);
        assertNoun(n, expected, PLURAL, DEFINITE);
    }

    // Test Swedish dictionary
    public void testSwedishDictionary() throws IllegalArgumentException, IOException {
        // Swedish has all definition. Just make sure that loader constructs them all right.
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("sv"));

        // "Account":
        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "F\u00f6retag", SINGULAR);
        assertNoun(n, "Ett f\u00f6retag", SINGULAR, INDEFINITE);
        assertNoun(n, "F\u00f6retaget", SINGULAR, DEFINITE);
        assertNoun(n, "F\u00f6retag", PLURAL);
        assertNoun(n, "F\u00f6retag", PLURAL, INDEFINITE);
        assertNoun(n, "F\u00f6retagen", PLURAL, DEFINITE);

        // "Contact":
        n = l.getNoun("contact", false);
        assertNotNull(n);
        assertEquals(n.getGender(), EUTER);
        assertEquals(n.getStartsWith(), CONSONANT);
        assertNoun(n, "Kontakt", SINGULAR);
        assertNoun(n, "En kontakt", SINGULAR, INDEFINITE);
        assertNoun(n, "Kontakten", SINGULAR, DEFINITE);
        assertNoun(n, "Kontakter", PLURAL);
        assertNoun(n, "Kontakter", PLURAL, INDEFINITE);
        assertNoun(n, "Kontakterna", PLURAL, DEFINITE);

        n = l.getNoun("pricebook", false);
        assertNotNull(n);
        assertEquals(n.getGender(), EUTER);
        assertEquals(n.getStartsWith(), CONSONANT);

        Adjective m = l.getAdjective("active");
        assertNotNull(m);
        assertEquals("Aktiv", m.getString(SINGULAR, EUTER, CONSONANT));
        assertEquals("Aktivt", m.getString(SINGULAR, NEUTER, CONSONANT));
        assertEquals("Aktiva", m.getString(PLURAL,  EUTER, CONSONANT));
        assertEquals("Aktiva", m.getString(PLURAL, NEUTER, CONSONANT));

        // A: is same as none
        assertEquals("Aktiv", m.getString(SINGULAR, INDEFINITE, EUTER, CONSONANT));
        assertEquals("Aktivt", m.getString(SINGULAR, INDEFINITE, NEUTER, CONSONANT));
        assertEquals("Aktiva", m.getString(PLURAL, INDEFINITE,  EUTER, CONSONANT));
        assertEquals("Aktiva", m.getString(PLURAL, INDEFINITE, NEUTER, CONSONANT));

        assertEquals("Aktiv", m.getString(SINGULAR, DEFINITE, EUTER, CONSONANT));
        assertEquals("Aktivt", m.getString(SINGULAR, DEFINITE, NEUTER, CONSONANT));
        assertEquals("Aktiva", m.getString(PLURAL, DEFINITE,  EUTER, CONSONANT));
        assertEquals("Aktiva", m.getString(PLURAL, DEFINITE, NEUTER, CONSONANT));
    }

    // Test Finnish dictionary
    public void testFinnishDictionary() throws IllegalArgumentException, IOException {
        // Finnish has no article, no gender and no dpendency of the first letter of noun. But just has very many cases and possessive
        LanguageDictionary l = loadDictionary(LanguageProviderFactory.get().getLanguage("fi"));

        Noun n = l.getNoun("account", false);
        assertNotNull(n);
        assertEquals(n.getGender(), NEUTER);
        assertEquals(n.getStartsWith(), CONSONANT);

        assertNoun(n, "Tili", NOMINATIVE, SINGULAR);
        assertNoun(n, "Tilini", NOMINATIVE, SINGULAR, FIRST);
        assertNoun(n, "Tilisi", NOMINATIVE, SINGULAR, SECOND);
        assertNoun(n, "Tilit", NOMINATIVE, PLURAL);
        assertNoun(n, "Tilini", NOMINATIVE, PLURAL, FIRST);
        assertNoun(n, "Tilisi", NOMINATIVE, PLURAL, SECOND);

        assertNoun(n, "Tilin", GENITIVE, SINGULAR);
        assertNoun(n, "Tilini", GENITIVE, SINGULAR, FIRST);
        assertNoun(n, "Tilisi", GENITIVE, SINGULAR, SECOND);
        assertNoun(n, "Tilien", GENITIVE, PLURAL);
        assertNoun(n, "Tilieni", GENITIVE, PLURAL, FIRST);
        assertNoun(n, "Tiliesi", GENITIVE, PLURAL, SECOND);

        assertNoun(n, "Tiliss\u00e4", INESSIVE, SINGULAR);
        assertNoun(n, "Tiliss\u00e4ni", INESSIVE, SINGULAR, FIRST);
        assertNoun(n, "Tiliss\u00e4si", INESSIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileiss\u00e4", INESSIVE, PLURAL);
        assertNoun(n, "Tileiss\u00e4ni", INESSIVE, PLURAL, FIRST);
        assertNoun(n, "Tileiss\u00e4si", INESSIVE, PLURAL, SECOND);

        assertNoun(n, "Tilist\u00e4", ELATIVE, SINGULAR);
        assertNoun(n, "Tilist\u00e4ni", ELATIVE, SINGULAR, FIRST);
        assertNoun(n, "Tilist\u00e4si", ELATIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileist\u00e4", ELATIVE, PLURAL);
        assertNoun(n, "Tileist\u00e4ni", ELATIVE, PLURAL, FIRST);
        assertNoun(n, "Tileist\u00e4si", ELATIVE, PLURAL, SECOND);

        assertNoun(n, "Tiliin", ILLATIVE, SINGULAR);
        assertNoun(n, "Tiliini", ILLATIVE, SINGULAR, FIRST);
        assertNoun(n, "Tiliisi", ILLATIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileihin", ILLATIVE, PLURAL);
        assertNoun(n, "Tileihini", ILLATIVE, PLURAL, FIRST);
        assertNoun(n, "Tileihisi", ILLATIVE, PLURAL, SECOND);

        assertNoun(n, "Tilill\u00e4", ADESSIVE, SINGULAR);
        assertNoun(n, "Tilill\u00e4ni", ADESSIVE, SINGULAR, FIRST);
        assertNoun(n, "Tilill\u00e4si", ADESSIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileill\u00e4", ADESSIVE, PLURAL);
        assertNoun(n, "Tileill\u00e4ni", ADESSIVE, PLURAL, FIRST);
        assertNoun(n, "Tileill\u00e4si", ADESSIVE, PLURAL, SECOND);

        assertNoun(n, "Tililt\u00e4", ABLATIVE, SINGULAR);
        assertNoun(n, "Tililt\u00e4ni", ABLATIVE, SINGULAR, FIRST);
        assertNoun(n, "Tililt\u00e4si", ABLATIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileilt\u00e4", ABLATIVE, PLURAL);
        assertNoun(n, "Tileilt\u00e4ni", ABLATIVE, PLURAL, FIRST);
        assertNoun(n, "Tileilt\u00e4si", ABLATIVE, PLURAL, SECOND);

        assertNoun(n, "Tilille", ALLATIVE, SINGULAR);
        assertNoun(n, "Tililleni", ALLATIVE, SINGULAR, FIRST);
        assertNoun(n, "Tilillesi", ALLATIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileille", ALLATIVE, PLURAL);
        assertNoun(n, "Tileilleni", ALLATIVE, PLURAL, FIRST);
        assertNoun(n, "Tileillesi", ALLATIVE, PLURAL, SECOND);

        assertNoun(n, "Tilin\u00e4", ESSIVE, SINGULAR);
        assertNoun(n, "Tilin\u00e4ni", ESSIVE, SINGULAR, FIRST);
        assertNoun(n, "Tilin\u00e4si", ESSIVE, SINGULAR, SECOND);
        assertNoun(n, "Tilein\u00e4", ESSIVE, PLURAL);
        assertNoun(n, "Tilein\u00e4ni", ESSIVE, PLURAL, FIRST);
        assertNoun(n, "Tilein\u00e4si", ESSIVE, PLURAL, SECOND);

        assertNoun(n, "Tiliksi", TRANSLATIVE, SINGULAR);
        assertNoun(n, "Tilikseni", TRANSLATIVE, SINGULAR, FIRST);
        assertNoun(n, "Tiliksesi", TRANSLATIVE, SINGULAR, SECOND);
        assertNoun(n, "Tileiksi", TRANSLATIVE, PLURAL);
        assertNoun(n, "Tileikseni", TRANSLATIVE, PLURAL, FIRST);
        assertNoun(n, "Tileiksesi", TRANSLATIVE, PLURAL, SECOND);

        assertNoun(n, "Tili\u00e4", PARTITIVE, SINGULAR);
        assertNoun(n, "Tili\u00e4ni", PARTITIVE, SINGULAR, FIRST);
        assertNoun(n, "Tili\u00e4si", PARTITIVE, SINGULAR, SECOND);
        assertNoun(n, "Tilej\u00e4", PARTITIVE, PLURAL);
        assertNoun(n, "Tilej\u00e4ni", PARTITIVE, PLURAL, FIRST);
        assertNoun(n, "Tilej\u00e4si", PARTITIVE, PLURAL, SECOND);

        // Check w/ article form - should be ignored but returns no-article form

        for (LanguageCase caseType : l.getDeclension().getRequiredCases()) {
            for (LanguagePossessive possessive : l.getDeclension().getRequiredPossessive()) {
                String s = n.getString(l.getDeclension().getApproximateNounForm(LanguageNumber.SINGULAR, caseType, possessive, LanguageArticle.ZERO));

                assertEquals(s, n.getString(l.getDeclension().getApproximateNounForm(LanguageNumber.SINGULAR, caseType, possessive, LanguageArticle.INDEFINITE)));
                assertEquals(s, n.getString(l.getDeclension().getApproximateNounForm(LanguageNumber.SINGULAR, caseType, possessive, LanguageArticle.DEFINITE)));
            }
        }

        // check adjective - Finnish cares only number/cases
        Adjective m = l.getAdjective("new");
        assertNotNull(m);
        assertEquals("Uusi", m.getString(LanguageNumber.SINGULAR, NOMINATIVE, NEUTER, CONSONANT));
        assertEquals("Uudet", m.getString(LanguageNumber.PLURAL, NOMINATIVE, NEUTER, CONSONANT));
        assertEquals("Uuden", m.getString(LanguageNumber.SINGULAR, GENITIVE, NEUTER, CONSONANT));
        assertEquals("Uusien", m.getString(LanguageNumber.PLURAL, GENITIVE, NEUTER, CONSONANT));
        assertEquals("Uudessa", m.getString(LanguageNumber.SINGULAR, INESSIVE, NEUTER, CONSONANT));
        assertEquals("Uusissa", m.getString(LanguageNumber.PLURAL, INESSIVE, NEUTER, CONSONANT));
        assertEquals("Uudesta", m.getString(LanguageNumber.SINGULAR, ELATIVE, NEUTER, CONSONANT));
        assertEquals("Uusista", m.getString(LanguageNumber.PLURAL, ELATIVE, NEUTER, CONSONANT));
        assertEquals("Uuteen", m.getString(LanguageNumber.SINGULAR, ILLATIVE, NEUTER, CONSONANT));
        assertEquals("Uusiin", m.getString(LanguageNumber.PLURAL, ILLATIVE, NEUTER, CONSONANT));
        assertEquals("Uudella", m.getString(LanguageNumber.SINGULAR, ADESSIVE, NEUTER, CONSONANT));
        assertEquals("Uusilla", m.getString(LanguageNumber.PLURAL, ADESSIVE, NEUTER, CONSONANT));
        assertEquals("Uudelta", m.getString(LanguageNumber.SINGULAR, ABLATIVE, NEUTER, CONSONANT));
        assertEquals("Uusilta", m.getString(LanguageNumber.PLURAL, ABLATIVE, NEUTER, CONSONANT));
        assertEquals("Uudelle", m.getString(LanguageNumber.SINGULAR, ALLATIVE, NEUTER, CONSONANT));
        assertEquals("Uusille", m.getString(LanguageNumber.PLURAL, ALLATIVE, NEUTER, CONSONANT));
        assertEquals("Uutena", m.getString(LanguageNumber.SINGULAR, ESSIVE, NEUTER, CONSONANT));
        assertEquals("Uusina", m.getString(LanguageNumber.PLURAL, ESSIVE, NEUTER, CONSONANT));
        assertEquals("Uudeksi", m.getString(LanguageNumber.SINGULAR, TRANSLATIVE, NEUTER, CONSONANT));
        assertEquals("Uusiksi", m.getString(LanguageNumber.PLURAL, TRANSLATIVE, NEUTER, CONSONANT));
        assertEquals("Uutta", m.getString(LanguageNumber.SINGULAR, PARTITIVE, NEUTER, CONSONANT));
        assertEquals("Uusia", m.getString(LanguageNumber.PLURAL, PARTITIVE, NEUTER, CONSONANT));
    }

    public void testRussianDictionary() throws IllegalArgumentException {
        /*
         * Nothing to test yet.
         */
    }

    /**
     * Unit test for utility methods
     */
    public void testDictionaryMethods() throws IOException {
        LanguageDictionary l1 = loadDictionary(LanguageProviderFactory.get().getLanguage("fi"));
        LanguageDictionary l2 = loadDictionary(LanguageProviderFactory.get().getLanguage("fi"));
        LanguageDictionary l3 = loadDictionary(LanguageProviderFactory.get().getLanguage("ru"));
        assertEquals("Loading isn't equal.", l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
        assertNotSame(l1.hashCode(), l3.hashCode());
        assertTrue(l1.isEntity("Account"));
        assertFalse(l1.isEntity("Accounts"));
        assertTrue(l1.isEntityPlural("Accounts"));
        assertTrue(l1.isNoun("Accounts"));
        assertTrue(l1.isNoun("Account"));
        assertFalse(l1.isNoun("item"));
        assertFalse(l1.isEntity("item"));
        assertFalse(l1.isEntityPlural("item"));

        // Make sure the names are the same
        assertEquals(ImmutableSet.of("account", "account_name", "parent_account"), l1.getNames("Account", true));
        assertEquals(ImmutableSet.of("account_name", "parent_account"), l1.getNames("Account", false));
        assertEquals(l1.getNames("Account", true), l3.getNames("Account", true));
        assertEquals(l1.getNames("Account", false), l3.getNames("Account", false));
        assertNotSame(l1.getNames("Account", true), l3.getNames("Account", false));
    }
}
