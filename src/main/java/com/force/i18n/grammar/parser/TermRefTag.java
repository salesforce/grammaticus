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

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.force.i18n.grammar.GrammaticalForm;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.LanguageDictionary;

/**
 * Base class of Term Reference Tag elements in labels file. This keeps any elements
 * appears in contents of <CODE>param</CODE> element.
 * @author yoikawa
 */
public abstract class TermRefTag extends RefTag {
    private static final long serialVersionUID = 1L;

    private transient String name; // non-final: see readObject()

    public static final char SEP = '-';

    protected TermRefTag(String name) {
        this.name = intern(name);
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
     * @return {@code true} if the values of this reference, besides the tag, are equivalent.
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

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeUTF(this.name);
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        // Read in the threshold, loadfactor, and any hidden stuff
        s.defaultReadObject();
        this.name = intern(s.readUTF());
    }

    protected Object readResolve() {
        return unique();
    }

    // utility method to uniquefy
    abstract TermRefTag unique();
}
