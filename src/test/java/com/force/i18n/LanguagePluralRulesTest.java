/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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
