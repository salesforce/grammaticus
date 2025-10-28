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

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.AbstractLanguageDeclension;
import com.force.i18n.grammar.Adjective;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageCase;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageGender;
import com.force.i18n.grammar.LanguageNumber;
import com.force.i18n.grammar.LanguagePosition;
import com.force.i18n.grammar.LanguagePossessive;
import com.force.i18n.grammar.LanguageStartsWith;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.NounForm;

/**
 * Basque declension implementation.
 *
 * <p>
 * Basque inflection (case, number, definiteness) is realized productively at render time from a bare stem;
 * dictionary-stored forms are optional overrides for irregular items. Articles are not separate tokens in Basque;
 * the definiteness/plural morphology is suffixal at the noun phrase edge. Accordingly:
 * </p>
 *
 * <ul>
 *   <li><b>No grammatical gender</b> or starts-with distinctions.</li>
 *   <li><b>Articles are not tokens</b> ( {@link #hasArticle()} returns false), but noun forms carry an article
 *       attribute so dictionary overrides can distinguish ZERO / INDEFINITE / DEFINITE.</li>
 *   <li><b>Number is preserved</b> on forms; plural uses definite morphology in Basque (no plural-indefinite form).</li>
 *   <li><b>Cases:</b> the API uses {@code NOMINATIVE} to represent the Basque <i>absolutive</i>. Additional
 *       postpositional cases are allowed for validation and used by the render-time surface generator
 *       (see {@link #renderSurface(String, LanguageCase, LanguageNumber, LanguageArticle)}).
 *   </li>
 * </ul>
 *
 * <p>
 * Forms and rendering:
 * </p>
 * <ul>
 *   <li>{@link BasqueNounForm}: a compact set of enum forms for optional noun overrides, including
 *       BASE (SG ABS with ZERO article), select singular definite/indefinite, and plural-definite forms.</li>
 *   <li>{@link BasqueAdjectiveForm}: adjective/determiner overrides are keyed by (number, case) only; article is not
 *       part of the form identity and is supplied at render time.</li>
 *   <li>{@link #getApproximateNounForm(LanguageNumber, LanguageCase, LanguagePossessive, LanguageArticle)}
 *       returns either an exact enum form when available or a Basque-specific dynamic form that preserves the requested
 *       (number, case, article) for render-time morphology.</li>
 *   <li>{@link #renderSurface(String, LanguageCase, LanguageNumber, LanguageArticle)}
 *       composes the surface using data-driven suffixes and stem-final rules (e.g., a-absorption, r-doubling).</li>
 * </ul>
 */
public class BasqueDeclension extends AbstractLanguageDeclension {

    public BasqueDeclension(HumanLanguage language) {
        super(language);
    }

    /**
     * Defines Basque noun forms used for optional dictionary overrides.
     *
     * <p>
     * Basque productive morphology (case, number, article) is realized at render time from the bare stem.
     * This enum exists to allow targeted overrides for irregular items; most surfaces are synthesized
     * by the rendering rules (e.g., stem-final analysis, a-absorption, r-doubling).
     * </p>
     *
     * <p>
     * Conventions:
     * <ul>
     *   <li>"NOMINATIVE" here denotes the Basque absolutive case.</li>
     *   <li>Possessive morphology is not supported for nouns (getPossessive() is always NONE).</li>
     *   <li><code>BASE</code> represents singular absolutive with zero article (the bare stem override).</li>
     * </ul>
     * </p>
     *
     * <p>
     * Enumerated overrides:
     * <ul>
     *   <li>Definite plural: <code>PL_*_DEF</code> entries are provided and used for all plural realizations
     *       (Basque plural uses definite morphology); plural indefinite is not used and is not enumerated.</li>
     *   <li>Definite singular core cases: <code>SG_N_DEF</code>, <code>SG_ERG_DEF</code>, <code>SG_DAT_DEF</code>, <code>SG_GEN_DEF</code>.</li>
     *   <li>Indefinite singular: <code>SG_N_IND</code>, <code>SG_ERG_IND</code>, <code>SG_DAT_IND</code>, <code>SG_GEN_IND</code>.</li>
     * </ul>
     * Non-core singular definite cases are not enumerated and are rendered productively.</p>
     *
     * <p>Key format: <code>getKey()</code> composes <code>number:case:article</code> using each attribute's <code>dbValue</code>.</p>
     *
     * <p>For label authoring and adjective behavior, see the class-level Basque documentation.</p>
     */
    public enum BasqueNounForm implements NounForm {
        BASE(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO),
        // Absolutive
        SG_N_DEF(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.DEFINITE),
        PL_N_DEF(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguageArticle.DEFINITE),
        // Definite plural forms (used for all plural realizations)
        PL_ERG_DEF(LanguageNumber.PLURAL, LanguageCase.ERGATIVE, LanguageArticle.DEFINITE),
        PL_DAT_DEF(LanguageNumber.PLURAL, LanguageCase.DATIVE, LanguageArticle.DEFINITE),
        PL_GEN_DEF(LanguageNumber.PLURAL, LanguageCase.GENITIVE, LanguageArticle.DEFINITE),
        PL_LOC_DEF(LanguageNumber.PLURAL, LanguageCase.LOCATIVE, LanguageArticle.DEFINITE),
        PL_INE_DEF(LanguageNumber.PLURAL, LanguageCase.INESSIVE, LanguageArticle.DEFINITE),
        PL_ALL_DEF(LanguageNumber.PLURAL, LanguageCase.ALLATIVE, LanguageArticle.DEFINITE),
        PL_ABL_DEF(LanguageNumber.PLURAL, LanguageCase.ABLATIVE, LanguageArticle.DEFINITE),
        PL_INS_DEF(LanguageNumber.PLURAL, LanguageCase.INSTRUMENTAL, LanguageArticle.DEFINITE),
        PL_COM_DEF(LanguageNumber.PLURAL, LanguageCase.COMITATIVE, LanguageArticle.DEFINITE),
        PL_BEN_DEF(LanguageNumber.PLURAL, LanguageCase.BENEFACTIVE, LanguageArticle.DEFINITE),
        // Definite singular core cases
        SG_ERG_DEF(LanguageNumber.SINGULAR, LanguageCase.ERGATIVE, LanguageArticle.DEFINITE),
        SG_DAT_DEF(LanguageNumber.SINGULAR, LanguageCase.DATIVE, LanguageArticle.DEFINITE),
        SG_GEN_DEF(LanguageNumber.SINGULAR, LanguageCase.GENITIVE, LanguageArticle.DEFINITE),
        // Indefinite singular (plural indefinite is not used; plural uses definite morphology)
        SG_N_IND(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.INDEFINITE),
        SG_ERG_IND(LanguageNumber.SINGULAR, LanguageCase.ERGATIVE, LanguageArticle.INDEFINITE),
        SG_DAT_IND(LanguageNumber.SINGULAR, LanguageCase.DATIVE, LanguageArticle.INDEFINITE),
        SG_GEN_IND(LanguageNumber.SINGULAR, LanguageCase.GENITIVE, LanguageArticle.INDEFINITE);

