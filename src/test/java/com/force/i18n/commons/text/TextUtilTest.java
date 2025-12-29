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

package com.force.i18n.commons.text;

import junit.framework.TestCase;

/**
 * Tests for TextUtil class
 */
public class TextUtilTest extends TestCase {
    
    public TextUtilTest(String name) {
        super(name);
    }

    // Tests for escapeForMessageFormat(String str)
    
    public void testEscapeForMessageFormat_NullInput() {
        // Test that null input returns null without throwing NPE
        assertNull(TextUtil.escapeForMessageFormat(null));
    }
    
    public void testEscapeForMessageFormat_EmptyString() {
        // Test that empty string returns empty string
        assertEquals("", TextUtil.escapeForMessageFormat(""));
    }
    
    public void testEscapeForMessageFormat_SimpleString() {
        // Test simple string without special characters
        assertEquals("Hello World", TextUtil.escapeForMessageFormat("Hello World"));
    }
    
    public void testEscapeForMessageFormat_StringWithoutBraces() {
        // Test string with single quotes but no braces (should not be escaped)
        assertEquals("It's working", TextUtil.escapeForMessageFormat("It's working"));
    }
    
    public void testEscapeForMessageFormat_StringWithMessageFormatParam() {
        // Test string with valid message format parameter
        assertEquals("Hello {0}", TextUtil.escapeForMessageFormat("Hello {0}"));
        assertEquals("Hello {0} and {1}", TextUtil.escapeForMessageFormat("Hello {0} and {1}"));
    }
    
    public void testEscapeForMessageFormat_StringWithInvalidBraces() {
        // Test string with braces that should be escaped
        assertEquals("Hello '{'}", TextUtil.escapeForMessageFormat("Hello {}"));
        assertEquals("Hello '{'world}", TextUtil.escapeForMessageFormat("Hello {world}"));
    }
    
    public void testEscapeForMessageFormat_StringWithSingleQuotes() {
        // Test string with single quotes and message format params
        assertEquals("It''s {0}", TextUtil.escapeForMessageFormat("It's {0}"));
    }
    
    // Tests for escapeForMessageFormat(String src, StringBuilder sb, boolean check)
    
    public void testEscapeForMessageFormatWithStringBuilder_NullInput() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = TextUtil.escapeForMessageFormat(null, sb, true);
        assertEquals("", sb.toString());
        assertSame(sb, result);
    }
    
    public void testEscapeForMessageFormatWithStringBuilder_EmptyString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = TextUtil.escapeForMessageFormat("", sb, true);
        assertEquals("", sb.toString());
        assertSame(sb, result);
    }
    
    public void testEscapeForMessageFormatWithStringBuilder_ExistingContent() {
        // Test that existing StringBuilder content is preserved
        StringBuilder sb = new StringBuilder("Prefix: ");
        StringBuilder result = TextUtil.escapeForMessageFormat("Hello {0}", sb, true);
        assertEquals("Prefix: Hello {0}", sb.toString());
        assertSame(sb, result);
    }
    
    public void testEscapeForMessageFormatWithStringBuilder_CheckFalse() {
        // Test with check=false, should always process escaping
        StringBuilder sb = new StringBuilder();
        TextUtil.escapeForMessageFormat("Hello World", sb, false);
        assertEquals("Hello World", sb.toString());
        
        // Even without braces, single quotes should be escaped when check=false
        sb = new StringBuilder();
        TextUtil.escapeForMessageFormat("It's working", sb, false);
        assertEquals("It''s working", sb.toString());
    }
    
    public void testEscapeForMessageFormatWithStringBuilder_NullInputWithExistingContent() {
        // Test that null input doesn't overwrite existing StringBuilder content
        StringBuilder sb = new StringBuilder("Existing");
        TextUtil.escapeForMessageFormat(null, sb, true);
        assertEquals("Existing", sb.toString());
    }
    
    public void testEscapeForMessageFormatWithStringBuilder_ComplexFormats() {
        // Test complex message format patterns
        StringBuilder sb = new StringBuilder();
        TextUtil.escapeForMessageFormat("Value: {0, number}", sb, false);
        assertEquals("Value: {0, number}", sb.toString());
        
        sb = new StringBuilder();
        TextUtil.escapeForMessageFormat("Value: {0, number, integer}", sb, false);
        assertEquals("Value: {0, number, integer}", sb.toString());
    }
    
    public void testEscapeForMessageFormatWithStringBuilder_EdgeCases() {
        // Test edge cases
        StringBuilder sb = new StringBuilder();
        
        // Lone opening brace at end
        TextUtil.escapeForMessageFormat("Hello {", sb, false);
        assertEquals("Hello '{'", sb.toString());
        
        // Multiple params
        sb = new StringBuilder();
        TextUtil.escapeForMessageFormat("{0} meets {1} at {2}", sb, true);
        assertEquals("{0} meets {1} at {2}", sb.toString());
        
        // Mixed valid and invalid braces
        sb = new StringBuilder();
        TextUtil.escapeForMessageFormat("{0} and {} and {1}", sb, false);
        assertEquals("{0} and '{'} and {1}", sb.toString());
    }
    
    public void testEscapeForMessageFormat_RealWorldScenarios() {
        // Test scenarios that would have caused NPE before the fix
        
        // Scenario from the stack trace: null string from RefTag.toString()
        String nullString = null;
        assertNull(TextUtil.escapeForMessageFormat(nullString));
        
        // Test with StringBuilder for null handling
        StringBuilder sb = new StringBuilder("Label: ");
        TextUtil.escapeForMessageFormat(null, sb, false);
        assertEquals("Label: ", sb.toString());
        
        // Test message format with single quotes that need escaping
        assertEquals("Don''t forget {0}", TextUtil.escapeForMessageFormat("Don't forget {0}"));
        
        // Test nested braces (invalid format that should be escaped)
        assertEquals("Value: '{'{0}}", TextUtil.escapeForMessageFormat("Value: {{0}}"));
        
        // Test format with choice format
        assertEquals("There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}",
                     TextUtil.escapeForMessageFormat("There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}"));
    }
    
    public void testEscapeForMessageFormat_SpecialCases() {
        // Test various special cases
        
        // Only opening brace
        assertEquals("Test '{'", TextUtil.escapeForMessageFormat("Test {"));
        
        // Only closing brace (should not be escaped)
        assertEquals("Test }", TextUtil.escapeForMessageFormat("Test }"));
        
        // Multiple single quotes (already escaped quotes are preserved)
        assertEquals("It''s working", TextUtil.escapeForMessageFormat("It''s working"));
        
        // Mixed quotes and braces
        assertEquals("Say ''hello'' to {0}", TextUtil.escapeForMessageFormat("Say 'hello' to {0}"));
        
        // Very long string handling (just to ensure no issues with StringBuilder sizing)
        String longString = "a".repeat(1000) + " {0} " + "b".repeat(1000);
        String result = TextUtil.escapeForMessageFormat(longString);
        assertTrue(result.contains("{0}"));
        assertEquals(2005, result.length()); // 1000 + 5 (" {0} ") + 1000 = 2005 chars
    }
}
