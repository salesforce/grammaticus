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

import java.util.Set;

/**
 * Provides an abstraction for the plural rules for a human language.  The default implementation
 * uses the optional dependency ICU4J, but that is not required and can be overridden in the
 * LanguageProviderFactory.  This will allow new languages to customize support
 *
 * @author stamm
 * @since 0.6.0
 */
public interface LanguagePluralRules {
	/**
	 * @return the human language associated with these rules
	 */
	HumanLanguage getHumanLanguage();

	/**
	 * @param value the number to check.  If null, the value {@code 0} will be used
	 * @return the PluralCategory to use for the given number as a cardinal number
	 */
	default PluralCategory getPluralCategory(Number value) {
		return getPluralCategory(value, NumberType.CARDINAL);
	}

	/**
	 * @param value  the number to test
	 * @param numberType Cardinal or Ordinal number
	 * @return the plural category for the number
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
