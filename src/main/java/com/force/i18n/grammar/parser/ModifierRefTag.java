/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.util.List;
import java.util.logging.Logger;

import com.force.i18n.Renameable;
import com.force.i18n.grammar.*;


/**
 * A reference to a modifier in a label file.
 * @author yoikawa,stamm
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
    private final int hashCode;

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
        this.hashCode = calcHashCode();
    }

    private int calcHashCode() {
        int result = super.hashCode();
        result = calcHashCodeField(result, associatedNounRef);
        result = calcHashCodeField(result, nextTermRef);
        result = calcHashCodeField(result, overrides);
        result = calcHashCodeField(result, isCapital);
        return result;
    }

    private int calcHashCodeField(int current, Object obj) {
        return (obj == null ? current : current << 2 ^ obj.hashCode());
    }

    private int calcHashCodeField(int current, boolean b) {
        return current << 1 + ( b ? 1 : 0 );
    }

    @Override
    public String getKey() {
        return getKey(getName(), getAssociatedNounRef(), isCapital(), getDeclensionOverrides());
    }

    /**
     * @return whether this modifier was denoted in the label fields with a capital letter
     */
    @Override
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
    public String toString(LanguageDictionary formatter, boolean overrideForms, Object[] vals, Renameable... entities) {
        NounModifier modifier = resolveModifier(formatter);

        if (modifier == null && overrideForms) {
            logger.fine("Missing modifier " + getName() + " for " + formatter.getLanguage());
            return ""; //This is the "legacy" behavior, needed for LabelParserComparisonTest.  It should be modifier.getDefaultValue();
        }

        assert modifier != null: "Can't find modifier '" + getName() + "'";

        if (getAssociatedNounRef() == null) {
            return ""; // This is the "legacy" behavior, needed for LabelParserComparisonTest.  It should be modifier.getDefaultValue();
        }

        NounForm nounForm = associatedNounRef.getForm(formatter, overrideForms);

        // Get the relevant noun to see get the right value for StartsWith/Gender
        Noun n = associatedNounRef.resolveNoun(formatter, entities);

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
    public int hashCode() {
        return hashCode;
    }

 
    @Override
    protected boolean equalsValue(TermRefTag obj) {
        ModifierRefTag otherTag = (ModifierRefTag) obj;
        return this.isCapital == otherTag.isCapital
            && (this.associatedNounRef == null ? otherTag.associatedNounRef  == null : this.associatedNounRef.equals(otherTag.associatedNounRef))
            && (this.nextTermRef == null ? otherTag.nextTermRef  == null : this.nextTermRef.equals(otherTag.nextTermRef))
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

	@Override
	String extraJson(LanguageDictionary dictionary, List<?> list) {
        if (list == null || list.isEmpty()) return "";

		Integer associatedNounIndex = null;
		Integer nextTermIndex = null;
		for (int i = 0; i < list.size(); i++) {
			Object term = list.get(i);
			if (term != null && term.equals(this.associatedNounRef)) {
				associatedNounIndex = i;
			}
			
			if (term != null) {
				if (term.equals(this.nextTermRef)) {
					nextTermIndex = i;
				// Since the noun refs have been resolved for modifiers, the associatedNounRef makes the "equalsValue" value false
			    // But, since the noun has been resolved, we can use that to compare, along with the name of the modifier
				} else if (this.nextTermRef instanceof ModifierRefTag && term instanceof ModifierRefTag
						&& ((ModifierRefTag)this.nextTermRef).getName().equals(((ModifierRefTag)term).getName())
						&& (((ModifierRefTag)term).getAssociatedNounRef().equals(this.associatedNounRef))) {
					nextTermIndex = i;
				}
			}
		}
		StringBuilder json = new StringBuilder();
		if (associatedNounIndex != null) json.append(",\"an\":").append(associatedNounIndex.intValue());
		if (nextTermIndex != null) json.append(",\"nt\":").append(nextTermIndex.intValue());
		return json.toString();
	}

	abstract ModifierRefTag getNewModifierRef(NounRefTag entity, TermRefTag nextTermRef, LanguageArticle override);
}
