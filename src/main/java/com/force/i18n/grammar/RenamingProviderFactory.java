/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.util.concurrent.atomic.AtomicReference;

import com.force.i18n.*;

/**
 * Provides access to a "renaming" provider that is global to the application
 * @author stamm
 */
public enum RenamingProviderFactory {
    INSTANCE;

    private AtomicReference<RenamingProvider> providerRef = new AtomicReference<RenamingProvider>(new DefaultRenamingProviderImpl()); // Default is nothing

    public static RenamingProviderFactory get() { return INSTANCE; }

    public void setProvider(RenamingProvider provider) {
        this.providerRef.set(provider);
    }

    public RenamingProvider getProvider() {
        return this.providerRef.get();
    }

    static class DefaultRenamingProviderImpl implements RenamingProvider {
        @Override
        public Noun getRenamedNoun(HumanLanguage language, String key) {
            return null;
        }

        @Override
        public Noun getPackagedNoun(HumanLanguage language, String key) {
            return null;
        }

        @Override
        public Noun getNoun(HumanLanguage language, Renameable key) {
            return key.getStandardNoun(language);
        }

        @Override
        public boolean isCustomKey(String key) {
            return false;
        }

        @Override
        public boolean isRenamed(HumanLanguage language, String key) {
            return false;
        }

        @Override
        public boolean useRenamedNouns() {
            return false;
        }

        @Override
        public boolean supportOldGrammarEngine() {
            return false;
        }

        @Override
        public boolean displayMiddleNameInCalculatedPersonName() {
            return false;
        }

        @Override
        public boolean displaySuffixInCalculatedPersonName() {
            return false;
        }
    }

}
