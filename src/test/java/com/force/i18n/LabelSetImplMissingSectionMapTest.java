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

package com.force.i18n;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.force.i18n.settings.MapPropertyFileData;

/**
 * The section-to-filename map is null whenever a set was parsed while label hints were disallowed (the default),
 * so {@code getFilenameFromLabelSection} -- a public convenience accessor -- must tolerate the absent map and
 * return null rather than tripping an {@code assert} (with -ea) or throwing an NPE. This mirrors the null-safe
 * handling already in {@code LabelDebug.getFilename()} and {@code LabelValidator}.
 */
class LabelSetImplMissingSectionMapTest {

    /** Minimal concrete LabelSetImpl exposing the protected map-carrying constructor. */
    private static final class TestLabelSet extends LabelSetImpl {
        private static final long serialVersionUID = 1L;

        TestLabelSet(Map<String, String> sectionToFilename) {
            super(new MapPropertyFileData(java.util.Locale.US), sectionToFilename);
        }
    }

    @Test
    @DisplayName("getFilenameFromLabelSection returns null when the section map is absent")
    void returnsNullWhenMapAbsent() {
        TestLabelSet set = new TestLabelSet(null);
        assertNull(set.getLabelSectionToFilename(), "precondition: the section map is absent");

        String filename = assertDoesNotThrow(() -> set.getFilenameFromLabelSection("Sales"),
                "an absent section map must not throw");
        assertNull(filename);
    }

    @Test
    @DisplayName("getFilenameFromLabelSection still resolves a section when the map is present")
    void resolvesWhenMapPresent() {
        TestLabelSet set = new TestLabelSet(Map.of("Sales", "sales.xml"));
        assertEquals("sales.xml", set.getFilenameFromLabelSection("Sales"));
    }
}
