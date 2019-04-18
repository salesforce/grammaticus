/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.util.Set;

import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.parser.TestLanguageLabelSetDescriptor;
import com.google.common.collect.ImmutableSet;

/**
 * Test various issues details around the auto deriving of declensions.
 *
 * @author stamm
 */
public class AutoDerivingDeclensionTest extends BaseGrammaticalLabelTest {
    public AutoDerivingDeclensionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public String getValue(HumanLanguage language, String label, String grammarSnippet) throws IOException {
        TestLanguageLabelSetDescriptor testDesc = new TestLanguageLabelSetDescriptor(getDescriptor(language), LabelUtils.getSampleLabelFile(label), LabelUtils.getSampleGrammarFile(grammarSnippet));
        GrammaticalLabelSet ls = GrammaticalLabelSetImpl.getTestLabelSet(testDesc);
        return ls.getString("Test", "Test");
    }

    public void assertValue(HumanLanguage language, String label, String grammarSnippet, String expectedResult) throws IOException {
        String result = getValue(language, label, grammarSnippet);
        assertEquals("Mismatch for " + label, expectedResult, result);
    }

    private final static String DA_THE = "    <adjective name=\"The\">\r\n" +
    		"        <value gender=\"e\" plural=\"n\">Den</value>\r\n" +
    		"        <value gender=\"n\" plural=\"n\">Det</value>\r\n" +
    		"        <value gender=\"e\" plural=\"y\">De</value>\r\n" +
    		"        <value gender=\"n\" plural=\"y\">De</value>\r\n" +
    		"	</adjective> ";
    private final static String DA_OLDER= "	<adjective name=\"Elder\">" +  // Use elder instead of older because adding a form to an adjective after closing only affects one form
    		"        <value gender=\"n\" plural=\"n\">Ældre</value>	" +
    		"    </adjective>";
    private final static String DA_ACCOUNT = "    <noun name=\"Account\" entity=\"Account\" type=\"entity\" alias=\"Accounts\" gender=\"e\" startsWith=\"v\">" +
    		"       <value plural=\"n\">Konto</value>" +
    		"         <value plural=\"y\">Kontoer</value>" +
    		"        <value plural=\"n\" article=\"a\">En konto</value>" +
    		"        <value plural=\"y\" article=\"a\">Konti</value>" +
    		"        <value plural=\"n\" article=\"the\">Kontoen</value>" +
    		"        <value plural=\"y\" article=\"the\">Kontiene</value>" +
    		"        </noun>";

    /**
     * Test of how we handle general defaulting (adjectives)
     */
    public void testDefaultMissingForms() throws Exception {
    	final HumanLanguage DANISH = LanguageProviderFactory.get().getLanguage("da");
        // Make sure "older" (which only has a singular form defined) defaults correctly
        assertValue(DANISH, "<elder/> <account/>", DA_THE+DA_ACCOUNT+DA_OLDER, "ældre konto");
        assertValue(DANISH, "<the/> <elder/> <account/>", DA_THE+DA_ACCOUNT+DA_OLDER, "den ældre konto");
        assertValue(DANISH, "<elder/> <accounts/>", DA_THE+DA_ACCOUNT+DA_OLDER, "ældre kontoer");
        assertValue(DANISH, "<Elder/> <Accounts article=\"the\"/>", DA_THE+DA_ACCOUNT+DA_OLDER, "Ældre Kontiene");
        assertValue(DANISH, "<Elder/> <Accounts article=\"a\"/>", DA_THE+DA_ACCOUNT+DA_OLDER, "Ældre Konti");
    }



    private final static String BG_THIN = "<adjective name=\"Thin\">\r\n" +
            "  <value gender=\"m\" plural=\"n\">\u0441\u043b\u0430\u0431</value>\r\n" +
            "  <value gender=\"m\" plural=\"n\" article=\"the\">\u0441\u043b\u0430\u0431\u0438\u044f\u0442</value>\r\n" +
            "  <value gender=\"f\" plural=\"n\">\u0441\u043b\u0430\u0431\u0430</value>\r\n" +
            "  <value gender=\"n\" plural=\"n\">\u0441\u043b\u0430\u0431\u043e</value>\r\n" +
            "  <value plural=\"y\">\u0441\u043b\u0430\u0431\u0438</value>\r\n" +
            "</adjective>";
    private final static String BG_RICH = "<adjective name=\"Rich\">\r\n" +
            "<value gender=\"m\" plural=\"n\">\u041f\u043e\u043b\u0435\u0437\u0435\u043d</value>\r\n" +
            "<value gender=\"m\" plural=\"n\" article=\"the\">\u041f\u043e\u043b\u0435\u0437\u043d\u0438\u044f\u0442</value>\r\n" +
            "<value gender=\"f\" plural=\"n\">\u041f\u043e\u043b\u0435\u0437\u043d\u0430</value>\r\n" +
            "<value gender=\"n\" plural=\"n\">\u041f\u043e\u043b\u0435\u0437\u043d\u043e</value>\r\n" +
            "<value plural=\"y\">\u041f\u043e\u043b\u0435\u0437\u043d\u0438</value>\r\n" +
            "</adjective>";

