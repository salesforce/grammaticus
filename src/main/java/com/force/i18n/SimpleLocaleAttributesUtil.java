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
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * {@link LocaleAttributesUtil} implementation that is configured with a collection of LocaleInfo instances.
 * This is useful for testing because locale information can be specified at creation (unlike {@link DefaultLocaleAttributesUtil}
 * which creates the information internally).
 * @author jared.pearson
 */
class SimpleLocaleAttributesUtil implements LocaleAttributesUtil {
    private final Set<Locale> easternNameOrderLocales;
    private final Set<Locale> formalLocales;

    /**
     * Builds a config from the locale info instances provided.
     */
    public SimpleLocaleAttributesUtil(LocaleInfo... localeInfos) {
        this(Lists.newArrayList(localeInfos));
    }

    /**
     * Builds a config from the locale info instances provided.
     */
    public SimpleLocaleAttributesUtil(Iterable<? extends LocaleInfo> localeInfos) {
        Preconditions.checkArgument(localeInfos != null, "localeInfos should not be null");
        final ImmutableSet.Builder<Locale> eastern = ImmutableSet.builder();
        final ImmutableSet.Builder<Locale> formal = ImmutableSet.builder();
        for (final LocaleInfo localeInfo : localeInfos) {
            if (localeInfo.useEasternNameOrder()) {
                eastern.add(localeInfo.getLocale());
            }
            if (localeInfo.isFormal()) {
                formal.add(localeInfo.getLocale());
            }
        }
        this.easternNameOrderLocales = eastern.build();
        this.formalLocales = formal.build();
    }

    @Override
    public boolean isFormalLocale(Locale locale) {
        return formalLocales.contains(locale);
    }

    @Override
    public boolean useEasternNameOrder(Locale locale) {
        return easternNameOrderLocales.contains(locale);
    }
}