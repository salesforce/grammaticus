/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.force.i18n.commons.text.GenericTrieMatcher;
import com.force.i18n.commons.text.GenericTrieMatcher.GenericTrieMatch;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageDictionary;
import com.force.i18n.grammar.LanguagePossessive;
import com.force.i18n.grammar.ModifierForm;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.NounForm;
import com.force.i18n.grammar.NounModifier;
import com.force.i18n.settings.BasePropertyFile;
import com.force.i18n.settings.ParameterNotFoundException;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Set of routines to simplify access to LabelUtils
 *
 * @author stamm
 */
public enum LabelUtils {
    INSTANCE;

    public static LabelUtils get() { return INSTANCE; }

    /**
     * Will either throw an exception or log the missing label, depending on
     * whether or not we're in production
     *
     * @param message - String to return and log with
     * @return the message with a prefix
     */
    public String processMissingLabel(String message) {
        if (I18nJavaUtil.isDebugging()) {
            throw new ParameterNotFoundException(message);
        } else {
            return BasePropertyFile.MISSING_LABEL + message;
        }
    }

    public String processMissingLabel(String labelSection, String labelId) {
        // Message string copied from GrammaticalLabelSetImpl.get()
        return processMissingLabel(
            "PropertyFile - val " + labelId + " not found in section " + labelSection);
    }

    /**
     * Provide an english-list description of a noun form.  Note, if there is only one form supported, this will return the empty string.
     * @param declension the language being declined
     * @param form the noun form in question
     * @return an english string that describes the noun form suitable for use in the metadata api
     */
    public String getFormDescriptionInEnglish(LanguageDeclension declension, NounForm form) {
        StringBuilder descStr = new StringBuilder();
        if (declension.hasPlural()) descStr.append(" ").append(form.getNumber().name().toLowerCase());
        if (form.getArticle() != LanguageArticle.ZERO) descStr.append(" ").append(form.getArticle().name().toLowerCase());
        if (declension.hasPossessive() && form.getPossessive() != LanguagePossessive.NONE) {
            descStr.append(" ");
            switch (form.getPossessive()) {
            case NONE:             break;
            case SECOND:           descStr.append("second person possessive"); break;
            case SECOND_PLURAL:    descStr.append("second person plural possessive"); break;
            case FIRST:            descStr.append("first person possessive"); break;
            case FIRST_PLURAL:     descStr.append("first person plural possessive"); break;
            }
        }
        if (declension.hasAllowedCases()) descStr.append(" ").append(form.getCase().name().toLowerCase());
        return descStr.length() > 0 ? descStr.substring(1) : "simple";
    }

    /**
     *
     * @param labelSet the label set that contains the
     * @param section the section to return
     * @param param the key to the label to return
     * @throws IllegalArgumentException if you cannot access this label
     * @return the public label at the given section and param, if the parameter is public
     */
    public String getPublicString(GrammaticalLabelSet labelSet, String section, String param) throws IllegalArgumentException {
        if (labelSet.getPublicSectionNames() == null || !labelSet.getPublicSectionNames().contains(section.toLowerCase())) {
            throw new IllegalArgumentException("You cannot access this section");
        }
        return labelSet.getString(section.toLowerCase(), param.toLowerCase());
    }

    /**
     * Construct a valid names.xml style file out of the user-entered grammar file.
     * This will check to see if the file is "valid" xml, and if so, it will remove the DTD which causes
     * the parser to choke; otherwise it will turn the XML fragment into a fully formed names file.
     * @param grammar XML snippet
     * @return a sample dictionary file containing the xml snippet inside the "names" tag
     */
    public static String getSampleGrammarFile(@Nullable String grammar) {
        if (grammar == null || grammar.isEmpty()) {
            return null;
        } else {
            // If they pasted in a whole file, it's fine.
            if (!grammar.contains("<sfdcnames>") && !grammar.contains("<sfdcadjectives>")
            		&& !grammar.contains("<names>") && !grammar.contains("<adjectives>")) {
                StringBuilder sb = new StringBuilder(grammar.length() + 100);
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sb.append("<names>");
                sb.append(grammar);
                sb.append("</names>");
                return sb.toString();
            } else {
                // Remove the dtd which confuses the parser
            	String fixedGrammar = grammar.replace("<!DOCTYPE sfdcnames SYSTEM \"sfdcnames.dtd\">", "");
                fixedGrammar = fixedGrammar.replace("<!DOCTYPE sfdcadjectives SYSTEM \"sfdcadjectives.dtd\">", "");
                fixedGrammar = fixedGrammar.replace("<!DOCTYPE names SYSTEM \"names.dtd\">", "");
                fixedGrammar = fixedGrammar.replace("<!DOCTYPE adjectives SYSTEM \"adjectives.dtd\">", "");
                return fixedGrammar;  // it's good enough
            }
        }
    }

