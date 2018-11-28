/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Provider for a Localizer.
 *
 * @author stamm
 */
public interface LocalizerProvider {
    /**
     * NOTE: DO NOT USE THIS FOR CULTURE LOCALIZATION
     * This really should be deprecated
     *
     * @param language the language for the user
     * @return a localizer that <em>can only be used for labels</em>
     */
    BaseLocalizer getLabelLocalizer(HumanLanguage language);

    /**
     * Get a localizer based on a locale, a currency, a language and a timezone.
     */
    BaseLocalizer getLocalizer(Locale locale,
                               Locale currencyLocale, HumanLanguage language, TimeZone timeZone);

    /**
     * @param langLocale the localizer for the given locale, using that locale for language and localization
     * @return a localizer for the given locale
     */
    BaseLocalizer getLocalizer(Locale langLocale);

    BaseLocalizer getLocalizer(HumanLanguage language);

    LabelSet findLabelSet(HumanLanguage language);

    /**
     * @return the default localizer for this instance, generally English, unless this particular instance
     * of Grammaticus is set to a different default.
     * Use this if you have no idea what to display or you don't care too much; generally you do.
     */
    BaseLocalizer getDefaultLocalizer();

    /**
     * @return the localizer for US English.
     */
    BaseLocalizer getEnglishLocalizer();

    URL getLabelsDirectory();
}
