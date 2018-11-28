/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Builder for a {@link LocaleAttributesUtil} instance built from {@link LocaleInfo} instances. This is mostly syntactic sugar to make the
 * DefaultLocaleAttributesUtil#createLocaleAttributesUtil() easier to read.
 *
 * @author jared.pearson
 */
class LocaleAttributesUtilBuilder {
    private final ImmutableList.Builder<LocaleInfo> localeInfoListBuilder = ImmutableList.<LocaleInfo>builder();

    /**
     * Adds a new local info instances to the config.
     *
     * @return this
     */
    public LocaleAttributesUtilBuilder add(LocaleInfoBuilder localeInfoBuilder) {
        Preconditions.checkArgument(localeInfoBuilder != null, "localeInfoBuilder should not be null");

        final LocaleInfo localeInfo = localeInfoBuilder.build();
        if (localeInfo == null) {
            throw new IllegalStateException("Unable to add locale info since the localeInfoBuilder returned null: " + localeInfoBuilder.getClass().getName());
        }

        localeInfoListBuilder.add(localeInfo);
        return this;
    }

    /**
     * Builds a new {@link LocaleAttributesUtil} instance.
     */
    public LocaleAttributesUtil build() {
        final List<LocaleInfo> localeInfoList = localeInfoListBuilder.build();
        return new SimpleLocaleAttributesUtil(localeInfoList);
    }
}