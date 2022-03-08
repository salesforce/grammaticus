/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.I18nJavaUtil;
import com.force.i18n.LabelSetDescriptorImpl;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.force.i18n.grammar.GrammaticalLabelSetFallbackImpl;
import com.force.i18n.grammar.GrammaticalLabelSetImpl;
import com.force.i18n.grammar.GrammaticalLabelSetProvider;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.settings.MapPropertyFileData;
import com.force.i18n.settings.PropertyFileData;
import com.force.i18n.settings.SharedKeyMap;
import com.force.i18n.settings.SharedKeyMapPropertyFileData;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
    final boolean useSharedKeys;
    final Set<String> publicSections = new HashSet<String>();

    // respect HumanLanguage#isTranslatedLanguage. see #compute(GrammaticalLabelSetDescriptor) how it's used.
    private boolean useTranslatedLanguage = "true".equals(I18nJavaUtil.getProperty("useTranslatedLanguage"));

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
        this.baseDesc = baseDesc;
        this.useSharedKeys = useSharedKeys;
        this.parentProvider = parent;
        // Share the keys of the parent loader if possible
        if (this.useSharedKeys) {
            if (parent instanceof GrammaticalLabelSetLoader && ((GrammaticalLabelSetLoader)parent).useSharedKeys) {
                this.seedKeyMap = new SharedKeyMap<String, SharedKeyMap<String, Object>>(((GrammaticalLabelSetLoader)parent).seedKeyMap);
            } else {
                this.seedKeyMap = new SharedKeyMap<String, SharedKeyMap<String, Object>>();
            }
        } else {
            this.seedKeyMap = null;
        }
        // might need to be able to configure -- e.g. set max size
        this.cache = CacheBuilder.newBuilder()
            .initialCapacity(64)
            .build(new Loader());
    }

    /**
     * Use an inner class for class loading.
     */
    class Loader extends CacheLoader<GrammaticalLabelSetDescriptor, GrammaticalLabelSet> {
        @Override
        public GrammaticalLabelSet load(GrammaticalLabelSetDescriptor desc) throws Exception {
                return GrammaticalLabelSetLoader.this.makeSet(desc);
        }
    }

    GrammaticalLabelSet makeSet(GrammaticalLabelSetDescriptor desc) throws IOException {
        if (parentProvider != null) {
            GrammaticalLabelSet parentSet = parentProvider.getSet(desc.getLanguage());
            return new GrammaticalLabelSetFallbackImpl(GrammaticalLabelSetLoader.this.compute(desc), parentSet);
        }
        return GrammaticalLabelSetLoader.this.compute(desc);
    }

    GrammaticalLabelSet compute(GrammaticalLabelSetDescriptor desc) throws IOException {
        HumanLanguage lang = desc.getLanguage();
        long start = System.currentTimeMillis();

        GrammaticalLabelSetImpl result = null;
        if (this.useTranslatedLanguage && !lang.isTranslatedLanguage()) {
            // this is for platform languages. if the requested language has no translation, try creating a (shallow) copy of
            // LabelSet from its fallback.
            HumanLanguage fallbackLang = lang.getFallbackLanguage();
            GrammaticalLabelSet fallback = getSetByDescriptor(desc.getForOtherLanguage(fallbackLang));

            // if declension is proxy (ForwardingLanguageDeclension), good to reuse fallback data
            if (LanguageDeclensionFactory.get().isForwardingProxy(lang)) {
                // simply copy all data (except language) from the fallback dictionary
                LanguageDictionary dictionary = new LanguageDictionary(lang, fallback.getDictionary());

                // use copy constructor for requested language
                result = new GrammaticalLabelSetImpl(dictionary, fallback);
            } else {
                // if declension is unique, do full-parse dictionary/label file
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

    private GrammaticalLabelSetImpl loadLabels(GrammaticalLabelSetDescriptor desc) throws IOException {
        HumanLanguage lang = desc.getLanguage();

        // dictionaries are always unique for every language because it may use different LanguageDeclension
        LanguageDictionaryParser dictParser = new LanguageDictionaryParser(desc, lang, this.parentProvider);
        LanguageDictionary dictionary = dictParser.getDictionary();

        // all standard/end-user languages comes here. Create a parser to read from XML files.
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dictionary, desc, this.parentProvider);

        PropertyFileData propertyFileData = GrammaticalLabelSetLoader.this.useSharedKeys
                ? new SharedKeyMapPropertyFileData(lang.getLocale(), !desc.hasOverridingFiles(), seedKeyMap, publicSections)
                : new MapPropertyFileData(lang.getLocale());

        GrammaticalLabelSetImpl result = new GrammaticalLabelSetImpl(parser.getDictionary(), parser, propertyFileData);
        if (useSharedKeys) {
            ((SharedKeyMapPropertyFileData) propertyFileData).compact();
        }
        result.setLabelSectionToFilename(GrammaticalLabelFileHandler.SECTION_TO_FILENAME);
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
     * @author yoikawa
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
}
