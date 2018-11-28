/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.util.logging.Logger;

import com.force.i18n.I18nJavaUtil;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.*;


/**
 * A reference to a modifier in a label file.
 *
 * @author yoikawa, stamm
 */
abstract class ModifierRefTag extends TermRefTag {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final boolean isCapital;
    private final NounRefTag associatedNounRef; // entity that this modifier associated with.
    private final TermRefTag nextTermRef;       // The term that comes after this one
    private final TermAttributes overrides;  // TODO:  Overrides *could* be completely unnecessary

    private static final Logger logger = Logger.getLogger(ModifierRefTag.class.getName());


    /**
     * helper method to keep/find exactly the same data for reuse.
     */
    public String getKey(String name, NounRefTag nounTag, boolean isCapital, TermAttributes overrides) {
        // Note that the name identifies modifier itself, so there's no reason to add modifier into the key
        StringBuilder sb = new StringBuilder(name.toLowerCase()).append(SEP).append(isCapital ? "1" : "0");
        if (nounTag != null) sb.append(SEP).append(nounTag);
        if (overrides != null) sb.append(SEP).append(overrides);
        return sb.toString();
    }

    ModifierRefTag(String name, NounRefTag nounTag, TermRefTag nextTerm, boolean isCapital, TermAttributes overrides) {
        super(name.toLowerCase());

        this.associatedNounRef = nounTag;
        this.nextTermRef = nextTerm;
        this.isCapital = isCapital;
        this.overrides = overrides;
    }

    @Override
    public String getKey() {
        return getKey(getName(), getAssociatedNounRef(), isCapital(), getDeclensionOverrides());
    }

    /**
     * @return whether this modifier was denoted in the label fields with a capital letter
     */
    protected final boolean isCapital() {
        return this.isCapital;
    }

    protected abstract NounModifier resolveModifier(LanguageDictionary dictionary);

    protected abstract ModifierForm getForm(LanguageDictionary dictionary, NounForm associatedNounForm, Noun noun, GrammaticalTerm nextTerm);


    @Override
    public GrammaticalForm getForm(LanguageDictionary dictionary, boolean overrideForms) {
        if (getAssociatedNounRef() == null) return null;
        NounForm nounForm = getAssociatedNounRef().getForm(dictionary, overrideForms);
        GrammaticalTerm nextTerm = resolveModifier(dictionary);
        return getForm(dictionary, nounForm, dictionary.getNoun(getAssociatedNounRef().getName(), true), nextTerm);
    }

    @Override
    public String toString(LanguageDictionary formatter, boolean overrideForms, Renameable... entities) {
        NounModifier modifier = resolveModifier(formatter);

        if (modifier == null && overrideForms) {
            logger.info("Missing modifier " + getName() + " for " + formatter.getLanguage());
            return ""; //This is the "legacy" behavior, needed for LabelParserComparisonTest.  It should be modifier.getDefaultValue();
        }

        assert modifier != null : "Can't find modifier '" + getName() + "'";

        if (getAssociatedNounRef() == null) {
            return ""; // This is the "legacy" behavior, needed for LabelParserComparisonTest.  It should be modifier.getDefaultValue();
        }

        NounForm nounForm = associatedNounRef.getForm(formatter, overrideForms);

        // Get the relevant noun to see get the right value for StartsWith/Gender
        Noun n;
        if (associatedNounRef.isDynamic() && entities != null) {
            Renameable ei = entities[associatedNounRef.getReference()];
            n = formatter.getDynamicNoun(associatedNounRef.getName(), ei, true, false);
        } else {
            assert !associatedNounRef.isDynamic() || I18nJavaUtil.isDebugging()
                    : "Only allowed in label debug mode, mode: " + I18nJavaUtil.isDebugging()
                    + " isDynamic: " + associatedNounRef.isDynamic();
            n = formatter.getNoun(associatedNounRef.getName(), true);
        }

        TermRefTag nextTermTag = getNextTerm();
        GrammaticalTerm nextTerm = null;
        if (nextTermTag != null && nextTermTag.isAdjective()) {
            nextTerm = formatter.getAdjective(nextTermTag.getName());
        } else {
            nextTerm = n;  // Use the noun by default
        }

        ModifierForm adjForm = getForm(formatter, nounForm, n, nextTerm);

        String s = modifier.getString(adjForm);

        if (s == null) {
            logger.warning("INFORMATIONAL: Invalid modifier: trying to access " + adjForm + " for modifier " + getName() + " and not defined for " + formatter.getLanguage().getLocaleString());
            return "";
        }
        if (!isCapital) {
            // Make sure that the modifier value is lowercase
            s = formatter.getLanguage().toFoldedCase(s);
        }
        return s;
    }

    public NounRefTag getAssociatedNounRef() {
        return associatedNounRef;
    }

    public TermAttributes getDeclensionOverrides() {
        return overrides;
    }

    public TermRefTag getNextTerm() {
        return nextTermRef;
    }


    @Override
    protected boolean equalsValue(TermRefTag obj) {
        ModifierRefTag otherTag = (ModifierRefTag) obj;
        return this.getName().equals(otherTag.getName())
                && (this.associatedNounRef == null ? otherTag.associatedNounRef == null : this.associatedNounRef.equals(otherTag.associatedNounRef))
                && (this.nextTermRef == null ? otherTag.nextTermRef == null : this.nextTermRef.equals(otherTag.nextTermRef))
                && this.isCapital == otherTag.isCapital
                && this.overrides.equals(otherTag.overrides);
    }

    public ModifierRefTag fixupModifier(NounRefTag nounTag, TermRefTag nextTermRef) {
        return fixupModifier(nounTag, nextTermRef, null);
    }

    public ModifierRefTag fixupModifier(NounRefTag nounTag, TermRefTag nextTermRef, LanguageArticle override) {
        // defaultEntity keeps the first appeared entity in the same param
        if (this.getAssociatedNounRef() == null) {
            if (nounTag == null)
                return null; //error

            // Get a new one with the appropriate noun tag.
            return getNewModifierRef(nounTag, nextTermRef, override);
        }
        return this;
    }

    abstract ModifierRefTag getNewModifierRef(NounRefTag entity, TermRefTag nextTermRef, LanguageArticle override);
}
