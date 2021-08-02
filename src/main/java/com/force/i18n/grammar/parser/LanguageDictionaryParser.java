/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.settings.TrackingHandler;

/**
 * Parser class for dictionary based label fileset.
 * <p>
 * During parsing, all state is kept in the DictionaryLoaderData object, which annotates
 * the LanguageDictionary with alias information
 *
 * @author nveeser,stamm
 */
public final class LanguageDictionaryParser {
    private final LanguageDictionary dictionary;
    private final GrammaticalLabelSetDescriptor dictDesc;
    private final GrammaticalLabelSetProvider parentProvider; // Optional parent dictionary
    private final LanguageDictionary parentDictionary;

    /**
     * Parse and load the dictionary for the descriptor with the given language
     * @param dictDesc the description of the location of the grammatical label
     * @param language the dictionary to stat with
     * @param parentProvider the parentProvider if this parser is for overriding labels from a different labelset
     * @throws IOException if there is a parsing exception.
     */
    public LanguageDictionaryParser(GrammaticalLabelSetDescriptor dictDesc, HumanLanguage language, GrammaticalLabelSetProvider parentProvider) throws IOException {
        this(new LanguageDictionary(language), dictDesc, parentProvider);
        loadDictionary();
        dictionary.makeSkinny();
    }

    // Private constructor used *only* to get default english values for nouns that don't otherwise exist
    private LanguageDictionaryParser(LanguageDictionary dictionary, GrammaticalLabelSetDescriptor dictDesc, GrammaticalLabelSetProvider parentProvider) {
        this.dictionary = dictionary;
        this.dictDesc = dictDesc;
        this.parentProvider = parentProvider;
        this.parentDictionary = parentProvider != null ? this.parentProvider.getSet(this.dictionary.getLanguage()).getDictionary() : null;
        // DO NOTHING HERE, IT'S UP TO THE CALLER TO PARSE OR WHATEVER
    }

    private void copyFallbackTerms(HumanLanguage fallbackLanguage) throws IOException {
        assert fallbackLanguage != null && fallbackLanguage != this.dictionary.getLanguage(): "You cannot have fallback terms without a fallback language";

        // We need to check for things that haven't yet been translated into this language (or never will be), but may appear in the labels.
        // So, we parse english, see the set of nouns defined in english, and then make sure we have a value in *this* language for all of those nouns by defaulting to the Singular value from english
        LanguageDictionaryParser fallbackParser = new LanguageDictionaryParser(new LanguageDictionary(fallbackLanguage), this.dictDesc.getForOtherLanguage(fallbackLanguage), parentProvider);
        fallbackParser.parseAllDictionaries();
        LanguageDictionary fallbackDictionary = fallbackParser.getDictionary();

        // If the declension is a subclass of the parent declesnion or proxy, then we should copy over the forms directly
        boolean copyFormsDirectly = LanguageDeclensionFactory.get().isForwardingProxy(this.dictionary.getDeclension())
            || fallbackDictionary.getDeclension().getClass().isAssignableFrom(this.dictionary.getDeclension().getClass());

        Set<String> fallbackNouns = new HashSet<String>(fallbackDictionary.getAllTermNames(TermType.Noun));
        fallbackNouns.removeAll(this.dictionary.getAllTermNames(TermType.Noun));  // Remove all translated nouns

        NounForm defaultNounForm = this.dictionary.getDeclension().getNounForm(LanguageNumber.SINGULAR, LanguageArticle.ZERO);
        NounForm pluralNounForm = this.dictionary.getDeclension().hasPlural() ? this.dictionary.getDeclension().getNounForm(LanguageNumber.PLURAL, LanguageArticle.ZERO) : null;
        NounForm fallbackPluralNounForm = fallbackDictionary.getDeclension().getNounForm(LanguageNumber.PLURAL, LanguageArticle.ZERO);

        for (String nounToAdd : fallbackNouns) {
            Noun fallbackNoun = fallbackDictionary.getNoun(nounToAdd, false);
            Noun localNoun;
            if (copyFormsDirectly) {
                localNoun = this.dictionary.createNoun(fallbackNoun.getName(), fallbackNoun.getPluralAlias(), fallbackNoun.getNounType(), fallbackNoun.getEntityName(),
                        fallbackNoun.getStartsWith(), fallbackNoun.getGender(), fallbackNoun.getAccess(), fallbackNoun.isStandardField(), true);
                for (Map.Entry<? extends NounForm, String> formEntry : fallbackNoun.getAllDefinedValues().entrySet()) {
                    // Store the fallback NounForm's value into the current dictionary using
                    // this.dictionary's NounForm (nounForms are created once per declension)
                    // This is also true for AdjectiveForm and ArticleForm.
                    NounForm dictNf = this.dictionary.getDeclension().getNounForm(formEntry.getKey());
                    this.dictionary.setString(localNoun, dictNf, formEntry.getValue());
                }
            } else {
                localNoun = this.dictionary.createNoun(fallbackNoun.getName(), fallbackNoun.getPluralAlias(), fallbackNoun.getNounType(), fallbackNoun.getEntityName(),
                        dictionary.getDeclension().getDefaultStartsWith(), dictionary.getDeclension().getDefaultGender(), fallbackNoun.getAccess(), fallbackNoun.isStandardField(), true);
                this.dictionary.setString(localNoun, defaultNounForm, fallbackNoun.getDefaultString(false));
                if (pluralNounForm != null && fallbackNoun.getAllDefinedValues().containsKey(fallbackPluralNounForm)) {
                    // Copy the plural value if it exists.
                    this.dictionary.setString(localNoun, pluralNounForm, fallbackNoun.getDefaultString(true));
                }
            }
            this.dictionary.put(nounToAdd, localNoun);
        }

        // Copy over adjectives (but not articles)
        Set<String> fallbackAdjectives = new HashSet<String>(fallbackDictionary.getAllTermNames(TermType.Adjective));
        fallbackAdjectives.removeAll(this.dictionary.getAllTermNames(TermType.Adjective));  // Remove all translated adjectives
        for (String adjectiveToAdd : fallbackAdjectives) {
            Adjective fallbackAdj = fallbackDictionary.getAdjective(adjectiveToAdd);
            Adjective localAdj;
            if (copyFormsDirectly) {
                localAdj = this.dictionary.createAdjective(fallbackAdj.getName(), fallbackAdj.getStartsWith(), fallbackAdj.getPosition(), true); // True means we're inherited
                for (Map.Entry<? extends AdjectiveForm, String> formEntry : fallbackAdj.getAllValues().entrySet()) {
                    AdjectiveForm dictAjF = this.dictionary.getDeclension().getAdjectiveForm(formEntry.getKey());
                    this.dictionary.setString(localAdj, dictAjF, formEntry.getValue());
                }
            } else {
                localAdj = this.dictionary.createAdjective(fallbackAdj.getName(), dictionary.getDeclension().getDefaultStartsWith(),
                        dictionary.getDeclension().getDefaultAdjectivePosition(), true); // True means we're inherited
                for (AdjectiveForm form : this.dictionary.getDeclension().getAdjectiveForms()) {
                    this.dictionary.setString(localAdj, form, fallbackAdj.getDefaultValue());
                }
            }
            this.dictionary.put(adjectiveToAdd, localAdj);
        }

        // Copy over article iff we are a child class.
        if (dictionary.getDeclension().hasArticle() && copyFormsDirectly) {
            Set<String> fallbackArticles = new HashSet<String>(fallbackDictionary.getAllTermNames(TermType.Article));
            fallbackArticles.removeAll(this.dictionary.getAllTermNames(TermType.Article));  // Remove all translated Articles
            for (String articleToAdd : fallbackArticles) {
                Article fallbackArt = fallbackDictionary.getArticle(articleToAdd);
                Article localArt = this.dictionary.createArticle(fallbackArt.getName(), fallbackArt.getArticleType(), true); // True means we're inherited
                for (Map.Entry<? extends ArticleForm, String> formEntry : fallbackArt.getAllValues().entrySet()) {
                    ArticleForm dictArF = this.dictionary.getDeclension().getArticleForm(formEntry.getKey());
                    this.dictionary.setString(localArt, dictArF, formEntry.getValue());
                }
                this.dictionary.put(articleToAdd, localArt);
            }
        }
    }

