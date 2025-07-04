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
