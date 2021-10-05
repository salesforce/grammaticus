/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.icu.util.ULocale;

/**
 * @author stamm
 * @since 1.1
 */
public class TextDirectionTest {

	/**
	 * Validate that the TextDirection matches 
	 */
	@Test
	public void testTextDirectionCLDR() {
		for (ULocale locale : ULocale.getAvailableLocales()) {
			TextDirection fromCLDR = locale.getCharacterOrientation().contains("right-to-left") ? TextDirection.RTL : TextDirection.LTR;
			TextDirection fromCode = TextDirection.getDirection(locale.toLocale());
			
			if (locale.toString().equals("en_IL")) continue;  // This is used to support RTL pseudolocalization in english
			
			Assert.assertEquals( "TextDirection from CLDR doesn't match from JDK for " + locale, fromCLDR, fromCode);
		}
	}
	
	@Test
	public void testTokens() {
		Assert.assertEquals("left", TextDirection.LTR.getToken("left"));
		Assert.assertEquals("left", TextDirection.RTL.getToken("right"));
		Assert.assertEquals("Left", TextDirection.LTR.getToken("Left"));
		Assert.assertEquals("Left", TextDirection.RTL.getToken("Right"));
		Assert.assertEquals("right", TextDirection.RTL.getToken("left"));
		Assert.assertEquals("right", TextDirection.LTR.getToken("right"));
		Assert.assertEquals("Right", TextDirection.RTL.getToken("Left"));
		Assert.assertEquals("Right", TextDirection.LTR.getToken("Right"));
	}
	
	@Test
	public void testEmbeddingMarks() {
		Assert.assertNull(TextDirection.LTR.addEmbeddingMarks(null));
		Assert.assertEquals("Hi", TextDirection.LTR.makeStringLeftToRight("Hi"));
		Assert.assertEquals("\u202AHi\u202C", TextDirection.LTR.addEmbeddingMarks("Hi"));
		Assert.assertEquals("\u202BHi\u202C", TextDirection.RTL.addEmbeddingMarks("Hi"));
		Assert.assertEquals("\u202AHi\u202C", TextDirection.RTL.makeStringLeftToRight("Hi"));
	}

}
