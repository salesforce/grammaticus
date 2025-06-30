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

import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Provider for a Localizer.
 * @author stamm
 */
public interface LocalizerProvider {
	/**
	 * NOTE: DO NOT USE THIS FOR CULTURE LOCALIZATION
	 * This really should be deprecated
	 * @param language the language for the user
	 * @return a localizer that <em>can only be used for labels</em>
	 */
	BaseLocalizer getLabelLocalizer(HumanLanguage language);

	/**
	 * @return a localizer based on a locale, a currency, a language and a timezone.
	 * @param locale the locale for number and date formatting
	 * @param currencyLocale the locale for currencies
	 * @param language the human language
	 * @param timeZone the time zone
	 *
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
