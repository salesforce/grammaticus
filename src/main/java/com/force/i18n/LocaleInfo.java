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

import java.io.Serializable;
import java.util.Locale;

/**
 * Additional meta-data about a locale.
 * @author stamm
 */
public interface LocaleInfo extends Serializable {
    /**
     * @return the locale referenced by this locale
     */
    Locale getLocale();

    /**
     * @return whether this locale should use eastern name order, which means that the name should be
     * formatted as "last first[ middle][ suffix]".
     */
    boolean useEasternNameOrder();

    /**
     * @return whether this locale should be considered "formal", in that you should always use the full
     * name when addressing a user.
     */
    boolean isFormal();
}