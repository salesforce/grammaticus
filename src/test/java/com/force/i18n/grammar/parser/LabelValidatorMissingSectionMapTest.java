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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LabelDebugProvider;
import com.force.i18n.LabelSetDescriptorImpl;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.GrammaticalLabelSet;

/**
 * When label hints are disallowed (the default), a parsed label set has no section-to-filename map.
 * {@code LabelValidator} builds its noun-form error messages from that map, so it must tolerate the map
 * being absent instead of dereferencing it.
 * <p>
 * This is the same absent-map contract already handled at {@code LabelDebug.getFilename()} and in the
 * {@code GrammaticalLabelTest} copy of this logic; {@code LabelValidator} is the production consumer that
 * was missed.
 */
class LabelValidatorMissingSectionMapTest {

    private LabelDebugProvider originalProvider;

    @BeforeEach
    void captureProvider() {
        this.originalProvider = LabelDebugProvider.get();
    }

    @AfterEach
    void restoreProvider() {
        LabelDebugProvider.setLabelDebugProviderEnabled(this.originalProvider);
    }

    @Test
    @DisplayName("noun-form validation reports the error instead of NPE when the section map is absent")
    void reportsMissingFormWhenSectionMapAbsent() throws Exception {
        // Label hints off => the parser never builds the section-to-filename map (it is null).
        LabelDebugProvider.setLabelDebugProviderEnabled(false);

        BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("LabelValidatorMissingSectionMapTest") {};
        helper.setUp();

        HumanLanguage base = LanguageProviderFactory.get().getBaseLanguage();
        // Basque is inflected and treats every case form as an entity form, so a noun defined with only its
        // base value is missing the case forms the reference below asks for -> the error-reporting branch.
        HumanLanguage basque = LanguageProviderFactory.get().getLanguage("eu");

        LabelSetDescriptorImpl baseDesc = new LabelSetDescriptorImpl(helper.getBaseDir(), base, "sample");
        LabelValidator validator = new LabelValidator(baseDesc, null);

        // The label must hold more than a single tag so the value is stored as a List; a lone NounRefTag is
        // not, and LabelValidator only inspects List-valued labels.
        String grammar = "<noun name=\"Widget\" type=\"entity\" entity=\"Widget\" alias=\"Widgets\">"
                + "<value plural=\"n\">Trepeta</value></noun>";
        String label = "prefix <Widget case=\"d\" article=\"d\"/> suffix";

        GrammaticalLabelSet labelSet = validator.getTestLabelSet(basque, label, grammar);
        assertNull(labelSet.getLabelSectionToFilename(),
                "precondition: with hints disallowed the section-to-filename map must be absent");

        List<String> errors = assertDoesNotThrow(
                () -> validator.validateReferencedCaseFormsExistTest(List.of(labelSet)),
                "validation must not NPE when the section-to-filename map is absent");

        // The missing form must still be reported; a swallowed error would defeat the validator's purpose.
        assertFalse(errors.isEmpty(), "the missing noun form should still be reported");
        assertTrue(errors.stream().anyMatch(e -> e.contains("Test.Test")),
                () -> "expected the Test.Test label in the error, got: " + errors);
    }
}
