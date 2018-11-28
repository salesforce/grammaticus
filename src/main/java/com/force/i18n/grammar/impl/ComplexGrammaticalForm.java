/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.io.*;
import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.ArticledDeclension.LegacyArticledNoun;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Implementation that correctly serialized complex noun forms in a way that they are "enumified" and
 * serialization will not "leak" copies around
 * <p>
 * Most of the time, the forms *are* enums, so you dont' need this.  But when they are not, you should use one of the
 * subclasses here to implement them.  This will guarantee that your Nouns are serializable
 * when used in OrgInfo.
 * <p>
 * You'll want to construct them something like this in the LanguageDeclension's constructor
 * <code>
 * ImmutableList.Builder&lt;EsperantoNounForm&gt; fieldBuilder = ImmutableList.builder();
 * int ordinal = 0;
 * // Iterate through your forms
 * for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
 * ///
 * EsperantoNounForm form = new EsperantoNounForm(this, number, ordinal++);
 * entityBuilder.add(form);
 * }
 * this.allEntityForms = entityBuilder.build();
 * </code>
 *
 * @author stamm
 */
abstract class ComplexGrammaticalForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private LanguageDeclension declension;
    private int ordinal;  // The order inside "allNounForms"

    protected ComplexGrammaticalForm(LanguageDeclension declension, int ordinal) {
        this.declension = declension;
        this.ordinal = ordinal;
    }

    public final int getOrdinal() {
        return this.ordinal;
    }

    public final LanguageDeclension getDeclension() {
        return this.declension;
    }

    protected abstract TermType getTermType();


    // Implement a *strict* notion of equality, like in an enum
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    /**
     * Use the serialization proxy for random noun forms (with a 7-ish byte cost)
     * If you have a map, using SerializeMap below is *much* better
     */
    protected final Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * Proxy for ComplexGrammaticalForms to ensure their "Enum" ness
     */
    static class SerializationProxy implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final short languageOrdinal;
        private final byte termTypeOrdinal;
        private final byte ordinal;

        private final static List<? extends HumanLanguage> LANGUAGE_ARRAY = LanguageProviderFactory.get().getProvider().getAll();
        private final static TermType[] TERM_TYPE_ARRAY = TermType.values();

        public SerializationProxy(ComplexGrammaticalForm form) {
            this.languageOrdinal = (short) form.getDeclension().getLanguage().ordinal();
            this.termTypeOrdinal = (byte) form.getTermType().ordinal();
            this.ordinal = (byte) form.getOrdinal();
        }

        protected Object readResolve() {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(LANGUAGE_ARRAY.get(this.languageOrdinal));
            List<?> formList = null;
            switch (TERM_TYPE_ARRAY[this.termTypeOrdinal]) {
                case Noun:
                    formList = declension.getAllNounForms();
                    break;
                case Adjective:
                    formList = declension.getAdjectiveForms();
                    break;
                case Article:
                    formList = declension.getArticleForms();
                    break;
            }
            assert formList != null;
            return formList.get(this.ordinal);
        }
    }


    /**
     * "Complex" (i.e. non-enumified) noun forms should implement this interface, which is *not* serializable
     * Users that care, should use the declension/ordinal
     *
     * @author stamm
     */
    abstract static class ComplexNounForm extends ComplexGrammaticalForm implements NounForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        protected ComplexNounForm(LanguageDeclension declension, int ordinal) {
            super(declension, ordinal);
        }

        @Override
        protected TermType getTermType() {
            return TermType.Noun;
        }

        @Override
        public String getKey() {
            // TODO Auto-generated method stub
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getPossessive().getDbValue() + "-" + getArticle().getDbValue();
        }

        @Override
        public String toString() {
            return getDeclension().getLanguage() + "Noun:" + getOrdinal() + "-" + getKey();
        }
    }

    /**
     * See ComplexNounForm for an explanation
     *
     * @author stamm
     */
    abstract static class ComplexAdjectiveForm extends ComplexGrammaticalForm implements AdjectiveForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        protected ComplexAdjectiveForm(LanguageDeclension declension, int ordinal) {
            super(declension, ordinal);
        }

        @Override
        protected TermType getTermType() {
            return TermType.Adjective;
        }

        @Override
        public String toString() {
            return getDeclension().getLanguage() + "Adj:" + getOrdinal() + "-" + getNumber().getDbValue() + "-" + getArticle().getDbValue() + "-" + getCase().getDbValue() + "-" + getGender().getDbValue() + "-" + getStartsWith().getDbValue();
        }
    }

    /**
     * See ComplexNounForm for an explanation
     *
     * @author stamm
     */
    abstract static class ComplexArticleForm extends ComplexGrammaticalForm implements ArticleForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        protected ComplexArticleForm(LanguageDeclension declension, int ordinal) {
            super(declension, ordinal);
        }

        @Override
        protected TermType getTermType() {
            return TermType.Article;
        }

        @Override
        public String toString() {
            return getDeclension().getLanguage() + "Art" + getOrdinal() + "-" + getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getGender().getDbValue() + "-" + getStartsWith().getDbValue();
        }
    }


    /*
     * Some utilities/methods for reducing the cache size of the dictionaries, if you need to.  I ran some tests and it seemed to be not a huge deal.
     *
     * Here's the base line when going to the disk
     * Wrote FINNISH dictionary in 147 msec of size 472940
     * Read FINNISH dictionary in 92 msec
     * InMemTest: fi dictionary in 10260/15 usec
     *
     * Make the noun map on FinnishNoun transient and serialize by hand using the ordinal
     * FINNISH dictionary in 190 msec of size 392940
     * Read FINNISH dictionary in 112 msec
     * InMemTest: fi dictionary in 8072/18 usec; size= 390270
     *
     *
     * If you feel like trading space for a little speed, make your maps transient add this code to your subclass of ComplexGrammaticalForm

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            ComplexGrammaticalForm.serializeFormMap(out, values);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.values = ComplexGrammaticalForm.deserializeFormMap(in, getDeclension(), TermType.Noun);
        }

   */

    static <T extends ComplexGrammaticalForm> void serializeFormMap(ObjectOutputStream out, Map<T, String> values) throws IOException {
        if (values == null) {
            assert false : "Don't bother me with this...";
            out.writeByte(0);
        } else if (values.size() == 0) {
            out.writeByte(0);
        } else {
            out.writeByte(values.size());
            for (Map.Entry<T, String> entry : values.entrySet()) {
                out.writeByte(entry.getKey().getOrdinal());
                out.writeObject(entry.getValue());  // Serialize the "object" because it's been uniquefied
            }
        }
    }

    @SuppressWarnings("unchecked") // Deserializing a map using trickery
    static <T extends ComplexGrammaticalForm> Map<T, String> deserializeFormMap(ObjectInputStream in, LanguageDeclension declension, TermType termType) throws IOException, ClassNotFoundException {
        int size = in.readByte();
        Map<T, String> result = new HashMap<T, String>(size << 1);
        if (size == 0) {
            return result;
        }
        List<T> formList = null;
        switch (termType) {
            case Noun:
                formList = (List<T>) declension.getAllNounForms();
                break;
            case Adjective:
                formList = (List<T>) declension.getAdjectiveForms();
                break;
            case Article:
                formList = (List<T>) declension.getArticleForms();
                break;
        }
        assert formList != null;
        for (int i = 0; i < size; i++) {
            int ordinal = in.readByte();
            String value = (String) in.readObject();
            result.put(formList.get(ordinal), value);
        }
        return result;
    }


    /**
     * Optimize searching through large sets of modifiers at runtime through a linear search is expensive.
     * This version is optimized by using arrays of the right size at runtime.  This is only useful for
     * declensions that autogenerate the forms and have a large number of them; otherwise logic is probably
     * better (see RomanceDeclension)
     *
     * @author stamm
     */
    static final class ModifierFormMap<T extends ModifierForm> {
        private final ModifierForm[][][] singularMap;
        private final ModifierForm[][][] pluralMap;

        private static final int genderLength = LanguageGender.values().length;
        private static final int caseLength = LanguageCase.values().length;
        private static final int startsWithLength = LanguageStartsWith.values().length;

        public ModifierFormMap(Collection<? extends T> modifiers) {
            ModifierForm[][][] gcs0Map = new ModifierForm[genderLength][][];
            ModifierForm[][][] gcs1Map = new ModifierForm[genderLength][][];
            for (T modifier : modifiers) {
                ModifierForm[][][] gcsMap = modifier.getNumber().isPlural() ? gcs1Map : gcs0Map;
                ModifierForm[][] csMap = gcsMap[modifier.getGender().ordinal()];
                if (csMap == null) {
                    csMap = new ModifierForm[caseLength][];
                    gcsMap[modifier.getGender().ordinal()] = csMap;
                }
                ModifierForm[] sMap = csMap[modifier.getCase().ordinal()];
                if (sMap == null) {
                    sMap = new ModifierForm[startsWithLength];
                    csMap[modifier.getCase().ordinal()] = sMap;
                }
                if (sMap[modifier.getStartsWith().ordinal()] != null) {
                    throw new IllegalArgumentException("Duplicate modifier forms " + modifier + " != " + sMap[modifier.getStartsWith().ordinal()]);
                }
                sMap[modifier.getStartsWith().ordinal()] = modifier;
            }
            singularMap = gcs0Map;
            pluralMap = gcs1Map;
        }

        /**
         * If you have an article-supporting language, it makes sense to keep an enum map around.
         */
        public static <A extends AdjectiveForm> EnumMap<LanguageArticle, ModifierFormMap<A>> getArticleSpecificMap(Collection<? extends A> forms) {
            Multimap<LanguageArticle, A> mm = ArrayListMultimap.create();
            for (A form : forms) {
                mm.put(form.getArticle(), form);
            }
            EnumMap<LanguageArticle, ModifierFormMap<A>> result = new EnumMap<LanguageArticle, ModifierFormMap<A>>(LanguageArticle.class);
            for (LanguageArticle article : LanguageArticle.values()) {
                result.put(article, new ModifierFormMap<A>(mm.get(article)));
            }
            return result;
        }

        @SuppressWarnings("unchecked") // Generic arrays
        public T getForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number, LanguageCase _case) {
            if (number == null) {
                return null;
            }
            ModifierForm[][][] gcsMap = number.isPlural() ? pluralMap : singularMap;
            if (gcsMap == null) {
                return null;
            }
            ModifierForm[][] csMap = gcsMap[gender.ordinal()];
            if (csMap == null) {
                return null;
            }
            ModifierForm[] sMap = csMap[_case.ordinal()];
            if (sMap == null) {
                return null;
            }
            return (T) sMap[startsWith.ordinal()];
        }
    }

    /**
     * Optimize searching through large sets of noun forms at runtime through a linear search is expensive.
     * This version is optimized by using arrays of the right size at runtime.  This is only useful for
     * declensions that autogenerate the forms (and have at least 8 of them) and have a large number of them;
     * otherwise logic is probably better (see RomanceDeclension)
     *
     * @author stamm
     */
    static final class NounFormMap<T extends NounForm> {
        private final NounForm[] singularMap;
        private final NounForm[] pluralMap;

        private static final int caseLength = LanguageCase.values().length;

        public NounFormMap(Collection<? extends T> modifiers) {
            NounForm[] c0Map = new NounForm[caseLength];
            NounForm[] c1Map = new NounForm[caseLength];
            for (T modifier : modifiers) {
                NounForm[] cMap = modifier.getNumber().isPlural() ? c1Map : c0Map;
                if (cMap[modifier.getCase().ordinal()] != null) {
                    throw new IllegalArgumentException("Duplicate noun forms " + modifier + " != " + cMap[modifier.getCase().ordinal()]);
                }
                cMap[modifier.getCase().ordinal()] = modifier;
            }
            singularMap = c0Map;
            pluralMap = c1Map;
        }

        /**
         * If you have an article-supporting language, it makes sense to keep an enum map around (it's fairly efficient.)
         *
         * @return a map from article to NounFormMap for languages that have multiple articles in the noun form
         */
        public static <A extends NounForm> EnumMap<LanguageArticle, NounFormMap<A>> getArticleSpecificMap(Collection<? extends A> forms) {
            Multimap<LanguageArticle, A> mm = ArrayListMultimap.create();
            for (A form : forms) {
                mm.put(form.getArticle(), form);
            }
            EnumMap<LanguageArticle, NounFormMap<A>> result = new EnumMap<LanguageArticle, NounFormMap<A>>(LanguageArticle.class);
            for (LanguageArticle article : LanguageArticle.values()) {
                result.put(article, new NounFormMap<A>(mm.get(article)));
            }
            return result;
        }

        /**
         * For possessive languages, it makes sense to keep an enum map around for that
         */
        public static <A extends NounForm> EnumMap<LanguagePossessive, NounFormMap<A>> getPossessiveSpecificMap(Collection<? extends A> forms) {
            Multimap<LanguagePossessive, A> mm = ArrayListMultimap.create();
            for (A form : forms) {
                mm.put(form.getPossessive(), form);
            }
            EnumMap<LanguagePossessive, NounFormMap<A>> result = new EnumMap<LanguagePossessive, NounFormMap<A>>(LanguagePossessive.class);
            for (LanguagePossessive poss : LanguagePossessive.values()) {
                result.put(poss, new NounFormMap<A>(mm.get(poss)));
            }
            return result;
        }

        @SuppressWarnings("unchecked") // Generic arrays
        public T getForm(LanguageNumber number, LanguageCase _case) {
            if (number == null) {
                return null;
            }
            NounForm[] cMap = number.isPlural() ? pluralMap : singularMap;
            return (T) cMap[_case.ordinal()];
        }
    }

    /**
     * Abstract class for ComplexAdjective's that handles the quick serialization
     */
    abstract static class ComplexAdjective<T extends ComplexAdjectiveForm> extends Adjective {
        private static final long serialVersionUID = 1L;
        // The "keys" here are StartsWith, Gender, and Plurality
        private transient Map<T, String> values = new HashMap<T, String>();

        public ComplexAdjective(LanguageDeclension declension, String name,
                                LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        public Map<? extends AdjectiveForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(AdjectiveForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(AdjectiveForm form, String value) {
            values.put(getFormClass().cast(form), value);
        }

        protected abstract Class<T> getFormClass();

        // Reduce size of adjective maps
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            ComplexGrammaticalForm.serializeFormMap(out, values);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.values = ComplexGrammaticalForm.deserializeFormMap(in, getDeclension(), TermType.Adjective);
        }

    }

    /**
     * Abstract class for ComplexNoun's that handles the quick serialization
     */
    abstract static class ComplexNoun<T extends ComplexNounForm> extends Noun {
        private static final long serialVersionUID = 1L;
        // The "keys" here are StartsWith, Gender, and Plurality
        private transient Map<T, String> values = new HashMap<T, String>();

        ComplexNoun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
                    LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
        }

        protected abstract Class<T> getFormClass();

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return values;
        }

        @Override
        public String getString(NounForm form) {
            assert getFormClass().isInstance(form) : "You must provide a correct " + getFormClass() + " noun form for a " + getClass();
            return values.get(form);
        }

        @Override
        public void setString(String value, NounForm form) {
            values.put(getFormClass().cast(form), value);
        }

        @Override
        public Noun clone() {
            @SuppressWarnings("unchecked") // Clone not generalized
                    ComplexNoun<T> noun = (ComplexNoun<T>) super.clone();
            noun.values = new HashMap<T, String>(noun.values);
            return noun;
        }

        // Reduce size of noun maps
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            ComplexGrammaticalForm.serializeFormMap(out, values);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.values = ComplexGrammaticalForm.deserializeFormMap(in, getDeclension(), TermType.Noun);
        }

        @Override
        public void makeSkinny() {
            values = makeSkinny(values);
        }
    }


    /**
     * Abstract class for ComplexArticledNoun that handles the quick serialization
     * TODO: Get rid of LegacyArticledNoun/fold it into Noun itself.
     */
    abstract static class ComplexArticledNoun<T extends ComplexNounForm> extends LegacyArticledNoun {
        private static final long serialVersionUID = 1L;
        // The "keys" here are StartsWith, Gender, and Plurality
        private transient Map<T, String> values = new HashMap<T, String>();

        ComplexArticledNoun(ArticledDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
                            LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
        }

        protected abstract Class<T> getFormClass();

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return values;
        }

        @Override
        public String getExactString(NounForm form) {
            assert getFormClass().isInstance(form) : "You must provide a correct " + getFormClass() + " noun form for a " + getClass();
            return values.get(form);
        }

        @Override
        public void setString(String value, NounForm form) {
            values.put(getFormClass().cast(form), value);
        }

        @Override
        public Noun clone() {
            @SuppressWarnings("unchecked") // Clone not generalized
                    ComplexArticledNoun<T> noun = (ComplexArticledNoun<T>) super.clone();
            noun.values = new HashMap<T, String>(noun.values);
            return noun;
        }

        // Reduce size of noun maps
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            ComplexGrammaticalForm.serializeFormMap(out, values);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.values = ComplexGrammaticalForm.deserializeFormMap(in, getDeclension(), TermType.Noun);
        }

        @Override
        public void makeSkinny() {
            values = makeSkinny(values);
        }
    }
}
