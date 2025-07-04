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
 * Builder of LocaleInfo instances.
 * @author jared.pearson
 */
class LocaleInfoBuilder {
    private final Locale locale;
    private boolean useEasternNameOrder = false;
    private boolean formal = false;

    /**
     * Creates a new {@link LocaleInfo} instance for the given Locale. The properties are set as follows:
     * <ul>
     * <li>useEasterNameOrder: false
     * <li>formal: false
     * </ul>
     * @param locale the Locale associated to the {@link LocaleInfo} being built
     */
    public LocaleInfoBuilder(Locale locale) {
        assert locale != null : "locale should not be null";
        this.locale = locale;
    }

    /**
     * Changes the {@link LocaleInfo#useEasternNameOrder()} value to true
     * @return this
     */
    public LocaleInfoBuilder useEasternNameOrder() {
        return this.useEasternNameOrder(true);
    }

    /**
     * Changes the {@link LocaleInfo#useEasternNameOrder()} value
     * @return this
     */
    public LocaleInfoBuilder useEasternNameOrder(boolean useEasternNameOrder) {
        this.useEasternNameOrder = useEasternNameOrder;
        return this;
    }

    /**
     * Changes the {@link LocaleInfo#isFormal()} value to true
     * @return this
     */
    public LocaleInfoBuilder formal() {
        return this.formal(true);
    }

    /**
     * Changes the {@link LocaleInfo#isFormal()} value
     * @return this
     */
    public LocaleInfoBuilder formal(boolean formal) {
        this.formal = formal;
        return this;
    }

    /**
     * Builds a new {@link LocaleInfo} instance
     */
    public LocaleInfo build() {
        return new LocaleInfoImpl(locale, useEasternNameOrder, formal);
    }
}