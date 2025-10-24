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

package com.force.i18n.grammar.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.force.i18n.*;

class GrammaticalLabelFileHandlerTest {

    @Nested
    @DisplayName("Parsing with BaseGrammaticalLabelTest helper")
    class ParsingWithHelper {
        // using BaseGrammaticalLabelTest as a helper instance
        private final BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("helper") {};

        @BeforeEach
        void initHelper() throws Exception {
            helper.setUp();
        }


        @Test
        void entityCapitalizationAndPluralAlias() throws Exception {
            final HumanLanguage english = LanguageProviderFactory.get().getLanguage("en_US");
            helper.assertValue(english, "Open <Account/>", "Open Account");
            helper.assertValue(english, "open <account/>", "open account");
            helper.assertValue(english, "See <documents/>", "See documents");
        }

        @Test
        void articleAndAdjectiveFixups() throws Exception {
            final HumanLanguage english = LanguageProviderFactory.get().getLanguage("en_US");
            helper.assertValue(english, "<a/> <new entity=\"Account\"/> <account/>", "a new account");
            helper.assertValue(english, "<the/> <open entity=\"Account\"/> <account/>", "the open account");
            helper.assertValue(english, "<the/> <new entity=\"Account\"/> <open entity=\"Account\"/> <account/>", "the new open account");
        }

        @Test
        void escapeHtmlAttributeAccepted() throws Exception {
            final HumanLanguage english = LanguageProviderFactory.get().getLanguage("en_US");
            helper.assertValue(english, "<account escapeHtml=\"y\"/>", "account");
            helper.assertValue(english, "<account escapeHtml=\"true\"/>", "account");
        }

        @Test
        void badTagTolerance() throws Exception {
            final HumanLanguage english = LanguageProviderFactory.get().getLanguage("en_US");
            helper.assertValue(english, "Hello <unknown/> world", "Hello  world");
        }
    }

    @Test
    void convertsPlainTextUnchanged() {
        assertEquals("hello", GrammaticalLabelFileHandler.convertEscapedToUnicode("hello"));
        assertEquals(" ", GrammaticalLabelFileHandler.convertEscapedToUnicode(" "));
    }

    @Test
    void convertsCommonEscapes() {
        assertEquals("\t", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\t"));
        assertEquals("\n", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\n"));
        assertEquals("\r", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\r"));
        assertEquals("\f", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\f"));
        assertEquals("a\tb\nc\rd\fe", GrammaticalLabelFileHandler.convertEscapedToUnicode("a\\tb\\nc\\rd\\fe"));
    }

    @Test
    void unknownEscapePassesThroughChar() {
        // Unknown escape like \x should become just 'x'
        assertEquals("x", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\x"));
        assertEquals("A", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\A"));
        assertEquals("/", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\/"));
        assertEquals("\\", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\\\"));
    }

    @Test
    void trailingBackslashIsPreserved() {
        assertEquals("abc\\", GrammaticalLabelFileHandler.convertEscapedToUnicode("abc\\"));
    }

    @Test
    void convertsUnicodeEscapes() {
        // Basic Latin
        assertEquals("A", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\u0041"));
        // Latin-1 Supplement
        assertEquals("ñ", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\u00F1"));
        // Lowercase hex digits
        assertEquals("ñ", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\u00f1"));
        // Greek small alpha
        assertEquals("α", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\u03B1"));
        // Mix of text and escapes
        assertEquals("Hi α!", GrammaticalLabelFileHandler.convertEscapedToUnicode("Hi \\u03B1!"));
        // Multiple consecutive sequences
        assertEquals("ABC", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\u0041\\u0042\\u0043"));
    }

    @Test
    void malformedUnicodeThrows() {
        // Too short after \\u
        assertThrows(IllegalArgumentException.class, () -> GrammaticalLabelFileHandler.convertEscapedToUnicode("\\u12"));
        // Non-hex
        assertThrows(IllegalArgumentException.class, () -> GrammaticalLabelFileHandler.convertEscapedToUnicode("\\uZZZZ"));
        // Index at end ("\\u")
        assertThrows(IllegalArgumentException.class, () -> GrammaticalLabelFileHandler.convertEscapedToUnicode("end \\u"));
        // Not enough characters remaining for 4 hex digits
        assertThrows(IllegalArgumentException.class, () -> GrammaticalLabelFileHandler.convertEscapedToUnicode("X \\u0"));
    }

    @Test
    void backslashBeforeNormalChar() {
        // Backslash then a normal char should yield the char
        assertEquals("-a-", GrammaticalLabelFileHandler.convertEscapedToUnicode("-\\a-"));
        assertEquals("-_-", GrammaticalLabelFileHandler.convertEscapedToUnicode("-\\_-"));
    }

    @Test
    void surrogatePairEscapesHandled() {
        // High surrogate D83D and low surrogate DE00 -> 😀
        assertEquals("😀", GrammaticalLabelFileHandler.convertEscapedToUnicode("\\uD83D\\uDE00"));
        // In a sentence
        assertEquals("smile 😀 end", GrammaticalLabelFileHandler.convertEscapedToUnicode("smile \\uD83D\\uDE00 end"));
    }
}
