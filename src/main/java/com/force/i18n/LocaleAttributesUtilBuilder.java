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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Builder for a {@link LocaleAttributesUtil} instance built from {@link LocaleInfo} instances. This is mostly syntactic sugar to make the
 * DefaultLocaleAttributesUtil#createLocaleAttributesUtil() easier to read.
 * @author jared.pearson
 */
class LocaleAttributesUtilBuilder {
    private final ImmutableList.Builder<LocaleInfo> localeInfoListBuilder = ImmutableList.<LocaleInfo>builder();

    /**
     * Adds a new local info instances to the config.
     * @return this
     */
    public LocaleAttributesUtilBuilder add(LocaleInfoBuilder localeInfoBuilder) {
        Preconditions.checkArgument(localeInfoBuilder != null, "localeInfoBuilder should not be null");

        final LocaleInfo localeInfo = localeInfoBuilder.build();
        assert localeInfo != null;

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