/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.force.i18n.commons.text.TextUtil;
import com.google.common.collect.Lists;

/**
 * Unit tests for {@link DefaultLocaleAttributesUtil}
 *
 * @author jared.pearson
 */
public class DefaultLocaleAttributesUtilUnitTest extends TestCase {

    private static List<Locale> EASTERN_NAME_ORDERED_LOCALES = Lists.newArrayList(
            new Locale("hu"),
            new Locale("hu", "HU"),
            new Locale("ja"),
            new Locale("ja", "JP"),
            new Locale("ko"),
            new Locale("ko", "KP"),
            new Locale("ko", "KR"),
            new Locale("vi"),
            new Locale("vi", "VN"),
            new Locale("zh"),
            new Locale("zh", "CN"),
            new Locale("zh", "HK"),
            new Locale("zh", "TW"),
            new Locale("zh", "MO"),
            new Locale("zh", "SG"),
            new Locale("zh", "CN", "STROKE"),
            new Locale("zh", "TW", "STROKE"),
            new Locale("zh", "HK", "STROKE"),
            new Locale("zh", "CN", "PINYIN"));

    private static List<Locale> FORMAL_LOCALES = Lists.newArrayList(
            new Locale("hu", "HU"),
            new Locale("in"),
            new Locale("in", "ID"),
            new Locale("ja"),
            new Locale("ja", "JP"),
            new Locale("ko"),
            new Locale("ko", "KR"),
            new Locale("th"),
            new Locale("th", "TH"),
            new Locale("vi"),
            new Locale("vi", "VN"),
            new Locale("zh"),
            new Locale("zh", "CN"),
            new Locale("zh", "HK"),
            new Locale("zh", "TW"),
            new Locale("zh", "MO"),
            new Locale("zh", "SG"),
            new Locale("zh", "CN", "STROKE"),
            new Locale("zh", "TW", "STROKE"),
            new Locale("zh", "HK", "STROKE"),
            new Locale("zh", "CN", "PINYIN"));

    /**
     * Verify that the isFormal method returns true for all of the given formal Locales
     */
    public void testIsFormal() throws Exception {
        final List<Locale> failedLocales = Lists.newArrayList();
        for (Locale locale : FORMAL_LOCALES) {
            if (!DefaultLocaleAttributesUtil.get().isFormalLocale(locale)) {
                failedLocales.add(locale);
            }
        }

        if (!failedLocales.isEmpty()) {
            fail("Expected the following Locales to be formal: " + TextUtil.join(", ", failedLocales));
        }
    }

    /**
     * Verify that the isFormal method returns true for all of the given formal Locales
     */
    public void testUseEasternNameOrder() throws Exception {
        final List<Locale> failedLocales = Lists.newArrayList();
        for (Locale locale : EASTERN_NAME_ORDERED_LOCALES) {
            if (!DefaultLocaleAttributesUtil.get().useEasternNameOrder(locale)) {
                failedLocales.add(locale);
            }
        }

        if (!failedLocales.isEmpty()) {
            fail("Expected the following Locales to use eastern name order: " + TextUtil.join(", ", failedLocales));
        }
    }
}
