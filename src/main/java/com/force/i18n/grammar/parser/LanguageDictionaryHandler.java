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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.xml.sax.*;

import com.force.i18n.commons.text.Uniquefy;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.settings.TrackingHandler;
import com.google.common.collect.ImmutableSet;

/**
 * DictionaryParser class for the dictionary data file - sfdcnames.xml.
 * The syntax for the dictionary file is defined in /config/labels/app/sfdcnames.dtd.<br>
 *
 * @author YOikawa
 */
class LanguageDictionaryHandler extends TrackingHandler {
    private static final Logger logger = Logger.getLogger(LanguageDictionaryHandler.class.getName());
    // valid tag names
    static final String ROOT = "names";
    static final String ALT_ROOT = "sfdcnames";
    static final String ADJECTIVE_ROOT = "adjectives";
    static final String ALT_ADJECTIVE_ROOT = "sfdcadjectives";
    static final String NOUN = "noun";
    static final String ADJECTIVE = "adjective";
    static final String ARTICLE = "article";
    static final String VALUE = "value";
    static final String IMPORT = "import";

    static final ImmutableSet<String> ROOTS = ImmutableSet.of(ROOT, ALT_ROOT, ADJECTIVE_ROOT, ALT_ADJECTIVE_ROOT);

    // attributes
    static final String NAME = "name";
    static final String ENTITY = "entity";

    static final String FIELD = "field";
    static final String OTHER = "other";

    static final String ALIAS = "alias";
    static final String TYPE = "type";
    static final String GENDER = "gender";
    static final String STARTS = "startsWith";
    static final String ENDS = "endsWith";
    static final String POSITION = "position";
    static final String ACCESS = "access";
    static final String STANDARDFIELD = "standardField";
    static final String COUNTER = "counter";

    static final String VERSION = "version";
    static final String ATLEAST = "atLeast";


    static final String YES = "y";
    static final String NO = "n";

    private final LanguageDictionaryParser parser;
    private final URL baseDir;

    private BaseTag currentTag;

    Uniquefy uniquefy = new Uniquefy();

    /**
     * package private ctor
     */
    LanguageDictionaryHandler(URL file, LanguageDictionaryParser parser) {
        super(file);
        this.parser = parser;
        this.currentTag = null;

        // Directory from which we're loading data.
        this.baseDir = file;
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentTag != null)
            this.currentTag.characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (this.currentTag != null) {
            this.currentTag.startElement(uri, localName, qName, attributes);

        } else if (ROOTS.contains(localName)) {
            this.currentTag = new RootTag();
        } else if (IMPORT.equals(localName)) {
            // TODO SLT: There's something fishy with the parser, with the currentTag getting hosed if you import twice
            try {
                parser.parseDictionary(new URL(baseDir, attributes.getValue(0)));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new SAXNotSupportedException("bad tag:" + localName);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.currentTag != null) {
            this.currentTag.endElement();
            this.currentTag = this.currentTag.getParent();
        }
    }

    //============================================================================
    // Tag handler

    abstract class BaseTag {
        private final BaseTag parent;

        final BaseTag getParent() {
            return parent;
        }

        // implicit constructor for subclasses
        BaseTag() {
            this.parent = null;
        }

        /**
         * Construct the tag
         * @param parent the parent of the tag
         */
        BaseTag(BaseTag parent, Attributes atts) {
            this.parent = parent;
        }

        void characters(char[] ch, int start, int length) {}

        /**
         * Start the next element by passing through the SAX event
         * @throws SAXException if there's an exception
         */
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {}

        /**
         * End the last element
         */
        void endElement() {}

        /**
         * Set the value of the tag
         * @param value the value of the parameter
         * @param attrs the parsed attributes associated with the term
         */
        void setString(String value, TermAttributes attrs) {}
    }

