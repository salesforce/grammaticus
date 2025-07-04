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

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.google.common.collect.ImmutableMap;

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
        out.writeInt(this.declension.getLanguage().ordinal());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.name = intern(name);
        HumanLanguage ul = LanguageProviderFactory.get().getProvider().getAll().get(in.readInt());
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
     * Utility method used to convert static {@link Map}'s concrete type to a {@link ImmutableMap}.
     * This copy could take a lot of cost. Used to use SortedMap but hash is way more cheaper.
     *
     * @param <T>
     *            the type of the grammatical form for this term
     * @param map
     *            the map to make skinny
     * @return A {@link ImmutableMap} created from a {@link Map} of {@link GrammaticalForm}'s (key) to
     *         {@link String}'s (value).
     */
    protected <T extends GrammaticalForm> Map<T, String> makeSkinny(Map<T, String> map) {
        return ImmutableMap.copyOf(map);
    }
}
