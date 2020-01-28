/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.parser;

import com.force.i18n.*;
import com.force.i18n.grammar.*;

/**
 * Parser test for plural and gender choice
 * @author stamm
 * @since 0.6.0
 */
public class ChoiceLabelTest extends BaseGrammaticalLabelTest {

    public ChoiceLabelTest(String name) {
        super(name);
    }

    public void testEnglishPluralsWithZero() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage("en_US");
        // Test autoderiving for entity nouns. These values were provided by the translators
        String[] synonyms = new String[] {
                "<plural num=\"0\"><when val=\"zero\">No items</when><when val=\"one\">{0} item</when><when default=\"y\">{0} items</when></plural>",
                "<plural num=\"0\"><when val=\"0\">No items</when><when val=\"1\">{0} item</when><when val=\"n\">{0} items</when></plural>",
                "<plural num=\"0\"><when val=\"0\">No items</when><when val=\"1\">{0} item</when><when val=\"other\">{0} items</when></plural>",
                "<plural num=\"0\"><when val=\"zero\">No items</when><when val=\"one\">{0} item</when>{0} items</plural>", };

        for (String itemsStr : synonyms) {
            assertValue(ENGLISH, itemsStr, "No items", 0);
            assertValue(ENGLISH, itemsStr, "1 item", 1);
            assertValue(ENGLISH, itemsStr, "2 items", 2);
            assertValue(ENGLISH, itemsStr, "-1 items", -1);
            assertValue(ENGLISH, itemsStr, "0.5 items", 0.5);
            assertValue(ENGLISH, itemsStr, "{0} items");
            assertValue(ENGLISH, itemsStr, "null items", new Object[] { null });
            assertValue(ENGLISH, itemsStr, "1.0 item", "1.0");
            assertValue(ENGLISH, itemsStr, "No items", "0");
        }
    }

    public void testEnglishPluralsNoZero() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage("en_US");
        // Test auto-deriving for entity nouns. These values were provided by the translators
        String itemsStr = "<plural num=\"0\"><when val=\"one\">{0} item</when><when default=\"y\">{0} items</when></plural>";
        assertValue(ENGLISH, itemsStr, "0 items", 0);
        assertValue(ENGLISH, itemsStr, "null items", new Object[] { null });
        assertValue(ENGLISH, itemsStr, "0 items", "0");

        String embedded = "Test: " + itemsStr;
        assertValue(ENGLISH, embedded, "Test: 0 items", 0);
        assertValue(ENGLISH, embedded, "Test: null items", new Object[] { null });
        assertValue(ENGLISH, embedded, "Test: 0 items", "0");
    }

    public void testEnglishLabels() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage("en_US");
        String testStr = "There <plural num=\"0\"><when val=\"0\">are <no/> <accounts/></when><when val=\"1\">is <a/> <account/></when><when default=\"y\">are {0} <accounts/></when></plural>";
        assertValue(ENGLISH, testStr, "There are no accounts", 0);
        assertValue(ENGLISH, testStr, "There is an account", 1);
        assertValue(ENGLISH, testStr, "There are 3 accounts", 3);
    }

    public void testBadPluralParsing() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage("en_US");
        assertValue(ENGLISH, "<plural num=\"1\"></plural>", "", 0);
        assertValue(ENGLISH, "<plural/>", "", 0);
        // Duplicate value
        assertValue(ENGLISH,
                "<plural num=\"1\"><when val=\"n\">{0} item</when><when val=\"n\">{0} items</when></plural>", "0 items",
                0);
        assertValue(ENGLISH, "<plural num=\"1\"><when>{0} item</when><when>{0} items</when></plural>", "", 0);
    }

    public void testRussianPlurals() throws Exception {
        final HumanLanguage RUSSIAN = LanguageProviderFactory.get().getLanguage("ru");

        // TODO: Test if this is grammatically correct. Note, this tests multiple categories per when.
        String itemsStr = "<plural num=\"0\"><when val=\"one\">Есть {0} <document/></when><when val=\"few\">Есть {0} <documents/></when><when val=\"many,other\">Есть {0} <documents case=\"g\"/></when></plural>";
        assertValue(RUSSIAN, itemsStr, "Есть 1 документ", 1);
        assertValue(RUSSIAN, itemsStr, "Есть 2 документы", 2);
        assertValue(RUSSIAN, itemsStr, "Есть 3 документы", 3);
        assertValue(RUSSIAN, itemsStr, "Есть 4 документы", 4);
        assertValue(RUSSIAN, itemsStr, "Есть 5 документов", 5);
        assertValue(RUSSIAN, itemsStr, "Есть 6 документов", 6);
        assertValue(RUSSIAN, itemsStr, "Есть 0 документов", 0);
        assertValue(RUSSIAN, itemsStr, "Есть 11 документов", 11);
        assertValue(RUSSIAN, itemsStr, "Есть 21 документ", 21);
        assertValue(RUSSIAN, itemsStr, "Есть 24 документы", 24);
        assertValue(RUSSIAN, itemsStr, "Есть 31 документ", 31);
        assertValue(RUSSIAN, itemsStr, "Есть 100 документов", 100);
    }

    public void testHebrewPlurals() throws Exception {
    	final HumanLanguage HEBREW = LanguageProviderFactory.get().getLanguage("iw");

    	// Try with dual
    	String itemsStr =  "<plural num=\"0\"><when val=\"1\">יום אחד</when><when val=\"2\">יומיים</when><when val=\"other\">{0} ימים</when></plural>";

        assertValue(HEBREW, itemsStr, "יום אחד", 1);
        assertValue(HEBREW, itemsStr, "יומיים", 2);
        assertValue(HEBREW, itemsStr, "3 ימים", 3);

        // Try without dual, make sure other is valid
    	itemsStr =  "<plural num=\"0\"><when val=\"1\">אדם אחד</when><when val=\"other\">{0} אנשים</when></plural>";

        assertValue(HEBREW, itemsStr, "אדם אחד", 1);
        assertValue(HEBREW, itemsStr, "2 אנשים", 2);
        assertValue(HEBREW, itemsStr, "3 אנשים", 3);
    }

    public void testHebrewGender() throws Exception {
        final HumanLanguage HEBREW = LanguageProviderFactory.get().getLanguage("iw");
        LanguageDictionary dict = loadDictionary(HEBREW);

        String[] accountStrs = new String[] { "<Account/> <gender><when val=\"f\">נפתחה</when>נפתח</gender>",
                "<Account/> <gender><when val=\"f\">נפתחה</when><when val=\"m\">נפתח</when></gender>",
                "<Account/> <gender><when val=\"f\">נפתחה</when><when default=\"y\">נפתח</when></gender>" };
        String[] entityStrs = new String[] { "<Entity entity=\"0\"/> <gender><when val=\"f\">נפתחה</when>נפתח</gender>",
                "<Entity entity=\"0\"/> <gender><when val=\"f\">נפתחה</when><when val=\"m\">נפתח</when></gender>",
                "<Entity entity=\"0\"/> <gender><when val=\"f\">נפתחה</when><when default=\"y\">נפתח</when></gender>" };
        // Try with a feminine noun
        String[] activityStrs = new String[] { "<Activity/> <gender><when val=\"m\">נפתח</when>נפתחה</gender>",
                "<Activity/> <gender><when val=\"m\">נפתח</when><when val=\"f\">נפתחה</when></gender>",
                "<Activity/> <gender><when val=\"m\">נפתח</when><when default=\"y\">נפתחה</when></gender>" };

        for (String itemsStr : accountStrs) {
            assertValue(HEBREW, itemsStr, "חשבון נפתח");
        }

        for (String itemsStr : activityStrs) {
            assertValue(HEBREW, itemsStr, "פעילות נפתחה");
        }

        // Try it with the entity reference
        for (String itemsStr : entityStrs) {
            assertValue(HEBREW, itemsStr, new Renameable[] { new MockExistingRenameable("account", dict) }, "חשבון נפתח");
            assertValue(HEBREW, itemsStr, new Renameable[] { new MockExistingRenameable("activity", dict) }, "פעילות נפתחה");
        }

        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            // Switch account to activity which is female and vice-versa
            Noun accountNoun = dict.getNoun("account", false);
            Noun activityNoun = dict.getNoun("activity", false);
            Noun renamedAccount = accountNoun.clone(activityNoun.getGender(), activityNoun.getStartsWith(),
                    activityNoun.getAllDefinedValues());
            Noun renamedActivity = activityNoun.clone(accountNoun.getGender(), accountNoun.getStartsWith(),
                    accountNoun.getAllDefinedValues());

            MockRenamingProvider newProvider = new MockRenamingProvider(renamedAccount, renamedActivity);
            RenamingProviderFactory.get().setProvider(newProvider);

            for (String itemsStr : accountStrs) {
                assertValue(HEBREW, itemsStr, "פעילות נפתחה"); // Feminine verb to go with feminine noun
            }
            for (String itemsStr : activityStrs) {
                assertValue(HEBREW, itemsStr, "חשבון נפתח");
            }

            // Turn off renamed nouns temporarily
            newProvider.setUseRenamedNouns(false);

            for (String itemsStr : accountStrs) {
                assertValue(HEBREW, itemsStr, "חשבון נפתח");
            }
            for (String itemsStr : activityStrs) {
                assertValue(HEBREW, itemsStr, "פעילות נפתחה");
            }
        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }
}