    /**
     * The root tag - &lt;iniFile&gt;. The root tag can only contain &lt;noun&gt;,
     * &lt;adjective&gt; or &lt;import&gt; tag
     */
    class RootTag extends BaseTag {
        // Root only accepts either <noun>, <adjective> or <import>
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (NOUN.equals(localName)) {
                currentTag = new NounTag(this, attributes);

            } else if (ADJECTIVE.equals(localName)) {
                currentTag = new AdjectiveTag(this, attributes);

            } else if (ARTICLE.equals(localName)) {
                currentTag = new ArticleTag(this, attributes);

            } else if (IMPORT.equals(localName)) {
                try {
                    parser.parseDictionary(new URL(baseDir, attributes.getValue(0)));
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }

            } else {
                throw new SAXNotSupportedException("bad tag:" + localName);
            }
        }
    }

    /**
     * &lt;value&gt; tag implementation
     */
    class ValueTag extends BaseTag {
        private TermAttributes attribute;
        private StringBuilder sb = new StringBuilder();

        ValueTag(BaseTag parent, Attributes atts) {
            super(parent, atts);
            this.attribute = new TermAttributes(parser.getDictionary().getDeclension(), atts);
        }

        @Override
        void characters(char[] ch, int start, int length) {
            this.sb.append(ch, start, length);
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // no nested tags are allowed
            throw new SAXNotSupportedException("bad tag:" + localName);
        }

        @Override
        void endElement() {
            assert getParent() != null;
            getParent().setString(intern(this.sb.toString()), attribute);
        }
    }

    /**
     * &lt;noun&gt; tag implementation
     */
    class NounTag extends BaseTag {
        private String name;
        private Noun n;

        NounTag(RootTag parent, Attributes atts) {
            super(parent, atts);

            // always store as lower case
            this.name = intern(atts.getValue(NAME).toLowerCase());
            NounType type = NounType.getByApiValue(atts.getValue(TYPE));

            String tableEnum = intern(atts.getValue(ENTITY));
            String access = atts.getValue(ACCESS);
            String alias = uniquefy.unique(atts.getValue(ALIAS)); // use local uniquefy 'cause it's temporally
            LanguageStartsWith starts = LanguageStartsWith.fromDbValue(atts.getValue(ENDS) != null ? atts.getValue(ENDS) : atts.getValue(STARTS));
            LanguageGender gen = LanguageGender.fromLabelValue(atts.getValue(GENDER));
            boolean isStandardField = (atts.getValue(STANDARDFIELD) == null ? true : atts
                .getValue(STANDARDFIELD).equals(YES)); // default is YES


            this.n = parser.getDictionary().getOrCreateNoun(tableEnum, name, alias, type, gen, starts, access, isStandardField);
            // We're reparsing the same noun.  Create it instead
            if (parser.hasParentDictionarySameLang() && this.n == parser.getParentDictionary().getNoun(name, false)) {
                this.n = this.n.clone(gen, starts);
            }
            String classifier = intern(atts.getValue(COUNTER));
            if (classifier != null) {
                if (n instanceof Noun.WithClassifier) {
                    ((Noun.WithClassifier)n).setClassifier(classifier);
                } else {
                    logger.fine("Attempting to set a counter word for a language without classifiers for noun " + this.name + " for " + parser.getDictionary().getLanguage());
                }
            }
        }

        Noun getNoun() {
            return this.n;
        }

        Noun cloneNoun(LanguageGender genderOverride, LanguageStartsWith startsWithOverride) {
            return this.n.clone(genderOverride, startsWithOverride);
        }

        // Noun only accepts <value> and <version> tag
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (VERSION.equals(localName)) {
                currentTag = new NounVersionOverrideTag(this, attributes);
                return;
            }
            if (!VALUE.equals(localName))
                throw new SAXNotSupportedException("bad tag:" + localName);

            currentTag = new ValueTag(this, attributes);
        }

        @Override
        void endElement() {
            assert getParent() != null;
            // do not validate here. Loader will take care of entire validation path
            parser.getDictionary().put(this.name, this.n);
        }

        @Override
        void setString(String value, TermAttributes attrs) {
            NounForm form = attrs.getExactNounForm();
            if (form != null) {
                parser.getDictionary().setString(this.n, form, value);
            } else {
                logger.fine("Attempting to set '" + value + "' with invalid noun form " + attrs + " for " + parser.getDictionary().getLanguage());
            }
        }
    }

    /**
     * &lt;noun&gt; tag implementation
     */
    class NounVersionOverrideTag extends BaseTag {
        private double version;
        private Noun parentNoun;
        private Noun override;

        NounVersionOverrideTag(NounTag parent, Attributes atts) {
            super(parent, atts);

            this.version = Double.parseDouble(atts.getValue(ATLEAST));

            LanguageStartsWith starts = LanguageStartsWith.fromDbValue(atts.getValue(ENDS) != null ? atts.getValue(ENDS) : atts.getValue(STARTS));
            LanguageGender gen = LanguageGender.fromLabelValue(atts.getValue(GENDER));
            this.parentNoun = parent.getNoun();
            this.override = parent.cloneNoun(gen, starts);
        }

        // Version only accepts <value> tag
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!VALUE.equals(localName))
                throw new SAXNotSupportedException("bad tag:" + localName);

            currentTag = new ValueTag(this, attributes);
        }

        @Override
        void endElement() {
            assert getParent() != null;
            parser.getDictionary().setNounOverride(parentNoun, override, version);
        }

        @Override
        void setString(String value, TermAttributes attrs) {
            NounForm form = attrs.getExactNounForm();
            if (form != null) {
                parser.getDictionary().setString(this.override, form, value);
            } else {
                logger.fine("Attempting to set override '" + value + "' with invalid noun form " + attrs + " for " + parser.getDictionary().getLanguage());
            }
        }
    }


    /**
     * &lt;adjective&gt; tag implementation
     */
    class AdjectiveTag extends BaseTag {
        private String name;
        private Adjective mod;

        AdjectiveTag(RootTag parent, Attributes atts) {
            super(parent, atts);

            // always store as lower case
            this.name = intern(atts.getValue(NAME).toLowerCase());
            LanguageStartsWith starts = LanguageStartsWith.fromDbValue(atts.getValue(ENDS) != null ? atts.getValue(ENDS) : atts.getValue(STARTS));
            if (starts == null) starts = parser.getDictionary().getDeclension().getDefaultStartsWith();
            LanguagePosition position = LanguagePosition.fromDbValue(atts.getValue(POSITION));
            if (position == null) position = parser.getDictionary().getDeclension().getDefaultAdjectivePosition();
            this.mod = parser.getDictionary().getOrCreateAdjective(name, starts, position);
            // We're reparsing the same noun.  Create it instead
            if (parser.hasParentDictionarySameLang() && this.mod == parser.getParentDictionary().getAdjective(name)) {
            	this.mod = parser.getDictionary().createAdjective(name, starts, position, false);
            }
        }

        @Override
        void setString(String value, TermAttributes attrs) {
            AdjectiveForm form = attrs.getAdjectiveForm();
            if (form != null) {
                parser.getDictionary().setString(this.mod, form, value);
            } else {
                logger.fine("Attempting to set '" + value + "' with invalid adjective form " + attrs + " for " + parser.getDictionary().getLanguage());
            }
        }

        // Adjective only accepts <value> tag
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!VALUE.equals(localName))
                throw new SAXNotSupportedException("bad tag:" + localName);

            currentTag = new ValueTag(this, attributes);
        }

        @Override
        void endElement() {
            assert getParent() != null;
            // do not validate here. Loader will take care of entire validation path
            parser.getDictionary().put(this.name, this.mod);
        }
    }

    /**
     * &lt;article&gt; tag implementation
     */
    class ArticleTag extends BaseTag {
        private String name;
        private Article art;

        ArticleTag(RootTag parent, Attributes atts) {
            super(parent, atts);

            // always store as lower case
            this.name = intern(atts.getValue(NAME).toLowerCase());
            LanguageArticle articleType = LanguageArticle.fromLabelValue(atts.getValue(TYPE));
            this.art = parser.getDictionary().getOrCreateArticle(name, articleType);
            // We're reparsing the same noun.  Create it instead
            if (parser.hasParentDictionarySameLang() && this.art == parser.getParentDictionary().getArticle(name)) {
                this.art = parser.getDictionary().createArticle(name, articleType, false);
            }
        }

        @Override
        void setString(String value, TermAttributes attrs) {
            ArticleForm form = attrs.getArticleForm();
            if (form != null) {
                parser.getDictionary().setString(this.art, form, value);
            } else {
                logger.fine("Attempting to set '" + value + "' with invalid article form " + attrs + " for " + parser.getDictionary().getLanguage());
            }

        }

        // Article only accepts <value> tag
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!VALUE.equals(localName))
                throw new SAXNotSupportedException("bad tag:" + localName);

            currentTag = new ValueTag(this, attributes);
        }

        @Override
        void endElement() {
            assert getParent() != null;
            // do not validate here. Loader will take care of entire validation path
            parser.getDictionary().put(this.name, this.art);
        }
    }
}
