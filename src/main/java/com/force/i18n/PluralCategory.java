/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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
