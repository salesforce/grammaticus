/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;


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
    protected static final ConcurrentUniquefy<NounRefTag> tagMap = new ConcurrentUniquefy<NounRefTag>();

    private final NounForm form;
    private final boolean isCapital; // capital case
    private final boolean escapeHtml;  // Should the entity name be "pre-escaped"
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
        result = result<<2 + (isCapital ? 2 : 0)
             + (escapeHtml ? 1 : 0);
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
        } else {
            return dict.getDeclension().getApproximateNounForm(getForm().getNumber(), getForm().getCase(), getForm().getPossessive(), getForm().getArticle());
        }
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

        if (isDynamic()) {
            if (entities == null) {
                if (!LabelDebug.isLabelHintAllowed()) {
                        logger.log(Level.SEVERE,
                                "Calling getLabel that has an <entity> without providing that entity only allowed in label debug mode");
                }
                s = frm.getNumber().isPlural() ? "<Entities>" : "<Entity>";
                return isCapital ? s : dict.getDeclension().formLowercaseNounForm(s, frm);
            }
            Renameable ei = entities[index];
            Noun n = dict.getDynamicNoun(getName(), ei, true, true);
            if (n != null) {
                s = n.getString(frm, !isCapital);

                if (s == null) {
                    logger.log(Level.INFO, "Noun reference in label files to an undefined form " + getForm() + " for " + getName());
                    // TODO: Gack in development.
                    s = n.getCloseButNoCigarString(frm);
                }

            // For non-renamable standard entity, simply return its label
            } else if (ei.hasStandardLabel()) {
                s = frm.getNumber().isPlural() ? ei.getLabelPlural() : ei.getLabel();
                if (!isCapital)
                    s = dict.getDeclension().formLowercaseNounForm(s, form);
            }
        } else {
            Noun n = dict.getNoun(getName(), true);
            assert n != null : "Couldn't find noun for " + getName();
            s = n.getString(frm, !isCapital);
            if (s == null) {
                logger.log(Level.INFO, "Noun reference in label files to an undefined form " + frm + " for " + getName());
                s = n.getDefaultString(frm.getNumber().isPlural());
                if (!isCapital) s = n.getDeclension().formLowercaseNounForm(s, frm);
                if (s == null && !frm.getNumber().isPlural()) {
                    // TODO: German has some nouns that only have plural versions.  WHAT THE HELL (campaign_member_information)
                    s = n.getDefaultString(true);
                    if (!isCapital) s = n.getDeclension().formLowercaseNounForm(s, frm);
                }
            }
        }


        assert s != null : "No string found for form : " + form + " for " + getName();

        if (escapeHtml) {
            s = TextUtil.escapeToHtml(s);
        }

        return s;
    }

    @Override
    String extraJson(LanguageDictionary dictionary, List<?> terms) {
        // grammaticus.js no longer supports 'legacy' article form such as <account article="a"/>
        if (this.form instanceof LegacyArticledNounForm) {
            logger.log(Level.SEVERE, "Noun reference in label files with legacy article directive for " + getName()
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
            assert !isDynamic() || I18nJavaUtil.isDebugging()
                : "Only allowed in label debug mode, mode: " + I18nJavaUtil.isDebugging()
                    + " isDynamic: " + isDynamic() + " entities: " + entities.length
                    + " reference: " + getReference();
            n = formatter.getNoun(getName(), true);
        }
        return n;
    }

    @Override
    NounRefTag unique() {
        return tagMap.unique(this);
    }
}
