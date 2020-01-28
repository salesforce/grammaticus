/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.util.Locale;
import java.util.TreeSet;

import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * @author stamm
 */
public class GrammaticalLabelFileTest extends BaseGrammaticalLabelTest {
    public GrammaticalLabelFileTest(String name) {
        super(name);
    }

    public void testEnglishArticles() throws Exception {
    	final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
    	final HumanLanguage ENGLISH_CA = LanguageProviderFactory.get().getLanguage(Locale.CANADA);
        assertEquals("This quote can't be synced because it has an inactive or archived price book.",
                renderLabel(ENGLISH, "<This/> <quote/> can't be synced because it has <a/> <inactive/> or <archived/> <pricebook/>.", null));
        assertEquals("Ask a Question", renderLabel(ENGLISH, "Ask <a/> <Question/>", null));
        assertEquals("Ask a Question", renderLabel(ENGLISH_CA, "Ask <a/> <Question/>", null));
    }

    public void testGermanArticles() throws Exception {
    	final HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage("de");
        // Validate that the definitiveness passes throughout the whole thing
        assertEquals("Die Neuen Accounts", renderLabel(GERMAN, "<The/> <New/> <Accounts/>", null));
        assertEquals("Der Neue Account", renderLabel(GERMAN, "<The/> <New/> <Account/>", null));
        assertEquals("Ein Neuer Account", renderLabel(GERMAN, "<A/> <New/> <Account/>", null));
        assertEquals("Ein Neuer Account und Ein Neues Dokument und Eine Neue Kampagne", renderLabel(GERMAN, "<A/> <New/> <Account/> und <A/> <New/> <Document/> und <A/> <New/> <Campaign/>", null));
        assertEquals("Einem neuem Account und Einem neuem Dokument und Einer neuer Kampagne", renderLabel(GERMAN, "<A/> <New/> <Account case='d'/> und <A/> <New/> <Document case='d'/> und <A/> <New/> <Campaign case='d'/>", null));

        // tests article override in adjectives
        assertEquals("Ein Anderer Account", renderLabel(GERMAN, "<A/> <Other article=\"the\"/> <Account/>", null));
    }

    public void testLegacyArticleForm() throws Exception {
    	final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
    	final HumanLanguage ITALIAN = LanguageProviderFactory.get().getLanguage("it");
    	final HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage("de");
    	// This tests some old crappy behavior in label files.  Don't use this
        assertEquals("An account", renderLabel(ENGLISH, "<Account article=\"a\"/>", null));
        assertEquals("The account", renderLabel(ENGLISH, "<Account article=\"the\"/>", null));
        assertEquals("Un account", renderLabel(ITALIAN, "<Account article=\"a\"/>", null));
        assertEquals("L'account", renderLabel(ITALIAN, "<Account article=\"the\"/>", null));
        // Note, in german the nouns are always capitalized.  Always
        assertEquals("Ein Account", renderLabel(GERMAN, "<Account article=\"a\"/>", null));
        assertEquals("Der Account", renderLabel(GERMAN, "<Account article=\"the\"/>", null));
        assertEquals("Einem Account", renderLabel(GERMAN, "<Account article=\"a\" case=\"d\"/>", null));
        assertEquals("Dem Account", renderLabel(GERMAN, "<Account article=\"the\" case=\"d\"/>", null));
    }

    /**
     * There are two lowercase sigmas for Greek (σ and ς), depending on where in the word the sigma appears
     * Make sure that these remain unchanged on case-folding, and that upper-case sigma (Σ) folds to
     * the right character, which should be σ since we only capitalize the first letter in sfdcnames.xml
     * and sfdcadjectives.xml
     */
    public void testGreekSigma() throws Exception {
    	final HumanLanguage GREEK = LanguageProviderFactory.get().getLanguage(LanguageConstants.GREEK);
        // lowercase sigmas should remain unchanged with case folding
        assertEquals("Ανοιχτές Εργασίες", renderLabel(GREEK, "<Open/> <Tasks case=\"a\"/>"));
        assertEquals("ανοιχτές εργασίες", renderLabel(GREEK, "<open/> <tasks case=\"a\"/>"));
        assertEquals("Ανοιχτή Εργασία", renderLabel(GREEK, "<Open/> <Task case=\"a\"/>"));
        assertEquals("ανοιχτή εργασία", renderLabel(GREEK, "<open/> <task case=\"a\"/>"));

        // uppercase sigma should correctly fold to lowercase sigma;
        // we never have all-cap words in sfdcnames.xml and sfdcadjectives.xml, so don't need to worry
        // about folding uppercase sigma to word-final sigma
        assertEquals("Συσχετισμένη Εκστρατεία", renderLabel(GREEK, "<Associated/> <Campaign/>"));
        assertEquals("συσχετισμένη εκστρατεία", renderLabel(GREEK, "<associated/> <campaign/>"));
    }

    public void testDifferenceInRenameTabs() throws Exception {
        StringBuilder diffs = new StringBuilder();
        for (HumanLanguage language : LanguageProviderFactory.get().getAll()) {
            if (!LanguageDeclensionFactory.get().getDeclension(language).hasPlural()) continue;
            LanguageDictionary dictionary = loadDictionary(language);
            Multimap<String,String> nowHasPlural = TreeMultimap.create();
            Multimap<String,String> missingPlural = TreeMultimap.create();

            for (String entity : new TreeSet<String>(dictionary.getNounsByEntity().keySet())) {
                for (Noun n : dictionary.getNounsByEntity().get(entity)) {
                    if (n.getNounType() == NounType.ENTITY) continue;
                    boolean hasPluralForm = false;
                    for (NounForm form : n.getAllDefinedValues().keySet()) {
                        if (form.getNumber() == LanguageNumber.PLURAL && form.getCase() == LanguageCase.NOMINATIVE) {
                            hasPluralForm = true;
                            break;
                        }
                    }
                    if (hasPluralForm != (n.getNounType() == NounType.FIELD)) {
                        if (hasPluralForm) {
                            nowHasPlural.put(entity, n.getName());
                        } else {
                            missingPlural.put(entity, n.getName());
                        }
                    }
                }
            }
            if (nowHasPlural.size() > 0) {
                diffs.append(language).append(" can rename plural fields for: ").append(nowHasPlural).append('\n');
            }
            if (missingPlural.size() > 0) {
                diffs.append(language).append(" has these plural fields removed for rename: ").append(missingPlural).append('\n');
            }
        }
        System.out.println(diffs.toString());
    }
}
