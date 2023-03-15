/*
 * Copyright (c) 2023, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.nio.file.Path;
import java.time.Duration;
import java.util.MissingResourceException;

import com.force.i18n.I18nJavaUtil;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.GrammaticalLabelSetProvider;
import com.force.i18n.settings.BasePropertyFile;

public class LabelSetLoaderConfig {
    public static final String CACHE_DIR = "cacheDir";
    public static final String USE_TRANSLATED_LANG = "useTranslatedLanguage";
    public static final String SKIP_PARSING_PLATFORM = "skipParsingLabelForPlatformLanguage";
    public static final String RECORD_STATS = "loader.cache.stats";
    public static final String LOADER_EXPIRE_AFTER = "loader.cache.expireAfter";
    public static final String LOADER_MAX_SIZE = "loader.cache.maxSize";

    private final GrammaticalLabelSetDescriptor desc;
    private final GrammaticalLabelSetProvider parent;

    private Path cacheDir;
    private boolean useTranslatedLanguage;
    private boolean skipParsingLabelForPlatform;
    private boolean isCacheStatsEnabled;
    private Duration cacheExpireAfter; // expiration in minues
    private long cacheMaxSize; // max allowed entires

    public LabelSetLoaderConfig(GrammaticalLabelSetDescriptor baseDesc, GrammaticalLabelSetProvider parent) {
        this.desc = baseDesc;
        this.parent = parent;

        // setup defaults from properties
        setCacheDir(Path.of(getProperty(CACHE_DIR)));
        setTranslatedLanguage(BasePropertyFile.stringToBoolean(getProperty(USE_TRANSLATED_LANG)));
        setSkipParsingLabelForPlatform(BasePropertyFile.stringToBoolean(getProperty(SKIP_PARSING_PLATFORM)));
        setCacheStatsEnabled(BasePropertyFile.stringToBoolean(getProperty(RECORD_STATS)));
        setCacheExpireAfter(Duration.ofMinutes(getPropertyLong(LOADER_EXPIRE_AFTER)));
        setCacheMaxSize(getPropertyLong(LOADER_MAX_SIZE));
    }

    public LabelSetLoaderConfig(LabelSetLoaderConfig copyFrom) {
        this(copyFrom.getDescriptor(), copyFrom.getParent());

        setCacheDir(copyFrom.getCacheDir());
        setTranslatedLanguage(copyFrom.useTranslatedLanguage());
        setSkipParsingLabelForPlatform(copyFrom.skipParsingLabelForPlatform());
        setCacheStatsEnabled(copyFrom.isCacheStatsEnabled());
        setCacheExpireAfter(copyFrom.getCacheExpireAfter());
        setCacheMaxSize(copyFrom.getCacheMaxSize());
    }

    public static String getProperty(String prop) {
        try {
            return I18nJavaUtil.getProperty(prop);
        } catch (MissingResourceException ignore) {
            return null;
        }
    }

    public static long getPropertyLong(String prop) {
        String s = getProperty(prop);
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    public GrammaticalLabelSetDescriptor getDescriptor() {
        return this.desc;
    }

    public GrammaticalLabelSetProvider getParent() {
        return this.parent;
    }

    public LabelSetLoaderConfig setCacheDir(Path dir) {
        this.cacheDir = dir;
        return this;
    }
    public Path getCacheDir() {
        return this.cacheDir;
    }

    public LabelSetLoaderConfig setTranslatedLanguage(boolean newValue) {
        this.useTranslatedLanguage = newValue;
        return this;
    }

    public boolean useTranslatedLanguage() {
        return useTranslatedLanguage;
    }

    public LabelSetLoaderConfig setSkipParsingLabelForPlatform(boolean newValue) {
        this.skipParsingLabelForPlatform = newValue;
        return this;
    }

    public boolean skipParsingLabelForPlatform() {
        return this.skipParsingLabelForPlatform;
    }

    public boolean isCacheStatsEnabled() {
        return this.isCacheStatsEnabled;
    }

    public LabelSetLoaderConfig setCacheStatsEnabled(boolean newVal) {
        this.isCacheStatsEnabled = newVal;
        return this;
    }

    public Duration getCacheExpireAfter() {
        return this.cacheExpireAfter;
    }

    public LabelSetLoaderConfig setCacheExpireAfter(Duration newVal) {
        this.cacheExpireAfter = newVal;
        return this;
    }

    public long getCacheMaxSize() {
        return this.cacheMaxSize;
    }

    public LabelSetLoaderConfig setCacheMaxSize(long newVal) {
        this.cacheMaxSize = newVal < 0 ? 0 : newVal;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("stats=").append(this.isCacheStatsEnabled)
                .append(", expire=").append(this.cacheExpireAfter)
                .append(", size=").append(this.cacheMaxSize);
        return sb.toString();
    }
}
