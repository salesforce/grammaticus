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

package com.force.i18n;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.force.i18n.LanguagePluralRules.NumberType;
import com.google.common.collect.ImmutableSet;
import com.ibm.icu.math.BigDecimal;


/**
 * Test the default ICU implementation.
 * @author stamm
 */
public class LanguagePluralRulesTest {

	private static LanguagePluralRules getRules(Locale locale) {
		HumanLanguage base = LanguageProviderFactory.get().getLanguageForLocale(locale);
		return LanguageProviderFactory.get().getPluralRules(base);
	}

	@Test
	public void testEnglish() {
		HumanLanguage base = LanguageProviderFactory.get().getLanguageForLocale(Locale.US);
		LanguagePluralRules rules = LanguageProviderFactory.get().getPluralRules(base);
		Assert.assertEquals(ImmutableSet.of(PluralCategory.ONE, PluralCategory.OTHER), rules.getSupportedCategories());
		Assert.assertEquals(ImmutableSet.of(PluralCategory.ONE, PluralCategory.OTHER), rules.getSupportedCategories(NumberType.CARDINAL));
		// 1st, 2nd, 3rd, Nth
		Assert.assertEquals(ImmutableSet.of(PluralCategory.ONE, PluralCategory.OTHER, PluralCategory.FEW, PluralCategory.TWO), rules.getSupportedCategories(NumberType.ORDINAL));

		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(BigDecimal.ZERO));
		Assert.assertEquals(PluralCategory.ONE, rules.getPluralCategory(BigDecimal.ONE));
		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(2));
		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(3));
		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(4));
		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(BigDecimal.TEN));

		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(BigDecimal.ZERO, NumberType.ORDINAL));
		Assert.assertEquals(PluralCategory.ONE, rules.getPluralCategory(BigDecimal.ONE, NumberType.ORDINAL));
		Assert.assertEquals(PluralCategory.TWO, rules.getPluralCategory(2, NumberType.ORDINAL));
		Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(3, NumberType.ORDINAL));
		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(4, NumberType.ORDINAL));
		Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(BigDecimal.TEN, NumberType.ORDINAL));
	}

	@Test
	public void testArabicVariants() {
		// Make sure all arabic is "the same"
		for (Locale locale : ImmutableSet.of(new Locale.Builder().setLanguage("ar").build(), new Locale.Builder().setLanguage("ar").setRegion("SA").build(), new Locale.Builder().setLanguage("ar").setRegion("TN").build(), new Locale.Builder().setLanguage("ar").setRegion("EG").build())) {
			LanguagePluralRules rules = getRules(locale);
			Assert.assertEquals(PluralCategory.ZERO, rules.getPluralCategory(BigDecimal.ZERO));
			Assert.assertEquals(PluralCategory.ONE, rules.getPluralCategory(BigDecimal.ONE));
			Assert.assertEquals(PluralCategory.TWO, rules.getPluralCategory(2));
			Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(3));
			Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(4));
			Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(BigDecimal.TEN));
			Assert.assertEquals(PluralCategory.MANY, rules.getPluralCategory(15.0));
			Assert.assertEquals(PluralCategory.OTHER, rules.getPluralCategory(101.0f));
		}
	}

	@Test
	public void testRussian() {
		LanguagePluralRules rules = getRules(new Locale.Builder().setLanguage("ru").build());
		Assert.assertEquals(PluralCategory.MANY, rules.getPluralCategory(BigDecimal.ZERO));
		Assert.assertEquals(PluralCategory.ONE, rules.getPluralCategory(BigDecimal.ONE));
		Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(2));
		Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(3));
		Assert.assertEquals(PluralCategory.FEW, rules.getPluralCategory(4));
		Assert.assertEquals(PluralCategory.MANY, rules.getPluralCategory(BigDecimal.TEN));
		Assert.assertEquals(PluralCategory.MANY, rules.getPluralCategory(11));
		Assert.assertEquals(PluralCategory.MANY, rules.getPluralCategory(15.0));
		Assert.assertEquals(PluralCategory.ONE, rules.getPluralCategory(101.0f));
	}

}
