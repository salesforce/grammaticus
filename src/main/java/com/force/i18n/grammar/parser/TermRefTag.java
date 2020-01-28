/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.util.*;

import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.LanguageDictionary;

/**
 * Base class of Term Reference Tag elements in labels file. This keeps any elements
 * appears in contents of <CODE>param</CODE> element.
 * @author yoikawa
 */
public abstract class TermRefTag extends RefTag {
	private static final long serialVersionUID = 1L;

	private final String name;

    public static final char SEP = '-';

    public TermRefTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return a "key" that uniquely identifies all the attributes of the reference tag.
     * Used by toString to provide some usefulness
     */
    @Override
    public abstract String getKey();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof TermRefTag) {
            TermRefTag trt=(TermRefTag)obj;
            return trt.getType() == getType() 
                && name.equals(trt.name) 
                && equalsValue(trt);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        return this.name != null ? this.name.hashCode() : 0;
    }

    /**
     * Abstract method to force subclasses to correctly implement equals
     * @param obj the other object being compared.
     * @return <tt>true</tt> if the values of this reference, besides the tag, are equivalent.
     */
    protected abstract boolean equalsValue(TermRefTag obj);

    public boolean isNoun() {
        return getType() == TermType.Noun;
    }

    public boolean isArticle() {
        return getType() == TermType.Article;
    }

    public boolean isAdjective() {
        return getType() == TermType.Adjective;
    }

    public boolean isModifier() {
        return isArticle() || isAdjective();
    }


    /**
     * @return the particle type that this reference represents
     */
    protected abstract TermType getType();

    /**
     * @param dictionary the dictionary this tag is associated with
     * @param overrideForms if the forms should be looked up in the dictionary based on the context of this tag 
     * @return the form of this GrammaticalTerm for the given dictionary.
     */
    public abstract GrammaticalForm getForm(LanguageDictionary dictionary, boolean overrideForms);

    protected abstract boolean isCapital();

    @Override
    public String toString() {
        return getKey();
    }

    /**
     * Get the current term ref tag as a JSON object for use in offline mode
     * @param dictionary the dictionary with all the nouns
     * @param list the list of the current set of terms being processed (so that for modifiers it can find the associated term by index)
     */
    @Override
	public String toJson(LanguageDictionary dictionary, List<?> list) {
		// Default implementation uses the key
        return "{\"t\":\"" + getType().getCharId() + "\",\"l\":\"" + getName().toLowerCase() + "\",\"f\":\""
                + (getForm(dictionary, true) == null ? "" : getForm(dictionary, true).getKey()) + "\",\"c\":"
                + isCapital() + extraJson(dictionary, list) + "}";
	}

	@Override
	public Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary) {
		GrammaticalTerm term = dictionary.getTerm(getName());
		//assert term != null : "Reference to invalid term: " + ((TermRefTag)o).getName();
		return (term != null) ? Collections.singleton(term) : Collections.emptySet();
	}

	// Extra json for the term
	abstract String extraJson(LanguageDictionary dictionary, List<?> list);
}
