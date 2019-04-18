/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.parser;

import java.io.IOException;

import com.force.i18n.*;
import com.force.i18n.grammar.Noun;

/**
 * Test of classifiers in various languages that support classifier words.
 *
 * @author stamm
 * @since 1.1
 */
public class ClassifierLabelTest extends BaseGrammaticalLabelTest {
    public ClassifierLabelTest(String name) {
        super(name);
    }

    public void assertValue(HumanLanguage language, String label, String grammarSnippet, Renameable[] entities, String expectedResult, Object... vals) throws IOException {
        String result = getValue(language, label, entities, grammarSnippet, vals);
        assertEquals("Mismatch for " + label, expectedResult, result);
    }

    /**
     * Mock Renameable for use with <Entity>
     */
    public class MockTestRenameable extends MockRenameable {
        public MockTestRenameable(String name) {
            super(name);
        }
        @Override
        public String getLabel() {
            return "";
        }
        @Override
        public String getLabelPlural() {
            return "";
        }
        @Override
        public String getStandardFieldLabel(HumanLanguage language, StandardField field) {
            return "";
        }
        @Override
        public Noun getStandardNoun(HumanLanguage language) {
            return null;
        }
    }

    public void testJapaneseCounterWord() throws Exception {
        final HumanLanguage JAPANESE = LanguageProviderFactory.get().getLanguage("ja");
        // Accounts are counted using つ
        String label = "<Entity entity=\"0\"/>が{0}<counter/>あります";
        assertValue(JAPANESE, label, null, new Renameable[] {new MockTestRenameable("account")}, "取引先が1つあります", "1");
        // Opportunity uses 社 (parsed from the label files)
        assertValue(JAPANESE, label, null, new Renameable[] {new MockTestRenameable("opportunity")}, "商談が1社あります", "1");

        // Students are counted using 人
        String student= "<noun name=\"Student\" entity=\"Student\" alias=\"Students\" counter=\"人\">\r\n" +
                "  <value plural=\"n\">学生</value>\r\n" +
                "</noun>";
        assertValue(JAPANESE, label, student, new Renameable[] {new MockTestRenameable("student")}, "学生が1人あります", "1");
    }

    public void testSimplifiedChineseCounterWord() throws Exception {
        final HumanLanguage HANS = LanguageProviderFactory.get().getLanguage("zh_CN");
        // Accounts are counted using default 个
        String label = "有{0}<counter/><entity entity=\"0\"/>";
        assertValue(HANS, label, null, new Renameable[] {new MockTestRenameable("account")}, "有1个客户", "1");

        // Chairs are counted using 把?
        String chair= "<noun name=\"Chair\" entity=\"Chair\" alias=\"Chairs\" counter=\"把\">\r\n" +
                "  <value plural=\"n\">椅子</value>\r\n" +
                "</noun>";
        assertValue(HANS, label, chair, new Renameable[] {new MockTestRenameable("chair")}, "有1把椅子", "1");

        // Puppies are counted using 把?
        String puppy= "<noun name=\"Puppy\" entity=\"Puppy\" alias=\"Puppies\" counter=\"只\">\r\n" +
                "  <value plural=\"n\">小狗</value>\r\n" +
                "</noun>";
        assertValue(HANS, label, puppy, new Renameable[] {new MockTestRenameable("puppy")}, "有2只小狗", "2");
    }

    public void testTraditionalChineseCounterWord() throws Exception {
        final HumanLanguage HANT = LanguageProviderFactory.get().getLanguage("zh_TW");
        // Accounts are counted using default 個
        String label = "有{0}<counter/><entity entity=\"0\"/>";
        assertValue(HANT, label, null, new Renameable[] {new MockTestRenameable("account")}, "有1個帳戶", "1");

        // Chairs are counted using 把?
        String chair= "<noun name=\"Chair\" entity=\"Chair\" alias=\"Chairs\" counter=\"把\">\r\n" +
                "  <value plural=\"n\">椅子</value>\r\n" +
                "</noun>";
        assertValue(HANT, label, chair, new Renameable[] {new MockTestRenameable("chair")}, "有1把椅子", "1");

        // Puppies are counted using 把?
        String puppy= "<noun name=\"Puppy\" entity=\"Puppy\" alias=\"Puppies\" counter=\"隻\">\r\n" +
                "  <value plural=\"n\">小狗</value>\r\n" +
                "</noun>";
        assertValue(HANT, label, puppy, new Renameable[] {new MockTestRenameable("puppy")}, "有2隻小狗", "2");
    }

    public void testKoreanCounterWord() throws Exception {
        final HumanLanguage KOREAN = LanguageProviderFactory.get().getLanguage("ko");
        // Accounts are counted using default 개
        String label = "{0} <counter/>의 <entity entity=\"0\"/> 있습니다.";
        assertValue(KOREAN, label, null, new Renameable[] {new MockTestRenameable("account")}, "1 개의 계정 있습니다.", "1");

        // Students are counted using 명?
        String student= "<noun name=\"Student\" entity=\"Student\" alias=\"Students\" counter=\"명\">\r\n" +
                "  <value plural=\"n\">학생이</value>\r\n" +
                "</noun>";
        assertValue(KOREAN, label, student, new Renameable[] {new MockTestRenameable("student")}, "1 명의 학생이 있습니다.", "1");

        // Puppies are counted using 마리?
        String puppy= "<noun name=\"Puppy\" entity=\"Puppy\" alias=\"Puppies\" counter=\"마리\">\r\n" +
                "  <value plural=\"n\">강아지가</value>\r\n" +
                "</noun>";
        assertValue(KOREAN, label, puppy, new Renameable[] {new MockTestRenameable("puppy")}, "2 마리의 강아지가 있습니다.", "2");
    }

    public void testLanguageWithNoClassifiers() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage("en_US");
        // Make sure counter doesn't gack but instead converts to "" in non-classifying languages
        String label = "There were {0} <counter/> of <entity entity=\"0\"/>";
        assertValue(ENGLISH, label, null, new Renameable[] {new MockTestRenameable("account")}, "There were 1  of account", "1");

    }
}
