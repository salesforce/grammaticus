/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import org.junit.Test;

import junit.framework.TestCase;

/**
 *
 * General ftests for languages.
 *
 * @author cschell
 */

public class DefaultHumanLanguageImplTest extends TestCase {

    public DefaultHumanLanguageImplTest(String name) {
        super(name);
    }

    /**
     * test that the correct fallback language, which is used for retrieving
     * labels when the language is not available, is returned. For example, if
     * the DefaultHumanLanguageImpl is French (Canadian), which  is a platform-only language,
     *  the fallback language is French, since we don't have labels translated
     *  into French (Canadian).
     */
    @Test
    public void testDefaultHumanLanguageImplFallbackLanguage() throws Exception {
        assertNull("Did not get the correct fallback language for English",
            DefaultHumanLanguageImpl.ENGLISH.getFallbackLanguage());
        assertNull("Did not get the correct translation fallback language for English",
                DefaultHumanLanguageImpl.ENGLISH.getTranslationFallbackLanguage());
        assertSame("Did not get the correct fallback language for French (Canadian)", DefaultHumanLanguageImpl.FRENCH,
            DefaultHumanLanguageImpl.FRENCH_CA.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish (Mexican)", DefaultHumanLanguageImpl.SPANISH,
            DefaultHumanLanguageImpl.SPANISH_MX.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Bulgarian", DefaultHumanLanguageImpl.ENGLISH,
            DefaultHumanLanguageImpl.BULGARIAN.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Moldovan", DefaultHumanLanguageImpl.ROMANIAN,
            DefaultHumanLanguageImpl.MOLDOVAN.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Portuguese (European)", DefaultHumanLanguageImpl.PORTUGUESE_BR,
            DefaultHumanLanguageImpl.PORTUGUESE_PT.getFallbackLanguage());
        assertSame("Did not get the correct translation fallback language for Portuguese (European)", DefaultHumanLanguageImpl.PORTUGUESE_BR,
                DefaultHumanLanguageImpl.PORTUGUESE_PT.getTranslationFallbackLanguage());
        assertNull("Did not get the correct translation fallback language for Portuguese (Brazilian)",
                DefaultHumanLanguageImpl.PORTUGUESE_BR.getTranslationFallbackLanguage());
        assertSame("Did not get the correct fallback language for Malaysian",  DefaultHumanLanguageImpl.INDONESIAN,
                DefaultHumanLanguageImpl.MALAY.getFallbackLanguage());  // Indonesian is a "register" of Malay
        assertNull("Did not get the correct fallback language for Malaysian",
                DefaultHumanLanguageImpl.MALAY.getTranslationFallbackLanguage());  // Indonesian is a "register" of Malay
        assertSame("Did not get the correct fallback language for Arabic Algerian", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_DZ.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Chineese Singapore", DefaultHumanLanguageImpl.CHINESE_SIMP,
                DefaultHumanLanguageImpl.CHINESE_SG.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Chineese Hong Kong", DefaultHumanLanguageImpl.CHINESE_TRAD,
                DefaultHumanLanguageImpl.CHINESE_HK.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for English Hong Kong", DefaultHumanLanguageImpl.ENGLISH_GB,
                DefaultHumanLanguageImpl.ENGLISH_HK.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for English Ireland", DefaultHumanLanguageImpl.ENGLISH_GB,
                DefaultHumanLanguageImpl.ENGLISH_IE.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Singapore", DefaultHumanLanguageImpl.ENGLISH_GB,
                DefaultHumanLanguageImpl.ENGLISH_SG.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for South Africa", DefaultHumanLanguageImpl.ENGLISH_GB,
                DefaultHumanLanguageImpl.ENGLISH_ZA.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for French Switzerland", DefaultHumanLanguageImpl.FRENCH,
                DefaultHumanLanguageImpl.FRENCH_CH.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for German Luxembourg", DefaultHumanLanguageImpl.GERMAN,
                DefaultHumanLanguageImpl.GERMAN_CH.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Italian Switzerland", DefaultHumanLanguageImpl.ITALIAN,
                DefaultHumanLanguageImpl.ITALIAN_CH.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Argentina", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_AR.getFallbackLanguage());

    }

    @Test
    public void testLanugageTag() {
        assertEquals("he", DefaultHumanLanguageImpl.HEBREW.getHttpLanguageCode());
        assertEquals("id", DefaultHumanLanguageImpl.INDONESIAN.getHttpLanguageCode());
        assertEquals("yi", DefaultHumanLanguageImpl.YIDDISH.getHttpLanguageCode());
        assertEquals("en-US", DefaultHumanLanguageImpl.ENGLISH.getHttpLanguageCode());
        assertEquals("zh-Hans-CN", DefaultHumanLanguageImpl.CHINESE_SIMP.getHttpLanguageCode());
        assertEquals("zh-Hant-TW", DefaultHumanLanguageImpl.CHINESE_TRAD.getHttpLanguageCode());
        assertEquals("zh-Hans-SG", DefaultHumanLanguageImpl.CHINESE_SG.getHttpLanguageCode());
        assertEquals("zh-Hant-HK", DefaultHumanLanguageImpl.CHINESE_HK.getHttpLanguageCode());

        assertEquals(TextDirection.RTL, DefaultHumanLanguageImpl.HEBREW.getDirection());
        assertEquals(TextDirection.RTL, DefaultHumanLanguageImpl.ARABIC_DZ.getDirection());
        assertEquals(TextDirection.RTL, DefaultHumanLanguageImpl.YIDDISH.getDirection());
        assertEquals(TextDirection.RTL, DefaultHumanLanguageImpl.ENGLISH_IL.getDirection());
        assertEquals(TextDirection.LTR, DefaultHumanLanguageImpl.ESPERANTO.getDirection());
    }
    
    @Test
    public void testDefaults() {
    	assertFalse(DefaultHumanLanguageImpl.HEBREW.isTestOnlyLanguage());
    	assertTrue(DefaultHumanLanguageImpl.HEBREW.isTranslatedLanguage());
    	assertFalse(DefaultHumanLanguageImpl.ENGLISH_IL.isTranslatedLanguage());

    	assertEquals("UTF-8", DefaultHumanLanguageImpl.HEBREW.getDefaultFileEncoding());
    	assertEquals("UTF-8", DefaultHumanLanguageImpl.HEBREW.getSystemEmailEncoding());
    	assertEquals("UTF-8", DefaultHumanLanguageImpl.HEBREW.getDefaultUserEmailEncoding());
    	assertFalse(DefaultHumanLanguageImpl.HEBREW.hasTurkicCaseFolding());

    	assertEquals("MS932", DefaultHumanLanguageImpl.JAPANESE.getDefaultFileEncoding());
    	assertEquals("ISO-2022-JP", DefaultHumanLanguageImpl.JAPANESE.getSystemEmailEncoding());
    	assertEquals("ISO-2022-JP", DefaultHumanLanguageImpl.JAPANESE.getDefaultUserEmailEncoding());
    	assertTrue(DefaultHumanLanguageImpl.TURKISH.hasTurkicCaseFolding());

    	assertEquals("Big5", DefaultHumanLanguageImpl.CHINESE_HK.getDefaultFileEncoding());
    	assertEquals("GB2312", DefaultHumanLanguageImpl.CHINESE_SG.getDefaultFileEncoding());

    }
}
