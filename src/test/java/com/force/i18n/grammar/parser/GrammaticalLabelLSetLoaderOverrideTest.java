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
