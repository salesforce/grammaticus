/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;


/**
 * Test of the base "renaming" functionality
 * @author stamm
 */
public class GrammaticalLabelRenamingTest extends BaseGrammaticalLabelTest {
    public GrammaticalLabelRenamingTest(String name) {
        super(name);
    }

    /**
     * Test the escapeHtml flag on entities in cases where the label will be used unescaped directly.
     */
    public void testEscapeHtml() throws Exception {
        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        HumanLanguage ENGLISH = LanguageProviderFactory.get().getBaseLanguage();
        try {
            Renameable account = getStandardRenameable("Account");
            assertEquals("{0} with these Accounts", renderDynamicLabel(ENGLISH, "{0} with <these/> <Entity plural=\"y\" entity=\"0\"/>", account));
            assertEquals("{0} with these Accounts", renderDynamicLabel(ENGLISH, "{0} with <these/> <Entity plural=\"y\" entity=\"0\" escapeHtml=\"true\"/>", account));

            MockRenamingProvider newProvider = new MockRenamingProvider(makeEnglishNoun("Account", NounType.ENTITY, LanguageStartsWith.CONSONANT,
                    "Client or Person", "Clients & People"));
            RenamingProviderFactory.get().setProvider(newProvider);

            assertEquals("{0} with these Clients & People", renderDynamicLabel(ENGLISH, "{0} with <these/> <Entity plural=\"y\" entity=\"0\"/>", account));
            assertEquals("{0} with these Clients & People", renderDynamicLabel(ENGLISH, "{0} with <these/> <Entity plural=\"y\" entity=\"0\" escapeHtml=\"n\"/>", account));
            assertEquals("{0} with these Clients &amp; People", renderDynamicLabel(ENGLISH, "{0} with <these/> <Entity plural=\"y\" entity=\"0\" escapeHtml=\"true\"/>", account));
            assertEquals("{0} with these Clients &amp; People", renderDynamicLabel(ENGLISH, "{0} with <these/> <Entity plural=\"y\" entity=\"0\" escapeHtml=\"y\"/>", account));
        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }


}
