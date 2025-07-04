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

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Representation of the Plural Category as used in CLDR.  Each language needs to define how to manage
 * it.
 * @author stamm
 * @see <a href="http://www.unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html">CLDR Plural Rules</a>
 */
public enum PluralCategory {
	ZERO("0"),
	ONE("1"),
	TWO("2"),
	FEW("f"),
	MANY("m"),
	OTHER("n");

	private final String category;
	private final String alias;
	PluralCategory(String alias) {
		this.category = this.name().toLowerCase();
		this.alias = alias;
	}
	public String getCldrCategory() {
		return this.category;
	}
	public String getAlias() {
		return this.alias;
	}


	private static final Map<String,PluralCategory> BY_NAME;
	private static final Map<String,PluralCategory> BY_LABEL;
	static {
		// Create an immutable map by category
		BY_NAME = Maps.uniqueIndex(Arrays.asList(values()), a->a.getCldrCategory());
		BY_LABEL = ImmutableMap.<String,PluralCategory>builder().putAll(BY_NAME).putAll(Maps.uniqueIndex(Arrays.asList(values()), a->a.getAlias())).build();
    }
	/**
	 * @param category the CLDR name of the plural category
	 * @return the category corresponding to the CLDR name, or null if not specified
	 */
    public static PluralCategory fromCategory(String category) {
    	return BY_NAME.get(category);
    }
	/**
	 * @param category the CLDR name of the plural category, or the one character name for simplicity (n = other, f = few, m = many)
	 * @return the category corresponding to the CLDR name, or null if not specified
	 */
    public static PluralCategory fromLabel(String category) {
    	return BY_LABEL.get(category);
    }


}
