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

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.settings.*;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.*;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * GrammaticalLabelSetLoader which loads GrammaticalLabelSets from one or more files, and a dictionary file.
 * <p>
 * The labelset is stored in one or more XML files with the ini-style schema. The dictionary is in an XML file, called
 * names.xml. All of these files are organized in the filesystem relative to the locale to which they apply.
 * <p>
 * This Loader implements an in-memory cache map to prevent loading the same labelset more than once.
 * <p>
 * This uses an in-memory cache for the dictionaries.
 * <p>
 * This has optional support for a parentLoader, however it will share a common dictionary per language
 * if you use it (for performance reasons).
 *
 * @author nveeser,stamm
 */
public class GrammaticalLabelSetLoader implements GrammaticalLabelSetProvider {
    private static final Logger logger = Logger.getLogger(GrammaticalLabelSetLoader.class.getName());

    private static final boolean USE_SHARED_KEYS_DEFAULT = true;  // You really want this, so it isn't an option

    //@GuardedBy("itself") // this is passed as seed data to multiple threads and modifications need to be synchronized
    private final SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap;

    private final GrammaticalLabelSetProvider parentProvider;
    private final GrammaticalLabelSetDescriptor baseDesc;
    private final LoadingCache<GrammaticalLabelSetDescriptor, GrammaticalLabelSet> cache;

    // These leak to the computable above.
    protected final boolean useSharedKeys;
    protected final Set<String> publicSections = new HashSet<>();

    // respect HumanLanguage#isTranslatedLanguage. see #compute(GrammaticalLabelSetDescriptor) how it's used.
    private boolean useTranslatedLanguage;

    // used only if useTranslatedLanguage is ture. see #compute(GrammaticalLabelSetDescriptor) how it's used.
    private boolean skipParsingLabelForPlatform  = false;

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public void initEnglish() {
        init();
    }

    /**
     * Used only for debugging purposes.
     */
    @Override
    public void resetMap() {
        // invalidate everything, include parent.
        resetMap(null, true);
    }

    /**
     * Utility method to invalidate cached data. After calling this method, the next {@link #getSet(HumanLanguage)} call
     * will load label data from the source XML file.
     *
     * @param languages
     *            collection of languages to remove from cache. if {@code languages} is {@code null} or
     *            {@code languages.isEmpty()} is {@code true}, removes everything from cache.
     * @param resetParent
     *            if {@code true}, also calls parent provider to reset, {@code false} otherwise.
     */
    public void resetMap(Collection<? extends HumanLanguage> languages, boolean resetParent) {
        if (resetParent && parentProvider != null) {
            // Invalidate parent loader cache.
            if (parentProvider instanceof GrammaticalLabelSetLoader) {
                ((GrammaticalLabelSetLoader)parentProvider).resetMap(languages, resetParent);
            } else {
                parentProvider.resetMap();
            }
        }

        if (languages == null || languages.isEmpty()) {
            cache.invalidateAll();
        } else {
            for (HumanLanguage lang: languages) {
                cache.invalidate(getDescriptor(lang));
            }
        }
    }

    /**
     * @deprecated use {@link #GrammaticalLabelSetLoader(GrammaticalLabelSetDescriptor)}
     */
    @Deprecated
    public GrammaticalLabelSetLoader(URL baseDir, String labelSetName) {
        this(baseDir, labelSetName, null);
    }

    public GrammaticalLabelSetLoader(URL baseDir, String labelSetName, GrammaticalLabelSetProvider parent) {
        this(new LabelSetDescriptorImpl(baseDir, LanguageProviderFactory.get().getBaseLanguage(), labelSetName), parent);
    }

    public GrammaticalLabelSetLoader(GrammaticalLabelSetDescriptor baseDesc) {
        this(baseDesc, USE_SHARED_KEYS_DEFAULT, null);
    }

    public GrammaticalLabelSetLoader(GrammaticalLabelSetDescriptor baseDesc, GrammaticalLabelSetProvider parent) {
        this(baseDesc, USE_SHARED_KEYS_DEFAULT, parent);
    }

    public GrammaticalLabelSetLoader(GrammaticalLabelSetDescriptor baseDesc, boolean useSharedKeys, GrammaticalLabelSetProvider parent) {
        this(new LabelSetLoaderConfig(baseDesc, parent), useSharedKeys);
    }

    public GrammaticalLabelSetLoader(LabelSetLoaderConfig config) {
        this(config, USE_SHARED_KEYS_DEFAULT);
    }

