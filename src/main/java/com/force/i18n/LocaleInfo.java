/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * Additional meta-data about a locale.
 *
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