    /**
     * Construct a valid labels.xml style file for the given text, where the label will be at
     * LabelRef("Test","Test").
     * @param text the unescaped text to place inside the label, or to construct an alias if the string
     * starts with "alias="
     * @return a sample label file that can be used for evaluating a given label.
     */
    public static String getSampleLabelFile(String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE iniFile SYSTEM \"labels.dtd\">");
        sb.append("<iniFile><section name=\"Test\"><param name=\"Test\" ");
        if (text != null && text.startsWith("alias=")) {
            sb.append(text).append(">");
        } else {
            sb.append(">").append(text);
        }
        sb.append("</param></section></iniFile>");

        return sb.toString();
    }

    /**
     * Given a non-nounified input, try to replace nouns and adjectives from the dictionary
     * with the appropriate noun xml tags.
     * @return the input string with Nouns and Articles converted to tags if possible
     * @param input the string to nounify
     * @param dictionary the dictionary with the nouns to use to match
     */
    public String nounify(String input, LanguageDictionary dictionary) {
        return new Nounifier(dictionary).nounifyString(input);
    }

    /**
     * Set of locale langauges strings where the results are JDK dependent
     */
    static final Set<String> JDK_DEPENDENT_LANGUAGE = ImmutableSet.of(LanguageConstants.YIDDISH_ISO,
    		LanguageConstants.HEBREW_ISO, LanguageConstants.INDONESIAN_ISO);


    public static List<URL> getFileNames(HumanLanguage language,URL rootDirectory, String basename ) {
        List<URL> list = new ArrayList<>();
        try {
    	    Locale locale = language.getLocale();
            list.add(new URL(rootDirectory,  locale.getLanguage() + '/' + basename));
            if (JDK_DEPENDENT_LANGUAGE.contains(locale.getLanguage())) {
                list.add(new URL(rootDirectory,  language.getOverrideLanguage() + '/' + basename));
            }
    	    if (locale.getCountry().length() > 0) {
    	        /*
    	         * Code required because there is no Chinese traditional locale in jdk Locale.java and taiwan and Hong kong are 2 different countries
    	         * We will assume that Hong Kong (HK country code) derive the Traditional Chinese from Taiwan (TW)
    	         * In the future, to define Hong Kong label, a new directory will need to be created zh/HK.
    	         */
    	        if(language.getLocaleString().equals(LanguageConstants.CHINESE_HK)){
    	            //adding Taiwan as fallback
    	            list.add(new URL(rootDirectory, locale.getLanguage() + '/'+ Locale.TRADITIONAL_CHINESE.getCountry() +'/'+ basename));
    	            list.add(new URL(rootDirectory, locale.getLanguage() + '/'+ locale.getCountry() + '/'+ basename));
    	        }else{
    	            list.add(new URL(rootDirectory, locale.getLanguage() + '/' + locale.getCountry() + '/' + basename));
    	            if (locale.getVariant().length() > 0) {
    	                list.add(new URL(rootDirectory, locale.getLanguage() + '/' + locale.getCountry() + '/' + locale.getVariant() + '/' + basename));
    	            }
    	        }
    	    }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
	    return list;
	}

	private static final Pattern nonalphaPattern = Pattern.compile("\\W");

    // TODO: Switch this to splitter.
    static List<String> tokenize(String input) {
        List<String> result = new ArrayList<String>(24);
        Matcher matcher = nonalphaPattern.matcher(input);
        int startLoc = 0;
        while (matcher.find()) {
            result.add(input.substring(startLoc, matcher.start()));
            result.add(input.substring(matcher.start(), matcher.end()));
            startLoc = matcher.end();
        }
        if (result.size() == 0) {
            result.add(input);
        } else if (startLoc < input.length()) {
            result.add(input.substring(startLoc, input.length()));
        }
        return result;
    }

    /**
     * A structure generated from a dictionary that performs a simple "nounification" process
     * to determine if there should be some "tokenization" of the string.
     */
    public static class Nounifier {
        private final static Set<String> EXCLUDED_NOUNS = ImmutableSet.of("Role", "Email", "Address", "{0}");

        private final GenericTrieMatcher<String> nounMatcher;
        private final GenericTrieMatcher<String> adjMatcher;
        private final GenericTrieMatcher<String> artMatcher;

        public Nounifier(LanguageDictionary dictionary) {
            Map<List<String>, List<String>> nounMap = generateNounToXmlTag(dictionary);
            this.nounMatcher = GenericTrieMatcher.compile(new ArrayList<List<String>>(nounMap.keySet()), new ArrayList<List<String>>(nounMap.values()));
            Map<List<String>, List<String>> adjMap = generateModifierToXmlTag(dictionary, TermType.Adjective);
            this.adjMatcher = GenericTrieMatcher.compile(new ArrayList<List<String>>(adjMap.keySet()), new ArrayList<List<String>>(adjMap.values()));
            if (dictionary.getDeclension().hasArticle()) {
                Map<List<String>, List<String>> artMap = generateModifierToXmlTag(dictionary, TermType.Article);
                this.artMatcher = artMap.size() > 0 ? GenericTrieMatcher.compile(new ArrayList<List<String>>(artMap.keySet()), new ArrayList<List<String>>(artMap.values())) : null;
            } else {
                this.artMatcher = null;
            }
        }

        /**
         * Given a dictionary, create a map from List of tokens of rendered nouns to a list containing a single string
         * which is the Xml tag for that noun.
         * @param dictionary
         * @return
         */
        private Map<List<String>,List<String>> generateNounToXmlTag(LanguageDictionary dictionary) {
            Multimap<Noun,String> nounToEntity = Multimaps.invertFrom(dictionary.getNounsByEntity(), ArrayListMultimap.<Noun,String>create());

            Map<List<String>, List<String>> nounMap = new TreeMap<List<String>,List<String>>(LIKE_BIGGER_COMPARATOR);
            for (String nounName : dictionary.getAllTermNames(TermType.Noun)) {
                Noun n = dictionary.getNoun(nounName, false);
                Set<String> seenValues = new HashSet<String>();
                Map<? extends NounForm,String> entries = n.getAllDefinedValues();
                for (NounForm form : dictionary.getDeclension().getAllNounForms()) {  // Iterate through the noun forms in "canonical" form
                    String value = entries.get(form);
                    if (value == null) continue;

                    // Exclude, by default, nouns that could cause ambiguity
                    if (nounName.contains("_") && !value.contains(" ")) continue;  // Ignore field names that are entity specific (for now)
                    // FIXME: Exclude nouns where the entity name isn't included in the label
                    if (n.getNounType() != NounType.ENTITY) {
                        // Get the related entity, if there is one.
                        Collection<String> entities = nounToEntity.get(n);
                        Noun entityNoun = entities.size() > 0 ? dictionary.getNoun(entities.iterator().next().toLowerCase(), false) : null;
                        if (entityNoun != null) {
                            if (!value.toLowerCase().contains(entityNoun.getDefaultString(false).toLowerCase())
                                    && !value.toLowerCase().contains(entityNoun.getDefaultString(true).toLowerCase())) continue;
                        }
                    }
                    if (value.contains("{0}")) continue;
                    if (EXCLUDED_NOUNS.contains(value)) continue;  // Ignore overly ambitious nouns
                    if (!seenValues.add(value)) continue;           // If we've already seen this form for this noun name, ignore it.

                    String valueLower = value.toLowerCase();

                    StringBuilder extras = new StringBuilder(16);
                    if (form.getCase() != dictionary.getDeclension().getDefaultCase()) {
                        extras.append(" case=\"").append(form.getCase().getDbValue()).append("\"");
                    }
                    if (form.getArticle() != dictionary.getDeclension().getDefaultArticle()) {
                        extras.append(" article=\"").append(form.getArticle().getDbValue()).append("\"");
                    }
                    if (form.getPossessive() != dictionary.getDeclension().getDefaultPossessive()) {
                        extras.append(" poss=\"").append(form.getPossessive().getDbValue()).append("\"");
                    }
                    List<String> tokenizedValue = tokenize(value);
                    List<String> tokenizedLower = tokenize(valueLower);
                    if (form.getNumber().isPlural()) {
                        if (n.getPluralAlias() != null) {
                            nounMap.put(tokenizedValue, Collections.singletonList("<" + TextUtil.initCap(n.getPluralAlias()) + extras + "/>"));
                            nounMap.put(tokenizedLower, Collections.singletonList("<" + n.getPluralAlias().toLowerCase() + extras + "/>"));
                        }
                    } else {
                        nounMap.put(tokenizedValue, Collections.singletonList("<" + TextUtil.initCap(n.getName()) + extras + "/>"));
                        nounMap.put(tokenizedLower, Collections.singletonList("<" + n.getName().toLowerCase() + extras + "/>"));
                    }
                }
            }
            return nounMap;
        }

        private static final Comparator<List<String>> LIKE_BIGGER_COMPARATOR = new Comparator<List<String>>() {
            @Override
            public int compare(List<String> o1, List<String> o2) {
                if (o1.size() != o2.size()) return o2.size() - o1.size();
                for (int i = 0; i < o1.size(); i++) {
                    int compareTo = o1.get(i).compareTo(o2.get(i));
                    if (compareTo != 0) return compareTo;
                }
                assert o1.equals(o2) : "Returning 0 for comparison between list of strings";
                return 0;  // They're actually the same
            }

        };



        private Map<List<String>, List<String>> generateModifierToXmlTag(LanguageDictionary dictionary, TermType termType) {
            Map<List<String>, List<String>> modMap = new TreeMap<List<String>,List<String>>(LIKE_BIGGER_COMPARATOR);
            for (String modName : dictionary.getAllTermNames(termType)) {
                NounModifier m = termType == TermType.Adjective ? dictionary.getAdjective(modName) : dictionary.getArticle(modName);
                for (Map.Entry<? extends ModifierForm,String> entry : m.getAllValues().entrySet()) {
                    String value = entry.getValue();
                    String valueLower = value.toLowerCase();
                    // Put in spaces to make sure you don't match stuff inside other tags
                    modMap.put(tokenize(value), Collections.singletonList("<" + TextUtil.initCap(m.getName()) + "/>"));
                    if (!valueLower.equals(value)) {
                        modMap.put(tokenize(valueLower), Collections.singletonList("<" + m.getName().toLowerCase() + "/>"));
                    }
                }
            }
            return modMap;
         }

        public GenericTrieMatcher<String> getNounMatcher() {
            return this.nounMatcher;
        }

        public GenericTrieMatcher<String> getAdjectiveMatcher() {
            return this.adjMatcher;
        }
        public GenericTrieMatcher<String> getArticleMatcher() {
            return this.artMatcher;
        }
        public String nounifyString(String input) {
            List<String> tokenized = tokenize(input);
            List<String> result = GenericTrieMatcher.replaceMultiple(tokenized, getNounMatcher());
            if (result.equals(tokenized)) return input;
            // Try to adjectivize
            result = GenericTrieMatcher.replaceMultiple(result, getAdjectiveMatcher(), MODIFIER_VALIDATOR);
            if (getArticleMatcher() != null) {
                result = GenericTrieMatcher.replaceMultiple(result, getArticleMatcher(), MODIFIER_VALIDATOR);
            }
            // OK, now try to render and make sure it's the same.
            return Joiner.on("").join(result);
        }

        /**
         * A modifier validator that only tries to find noun modifiers *near* a real noun.
         */
        private static final GenericTrieMatcher.MatchValidator<String> MODIFIER_VALIDATOR = new GenericTrieMatcher.MatchValidator<String>() {
            @Override
            public boolean isValidMatch(GenericTrieMatch<String> match, List<String> src) {
                // Look to see if there's a noun near the position.  Use 5 to mean 2 words and a space
                int min = Math.max(0, match.getPosition() - 5);
                int max = Math.min(src.size(), match.getPosition() + 5);
                for (String str : src.subList(min, max)) {
                    if (str.startsWith("<")) return true;
                }
                return false;
            }
        };
    }

    /**
     * If you have a URL from Class.getResoruce, what do you provide to the URL constructor to get to the directory
     * levels above the current one with that URL as the context
     * @param levels the number of directories to go up
     * @return what to pass into {@link URL#URL(URL, String)} to get the URL to be levels above
     */
    public static String getParentLevelPath(int levels) {
        switch(levels) {
        case 0: return ".";
        case 1: return "..";
        case 2: return "../..";
        case 3: return "../../..";
        }
        throw new IllegalArgumentException("Bad locale");
    }
}
