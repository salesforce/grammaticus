package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.*;
import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.commons.util.collection.MapSerializer;
import com.force.i18n.grammar.*;
import com.google.common.collect.ImmutableSortedMap;

/**
 * In-memory version of GrammaticalTermMap
 *
 * @author ytanida
 */
public class GrammaticalTermMapImpl<T extends GrammaticalTerm> implements GrammaticalTermMap<T> {
    private static final long serialVersionUID = 2099717329853215271L;

    protected transient Map<String, T> map;
    private boolean isSkinny = false;

    public GrammaticalTermMapImpl() {
        map = new HashMap<>();
    }

    public GrammaticalTermMapImpl(Map<String, T> map, boolean isSkinny) {
        this.isSkinny = isSkinny;
        if (isSkinny) {
            // this uses "natural ordering" built-in comparator that ends up calling GrammaticalTerm.compareTo()
            // maybe better to specify comparator for just comparing GrammaticalTerm.getName() here
            this.map = ImmutableSortedMap.copyOf(map);
        } else {
            this.map = map;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GrammaticalTermMapImpl)) return false;

        @SuppressWarnings("unchecked")
        GrammaticalTermMapImpl<T> other = (GrammaticalTermMapImpl<T>)obj;
        return isSkinny == other.isSkinny && map.equals(other.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode() + (isSkinny ? 37 : 0);
    }

    @Override
    public boolean isSkinny() {
        return isSkinny;
    }

    @Override
    public GrammaticalTermMap<T> makeSkinny() {
        return new GrammaticalTermMapImpl<>(map, true);
    }

    @Override
    public void writeJson(Appendable out, RenamingProvider renamingProvider, LanguageDictionary dictionary,
            Collection<String> termsToInclude) throws IOException {
        Set<String> wrote = new HashSet<>();
        out.append('{');
        if (termsToInclude != null) {
            boolean first = true;
            for (String name : termsToInclude) {
                GrammaticalTerm term = map.get(name);
                if (term != null) {
                    if (term instanceof Noun) term = dictionary.getNounOverride((Noun)term);
                    if (!first) {
                        out.append(',');
                    } else {
                        first = false;
                    }
                    writeJsonTerm(out, renamingProvider, term, dictionary.getLanguage());
                    wrote.add(name);
                }
            }
            termsToInclude.removeAll(wrote);
        } else {
            writeJson(out, renamingProvider, dictionary.getLanguage());
        }
        out.append('}');
    }

    private void writeJson(Appendable out, RenamingProvider renamingProvider, HumanLanguage lang) throws IOException {
        boolean first = true;
        for (GrammaticalTerm term : map.values()) {
            if (!first) {
                out.append(',');
            } else {
                first = false;
            }
            writeJsonTerm(out, renamingProvider, term, lang);
        }
    }

    private void writeJsonTerm(Appendable out, RenamingProvider renamingProvider, GrammaticalTerm term,
            HumanLanguage lang) throws IOException {
        if (renamingProvider != null && term instanceof Noun && renamingProvider.useRenamedNouns()) {
            Noun renamedNoun = renamingProvider.getRenamedNoun(lang, ((Noun)term).getName());
            if (renamedNoun != null) term = renamedNoun;
        }
        out.append('\"').append(term.getName().toLowerCase()).append("\":");
        term.toJson(out);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public T get(String name) {
        return map.get(name);
    }

    @Override
    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    @Override
    public Set<Map.Entry<String, T>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Collection<T> values() {
        return map.values();
    }

    @Override
    public void put(String k, T v) {
        if (isSkinny) throw new IllegalStateException("This map is not able to modify");
        map.put(k, v);
    }

    @Override
    public void putAll(GrammaticalTermMap<T> other) {
        if (isSkinny) throw new IllegalStateException("This map is not able to modify");
        map.putAll(((GrammaticalTermMapImpl<T>)other).map);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Override default serializer to avoid any duplicated in the serialized map.
     *
     * @param in
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.map = ((TermMapSerializer<T>)in.readObject()).getMap();
    }

    /**
     * Override default serializer to avoid any duplicated in the serialized map.
     *
     * @param in
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(new TermMapSerializer<>(map));
    }

    static final class TermMapSerializer<T extends GrammaticalTerm> extends MapSerializer<String, T> {
        protected TermMapSerializer(Map<String, T> map) {
            super(map);
        }

        @Override
        protected String internKey(String key) {
            return intern(key);
        }

        protected Map<String, T> getMap() {
            return super.map;
        }
    }
}