    private final static String BG_GRANDFATHER= "<noun name=\"Grandfather\" entity=\"Grandfather\" alias=\"Grandfathers\" gender=\"m\">\r\n" +
            "  <value plural=\"n\">\u0434\u044f\u0434\u043e</value>\r\n" +
            "  <value plural=\"y\">\u0434\u044f\u0434\u043e</value>\r\n" +
            "</noun>";
    private final static String BG_MAN= "<noun name=\"Man\" entity=\"Man\" alias=\"Men\" gender=\"m\">\r\n" +
        "  <value plural=\"n\">\u043c\u044a\u0436</value>\r\n" +
        "  <value plural=\"y\">\u043c\u044a\u0436\u0435</value>\r\n" +
        "</noun>";
    private final static String BG_HERO= "<noun name=\"Hero\" entity=\"Hero\" alias=\"Heroes\" gender=\"m\">\r\n" +
        "  <value plural=\"n\">\u0433\u0435\u0440\u043e\u0439</value>\r\n" +
        "  <value plural=\"y\">\u0433\u0435\u0440\u043e\u0438</value>\r\n" +
        "</noun>";
    private final static String BG_TABLE= "<noun name=\"Table\" entity=\"Table\" alias=\"Tables\" gender=\"f\">\r\n" +
        "  <value plural=\"n\">\u043c\u0430\u0441\u0430</value>\r\n" +
        "  <value plural=\"y\">\u043c\u0430\u0441\u0430\u044f</value>\r\n" +
        "</noun>";
    private final static String BG_WHISKY= "<noun name=\"Whisky\" entity=\"Whisky\" alias=\"Whiskys\" gender=\"n\">\r\n" +
        "  <value plural=\"n\">\u0443\u0438\u0441\u043a\u0438</value>\r\n" +
        "  <value plural=\"y\">\u0443\u0438\u0441\u043a\u0438\u0442\u0430</value>\r\n" +
        "</noun>";

    //     חשבונות   חשבון
    private final static String IW_MAN =
        "<noun name=\"Man\" entity=\"Man\" type=\"entity\" alias=\"Men\" gender=\"m\">\r\n"
            + "  <value plural=\"n\">\u05d7\u05e9\u05d1\u05d5\u05df</value>\r\n"
            + "  <value plural=\"y\">\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea</value>\r\n"
            + "</noun>\r\n";

    private final static String IW_MAN_TYPE_FIELD =
        "<noun name=\"ManType\" entity=\"Man\" type=\"field\" alias=\"ManTypes\" gender=\"m\">\r\n"
            + "  <value plural=\"n\">\u05d7\u05e9\u05d1\u05d5\u05df</value>\r\n"
            + "  <value plural=\"y\">\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea</value>\r\n"
            + "</noun>\r\n";

    private final static String IW_TABLE =
        "<noun name=\"Table\" entity=\"Table\" type=\"entity\" alias=\"Tables\" gender=\"m\">\r\n"
            + "<value plural=\"n\">מבצע קידום</value>\r\n"
            + "<value plural=\"y\">מבצעי קידום</value>\r\n"
            + "<value plural=\"n\" article=\"the\">מבצע הקידום</value>\r\n"
            + "<value plural=\"y\" article=\"the\">מבצעי הקידום</value>\r\n"
            + "</noun>";

    private final static String IW_LOTSA = "<adjective name=\"Lotsa\">\r\n" +  // Used to be "more" but that's defined in sfdcjectives.xml now ...
        "    <value gender=\"m\" plural=\"n\">\u05e0\u05d5\u05e1\u05e3</value>\r\n" +  // נוסף
        "    <value gender=\"f\" plural=\"n\">\u05e0\u05d5\u05e1\u05e4\u05ea</value>\r\n" +  // נוספת
        "    <value gender=\"m\" plural=\"y\">\u05e0\u05d5\u05e1\u05e4\u05d9\u05dd</value>\r\n" +  // נוספים
        "    <value gender=\"f\" plural=\"y\">\u05e0\u05d5\u05e1\u05e4\u05d5\u05ea</value>\r\n" +  // נוספות
        "</adjective>";


