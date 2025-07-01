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

import java.net.*;
import java.util.List;
import java.util.Objects;

import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * Represents a descriptor of a standard language-specific language set
 * @author yoikawa,stamm
 */
public class LabelSetDescriptorImpl implements GrammaticalLabelSetDescriptor {

    private final URL rootDirectory;
    private final HumanLanguage language;
    private final String setName;
    private final String basename;
    private final String dictionaryName;

    /**
     * Construct a LabelSetDescriptor for the given labelSetName in the default language
     * Uses the label and dictionary file name from the properties files
     * @param rootDirectory the root directory of the files in the given language
     * @param labelSetName the name of the label set
     */
    public LabelSetDescriptorImpl(URL rootDirectory, String labelSetName) {
        this(rootDirectory, LanguageProviderFactory.get().getBaseLanguage(), labelSetName);
    }

    /**
     * Construct a LabelSetDescriptor for the given labelSetName.
     * Uses the label and dictionary file name from the properties files
     * @param rootDirectory the root directory of the files in the given language
     * @param language the language of the label set
     * @param labelSetName the name of the label set
     */
    public LabelSetDescriptorImpl(URL rootDirectory, HumanLanguage language, String labelSetName) {
        this(rootDirectory,language, labelSetName, LABELS_FILENAME, DICTIONARY_FILENAME);
    }

    @Override
    public LabelSetDescriptorImpl getForOtherLanguage(HumanLanguage otherLanguage) {
///CLOVER:OFF
        if (otherLanguage == this.getLanguage()) {
            throw new IllegalArgumentException("Programmer error, you shouldn't be asking for this in the same language");
        }
///CLOVER:ON
        return new LabelSetDescriptorImpl(this.rootDirectory, otherLanguage, this.setName, this.basename, this.dictionaryName);
    }

    /**
     * Constructor to override the default file basename with the specified basename.
     * @param rootDirectory the rootDirectory of the labels
     * @param language the language associated with the labels
     * @param setName the debug name for the set in logs
     * @param basename the name without extension of the XML file containing the labels
     * @param dictionaryName the name without extension of the XML file containing the nouns
     */
    public LabelSetDescriptorImpl(URL rootDirectory, HumanLanguage language, String setName, String basename, String dictionaryName) {
        assert language != null : "You must provide a language";
        assert rootDirectory != null : "You must provide a root directory";
        this.rootDirectory = rootDirectory;
        this.basename = basename;
        this.setName = setName;
        this.language = language;
        this.dictionaryName = dictionaryName;
    }

    @Override
    public URL getRootDir() {
        return this.rootDirectory;
    }

