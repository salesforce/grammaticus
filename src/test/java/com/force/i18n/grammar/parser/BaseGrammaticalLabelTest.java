/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.Renameable.StandardField;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.settings.MapPropertyFileData;
import com.force.i18n.settings.TrackingHandler;
import com.google.common.collect.ImmutableMap;

/**
 * @author stamm
 */
public abstract class BaseGrammaticalLabelTest extends TestCase {
    private static Map<HumanLanguage, LanguageDictionary> dictionaryMap = new ConcurrentHashMap<HumanLanguage, LanguageDictionary>(64, 0.75f, 1);

    public BaseGrammaticalLabelTest(String name) {
        super(name);
    }

    private URL baseDir = null;

    private static final String LABEL_SET_NAME = "sample";
    private static final String LABELS_XML = "labels.xml";
    private static final String NAMES_XML = "names.xml";

    /**
     * Allow child
     * @return the label URL set
     * @throws IOException if there's an error
     */
    protected URL getLabelURL() throws IOException {
        // Yes Indeed!  Math.Random()!
        if (Math.random() > 0.5) {
            return getLabelDirURL();
        } else {
            return getLabelJarURL();
        }
    }

    public static final URL getLabelDirURL() throws IOException {
        return new File("src/test/resources/sample").getCanonicalFile().toURI().toURL();
    }

    public static final URL getLabelJarURL() throws IOException {
        return new URL("jar:" + new File("config/labels/sample.jar").getCanonicalFile().toURI() + "!/sample/");  // Assumes this test is run in the "base"
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        baseDir = getLabelURL();
        assert TrackingHandler.exists(baseDir);

    }

    protected URL getBaseDir() { return this.baseDir;}

    protected File getBaseCacheDir() throws IOException {
        return new File("target/ignore/these").getCanonicalFile();
    }

    protected GrammaticalLabelSetDescriptor getDescriptor() {
        return getDescriptor(LanguageProviderFactory.get().getBaseLanguage());
    }
    
    public static GrammaticalLabelSetDescriptor getSampleDescriptor(HumanLanguage language) throws IOException {
        return new LabelSetDescriptorImpl(getLabelJarURL(), language, LABEL_SET_NAME, LABELS_XML, NAMES_XML);
    }

    public static GrammaticalLabelSetDescriptor getSampleDescriptorDir(HumanLanguage language) throws IOException {
        return new LabelSetDescriptorImpl(getLabelDirURL(), language, LABEL_SET_NAME, LABELS_XML, NAMES_XML);
    }

    protected GrammaticalLabelSetDescriptor getDescriptor(HumanLanguage language) {
        return new LabelSetDescriptorImpl(baseDir, language, LABEL_SET_NAME, LABELS_XML, NAMES_XML);
    }

    protected GrammaticalLabelSetLoader getLoader() {
        return new GrammaticalLabelSetLoader(getDescriptor());
    }

    protected LanguageDictionary loadDictionary(HumanLanguage language) throws IOException {
        if (!dictionaryMap.containsKey(language)) {
            LanguageDictionaryParser parser = new LanguageDictionaryParser(getDescriptor(language), language, null);
            dictionaryMap.put(language, parser.getDictionary());
        }
        return dictionaryMap.get(language);
    }

