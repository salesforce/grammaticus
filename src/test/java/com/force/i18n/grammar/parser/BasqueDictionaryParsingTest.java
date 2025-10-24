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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.LabelUtils;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.Adjective;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.impl.BasqueDeclension.BasqueNounForm;
import com.force.i18n.grammar.impl.BasqueDeclension.BasqueAdjectiveForm;

/**
 * Tests parsing of irregular adjective and noun values in Basque dictionaries.
 */
class BasqueDictionaryParsingTest {
    // Use composition instead of inheritance because BaseGrammaticalLabelTest extends
    // junit.framework.TestCase (JUnit 3/4), which interferes with JUnit 5 (Jupiter)
    // lifecycle and assertions. This keeps this class purely JUnit 5 while reusing helpers.
    private final BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("BasqueDictionaryParsingTest") {};

    @BeforeEach
    void setUpEach() throws Exception {
        helper.setUp();
    }

    @Test
    void testIrregularDemonstrativeValues() throws IOException {
        final HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");

        final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><adjectives>%s</adjectives>";
        final String adjectives =
            "  <adjective name=\"This\">" +
            "    <value>hau</value>" + // case="n" plural="n"
            "    <value plural=\"y\">hauek</value>" + // case="n"
            "    <value case=\"er\" plural=\"n\">honek</value>" +
            "    <value case=\"d\" plural=\"n\">honi</value>" +
            "    <value case=\"g\" plural=\"n\">honen</value>" +
            "    <value case=\"be\" plural=\"n\">honentzat</value>" +
            "  </adjective>";

        String grammar = String.format(header, adjectives);
        TestLanguageLabelSetDescriptor descriptor = new TestLanguageLabelSetDescriptor(helper.getDescriptor(eu),
                LabelUtils.getSampleLabelFile("<section name=\"T\"><param name=\"p\">t</param></section>"),
                LabelUtils.getSampleGrammarFile(grammar));

        LanguageDictionary dict = new LanguageDictionaryParser(descriptor, descriptor.getLanguage(), null)
                .getDictionary();

        Adjective thisAdj = dict.getAdjective("this");
        java.util.Map<? extends AdjectiveForm, String> vals = thisAdj.getAllValues();

        // Absolutive (nom) forms
        assertEquals("hau", vals.get(BasqueAdjectiveForm.SG_ABS));
        assertEquals("hauek", vals.get(BasqueAdjectiveForm.PL_ABS));

        // Ergative
        assertEquals("honek", vals.get(BasqueAdjectiveForm.SG_ERG));

        // Dative
        assertEquals("honi", vals.get(BasqueAdjectiveForm.SG_DAT));

        // Genitive
        assertEquals("honen", vals.get(BasqueAdjectiveForm.SG_GEN));

        // Benefactive (mapped to CAUSALFINAL)
        assertEquals("honentzat", vals.get(BasqueAdjectiveForm.SG_BEN));
    }

    @Test
    void testIrregularNounValues() throws IOException {
        final HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");

        final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><names>%s</names>";
        final String nouns = """
                <noun name="DemoNoun">
                    <value plural="n">demo</value> <!-- base (case="n", plural="n", article="i") -->
                    <value case="g" plural="n" article="d">demo-gen-sg-def</value>
                    <value case="d" plural="n" article="d">demo-dat-sg-def</value>
                </noun>
                """;

        String grammar = String.format(header, nouns);
        TestLanguageLabelSetDescriptor descriptor = new TestLanguageLabelSetDescriptor(helper.getDescriptor(eu),
                LabelUtils.getSampleLabelFile("<section name=\"T\"><param name=\"p\">t</param></section>"),
                LabelUtils.getSampleGrammarFile(grammar));

        LanguageDictionary dict = new LanguageDictionaryParser(descriptor, descriptor.getLanguage(), null)
                .getDictionary();

        // Noun multi-value check: explicit GEN/SG/DEF and DAT/SG/DEF override values should be retrievable
        // (Here we only validate dictionary stores them; rendering selection is exercised elsewhere.)
        com.force.i18n.grammar.Noun demoNoun = dict.getNoun("demonoun", false);
        java.util.Map<? extends com.force.i18n.grammar.NounForm, String> nounVals = demoNoun.getAllDefinedValues();
        assertEquals("demo", nounVals.get(BasqueNounForm.SG_N_IND));
        assertNull(nounVals.get(BasqueNounForm.SG_N_DEF));

        assertTrue(nounVals.values().contains("demo-gen-sg-def"));
        assertTrue(nounVals.values().contains("demo-dat-sg-def"));
    }
}


