/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.offline;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;
import com.google.common.collect.ImmutableSet;


/**
 * Test of the json serialization of labels, term, etc across different language
 * @author stamm
 * @since 1.1
 */
public class JsonSerializationTest extends BaseGrammaticalLabelTest {


	public JsonSerializationTest(String name) {
		super(name);
	}

	public void _testLabelSerialization(Locale languageLocale, String label, String terms) throws Exception {
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(getDescriptor());
        HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage(languageLocale);
        GrammaticalLabelSet set = loader.getSet(GERMAN);
        LanguageDictionary dict = set.getDictionary();

        StringBuilder sb = new StringBuilder();

        
        set.writeJson(sb, Collections.singleton("Sample.click_here_to_create_new_account"), null);
        Assert.assertEquals(label, sb.toString());

        //
        Set<GrammaticalTerm> termsInUse = new TreeSet<>();
        set.writeJson(sb, Collections.singleton("Sample.click_here_to_create_new_account"), termsInUse);
        Set<? extends GrammaticalTerm> expectedTerms;
        if (dict.getDeclension().hasArticle()) {
        	expectedTerms = ImmutableSet.of(dict.getNoun("account", false), dict.getArticle("a"), dict.getAdjective("new"));
        } else {
        	// Assume korean
        	expectedTerms = ImmutableSet.of(dict.getNoun("account", false), dict.getAdjective("eul"), dict.getAdjective("new"));
        }
        Assert.assertEquals("Terms did not match", expectedTerms, termsInUse);

        sb = new StringBuilder();
        dict.writeJson(sb, false, termsInUse.stream().map(a->a.getName()).collect(Collectors.toList()));
        Assert.assertEquals(terms, sb.toString());
        
	}
	
	@Test
	public void testLabelSerializationDe() throws Exception {
		_testLabelSerialization(Locale.GERMAN,
				"{\"Sample.click_here_to_create_new_account\":[\"{0}, um jetzt \",{\"t\":\"d\",\"l\":\"a\",\"f\":\"m-0-a-c\",\"c\":false,\"an\":5,\"nt\":3},\" \",{\"t\":\"a\",\"l\":\"new\",\"f\":\"m-0-a-n\",\"c\":false,\"an\":5,\"nt\":5},\" \",{\"t\":\"n\",\"l\":\"account\",\"f\":\"0-a\",\"c\":false},\" zu erstellen.\"]}",

				"{\"n\":{\"account\":{\"t\":\"n\",\"l\":\"account\",\"g\":\"m\",\"v\":{\"0-n\":\"Account\",\"0-a\":\"Account\",\"0-g\":\"Accounts\",\"0-d\":\"Account\",\"1-n\":\"Accounts\",\"1-a\":\"Accounts\",\"1-g\":\"Accounts\",\"1-d\":\"Accounts\"}}},\"a\":{\"new\":{\"t\":\"a\",\"l\":\"new\",\"v\":{\"n-0-n-n\":\"Neues\",\"n-0-a-n\":\"Neues\",\"n-0-g-n\":\"Neuen\",\"n-0-d-n\":\"neuem\",\"n-0-n-d\":\"Neue\",\"n-0-a-d\":\"Neue\",\"n-0-g-d\":\"Neuen\",\"n-0-d-d\":\"neuen\",\"f-0-n-n\":\"Neue\",\"f-0-a-n\":\"Neue\",\"f-0-g-n\":\"Neuen\",\"f-0-d-n\":\"neuer\",\"f-0-n-d\":\"Neue\",\"f-0-a-d\":\"Neue\",\"f-0-g-d\":\"Neuen\",\"f-0-d-d\":\"neuen\",\"m-0-n-n\":\"Neuer\",\"m-0-a-n\":\"Neuen\",\"m-0-g-n\":\"Neuen\",\"m-0-d-n\":\"neuem\",\"m-0-n-d\":\"Neue\",\"m-0-a-d\":\"Neuen\",\"m-0-g-d\":\"Neuen\",\"m-0-d-d\":\"neuen\",\"n-1-n-n\":\"Neue\",\"n-1-a-n\":\"Neuen\",\"n-1-g-n\":\"Neuen\",\"n-1-d-n\":\"neuen\",\"n-1-n-d\":\"Neuen\",\"n-1-a-d\":\"Neuen\",\"n-1-g-d\":\"Neuen\",\"n-1-d-d\":\"neuen\",\"f-1-n-n\":\"Neue\",\"f-1-a-n\":\"Neuen\",\"f-1-g-n\":\"Neuen\",\"f-1-d-n\":\"neuen\",\"f-1-n-d\":\"Neuen\",\"f-1-a-d\":\"Neuen\",\"f-1-g-d\":\"Neuen\",\"f-1-d-d\":\"neuen\",\"m-1-n-n\":\"Neue\",\"m-1-a-n\":\"Neuen\",\"m-1-g-n\":\"Neuen\",\"m-1-d-n\":\"neuen\",\"m-1-n-d\":\"Neuen\",\"m-1-a-d\":\"Neuen\",\"m-1-g-d\":\"Neuen\",\"m-1-d-d\":\"neuen\"}}},\"d\":{\"a\":{\"t\":\"d\",\"l\":\"a\",\"v\":{\"n-0-n-c\":\"Ein\",\"n-0-a-c\":\"Ein\",\"n-0-g-c\":\"Eines\",\"n-0-d-c\":\"Einem\",\"f-0-n-c\":\"Eine\",\"f-0-a-c\":\"Eine\",\"f-0-g-c\":\"Einer\",\"f-0-d-c\":\"Einer\",\"m-0-n-c\":\"Ein\",\"m-0-a-c\":\"Einen\",\"m-0-g-c\":\"Eines\",\"m-0-d-c\":\"Einem\",\"n-1-n-c\":\"Ein\",\"n-1-a-c\":\"Ein\",\"n-1-g-c\":\"Ein\",\"n-1-d-c\":\"Ein\",\"f-1-n-c\":\"Ein\",\"f-1-a-c\":\"Ein\",\"f-1-g-c\":\"Ein\",\"f-1-d-c\":\"Ein\",\"m-1-n-c\":\"Ein\",\"m-1-a-c\":\"Ein\",\"m-1-g-c\":\"Ein\",\"m-1-d-c\":\"Ein\"}}}}"
        		 );
        
	}
	
