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

package com.force.i18n.grammar.offline;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;

/**
 * Simple file generator to convert language dependent files for grammaticus.js.
 * <p>
 * See also an sample implementation of executable code in test: <code>com.force.i18n.grammar.tools.JSLabelGen</code>
 * @see <a href="https://github.com/salesforce/grammaticus/tree/master/src/main/resources/com/force/i18n/grammar/offline">grammaticus.js</a>
 * @author yoikawa
 * @since 226
 */
public class JavaScriptLabelsGenerator {
    private static final Logger logger = Logger.getLogger(JavaScriptLabelsGenerator.class.getName());

    protected static final String COPYRIGHT_TEXT = "Copyright (c) 2020, salesforce.com, inc. All rights reserved.";
    protected static final String LABEL_SET_NAME = "js";
    protected static final String DEFAULT_LABELS_XML = "labels.xml";
    protected static final String DEFAULT_NAMES_XML = "names.xml";
    protected static final HumanLanguage BASE_LANG = LanguageProviderFactory.get().getBaseLanguage();

    protected final GrammaticalLabelSetLoader loader;
    protected final Path sourceRootDir;
    protected final String labelFileName;

    public JavaScriptLabelsGenerator(File rootDirectory) throws MalformedURLException, URISyntaxException  {
        this(rootDirectory, DEFAULT_LABELS_XML, DEFAULT_NAMES_XML);
    }

    public JavaScriptLabelsGenerator(File rootDirectory, String labelFileName, String dictionaryFileName)
            throws MalformedURLException, URISyntaxException {
        this(new LabelSetDescriptorImpl(rootDirectory.toURI().toURL(), BASE_LANG, LABEL_SET_NAME, labelFileName,
                dictionaryFileName));
    }

    public JavaScriptLabelsGenerator(GrammaticalLabelSetDescriptor baseDesc) throws URISyntaxException {
        this.loader = new GrammaticalLabelSetLoader(baseDesc);
        this.labelFileName = Paths.get(this.loader.getBaseDesc().getRootFile().toURI()).getFileName().toString();
        this.sourceRootDir = Paths.get(this.loader.getBaseDesc().getRootDir().toURI());
    }


    /**
     * Utility method to generate the target language file in JavaScript.
     * <p>
     * The file name follows UTS#35 by using Locale.toLocaleString(). For example, if the <code>language</code> is
     * British English, the generated file name becomes 'en_GB.js'.
     * <p>
     * Note that we treat the base label as a 'root' and associate with the en_US locale, this method generates 'en.js'
     * instead.
     *
     * @param language to specify which language file to generate
     * @param outDir is the target directory to generate [language].js file
     * @param shouldGenerateComment to specify whether generating comment header texts in [language].js file
     * @param shouldGenerateAllNames to specify whether generating names defined in the dictionary file
     * @return true if success; false otherwise.
     */
    protected boolean write(HumanLanguage language, File outDir, boolean shouldGenerateComment, boolean shouldGenerateAllNames) {
        // ignore empty directory
        if (!isValid(this.sourceRootDir, language)) return false;

        // handle only languages that LanguageProviderFactory knows. "en.json" will be skipped because of this.
        // However, if you override/implement your own LangaugeProvider and HumanLanguage, you can provide any translations.
        GrammaticalLabelSet labelSet = this.loader.getSet(language);
        try {
            // if the target language is base language, strip off the country/variant part. if base language is "en_US",
            // renames to "en" (means that the file name becomes 'en.js'
            Locale loc = language != BASE_LANG ? language.getLocale() : new Locale(language.getLocale().getLanguage());
            String filename = loc.toString() + ".js";

            log("processing language: " + language.getLocaleString() + " (as filename: " + filename + ")");

            // assume first existing names.xml (from the bottom of the list) to be target folder
            try (Writer out = new FileWriter(new File(outDir, filename))) {
                if (shouldGenerateComment) {
                    out.append("/* Grammaticus v").append(VersionInfo.VERSION).append("\n")
                       .append(" * ").append(COPYRIGHT_TEXT).append("\n")
                       .append("\n")
                       .append(" * language depedent file for grammaticus.js\n")
                       .append(" * DO NOT EDIT: This file is generated by Grammaticus\n")
                       .append(" */\n");
                }
                out.append("export function override(baseObject) {'use strict';");

                out.append("baseObject.addLabels(");
                Set<GrammaticalTerm> termsInUse = new HashSet<>();
                labelSet.writeJson(out, null, termsInUse);

                out.append("); baseObject.addTerms(");
                labelSet.getDictionary().writeJsonTerms(out, false, shouldGenerateAllNames ? null : termsInUse);
                out.append(");");

                labelSet.getDictionary().getDeclension().writeJsonOverrides(out, "baseObject");
                out.append("};");
            }
        } catch (IOException e) {
            error("ERROR: unknown exception while processing language: " + language);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * Returns expected path to the label source file.
     * For example, if the baseDir is "/label" and language is en_GB, this returns "/label/en/GB".
     *
     * <p> for historic reason, we have tweaked directory path for some languages, such as US English.
     * Please checkout the actual implementation of
     * {@link com.force.i18n.HumanLanguage#getDefaultLabelDirectoryPath()} for more detail.
     *
     * @param path to the root input directory
     * @param language to read
     * @return Path to the actual label source file
     * @see com.force.i18n.HumanLanguage#getDefaultLabelDirectoryPath()
     */
    protected Path resolve(Path baseDir, HumanLanguage language) {
        Path path = baseDir.resolve(language.getDefaultLabelDirectoryPath());
        Path ret = path.resolve(this.labelFileName);
        return ret;
    }

    protected boolean isValid(Path baseDir, HumanLanguage language) {
        if (language == null) return false;
        Path rootLabel = resolve(baseDir, language);
        return Files.isRegularFile(rootLabel) && Files.isReadable(rootLabel);
    }

    public void generateLabels(File outputDir, boolean shouldGenerateComment, boolean shouldGenerateAllNames) throws IOException, URISyntaxException {
        log("\nGenerating files into: " + outputDir);

        // going through all sub-directories in source directory
        Path sourceDir = Paths.get(this.loader.getBaseDesc().getRootDir().toURI());
        log("Scanning source directory: " + sourceDir);

        // ensure the root label file exists
        if (!isValid(sourceDir, BASE_LANG))
            throw new IOException("root label is missing: " + resolve(sourceDir, BASE_LANG));

        // ensure base language (by default, en_US) gets loaded. this may not be required, but for just in case.
        this.loader.getSet(BASE_LANG);

        AtomicInteger cnt = new AtomicInteger(0);
        LanguageProviderFactory.get().getAll().parallelStream().filter(l -> isValid(sourceDir, l)).forEach(l -> {
            this.write(l, outputDir, shouldGenerateComment, shouldGenerateAllNames);
            cnt.incrementAndGet();
        });

        log("Done. Generated " + cnt.toString() + " files.");
    }

    protected void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    protected void error(String msg) {
        logger.log(Level.SEVERE, msg);
    }
}