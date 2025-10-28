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

package com.force.i18n.grammar.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageCase;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageGender;
import com.force.i18n.grammar.LanguageNumber;
import com.force.i18n.grammar.LanguagePossessive;
import com.force.i18n.grammar.NounForm;

class BasqueDeclensionTest {
    private LanguageDeclension getBasque() {
        HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");
        return LanguageDeclensionFactory.get().getDeclension(eu);
    }

    private BasqueDeclension.BasqueNoun newNoun() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        return (BasqueDeclension.BasqueNoun)decl.createNoun("Demo", null, com.force.i18n.grammar.Noun.NounType.OTHER, null,
            null, null, null, false, false);
    }

    @Test
    void testNounForms() {
        LanguageDeclension decl = getBasque();

        List<? extends NounForm> nounForms = decl.getAllNounForms();
        assertTrue(nounForms.size() >= 1);
        // all forms are the same
        assertSame(nounForms, decl.getEntityForms());
        assertSame(nounForms, decl.getFieldForms());
        assertSame(nounForms, decl.getOtherForms());

        NounForm base = nounForms.get(0);
        assertEquals(LanguageNumber.SINGULAR, base.getNumber());
        assertEquals(LanguageCase.NOMINATIVE, base.getCase());
        assertEquals(LanguageArticle.ZERO, base.getArticle());
        assertEquals("0:n:n", base.getKey());

        // Basque defines additional optional override forms for irregulars
        boolean hasDefSgNom = nounForms.stream().anyMatch(f -> f.getNumber() == LanguageNumber.SINGULAR && f.getCase() == LanguageCase.NOMINATIVE && f.getArticle() == LanguageArticle.DEFINITE);
        assertTrue(hasDefSgNom);

        // Default article for declension corresponds to ABS(Indef)
        assertEquals(LanguageArticle.INDEFINITE, decl.getDefaultArticle());
    }

    @Test
    void testAdjectiveForms() {
        LanguageDeclension decl = getBasque();

        List<? extends AdjectiveForm> adjForms = decl.getAdjectiveForms();
        // Expect all Basque adjective forms (SG_ABS acts as the base form)
        assertEquals(BasqueDeclension.BasqueAdjectiveForm.values().length, adjForms.size());

        // Verify the list contains all Basque adjective forms
        java.util.Arrays.stream(BasqueDeclension.BasqueAdjectiveForm.values()).forEach(f -> assertTrue(adjForms.contains(f)));

        // Verify the first form looks like a simple base form
        AdjectiveForm base = adjForms.get(0);
        assertEquals(LanguageNumber.SINGULAR, base.getNumber());
        assertEquals(LanguageCase.NOMINATIVE, base.getCase());
        assertEquals(LanguageGender.NEUTER, base.getGender());
        assertEquals(LanguageArticle.ZERO, base.getArticle());
        assertEquals("0:n", base.getKey());
    }

    @Test
    void testFlagsAndCases() {
        LanguageDeclension decl = getBasque();

        assertFalse(decl.hasGender());
        assertFalse(decl.hasArticle());
        assertTrue(decl.hasPlural());
        assertFalse(decl.hasStartsWith());

        // Required cases: exactly nominative
        assertEquals(EnumSet.of(LanguageCase.NOMINATIVE), decl.getRequiredCases());

        // Allowed cases: assert exact set
        EnumSet<LanguageCase> expectedAllowed = EnumSet.of(
            LanguageCase.NOMINATIVE,
            LanguageCase.ERGATIVE,
            LanguageCase.DATIVE,
            LanguageCase.GENITIVE,
            LanguageCase.LOCATIVE,
            LanguageCase.INESSIVE,
            LanguageCase.ALLATIVE,
            LanguageCase.ABLATIVE,
            LanguageCase.INSTRUMENTAL,
            LanguageCase.COMITATIVE,
            LanguageCase.BENEFACTIVE,
            LanguageCase.PARTITIVE
        );
        assertEquals(expectedAllowed, decl.getAllowedCases());
    }

    static Stream<Arguments> nounExactFormCases() {
        return Stream.of(
            Arguments.of(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, BasqueDeclension.BasqueNounForm.PL_N_DEF),
            Arguments.of(LanguageNumber.PLURAL, LanguageCase.ALLATIVE, LanguageArticle.INDEFINITE, BasqueDeclension.BasqueNounForm.PL_ALL_DEF),
            Arguments.of(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, BasqueDeclension.BasqueNounForm.BASE),
            Arguments.of(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.INDEFINITE, BasqueDeclension.BasqueNounForm.SG_N_IND),
            Arguments.of(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.DEFINITE, BasqueDeclension.BasqueNounForm.SG_N_DEF)
        );
    }

    @ParameterizedTest
    @MethodSource("nounExactFormCases")
    void testNounFormMapping(LanguageNumber number, LanguageCase kase, LanguageArticle article, BasqueDeclension.BasqueNounForm expected) {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        assertEquals(expected, decl.getExactNounForm(number, kase, null, article));
    }

    static Stream<Arguments> canonicalFormCases() {
        return Stream.of(
            Arguments.of(LanguageNumber.PLURAL, LanguageArticle.INDEFINITE, BasqueDeclension.BasqueNounForm.PL_N_DEF),
            Arguments.of(LanguageNumber.SINGULAR, LanguageArticle.ZERO, BasqueDeclension.BasqueNounForm.BASE)
        );
    }

    @ParameterizedTest
    @MethodSource("canonicalFormCases")
    void testCanonicalNounForm(LanguageNumber number, LanguageArticle article, BasqueDeclension.BasqueNounForm expected) {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        assertEquals(expected, decl.getCanonicalNounForm(number, article));
    }

    static Stream<Arguments> renderSurfaceCases() {
        return Stream.of(
            Arguments.of("ibar", LanguageCase.ERGATIVE, LanguageNumber.SINGULAR, LanguageArticle.DEFINITE, "ibarrak"),
            Arguments.of("Ur", LanguageCase.GENITIVE, LanguageNumber.SINGULAR, LanguageArticle.DEFINITE, "Uraren"),
            Arguments.of("neska", LanguageCase.GENITIVE, LanguageNumber.SINGULAR, LanguageArticle.DEFINITE, "neskaren"),
            Arguments.of("etxe", LanguageCase.PARTITIVE, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE, "etxerik"),
            Arguments.of("gizon", LanguageCase.PARTITIVE, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE, "gizonik"),
            Arguments.of("etxe", LanguageCase.LOCATIVE, LanguageNumber.PLURAL, LanguageArticle.DEFINITE, "etxeetako")
        );
    }

    @ParameterizedTest
    @MethodSource("renderSurfaceCases")
    void testRenderSurfaceRules(String base, LanguageCase kase, LanguageNumber number, LanguageArticle article, String expected) {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        assertEquals(expected, decl.renderSurface(base, kase, number, article));
    }

    // Direct coverage for helper: getBasqueNounForm(LanguageNumber, LanguageArticle)
    static Stream<Arguments> basqueNounHelperCases() {
        return Stream.of(
            // nulls default to SINGULAR + default article (INDEFINITE)
            Arguments.of(null, null, BasqueDeclension.BasqueNounForm.SG_N_IND),
            // plural uses definite morphology regardless of article
            Arguments.of(LanguageNumber.PLURAL, LanguageArticle.ZERO, BasqueDeclension.BasqueNounForm.PL_N_DEF),
            Arguments.of(LanguageNumber.PLURAL, LanguageArticle.INDEFINITE, BasqueDeclension.BasqueNounForm.PL_N_DEF),
            Arguments.of(LanguageNumber.PLURAL, LanguageArticle.DEFINITE, BasqueDeclension.BasqueNounForm.PL_N_DEF),
            Arguments.of(LanguageNumber.PLURAL, null, BasqueDeclension.BasqueNounForm.PL_N_DEF),
            // singular article-specific mappings
            Arguments.of(LanguageNumber.SINGULAR, LanguageArticle.ZERO, BasqueDeclension.BasqueNounForm.BASE),
            Arguments.of(LanguageNumber.SINGULAR, LanguageArticle.DEFINITE, BasqueDeclension.BasqueNounForm.SG_N_DEF),
            Arguments.of(LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE, BasqueDeclension.BasqueNounForm.SG_N_IND),

            // irregular case like partitive that is never used in Basque
            Arguments.of(LanguageNumber.SINGULAR, LanguageArticle.PARTITIVE, BasqueDeclension.BasqueNounForm.BASE)
        );
    }

    @ParameterizedTest
    @MethodSource("basqueNounHelperCases")
    void testGetBasqueNounForm(LanguageNumber number, LanguageArticle article, BasqueDeclension.BasqueNounForm expected) {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        assertEquals(expected, decl.getBasqueNounForm(number, article));
    }

    /**
     * Tests the renderSurface method for Basque nouns/adjectives with stems ending in "h", including
     * several corner cases:
     * <ul>
     *   <li>Ensures correct handling of a bare "h" (single-letter stem), which should simply passthrough in ABS(Indef).</li>
     *   <li>Checks that the correct suffix variants are chosen for "h"-final stems, both for instrumental and partitive (using consonant variant suffixes).</li>
     *   <li>Verifies that for stems ending in "ah" or "ahh", the vowel/consonant heuristics are correctly applied:
     *       - With "ah", a vowel path suffix should be chosen (e.g. "ri" or "ra").
     *       - With "ahh", the presence of double-h cancels the vowel flag, causing the consonant path to be chosen instead
     *         ("i" or "era").</li>
     * </ul>
     */
    @Test
    void testRenderSurface_HFinalAndCornerCases() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        // ABS(Indef) passthrough for single "h"
        assertEquals("h", decl.renderSurface("h", LanguageCase.NOMINATIVE, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE));
        // Instrumental (indef) uses consonant variant for "h" ("ez")
        assertEquals("hez", decl.renderSurface("h", LanguageCase.INSTRUMENTAL, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE));
        // Partitive (indef-only): consonant variant "ik" after "h"
        assertEquals("hik", decl.renderSurface("h", LanguageCase.PARTITIVE, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE));

        // h-final: treat preceding char for vowel/consonant heuristics
        // Dative (indef): vowel path for "ah" -> "ri"; double-h cancels vowel flag -> "i"
        assertEquals("ahri", decl.renderSurface("ah", LanguageCase.DATIVE, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE));
        assertEquals("ahhi", decl.renderSurface("ahh", LanguageCase.DATIVE, LanguageNumber.SINGULAR, LanguageArticle.INDEFINITE));

        // Allative (definite): vowel path for "ah" -> "ra"; double-h -> consonant path "era"
        assertEquals("ahra", decl.renderSurface("ah", LanguageCase.ALLATIVE, LanguageNumber.SINGULAR, LanguageArticle.DEFINITE));
        assertEquals("ahhera", decl.renderSurface("ahh", LanguageCase.ALLATIVE, LanguageNumber.SINGULAR, LanguageArticle.DEFINITE));
    }

    @Test
    void testGenerateSurfaceFromBase() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        assertEquals("etxeak", decl.generateSurfaceFromBase("etxe", BasqueDeclension.BasqueNounForm.PL_N_DEF));
    }

    @Test
    void testRenderAdjectiveSurface_direct_viaRenderSurface() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        // SG INE definite -> "Berrian"
        assertEquals("Berrian", decl.renderSurface("Berri", BasqueDeclension.BasqueAdjectiveForm.SG_INE.getCase(), BasqueDeclension.BasqueAdjectiveForm.SG_INE.getNumber(), LanguageArticle.DEFINITE));
        // PL ERG definite -> "Berriek"
        assertEquals("Berriek", decl.renderSurface("Berri", BasqueDeclension.BasqueAdjectiveForm.PL_ERG.getCase(), BasqueDeclension.BasqueAdjectiveForm.PL_ERG.getNumber(), LanguageArticle.DEFINITE));
        // SG ALL indefinite -> "Berritara"
        assertEquals("Berritara", decl.renderSurface("Berri", BasqueDeclension.BasqueAdjectiveForm.SG_ALL.getCase(), BasqueDeclension.BasqueAdjectiveForm.SG_ALL.getNumber(), LanguageArticle.INDEFINITE));
    }

    @Test
    void testBasqueDynamicNounForm_direct() {
        // Construct directly and validate accessors and key
        BasqueDeclension.BasqueDynamicNounForm f = new BasqueDeclension.BasqueDynamicNounForm(
            LanguageNumber.PLURAL, LanguageCase.ALLATIVE, LanguageArticle.DEFINITE);
        assertEquals(LanguageNumber.PLURAL, f.getNumber());
        assertEquals(LanguageCase.ALLATIVE, f.getCase());
        assertEquals(LanguageArticle.DEFINITE, f.getArticle());
        assertEquals(LanguagePossessive.NONE, f.getPossessive()); // always NONE for all forms
        assertEquals("1:al:d", f.getKey());

        // Identity equality (no equals override)
        BasqueDeclension.BasqueDynamicNounForm f2 = new BasqueDeclension.BasqueDynamicNounForm(
            LanguageNumber.PLURAL, LanguageCase.ALLATIVE, LanguageArticle.DEFINITE);
        assertNotEquals(f, f2);
    }

    @Test
    void testApproximateNounFormDynamicFallback() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        // Request ERGATIVE with ZERO article: exact maps to BASE (NOMINATIVE/ZERO), so dynamic should be returned to preserve attributes
        var nf = decl.getApproximateNounForm(LanguageNumber.SINGULAR, LanguageCase.ERGATIVE, null, LanguageArticle.ZERO);
        assertFalse(nf instanceof BasqueDeclension.BasqueNounForm);
        assertEquals(LanguageCase.ERGATIVE, nf.getCase());
        assertEquals(LanguageNumber.SINGULAR, nf.getNumber());
        assertEquals(LanguageArticle.ZERO, nf.getArticle());
    }

    @Test
    void testBasqueNounDefaultStringFallback() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        BasqueDeclension.BasqueNoun noun = (BasqueDeclension.BasqueNoun)decl.createNoun("Demo", null, com.force.i18n.grammar.Noun.NounType.OTHER, null,
            null, null, null, false, false);
        // Set only an uncommon override; getDefaultString should still return something (fallback to first value)
        noun.setString("x-pl-gen-def", BasqueDeclension.BasqueNounForm.PL_GEN_DEF);
        assertEquals("x-pl-gen-def", noun.getDefaultString(false));
    }

    @Test
    void basqueNoun_equals_and_hashCode() {
        // equals and hashCode
        BasqueDeclension.BasqueNoun noun = newNoun();
        BasqueDeclension.BasqueNoun noun2 = newNoun();
        assertEquals(noun, noun);
        assertEquals(noun, noun2);
        assertNotEquals(null, noun);
        assertNotEquals(noun, new Object());

        // hashCode
        assertEquals(noun.hashCode(), noun2.hashCode());
        assertNotEquals(noun.hashCode(), new Object().hashCode());

        // Changing flags affects equality: set a base on one
        noun.setString("Ibar", BasqueDeclension.BasqueNounForm.BASE);
        assertNotEquals(noun, noun2);

        // Synchronize base -> equal again
        noun2.setString("Ibar", BasqueDeclension.BasqueNounForm.BASE);
        assertEquals(noun, noun2);
        assertEquals(noun.hashCode(), noun2.hashCode());
    }

    @Test
    void singular_prefers_SG_N_IND_then_BASE_then_SG_N_DEF() {
        BasqueDeclension.BasqueNoun noun = newNoun();
        noun.setString("sg-def", BasqueDeclension.BasqueNounForm.SG_N_DEF);
        noun.setString("base", BasqueDeclension.BasqueNounForm.BASE);
        noun.setString("sg-ind", BasqueDeclension.BasqueNounForm.SG_N_IND);
        assertEquals("sg-ind", noun.getDefaultString(false));

        // Remove SG_N_IND to ensure BASE is next
        BasqueDeclension.BasqueNoun noun2 = newNoun();
        noun2.setString("sg-def", BasqueDeclension.BasqueNounForm.SG_N_DEF);
        noun2.setString("base", BasqueDeclension.BasqueNounForm.BASE);
        assertEquals("base", noun2.getDefaultString(false));

        // Only SG_N_DEF present
        BasqueDeclension.BasqueNoun noun3 = newNoun();
        noun3.setString("sg-def", BasqueDeclension.BasqueNounForm.SG_N_DEF);
        assertEquals("sg-def", noun3.getDefaultString(false));
    }

    @Test
    void singular_last_resort_uses_any_available_value() {
        BasqueDeclension.BasqueNoun noun = newNoun();
        noun.setString("x-pl-erg-def", BasqueDeclension.BasqueNounForm.PL_ERG_DEF);
        assertEquals("x-pl-erg-def", noun.getDefaultString(false));
    }

    @Test
    void plural_prefers_PL_N_DEF() {
        BasqueDeclension.BasqueNoun noun = newNoun();
        noun.setString("sg-def", BasqueDeclension.BasqueNounForm.SG_N_DEF);
        noun.setString("pl-def", BasqueDeclension.BasqueNounForm.PL_N_DEF);
        assertEquals("pl-def", noun.getDefaultString(true));
    }

    @Test
    void plural_last_resort_when_no_PL_N_DEF() {
        BasqueDeclension.BasqueNoun noun = newNoun();
        noun.setString("sg-def", BasqueDeclension.BasqueNounForm.SG_N_DEF);
        assertEquals("sg-def", noun.getDefaultString(true));
    }

    @Test
    void returns_null_when_no_values_defined() {
        BasqueDeclension.BasqueNoun noun = newNoun();
        assertNull(noun.getDefaultString(false));
        assertNull(noun.getDefaultString(true));
    }

    @Test
    void declension_properties_match_expectations() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        assertTrue(decl.hasArticleInNounForm());
        assertTrue(decl.isArticleInNounFormAutoDerived());
        assertEquals(com.force.i18n.grammar.LanguagePosition.POST, decl.getDefaultAdjectivePosition());
    }

    @Test
    void basqueAdjective_equals_and_hashCode() {
        BasqueDeclension decl = (BasqueDeclension)getBasque();
        BasqueDeclension.BasqueAdjective a1 = (BasqueDeclension.BasqueAdjective) decl.createAdjective(
            "New", com.force.i18n.grammar.LanguageStartsWith.CONSONANT, decl.getDefaultAdjectivePosition());
        BasqueDeclension.BasqueAdjective a2 = (BasqueDeclension.BasqueAdjective) decl.createAdjective(
            "New", com.force.i18n.grammar.LanguageStartsWith.CONSONANT, decl.getDefaultAdjectivePosition());

        // Baseline equality semantics: initially equal
        assertEquals(a1, a1);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a1.hashCode());
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(null, a1);
        assertNotEquals(a1, new Object());

        // Changing base value changes flags, so not equal
        a1.setString(BasqueDeclension.BasqueAdjectiveForm.SG_ABS, "Berri");
        assertNotEquals(a1, a2);

        // Synchronize base -> equal again
        a2.setString(BasqueDeclension.BasqueAdjectiveForm.SG_ABS, "Berri");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    // --- Serialization/readResolve tests for stemFlags recomputation ---
    private static <T> T roundTrip(T obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        byte[] bytes = bos.toByteArray();
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes))) {
            @SuppressWarnings("unchecked") T copy = (T) ois.readObject();
            return copy;
        }
    }

    @Test
    void noun_readResolve_recomputes_stemFlags() throws Exception {
        BasqueDeclension decl = (BasqueDeclension) getBasque();
        BasqueDeclension.BasqueNoun noun = (BasqueDeclension.BasqueNoun) decl.createNoun(
            "DemoRS", null, com.force.i18n.grammar.Noun.NounType.OTHER, null, null, null, null, false, false);

        String base = "Ibar"; // ends with 'r' ⇒ expect R flag set
        noun.setString(base, BasqueDeclension.BasqueNounForm.BASE);
        int expected = BasqueDeclension.computeStemFlags(decl.getLanguage().toFoldedCase(base));

        BasqueDeclension.BasqueNoun copy = roundTrip(noun);
        assertEquals(expected, copy.getBasqueStemFlags());
    }

    @Test
    void noun_readResolve_no_base_sets_minus_one() throws Exception {
        BasqueDeclension decl = (BasqueDeclension) getBasque();
        BasqueDeclension.BasqueNoun noun = (BasqueDeclension.BasqueNoun) decl.createNoun(
            "DemoRS2", null, com.force.i18n.grammar.Noun.NounType.OTHER, null, null, null, null, false, false);
        BasqueDeclension.BasqueNoun copy = roundTrip(noun);
        assertEquals(-1, copy.getBasqueStemFlags());
    }

    @Test
    void adjective_readResolve_recomputes_stemFlags() throws Exception {
        BasqueDeclension decl = (BasqueDeclension) getBasque();
        BasqueDeclension.BasqueAdjective adj = (BasqueDeclension.BasqueAdjective) decl.createAdjective(
            "NewRS", com.force.i18n.grammar.LanguageStartsWith.CONSONANT, decl.getDefaultAdjectivePosition());
        String base = "Berri"; // ends with vowel ⇒ expect VOWEL flag set
        adj.setString(BasqueDeclension.BasqueAdjectiveForm.SG_ABS, base);
        int expected = BasqueDeclension.computeStemFlags(decl.getLanguage().toFoldedCase(base));

        BasqueDeclension.BasqueAdjective copy = roundTrip(adj);
        assertEquals(expected, copy.getBasqueStemFlags());
    }

    @Test
    void adjective_readResolve_no_base_sets_minus_one() throws Exception {
        BasqueDeclension decl = (BasqueDeclension) getBasque();
        BasqueDeclension.BasqueAdjective adj = (BasqueDeclension.BasqueAdjective) decl.createAdjective(
            "OldRS", com.force.i18n.grammar.LanguageStartsWith.CONSONANT, decl.getDefaultAdjectivePosition());
        BasqueDeclension.BasqueAdjective copy = roundTrip(adj);
        assertEquals(-1, copy.getBasqueStemFlags());
    }
}
