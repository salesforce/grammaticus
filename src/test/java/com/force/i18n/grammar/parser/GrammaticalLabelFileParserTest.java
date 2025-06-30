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

import java.io.IOException;
import java.util.Set;

import org.junit.Assert;

import com.force.i18n.*;
import com.force.i18n.grammar.GrammaticalLabelSetImpl;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.parser.GrammaticalLabelFileParser.ErrorInfo;
import com.force.i18n.grammar.parser.GrammaticalLabelFileParser.ErrorType;

public class GrammaticalLabelFileParserTest extends BaseGrammaticalLabelTest {

    public GrammaticalLabelFileParserTest(String name) {
        super(name);
    }

    private Set<ErrorInfo> getParseError(HumanLanguage language, String testLabel) throws IOException {
        TestLanguageLabelSetDescriptor descriptor = new TestLanguageLabelSetDescriptor(getDescriptor(language),
                LabelUtils.getSampleLabelFile(testLabel), LabelUtils.getSampleGrammarFile(
                        "<noun name=\"Test\"><value plural=\"n\">Test</value><value plural=\"y\">Tests</value></noun>"));
        LanguageDictionary dict = new LanguageDictionaryParser(descriptor, descriptor.getLanguage(), null)
                .getDictionary();
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dict, descriptor, null, false);

        new GrammaticalLabelSetImpl(loadDictionary(language), parser);

        return parser.getInvalidLabels();
    }

    private ErrorInfo assertParseFail(HumanLanguage language, ErrorType expectedType, String testLabel) throws IOException {
        Set<ErrorInfo> errors = getParseError(language, testLabel);

        Assert.assertNotNull(errors);
        Assert.assertEquals(errors.size(), 1);

        ErrorInfo errorInfo = (ErrorInfo)errors.toArray()[0];
        Assert.assertEquals(errorInfo.type, expectedType);
        return errorInfo;
    }

    public void testParserError() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage("en_US");

        // noun does not exist
        assertParseFail(ENGLISH, ErrorType.UnknownEntity, "<MyTest/>");

        // recurring alias
        assertParseFail(ENGLISH, ErrorType.BadAlias,
                "dummy</param>\n"   // tweaking utility method. see LabelUtils.getSampleLabelFile
                + "<param name=\"val1\" alias=\"Test.val2\" />\n"
                + "<param name=\"val2\" alias=\"Test.val1\">");

        // wrong or missing "num" in <plural> tag
        assertParseFail(ENGLISH, ErrorType.BadPluralReference,
                "<plural><when val=\"one\">this will be ignored</when></plural>");
        assertParseFail(ENGLISH, ErrorType.BadPluralReference,
                "<plural num=\"xxx\"><when val=\"one\">this will be ignored</when></plural>");

        // duplicate "val" in <when> tag -- two "zero"s
        assertParseFail(ENGLISH, ErrorType.DuplicateWhen,
                "<plural num=\"0\">\n"
                + "  <when val=\"zero\">zero value</when>\n"
                + "  <when val=\"1\">one value</when>\n"
                + "  <when val=\"0\">zero value again</when>\n"
                + "</plural>");
        assertParseFail(ENGLISH, ErrorType.DuplicateWhen,
                "<plural num=\"0\">"
                + "  <when default=\"y\">default value</when>\n"
                +"   <when val=\"1\">one value</when>\n"
                +"   <when val=\"other\">default value again</when>\n"
                + "</plural>");

        // bad category value in <when> tag
        assertParseFail(ENGLISH, ErrorType.BadCategory,
                "<plural num=\"0\">"
                + "  <when default=\"yes\">bad default value</when>\n"
                + "</plural>");
        assertParseFail(ENGLISH, ErrorType.BadCategory,
                "<plural num=\"0\">"
                + "  <when val=\"xxx\">bad val value</when>\n"
                + "</plural>");
        }
    }