    @Override
    public URL getRootFile() {
        try {
            return new URL(rootDirectory, basename);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getLabelSetName() {
        return setName;
    }

    public URL getDictionaryDir() {
        return this.rootDirectory;
    }

    @Override
    public URL getDictionaryFile() {
        try {
            return dictionaryName != null ? new URL(getDictionaryDir(), dictionaryName) : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean hasOverridingFiles() {
        return language != null && language != LanguageProviderFactory.get().getProvider().getBaseLanguage();
    }

    @Override
    public HumanLanguage getLanguage() {
        return this.language;
    }

    @Override
    public List<URL> getOverridingFiles() {
        return LabelUtils.getFileNames(language, rootDirectory, basename);
    }

    @Override
    public List<URL> getOverridingDictionaryFiles() {
        return LabelUtils.getFileNames(language, getDictionaryDir(), dictionaryName);
    }

    @Override
    public int hashCode() {
        // ignoring setName and dictionary name
        return Objects.hash(language, setName, basename, dictionaryName, rootDirectory.toExternalForm());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        // ignoring setName and dictionary name
        LabelSetDescriptorImpl other = (LabelSetDescriptorImpl)obj;
        return language.equals(other.language)  // language can't be null
                && Objects.equals(this.setName, other.setName)
                && Objects.equals(this.basename, other.basename)
                && Objects.equals(this.dictionaryName, other.dictionaryName)
                && rootDirectory.toExternalForm().equals(other.rootDirectory.toExternalForm());
    }

    @Override
    public String toString() {
        return language + ": " + rootDirectory.getPath() + " - " + basename;
    }

    String getBaseName() {
        return this.basename;
    }

    String getDictionaryName() {
        return this.dictionaryName;
    }

    /**
     * Construct a LabelSetDescriptor with a function to determine the root per language
     * instead of a fixed one
     * @param rootMap a map from HumanLanguage to URL root
     * @param baseLanguage the first language you need (it still assumes base language is base)
     * @param setName the name of the label set
     * @param basename the "labels.xml" or equivalent for the set
     * @param dictionaryName the "names.xml" or equivalent for the set
     * @return a LabelSetDescriptor with the multiple roots
     */
    public static LabelSetDescriptorImpl getWithMultipleRoots(Function<HumanLanguage,URL> rootMap, HumanLanguage baseLanguage, String setName, String basename, String dictionaryName) {
        return new MultipleRoots(baseLanguage, setName, basename, dictionaryName, rootMap);
    }

    /**
     * @return a non-caching function that uses the ClassLoader to find label files on the classpath
     * @param rootPath the path to the directory containing the labels
     * @param labelName the name of the label file in the directory.
     * @param resourceLocator the relevant classloader to use to find the labels
     */
    public static Function<HumanLanguage, URL> getLabelRootFunction(String rootPath, String labelName, ClassLoader resourceLocator) {
        return new JarRootFinder(rootPath, labelName, resourceLocator);
    }

    /**
     * Implementation of LabelSetDescriptorImpl that uses a function to calculate a different root based on language.
     */
    static class MultipleRoots extends LabelSetDescriptorImpl {
        private final Function<HumanLanguage, URL> rootMap;

        public MultipleRoots(HumanLanguage language, String setName, String basename, String dictionaryName, Function<HumanLanguage, URL> rootMap) {
            super(rootMap.apply(language), language, setName, basename, dictionaryName);
            this.rootMap = rootMap;
        }

        @Override
        public LabelSetDescriptorImpl getForOtherLanguage(HumanLanguage otherLanguage) {
            Preconditions.checkNotNull(otherLanguage);
            if (getLanguage() == otherLanguage) {
                throw new IllegalArgumentException("You shouldn't ask for the same language"); // In case of recursion
            }
            return new MultipleRoots(otherLanguage, getLabelSetName(), getBaseName(), getDictionaryName(), this.rootMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), rootMap);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj) || getClass() != obj.getClass())
                return false;
            MultipleRoots other = (MultipleRoots)obj;
            return Objects.equals(rootMap, other.rootMap);
        }
    }

    /**
     * A class that uses the classpath of the application to find labels.
     */
    static class JarRootFinder implements Function<HumanLanguage, URL> {
        private final String rootPath;
        private final String labelName;
        private final ClassLoader resourceLocator;

        public JarRootFinder(String rootPath, String labelName, ClassLoader resourceLocator) {
            super();
            if (rootPath == null || !rootPath.startsWith("/")) {
                throw new IllegalArgumentException("You must provide an absolute path.");
            }
            this.rootPath = rootPath;
            this.labelName = labelName;
            this.resourceLocator = resourceLocator;
        }

        @Override
        public URL apply(HumanLanguage lang) {
            String dirPath = lang.getDefaultLabelDirectoryPath();
            String dir = dirPath.length() > 0 ? dirPath + "/" : dirPath;
            int levels = dirPath.indexOf('/') > 0 ? 2 : dirPath.length() > 0 ? 1 : 0; // How many "/"'s are in dir...
            String path = this.rootPath.substring(1) + "/" + dir + labelName; // remove the first "/"
            URL labelsXml = resourceLocator.getResource(path);
            if (labelsXml == null) {
                // We might not have labels for this language.
                assert lang.getFallbackLanguage() != null : "We should have labels for " + lang;
                if (lang == LanguageProviderFactory.get().getBaseLanguage()) {
                    throw new IllegalArgumentException("Could not find " + path);
                }
                return apply(LanguageProviderFactory.get().getBaseLanguage());
            }

            try {
                return new URL(labelsXml, LabelUtils.getParentLevelPath(levels));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Could not find " + labelName);
            }
        }
    }

    @Override
    public boolean hasModularizedFiles() {
        return false;
    }

    @Override
    public List<URL> getModularizedFiles() {
        throw new IllegalArgumentException("Should not call this function.");
    }
}
