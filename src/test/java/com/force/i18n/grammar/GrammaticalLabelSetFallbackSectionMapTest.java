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

package com.force.i18n.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The section-to-filename map is an "absent or complete" value: it is null when the set was parsed while label
 * hints were disallowed, and non-null consumers treat it as authoritative for every section. When two sets are
 * composed, the union must preserve that invariant -- a partial map (one side present, the other absent) would
 * misattribute a section's file. See {@link GrammaticalLabelSetFallbackImpl#unionSectionToFilename}.
 */
class GrammaticalLabelSetFallbackSectionMapTest {

    @Test
    @DisplayName("union is null when neither set has a map")
    void bothAbsentYieldsNull() {
        assertNull(GrammaticalLabelSetFallbackImpl.unionSectionToFilename(setWithMap(null), setWithMap(null)));
    }

    @Test
    @DisplayName("union is null when only the main set has a map (avoid a partial composite)")
    void onlyMainPresentYieldsNull() {
        GrammaticalLabelSet main = setWithMap(Map.of("Sales", "sales.xml"));
        assertNull(GrammaticalLabelSetFallbackImpl.unionSectionToFilename(main, setWithMap(null)),
                "a map present on only one side must not be surfaced as a complete composite");
    }

    @Test
    @DisplayName("union is null when only the fallback set has a map (avoid a partial composite)")
    void onlyFallbackPresentYieldsNull() {
        GrammaticalLabelSet fallback = setWithMap(Map.of("Sales", "sales.xml"));
        assertNull(GrammaticalLabelSetFallbackImpl.unionSectionToFilename(setWithMap(null), fallback),
                "a map present on only one side must not be surfaced as a complete composite");
    }

    @Test
    @DisplayName("union combines both maps, and the main set wins an overlapping section")
    void bothPresentUnionsWithMainPrecedence() {
        GrammaticalLabelSet main = setWithMap(Map.of("Sales", "main-sales.xml", "Only_Main", "main-only.xml"));
        GrammaticalLabelSet fallback = setWithMap(Map.of("Sales", "fallback-sales.xml", "Only_Fallback", "fallback-only.xml"));

        Map<String, String> union = GrammaticalLabelSetFallbackImpl.unionSectionToFilename(main, fallback);

        assertEquals("main-sales.xml", union.get("Sales"), "the overriding (main) set must win an overlapping section");
        assertEquals("main-only.xml", union.get("Only_Main"));
        assertEquals("fallback-only.xml", union.get("Only_Fallback"));
    }

    /**
     * A {@link GrammaticalLabelSet} stub that answers only {@code getLabelSectionToFilename()} -- the sole method
     * the union consults -- via a dynamic proxy, so we don't have to implement the whole 22-method interface.
     */
    private static GrammaticalLabelSet setWithMap(Map<String, String> sectionToFilename) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("getLabelSectionToFilename".equals(method.getName())) {
                return sectionToFilename;
            }
            throw new UnsupportedOperationException("stub does not implement " + method.getName());
        };
        return (GrammaticalLabelSet) Proxy.newProxyInstance(
                GrammaticalLabelSet.class.getClassLoader(),
                new Class<?>[] {GrammaticalLabelSet.class},
                handler);
    }
}
