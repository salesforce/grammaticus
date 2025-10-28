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

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.force.i18n.*;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;

/**
 * Noun Tag reference implementation.
 * Constructed through LabelHandler, and used by LabelInfo.
 *
 * @author yoikawa,stamm
 */
class NounRefTag extends TermRefTag {
    private static final long serialVersionUID = 992182143166275835L;

    private static final Logger logger = Logger.getLogger(NounRefTag.class.getName());
    // map for any LabelTag type to reuse. This must be static so all language can share the same entity
    protected static final ConcurrentUniquefy<NounRefTag> tagMap = new ConcurrentUniquefy<>();

    private final NounForm form;
    private final boolean isCapital; // capital case
    private final boolean escapeHtml; // Should the entity name be "pre-escaped"
    // used if this is dynamic reference to entity like <entity entity="0"/>
    private final int index;
    private final int hashCode;

    static NounRefTag getNounTag(String label, Integer refIndex, boolean isCapital, boolean escapeHtml,
            NounForm form) {
        assert label != null;
        return tagMap.unique(new NounRefTag(label, refIndex, form, isCapital, escapeHtml));
    }

    private NounRefTag(String label, Integer refIndex, NounForm form, boolean isCapital, boolean escapeHtml) {
        super(label.toLowerCase());
        assert form != null : "Why are you initializing a noun with an uninitialized form";
        this.index = refIndex == null ? -1 : refIndex;
        this.form = form;
        this.escapeHtml = escapeHtml;
        this.isCapital = isCapital;
        this.hashCode = calcHashCode();
    }

    private int calcHashCode() {
        int result = super.hashCode();
        final int PRIME = 37;
        result = result * PRIME + (form == null ? 0 : form.hashCode());
        result = result * PRIME + index;
        result = result << 2 + (isCapital ? 2 : 0) + (escapeHtml ? 1 : 0);
        return result;
    }

    public Integer getReference() {
        return isDynamic() ? index : null;
    }

    @Override
    protected TermType getType() {
        return TermType.Noun;
    }

    @Override
    public boolean isDynamic() {
        return this.index >= 0;
    }

    @Override
    public boolean isCapital() {
        return this.isCapital;
    }

    public final NounForm getForm() {
        return this.form;
    }

    @Override
    public NounForm getForm(LanguageDictionary dict, boolean overrideForm) {
        if (!overrideForm) {
            return getForm();
        }
        // For declensions that generate noun surfaces at render time (like Basque), the form was set as an approximate/dynamic one during label parsing,
        // so re-running approximation would be redundant and could lose the intended attributes. Just return the stored form.
        if (dict.getDeclension().shouldApproximateNounFormsAtParseTime()) {
            return getForm();
        }
        return dict.getDeclension().getApproximateNounForm(getForm().getNumber(), getForm().getCase(), getForm().getPossessive(), getForm().getArticle());
    }

    // leaves the number as part of the inflection, but removes possession, case, and article
    public NounRefTag makeUninflected(LanguageDictionary dictionary) {
        return NounRefTag.getNounTag(getName(), this.index, this.isCapital, this.escapeHtml,
                dictionary.getDeclension().getNounForm(this.form.getNumber(), LanguageArticle.ZERO));
    }

    // Override the article
    public NounRefTag makeArticled(LanguageDictionary dictionary, LanguageArticle article) {
        return NounRefTag.getNounTag(getName(), this.index, this.isCapital, this.escapeHtml,
                dictionary.getDeclension().getApproximateNounForm(this.form.getNumber(), this.form.getCase(), this.form.getPossessive(), article));
    }

    @Override
    public String getKey() {
        StringBuilder sb = new StringBuilder(getName().toLowerCase()).append(SEP);
        sb.append(getForm().getKey()).append(SEP).append(isCapital ? "1" : "0");
        if (isDynamic())
            sb.append(this.index).append(SEP);
        return sb.toString();
    }

    @Override
    public String toString(LanguageDictionary dict, boolean overrideForms, Object[] vals, Renameable... entities) {
        String s = null;

        NounForm frm = getForm(dict, overrideForms);

        // Preserve original early-return behavior for missing dynamic entity (no HTML escaping)
        if (isDynamic() && entities == null) {
            if (!LabelDebug.isLabelHintAllowed()) {
                logger.log(Level.SEVERE, "Calling getLabel that has an <entity> without providing that entity only allowed in label debug mode");
            }
            String placeholder = frm.getNumber().isPlural() ? "<Entities>" : "<Entity>";
            return isCapital ? placeholder : dict.getDeclension().formLowercaseNounForm(placeholder, frm);
        }

        // Dynamic vs static rendering
        s = isDynamic() ? renderDynamic(dict, frm, entities) : renderStatic(dict, frm);

        // Language-specific fallback generation from term if still missing
        if (s == null) {
            s = generateFromTermIfMissing(dict, frm, entities);
        }

        assert s != null : "No string found for form : " + form + " for " + getName();

        if (escapeHtml) {
            s = TextUtil.escapeToHtml(s);
        }

        return s;
    }

