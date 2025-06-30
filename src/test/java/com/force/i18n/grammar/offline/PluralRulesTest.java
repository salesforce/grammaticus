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
