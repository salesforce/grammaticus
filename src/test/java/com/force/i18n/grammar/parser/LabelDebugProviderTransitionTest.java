/*
 * Copyright (c) 2026, Salesforce, Inc.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LabelDebugProvider;
import com.force.i18n.LabelSetDescriptor;
import com.force.i18n.LabelSetDescriptorImpl;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.settings.MapPropertyFileData;
import com.force.i18n.settings.PropertyFileData;
import com.force.i18n.settings.TrackingHandler;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * The section-to-filename map is decided once per parse and never changes shape mid-flight: either it
 * covers every section the parse read, or it was never created. It is never partially populated.
 * <p>
 * A handler used to read {@code LabelDebug.isLabelHintAllowed()} twice — once when deciding whether to
 * initialize the map, and again for every {@code <section>} it wrote. A provider replaced between those
 * reads left the handler using a field the initialization branch never assigned, and a provider replaced
 * between two files of the same parse left a map covering only some of them. Because the sample label set
 * puts its sections in {@code <import>}ed files, these tests exercise the nested-handler path from the
 * reported stack rather than the top-level one.
 */
class LabelDebugProviderTransitionTest {

    private static final String LABEL_SET_NAME = "sample";
    /** The imported file that actually holds sections; the root {@code labels.xml} holds only imports. */
    private static final String SECTION_BEARING_FILE = "sample.xml";

    // BaseGrammaticalLabelTest used as a helper instance, matching GrammaticalLabelFileHandlerTest.
    private final BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("helper") {};

    private LabelDebugProvider originalProvider;

    @BeforeEach
    void setUp() throws Exception {
        helper.setUp();
        this.originalProvider = LabelDebugProvider.get();
    }

    @AfterEach
    void restoreProvider() {
        LabelDebugProvider.setLabelDebugProviderEnabled(this.originalProvider);
    }

    @Test
    @DisplayName("hints becoming allowed mid-parse does not break the parse")
    void hintsBecomingAllowedMidParse() throws Exception {
        ToggleableDebugProvider provider = install(new ToggleableDebugProvider(false));

        TransitionParser parser = newParser(provider, true);
        PropertyFileData data = newData();

        assertDoesNotThrow(() -> parser.load(data, new HashMap<>()));
        assertSectionsWereParsed(data);
        // The parse committed to "no label hints" before it started; enabling them halfway cannot make the
        // map appear for the files that were already read, so it must not appear for any of them either.
        assertNull(parser.getSectionToFileName(),
                "a parse that started with hints disallowed must not build a partial section map");
    }

    @Test
    @DisplayName("hints being revoked mid-parse leaves no gap in the section map")
    void hintsBeingRevokedMidParse() throws Exception {
        ToggleableDebugProvider provider = install(new ToggleableDebugProvider(true));

        TransitionParser parser = newParser(provider, false);
        PropertyFileData data = newData();

        parser.load(data, new HashMap<>());
        assertEverySectionMapped(parser, data);
    }

    @Test
    @DisplayName("hints allowed for the whole parse maps every section")
    void hintsAllowedThroughout() throws Exception {
        ToggleableDebugProvider provider = install(new ToggleableDebugProvider(true));

        TransitionParser parser = newParser(provider, true);
        PropertyFileData data = newData();

        parser.load(data, new HashMap<>());
        assertEverySectionMapped(parser, data);
    }

    @Test
    @DisplayName("no section map is retained when hints are never allowed")
    void hintsNeverAllowed() throws Exception {
        ToggleableDebugProvider provider = install(new ToggleableDebugProvider(false));

        TransitionParser parser = newParser(provider, false);
        PropertyFileData data = newData();

        parser.load(data, new HashMap<>());
        assertSectionsWereParsed(data);
        // The map exists only to answer LabelDebug.getFilename(); nothing should be retained for it in a
        // process that cannot render label hints.  At Core's corpus size this map is ~3MB per label set.
        assertNull(parser.getSectionToFileName(),
                "no section-to-filename map should be built when label hints are not allowed");
    }

    private static ToggleableDebugProvider install(ToggleableDebugProvider provider) {
        LabelDebugProvider.setLabelDebugProviderEnabled(provider);
        return provider;
    }

    /**
     * @param allowedAfterTransition what the provider reports once the parse reaches
     *        {@link #SECTION_BEARING_FILE}
     */
    private TransitionParser newParser(ToggleableDebugProvider provider, boolean allowedAfterTransition)
            throws Exception {
        HumanLanguage language = LanguageProviderFactory.get().getBaseLanguage();
        LanguageDictionary dictionary = helper.loadDictionary(language);
        LabelSetDescriptor desc = new LabelSetDescriptorImpl(helper.getBaseDir(), language, LABEL_SET_NAME);
        return new TransitionParser(dictionary, desc, provider, allowedAfterTransition);
    }

    private static PropertyFileData newData() {
        return new MapPropertyFileData(LanguageProviderFactory.get().getBaseLanguage().getLocale());
    }

    /** Guards against a vacuous pass: an empty parse would satisfy any assertion about the map. */
    private static void assertSectionsWereParsed(PropertyFileData data) {
        assertFalse(data.getSectionNames().isEmpty(), "the parse should have read at least one section");
    }

    private static void assertEverySectionMapped(GrammaticalLabelFileParser parser, PropertyFileData data) {
        assertSectionsWereParsed(data);
        assertNotNull(parser.getSectionToFileName(), "label hints were allowed, so the map should exist");

        Set<String> missing = new TreeSet<>(data.getSectionNames());
        missing.removeAll(parser.getSectionToFileName().keySet());
        assertTrue(missing.isEmpty(),
                () -> "section-to-filename map is missing " + missing.size() + " of " + data.getSectionNames().size()
                        + " parsed sections: " + missing);
    }

    /**
     * Replaces the global provider once the handler for {@link #SECTION_BEARING_FILE} has been constructed
     * but before that file's sections are parsed — the window the reported failure fell into, made
     * deterministic.
     */
    private static final class TransitionParser extends GrammaticalLabelFileParser {
        private final ToggleableDebugProvider provider;
        private final boolean allowedAfterTransition;

        TransitionParser(LanguageDictionary dictionary, LabelSetDescriptor desc, ToggleableDebugProvider provider,
                boolean allowedAfterTransition) {
            super(dictionary, desc, null, false);
            this.provider = provider;
            this.allowedAfterTransition = allowedAfterTransition;
        }

        @Override
        protected void parse(URL file, TrackingHandler handler) {
            if (file.getPath().endsWith(SECTION_BEARING_FILE)) {
                this.provider.setAllowed(this.allowedAfterTransition);
            }
            super.parse(file, handler);
        }
    }

    /** A provider whose label-hint permission can be changed while a parse is in flight. */
    private static final class ToggleableDebugProvider extends LabelDebugProvider {
        private volatile boolean allowed;

        ToggleableDebugProvider(boolean allowed) {
            this.allowed = allowed;
        }

        void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        @Override
        public boolean isAllowed() {
            return this.allowed;
        }

        @Override
        public String makeLabelHintIfRequested(String text, String section, String key) {
            return text;
        }

        @Override
        public boolean isTrackingLabelUsage() {
            return false;
        }

        @Override
        public void setTrackingLabelUsage(boolean trackUsage) {
        }

        @Override
        public void trackLabel(String section, String key) {
        }

        @Override
        public SetMultimap<String, String> getUsedLabels() {
            return ImmutableSetMultimap.of();
        }

        @Override
        public boolean setLabelHintRequest(boolean value) {
            return value;
        }

        @Override
        public void setLabelHintMode(String value) {
        }
    }
}