    public GrammaticalLabelSetLoader(LabelSetLoaderConfig config, boolean useSharedKeys) {
        this.baseDesc = config.getDescriptor();
        this.useSharedKeys = useSharedKeys;
        this.parentProvider = config.getParent();
        setUseTranslatedLanguage(config.useTranslatedLanguage());
        setSkipParsingLabelForPlatform(config.skipParsingLabelForPlatform());

        // Share the keys of the parent loader if possible
        if (this.useSharedKeys) {
            if (parentProvider instanceof GrammaticalLabelSetLoader && ((GrammaticalLabelSetLoader) parentProvider).useSharedKeys) {
                this.seedKeyMap = new SharedKeyMap<>(((GrammaticalLabelSetLoader) parentProvider).seedKeyMap);
            } else {
                this.seedKeyMap = new SharedKeyMap<>();
            }
        } else {
            this.seedKeyMap = null;
        }

        this.cache = initCache(config);
    }

    /**
     * initialize internal cache that holds {@link GrammaticalLabelSet}.
     *
     * @param config a configuration to initialize this loader
     * @return a {@code LoadingCache} object to use as a cache
     */
    protected LoadingCache<GrammaticalLabelSetDescriptor, GrammaticalLabelSet> initCache(LabelSetLoaderConfig config) {
        // switch between Caffeine / Guava based on the config
        return config.useCaffeine()
                ? CaffeinatedGuava.build(getCaffeineCacheBuilder(config), getCacheLoader())
                : getGuavaCacheBuilder(config).build(getCacheLoader());
    }

    /**
     * @param config
     *            a configuration to initialize this loader. if the configuration is set to use Guava, the
     *            {@link UnsupportedOperationException} will be thrown.
     * @return Caffeine instance to build a cache
     * @deprecated This method is no longer used. In order to obtain a cache builder to modify cache settings, use
     *             {@link #getCaffeineCacheBuilder(LabelSetLoaderConfig)} for Caffeine, or
     *             {@link #getGuavaCacheBuilder(LabelSetLoaderConfig)} for Guava.
     * @see #initCache(LabelSetLoaderConfig)
     * @see LabelSetLoaderConfig#setCaffeine(boolean)
     */
    @Deprecated(since = "1.2.27", forRemoval = true)
    protected Caffeine<Object, Object> getCacheBuilder(LabelSetLoaderConfig config) {
        if (!config.useCaffeine()) throw new UnsupportedOperationException();
        return getCaffeineCacheBuilder(config);
    }

    /**
     * Return a Caffeine-based cache builder instance.
     * Override this method to replace, or edit cache option.
     *
     * @param config a config to initialize this loader
     * @return Caffeine-based builder
     * @see #initCache(LabelSetLoaderConfig)
     */
    protected Caffeine<Object, Object> getCaffeineCacheBuilder(LabelSetLoaderConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder().initialCapacity(64);

        Duration expiration = config.getCacheExpireAfter();
        if (!expiration.isZero() && !expiration.isNegative()) builder.expireAfterAccess(expiration);

        long maxSize = config.getCacheMaxSize();
        if (maxSize > 0) builder.maximumSize(maxSize);

        if (config.isCacheStatsEnabled()) {
            builder.recordStats();
        }
        return builder;
    }

    /**
     * Return a Guava-based cache builder instance.
     * Override this method to replace, or edit cache option.
     *
     * @param config a config to initialize this loader
     * @return Guava-based builder
     * @see #initCache(LabelSetLoaderConfig)
     */
    protected CacheBuilder<Object, Object> getGuavaCacheBuilder(LabelSetLoaderConfig config) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().initialCapacity(64);

        Duration expiration = config.getCacheExpireAfter();
        if (!expiration.isZero() && !expiration.isNegative()) builder.expireAfterAccess(expiration);

        long maxSize = config.getCacheMaxSize();
        if (maxSize > 0) builder.maximumSize(maxSize);

