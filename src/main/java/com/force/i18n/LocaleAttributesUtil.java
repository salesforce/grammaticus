/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.Locale;

/**
 * Provides extra attributes about Locale instances.
 *
 * @author jared.pearson
 */
public interface LocaleAttributesUtil {
    /**
     * @return true when the specified locale should be considered "formal", in that you should always use the full
     * name when addressing a user.
     */
    public boolean isFormalLocale(Locale locale);

    /**
     * @return true when the specified locale uses an eastern name order.
     */
    public boolean useEasternNameOrder(Locale locale);

}
