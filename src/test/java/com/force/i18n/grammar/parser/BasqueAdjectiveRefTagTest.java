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

package com.force.i18n.grammar.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.AttributesImpl;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.Adjective;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageCase;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageNumber;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.NounForm;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;

/**
 * Unit tests for BasqueAdjectiveRefTag, verifying uniqueness, instance pooling,
 * and behavior relative to the AdjectiveRefTag superclass.
 */
class BasqueAdjectiveRefTagTest {

    private TermAttributes newAttrs(LanguageDeclension decl) {
        // useDefaults=false so we only set explicit overrides when provided
        return new TermAttributes(decl, new AttributesImpl(), false);
    }

    // --- Helpers -----------------------------------------------------------
    private LanguageDictionary newBasqueDict() {
        HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");
        return new LanguageDictionary(eu);
    }

    private NounRefTag newAssocNoun(LanguageDictionary dict, String name) {
        LanguageDeclension decl = dict.getDeclension();
        String nameLower = name.toLowerCase();
        Noun noun = dict.createNoun(nameLower, null, com.force.i18n.grammar.Noun.NounType.OTHER, null,
            decl.getDefaultStartsWith(), decl.getDefaultGender(), null, false, false);
        dict.put(nameLower, noun);
        NounForm nf = decl.getApproximateNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, null, LanguageArticle.ZERO);
        return NounRefTag.getNounTag(name, null, false, false, nf);
    }

    private TermAttributes overrides(LanguageDeclension decl, LanguageCase kase, LanguageNumber number, LanguageArticle article) {
        AttributesImpl atts = new AttributesImpl();
        if (number == LanguageNumber.PLURAL) {
            atts.addAttribute("", TermAttributes.PLURAL, TermAttributes.PLURAL, "CDATA", TermAttributes.YES);
        }
        if (kase != null) {
            atts.addAttribute("", TermAttributes.CASE, TermAttributes.CASE, "CDATA", kase.getDbValue());
        }
        if (article != null) {
            atts.addAttribute("", TermAttributes.ARTICLE, TermAttributes.ARTICLE, "CDATA", article.getDbValue());
        }
        return new TermAttributes(decl, atts, false);
    }

    private Adjective putAdjective(LanguageDictionary dict, String name) {
        LanguageDeclension decl = dict.getDeclension();
        Adjective adj = decl.createAdjective(name, decl.getDefaultStartsWith(), decl.getDefaultAdjectivePosition());
        dict.put(name.toLowerCase(), adj);
        return adj;
    }

    @Test
    void unique_usesBasquePoolAndDoesNotCollideWithSuperPool() {
        HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");
        LanguageDeclension decl = LanguageDeclensionFactory.get().getDeclension(eu);

        // Minimal noun ref (entity placeholder) to satisfy constructor signature; can be null safely here
        NounRefTag nounRef = null;

        AdjectiveRefTag a1 = BasqueAdjectiveRefTag.getAdjectiveRefTag(
            "new", nounRef, nounRef, false, newAttrs(decl));
        AdjectiveRefTag a2 = BasqueAdjectiveRefTag.getAdjectiveRefTag(
            "new", nounRef, nounRef, false, newAttrs(decl));

        // Same key should return pooled canonical instance
        assertSame(a1, a2);

        // Creating a superclass instance with same parameters should not collide with Basque pool
        AdjectiveRefTag superAdj = AdjectiveRefTag.getAdjectiveRefTag(
            "new", nounRef, nounRef, false, newAttrs(decl));
        assertNotSame(a1, superAdj);

        // unique() must return the same instance for Basque and never swap to superclass type
        assertSame(a1, a1.unique());
        assertInstanceOf(BasqueAdjectiveRefTag.class, a1);
        assertInstanceOf(AdjectiveRefTag.class, superAdj);
        assertFalse(superAdj instanceof BasqueAdjectiveRefTag);
    }

    @Test
    void superclassUniqueCanonical_forSameKey() {
        HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");
        LanguageDeclension decl = LanguageDeclensionFactory.get().getDeclension(eu);
        NounRefTag nounRef = null;

        AdjectiveRefTag a1 = AdjectiveRefTag.getAdjectiveRefTag("new", nounRef, nounRef, false, newAttrs(decl));
        AdjectiveRefTag a2 = AdjectiveRefTag.getAdjectiveRefTag("new", nounRef, nounRef, false, newAttrs(decl));
        assertSame(a1, a2);
        assertSame(a1, a1.unique());
    }

    @Test
    void poolKeyDifferentiation_byOverridesAndCapitalization() {
        HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");
        LanguageDeclension decl = LanguageDeclensionFactory.get().getDeclension(eu);

        NounRefTag nounRef = null;

        // Different capitalization flag should yield different pooled instances
        AdjectiveRefTag lower = BasqueAdjectiveRefTag.getAdjectiveRefTag(
            "new", nounRef, nounRef, false, newAttrs(decl));
        AdjectiveRefTag upper = BasqueAdjectiveRefTag.getAdjectiveRefTag(
            "new", nounRef, nounRef, true, newAttrs(decl));
        assertNotSame(lower, upper);

        // Different overrides (plural vs singular) should yield different pooled instances
        TermAttributes singular = overrides(decl, LanguageCase.NOMINATIVE, LanguageNumber.SINGULAR, decl.getDefaultArticle());
        TermAttributes plural = overrides(decl, LanguageCase.NOMINATIVE, LanguageNumber.PLURAL, decl.getDefaultArticle());
        AdjectiveRefTag s = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", nounRef, nounRef, false, singular);
        AdjectiveRefTag p = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", nounRef, nounRef, false, plural);
        assertNotSame(s, p);
    }

    @Test
    void integrationThroughHandler_sanity() throws Exception {
        // Ensure handler pipeline produces BasqueAdjectiveRefTag-backed rendering without exceptions
        BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("helper") {};
        helper.setUp();
        HumanLanguage eu = LanguageProviderFactory.get().getLanguage("eu");
        String rendered = helper.renderLabel(eu, "<the/> <new entity=\"Account\"/> <account/>");
        assertNotNull(rendered);
        assertFalse(rendered.isEmpty());
        // Result should be a plain string, not markup
        assertFalse(rendered.contains("<"));
    }




    @Test
    void toString_emptyEdgeCases_and_explicitVsSynthesis() {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();

        // Empty edge cases
        NounRefTag noun = newAssocNoun(dict, "Account");
        TermAttributes taDefault = overrides(decl, LanguageCase.NOMINATIVE, LanguageNumber.SINGULAR, decl.getDefaultArticle());
        AdjectiveRefTag missingModifier = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, taDefault);
        assertEquals("", missingModifier.toString(dict, true, null));

        putAdjective(dict, "new");
        AdjectiveRefTag noNoun = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", null, null, false, taDefault);
        assertEquals("", noNoun.toString(dict, true, null));

        // Explicit vs synthesis
        Adjective adj = (Adjective)dict.getAdjective("new");
        AdjectiveForm defForm = decl.getAdjectiveForm(
            decl.getDefaultStartsWith(), decl.getDefaultGender(), LanguageNumber.SINGULAR, decl.getDefaultCase(), decl.getDefaultArticle(), decl.getDefaultPossessive());
        dict.setString(adj, defForm, "FoObAr");

        TermAttributes taNoOverrides = overrides(decl, null, null, null);
        AdjectiveRefTag lower = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, taNoOverrides);
        AdjectiveRefTag upper = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, true, taNoOverrides);
        assertEquals("foobar", lower.toString(dict, true, null));
        assertEquals("FoObAr", upper.toString(dict, true, null));

        // Force synthesis by making explicit == tag name for target form, and providing base default
        AdjectiveForm target = decl.getAdjectiveForm(
            decl.getDefaultStartsWith(), decl.getDefaultGender(), LanguageNumber.PLURAL, LanguageCase.ERGATIVE, LanguageArticle.DEFINITE, decl.getDefaultPossessive());
        dict.setString(adj, target, "new");
        dict.setString(adj, defForm, "Berri");
        TermAttributes taErgPlDef = overrides(decl, LanguageCase.ERGATIVE, LanguageNumber.PLURAL, LanguageArticle.DEFINITE);
        AdjectiveRefTag synth = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, true, taErgPlDef);
        assertEquals("Berriek", synth.toString(dict, true, null));

        // Base default missing -> empty
        LanguageDictionary dict2 = newBasqueDict();
        LanguageDeclension decl2 = dict2.getDeclension();
        NounRefTag noun2 = newAssocNoun(dict2, "Account");
        putAdjective(dict2, "new");
        TermAttributes taErgPlDef2 = overrides(decl2, LanguageCase.ERGATIVE, LanguageNumber.PLURAL, LanguageArticle.DEFINITE);
        AdjectiveRefTag noBase = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun2, noun2, false, taErgPlDef2);
        assertEquals("", noBase.toString(dict2, true, null));
    }

    @Test
    void fixupModifier_variants_doNotThrow_andYieldStableStrings() {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();

        // Prepare noun and two adjectives with explicit base forms
        NounRefTag noun = newAssocNoun(dict, "Account");
        Adjective adjNew = putAdjective(dict, "new");
        Adjective adjOld = putAdjective(dict, "old");

        AdjectiveForm baseForm = decl.getAdjectiveForm(
            decl.getDefaultStartsWith(), decl.getDefaultGender(),
            LanguageNumber.SINGULAR, decl.getDefaultCase(), decl.getDefaultArticle(), decl.getDefaultPossessive());
        dict.setString(adjNew, baseForm, "new");
        dict.setString(adjOld, baseForm, "old");

        TermAttributes noOverrides = overrides(decl, null, null, null);
        AdjectiveRefTag tagNew = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, noOverrides);
        AdjectiveRefTag tagOld = BasqueAdjectiveRefTag.getAdjectiveRefTag("old", noun, noun, false, noOverrides);

        // No next term (uses noun as next) using NounForm variant
        NounForm nounForm = decl.getApproximateNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, null, LanguageArticle.ZERO);
        ModifierRefTag fixed1 = tagNew.fixupModifier(noun, noun, nounForm);
        String s1 = fixed1.toString(dict, true, null);
        assertNotNull(s1);

        // With next adjective as lookahead (two-arg variant)
        ModifierRefTag fixed2 = tagNew.fixupModifier(noun, tagOld);
        String s2 = fixed2.toString(dict, true, null);
        assertNotNull(s2);

        // With article override (returns ModifierRefTag)
        ModifierRefTag fixed3 = tagOld.fixupModifier(noun, noun, LanguageArticle.DEFINITE);
        String s3 = fixed3.toString(dict, true, null);
        assertNotNull(s3);
    }

    @Test
    void equals_hashCode_and_extraJson_mapping() {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();

        // Build noun and two adjective refs with same key
        NounRefTag noun = newAssocNoun(dict, "Account");
        TermAttributes noOverrides = overrides(decl, null, null, null);
        AdjectiveRefTag a = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, noOverrides);
        AdjectiveRefTag b = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, noOverrides);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        // Different nextTerm should not be equal
        AdjectiveRefTag c = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, null, false, noOverrides);
        assertNotEquals(a, c);

        // extraJson should include indices for associated noun (an) and next term (nt)
        java.util.List<Object> list = new java.util.ArrayList<>();
        list.add("prefix");
        list.add(noun); // nextTermRef for 'a' is the noun; include it to be indexed
        String json = a.toJson(dict, list);
        assertTrue(json.contains("\"an\":1"));
        assertTrue(json.contains("\"nt\":1"));
    }

    @Test
    void missingModifier_and_missingAdjectiveName_paths() {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();

        // Missing modifier in dictionary but overrideForms=true should yield empty string, not exception
        NounRefTag noun = newAssocNoun(dict, "Account");
        AdjectiveRefTag missing = BasqueAdjectiveRefTag.getAdjectiveRefTag("does_not_exist", noun, noun, false, overrides(decl, null, null, null));
        String s = missing.toString(dict, true, null);
        assertEquals("", s);
    }

    @Test
    void articleOverride_affectsForm_andCasing() {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();

        // Prepare adjective with distinct strings for ZERO vs DEFINITE article forms
        Adjective adj = putAdjective(dict, "shape");
        NounRefTag noun = newAssocNoun(dict, "Account");

        AdjectiveForm zeroForm = decl.getAdjectiveForm(
            decl.getDefaultStartsWith(), decl.getDefaultGender(),
            LanguageNumber.SINGULAR, decl.getDefaultCase(), LanguageArticle.ZERO, decl.getDefaultPossessive());
        AdjectiveForm defForm = decl.getAdjectiveForm(
            decl.getDefaultStartsWith(), decl.getDefaultGender(),
            LanguageNumber.SINGULAR, decl.getDefaultCase(), LanguageArticle.DEFINITE, decl.getDefaultPossessive());

        dict.setString(adj, zeroForm, "ZeroFormX");
        dict.setString(adj, defForm, "DeFFormY");

        TermAttributes noOverrides = overrides(decl, null, null, null);
        // lower (folded): expect lowercase of DeFFormY
        AdjectiveRefTag lower = BasqueAdjectiveRefTag.getAdjectiveRefTag("shape", noun, noun, false, noOverrides);
        String lowerOut = lower.fixupModifier(noun, noun, LanguageArticle.DEFINITE).toString(dict, true, null);
        assertEquals("defformy", lowerOut);

        // upper (preserve case): expect exactly DeFFormY
        AdjectiveRefTag upper = BasqueAdjectiveRefTag.getAdjectiveRefTag("shape", noun, noun, true, noOverrides);
        String upperOut = upper.fixupModifier(noun, noun, LanguageArticle.DEFINITE).toString(dict, true, null);
        assertEquals("DeFFormY", upperOut);
    }

    @Test
    void serialization_roundTrip_returnsCanonical_fromPool() throws Exception {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();
        NounRefTag noun = newAssocNoun(dict, "Account");
        TermAttributes noOverrides = overrides(decl, null, null, null);

        AdjectiveRefTag original = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, noOverrides);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }
        AdjectiveRefTag roundTripped;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()))) {
            Object obj = ois.readObject();
            roundTripped = (AdjectiveRefTag)obj;
        }

        // readResolve should have canonicalized; identity with pool instance should hold
        AdjectiveRefTag pooled = BasqueAdjectiveRefTag.getAdjectiveRefTag("new", noun, noun, false, noOverrides);
        assertSame(pooled, roundTripped);
        assertSame(original.unique(), roundTripped);
    }

    @Test
    void synthesizeFromBase_flagsMinusOne_usesRenderSurface() {
        LanguageDictionary dict = newBasqueDict();
        LanguageDeclension decl = dict.getDeclension();

        // Create adjective with only a non-base form set so stemFlags remain -1
        Adjective adj = putAdjective(dict, "rough");
        dict.setString(adj, com.force.i18n.grammar.impl.BasqueDeclension.BasqueAdjectiveForm.SG_ERG, "RoughERG");

        NounRefTag noun = newAssocNoun(dict, "Account");
        TermAttributes noOverrides = overrides(decl, null, null, null);
        AdjectiveRefTag tag = BasqueAdjectiveRefTag.getAdjectiveRefTag("rough", noun, noun, false, noOverrides);

        // This should go through synthesizeFromBase with flags == -1 branch
        String out = tag.toString(dict, true, null);
        assertNotNull(out);
        assertFalse(out.isEmpty());
    }
}
