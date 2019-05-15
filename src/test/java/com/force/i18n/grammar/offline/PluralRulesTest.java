/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.offline;

import java.util.*;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.force.i18n.*;
import com.ibm.icu.text.PluralRules;


/**
 * Test offline processing of labels.
 * @author stamm
 * @since 1.1
 */
@RunWith(Parameterized.class)
public class PluralRulesTest {
	@Parameters(name="{0}")
	public static Collection<Object[]> data() {
		// Do all languages.
		return LanguageProviderFactory.get().getAll().stream()
				.map(a->new Object[]{a}).collect(Collectors.toList());
	}
	
	@Parameter
	public HumanLanguage language;
	
	@Test
	public void testPluralRules() throws Exception {
	    PluralRules rules = PluralRules.forLocale(language.getLocale());
	    StringBuilder function = new StringBuilder();
	    function.append("var select = ");
	    function.append(PluralRulesJsImpl.getSelectFunction(language.getLocale()));
	    function.append(";");
        ScriptEngine engine = JsTestUtils.getScriptEngine();
        engine.eval(function.toString());
        String[] result = (String[]) engine.eval("var result = [];  for (var i = -0; i < 200; i+=0.5) {result.push(select(i));}; Java.to(result,'java.lang.String[]');");
        List<String> expected = new ArrayList<>(result.length);
        for (double i = 0; i < 200; i+= 0.5) {
            expected.add(rules.select(i));
        }
        //for (String keyword : rules.getKeywords()) {
        //    System.out.println(language.getLocale().getLanguage() + " " + keyword + " " + rules.getRules(keyword));
        //}
        Assert.assertArrayEquals(expected.toArray(new String[expected.size()]), result);
	}
	

	
}