        private final LanguageNumber number;
        private final LanguageCase kase;
        private final LanguageArticle article;

        BasqueNounForm(LanguageNumber number, LanguageCase kase, LanguageArticle article) {
            this.number = number;
            this.kase = kase;
            this.article = article;
        }

        @Override
        public LanguageArticle getArticle() { return this.article; }
        @Override
        public LanguageCase getCase() { return this.kase; }
        @Override
        public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override
        public LanguageNumber getNumber() { return this.number; }
        @Override
        public String getKey() { return this.number.getDbValue() + ":" + this.kase.getDbValue() + ":" + this.article.getDbValue(); }
    }

    /**
     * Basque adjective forms (case+number pairs) used to key irregular adjective/determiner values.
     * There is no separate <pos/> tag for Basque; these forms are used internally to index
     * lexical adjective values and to map requested case/number pairs.
     *
     * <p>
     * Why no article here (unlike {@link BasqueNounForm})?
     * <ul>
     *   <li>In Basque, article/definiteness is realized at the noun phrase edge (phrasal suffix),
     *       not as an inherent lexical feature of adjectives. Adjectives agree in case/number,
     *       but do not store distinct article-specific lexical variants.</li>
     *   <li>Accordingly, adjective forms are keyed only by <b>(number, case)</b>. The article
     *       is supplied at render time and influences the computed suffix, not the dictionary key.</li>
     *   <li><code>getArticle()</code> always returns ZERO for adjective forms to reflect that
     *       article is not part of the form identity. When rendering, the engine still accepts an
     *       article parameter so surfaces can reflect definiteness where appropriate (e.g.,
     *       demonstratives/determiners acting as the NP edge) via
     *       {@link #renderSurface(String, LanguageCase, LanguageNumber, LanguageArticle)}.</li>
     * </ul>
     * This design keeps the adjective form space compact and avoids article-driven duplication,
     * while still allowing render-time morphology to incorporate article when needed.
     * </p>
     */
    public enum BasqueAdjectiveForm implements AdjectiveForm {
        SG_ABS(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE),
        PL_ABS(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE),
        SG_ERG(LanguageNumber.SINGULAR, LanguageCase.ERGATIVE),
        PL_ERG(LanguageNumber.PLURAL, LanguageCase.ERGATIVE),
        SG_DAT(LanguageNumber.SINGULAR, LanguageCase.DATIVE),
        PL_DAT(LanguageNumber.PLURAL, LanguageCase.DATIVE),
        SG_GEN(LanguageNumber.SINGULAR, LanguageCase.GENITIVE),
        PL_GEN(LanguageNumber.PLURAL, LanguageCase.GENITIVE),
        SG_LOC(LanguageNumber.SINGULAR, LanguageCase.LOCATIVE),
        PL_LOC(LanguageNumber.PLURAL, LanguageCase.LOCATIVE),
        SG_INE(LanguageNumber.SINGULAR, LanguageCase.INESSIVE),
        PL_INE(LanguageNumber.PLURAL, LanguageCase.INESSIVE),
        SG_ALL(LanguageNumber.SINGULAR, LanguageCase.ALLATIVE),
        PL_ALL(LanguageNumber.PLURAL, LanguageCase.ALLATIVE),
        SG_ABL(LanguageNumber.SINGULAR, LanguageCase.ABLATIVE),
        PL_ABL(LanguageNumber.PLURAL, LanguageCase.ABLATIVE),
        SG_INS(LanguageNumber.SINGULAR, LanguageCase.INSTRUMENTAL),
        PL_INS(LanguageNumber.PLURAL, LanguageCase.INSTRUMENTAL),
        SG_COM(LanguageNumber.SINGULAR, LanguageCase.COMITATIVE),
        PL_COM(LanguageNumber.PLURAL, LanguageCase.COMITATIVE),
        SG_BEN(LanguageNumber.SINGULAR, LanguageCase.BENEFACTIVE),
        PL_BEN(LanguageNumber.PLURAL, LanguageCase.BENEFACTIVE),
        SG_PAR(LanguageNumber.SINGULAR, LanguageCase.PARTITIVE);

