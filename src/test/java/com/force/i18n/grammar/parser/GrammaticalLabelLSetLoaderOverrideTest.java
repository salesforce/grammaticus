/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.util.Locale;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.LanguageDictionary;

/**
 * Test for overriding LabelSetLoader to create another dictionary
 *
 */
public class GrammaticalLabelLSetLoaderOverrideTest extends BaseGrammaticalLabelTest{

    public GrammaticalLabelLSetLoaderOverrideTest(String name) {
        super(name);
    }

    /**
     * Loader for test
     */
    public static class TestLoader extends GrammaticalLabelSetLoader {
        public TestLoader(GrammaticalLabelSetDescriptor dictDesc) {
            super(dictDesc);
        }

        @Override
        protected LanguageDictionary createNewDictionary(HumanLanguage language) throws IOException {
            return new TestInMemoryDic(language);
        }
        @Override
        protected LanguageDictionary finalizeDictionary(LanguageDictionary dictionary) throws IOException {
            assertTrue(dictionary instanceof TestInMemoryDic);
            return new TestFinalizedDic(dictionary.getLanguage());
        }
    }
    /**
     * Teset for overriding LabelSetLoader to create another dictionary specified in TestLoader
     *
     * @throws Exception
     */
    public void testOverride() throws Exception {
        GrammaticalLabelSetLoader loader = new TestLoader(getDescriptor());
        HumanLanguage ENGLISH_CA = LanguageProviderFactory.get().getLanguage(Locale.CANADA);
        assertTrue (loader.getSet(ENGLISH_CA).getDictionary() instanceof TestFinalizedDic);
    }


    public static class TestInMemoryDic extends LanguageDictionary {
        public TestInMemoryDic(HumanLanguage language) {
            super(language);
        }
    }
    public static class TestFinalizedDic extends LanguageDictionary {
        public TestFinalizedDic(HumanLanguage language) {
            super(language);
        }

    }

}