	@Test
	public void testLabelSerializationEn() throws Exception {
		_testLabelSerialization(Locale.US,
				"{\"Sample.click_here_to_create_new_account\":[\"{0} to create \",{\"t\":\"d\",\"l\":\"a\",\"f\":\"0-c\",\"c\":false,\"an\":5,\"nt\":3},\" \",{\"t\":\"a\",\"l\":\"new\",\"f\":\"0\",\"c\":false,\"an\":5,\"nt\":5},\" \",{\"t\":\"n\",\"l\":\"account\",\"f\":\"0\",\"c\":false},\" now.\"]}",

				"{\"n\":{\"account\":{\"t\":\"n\",\"l\":\"account\",\"s\":\"v\",\"v\":{\"0\":\"Account\",\"1\":\"Accounts\"}}},\"a\":{\"new\":{\"t\":\"a\",\"l\":\"new\",\"s\":\"c\",\"v\":{\"0\":\"New\"}}},\"d\":{\"a\":{\"t\":\"d\",\"l\":\"a\",\"s\":\"c\",\"v\":{\"0-c\":\"A\",\"0-v\":\"An\",\"1-c\":\"A\"}}}}"
				);
	}
	
	@Test
	public void testLabelSerializationKo() throws Exception {
		_testLabelSerialization(new Locale("ko"),
				"{\"Sample.click_here_to_create_new_account\":[\"{0} 지금 \",{\"t\":\"a\",\"l\":\"new\",\"f\":\"c\",\"c\":false,\"an\":3,\"nt\":3},\" \",{\"t\":\"n\",\"l\":\"account\",\"f\":\"s\",\"c\":false},{\"t\":\"a\",\"l\":\"eul\",\"f\":\"c\",\"c\":false,\"an\":3,\"nt\":3},\" 작성합니다.\"]}",
				
				"{\"n\":{\"account\":{\"t\":\"n\",\"l\":\"account\",\"s\":\"c\",\"v\":{\"s\":\"계정\"}}},\"a\":{\"eul\":{\"t\":\"a\",\"l\":\"eul\",\"v\":{\"c\":\"을\",\"v\":\"를\",\"s\":\"을\"}},\"new\":{\"t\":\"a\",\"l\":\"new\",\"v\":{\"c\":\"새\",\"v\":\"새\",\"s\":\"새\"}}}}");
	}
}
