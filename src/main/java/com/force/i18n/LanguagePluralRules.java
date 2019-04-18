/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n;

import java.util.Set;

/**
 * Provides an abstraction for the plural rules for a human language.  The default implementation
 * uses the optional dependency ICU4J, but that is not required and can be overridden in the 
 * LanguageProviderFactory.  This will allow new languages to customize support
 * 
 * @author stamm
 * @since 1.1
 */
public interface LanguagePluralRules {
	/**
	 * @return the human language associated with these rules
	 */
	HumanLanguage getHumanLanguage();

	/**
	 * @param value the number to check.  If null, the value <tt>0</tt> will be used
	 * @return the PluralCategory to use for the given number as a cardinal number
	 */
	default PluralCategory getPluralCategory(Number value) {
		return getPluralCategory(value, NumberType.CARDINAL);
	}

	/**
	 * 
	 * @param value
	 * @param numberType
	 * @return
	 */
	PluralCategory getPluralCategory(Number value, NumberType numberType);

	/**
	 * @return the set of categories for cardinal numbers
	 */
	default Set<PluralCategory> getSupportedCategories() {
		return getSupportedCategories(NumberType.CARDINAL);
	}

	/**
	 * @param numberType the number of type to look for, cardinal or ordinal
	 * @return the set of categories for the given number type.
	 */
	Set<PluralCategory> getSupportedCategories(NumberType numberType);
	
	/**
	 * Linguistic type of number, whether used for counting or ordering (i.e. 1 item vs 2 items
	 * for cardinal or 1st or 2nd for ordinal)
	 */
	enum NumberType {
		CARDINAL,
		ORDINAL
	}
}