    private void parseAllDictionaries() throws IOException {
        URL rootFile = this.dictDesc.getDictionaryFile();  // Do you have the "english" sfdcnames.xml
        if (this.parentDictionary != null) {
            // Copy over the terms from the parent.
            // For performance and memory reasons, we clone the maps, but we do not clone the terms
            this.dictionary.putAll(parentDictionary);
            if (!TrackingHandler.exists(rootFile)) return;  // Allow a null root if we only have labels and no additional grammatical terms
        }
        if (this.dictDesc.hasOverridingFiles()) {
            // We're not english
            List<URL> labels = this.dictDesc.getOverridingDictionaryFiles();
            // Go through the locale specific files in order of specificity
            for (URL f : labels) {
                if (TrackingHandler.exists(f)) {
                    parseDictionary(f);
                }
            }

            copyFallbackTerms(this.dictDesc.getLanguage().getFallbackLanguage());
        } else {
            if (!TrackingHandler.exists(rootFile)) {
                throw new FileNotFoundException("can't read root names file: " + rootFile);
            }

            assert this.dictionary.getLanguage() == LanguageProviderFactory.get().getBaseLanguage();
            parseDictionary(rootFile);
        }
    }

    private void loadDictionary() throws IOException {
        parseAllDictionaries();

        // If we have a test language override, then use it to parse the stuff.
        if (this.dictDesc instanceof TestLanguageLabelSetDescriptor) {
            String grammarOverride = ((TestLanguageLabelSetDescriptor)this.dictDesc).getGrammar();
            if (grammarOverride != null) {
                LanguageDictionaryHandler handler = new LanguageDictionaryHandler(this.dictDesc.getDictionaryFile(), this);
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                try {
                    SAXParser saxParser = spf.newSAXParser();
                    saxParser.parse(new ByteArrayInputStream(grammarOverride.getBytes("UTF-8")), handler);
///CLOVER:OFF
                } catch (ParserConfigurationException x) {
                    throw new IOException(x);
                } catch (SAXException x) {
                    throw new IOException(x);
                }
///CLOVER:ON
            }
        }

        // finalize all adjectives
        this.getDictionary().validateAll();
    }

    public LanguageDictionary getDictionary() {
        return this.dictionary;
    }
    
    LanguageDictionary getParentDictionary() {
    	return this.parentDictionary;
    }

    /**
     * @return whether this.parentDictionary is not null and is the same language
     * Used to proactively clone entities if inherited from parent dictionaries
     */
    boolean hasParentDictionarySameLang() {
    	return this.parentDictionary != null && this.parentDictionary.getLanguage() == this.getDictionary().getLanguage();
    }

    void parseDictionary(URL file) {
        LanguageDictionaryHandler handler = new LanguageDictionaryHandler(file, this);
        parse(file, handler);
    }

    private void parse(URL file, TrackingHandler handler) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            saxParser.parse(file.openStream(), handler);

        }
        catch (Exception ex) {
            throw new RuntimeException("Error parsing XML file " + handler.getLineNumberString(), ex);
        }
    }
}
