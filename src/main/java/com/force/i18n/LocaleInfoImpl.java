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
 * Basic implementation of the {@link LocaleInfo}
 * @author jared.pearson
 */
class LocaleInfoImpl implements LocaleInfo {
    private static final long serialVersionUID = 1264587819352292136L;
    private final Locale locale;
    private final boolean useEasternNameOrder;
    private final boolean formal;

    public LocaleInfoImpl(Locale locale, boolean useEasternNameOrder, boolean formal) {
        assert locale != null : "locale should not be null";
        this.locale = locale;
        this.useEasternNameOrder = useEasternNameOrder;
        this.formal = formal;
    }

    @Override
    public boolean useEasternNameOrder() {
        return this.useEasternNameOrder;
    }

    @Override
    public boolean isFormal() {
        return this.formal;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocaleInfoImpl other = (LocaleInfoImpl) obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }

}