    // The highly chair (with the translators diacritics...
    private final static String AR_CHAIR =     "<noun name=\"Chair\" entity=\"Chair\" type=\"entity\" alias=\"Chairs\" gender=\"m\">\r\n"+
        "<value plural=\"n\">\u0643\u0631\u0633\u064a</value>\r\n"+  // كرسي
        "<value plural=\"n\" poss=\"f\">\u0643\u0631\u0633\u064a\u0651\u0650\u064a</value>\r\n"+  // كرسيِّي
        "<value plural=\"n\" poss=\"s\">\u0643\u0631\u0633\u064a\u0651\u064f\u0643\u064e</value>\r\n"+  // كرسيُّكَ
        "<value plural=\"y\">\u0643\u0631\u0627\u0633\u064a</value>\r\n"+   // كراسي
        "<value plural=\"y\" poss=\"f\" >\u0643\u0631\u0627\u0633\u064a\u0651\u064e</value>\r\n"+  // كراسيَّ
        "<value plural=\"y\" poss=\"s\">\u0643\u0631\u0627\u0633\u0650\u064a\u0643\u064e</value>\r\n"+  // كراسِيكَ
        "<value plural=\"n\" case=\"a\">\u0643\u0631\u0633\u064a</value>\r\n"+ // كرسي
        "<value plural=\"n\" case=\"a\" poss=\"f\">\u0643\u0631\u0633\u064a\u0651\u0650\u064a</value>\r\n"+  // كرسيِّي
        "<value plural=\"n\" case=\"a\" poss=\"s\">\u0643\u0631\u0633\u064a\u0651\u064f\u0643\u064e</value>\r\n"+  // كرسيُّكَ
        "<value plural=\"y\" case=\"a\">\u0643\u0631\u0627\u0633\u064a</value>\r\n"+  // كراسي
        "<value plural=\"y\" case=\"a\" poss=\"f\" >\u0643\u0631\u0627\u0633\u064a\u0651\u064e</value>\r\n"+  // كراسيَّ
        "<value plural=\"y\" case=\"a\" poss=\"s\">\u0643\u0631\u0627\u0633\u0650\u064a\u0643\u064e</value>\r\n"+  // كراسِيكَ
    "</noun>";

    private final static String AR_TABLE = "    <noun name=\"Table\" entity=\"Table\" type=\"entity\" alias=\"Tables\" gender=\"f\">\r\n" +
        "    <value plural=\"n\">\u0637\u0627\u0648\u0644\u0629</value>\r\n" +  // طاولة
        "    <value plural=\"n\" poss=\"f\">\u0637\u0627\u0648\u0644\u062a\u0650\u064a</value>\r\n" + // طاولتِي
        "    <value plural=\"n\" poss=\"s\">\u0637\u0627\u0648\u0644\u062a\u064f\u0643\u064e</value>\r\n" +  // طاولتُكَ
        "    <value plural=\"y\">\u0637\u0627\u0648\u0644\u0627\u062a</value>\r\n" +  // طاولات
        "    <value plural=\"y\" poss=\"f\">???</value>\r\n" +  // TODO: Not back from translators
        "    <value plural=\"y\" poss=\"s\">???</value>\r\n" +
        "</noun>";

    /*
    private final static String AR_NEWOLD = "    <adjective name=\"Neu\">\r\n" +  // Use Neu instead of New to prevent clash with english adjectives
     "   <value gender=\"m\" plural=\"n\">\u062c\u062f\u064a\u062f</value>\r\n" +  // جديد
     "   <value gender=\"m\" plural=\"n\" poss=\"f\">\u0627\u0644\u062c\u062f\u064a\u062f</value> \r\n" +  // الجديد
     "   <value gender=\"m\" plural=\"n\" poss=\"s\">\u0627\u0644\u062c\u062f\u064a\u062f</value> \r\n" +  // الجديد
     "   <value gender=\"m\" plural=\"y\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
     "   <value gender=\"m\" plural=\"y\" poss=\"f\">???</value>\r\n" +
     "   <value gender=\"m\" plural=\"y\" poss=\"s\">???</value>\r\n" +
     "   <value gender=\"f\" plural=\"n\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
     "   <value gender=\"f\" plural=\"n\" poss=\"f\">\u0627\u0644\u062c\u062f\u064a\u062f\u0629</value>  \r\n" +  // الجديدة
     "   <value gender=\"f\" plural=\"n\" poss=\"s\">\u0627\u0644\u062c\u062f\u064a\u062f\u0629</value>  \r\n" + // الجديدة
     "   <value gender=\"f\" plural=\"y\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
     "   <value gender=\"f\" plural=\"y\" poss=\"f\">???</value>\r\n" +
     "   <value gender=\"f\" plural=\"y\" poss=\"s\">???</value>\r\n" +
     "</adjective>";
*/

