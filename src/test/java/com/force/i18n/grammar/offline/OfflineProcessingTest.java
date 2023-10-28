/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.offline;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LabelRef;
import com.force.i18n.LabelReference;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.RenamingProvider;
import com.force.i18n.grammar.RenamingProviderFactory;
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
		//Set<Locale> locales = ImmutableSet.of(new Locale("fr"));
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