        if (config.isCacheStatsEnabled()) {
            builder.recordStats();
        }
        return builder;
    }


    protected CacheLoader<GrammaticalLabelSetDescriptor, GrammaticalLabelSet> getCacheLoader() {
        return new CacheLoader<GrammaticalLabelSetDescriptor, GrammaticalLabelSet>() {
            @Override
            public GrammaticalLabelSet load(GrammaticalLabelSetDescriptor desc) throws Exception {
                return GrammaticalLabelSetLoader.this.makeSet(desc);
            }
        };
    }

    protected GrammaticalLabelSet makeSet(GrammaticalLabelSetDescriptor desc) throws IOException {
        if (parentProvider != null) {
            GrammaticalLabelSet parentSet = parentProvider.getSet(desc.getLanguage());
            return new GrammaticalLabelSetFallbackImpl(compute(desc), parentSet);
        }
        return compute(desc);
    }

    protected GrammaticalLabelSet compute(GrammaticalLabelSetDescriptor desc) throws IOException {
        HumanLanguage lang = desc.getLanguage();
        long start = System.currentTimeMillis();

        GrammaticalLabelSet result = null;
        if (this.useTranslatedLanguage && !lang.isTranslatedLanguage()) {
            // this is for platform languages. if the requested language has no translation, try creating a (shallow) copy of
            // LabelSet from its fallback.
            HumanLanguage fallbackLang = lang.getFallbackLanguage();
            GrammaticalLabelSet fallback = getSetByDescriptor(desc.getForOtherLanguage(fallbackLang));

            if (LanguageDeclensionFactory.get().isForwardingProxy(lang)) {
                // simply copy all data (except language) from the fallback dictionary
                LanguageDictionary dictionary = new LanguageDictionary(lang, fallback.getDictionary());

                // use copy constructor for requested language
                result = new GrammaticalLabelSetImpl(dictionary, fallback);

            } else if (this.skipParsingLabelForPlatform) {
                // load dictionary because this language has unique declension
                LanguageDictionaryParser dictParser = new LanguageDictionaryParser(desc, createNewDictionary(lang), this.parentProvider);
                LanguageDictionary dictionary = dictParser.getDictionary();

                // use copy constructor for requested language
                result = new GrammaticalLabelSetImpl(dictionary, fallback);
            } else {
                // load both dictionary and labels
                result = loadLabels(desc);
            }
        } else {
            // load both dictionary and labels
            result = loadLabels(desc);
        }

        logger.fine(this.getClass().getSimpleName() + ": " + desc.getLabelSetName() + ":  Created LabelSet."
                + lang + " in " + (System.currentTimeMillis() - start) + " ms. ("
                + (desc.getDictionaryFile() != null ? desc.getDictionaryFile().getPath() : "") + ")");

        return result;
    }

    /**
     * Ger a new LanguageDictionary instance. If need some special, override this to return a subclass of
     * LanguageDictionary.
     *
     * @param language
     *            the language
     */
    protected LanguageDictionary createNewDictionary(HumanLanguage language) throws IOException {
        // default : using default LanguageDictionary
        return new LanguageDictionary(language);
    }

    /**
     * Finalize the dictionary after loading. If need something special (e.g. write some data to a file), override this
     * method.
     *
     *
     * @param dictionary the dictionary to be finalized.
     * @return the finalized dictionary.
     */

    protected LanguageDictionary finalizeDictionary(LanguageDictionary dictionary) throws IOException {
        // default : do nothing
        return dictionary;
    }

    protected GrammaticalLabelSet loadLabels(GrammaticalLabelSetDescriptor desc) throws IOException {
        HumanLanguage lang = desc.getLanguage();

        // dictionaries are always unique for every language because it may use different LanguageDeclension
        LanguageDictionaryParser dictParser = new LanguageDictionaryParser(desc, createNewDictionary(lang), this.parentProvider);
        LanguageDictionary dictionary = finalizeDictionary(dictParser.getDictionary());

        // all standard/end-user languages comes here. Create a parser to read from XML files.
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dictionary, desc, this.parentProvider);

        PropertyFileData propertyFileData = GrammaticalLabelSetLoader.this.useSharedKeys
                ? new SharedKeyMapPropertyFileData(lang.getLocale(), !desc.hasOverridingFiles(), seedKeyMap, publicSections)
                : new MapPropertyFileData(lang.getLocale());

        GrammaticalLabelSet result = new GrammaticalLabelSetImpl(parser.getDictionary(), parser, propertyFileData);
        if (useSharedKeys) {
            ((SharedKeyMapPropertyFileData) propertyFileData).compact();
        }
        if (result instanceof GrammaticalLabelSetImpl) {
            ((GrammaticalLabelSetImpl)result).setLabelSectionToFilename(parser.getSectionToFileName());
        }
        return result;
    }

    /**
     * @return a LabelSet based on the supplied descriptor.
     * @param desc the descriptor to load
     */
    protected GrammaticalLabelSet getSetByDescriptor(GrammaticalLabelSetDescriptor desc) {
        try {
            HumanLanguage fallbackLang = desc.getLanguage().getFallbackLanguage();
            if (fallbackLang != null) {
                // Always load fallbacks first. Note, the cache never includes GrammaticalLabelSetFallbackImpl
                GrammaticalLabelSet fallback = getSetByDescriptor(desc.getForOtherLanguage(fallbackLang));
                return new GrammaticalLabelSetFallbackImpl(cache.get(desc), fallback);
            } else {
                return cache.get(desc); // English only!
            }
        } catch (UncheckedExecutionException | ExecutionException e) {
            throw new RuntimeException("Unable to load label set for " + desc
                    + ". This may be caused by bad label/names file. Check application log and search '###\\tError:' for more detail.",
                    e);
        }
    }

    /**
     * @param userLanguage a language to load from label dictionary and data.
     * @return {@link GrammaticalLabelSet} object for a given {@code userLanguage}
     */
    @Override
    public GrammaticalLabelSet getSet(HumanLanguage userLanguage) {
        return getSetByDescriptor(getDescriptor(userLanguage));
    }

    public GrammaticalLabelSetDescriptor getBaseDesc() {
        return this.baseDesc;
    }

    protected SharedKeyMap<String, SharedKeyMap<String, Object>> getSeedKeyMap() {
        return this.seedKeyMap;
    }

    public GrammaticalLabelSetProvider getParent() {
        return this.parentProvider;
    }

    protected LoadingCache<GrammaticalLabelSetDescriptor, GrammaticalLabelSet> getCache() {
        return this.cache;
    }

    /**
     * Returns a {@link GrammaticalLabelSetDescriptor} for the given {@link HumanLanguage}.
     * This can be used as a key for the {@link #getCache()} such as:
     * <pre>
     * HumanLanguage language = LanguageProviderFactory.get().getLanguage(Locale.US);
     * GrammaticalLabelSet set = getCache().get(getDescriptor(language));
     * </pre>
     * @param language a language to return descriptor
     * @return {@link GrammaticalLabelSetDescriptor}
     */
    protected GrammaticalLabelSetDescriptor getDescriptor(HumanLanguage language) {
        return baseDesc.getLanguage() == language ? baseDesc : baseDesc.getForOtherLanguage(language);
    }

    /**
     * Test if the given language is already loaded. Unlike {@link #getSet(HumanLanguage)}, this method will not load
     * labels if not present in cache.
     *
     * @param language a language to test
     * @return {@code true} if the given {@code userLanguage} is already loaded, {@code false} otherwise.
     */
    public boolean isLoaded(HumanLanguage language) {
        return this.cache.getIfPresent(getDescriptor(language)) != null;
    }

    /**
     * Controls whether to trust {@link HumanLanguage#isTranslatedLanguage}. If {@code true}, and if
     * {@link HumanLanguage#isTranslatedLanguage} returns {@code false}, the loader will skip parsing XML files for that
     * particular language. For example, loading {@code en-AU} (Australia English) will simply make an object with
     * reference to its fallback language, {@code en-GB} instead of copying data from {@code en-GB} LabelSet.
     * <p>a
     * This also sets {@code useTranslatedLanguage} to its parent provider.
     *
     * @param useTranslatedLanguage
     *            Set {@code true} to respect {@link HumanLanguage#isTranslatedLanguage}. Default is {@code false}.
     * @see HumanLanguage#isTranslatedLanguage
     * @see #compute(GrammaticalLabelSetDescriptor)
     * @since 232
     */
    public void setUseTranslatedLanguage(boolean useTranslatedLanguage) {
        this.useTranslatedLanguage = useTranslatedLanguage;

        // don't forget parent
        GrammaticalLabelSetProvider p = this.parentProvider;
        if (p instanceof GrammaticalLabelSetLoader)
            ((GrammaticalLabelSetLoader)p).setUseTranslatedLanguage(useTranslatedLanguage);
    }

    /**
     * @return {@code true} if the loader is in "useTranslatedLanguage" mode.
     */
    public boolean getUseTranslatedLanguage() {
        return this.useTranslatedLanguage;
    }

    /*
     * see #compute(GrammaticalLabelSetDescriptor)
     */
    protected void setSkipParsingLabelForPlatform(boolean skip) {
        this.skipParsingLabelForPlatform = skip;
    }

    protected boolean  skipParsingLabelForPlatform() {
        return this.skipParsingLabelForPlatform;
    }
}
