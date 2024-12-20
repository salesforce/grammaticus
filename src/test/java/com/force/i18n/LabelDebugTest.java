/*
 * Copyright (c) 2024, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;

import com.force.i18n.grammar.*;
import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;
import com.force.i18n.settings.SettingsSectionNotFoundException;
import com.google.common.collect.SetMultimap;

/**
 * Test Label Debug system, which allows tracking of what labels are displayed, where labels come
 * from, masking them so that it's easier to see untranslated labels.
 * @author stamm
 * @since 1.2.0
 */
public class LabelDebugTest extends BaseGrammaticalLabelTest {

    /**
     * @param name
     */
    public LabelDebugTest(String name) {
        super(name);
    }

    public void testLabelDebug() {
        LocalizerProvider oldProvider = LocalizerFactory.get();
        try {
            // Set the debug provider to true
            LabelDebugProvider.setLabelDebugProviderEnabled(true);
            LabelDebugProvider debugProvider = LabelDebugProvider.get();
            assertFalse(debugProvider.isTrackingLabelUsage());
            debugProvider.setLabelHintMode("trace");
            debugProvider.setTrackingLabelUsage(true);
            debugProvider.setLabelHintRequest(true);


            final HumanLanguage ENGLISH_CA = LanguageProviderFactory.get().getLanguage(Locale.CANADA);
            GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(getDescriptor());
            GrammaticalLabelSet set = loader.getSet(ENGLISH_CA);

            // Note: LabelHints are done solely through the localizer, not the labelset.
            GrammaticalLocalizer localizer = new GrammaticalLocalizer(Locale.CANADA, Locale.CANADA, null, ENGLISH_CA, set);
            // You need a default provider factory to call getFilename
            @SuppressWarnings("deprecation")
            LocalizerProvider glf = new GrammaticalLocalizerFactory(GrammaticalLocalizerFactory.getLoader(getDescriptor(), null));
            LocalizerFactory.set(glf);

            assertEquals("click to create a new account now.[#0][#1]", localizer.getLabel("Sample", "click_here_to_create_new_account", "click"));
            assertFalse(localizer.labelExists("Sample", "invalid"));
            assertFalse(localizer.labelExists("invalid", "invalid"));
            try {
                localizer.getLabel(new LabelRef("invalid", "invalid", "invalid"));
                fail();
            } catch (SettingsSectionNotFoundException ex) {}
            assertEquals("__MISSING LABEL__ PropertyFile - val invalid not found in section Sample[#2]", localizer.getLabel(new LabelRef("Sample", "invalid", "invalid")));

            SetMultimap<String,String> used = debugProvider.getUsedLabels();
            assertEquals(3, used.size());
            assertTrue(used.containsEntry("Sample", "click_here_to_create_new_account"));
            assertTrue(used.containsEntry("invalid", "invalid"));

            List<LabelDebug> debugs = debugProvider.getLabelDebugs();
            assertEquals(3, debugs.size());
            assertEquals("Sample", debugs.get(0).getSection());
            assertEquals("click_here_to_create_new_account", debugs.get(0).getParameter());
            assertEquals("{0} to create a new account now.", debugs.get(0).getText());
            assertEquals("Sample", debugs.get(1).getSection());
            assertEquals("click_here_to_create_new_account", debugs.get(1).getParameter());

            assertTrue(debugs.get(1).getStack().contains("com.force.i18n.LabelDebugTest.testLabelDebug"));


            // The first one is before substitution, the second one is after
            Assert.assertNotEquals(debugs.get(0), debugs.get(1));
            Assert.assertNotEquals(debugs.get(0).hashCode(), debugs.get(1).hashCode());
            Assert.assertNotEquals(debugs.get(0).toString(), debugs.get(1).toString());

            // Now try with mask to visibly see if text leaks through
            debugProvider.setLabelHintMode("mask");
            assertEquals("################################", localizer.getLabel("Sample", "click_here_to_create_new_account", "click"));

        } finally {
            LabelDebugProvider.setLabelDebugProviderEnabled(false);
            LocalizerFactory.set(oldProvider);
        }

    }
}
