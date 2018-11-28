/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import com.force.i18n.DefaultHumanLanguageImpl;

import junit.framework.TestCase;

/**
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
     * the fallback language is French, since we don't have labels translated
     * into French (Canadian).
     */
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
        assertSame("Did not get the correct fallback language for Malaysian", DefaultHumanLanguageImpl.INDONESIAN,
                DefaultHumanLanguageImpl.MALAY.getFallbackLanguage());  // Indonesian is a "register" of Malay
        assertNull("Did not get the correct fallback language for Malaysian",
                DefaultHumanLanguageImpl.MALAY.getTranslationFallbackLanguage());  // Indonesian is a "register" of Malay
        assertSame("Did not get the correct fallback language for Arabic Algerian", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_DZ.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Bahrain", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_BH.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Egypt", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_EG.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Iraq", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_IQ.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Jordan", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_IQ.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Kuwait", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_KW.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Lebanon", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_LB.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Libya", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_LY.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Morocco", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_MA.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Oman", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_OM.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Quatar", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_QA.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Saudi Arabia", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_SA.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Sudan", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_SD.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Sudan", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_SY.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Algerian", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_TN.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Algerian", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_AE.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Arabic Yemen", DefaultHumanLanguageImpl.ARABIC,
                DefaultHumanLanguageImpl.ARABIC_YE.getFallbackLanguage());
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
        assertSame("Did not get the correct fallback language for French Belgium", DefaultHumanLanguageImpl.FRENCH,
                DefaultHumanLanguageImpl.FRENCH_BE.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for French Luxembourg", DefaultHumanLanguageImpl.FRENCH,
                DefaultHumanLanguageImpl.FRENCH_LU.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for French Switzerland", DefaultHumanLanguageImpl.FRENCH,
                DefaultHumanLanguageImpl.FRENCH_CH.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for German Luxembourg", DefaultHumanLanguageImpl.GERMAN,
                DefaultHumanLanguageImpl.GERMAN_LU.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Italian Switzerland", DefaultHumanLanguageImpl.ITALIAN,
                DefaultHumanLanguageImpl.ITALIAN_CH.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Argentina", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_AR.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Bolivia", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_BO.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Chile", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_CL.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Colombia", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_CO.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Costa Rica", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_CR.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Dominican Republic", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_DO.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Ecuador", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_EC.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish El Salvador", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_SV.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Guatemala", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_GT.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Honduras", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_HN.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Nicaragua", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_NI.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Panama", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_PA.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Paraguay", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_PY.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Peru", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_PE.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Puerto Rico", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_PR.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish United States", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_US.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Uruguay", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_UY.getFallbackLanguage());
        assertSame("Did not get the correct fallback language for Spanish Venezuela", DefaultHumanLanguageImpl.SPANISH,
                DefaultHumanLanguageImpl.SPANISH_VE.getFallbackLanguage());


    }
}
