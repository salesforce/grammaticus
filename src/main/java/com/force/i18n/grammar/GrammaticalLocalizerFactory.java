/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.parser.GrammaticalLabelSetFileCacheLoader;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;

/**
 * @author stamm
 *
 */
public class GrammaticalLocalizerFactory extends LocalizerFactory {
    private final GrammaticalLabelSetProvider labelSetLoader;
    private final URL labelsDir;


    public GrammaticalLocalizerFactory(GrammaticalLabelSetProvider loader) {
        this.labelSetLoader = loader;
        this.labelsDir = loader instanceof GrammaticalLabelSetLoader
                ? ((GrammaticalLabelSetLoader) loader).getBaseDesc().getRootDir()
                : null;
    }

    /**
     * @deprecated use {@link #GrammaticalLocalizerFactory(GrammaticalLabelSetProvider)}
     */
    @Deprecated
    public GrammaticalLocalizerFactory(GrammaticalLabelSetLoader loader) {
        this.labelSetLoader = loader;
        this.labelsDir = loader.getBaseDesc().getRootDir();
    }

    private static final Boolean DO_CACHE_LABEL_SETS = !"false".equals(I18nJavaUtil.getProperty("cacheLabelSets"));
    /**
	 * Helper method you can use to correctly provide the right "loader" for your labels
	 * @param desc the label set descriptor to load
	 * @param parent the optional parent of the label set for fallback labels
	 * @return the loader for your label set, which will do caching based on configuration
	 */
	public static GrammaticalLabelSetLoader getLoader(GrammaticalLabelSetDescriptor desc, GrammaticalLabelSetProvider parent) {
        if (DO_CACHE_LABEL_SETS) {
            return new GrammaticalLabelSetFileCacheLoader(desc, parent);
        } else {
            return new GrammaticalLabelSetLoader(desc, parent);
        }
	}

    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#findLabelSet(i18n.HumanLanguage)
	 */
    @Override
	public LabelSet findLabelSet(HumanLanguage language) {
        return this.labelSetLoader.getSet(language);
    }

    @Override
	public BaseLocalizer getLocalizer(Locale locale, Locale currencyLocale, HumanLanguage language, TimeZone timeZone) {
        if (locale == null)
            locale = getDefaultLocale();
        if (language == null)
            language = getDefaultLanguage();

        GrammaticalLabelSet labelSet = (GrammaticalLabelSet) findLabelSet(language);
        return new GrammaticalLocalizer(locale, currencyLocale, timeZone, language, labelSet);
    }

    private boolean isLabelProviderInitialized;
    private boolean isEnglishLabelProviderInitialized;

    public synchronized void initLabelProvider() {
        if (this.isLabelProviderInitialized) return;
        this.labelSetLoader.init();
        this.isLabelProviderInitialized = true;
    }

    public synchronized void initEnglishLabelProvider() {
        if (this.isEnglishLabelProviderInitialized) return;
        this.labelSetLoader.initEnglish();
        this.isEnglishLabelProviderInitialized = true;
    }


    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLabelsDirectory()
	 */
    @Override
	public URL getLabelsDirectory() {
        return this.labelsDir;
    }


    /**
     * This is for debugging purposes only! Do not call this if you don't know why this is here.
     */
    public void resetLabels() {
        if (I18nJavaUtil.isDebugging()) {
            labelSetLoader.resetMap();
        }
    }

	public void doLabelProviderPostInit() throws Exception {

    }
}
