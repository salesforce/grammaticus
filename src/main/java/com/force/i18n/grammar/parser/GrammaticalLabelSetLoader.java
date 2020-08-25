/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.*;
import com.force.i18n.settings.*;
import com.google.common.base.Throwables;
import com.google.common.cache.*;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * GrammaticalLabelSetLoader which loads GrammaticalLabelSets from one or more files, and a dictionary file.
 * <p>
 * The labelset is stored in one or more XML files with the ini-style schema. The dictionary is in an XML file, called
 * sfdcnames.xml. All of these files are organized in the filesystem relative to the locale to which they apply.
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
    private static final boolean USE_SHARED_KEYS_DEFAULT = true;  // You really want this, so it isn't an option

    //@GuardedBy("itself") // this is passed as seed data to multiple threads and modifications need to be synchronized
    private final SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap;

    private final GrammaticalLabelSetProvider parentProvider;
    private final GrammaticalLabelSetDescriptor baseDesc;
    private final LoadingCache<GrammaticalLabelSetDescriptor, GrammaticalLabelSet> cache;
    // These leak to the computable above.
    final boolean useSharedKeys;
    final Set<String> publicSections = new HashSet<String>();

    @Override
    public void init() {

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
    	if(parentProvider != null) { // Invalidate parent loader cache.
    		parentProvider.resetMap();
    	}
    	
        if (I18nJavaUtil.isDebugging()) {
            cache.invalidateAll();
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

    GrammaticalLabelSetImpl compute(GrammaticalLabelSetDescriptor desc) throws IOException {
        LanguageDictionaryParser dictParser = new LanguageDictionaryParser(desc, desc.getLanguage(), this.parentProvider);
        LanguageDictionary dictionary = dictParser.getDictionary();
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dictionary, desc, this.parentProvider);

        PropertyFileData propertyFileData = GrammaticalLabelSetLoader.this.useSharedKeys
            ? new SharedKeyMapPropertyFileData(desc.getLanguage().getLocale(), !desc.hasOverridingFiles(), seedKeyMap, publicSections)
            : new MapPropertyFileData(desc.getLanguage().getLocale());

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
                // Always load english first.  Note, the cache never includes fallback.
                GrammaticalLabelSet fallback = getSetByDescriptor(desc.getForOtherLanguage(fallbackLang));
                return new GrammaticalLabelSetFallbackImpl(cache.get(desc), fallback);
            } else {
                return cache.get(desc);  // English only!
            }
        }
        catch(UncheckedExecutionException | ExecutionException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException("Unable to load label set for " + desc, e);
        }
    }

    @Override
    public GrammaticalLabelSet getSet(HumanLanguage userLanguage) {
        return getSetByDescriptor(baseDesc.getLanguage() == userLanguage ? baseDesc : baseDesc.getForOtherLanguage(userLanguage));
    }

    public GrammaticalLabelSetDescriptor getBaseDesc() {
        return this.baseDesc;
    }

    protected SharedKeyMap<String, SharedKeyMap<String, Object>> getSeedKeyMap() {
        return this.seedKeyMap;
    }
}
