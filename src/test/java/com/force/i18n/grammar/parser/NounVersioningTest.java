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
