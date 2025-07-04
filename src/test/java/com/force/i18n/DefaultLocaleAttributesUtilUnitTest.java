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

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.force.i18n.commons.text.TextUtil;
import com.google.common.collect.Lists;

/**
 * Unit tests for {@link DefaultLocaleAttributesUtil}
 * @author jared.pearson
 */
public class DefaultLocaleAttributesUtilUnitTest extends TestCase {

    private static List<Locale> EASTERN_NAME_ORDERED_LOCALES = Lists.newArrayList(
            new Locale.Builder().setLanguage("hu").build(),
            new Locale.Builder().setLanguage("hu").setRegion("HU").build(),
            new Locale.Builder().setLanguage("ja").build(),
            new Locale.Builder().setLanguage("ja").setRegion("JP").build(),
            new Locale.Builder().setLanguage("ko").build(),
            new Locale.Builder().setLanguage("ko").setRegion("KP").build(),
            new Locale.Builder().setLanguage("ko").setRegion("KR").build(),
            new Locale.Builder().setLanguage("vi").build(),
            new Locale.Builder().setLanguage("vi").setRegion("VN").build(),
            new Locale.Builder().setLanguage("zh").build(),
            new Locale.Builder().setLanguage("zh").setRegion("CN").build(),
            new Locale.Builder().setLanguage("zh").setRegion("HK").build(),
            new Locale.Builder().setLanguage("zh").setRegion("TW").build(),
            new Locale.Builder().setLanguage("zh").setRegion("MO").build(),
            new Locale.Builder().setLanguage("zh").setRegion("SG").build(),
            new Locale.Builder().setLanguage("zh").setRegion("CN").setVariant("STROKE").build(),
            new Locale.Builder().setLanguage("zh").setRegion("TW").setVariant("STROKE").build(),
            new Locale.Builder().setLanguage("zh").setRegion("HK").setVariant("STROKE").build(),
            new Locale.Builder().setLanguage("zh").setRegion("CN").setVariant("PINYIN").build());

    private static List<Locale> FORMAL_LOCALES = Lists.newArrayList(
            new Locale.Builder().setLanguage("hu").setRegion("HU").build(),
            new Locale.Builder().setLanguage("in").build(),
            new Locale.Builder().setLanguage("in").setRegion("ID").build(),
            new Locale.Builder().setLanguage("ja").build(),
            new Locale.Builder().setLanguage("ja").setRegion("JP").build(),
            new Locale.Builder().setLanguage("ko").build(),
            new Locale.Builder().setLanguage("ko").setRegion("KR").build(),
            new Locale.Builder().setLanguage("th").build(),
            new Locale.Builder().setLanguage("th").setRegion("TH").build(),
            new Locale.Builder().setLanguage("vi").build(),
            new Locale.Builder().setLanguage("vi").setRegion("VN").build(),
            new Locale.Builder().setLanguage("zh").build(),
            new Locale.Builder().setLanguage("zh").setRegion("CN").build(),
            new Locale.Builder().setLanguage("zh").setRegion("HK").build(),
            new Locale.Builder().setLanguage("zh").setRegion("TW").build(),
            new Locale.Builder().setLanguage("zh").setRegion("MO").build(),
            new Locale.Builder().setLanguage("zh").setRegion("SG").build(),
            new Locale.Builder().setLanguage("zh").setRegion("CN").setVariant("STROKE").build(),
            new Locale.Builder().setLanguage("zh").setRegion("TW").setVariant("STROKE").build(),
            new Locale.Builder().setLanguage("zh").setRegion("HK").setVariant("STROKE").build(),
            new Locale.Builder().setLanguage("zh").setRegion("CN").setVariant("PINYIN").build());

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
