/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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
 *
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