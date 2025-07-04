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

import java.util.Locale;

/**
 * Provides extra configuration about Locale instances configured from an internal definition. This is the
 * default used.
 * <p>
 * See #createLocaleAttributesUtil() to add more configuration information.
 * @author jared.pearson
 */
public enum DefaultLocaleAttributesUtil implements LocaleAttributesUtil {
    INSTANCE; // enums are modern singletons

    private final LocaleAttributesUtil delegate;

    private DefaultLocaleAttributesUtil() {
        this.delegate = createLocaleAttributesUtil();
    }

    /**
     * @return the singleton instance of the {@link LocaleAttributesUtil}.
     */
    public static DefaultLocaleAttributesUtil get() {
        return INSTANCE;
    }

    /**
     * @return true when the specified locale should be considered "formal", in that you should always use the full
     * name when addressing a user.
     */
    @Override
    public boolean isFormalLocale(Locale locale) {
        return delegate.isFormalLocale(locale);
    }

    /**
     * @return true when the specified locale uses an eastern name order.
     */
    @Override
    public boolean useEasternNameOrder(Locale locale) {
        return delegate.useEasternNameOrder(locale);
    }

    /**
     * Creates the default utility for Locale information. This is only executed once and saved as
     * {@link DefaultLocaleAttributesUtil#delegate}.
     */
    private static LocaleAttributesUtil createLocaleAttributesUtil() {

        /*
         * When adding or changing Locale information below, make sure to update the LocaleConfigImplUnitTest.
         */

        return new LocaleAttributesUtilBuilder()
            .add(forLocale("hu").useEasternNameOrder())
            .add(forLocale("hu", "HU").useEasternNameOrder().formal())
            .add(forLocale("in").formal())
            .add(forLocale("in", "ID").formal())
            .add(forLocale("ja").useEasternNameOrder().formal())
            .add(forLocale("ja", "JP").useEasternNameOrder().formal())
            .add(forLocale("ko").useEasternNameOrder().formal())
            .add(forLocale("ko", "KP").useEasternNameOrder())
            .add(forLocale("ko", "KR").useEasternNameOrder().formal())
            .add(forLocale("th").formal())
            .add(forLocale("th", "TH").formal())
            .add(forLocale("vi").useEasternNameOrder().formal())
            .add(forLocale("vi", "VN").useEasternNameOrder().formal())
            .add(forLocale("zh").useEasternNameOrder().formal())
            .add(forLocale("zh", "CN").useEasternNameOrder().formal())
            .add(forLocale("zh", "HK").useEasternNameOrder().formal())
            .add(forLocale("zh", "TW").useEasternNameOrder().formal())
            .add(forLocale("zh", "MO").useEasternNameOrder().formal())
            .add(forLocale("zh", "SG").useEasternNameOrder().formal())
            .add(forLocale("zh", "CN", "STROKE").useEasternNameOrder().formal())
            .add(forLocale("zh", "TW", "STROKE").useEasternNameOrder().formal())
            .add(forLocale("zh", "HK", "STROKE").useEasternNameOrder().formal())
            .add(forLocale("zh", "CN", "PINYIN").useEasternNameOrder().formal())
            .build();
    }

    /**
     * Creates a new LocaleInfoBuilder for the Locale with the given language. This is mostly syntactic sugar to make
     * {@link DefaultLocaleAttributesUtil#createLocaleAttributesUtil()} easier to read.
     * @param language the language for {@link Locale#Locale(String)}.
     * @return a new {@link LocaleInfoBuilder} instance
     */
    private static LocaleInfoBuilder forLocale(String language) {
        return new LocaleInfoBuilder(new Locale(language));
    }

    /**
     * Creates a new LocaleInfoBuilder for the Locale with the given language and country. This is mostly syntactic sugar to make
     * {@link DefaultLocaleAttributesUtil#createLocaleAttributesUtil()} easier to read.
     * @param language the language for {@link Locale#Locale(String, String)}.
     * @param country the country for {@link Locale#Locale(String, String)}.
     * @return a new {@link LocaleInfoBuilder} instance
     */
    private static LocaleInfoBuilder forLocale(String language, String country) {
        return new LocaleInfoBuilder(new Locale(language, country));
    }

    /**
     * Creates a new LocaleInfoBuilder for the Locale with the given language, country and variant. This is mostly syntactic sugar to make
     * {@link DefaultLocaleAttributesUtil#createLocaleAttributesUtil()} easier to read.
     * @param language the language for {@link Locale#Locale(String, String, String)}.
     * @param country the country for {@link Locale#Locale(String, String, String)}.
     * @param variant the variant for {@link Locale#Locale(String, String, String)}.
     * @return a new {@link LocaleInfoBuilder} instance
     */
    private static LocaleInfoBuilder forLocale(String language, String country, String variant) {
        return new LocaleInfoBuilder(new Locale(language, country, variant));
    }
}