        private final LanguageNumber number;
        private final LanguageCase kase;

        BasqueAdjectiveForm(LanguageNumber number, LanguageCase kase) {
            this.number = number;
            this.kase = kase;
        }

        @Override
        public LanguageArticle getArticle() {
            return LanguageArticle.ZERO;
        }

        @Override
        public LanguageCase getCase() {
            return this.kase;
        }

        @Override
        public LanguageNumber getNumber() {
            return this.number;
        }

        @Override
        public LanguageGender getGender() {
            return LanguageGender.NEUTER;
        }

        @Override
        public LanguageStartsWith getStartsWith() {
            return LanguageStartsWith.CONSONANT;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return LanguagePossessive.NONE;
        }

        @Override
        public String getKey() {
            return this.number.getDbValue() + ":" + this.kase.getDbValue();
        }
    }

    /**
     * Basque noun with optional per-form overrides.
     *
     * <p>
     * INTERNAL: Implementation detail of {@link BasqueDeclension}. Instances are created via
     * {@link BasqueDeclension#createNoun(String, String, Noun.NounType, String, LanguageStartsWith, LanguageGender, String, boolean, boolean)}.
     * External code should not construct or rely on this concrete type; prefer the {@link Noun}
     * interface and declension APIs. This class may change without notice.
     * </p>
     *
     * <p>
     * Purpose: store only irregular surfaces keyed by {@link BasqueNounForm}. Regular surfaces
     * (case/number/article) are generated at render time from the bare stem; this class does not
     * synthesize surfaces.
     * </p>
     *
     * <p>
     * Behavior:
     * <ul>
     *   <li><b>getAllDefinedValues</b>: returns only explicitly-set overrides.</li>
     *   <li><b>getString(form)</b>: returns a value only when {@code form} is a {@link BasqueNounForm}
     *       and that specific override was provided; otherwise returns {@code null}. No fallback or
     *       render-time synthesis happens here.</li>
     *   <li><b>getDefaultString(isPlural)</b>: preferred fallback when a plain string is requested:
     *       <ul>
     *         <li>singular: try {@code SG_N_IND}, then {@code BASE}, then {@code SG_N_DEF}</li>
     *         <li>plural:   try {@code PL_N_DEF}</li>
     *       </ul>
     *   </li>
     *   <li><b>validateValues</b>: requires at least one override to be present.</li>
     * </ul>
     * Dynamic (number, case, article) requests are preserved by the declension via a dynamic form and
     * realized during rendering; this class only stores explicit overrides.
     * </p>
     */
    public static class BasqueNoun extends Noun {
        private static final long serialVersionUID = 1L;
        private final Map<BasqueNounForm, String> values = new EnumMap<>(BasqueNounForm.class);
        private transient volatile int stemFlags = -1;

        public BasqueNoun(AbstractLanguageDeclension declension, String name, String pluralAlias, NounType type,
                String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, LanguageGender.NEUTER,
                    access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public java.util.Map<? extends NounForm, String> getAllDefinedValues() {
            return java.util.Collections.unmodifiableMap(values);
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            // Prefer the bare stem for Basque nouns
            String v;
            if (!isPlural) {
                v = values.get(BasqueNounForm.SG_N_IND);
                if (v != null) return v;
                v = values.get(BasqueNounForm.BASE);
                if (v != null) return v;
                v = values.get(BasqueNounForm.SG_N_DEF);
                if (v != null) return v;
            } else {
                v = values.get(BasqueNounForm.PL_N_DEF);
                if (v != null) return v;
                // As a last resort, use any available value
            }
            // Any value as last resort
            return values.values().stream().findFirst().orElse(null);
        }

        @Override
        public String getString(NounForm form) {
            // For Basque, only return explicitly defined overrides for the requested form.
            // If the form is not a BasqueNounForm (e.g., a dynamic label-time form), return null
            // so that render-time surface generation composes the correct suffixes from the base.
            if (form instanceof BasqueNounForm bnf) {
                return values.get(bnf);
            }
            return null;
        }

        @Override
        public void setString(String value, NounForm form) {
            values.put((BasqueNounForm) form, value);
            if (value != null) {
                BasqueNounForm bnf = (BasqueNounForm) form;
                if (bnf == BasqueNounForm.BASE || bnf == BasqueNounForm.SG_N_IND || bnf == BasqueNounForm.SG_N_DEF) {
                    String lower = getDeclension().getLanguage().toFoldedCase(value);
                    stemFlags = computeStemFlags(lower);
                }
            }
        }

        @Override
        protected boolean validateValues(String name, LanguageCase caseType) {
            return !this.values.isEmpty();
        }

        public int getBasqueStemFlags() {
            return stemFlags;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BasqueNoun bn) {
                return super.equals(obj) && bn.stemFlags == stemFlags;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), stemFlags);
        }

