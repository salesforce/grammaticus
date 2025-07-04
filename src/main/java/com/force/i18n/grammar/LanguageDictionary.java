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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.force.i18n.HumanLanguage;
import com.force.i18n.Renameable;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.commons.text.Uniquefy;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.GrammaticalTermMapImpl;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.grammar.parser.RefTag;
import com.google.common.base.Supplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;

/**
 * The base class of new label set for a language. This class is constructed by
 * LabelSet so each language would share the same instance of LabelInfo through
 * the application.
 *
 * This class does: - Construct and keep all generic entity names, nouns
 * (includes "compound noun") and modifier.
 * - format array of (String | LabelTag)+ to string
 *
 * @author yoikawa,stamm
 * @see com.force.i18n.LabelSet
 */
public class LanguageDictionary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final HumanLanguage language;
    private transient LanguageDeclension declension;  // Details about noun structure

    // TODO: These could all be made lists when serialized

    // map to Noun
    protected GrammaticalTermMap<Noun> nounMap;
    // map to Noun
    protected GrammaticalTermMap<Noun>  nounMapByPluralAlias;
    // map to Adjective
    protected GrammaticalTermMap<Adjective> adjectiveMap;
    // map to Article
    protected GrammaticalTermMap<Article> articleMap;

    // Override of noun to nounOverrides
    private SortedSetMultimap<Noun, NounVersionOverride> nounVersionOverrides;
    // Whether or not we've been "made skinny".
    private transient boolean isSkinny;


    // TODO:  Following two map/set *can* be shared across the all
    // instances; but it would have to be concurrent.
    // TODO: "TableEnumOrId" should be stored on each Noun, right?
    /** For UI support. keyed by TableEnumOrId to HashMap(name, NounType) */
    protected transient Multimap<String, Noun> nounsByEntityType;

    public LanguageDictionary(HumanLanguage language) {
        this.language = language;
        this.declension = LanguageDeclensionFactory.get().getDeclension(language);
        this.nounsByEntityType = ArrayListMultimap.create();
        createGrammaticalTermMap();
    }

    /**
     * Copy constructor. Use only if the declention for the {@code language} is proxying to the declension of
     * {@code from}.
     *
     * @param language language to use
     * @param from the source {@code LanguageDictionary} to copy from
     */
    public LanguageDictionary(HumanLanguage language, LanguageDictionary from) {
        this.language = language;
        this.declension = LanguageDeclensionFactory.get().getDeclension(language);
        this.nounsByEntityType = from.nounsByEntityType;

        this.nounMap = from.nounMap;
        this.nounMapByPluralAlias = from.nounMapByPluralAlias;
        this.adjectiveMap = from.adjectiveMap;
        this.articleMap = from.articleMap;
        this.nounVersionOverrides = from.nounVersionOverrides;
        this.isSkinny = from.isSkinny;
    }

    protected void createGrammaticalTermMap() {
        this.nounMap = new GrammaticalTermMapImpl<>();
        this.nounMapByPluralAlias = new GrammaticalTermMapImpl<>();
        this.adjectiveMap = new GrammaticalTermMapImpl<>();
        this.articleMap = new GrammaticalTermMapImpl<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof LanguageDictionary) {
            LanguageDictionary l = (LanguageDictionary)o;

            return this.language == l.language
                    && this.nounMap.equals(l.nounMap)
                    && this.adjectiveMap.equals(l.adjectiveMap)
                    && this.articleMap.equals(l.articleMap)
                    && this.nounMapByPluralAlias.equals(l.nounMapByPluralAlias)
                    && this.nounsByEntityType.equals(l.nounsByEntityType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + ((null != language) ? language.hashCode() : 0);
        hash = 37 * hash + ((null != nounMap) ? nounMap.hashCode() : 0);
        hash = 37 * hash + ((null != adjectiveMap) ? adjectiveMap.hashCode() : 0);
        hash = 37 * hash + ((null != articleMap) ? articleMap.hashCode() : 0);
        hash = 37 * hash + ((null != nounMapByPluralAlias) ? nounMapByPluralAlias.hashCode() : 0);
        hash = 37 * hash + ((null != nounsByEntityType) ? nounsByEntityType.hashCode() : 0);
        return hash;
    }

    public HumanLanguage getLanguage() {
        return language;
    }

    public LanguageDeclension getDeclension() {
        return declension;
    }

    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        if (entityName != null && entityName.isEmpty()) entityName = null;
        Noun n = getDeclension().createNoun(name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);

        // Add to the noun map
        if (entityName != null) {
            nounsByEntityType.put(intern(entityName.toLowerCase()), n);
        }
        return n;
    }

    /**
     * construct new noun from result set.  Assumed the column name is STARTS_WITH, GENDER, and NAME
     * @param tableEnumOrId the "DB" name of the entity
     * @param type field, entity, or other
     * @param rs the result set to read.
     * @param uniquefy required to prevent reused strings from being duplicated
     * @throws SQLException if there's a problem reading from the database
     * @return a noun with no values, yet.
     */
    public Noun createNoun(String tableEnumOrId, NounType type, ResultSet rs, Uniquefy uniquefy) throws SQLException {
        LanguageStartsWith starts = LanguageStartsWith.fromDbValue(rs.getString("STARTS_WITH"));
        LanguageGender gen = LanguageGender.fromDbValue(rs.getString("GENDER"));
        String name = rs.getString("NAME");
        return createNoun(name, null, type, tableEnumOrId, starts, gen, null, false, false);
    }

    /**
     * get termMap of Noun
     */
    public GrammaticalTermMap<Noun> getNounMap() {
        return nounMap;
    }

    /**
     * get termMap of NounByPluralAlias
     */
    public GrammaticalTermMap<Noun> getNounByPluralAlias() {
        return nounMapByPluralAlias;
    }

    /**
     * get termMap of Adjective
     */
    public GrammaticalTermMap<Adjective> getAdjectiveMap() {
        return adjectiveMap;
    }

    /**
     * get termMap of Article
     */
    public GrammaticalTermMap<Article> getArticleMap() {
        return articleMap;
    }


    private void forAllTerms (TermType type, BiConsumer<String, GrammaticalTerm> f) {
        switch (type) {
        case Noun:
            for(Map.Entry<String, Noun> e : nounMap.entrySet()) f.accept(e.getKey(), e.getValue());
            break;
        case Adjective:
            for(Map.Entry<String, Adjective> e : adjectiveMap.entrySet()) f.accept(e.getKey(), e.getValue());
            break;
        case Article:
            for(Map.Entry<String, Article> e : articleMap.entrySet()) f.accept(e.getKey(), e.getValue());
            break;
        default:
            throw new AssertionError("Invalid term type " + type);
        }
    }



    public Set<String> getAllTermNames(TermType type) {
        switch (type) {
        case Noun:
            return Collections.unmodifiableSet(nounMap.keySet());
        case Adjective:
            return Collections.unmodifiableSet(adjectiveMap.keySet());
        case Article:
            return Collections.unmodifiableSet(articleMap.keySet());
        default:
            throw new AssertionError("Invalid term type");
        }
    }

    /**
     * @param type the term type to search
     * @return the names of all the terms of the given type that are copied from the default language
     */
    public Set<String> getAllInheritedTermNames(TermType type) {
        Set<String> result = new HashSet<String>();
        forAllTerms(type, (k,v) -> {
            if (v.isCopiedFromDefault()) result.add(k);
        });
        return result;
    }

    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position, boolean copiedFromDefault) {
        Adjective result = getDeclension().createAdjective(name, startsWith, position);
        result.setInheritedFromDefault(copiedFromDefault);
        return result;
    }

    public Article createArticle(String name, LanguageArticle articleType, boolean copiedFromDefault) {
        if (!getDeclension().hasArticle()) {
            throw new RuntimeException("Language doesn't support articles");
        }
        Article result = getDeclension().createArticle(name, articleType);
        result.setInheritedFromDefault(copiedFromDefault);
        return result;
    }

    /**
     * Construct String value from the given Label structure, which could be
     * either String or LabelTag.<br>
     * @param obj the object to parse
     * @param entities the renameable objects that are the parameters for the value
     * @param vals the values to use when resolving choiceformats.  Mostly for numbers
     * @param forMessageFormat if the MessageFormat values need to be preserved, such as {0}
     * @param overrideForms if true, it means that the forms in the term refs in the objects do not match the current dictionary and need to be recalculated
     * @return the label with all the values replaced
     */
    public String format(Object obj, Renameable[] entities, Object[] vals, boolean overrideForms, boolean forMessageFormat) {
        if (obj instanceof List<?>) {
            StringBuilder sb = new StringBuilder();
            for (Object o : (List< ? >)obj) {
                assert o != null;

                if (!(o instanceof RefTag)) {
                    if (forMessageFormat) {
                        TextUtil.escapeForMessageFormat(o.toString(), sb, false);
                    } else {
                        sb.append(o);
                    }
                } else {
                    String s = ((RefTag)o).toString(this, overrideForms, vals, entities);
                    if (forMessageFormat) {
                        TextUtil.escapeForMessageFormat(s, sb, false);
                    } else {
                        sb.append(s);
                    }
                }
            }
            return sb.toString();

        } else if (obj instanceof RefTag) {
            String s = ((RefTag)obj).toString(this, overrideForms, vals, entities);
            return (!forMessageFormat)
                ? s
                : TextUtil.escapeForMessageFormat(s, new StringBuilder(s.length() + 8), false).toString();
        }
        // nothing related to LabelInfo, just return as String
        return obj.toString();
    }

    private RenamingProvider getRenamingProvider() {
        return RenamingProviderFactory.get().getProvider();
    }

    /**
     * used to resolve custom object name at runtime. For the most case, this is
     * for resolving label like: &lt;entity entity="0"/&gt; &lt;entity_xxxx entity="0"/&gt;
     * @param name the default name of the noun
     * @param ei the renameable entity
     * @param getRenamedValue return the renamed value if the renaming provider includes a renamed noun
     * @param doFormat if {0}'s in the renameable noun should be replaced
     * @return the noun that should be used for the given renameable entity, or the default under name if not applicable.
     */
    public Noun getDynamicNoun(String name, Renameable ei, boolean getRenamedValue, boolean doFormat) {
        String resolvedDbName = ei.getEntitySpecificDbLabelKey(name);
        if (getRenamedValue) {
            Noun n = getRenamingProvider().getRenamedNoun(getLanguage(), resolvedDbName);
            if (n != null)
                return n;
        } else if (!ei.hasStandardLabel()) {  // i.e. custom object
            // Try the original noun if necessary.
            Noun n = getRenamingProvider().getPackagedNoun(getLanguage(), resolvedDbName);
            if (n != null) return n;
            // OK, if we don't have a packaged one, try the renamed one anyway
            n = getRenamingProvider().getRenamedNoun(getLanguage(), resolvedDbName);
            if (n != null) return n;
        }

        Noun n;
        // okay, we need to construct Noun from template.
        if (!ei.hasStandardLabel()) {
            // The name field's in the translation table or on the object itself
            // if its custom, it normally returns the value above. Only gets
            // here if renaming in another language
            n = getNoun(name, false);
            if (n != null) {
                n = n.clone();
                Renameable.StandardField f = ei.getRenameableFieldForKey(name);
                if (f != null) {
                    String value = ei.getStandardFieldLabel(getLanguage(), f);
                    if (value != null) {
                        for (NounForm form : getDeclension().getAllNounForms()) {
                            n.setString(intern(value), form);
                        }
                    }
                } else if (doFormat) {
                    // Create the forms based on the plural vs. not plural of the "default" noun
                    for (NounForm form : n.getNounType() == NounType.ENTITY? getDeclension().getAllNounForms() : getDeclension().getFieldForms()) {
                        // Usually, the strings are of the form "{0} View" or something like that.  If the string's filled in, assume it's a MessageFormat
                        String str = n.getString(form);
                        if (str == null) {
                            // Display something, at least
                            n.setString(intern(form.getNumber().isPlural() ? ei.getLabelPlural() : ei.getLabel()), form);
                        } else {
                            // Escape the string of { and ' before using MessageFormat
                            // because they are special characters
                            java.text.MessageFormat formatter =
                                new java.text.MessageFormat(TextUtil.escapeForMessageFormat(str));
                            formatter.setLocale(getLanguage().getLocale());
                            n.setString(intern(formatter.format(new String[] {form.getNumber().isPlural() ? ei.getLabelPlural() : ei.getLabel()})), form);
                        }
                    }
                }
            }
        } else {
            // OK, so it is custom.  Try and get it from the renaming provider.
            n = getNoun(resolvedDbName, false);
        }
        return n;
    }

    public Noun getNoun(String name, boolean getRenamedValue) {
        // check cache first.
        // Note that any calls from setup screen, this condition always fails.
        if (getRenamedValue && getRenamingProvider().useRenamedNouns()) {
            Noun n = getRenamingProvider().getRenamedNoun(getLanguage(), name);
            if (n != null) return n;
        }
        Noun n = nounMap.get(name);
        return getNounOverride(n);
    }


    public Noun getNounByPluralAlias(String name, boolean getRenamedValue) {
        Noun n = this.nounMapByPluralAlias.get(name);
        // check cache first.
        // Note that any calls from setup screen, this condition always fails.
        if (n != null && getRenamedValue && getRenamingProvider().useRenamedNouns()) {
            Noun renamed = getRenamingProvider().getRenamedNoun(getLanguage(), n.getName());
            if (renamed != null) return renamed;
        }
        return getNounOverride(n);
    }

    public Adjective getAdjective(String name) {
        return adjectiveMap.get(name);
    }

    public Article getArticle(String name) {
        return articleMap.get(name);
    }

    public Multimap<String,Noun> getNounsByEntity() {
        return Multimaps.unmodifiableMultimap(this.nounsByEntityType);
    }

    public void put(String name, GrammaticalTerm term) {
        if (term instanceof Noun) {
            Noun noun = (Noun) term;
            assert name.equals(((Noun)term).getName()) : "Trying to put a noun into the map at the wrong name";
            nounMap.put(intern(name), noun);
            if (noun.getPluralAlias() != null) nounMapByPluralAlias.put(intern(noun.getPluralAlias().toLowerCase()), noun);
        } else if (term instanceof Article) {
            articleMap.put(intern(name), (Article)term);
        } else {
            adjectiveMap.put(intern(name), (Adjective)term);
        }
    }

    /**
     * Copy the terms from the other dictionary into this one.
     * Note, this will *not* clone the Grammatical Terms, so changes made to the nouns in the child
     * label set will override the parent.
     * @param otherDictionary the other dictionary which contains all of the values.
     *
     */
    public void putAll(LanguageDictionary otherDictionary) {
        if (otherDictionary.getLanguage() != this.getLanguage()) {
            throw new IllegalArgumentException("Language mismatch: " + this.getLanguage() + " != " + otherDictionary.getLanguage());
        }
        nounMap.putAll(otherDictionary.nounMap);
        nounMapByPluralAlias.putAll(otherDictionary.nounMapByPluralAlias);
        articleMap.putAll(otherDictionary.articleMap);
        adjectiveMap.putAll(otherDictionary.adjectiveMap);
        nounsByEntityType.putAll(otherDictionary.nounsByEntityType);
        if (otherDictionary.nounVersionOverrides != null) {
            if (this.nounVersionOverrides == null) {
                this.nounVersionOverrides = makeNounVersionOverrideMap();
            }
            this.nounVersionOverrides.putAll(otherDictionary.nounVersionOverrides);
        }
    }

    // validate all modifiers
    public void validateAll() {
        nounMap.validate();
        adjectiveMap.validate();
        articleMap.validate();
        if (this.nounVersionOverrides != null) {
            for (NounVersionOverride nvo : this.nounVersionOverrides.values()) {
                nvo.getNoun().validate(nvo.getNoun().getName());
                nvo.getNoun().makeSkinny();
            }
        }
    }

    /**
     * @return a sorted list of noun names for the given entity.
     * @param tableEnum the entity name to get the group
     * @param includeEntity if the entity noun itself should be included, or if false, just the fields.
     */
    public final SortedSet<String> getNames(String tableEnum, boolean includeEntity) {
        Collection<Noun> m = nounsByEntityType.get(tableEnum.toLowerCase());
        TreeSet<String> list = new TreeSet<>();
        for (Noun noun : m) {
            String s = noun.getName();
            // skip for the custom dummy name
            if (s.equalsIgnoreCase(Renameable.ENTITY_NAME)) continue;

            if (noun.getNounType() != NounType.ENTITY || includeEntity) list.add(s);
        }
        return list;
    }

    /**
     * @return true if the given name has entity dictionary data
     * @param name the name of the entity to lookup
     */
    public boolean isEntity(String name) {
        Noun n = this.nounMap.get(name.toLowerCase());
        if (n == null) return false;
        return n.getNounType() == NounType.ENTITY;
    }

    public boolean isEntityPlural(String name) {
        Noun n = this.nounMapByPluralAlias.get(name.toLowerCase());
        if (n == null) return false;
        return n.getNounType() == NounType.ENTITY;
    }

    public boolean isAdjective(String name) {
        return this.adjectiveMap.containsKey(name.toLowerCase());
    }

    public boolean isArticle(String name) {
        return this.articleMap.containsKey(name.toLowerCase());
    }

    public boolean isNoun(String name) {
        return this.nounMap.containsKey(name.toLowerCase()) || this.nounMapByPluralAlias.containsKey(name.toLowerCase());
    }

    /**
     * For the given term (which should be in lowercase), return the grammatical term it may be
     * @param name the name of the term (which must be in lowercase
     * @return  the term associated with the name, or null if there is no such term
     */
    public GrammaticalTerm getTerm(String name) {
        Noun n = this.nounMap.get(name);
        if (n != null) return getNounOverride(n);

        n = this.nounMapByPluralAlias.get(name);
        if (n != null) return getNounOverride(n);

        Adjective adj = this.adjectiveMap.get(name);
        if (adj != null) return adj;

        Article art = this.articleMap.get(name);
        if (art != null) return art;
        return null;
    }

    /**
     * @return true if the given name is for custom entity
     * @param name the name of the entity to lookup
     */
    public boolean isCustom(String name) {
        Collection<Noun> nouns = nounsByEntityType.get(Renameable.ENTITY_NAME);
        for (Noun n : nouns) {
            if (n.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------
    // LanguageDictionaryParser support methods
    //

    // TODO: We shouldn't allow duplicates. (Although the way that zh_CN works, means we need to support "overrides"

    public Noun getOrCreateNoun(String tableEnum, String name, String pluralAlias, NounType type, LanguageGender gender, LanguageStartsWith startsWith,
            String access, boolean isStandardField) {
        // final stage. create Noun if it does not exist yet.
        // note that if localized dictionary is loaded, this is
        // called more than once against the same name. In that
        // case, it should reuse and overwrite the attributes only.
        Noun n = nounMap.get(name);
        if (n == null)
            n = createNoun(name, pluralAlias, type,
                    tableEnum,
                    startsWith == null ? getDeclension().getDefaultStartsWith() : startsWith, gender == null ? getDeclension().getDefaultGender() : gender, access, isStandardField, false);
        else {
            if (n.isCopiedFromDefault()) {
                // If the noun was copied, we must be overwriting it with new stuff
                n = createNoun(name, pluralAlias, type,
                        tableEnum,
                        startsWith == null ? getDeclension().getDefaultStartsWith() : startsWith, gender == null ? getDeclension().getDefaultGender() : gender, access, isStandardField, false);

            } else  if (n.getGender() != gender // Validate that it's the same
                    || n.getStartsWith() != startsWith
                    || n.isStandardField() != isStandardField) {
                // We go in here when a label file is overriden with another
                // Ex: when processing Mexican Spanish over regular Spanish OR when using the label renderer
                n.setGender(gender);
                n.setStartsWith(startsWith);
            }
        }

        return n;
    }

    public Adjective getOrCreateAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        Adjective adjective = adjectiveMap.get(name);
        if (adjective == null) {
            adjective = createAdjective(name, startsWith, position, false);
        }
        return adjective;
    }

    public Article getOrCreateArticle(String name, LanguageArticle articleType) {
        Article article = articleMap.get(name);
        if (article == null) {
            article = getDeclension().createArticle(name, articleType);
        } else {
            if (article.getArticleType() != articleType) {
                throw new RuntimeException("Parsing the article again with different values?");
            }
        }
        return article;
    }

    // --------------------------------------------------------------------------------------------
    // Utility methods
    //

    // Provides access to the language dictionary during the parsing phase.  After parsing is over, this is "shut off"
    public void setString(Noun n, NounForm form, String value) {
        if (isSkinny) throw new UnsupportedOperationException("Trying to modify noun " + n + " after made skinny.");
        n.setString(intern(value), form);
    }

    public void setString(Adjective m, AdjectiveForm form, String value) {
        if (isSkinny) throw new UnsupportedOperationException("Trying to modify adjective " + m + " after made skinny.");
        m.setString(form, intern(value));
    }


    public void setString(Article m, ArticleForm form, String value) {
        if (isSkinny) throw new UnsupportedOperationException("Trying to modify article " + m + " after made skinny.");
        m.setString(form, intern(value));
    }

    @Override
    public String toString() {
        return "LanguageDictionary:" + getLanguage();
    }

    /**
     * Write the grammatical terms associated with the given terms
     * @param appendable the appendable to write to
     * @param useRenamedNouns if true, use the renamed nouns instead of the default ones.  If you're doing work around
     *        renaming nouns, you often want to pass in false for this
     * @param termsToInclude optional set of term to include, if omitted all terms are included
     * @throws IOException if an error happens during append
     */
    /**
     * Write the grammatical terms associated with the given terms
     * @param appendable the appendable to write to
     * @param useRenamedNouns if true, use the renamed nouns instead of the default ones.  If you're doing work around
     *        renaming nouns, you often want to pass in false for this
     * @param termsToInclude optional set of term to include, if omitted all terms are included
     * @throws IOException if an error happens during append
     */
    public void writeJson(Appendable appendable, boolean useRenamedNouns, Collection<String> termsToInclude) throws IOException {
        RenamingProvider renamingProvider = useRenamedNouns ? RenamingProviderFactory.get().getProvider() : null;
        appendable.append("{\"n\":");
        nounMap.writeJson(appendable, renamingProvider, this, termsToInclude);
        appendable.append(",\"a\":");
        adjectiveMap.writeJson(appendable, renamingProvider, this, termsToInclude);
        if (!this.articleMap.isEmpty()) {
            appendable.append(",\"d\":");
            articleMap.writeJson(appendable, renamingProvider, this, termsToInclude);
        }
        appendable.append("}");
    }

    /**
     * Write the grammatical terms associated with
     * @param appendable the appendable to write to
     * @param useRenamedNouns if true, use the renamed nouns instead of the default ones.  If you're doing work around
     *        renaming nouns, you often want to pass in false for this
     * @param termsToInclude optional set of term to include, if omitted all terms are included
     * @throws IOException if an error happens during append
     */
    public void writeJsonTerms(Appendable out, boolean useRenamedNouns, Collection<GrammaticalTerm> terms) throws IOException {
        Collection<String> termsToInclude =
                terms == null ? null :
                terms.stream().map((t)->t.getName()).collect(Collectors.toSet());
        writeJson(out, useRenamedNouns, termsToInclude);
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        makeSkinny();
        out.defaultWriteObject();
        writeObjectInternal(out);
    }

    protected void writeObjectInternal(ObjectOutputStream out) throws IOException {
        // do nothing here - for hook
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.declension = LanguageDeclensionFactory.get().getDeclension(this.language);
        this.readObjectInternal(in);
    }

    protected void readObjectInternal(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.nounsByEntityType = ArrayListMultimap.create();
        for (Noun n : this.nounMap.values()) {
            if (n.getEntityName() != null) this.nounsByEntityType.put(intern(n.getEntityName().toLowerCase()), n);
        }
        this.isSkinny = true;
    }

    public Noun getNounOverride(Noun n) {
        if (n == null) return null;
        if (this.nounVersionOverrides == null) return n;
        SortedSet<NounVersionOverride> overrides = this.nounVersionOverrides.get(n);
        if (overrides == null) return n;

        RenamingProvider renamingProvider = getRenamingProvider();
        double labelVersion = renamingProvider.getLabelVersion();
        for (NounVersionOverride nvo : overrides) {
            if (labelVersion >= nvo.getAtLeast()) {
                return nvo.getNoun();
            }
        }

        return n;
    }

    private static class TreeSetSupplier<V> implements Supplier<SortedSet<V>>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public SortedSet<V> get() {
            return new TreeSet<V>();
        }
    }

    private SortedSetMultimap<Noun, NounVersionOverride> makeNounVersionOverrideMap() {
        return Multimaps.newSortedSetMultimap(new IdentityHashMap<>(), new TreeSetSupplier<NounVersionOverride>());
    }

    public void setNounOverride(Noun n, Noun override, double version) {
        if (this.nounVersionOverrides == null) {
            // IdentityHashMap to keep it quick and prevent any funny business
            this.nounVersionOverrides = makeNounVersionOverrideMap();
        }

        if (!this.nounVersionOverrides.put(n, new NounVersionOverride(override, version))) {
            throw new IllegalArgumentException(
                    "Noun " + n + " in language " + this.getLanguage() + " has duplicate versions for " + version);
        }
    }

    /**
     * {@link LanguageDictionary} store metadata in {@link Map}'s that have a high memory per element cost. The various
     * dictionaries are static in nature and would not change after the initial load. {@link ImmutableSortedMap}'s have
     * a 8 byte overhead cost which is attractive to keep the required space of {@link LanguageDictionary} to a minimum.
     * Since {@link LanguageDictionary}'s are static, this make them a perfect candidate for {@link ImmutableSortedMap}.
     */
    public void makeSkinny() {
        nounMap = nounMap.makeSkinny();
        nounMapByPluralAlias = nounMapByPluralAlias.makeSkinny();
        adjectiveMap = adjectiveMap.makeSkinny();
        articleMap = articleMap.makeSkinny();
        // don't convert nounsByEntityType here. it may be updated by #createNoun

        isSkinny = true; // Prevent adding anything to this dictionary set. By assumption, the nouns are skinny.
    }

    /**
     * Has the override for a noun that has a different value for for different API versions.
     *
     * Note: as of 0.7.0 (226), the "newer" version is the override, and the old version will be the
     * generic basis for overrides.
     *
     * The reasoning is that overriding the "old" values causes the least surprise if you don't
     * override every value.
     * If this reasoning is faulty, the "atLeast" should probably switch to "lessThan" so that
     * the default noun is the latest one and the oldest/original one should be the override.
     *
     * This means the comparison is *reversed*
     *
     * @author stamm
     * @since 226.0
     */
    static final class NounVersionOverride implements Serializable, Comparable<NounVersionOverride> {
        private static final long serialVersionUID = 1L;
        private final Noun noun;
        private final double atLeast;

        public NounVersionOverride(Noun noun, double atLeast) {
            this.noun = noun;
            this.atLeast = atLeast;
        }

        /**
         * @return the noun override.
         */
        public Noun getNoun() {
            return noun;
        }

        /**
         * @return the version that should be returned if the api version on RenmaingProvider is
         * at least this value.
         */
        public double getAtLeast() {
            return atLeast;
        }

        @Override
        public int hashCode() {
            return Objects.hash(atLeast, noun);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            NounVersionOverride other = (NounVersionOverride) obj;
            return Double.doubleToLongBits(atLeast) == Double.doubleToLongBits(other.atLeast)
                    && Objects.equals(noun, other.noun);
        }

        @Override
        public int compareTo(NounVersionOverride o) {
            // comparison is reversed... so you can go in order from newest to oldest and ask atLeast
            return Double.compare(o.getAtLeast(), atLeast);
        }
    }

}