    private String renderDynamic(LanguageDictionary dict, NounForm frm, Renameable... entities) {
        Renameable entity = getEntityAtIndexOrNull(entities);
        if (entity == null) return null;

        Noun n = dict.getDynamicNoun(getName(), entity, true, true);
        if (n != null) {
            String s = n.getString(frm, !isCapital);
            if (s != null) return s;

            // If explicit form is missing, try language-specific surface generation from the term
            String generated = dict.getDeclension().generateSurfaceFromTerm(n, frm);
            if (generated != null) {
                return isCapital ? generated : n.getDeclension().formLowercaseNounForm(generated, frm);
            }
            logger.info(() -> "Noun reference in label files to an undefined form " + getForm() + " for " + getName());
            return n.getCloseButNoCigarString(frm);
        }

        // For non-renamable standard entity, simply return its label
        if (entity.hasStandardLabel()) {
            String s = frm.getNumber().isPlural() ? entity.getLabelPlural() : entity.getLabel();
            return isCapital ? s : dict.getDeclension().formLowercaseNounForm(s, frm);
        }
        return null;
    }

    private String renderStatic(LanguageDictionary dict, NounForm frm) {
        Noun n = dict.getNoun(getName(), true);
        assert n != null : "Couldn't find noun for " + getName();

        String s = n.getString(frm, !isCapital);
        if (s != null) return s;

        // If the declension can generate surfaces from base, use it when explicit form is absent
        String generated = dict.getDeclension().generateSurfaceFromBase(n.getDefaultString(false), frm);
        if (generated != null) {
            return isCapital ? generated : n.getDeclension().formLowercaseNounForm(generated, frm);
        }

        logger.info(() -> "Noun reference in label files to an undefined form " + frm + " for " + getName());
        s = n.getDefaultString(frm.getNumber().isPlural());
        if (s != null) {
            return isCapital ? s : n.getDeclension().formLowercaseNounForm(s, frm);
        }
        if (!frm.getNumber().isPlural()) {
            // TODO: German has some nouns that only have plural versions. WHAT THE HELL (campaign_member_information)
            s = n.getDefaultString(true);
            if (s != null) {
                return isCapital ? s : n.getDeclension().formLowercaseNounForm(s, frm);
            }
        }
        return null;
    }

    private String generateFromTermIfMissing(LanguageDictionary dict, NounForm frm, Renameable... entities) {
        Noun n;
        if (isDynamic()) {
            Renameable entity = getEntityAtIndexOrNull(entities);
            n = entity != null ? dict.getDynamicNoun(getName(), entity, true, false) : dict.getNoun(getName(), true);
        } else {
            n = dict.getNoun(getName(), true);
        }
        String generated = dict.getDeclension().generateSurfaceFromTerm(n, frm);
        if (generated == null) return null;

        return isCapital ? generated : n.getDeclension().formLowercaseNounForm(generated, frm);
    }

    private Renameable getEntityAtIndexOrNull(Renameable... entities) {
        return (entities != null && index >= 0 && index < entities.length) ? entities[index] : null;
    }

    @Override
    String extraJson(LanguageDictionary dictionary, List<?> terms) {
        // grammaticus.js no longer supports 'legacy' article form such as <account article="a"/>
        if (this.form instanceof LegacyArticledNounForm) {
            logger.severe(() -> "Noun reference in label files with legacy article directive for " + getName()
                    + ". this is no longer supported in Javascript. please use proper article tag such as \"<a/>\".");
        }

        if (index >= 0) {
            return ",\"i\":" + index;
        } else {
            return "";
        }
    }

    @Override
    protected boolean equalsValue(TermRefTag obj) {
        return Objects.equals(this.form, ((NounRefTag)obj).form)
            && this.index == ((NounRefTag)obj).index
            && this.escapeHtml == ((NounRefTag)obj).escapeHtml
            && this.isCapital == ((NounRefTag)obj).isCapital;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NounRefTag) {
            return super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    Noun resolveNoun(LanguageDictionary formatter, Renameable[] entities) {
        // Get the relevant noun to see get the right value for StartsWith/Gender
        Noun n;
        if (isDynamic() && entities != null && getReference() < entities.length) {
            Renameable ei = entities[getReference()];
            n = formatter.getDynamicNoun(getName(), ei, true, false);
        } else {
            checkState(!isDynamic() || I18nJavaUtil.isDebugging(),
                    "Only allowed in label debug mode, mode: %s isDynamic: %s entities: %s reference: %s",
                    I18nJavaUtil.isDebugging(), isDynamic(),
                    entities == null ? "null" : Integer.toString(entities.length), getReference());
            n = formatter.getNoun(getName(), true);
        }
        return n;
    }

    @Override
    NounRefTag unique() {
        return tagMap.unique(this);
    }
}
