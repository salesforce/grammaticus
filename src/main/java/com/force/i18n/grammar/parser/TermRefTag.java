/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.Serializable;

import com.force.i18n.Renameable;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;

/**
 * Base class of Term Reference Tag elements in labels file. This keeps any elements
 * appears in contents of <CODE>param</CODE> element.
 * @author yoikawa
 */
public abstract class TermRefTag implements Serializable {
    /**
	 *
	 */
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
    public abstract String getKey();

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj.getClass() == this.getClass()) && name.equals(((TermRefTag)obj).name) && equalsValue((TermRefTag)obj);
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
     * @return the form of this GrammaticalTerm for the given dictionary.
     */
    public abstract GrammaticalForm getForm(LanguageDictionary dictionary, boolean overrideForms);

    /**
     * @return whether this GrammaticalTerm is "dynamic", i.e. it requires a
     * Renameable to be used.
     */
    public boolean isDynamic() {
        return false;
    }

    /**
     * Format this reference into a user-readable format based on the given dictionary
     * and dynamic entities
     * @param dictionary the dictionary of language particles.
     * @param entities the set of dynamic renameable entities
     * @param overrideForms if the form contained inside this term ref might be a different language than the dictionary provided.
     * In this case, the implementation must try to find the closest term to the one stored with this term ref.
     * @return a user-readable string for this reference
     */
    public abstract String toString(LanguageDictionary dictionary, boolean overrideForms, Renameable... entities);

    @Override
    public String toString() {
        return getKey();
    }
}
