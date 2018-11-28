/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to a "renaming" provider that is global to the application
 *
 * @author stamm
 */
public enum LanguageProviderFactory implements LanguageProvider {
    INSTANCE;

    private final AtomicReference<LanguageProvider> providerRef = new AtomicReference<LanguageProvider>(); // Default is nothing
    private final Logger logger = Logger.getLogger(LanguageProviderFactory.class.getName());

    private LanguageProviderFactory() {
        try {
            String provider = I18nJavaUtil.getProperty("LanguageProvider");
            if (provider != null) {
                try {
                    providerRef.set(Class.forName(provider).asSubclass(LanguageProvider.class).newInstance());
                    return;
                } catch (InstantiationException | IllegalAccessException
                        | ClassNotFoundException e) {
                    logger.log(Level.INFO, "Couldn't find provider", e);
                }
            }
        } catch (MissingResourceException ex) {
            // ignore
        }
        providerRef.set(new DefaultHumanLanguageImpl.DefaultHumanLanguageImplProvider());
    }

    public static LanguageProviderFactory get() {
        return INSTANCE;
    }

    public void setProvider(LanguageProvider provider) {
        this.providerRef.set(provider);
    }

    public LanguageProvider getProvider() {
        return this.providerRef.get();
    }

    @Override
    public List<? extends HumanLanguage> getAll() {
        return getProvider().getAll();
    }

    @Override
    public HumanLanguage getBaseLanguage() {
        return getProvider().getBaseLanguage();
    }

    @Override
    public HumanLanguage getLanguage(Locale loc) {
        return getProvider().getLanguage(loc);
    }

    @Override
    public HumanLanguage getLanguage(String isoCode) {
        return getProvider().getLanguage(isoCode);
    }

    @Override
    public boolean isSupportedLanguageLocale(Locale loc) {
        return getProvider().isSupportedLanguageLocale(loc);
    }

    @Override
    public HumanLanguage getLanguageForLocale(String localeString) {
        return getProvider().getLanguageForLocale(localeString);
    }

    @Override
    public HumanLanguage getLanguageForLocale(Locale loc) {
        return getProvider().getLanguageForLocale(loc);
    }

    @Override
    public <L extends HumanLanguage, T> Map<L, T> getNewMap() {
        return getProvider().getNewMap();
    }

}