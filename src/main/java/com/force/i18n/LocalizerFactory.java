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
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

/**
 *  The real nitty-gritty of localization - every App should have 1 of these
 *  Note: this provides access to loaders, but not much else.
 */
public abstract class LocalizerFactory implements LocalizerProvider {

	private static LocalizerProvider INSTANCE;

	// TODO: Put this someplace else
	public static LocalizerProvider get() {
		return INSTANCE;
	}
	public static void set(LocalizerProvider factory) {
		INSTANCE = factory;
	}


    private static final AtomicReference<Locale> default_locale =
    		new AtomicReference<Locale>(Locale.US);  // TODO: Less 'mericun


	/* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLabelLocalizer(i18n.UserLanguage)
	 */
    @Override
	public BaseLocalizer getLabelLocalizer(HumanLanguage language) {
        return getLocalizer(language.getLocale(), language.getLocale(), language, TimeZone.getDefault());
    }



    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLocalizer(java.util.Locale)
	 */
    @Override
	public BaseLocalizer getLocalizer(Locale langLocale) {
        return getLocalizer(langLocale, null, // currency locale
            LanguageProviderFactory.get().getLanguageForLocale(langLocale), TimeZone.getDefault());
    }

    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLocalizer(i18n.UserLanguage)
	 */
    @Override
	public BaseLocalizer getLocalizer(HumanLanguage language) {
        return getLocalizer(language.getLocale(), null, // currency locale
            language, TimeZone.getDefault());
    }

    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getDefaultLocalizer()
	 */
    @Override
	public BaseLocalizer getDefaultLocalizer() {
        return getLocalizer(LocalizerFactory.getDefaultLocale(),
            LocalizerFactory.getDefaultLocale(),
            LocalizerFactory.getDefaultLanguage(), TimeZone.getDefault());
    }


    public static Locale getDefaultLocale() {
        return default_locale.get();
    }

    public static HumanLanguage getDefaultLanguage() {
    	return LanguageProviderFactory.get().getLanguageForLocale(default_locale.get());
    }


    @Override
	public BaseLocalizer getEnglishLocalizer() {
        return getLocalizer(Locale.US, Locale.US, HumanLanguage.Helper.get(Locale.US), TimeZone.getDefault());
    }

    /**
     * Return the translated name of the language.  This will be the same in all languages, as retrieved from
     * public_i18n.xml (With ENGLISH using the special "en" label instead of en_US)
     * @param language the language for whom the language should be retrieved
     * @return the name of the human language in that language
     */
    public static String getTranslatedLanguageLabel(HumanLanguage language) {
        assert language != null : "You cannot provide a null language to this method";
        BaseLocalizer loc = get().getDefaultLocalizer();
        String label = loc.getLabel("translatedlanguagename", language.getLabelKey());
        if (language.getDirection() != loc.getUserLanguage().getDirection()) {  // Only embed for languages where the direction differs
            label = language.getDirection().addEmbeddingMarks(label);
        }
        return label;
     }

}