        @Override
        protected Object readResolve() {
            super.readResolve();
            String base = getDefaultString(false);
            if (base != null) {
                String lower = getDeclension().getLanguage().toFoldedCase(base);
                stemFlags = computeStemFlags(lower);
            } else {
                stemFlags = -1;
            }
            return this;
        }
    }

    /**
     * Dynamic noun form used to carry the requested attributes for render-time morphology
     * without enumerating the full (number × case × article) space in dictionaries.
     *
     * <p>
     * Why it exists:
     * <ul>
     *   <li>Basque surfaces are productively generated from the base; dictionary forms are
     *       optional overrides. When no exact {@link BasqueNounForm} override exists, we still
     *       need to preserve the caller's requested <b>(number, case, article)</b> to compute
     *       the correct suffix at render time.</li>
     *   <li>{@link #getApproximateNounForm(LanguageNumber, LanguageCase, LanguagePossessive, LanguageArticle)}
     *       returns this dynamic form when an exact enum form is unavailable, ensuring the
     *       pipeline keeps the full attribute set through to
     *       {@link #generateSurfaceFromBase(String, NounForm)} and
     *       {@link #renderSurface(String, LanguageCase, LanguageNumber, LanguageArticle)}.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Usage and constraints:
     * <ul>
     *   <li>Not an enum and <b>not</b> intended as a dictionary key; equals/hashCode are
     *       intentionally not overridden to avoid accidental use in enum-keyed maps.</li>
     *   <li><code>getKey()</code> follows the same <code>number:case:article</code> pattern for
     *       consistency/debugging, but should not be used for dictionary indexing.</li>
     *   <li>Transient: created on demand to flow attributes to the render stage; not persisted.</li>
     * </ul>
     * </p>
     */
    static final class BasqueDynamicNounForm implements NounForm {
        private final LanguageNumber number;
        private final LanguageCase kase;
        private final LanguageArticle article;

        BasqueDynamicNounForm(LanguageNumber number, LanguageCase kase, LanguageArticle article) {
            this.number = number;
            this.kase = kase;
            this.article = article;
        }

        @Override
        public LanguageArticle getArticle() { return this.article; }
        @Override
        public LanguageCase getCase() { return this.kase; }
        @Override
        public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override
        public LanguageNumber getNumber() { return this.number; }
        @Override
        public String getKey() { return this.number.getDbValue() + ":" + this.kase.getDbValue() + ":" + this.article.getDbValue(); }

        // Do not override equals/hashCode to avoid being used as keys in maps expecting enum forms
    }

    private static final List<? extends NounForm> ALL_NOUN_FORMS = List.of(BasqueNounForm.values());
    private static final List<? extends AdjectiveForm> ADJ_FORMS = List.of(BasqueAdjectiveForm.values());

    // Compact, data-driven mappings to reduce branching
    private static final class NounForms {
        final BasqueNounForm pluralDef;
        final BasqueNounForm singularDef;
        final BasqueNounForm singularIndef;
        NounForms(BasqueNounForm pluralDef, BasqueNounForm singularDef, BasqueNounForm singularIndef) {
            this.pluralDef = pluralDef;
            this.singularDef = singularDef;
            this.singularIndef = singularIndef;
        }
    }
    private static final EnumMap<LanguageCase, NounForms> NOUN_FORMS = new EnumMap<>(LanguageCase.class);

    private static final class CaseForms<T> {
        final T singular;
        final T plural;
        CaseForms(T singular, T plural) { this.singular = singular; this.plural = plural; }
    }
    private static final EnumMap<LanguageCase, CaseForms<BasqueAdjectiveForm>> ADJ_FORMS_BY_CASE = new EnumMap<>(LanguageCase.class);

    static {
        // Noun forms per case (pluralDef, sgDef, sgInd). Use null where non-core forms fall back to BASE.
        NOUN_FORMS.put(LanguageCase.NOMINATIVE,   new NounForms(BasqueNounForm.PL_N_DEF,  BasqueNounForm.SG_N_DEF,  BasqueNounForm.SG_N_IND));
        NOUN_FORMS.put(LanguageCase.ERGATIVE,     new NounForms(BasqueNounForm.PL_ERG_DEF, BasqueNounForm.SG_ERG_DEF, BasqueNounForm.SG_ERG_IND));
        NOUN_FORMS.put(LanguageCase.DATIVE,       new NounForms(BasqueNounForm.PL_DAT_DEF, BasqueNounForm.SG_DAT_DEF, BasqueNounForm.SG_DAT_IND));
        NOUN_FORMS.put(LanguageCase.GENITIVE,     new NounForms(BasqueNounForm.PL_GEN_DEF, BasqueNounForm.SG_GEN_DEF, BasqueNounForm.SG_GEN_IND));
        NOUN_FORMS.put(LanguageCase.LOCATIVE,     new NounForms(BasqueNounForm.PL_LOC_DEF, null, null));
        NOUN_FORMS.put(LanguageCase.INESSIVE,     new NounForms(BasqueNounForm.PL_INE_DEF, null, null));
        NOUN_FORMS.put(LanguageCase.ALLATIVE,     new NounForms(BasqueNounForm.PL_ALL_DEF, null, null));
        NOUN_FORMS.put(LanguageCase.ABLATIVE,     new NounForms(BasqueNounForm.PL_ABL_DEF, null, null));
        NOUN_FORMS.put(LanguageCase.INSTRUMENTAL, new NounForms(BasqueNounForm.PL_INS_DEF, null, null));
        NOUN_FORMS.put(LanguageCase.COMITATIVE,   new NounForms(BasqueNounForm.PL_COM_DEF, null, null));
        NOUN_FORMS.put(LanguageCase.BENEFACTIVE,  new NounForms(BasqueNounForm.PL_BEN_DEF, null, null));

        // Adjective forms per case
        ADJ_FORMS_BY_CASE.put(LanguageCase.NOMINATIVE,   new CaseForms<>(BasqueAdjectiveForm.SG_ABS, BasqueAdjectiveForm.PL_ABS));
        ADJ_FORMS_BY_CASE.put(LanguageCase.ERGATIVE,     new CaseForms<>(BasqueAdjectiveForm.SG_ERG, BasqueAdjectiveForm.PL_ERG));
        ADJ_FORMS_BY_CASE.put(LanguageCase.DATIVE,       new CaseForms<>(BasqueAdjectiveForm.SG_DAT, BasqueAdjectiveForm.PL_DAT));
        ADJ_FORMS_BY_CASE.put(LanguageCase.GENITIVE,     new CaseForms<>(BasqueAdjectiveForm.SG_GEN, BasqueAdjectiveForm.PL_GEN));
        ADJ_FORMS_BY_CASE.put(LanguageCase.LOCATIVE,     new CaseForms<>(BasqueAdjectiveForm.SG_LOC, BasqueAdjectiveForm.PL_LOC));
        ADJ_FORMS_BY_CASE.put(LanguageCase.INESSIVE,     new CaseForms<>(BasqueAdjectiveForm.SG_INE, BasqueAdjectiveForm.PL_INE));
        ADJ_FORMS_BY_CASE.put(LanguageCase.ALLATIVE,     new CaseForms<>(BasqueAdjectiveForm.SG_ALL, BasqueAdjectiveForm.PL_ALL));
        ADJ_FORMS_BY_CASE.put(LanguageCase.ABLATIVE,     new CaseForms<>(BasqueAdjectiveForm.SG_ABL, BasqueAdjectiveForm.PL_ABL));
        ADJ_FORMS_BY_CASE.put(LanguageCase.INSTRUMENTAL, new CaseForms<>(BasqueAdjectiveForm.SG_INS, BasqueAdjectiveForm.PL_INS));
        ADJ_FORMS_BY_CASE.put(LanguageCase.COMITATIVE,   new CaseForms<>(BasqueAdjectiveForm.SG_COM, BasqueAdjectiveForm.PL_COM));
        ADJ_FORMS_BY_CASE.put(LanguageCase.BENEFACTIVE,  new CaseForms<>(BasqueAdjectiveForm.SG_BEN, BasqueAdjectiveForm.PL_BEN));
        ADJ_FORMS_BY_CASE.put(LanguageCase.PARTITIVE,    new CaseForms<>(BasqueAdjectiveForm.SG_PAR, BasqueAdjectiveForm.SG_PAR));
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return ALL_NOUN_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return getAllNounForms();
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ADJ_FORMS;
    }

    /**
     * Maps (number, article) combinations to the corresponding concrete Basque noun form used for dictionary storage.
     * Used during dictionary fallback to select minimal, valid forms for Basque nouns.
     * Prefer this Basque-specific helper instead of overriding the base class method.
     */
    NounForm getBasqueNounForm(LanguageNumber number, LanguageArticle articleType) {
        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        LanguageArticle art = (articleType == null) ? getDefaultArticle() : articleType;
        if (num.isPlural()) return BasqueNounForm.PL_N_DEF; // plural uses definite morphology
        if (art == LanguageArticle.ZERO) return BasqueNounForm.BASE; // bare stem
        if (art == LanguageArticle.DEFINITE) return BasqueNounForm.SG_N_DEF;
        if (art == LanguageArticle.INDEFINITE) return BasqueNounForm.SG_N_IND;
        return BasqueNounForm.BASE;
    }

    @Override
    public boolean hasArticle() {
        return false;
    }

    @Override
    public boolean hasGender() {
        return false;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    public LanguagePosition getDefaultAdjectivePosition() {
        // Basque adjectives are post-nominal by default
        return LanguagePosition.POST;
    }

    @Override
    public boolean hasPlural() {
        // Preserve number in forms so rendering can realize plural suffixes at label time
        return true;
    }

    @Override
    public boolean hasArticleInNounForm() {
        // Basque noun forms distinguish ZERO/INDEFINITE/DEFINITE in overrides
        return true;
    }

    @Override
    public boolean isArticleInNounFormAutoDerived() {
        // Basque noun forms follow the strict rules of the language, so they are auto-derived
        return true;
    }

    @Override
    public boolean shouldApproximateNounFormsAtParseTime() { return true; }

    @Override
    public String generateSurfaceFromBase(String base, NounForm form) {
        return renderSurface(base, form.getCase(), form.getNumber(), form.getArticle());
    }

    @Override
    public String generateSurfaceFromTerm(com.force.i18n.grammar.Noun noun, com.force.i18n.grammar.NounForm form) {
        if (noun == null) return null;

        String base = noun.getDefaultString(false);
        if (base == null) return null;

        int flags = (noun instanceof BasqueNoun ba) ? ba.getBasqueStemFlags() : -1;
        return flags >= 0
            ? renderSurfaceWithFlags(base, form.getCase(), form.getNumber(), form.getArticle(), flags)
            : renderSurface(base, form.getCase(), form.getNumber(), form.getArticle());
    }

    @Override
    public LanguageArticle getDefaultArticle() {
        // Base forms correspond to ABS(Indef)
        return LanguageArticle.INDEFINITE;
    }

    @Override
    public java.util.Set<LanguageArticle> getAllowedArticleTypes() {
        return java.util.EnumSet.of(LanguageArticle.ZERO, LanguageArticle.INDEFINITE, LanguageArticle.DEFINITE);
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE);
    }

    @Override
    public EnumSet<LanguageCase> getAllowedCases() {
        // Allow the Basque cases used via postpositions
        return EnumSet.of(LanguageCase.NOMINATIVE, // used to represent Absolutive (ABS)
                LanguageCase.ERGATIVE, LanguageCase.DATIVE, LanguageCase.GENITIVE, LanguageCase.INESSIVE,
                LanguageCase.ALLATIVE, LanguageCase.ABLATIVE, LanguageCase.INSTRUMENTAL, LanguageCase.COMITATIVE,
                LanguageCase.BENEFACTIVE,
                LanguageCase.PARTITIVE,
                LanguageCase.LOCATIVE // Local‑Genitive (l)
        );
    }

    @Override
    public NounForm getApproximateNounForm(LanguageNumber number, LanguageCase languageCase, LanguagePossessive possessive, LanguageArticle article) {
        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        LanguageCase kase = (languageCase == null) ? LanguageCase.NOMINATIVE : languageCase;
        LanguageArticle art = (article == null) ? getDefaultArticle() : article;

        // Try exact first
        NounForm exact = getExactNounForm(num, kase, possessive, art);
        // If the exact form preserves the requested attributes, use it (allows dictionary overrides)
        if (exact != null && exact.getNumber() == num && exact.getCase() == kase && exact.getArticle() == art) {
            return exact;
        }
        // Otherwise, fall through to dynamic to preserve requested case/number/article for render-time suffixing
        return new BasqueDynamicNounForm(num, kase, art);
    }

    @Override
    public NounForm getCanonicalNounForm(LanguageNumber number, LanguageArticle articleType) {
        return getBasqueNounForm(number, articleType);
    }

    /**
     * Map (number, case, article) to a focused BasqueNounForm set.
     *
     * This mapping is used only for optional dictionary overrides. The full case/number/article
     * realization (all 12 cases as documented) is still performed by <pos/> at render time.
     * Unknown/unsupported combinations fall back to BASE so that regular rendering proceeds.
     */
    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase languageCase, LanguagePossessive possessive,
            LanguageArticle article) {
        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        LanguageCase kase = (languageCase == null) ? LanguageCase.NOMINATIVE : languageCase;
        LanguageArticle art = (article == null) ? LanguageArticle.ZERO : article;

        NounForms forms = NOUN_FORMS.get(kase);
        if (num.isPlural()) {
            return forms != null && forms.pluralDef != null ? forms.pluralDef : BasqueNounForm.BASE;
        }
        if (art == LanguageArticle.DEFINITE) {
            return forms != null && forms.singularDef != null ? forms.singularDef : BasqueNounForm.BASE;
        }
        if (art == LanguageArticle.INDEFINITE) {
            return forms != null && forms.singularIndef != null ? forms.singularIndef : BasqueNounForm.BASE;
        }
        // ZERO article singular → BASE
        return BasqueNounForm.BASE;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase languageCase, LanguageArticle article, LanguagePossessive possessive) {
        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        LanguageCase kase = (languageCase == null) ? LanguageCase.NOMINATIVE : languageCase;
        CaseForms<BasqueAdjectiveForm> cf = ADJ_FORMS_BY_CASE.get(kase);
        if (cf == null) return SimpleModifierForm.SINGULAR;
        return num.isPlural() ? cf.plural : cf.singular;
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new BasqueAdjective(this, name, position == null ? getDefaultAdjectivePosition() : position);
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
            LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField,
            boolean isCopiedFromDefault) {
        return new BasqueNoun(this, name, pluralAlias, type, entityName, access, isStandardField, isCopiedFromDefault);
    }

    /**
     * Basque adjective/determiner implementation with optional overrides.
     *
     * <p>
     * Keying and article handling:
     * <ul>
     *   <li>Overrides are keyed by {@link BasqueAdjectiveForm} (a pair of <b>number</b> and <b>case</b>).</li>
     *   <li>Adjective forms do <b>not</b> include an article dimension; {@code getArticle()} on forms is ZERO.
     *       Definiteness is applied at render time when composing the surface via
     *       {@link #renderSurface(String, com.force.i18n.grammar.LanguageCase, com.force.i18n.grammar.LanguageNumber, com.force.i18n.grammar.LanguageArticle)}.
     *   </li>
     * </ul>
     * </p>
     *
     * <p>
     * Behavior:
     * <ul>
     *   <li><b>getAllValues()</b>: returns an unmodifiable view of explicitly-set overrides.</li>
     *   <li><b>getString(form)</b>/<b>setString(form, value)</b>: access stored overrides by {@link BasqueAdjectiveForm}.</li>
     *   <li><b>validate(name)</b>: requires at least one value to be defined.</li>
     *   <li>When no explicit value is present for a requested form, label-time rendering synthesizes the surface
     *       from the base via the declension's render path.</li>
     * </ul>
     * </p>
     *
     * <p>
     * INTERNAL: Implementation detail of {@link BasqueDeclension}. Instances are created via
     * {@link BasqueDeclension#createAdjective(String, com.force.i18n.grammar.LanguageStartsWith, com.force.i18n.grammar.LanguagePosition)}.
     * External callers should not depend on this concrete type and should use the {@link com.force.i18n.grammar.Adjective}
     * interface. This class may change without notice.
     * </p>
     */
    public static class BasqueAdjective extends Adjective {
        private static final long serialVersionUID = 1L;

        private final Map<AdjectiveForm, String> values = new HashMap<>();
        private transient volatile int stemFlags = -1;

        public BasqueAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        public java.util.Map<? extends AdjectiveForm, String> getAllValues() {
            return java.util.Collections.unmodifiableMap(values);
        }

        @Override
        public String getString(AdjectiveForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(AdjectiveForm form, String value) {
            values.put(form, value);
            if (value != null && form == BasqueAdjectiveForm.SG_ABS) {
                String lower = getDeclension().getLanguage().toFoldedCase(value);
                stemFlags = computeStemFlags(lower);
            }
        }

        @Override
        public boolean validate(String name) {
            // At least one value must be defined
            return !values.isEmpty();
        }

        public int getBasqueStemFlags() {
            return stemFlags;
        }

        protected Object readResolve() {
            // No superclass readResolve required here.
            String base = values.get(BasqueAdjectiveForm.SG_ABS);
            if (base != null) {
                String lower = getDeclension().getLanguage().toFoldedCase(base);
                stemFlags = computeStemFlags(lower);
            } else {
                stemFlags = -1;
            }
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof BasqueAdjective ba) {
                return super.equals(obj) && ba.stemFlags == stemFlags;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), stemFlags);
        }
    }

    // Bitmask flags for stem endings: 1=a, 2=r, 4=h, 8=vowel, 16=consonantFinal
    static final int A = 1;
    static final int R = 1 << 1;
    static final int H = 1 << 2;
    static final int VOWEL = 1 << 3;

    /** Compute Basque stem-ending flags directly from a lowercased stem. */
    static int computeStemFlags(String lower) {
        boolean endsWithH = lower.endsWith("h");
        char target = lower.charAt(Math.max(0, lower.length() - (endsWithH && lower.length() >= 2 ? 2 : 1)));
        boolean endsWithA = target == 'a';
        boolean endsWithR = target == 'r';
        boolean endsWithVowel = target == 'a' || target == 'e' || target == 'i' || target == 'o' || target == 'u';

        int flags = 0;
        if (endsWithA) flags |= A;
        if (endsWithR) flags |= R;
        if (endsWithH) flags |= H;
        if (endsWithVowel) flags |= VOWEL;
        return flags;
    }

    /** Container for case-specific suffix choices. */
    private static final class SuffixChoices {
        final String indefVowel;
        final String indefCons;
        final String defSingVowel;
        final String defSingCons;
        final String defPlural;

        SuffixChoices(String indefVowel, String indefCons, String defSingVowel, String defSingCons, String defPlural) {
            this.indefVowel = indefVowel;
            this.indefCons = indefCons;
            this.defSingVowel = defSingVowel;
            this.defSingCons = defSingCons;
            this.defPlural = defPlural;
        }
    }

    /** Data-driven mapping of case to suffix choices by definiteness/number and stem ending. */
    private static final EnumMap<LanguageCase, SuffixChoices> SUFFIXES = new EnumMap<>(LanguageCase.class);
    static {
        // For NOMINATIVE (Absolutive): handled specially; values here serve for definite forms
        SUFFIXES.put(LanguageCase.NOMINATIVE,    new SuffixChoices("a",   "a",   "a",   "a",   "ak"));
        SUFFIXES.put(LanguageCase.ERGATIVE,      new SuffixChoices("k",   "ek",  "ak",  "ak",  "ek"));
        SUFFIXES.put(LanguageCase.DATIVE,        new SuffixChoices("ri",  "i",   "ari", "ari", "ei"));
        SUFFIXES.put(LanguageCase.GENITIVE,      new SuffixChoices("ren", "en",  "aren","aren","en"));
        SUFFIXES.put(LanguageCase.LOCATIVE,      new SuffixChoices("ko",  "eko", "ko",  "eko", "etako"));
        SUFFIXES.put(LanguageCase.INESSIVE,      new SuffixChoices("tan", "etan","an",  "ean", "etan"));
        SUFFIXES.put(LanguageCase.ALLATIVE,      new SuffixChoices("tara","etara","ra","era", "etara"));
        SUFFIXES.put(LanguageCase.ABLATIVE,      new SuffixChoices("tatik","etatik","tik","etik","etatik"));
        SUFFIXES.put(LanguageCase.INSTRUMENTAL,  new SuffixChoices("z",   "ez",  "az",  "az",  "ez"));
        SUFFIXES.put(LanguageCase.COMITATIVE,    new SuffixChoices("rekin","ekin","arekin","arekin","ekin"));
        SUFFIXES.put(LanguageCase.BENEFACTIVE,   new SuffixChoices("rentzat","entzat","arentzat","arentzat","entzat"));
        // PARTITIVE handled specially (indef-only): see chooseSuffix
    }

    private static boolean isIndefinite(LanguageNumber number, LanguageArticle article) {
        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        return (num != LanguageNumber.PLURAL) && (article == LanguageArticle.INDEFINITE || article == LanguageArticle.ZERO);
    }

    private static boolean startsWithVowel(String s) {
        if (s == null || s.isEmpty()) return false;
        char c0 = Character.toLowerCase(s.charAt(0));
        return c0 == 'a' || c0 == 'e' || c0 == 'i' || c0 == 'o' || c0 == 'u';
    }

    private static String chooseSuffix(LanguageCase grammaticalCase, boolean isIndef, boolean isPlural, boolean endsWithVowel) {
        if (grammaticalCase == LanguageCase.PARTITIVE) {
            // Partitive is indefinite-only; plural not used
            return endsWithVowel ? "rik" : "ik";
        }
        if (grammaticalCase == LanguageCase.NOMINATIVE && isIndef) {
            // ABS(Indef) uses bare stem
            return "";
        }
        SuffixChoices choices = SUFFIXES.get(grammaticalCase);
        if (choices == null) return "";
        if (isPlural) return choices.defPlural;
        if (isIndef) return endsWithVowel ? choices.indefVowel : choices.indefCons;
        return endsWithVowel ? choices.defSingVowel : choices.defSingCons;
    }

    private static String applyAAbsorption(String stem, String suffix, boolean endsWithA) {
        if (!endsWithA || suffix == null || suffix.isEmpty()) return stem;
        char c0 = Character.toLowerCase(suffix.charAt(0));
        if (c0 == 'a' || c0 == 'e') {
            return stem.substring(0, stem.length() - 1);
        }
        return stem;
    }

    // UR_WORDS contains exceptional stems ending with 'r' that must not undergo r-doubling when suffixes are appended.
    static final Set<String> UR_WORDS = Set.of("ur", "paper", "plater");

    private static String applyRDoubling(String nameLower, String suffix, boolean endsWithR) {
        if (!endsWithR || UR_WORDS.contains(nameLower) || suffix == null || suffix.isEmpty()) return suffix;
        return startsWithVowel(suffix) ? ("r" + suffix) : suffix;
    }

    /**
     * Render the Basque surface form for a bare stem using the requested attributes.
     *
     * <p>
     * INTERNAL: This method is public primarily for testing and internal reuse via public delegate methods
     * (e.g., {@link #generateSurfaceFromBase(String, com.force.i18n.grammar.NounForm)}).
     * It is not a stable external API and may change without notice.
     * </p>
     *
     * <p>
     * Semantics and rules:
     * <ul>
     *   <li><b>NOMINATIVE</b> denotes the Basque <i>absolutive</i> case.</li>
     *   <li><b>ABS(Indef)</b> is the bare stem: when {@code kase == NOMINATIVE} and the request is
     *       indefinite (singular with {@link LanguageArticle#ZERO} or {@link LanguageArticle#INDEFINITE}),
     *       the method returns {@code base} unchanged.</li>
     *   <li><b>Plural</b> uses definite morphology in Basque: plural suffixes are chosen from the definite column.</li>
     *   <li><b>PARTITIVE</b> is indefinite-only and ignores plural; the suffix is {@code -ik} after consonant,
     *       {@code -rik} after vowel.</li>
     *   <li>Otherwise, a data-driven suffix is selected per case/definiteness/number and stem ending (vowel vs consonant).</li>
     *   <li>Post-processing applies <b>a-absorption</b> (drop a final stem 'a' before vowel-initial suffixes)
     *       and <b>r-doubling</b> (prepend 'r' to vowel-initial suffixes for stems ending in 'r', except the
     *       lexical item "ur").</li>
     * </ul>
     * Defaults: {@code kase} defaults to {@link LanguageCase#NOMINATIVE}, {@code number} defaults to
     * {@link LanguageNumber#SINGULAR}. If {@code base} is {@code null}, returns {@code null}.
     * </p>
     *
     * @param base the bare stem to inflect
     * @param kase the requested grammatical case (NOMINATIVE represents absolutive); may be {@code null}
     * @param number the requested number; may be {@code null}
     * @param article the requested article/definiteness; may be {@code null}
     * @return the inflected surface form per Basque morphophonology
     */
    public String renderSurface(String base, LanguageCase kase, LanguageNumber number, LanguageArticle article) {
        if (base == null) return null;
        String stem = base;
        String lower = getLanguage().toFoldedCase(stem);

        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        boolean indef = isIndefinite(num, article);

        LanguageCase grammaticalCase = (kase == null) ? LanguageCase.NOMINATIVE : kase;
        if (grammaticalCase == LanguageCase.NOMINATIVE && indef) return base; // ABS(Indef)

        // If the caller passed a Basque-aware term elsewhere and precomputed flags, prefer using them.
        // Since renderSurface's signature doesn't carry flags, recompute here as a fallback path.
        int stemFlags = computeStemFlags(lower);
        String suffix = chooseSuffix(grammaticalCase, indef, num.isPlural(), (stemFlags & VOWEL) != 0);
        // Post-processing
        stem = applyAAbsorption(stem, suffix, (stemFlags & A) != 0);
        suffix = applyRDoubling(lower, suffix, (stemFlags & R) != 0);
        return stem + suffix;
    }

    /**
     * Internal helper to render using precomputed stem-ending flags when available.
     */
    public String renderSurfaceWithFlags(String base, LanguageCase kase, LanguageNumber number, LanguageArticle article, int stemFlags) {
        if (base == null) return null;
        LanguageNumber num = (number == null) ? LanguageNumber.SINGULAR : number;
        boolean indef = isIndefinite(num, article);
        LanguageCase grammaticalCase = (kase == null) ? LanguageCase.NOMINATIVE : kase;
        if (grammaticalCase == LanguageCase.NOMINATIVE && indef) return base; // ABS(Indef)

        String lower = getLanguage().toFoldedCase(base);
        String suffix = chooseSuffix(grammaticalCase, indef, num.isPlural(), (stemFlags & VOWEL) != 0);
        String stem = applyAAbsorption(base, suffix, (stemFlags & A) != 0);
        suffix = applyRDoubling(lower, suffix, (stemFlags & R) != 0);
        return stem + suffix;
    }
}