    // This is the new that came from the translators...
    private final static String AR_NEW = "    <adjective name=\"Neu\">\r\n" +  // Use Neu instead of New to prevent clash with english adjectives
    "    <value gender=\"m\" plural=\"n\">\u062c\u062f\u064a\u062f</value>\r\n" +  // جديد
    "    <value gender=\"m\" plural=\"n\" poss=\"f\">\u0627\u0644\u062c\u062f\u064a\u062f</value>\r\n" +  // الجديد
    "    <value gender=\"m\" plural=\"n\" poss=\"s\">\u0627\u0644\u062c\u062f\u064a\u062f</value> \r\n" +  // الجديد
    "    <value gender=\"m\" plural=\"y\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
    "    <value gender=\"m\" plural=\"y\" poss=\"f\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
    "    <value gender=\"m\" plural=\"y\" poss=\"s\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
    "    <value gender=\"f\" plural=\"n\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +   // جديدة
    "    <value gender=\"f\" plural=\"n\" poss=\"f\">\u0627\u0644\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // الجديدة
    "    <value gender=\"f\" plural=\"n\" poss=\"s\">\u0627\u0644\u062c\u062f\u064a\u062f\u0629</value> \r\n" + // الجديدة
    "    <value gender=\"f\" plural=\"y\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
    "    <value gender=\"f\" plural=\"y\" poss=\"f\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" + // جديدة
    "    <value gender=\"f\" plural=\"y\" poss=\"s\">\u062c\u062f\u064a\u062f\u0629</value>\r\n" +  // جديدة
    "</adjective>";