    LanguageDictionary loadDictionaryNoThrow(HumanLanguage language) {
        try {
            return loadDictionary(language);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String renderLabel(HumanLanguage language, String text) throws IOException {
        return renderLabel(language, text, null);
    }

    /**
     * Render the label for the given language with the appropriate override
     */
    public String renderLabel(HumanLanguage language, String text, String grammarOverride) throws IOException {
        return getTestLabelSet(language, text, grammarOverride).getString("Test", "Test");
    }

    public String renderDynamicLabel(HumanLanguage language, String text, Renameable... entities) throws IOException {
        return getTestLabelSet(language, text, null).getString("Test", entities, "Test");
    }

    public GrammaticalLabelSet getTestLabelSet(HumanLanguage language, String text, String grammarOverride) throws IOException {
        TestLanguageLabelSetDescriptor testDesc = new TestLanguageLabelSetDescriptor(getDescriptor(language), LabelUtils.getSampleLabelFile(text), LabelUtils.getSampleGrammarFile(grammarOverride));

        return GrammaticalLabelSetImpl.getTestLabelSet(testDesc);
    }

    /**
     * Given a language, retrieves the labels associated with that language.
     * This method was created to be used to retrieve the label set for end-user languages,
     * but can also be used to retrieve the label set for fully-supported languages.
     * NOTE that the label set for fully-supported languages can also be retrieved from the
     * Localizer
     */
    protected GrammaticalLabelSet parseLabelsDirectly(HumanLanguage language) throws IOException {
        URL baseDir = getBaseDir();
        LabelSetDescriptorImpl desc = new LabelSetDescriptorImpl(baseDir, language, LABEL_SET_NAME);

        LanguageDictionary dictionary = loadDictionary(language);
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dictionary, desc, null, false);

        return new GrammaticalLabelSetImpl(loadDictionary(language), parser, new MapPropertyFileData(language.getLocale()));
    }

    /**
     * @param name the name of an existing noun
     * @return a Renameable entity for the given noun
     */
    public Renameable getStandardRenameable(String name) {
        return new MockExistingRenameable(name.toLowerCase(), loadDictionaryNoThrow(LanguageProviderFactory.get().getBaseLanguage()));
    }

    protected Renameable makeCustomRenameable(String name, LanguageStartsWith startsWith, String singular, String plural) {
        return new MockCustomRenameable(name, makeEnglishNoun(name, NounType.ENTITY, startsWith, singular, plural));
    }

    protected Noun makeEnglishNoun(String name, NounType type, LanguageStartsWith startsWith, String singular, String plural) {
        LanguageDeclension decl = LanguageDeclensionFactory.get().getDeclension(LanguageProviderFactory.get().getBaseLanguage());
        return decl.createNoun(name, type, null, startsWith, LanguageGender.NEUTER,
                ImmutableMap.of(decl.getNounForm(LanguageNumber.SINGULAR, LanguageArticle.ZERO), singular,
                        decl.getNounForm(LanguageNumber.PLURAL, LanguageArticle.ZERO), plural));
    }


    public String getValue(HumanLanguage language, String label, Renameable[] entities, String grammarSnippet, Object... vals) throws IOException {
        TestLanguageLabelSetDescriptor testDesc = new TestLanguageLabelSetDescriptor(getDescriptor(language), LabelUtils.getSampleLabelFile(label), LabelUtils.getSampleGrammarFile(grammarSnippet));
        GrammaticalLabelSet ls = GrammaticalLabelSetImpl.getTestLabelSet(testDesc);
        return new MessageFormat(ls.getString("Test", "Test", entities, true, vals), language.getLocale()).format(vals);
    }

    public void assertValue(HumanLanguage language, String label, Renameable[] entities, String expectedResult, Object... vals) throws IOException {
        String result = getValue(language, label, entities, "", vals);
        assertEquals("Mismatch for " + label, expectedResult, result);
    }

    public void assertValue(HumanLanguage language, String label, String expectedResult, Object... vals) throws IOException {
        assertValue(language, label, null, expectedResult, vals);
    }

    private static final String CUSTOM_OBJECT_PREFIX = "01N";  // Needed because we can't refer to the udd here
    /**
     * A mock renaming provider, where you provide it with a series of nouns, and it will
     * return those nouns as the "renamed" values when you specify it.
     *
     * Make sure you use this in a try finally
     *
     * <pre>
     *  RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
     *  try {
     *      MockRenamingProvider newProvider = new MockRenamingProvider(makeEnglishNoun("account", NounType.ENTITY, LanguageStartsWith.CONSONANT,
     *               "Client or Person", "Clients & People"));
     *  } finally {
     *      RenamingProviderFactory.get().setProvider(curProvider);
     *  }
     * </pre>
     *
     * @author stamm
     */
    public static class MockRenamingProvider implements RenamingProvider {
        private final Map<? extends HumanLanguage,Map<String,Noun>> nounMap;
        private boolean useRenamedNouns = true;

        public MockRenamingProvider(Map<? extends HumanLanguage,Map<String,Noun>> nounMap) {
            assert nounMap != null;
            this.nounMap = nounMap;
        }

        /**
         * Construct a mock renaming provider based on the given nouns
         * @param nouns the nouns to use for this mock provider
         */
        public MockRenamingProvider(Noun... nouns) {
            this(makeNounMap(nouns));
            assert nouns.length > 0;
        }


        /**
         * Convert a series of nouns into a noun map suitable for using in constructing a MockRenameable
         * @param nouns the test nouns
         * @return a noun map from the given nouns sorted by the language of those nouns
         */
        static Map<HumanLanguage,Map<String,Noun>> makeNounMap(Noun... nouns) {
            Map<HumanLanguage,Map<String,Noun>> result = LanguageProviderFactory.get().getNewMap();

            for (Noun noun : nouns) {
            	HumanLanguage language = noun.getDeclension().getLanguage();
                Map<String,Noun> map = result.get(language);
                if (map == null) {
                    map = new HashMap<String,Noun>(4);
                    result.put(language,map);
                }
                map.put(noun.getName().toLowerCase(), noun);
            }
            return result;
        }

        @Override
        public Noun getRenamedNoun(HumanLanguage language, String key) {
            Map<String,Noun> m = nounMap.get(language);
            return m != null ? m.get(key.toLowerCase()) : null;
        }

        @Override
        public Noun getPackagedNoun(HumanLanguage language, String key) {
            return null;  // This only applies to packaged entities
        }

        @Override
        public Noun getNoun(HumanLanguage language, Renameable key) {
            if (useRenamedNouns()) {
                Noun n = getRenamedNoun(language, key.getEntitySpecificDbLabelKey(Renameable.ENTITY_NAME));
                if (n != null) {
                    return n;
                }
            }
            return key.getStandardNoun(language);
        }

        @Override
        public boolean isCustomKey(String key) {
            return key.startsWith(CUSTOM_OBJECT_PREFIX);
        }

        @Override
        public boolean isRenamed(HumanLanguage language, String key) {
            Map<String,Noun> m = nounMap.get(language);
            return m != null && m.containsKey(key);
        }

        @Override
        public boolean useRenamedNouns() {
            return useRenamedNouns;
        }

        public void setUseRenamedNouns(boolean useRenamedNouns) {
            this.useRenamedNouns = useRenamedNouns;
        }

        @Override
        public boolean supportOldGrammarEngine() {
            return false;
        }

        @Override
        public boolean displayMiddleNameInCalculatedPersonName() {
            return false;
        }

        @Override
        public boolean displaySuffixInCalculatedPersonName() {
            return false;
        }
    }

    /**
     * Abstract renameable object.  Since custom objects and standard objects
     * are quite different; this handles the commonality in getEntitySpecificDbLabelKey.
     * @author stamm
     */
    public abstract static class MockRenameable implements Renameable {
        private final String name;

        public MockRenameable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEntitySpecificDbLabelKey(String labelKey) {
            boolean isEntity = Renameable.ENTITY_NAME.equalsIgnoreCase(labelKey) || labelKey.equals(name);
            if (name.startsWith(CUSTOM_OBJECT_PREFIX)) {
                // for custom entity, return as custom entity ID for <Entity> tag,
                // and ID_name format
                return isEntity ? name : name + "_" + labelKey;
            } else if (isEntity) {
                // for standard entity name, always returns as lower case - "account"
                return name.toLowerCase();
            }

            // if this is custom compound noun such as <Entity_Record_Type> for standard entity,
            // retranslate them to the right name: For Account, translate to the account_record_type
            String namePrefix = labelKey.toLowerCase();
            return namePrefix.startsWith(Renameable.ENTITY_NAME_PREFIX) ? name.toLowerCase() + "_"
                + namePrefix.substring(Renameable.ENTITY_NAME_PREFIX.length()) : namePrefix;
        }


        @Override
        public StandardField getRenameableFieldForKey(String labelKey) {
            if (Renameable.ENTITY_NAME_PREFIX.equals(labelKey)) {
                return MockStandardField.NAME;
            }
            return null;
        }

        @Override
        public boolean hasStandardLabel() {
            return !name.startsWith(CUSTOM_OBJECT_PREFIX);
        }
    }

    /**
     * Mock Custom Renameable Entity
     * @author stamm
     */
    public static class MockCustomRenameable extends MockRenameable {
        private final Noun baseNoun;
        private final Map<HumanLanguage,Noun> standardNouns;

        public MockCustomRenameable(String name, Noun englishNoun) {
            this(name, ImmutableMap.of(LanguageProviderFactory.get().getBaseLanguage(), englishNoun));
        }

        public MockCustomRenameable(String name, Map<HumanLanguage,Noun> standardNouns) {
            super(name);
            assert standardNouns != null && standardNouns.size() > 0 : "You must provide a standard noun";
            this.standardNouns = standardNouns;
            this.baseNoun = standardNouns.get(LanguageProviderFactory.get().getBaseLanguage());
            assert this.baseNoun != null : "You must provide a standard english noun";
        }

        @Override
        public String getLabel() {
            return baseNoun.getDefaultString(false);
        }

        @Override
        public String getLabelPlural() {
            return baseNoun.getDefaultString(true);
        }

        @Override
        public String getStandardFieldLabel(HumanLanguage language, StandardField field) {
            return baseNoun + ((MockStandardField)field).getKey();
        }

        @Override
        public Noun getStandardNoun(HumanLanguage language) {
            Noun n = standardNouns.get(language);
            return n != null ? n : baseNoun;
        }
    }

    /**
     * Mock Standard Renameable Entity
     * @author stamm
     */
    public static class MockExistingRenameable extends MockRenameable {
        private final Noun baseNoun;
        private final LanguageDictionary dict;
        public MockExistingRenameable(String name, LanguageDictionary dict) {
            super(name);
            this.dict = dict;
            baseNoun = dict.getNoun(name.toLowerCase(), false);
            assert this.baseNoun != null : "You must provide a standard english noun";
        }

        @Override
        public String getLabel() {
            return baseNoun.getDefaultString(false);
        }

        @Override
        public String getLabelPlural() {
            return baseNoun.getDefaultString(true);
        }

        @Override
        public String getStandardFieldLabel(HumanLanguage language, StandardField field) {
            return baseNoun + ((MockStandardField)field).getKey();
        }

        @Override
        public Noun getStandardNoun(HumanLanguage language) {
            if (language == LanguageProviderFactory.get().getBaseLanguage()) {
                return baseNoun;
            }
            Noun n = dict.getNoun(getName(), false);
            return n != null ? n : baseNoun;
        }
    }


    /**
     * The set of "standard" translateable fields
     * @author stamm
     */
    public enum MockStandardField implements StandardField {
        NAME(" Name"),
        FIRST_NAME(" FirstName"),
        LAST_NAME(" LastName"),
        ;
        private final String key;
        MockStandardField(String key) {
            this.key = key;
        }
        public String getKey() {
            return this.key;
        }

    }

}
