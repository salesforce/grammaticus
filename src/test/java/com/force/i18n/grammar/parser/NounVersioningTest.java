/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;

/**
 * Test of versioning of nouns.
 *
 * @author stamm
 * @since 226.0
 */
public class NounVersioningTest extends BaseGrammaticalLabelTest {

    /**
     * @param name
     */
    public NounVersioningTest(String name) {
        super(name);
    }

    @Override
    protected URL getLabelURL() throws IOException {
        return new File("src/test/resources/version").getCanonicalFile().toURI().toURL();
    }

    public void testEnglish() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(getDescriptor());
        GrammaticalLabelSet set = loader.getSet(ENGLISH);
        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            assertEquals("An Account", set.getString("Global", "anAccount"));
            MockRenamingProvider newProvider = new MockRenamingProvider();
            RenamingProviderFactory.get().setProvider(newProvider);
            newProvider.setLabelVersion(1.0);
            assertEquals("An Account", set.getString("Global", "anAccount"));
            newProvider.setLabelVersion(2.0);
            // Make sure starts with consonant
            assertEquals("A Client", set.getString("Global", "anAccount"));
            newProvider.setLabelVersion(3.0);
            assertEquals("A Client", set.getString("Global", "anAccount"));

        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }

    public void testGerman() throws Exception {
        final HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage(Locale.GERMAN);
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(getDescriptor());
        GrammaticalLabelSet set = loader.getSet(GERMAN);
        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            assertEquals("Neuer Account", set.getString("Global", "newAccount"));
            MockRenamingProvider newProvider = new MockRenamingProvider();
            RenamingProviderFactory.get().setProvider(newProvider);
            newProvider.setLabelVersion(1.0);
            assertEquals("Neuer Account", set.getString("Global", "newAccount"));
            newProvider.setLabelVersion(2.0);
            assertEquals("Neuer Kunde", set.getString("Global", "newAccount"));
            newProvider.setLabelVersion(3.0);
            // Make sure it's neuter
            assertEquals("Neues Konto", set.getString("Global", "newAccount"));

        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }
}
