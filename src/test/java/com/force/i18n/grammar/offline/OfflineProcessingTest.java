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

package com.force.i18n.grammar.offline;

import java.util.*;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest;
import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest.MockRenamingProvider;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;
import com.google.common.collect.ImmutableList;


/**
 * Test offline processing of labels.
 * @author stamm
 * @since 1.1
 */
@RunWith(Parameterized.class)
public class OfflineProcessingTest {
	@Parameters(name="{0}")
	public static Collection<Object[]> data() {
		//Set<Locale> locales = ImmutableSet.of(new Locale.Builder().setLanguage("fr").build());
		return LanguageProviderFactory.get().getAll().stream().filter(a->a.isTranslatedLanguage())
		        //.filter(a->locales.contains(a.getLocale()))
				.map(a->new Object[]{a}).collect(Collectors.toList());
	}

	@Parameter
	public HumanLanguage language;

	@Test
	public void simpleSerialization() throws Exception {
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(BaseGrammaticalLabelTest.getSampleDescriptor(language));
        GrammaticalLabelSet set = loader.getSet(language);
        LanguageDictionary dict = set.getDictionary();
        if (dict == null) return;  // Ignore if there's no sample data

        StringBuilder sb = new StringBuilder("var labels=");
        Set<GrammaticalTerm> termsInUse = new HashSet<>();
        set.writeJson(sb, Collections.singleton("Sample.click_here_to_create_new_account"), termsInUse);
        sb.append(";\nvar terms=");
        dict.writeJson(sb, false, termsInUse.stream().map(a->a.getName()).collect(Collectors.toList()));
        ScriptEngine engine = JsTestUtils.getScriptEngine();
        engine.eval(sb.toString());

        // make sure dict will run with null terms. tests for contents TBD
        dict.writeJson(new StringBuilder(), false, null);
	}


	@Test
	public void compareRenameable() throws Exception {
        GrammaticalLabelSetDescriptor descriptor = BaseGrammaticalLabelTest.getSampleDescriptor(language);
        Collection<LabelReference> labels = ImmutableList.of(
        		new LabelRef("Sample", "click_here_to_create_new_account"),
        		new LabelRef("Sample", "openAnAccount")
		);

        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(BaseGrammaticalLabelTest.getSampleDescriptor(language));
        GrammaticalLabelSet set = loader.getSet(language);
        LanguageDictionary dict = set.getDictionary();

        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            Noun accountNoun = dict.getNoun("account", false);
            Noun leadNoun = dict.getNoun("lead", false);
            Noun renamedNoun = accountNoun.clone(leadNoun.getGender(), leadNoun.getStartsWith(), leadNoun.getAllDefinedValues());

            MockRenamingProvider newProvider = new MockRenamingProvider(renamedNoun);
            RenamingProviderFactory.get().setProvider(newProvider);
            JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[0]);
            // Turn off renamed nouns temporarily
            newProvider.setUseRenamedNouns(false);
            JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[0]);
        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }


	}


	@Test
	public void compareAddProduct() throws Exception {
        GrammaticalLabelSetDescriptor descriptor = BaseGrammaticalLabelTest.getSampleDescriptorDir(language);
        Collection<LabelReference> labels = ImmutableList.of(
        		new LabelRef("Buttons", "add_product"),
        		new LabelRef("Sample", "openAnAccount")
		);
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[0]);
	}


	@Test
	public void compareNewEntity() throws Exception {
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(BaseGrammaticalLabelTest.getSampleDescriptor(language));
        GrammaticalLabelSet set = loader.getSet(language);
        LanguageDictionary dict = set.getDictionary();


        GrammaticalLabelSetDescriptor descriptor = BaseGrammaticalLabelTest.getSampleDescriptorDir(language);
        Collection<LabelReference> labels = ImmutableList.of(
        		new LabelRef("Buttons", "new_entity"),
        		new LabelRef("Sample", "openAnAEntity")
		);
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[] {new BaseGrammaticalLabelTest.MockExistingRenameable("Account", dict)});
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[] {new BaseGrammaticalLabelTest.MockExistingRenameable("Opportunity", dict)});
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[] {new BaseGrammaticalLabelTest.MockExistingRenameable("Quote", dict)});

	}

    @Test
    public void comparePlural() throws Exception {
        GrammaticalLabelSetDescriptor descriptor = BaseGrammaticalLabelTest.getSampleDescriptorDir(language);
        Collection<LabelReference> labels = ImmutableList.of(
                new LabelRef("Sample", "num_records", 0),
                new LabelRef("Sample", "num_records", 1),
                new LabelRef("Sample", "num_records", 2),
                new LabelRef("Sample", "num_records_zero", 0),
                new LabelRef("Sample", "num_records_zero", 1),
                new LabelRef("Sample", "num_records_zero", 2)
        );
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[0]);
    }

    @Test
    public void comparePluralEntity() throws Exception {
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(BaseGrammaticalLabelTest.getSampleDescriptor(language));
        GrammaticalLabelSet set = loader.getSet(language);
        LanguageDictionary dict = set.getDictionary();

        GrammaticalLabelSetDescriptor descriptor = BaseGrammaticalLabelTest.getSampleDescriptorDir(language);
        Collection<LabelReference> labels = ImmutableList.of(
                new LabelRef("Sample", "num_records_entity", 0),
                new LabelRef("Sample", "num_records_entity", 1),
                new LabelRef("Sample", "num_records_entity", 2),
                new LabelRef("Sample", "num_records_zero_entity", 0),
                new LabelRef("Sample", "num_records_zero_entity", 1),
                new LabelRef("Sample", "num_records_zero_entity", 2)
        );
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[] {new BaseGrammaticalLabelTest.MockExistingRenameable("Account", dict)});
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[] {new BaseGrammaticalLabelTest.MockExistingRenameable("Opportunity", dict)});
        JsTestUtils.compareResults(this.language, descriptor, labels, new Renameable[] {new BaseGrammaticalLabelTest.MockExistingRenameable("Quote", dict)});

    }
}
