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