    public void testBulgarianDerivations() throws Exception {
    	final HumanLanguage BULGARIAN = LanguageProviderFactory.get().getLanguage("bg");
        assertValue(BULGARIAN, "<Man article=\"the\"/>", BG_MAN, "\u043c\u044a\u0436\u044a\u0442");
        assertValue(BULGARIAN, "<Man article=\"the\" case=\"o\"/>", BG_MAN, "\u043c\u044a\u0436\u0430");
        assertValue(BULGARIAN, "<Men article=\"the\"/>", BG_MAN, "\u043c\u044a\u0436\u0435\u0442\u0435");
        assertValue(BULGARIAN, "<Thin/> <Men/>", BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438 \u043c\u044a\u0436\u0435");
        assertValue(BULGARIAN, "<Thin article=\"the\"/> <Men/>", BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438\u0442\u0435 \u043c\u044a\u0436\u0435");
        assertValue(BULGARIAN, "<Thin/> <Men article=\"the\"/>", BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438\u0442\u0435 \u043c\u044a\u0436\u0435");
        assertValue(BULGARIAN, "<Thin article=\"the\" case=\"o\"/> <Man/>", BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438\u044f \u043c\u044a\u0436");  // слабия мъж
        assertValue(BULGARIAN, "<Thin/> <Man article=\"the\" case=\"o\"/>", BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438\u044f \u043c\u044a\u0436");
        assertValue(BULGARIAN, "<Thin article=\"the\" case=\"o\"/> <Rich/> <Man/>", BG_RICH + BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438\u044f \u041f\u043e\u043b\u0435\u0437\u0435\u043d \u043c\u044a\u0436");
        assertValue(BULGARIAN, "<Thin/> <Rich/> <Man article=\"the\" case=\"o\"/>", BG_RICH + BG_THIN + BG_MAN, "\u0441\u043b\u0430\u0431\u0438\u044f \u041f\u043e\u043b\u0435\u0437\u0435\u043d \u043c\u044a\u0436");
        assertValue(BULGARIAN, "<Thin article=\"the\" case=\"o\"/> <Rich/> <Table/>", BG_RICH + BG_THIN + BG_TABLE, "\u0441\u043b\u0430\u0431\u0430 \u041f\u043e\u043b\u0435\u0437\u043d\u0430 \u043c\u0430\u0441\u0430");
        assertValue(BULGARIAN, "<Thin/> <Rich/> <Table article=\"the\" case=\"o\"/>", BG_RICH + BG_THIN + BG_TABLE, "\u0441\u043b\u0430\u0431\u0430 \u041f\u043e\u043b\u0435\u0437\u043d\u0430 \u043c\u0430\u0441\u0430");

        assertValue(BULGARIAN, "<Grandfather article=\"the\"/>", BG_GRANDFATHER, "\u0434\u044f\u0434\u043e\u0442\u043e");
        assertValue(BULGARIAN, "<Table article=\"the\"/>", BG_TABLE, "\u043c\u0430\u0441\u0430\u0442\u0430");
        assertValue(BULGARIAN, "<Table article=\"the\"/>", BG_TABLE, "\u043c\u0430\u0441\u0430\u0442\u0430");
        assertValue(BULGARIAN, "<Hero article=\"the\"/>", BG_HERO, "\u0433\u0435\u0440\u043e\u044f\u0442");
        assertValue(BULGARIAN, "<Hero article=\"the\" case=\"o\"/>", BG_HERO, "\u0433\u0435\u0440\u043e\u044f");
        assertValue(BULGARIAN, "<Thin article=\"the\"/> <Hero/>", BG_THIN + BG_HERO, "\u0441\u043b\u0430\u0431\u0438\u044f\u0442 \u0433\u0435\u0440\u043e\u0439");
        assertValue(BULGARIAN, "<Thin article=\"the\" case=\"o\"/> <Hero/>", BG_THIN + BG_HERO, "\u0441\u043b\u0430\u0431\u0438\u044f \u0433\u0435\u0440\u043e\u0439");
        assertValue(BULGARIAN, "<Heroes article=\"the\"/>", BG_HERO, "\u0433\u0435\u0440\u043e\u0438\u0442\u0435");
        assertValue(BULGARIAN, "<Whisky article=\"the\"/>", BG_WHISKY, "\u0443\u0438\u0441\u043a\u0438\u0442\u043e");
        assertValue(BULGARIAN, "<Whiskys article=\"the\"/>", BG_WHISKY, "\u0443\u0438\u0441\u043a\u0438\u0442\u0430\u0442\u0430");
    }

    // Set of adjectives that are really differently declned adjectives
    private final static Set<String> DIFFERENTLY_DECLINED_ADJECTIVES = ImmutableSet.of("recently", "no", "num", "each", "every", "that", "this", "these", "default", "more", "any", "multiple");

    public void testBulgarianAdjectives() throws Exception {
    	final HumanLanguage BULGARIAN = LanguageProviderFactory.get().getLanguage("bg");
        LanguageDictionary dictionary = loadDictionary(BULGARIAN);
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (String adjName : dictionary.getAllTermNames(TermType.Adjective)) {
            Adjective adj = dictionary.getAdjective(adjName);
            assertNotNull(adj);
            if (adj.isCopiedFromDefault())
             {
                continue;  // Ignore if we're copied from default
            }
            if (DIFFERENTLY_DECLINED_ADJECTIVES.contains(adjName))
             {
                continue;  // Ignore adverbs
            }
            String value = adj.getString(dictionary.getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.MASCULINE,
                    LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.DEFINITE, LanguagePossessive.NONE));
            if (value != null && !value.endsWith("\u0442")) {
                int index = value.indexOf(' ');
                if (index > 0 && value.charAt(index-1) == '\u0442')
                 {
                    continue;  // The first word ends with T, which is ok
                }
                sb.append("Bulgarian Adjective " + adj.getName() + " has an incorrect value for the singular masculing definite.  Needs to end with \u0442\n");
                count++;
            }
        }
        assertEquals(sb.toString(), 0, count);
    }

    public void testHebrewDerivations() throws Exception {
    	final HumanLanguage HEBREW = LanguageProviderFactory.get().getLanguage("iw");
        // Test autoderiving for entity nouns
        assertValue(HEBREW, "<Man/>", IW_MAN, "\u05d7\u05e9\u05d1\u05d5\u05df");  // חשבון
        assertValue(HEBREW, "<Men/>", IW_MAN, "\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea");  // חשבונות
        assertValue(HEBREW, "<Man article=\"the\"/>", IW_MAN, "\u05d4\u05d7\u05e9\u05d1\u05d5\u05df");  // החשבון
        assertValue(HEBREW, "<Men article=\"the\"/>", IW_MAN, "\u05d4\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea");  // החשבונות

        // Test that a noun with the article included works correctly as well
        assertValue(HEBREW, "<Table/>", IW_TABLE, "\u05de\u05d1\u05e6\u05e2 \u05e7\u05d9\u05d3\u05d5\u05dd");  // מבצע קידום
        assertValue(HEBREW, "<Tables/>", IW_TABLE, "\u05de\u05d1\u05e6\u05e2\u05d9 \u05e7\u05d9\u05d3\u05d5\u05dd");  // מבצעי קידום
        assertValue(HEBREW, "<Table article=\"the\"/>", IW_TABLE, "\u05de\u05d1\u05e6\u05e2 \u05d4\u05e7\u05d9\u05d3\u05d5\u05dd");  // מבצע הקידום
        assertValue(HEBREW, "<Tables article=\"the\"/>", IW_TABLE, "\u05de\u05d1\u05e6\u05e2\u05d9 \u05d4\u05e7\u05d9\u05d3\u05d5\u05dd");  // מבצעי הקידום

        // Make sure field nouns do not autoderive themselves with the ה
        String singularManType = "\u05d7\u05e9\u05d1\u05d5\u05df";
        assertValue(HEBREW, "<ManType/>", IW_MAN_TYPE_FIELD, singularManType);  // חשבון
        assertValue(HEBREW, "<ManTypes/>", IW_MAN_TYPE_FIELD, "\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea");  // חשבונות
        assertValue(HEBREW, "<ManType article=\"the\"/>", IW_MAN_TYPE_FIELD, singularManType);  // חשבון
        assertValue(HEBREW, "<ManTypes article=\"the\"/>", IW_MAN_TYPE_FIELD, "\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea");  // חשבונות

        // Make sure the adjective auto derive the adjective
        assertValue(HEBREW, "<Man/> <lotsa/>", IW_MAN + IW_LOTSA,  "\u05d7\u05e9\u05d1\u05d5\u05df \u05e0\u05d5\u05e1\u05e3");  //   חשבון נוסף
        assertValue(HEBREW, "<Men/> <lotsa/>", IW_MAN + IW_LOTSA, "\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea \u05e0\u05d5\u05e1\u05e4\u05d9\u05dd");  // חשבונות נוספים
        assertValue(HEBREW, "<Man article=\"the\"/> <lotsa/>", IW_MAN + IW_LOTSA, "\u05d4\u05d7\u05e9\u05d1\u05d5\u05df \u05d4\u05e0\u05d5\u05e1\u05e3");  // החשבון הנוסף
        assertValue(HEBREW, "<Men article=\"the\"/> <lotsa/>", IW_MAN + IW_LOTSA, "\u05d4\u05d7\u05e9\u05d1\u05d5\u05e0\u05d5\u05ea \u05d4\u05e0\u05d5\u05e1\u05e4\u05d9\u05dd");  // חשבונות הנוספים


    }

    public void testArabicDerivations() throws Exception {
    	final HumanLanguage ARABIC = LanguageProviderFactory.get().getLanguage("ar");
        // Test autoderiving for entity nouns.  These values were provided by the translators
        // TODO: Waiting on next line for resolution to the final alif in the accusative question.
        assertValue(ARABIC, "<Chair case=\"a\"/>", AR_CHAIR, "\u0643\u0631\u0633\u064a");  // كرسي
        assertValue(ARABIC, "<Chair case=\"a\" article=\"the\"/>", AR_CHAIR, "\u0627\u0644\u0643\u0631\u0633\u064a");  // الكرسي
        assertValue(ARABIC, "<Chairs case=\"a\"/>", AR_CHAIR, "\u0643\u0631\u0627\u0633\u064a");  // كراسي
        assertValue(ARABIC, "<Chairs case=\"a\" article=\"the\"/>", AR_CHAIR, "\u0627\u0644\u0643\u0631\u0627\u0633\u064a");  // الكراسي
        assertValue(ARABIC, "<Chair case=\"a\" article=\"the\" plural=\"d\"/>", AR_CHAIR, "\u0627\u0644\u0643\u0631\u0627\u0633\u064a");  // الكراسي

        assertValue(ARABIC, "<Chair case=\"a\" poss=\"f\"/>", AR_CHAIR, "\u0643\u0631\u0633\u064a\u0651\u0650\u064a");  // كرسيِّي
        assertValue(ARABIC, "<Chair case=\"a\" poss=\"f\" article=\"the\"/>", AR_CHAIR, "\u0627\u0644\u0643\u0631\u0633\u064a\u0651\u0650\u064a");  // الكرسيِّي
        assertValue(ARABIC, "<Chair case=\"a\" poss=\"s\"/>", AR_CHAIR, "\u0643\u0631\u0633\u064a\u0651\u064f\u0643\u064e");  // كرسيُّكَ
        assertValue(ARABIC, "<Chair case=\"a\" poss=\"s\" article=\"the\"/>", AR_CHAIR, "\u0627\u0644\u0643\u0631\u0633\u064a\u0651\u064f\u0643\u064e");  // الكرسيُّكَ

        // Now test for table
        assertValue(ARABIC, "<Table case=\"a\"/>", AR_TABLE, "\u0637\u0627\u0648\u0644\u0629");  // طاولة
        assertValue(ARABIC, "<Table case=\"a\" article=\"the\"/>", AR_TABLE, "\u0627\u0644\u0637\u0627\u0648\u0644\u0629");  // الطاولة
        assertValue(ARABIC, "<Tables case=\"a\"/>", AR_TABLE, "\u0637\u0627\u0648\u0644\u0627\u062a");  // طاولات
        assertValue(ARABIC, "<Tables case=\"a\" article=\"the\"/>", AR_TABLE, "\u0627\u0644\u0637\u0627\u0648\u0644\u0627\u062a");  // الطاولات

        assertValue(ARABIC, "<Table case=\"a\" poss=\"f\"/>", AR_TABLE, "\u0637\u0627\u0648\u0644\u062a\u0650\u064a");  // طاولتِي
        assertValue(ARABIC, "<Table case=\"a\" poss=\"f\" article=\"the\"/>", AR_TABLE, "\u0627\u0644\u0637\u0627\u0648\u0644\u062a\u0650\u064a");  // الطاولتِي
        //assertValue(ARABIC, "<Table case=\"a\" poss=\"s\"/>", AR_TABLE, "\u0637\u0627\u0648\u0644\u062a\u0650\u0643\u064e");  // طاولتِكَ
        //assertValue(ARABIC, "<Table case=\"a\" poss=\"s\" article=\"the\"/>", AR_TABLE, "\u0627\u0644\u0637\u0627\u0648\u0644\u062a\u0650\u0643\u064e");  // الطاولتِكَ

        // Now try the same with but with "new"
        assertValue(ARABIC, "<Chair case=\"a\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0643\u0631\u0633\u064a \u062c\u062f\u064a\u062f");  // كرسي جديد
        assertValue(ARABIC, "<Chair article=\"the\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0627\u0644\u0643\u0631\u0633\u064a \u0627\u0644\u062c\u062f\u064a\u062f");  // الكرسي الجديد
        assertValue(ARABIC, "<Chairs article=\"the\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0627\u0644\u0643\u0631\u0627\u0633\u064a \u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // الكراسي الجديدة
        assertValue(ARABIC, "<Chair poss=\"f\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0643\u0631\u0633\u064a\u0651\u0650\u064a \u0627\u0644\u062c\u062f\u064a\u062f");  // كرسيِّي الجديد
        assertValue(ARABIC, "<Chair case=\"a\" poss=\"f\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0643\u0631\u0633\u064a\u0651\u0650\u064a \u0627\u0644\u062c\u062f\u064a\u062f");  // كرسيِّي الجديد
        assertValue(ARABIC, "<Chair case=\"a\" poss=\"s\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0643\u0631\u0633\u064a\u0651\u064f\u0643\u064e \u0627\u0644\u062c\u062f\u064a\u062f"); // كرسيُّكَ الجديد
        assertValue(ARABIC, "<Chair case=\"a\" poss=\"s\" article=\"the\"/> <Neu/>", AR_CHAIR + AR_NEW, "\u0627\u0644\u0643\u0631\u0633\u064a\u0651\u064f\u0643\u064e \u0627\u0644\u062c\u062f\u064a\u062f");  // الكرسيُّكَ الجديد


        assertValue(ARABIC, "<Table case=\"a\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0637\u0627\u0648\u0644\u0629 \u062c\u062f\u064a\u062f\u0629");  // طاولة جديدة
        assertValue(ARABIC, "<Table article=\"the\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0627\u0644\u0637\u0627\u0648\u0644\u0629 \u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // الطاولة الجديدة
        assertValue(ARABIC, "<Tables article=\"the\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0627\u0644\u0637\u0627\u0648\u0644\u0627\u062a \u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // الطاولات الجديدة
        assertValue(ARABIC, "<Table case=\"a\" poss=\"f\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0637\u0627\u0648\u0644\u062a\u0650\u064a \u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // طاولتِي الجديدة
        //assertValue(ARABIC, "<Table case=\"a\" poss=\"s\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0627\u0644\u0637\u0627\u0648\u0644\u062a\u0650\u064a \u0627\u0644\u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // الطاولتِي الالجديدة

        // Assert that duals is the same as plural
        assertValue(ARABIC, "<Table article=\"the\" plural=\"d\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0627\u0644\u0637\u0627\u0648\u0644\u0627\u062a \u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // الطاولات الجديدة
        assertValue(ARABIC, "<Table article=\"the\" plural=\"two\"/> <Neu/>", AR_TABLE + AR_NEW, "\u0627\u0644\u0637\u0627\u0648\u0644\u0627\u062a \u0627\u0644\u062c\u062f\u064a\u062f\u0629");  // الطاولات الجديدة
    }

    public void testKoreanEndsWith() throws Exception {
    	final HumanLanguage KOREAN = LanguageProviderFactory.get().getLanguage("ko");

    	String KO_PARTICLES = "<adjective name=\"Eul\"><value endsWith=\"c\">을</value><value endsWith=\"v\">를</value></adjective>"
    			+ "<adjective name=\"Euro\"><value endsWith=\"c\">으로</value><value endsWith=\"v\">로</value><value endsWith=\"s\">로</value></adjective>";

    	String KO_NOUN_FORMAT= "<noun name=\"Table\" entity=\"Table\" type=\"entity\" alias=\"Tables\" gender=\"f\"><value plural=\"n\">%s</value></noun>";
    
    
    	// Note
        assertValue(KOREAN, "<Table/><eul/>", String.format(KO_NOUN_FORMAT, "작업") + KO_PARTICLES, "작업을");  // Jag-eob: Ends with consonant
        assertValue(KOREAN, "<Table/><euro/>", String.format(KO_NOUN_FORMAT, "작업") + KO_PARTICLES, "작업으로");  // Jag-eob: Ends with consonant

        // Task
        assertValue(KOREAN, "<Table/><eul/>", String.format(KO_NOUN_FORMAT, "노트") + KO_PARTICLES, "노트를");  // Noteu: Ends with vowel
        assertValue(KOREAN, "<Table/><euro/>", String.format(KO_NOUN_FORMAT, "노트") + KO_PARTICLES, "노트로");  // Noteu: Ends with vowel

        // Skill
        assertValue(KOREAN, "<Table/><eul/>", String.format(KO_NOUN_FORMAT, "기술") + KO_PARTICLES, "기술을");  // gisul: Ends with flap
        assertValue(KOREAN, "<Table/><euro/>", String.format(KO_NOUN_FORMAT, "기술") + KO_PARTICLES, "기술로");  // Gisul: Ends with flap
        
    }
    
    /*
    // This is the above test without the unicode conversion for easy readibility
    private final static String BG_THIN = "<adjective name=\"Thin\">\r\n" +
            "  <value gender=\"m\" plural=\"n\">слаб</value>\r\n" +
            "  <value gender=\"m\" plural=\"n\" article=\"the\">слабият</value>\r\n" +
            "  <value gender=\"f\" plural=\"n\">слаба</value>\r\n" +
            "  <value gender=\"n\" plural=\"n\">слабо</value>\r\n" +
            "  <value plural=\"y\">слаби</value>\r\n" +
            "</adjective>";
    private final static String BG_GRANDFATHER= "<noun name=\"Grandfather\" entity=\"Grandfather\" alias=\"Grandfathers\" gender=\"m\">\r\n" +
            "  <value plural=\"n\">дядо</value>\r\n" +
            "  <value plural=\"y\">дядо</value>\r\n" +
            "</noun>";
    private final static String BG_MAN= "<noun name=\"Man\" entity=\"Man\" alias=\"Men\" gender=\"m\">\r\n" +
        "  <value plural=\"n\">мъж</value>\r\n" +
        "  <value plural=\"y\">мъже</value>\r\n" +
        "</noun>";
    private final static String BG_HERO= "<noun name=\"Hero\" entity=\"Hero\" alias=\"Heroes\" gender=\"m\">\r\n" +
        "  <value plural=\"n\">герой</value>\r\n" +
        "  <value plural=\"y\">герои</value>\r\n" +
        "</noun>";
    private final static String BG_TABLE= "<noun name=\"Table\" entity=\"Table\" alias=\"Tables\" gender=\"f\">\r\n" +
        "  <value plural=\"n\">маса</value>\r\n" +
        "  <value plural=\"y\">масая</value>\r\n" +
        "</noun>";
    private final static String BG_WHISKY= "<noun name=\"Whisky\" entity=\"Whisky\" alias=\"Whiskys\" gender=\"n\">\r\n" +
        "  <value plural=\"n\">уиски</value>\r\n" +
        "  <value plural=\"y\">уискита</value>\r\n" +
        "</noun>";


    public void testNounDerivations() throws Exception {
        assertValue(BULGARIAN, "<Man article=\"the\"/>", BG_MAN, "мъжът");
        assertValue(BULGARIAN, "<Man article=\"the\" case=\"o\"/>", BG_MAN, "мъжа");
        assertValue(BULGARIAN, "<Men article=\"the\"/>", BG_MAN, "мъжете");
        assertValue(BULGARIAN, "<Thin article=\"the\"/> <Men/>", BG_THIN + BG_MAN, "слабите мъже");
        assertValue(BULGARIAN, "<Thin/> <Men/>", BG_THIN + BG_MAN, "слаби мъже");
        assertValue(BULGARIAN, "<Grandfather article=\"the\"/>", BG_GRANDFATHER, "дядото");
        assertValue(BULGARIAN, "<Table article=\"the\"/>", BG_TABLE, "масата");
        assertValue(BULGARIAN, "<Table article=\"the\"/>", BG_TABLE, "масата");
        assertValue(BULGARIAN, "<Hero article=\"the\"/>", BG_HERO, "героят");
        assertValue(BULGARIAN, "<Hero article=\"the\" case=\"o\"/>", BG_HERO, "героя");
        assertValue(BULGARIAN, "<Thin article=\"the\"/> <Hero/>", BG_THIN + BG_HERO, "слабият герой");
        assertValue(BULGARIAN, "<Thin article=\"the\" case=\"o\"/> <Hero/>", BG_THIN + BG_HERO, "слабия герой");
        assertValue(BULGARIAN, "<Heroes article=\"the\"/>", BG_HERO, "героите");
        assertValue(BULGARIAN, "<Whisky article=\"the\"/>", BG_WHISKY, "уискито");
        assertValue(BULGARIAN, "<Whiskys article=\"the\"/>", BG_WHISKY, "уискитата");
    }
     */


}
