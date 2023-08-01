/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.*;
import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Represents a grammatical term; generally one that is declined based on a noun form or other
 * factors.  This provides some default behavior, such as having a transient pointer to the
 * associated declension (but not the dictionary), and dealing with serialization
 *
 * Currently, a Noun or a Modifier/Adjective.
 *
 * @author stamm
 */
public abstract class GrammaticalTerm implements Serializable, Comparable<GrammaticalTerm> {
    private static final long serialVersionUID = 1L;

    private String name; // non-final.  see readObject()
    private transient LanguageDeclension declension;

    public enum TermType {
        Noun('n'),
        Adjective('a'),
        Article('d');

        private final char id;

        TermType(char id) {
            this.id = id;
        }
        public char getCharId() {return this.id;}
    }

    protected GrammaticalTerm(LanguageDeclension declension, String name) {
        this.name = intern(name);
        this.declension = declension;
    }

    public String getName() {
        return this.name;
    }

    /**
     * After all of the parsing has been done for a noun or modifier, "fix up"
     * any missing values needed.
     *
     * TODO: This may not be very necessary.
     * @param name the name of the term
     * @return {@code true} if the term is valid
     */
    protected abstract boolean validate(String name);

    /**
     * @return the type of this term
     */
    protected abstract TermType getTermType();

    /**
     * The "startsWith" is based on the next term in the sequence, not the next noun
     * @return the startsWith of this grammatical term
     */
    public abstract LanguageStartsWith getStartsWith();

    /**
     * @return whether this grammatical term was inherited from english
     */
    public abstract boolean isCopiedFromDefault();

    /**
     * @return the declension associated with the language.  This
     */
    public LanguageDeclension getDeclension() {
        return this.declension;
    }

    @Override
    public int compareTo(GrammaticalTerm o) {
        int langComp = Integer.compare(getDeclension().getLanguage().ordinal(),
                o.getDeclension().getLanguage().ordinal());
        if (langComp != 0) return langComp;

        TermType thisType = getTermType();
        TermType oType = o.getTermType();
        int typeComp = thisType.compareTo(oType);
        return typeComp == 0 ? getName().compareTo(o.getName()) : typeComp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return compareTo((GrammaticalTerm)o) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.declension.getLanguage().ordinal(), getTermType(), this.name);
    }

    public abstract void toJson(Appendable appendable) throws IOException;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.declension.getLanguage().getLocaleString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.name = intern(name);
        HumanLanguage ul = LanguageProviderFactory.get().getProvider().getLanguage((String)in.readObject());
        this.declension = LanguageDeclensionFactory.get().getDeclension(ul);
    }

    /**
     * Provides clients the capability of indicating when members of Noun's can be converted to space efficient data
     * structures.
     */
    public void makeSkinny() {
        // the default implementation does nothing
    }

    /**
     * Utility method used to convert static {@link Map}'s concrete type to a {@link ImmutableSortedMap}.
     * {@link ImmutableSortedMap} have a 8 byte overhead per element and are useful for reducing the per element
     * overhead, that is traditionally high on most {@code Map} implementations.
     *
     * @param <T>
     *            the type of the grammatical form for this term
     * @param map
     *            the map to make skinny
     * @return A {@link ImmutableSortedMap} created from a {@link Map} of {@link GrammaticalForm}'s (key) to
     *         {@link String}'s (value).
     */
    protected <T extends GrammaticalForm> Map<T, String> makeSkinny(Map<T, String> map) {
        return ImmutableSortedMap.copyOf(map, new KeyComparator<T>());
    }

    private static class KeyComparator<T extends GrammaticalForm> implements Comparator<T>, Serializable {
        @Override
        public int compare(T o1, T o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }
}
