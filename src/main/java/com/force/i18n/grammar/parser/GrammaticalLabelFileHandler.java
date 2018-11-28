/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.*;

import com.force.i18n.*;
import com.force.i18n.commons.text.DeferredStringBuilder;
import com.force.i18n.grammar.*;
import com.force.i18n.settings.PropertyFileData;
import com.force.i18n.settings.TrackingHandler;
import com.google.common.collect.ImmutableSet;

/**
 * @author stamm
 */
class GrammaticalLabelFileHandler extends TrackingHandler {
    private static final Logger logger = Logger.getLogger(GrammaticalLabelFileHandler.class.getName());

    private static final boolean LOG_DUPLICATE_LABELS = "true".equals(I18nJavaUtil.getProperty("logDuplicateLabels"));  // Don't log duplicate as a matter of course because they are often duplicated to ease translation

    private final GrammaticalLabelFileParser parser;
    private final URL baseDir;
    private final PropertyFileData data;

    private BaseTag currentTag = null;
    private BaseTag currentParam = null;
    private SectionTag currentSection = null;
    private boolean isSectionPublic = false;


    /**
     * Package protected constructor used by the LabelParser.
     *
     * @param dataFile XML file this handler is reading.
     * @param parser   parser that is using this handler and thus the overall parse context.
     */
    GrammaticalLabelFileHandler(URL dataFile, PropertyFileData data, GrammaticalLabelFileParser parser) {
        super(dataFile);
        assert dataFile != null && data != null && parser != null;
        this.data = data;
        this.parser = parser;
        // Directory from which we're loading data.
        this.baseDir = dataFile;

        if (LabelDebug.isLabelHintAllowed()) {
            BASE_FILE = dataFile.getPath();
        }
    }

    final Level getProblemLogLevel() {
        return LanguageProviderFactory.get().getBaseLanguage() == parser.getDictionary().getLanguage() ? Level.INFO : Level.FINE; // TODO: This should turn into INFO;
    }

    final Level getSevereProblemLogLevel() {
        return LanguageProviderFactory.get().getBaseLanguage() == parser.getDictionary().getLanguage() ? Level.SEVERE : Level.FINE; // TODO: This should turn into INFO for other langs;
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
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (this.currentTag != null) {
            this.currentTag.startElement(uri, localName, qName, attributes);
        } else if (ROOT.equals(localName)) {
            this.currentTag = new RootTag();
///CLOVER:OFF
        } else {
            throw new SAXNotSupportedException("bad tag:" + localName);
///CLOVER:ON
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.currentTag != null) {
            if (localName.equals(this.currentTag.getTagName())) {
                this.currentTag.endElement();
                this.currentTag = this.currentTag.getParent();
///CLOVER:OFF
            } else {
                logger.log(getProblemLogLevel(), "###\tBad end tag <" + localName + "> found, ignored: " + getLineNumberString());
///CLOVER:ON
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Inner classes for each tag implementation
    //
    // -----------------------------------------------------------------------------
    // tag name
    static final String ROOT = "iniFile";
    static final String SECTION = "section";
    static final String PUBLIC = "public";
    static final String PARAM = "param";
    static final String IMPORT = "import";
    static final String IMAGE = "image";
    static final String ENTITY = "entity";
    static final String MOD = "modifier";
    static final String ESCAPE_HTML = "escapeHtml";

    // attributes
    static final String NAME = "name";
    static final String ALIAS = "alias";
    static final String ERROR = "error";
    static final String TYPE = "type";

    static final String YES = "y";
    static final String NO = "n";

    GrammaticalLabelFileParser getParser() {
        return this.parser;
    }

    LanguageDictionary getDictionary() {
        return getParser().getDictionary();
    }

    /**
     * The base class for each tag implementation.
     */
    abstract class BaseTag {
        private BaseTag parent = null;
        private String name;

        BaseTag() {
        }

        BaseTag(BaseTag parent, Attributes atts) throws SAXParseException {
            this.parent = parent;
            if (atts != null) {
                this.name = getParser().uniquefy(atts.getValue(NAME));
///CLOVER:OFF
                if (this.name == null && isNameRequired())
                    throw new SAXParseException("Missing required attribuite:" + NAME, getLocator());
///CLOVER:ON
            }
        }

        final BaseTag getParent() {
            return this.parent;
        }

        abstract String getTagName();

        final String getName() {
            return this.name;
        }

        /**
         * Pass through the SAX event for characters
         */
        void characters(char[] ch, int start, int length) {
        }

        /**
         * Pass through the SAX event for starting the element
         */
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        }

        /**
         * Pass through the SAX event for ending the last element
         */
        void endElement() {
        }

        /**
         * @return whether the name attribute is required for this tag
         */
        boolean isNameRequired() {
            return false;
        }
    }

    class BadTag extends BaseTag {
        private final String tagName;

        BadTag(BaseTag parent, String localName) throws SAXParseException {
            super(parent, null);
            this.tagName = localName;
            logger.log(getProblemLogLevel(), "###\tBad tag <" + localName + "> found, ignored: " + getLineNumberString());
        }

        @Override
        String getTagName() {
            return this.tagName;
        }
    }

    /**
     * The root tag - &lt;iniFile&gt;. The root tag can only contain &lt;section&gt; or &lt;import&gt; tag
     */
    private class RootTag extends BaseTag {
        @Override
        String getTagName() {
            return ROOT;
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (SECTION.equals(localName)) {
                currentTag = new SectionTag(this, attributes);

            } else if (IMPORT.equals(localName)) {
                try {
                    currentTag = new ImportTag(this, attributes);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }

            } else {
                currentTag = new BadTag(this, localName);
            }
        }
    }

    private class ImportTag extends BaseTag {
        @Override
        String getTagName() {
            return IMPORT;
        }

        ImportTag(RootTag parent, Attributes atts) throws SAXParseException, MalformedURLException {
            super(parent, atts);

            // Fire up the new parser and populate the inifile with the retrieved data.
            getParser().parseLabels(GrammaticalLabelFileHandler.this.data, new URL(baseDir, atts.getValue(0)));
        }
    }

    /**
     * The section tag - &lt;section&gt;. The section tag contains either &lt;param&gt; or &lt;img&gt; tag
     */
    private class SectionTag extends BaseTag {
        @Override
        String getTagName() {
            return SECTION;
        }

        SectionTag(RootTag parent, Attributes atts) throws SAXParseException {
            super(parent, atts);

            if (LabelDebug.isLabelHintAllowed()) {
                SECTION_TO_FILENAME.put(getName(), BASE_FILE);
            }

            currentSection = this;
            isSectionPublic = "true".equalsIgnoreCase(atts.getValue(PUBLIC));
            if (isSectionPublic) {
                GrammaticalLabelFileHandler.this.data.setSectionAsPublic(this.getName());
            }
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (PARAM.equals(localName)) {
                currentTag = new ParamTag(this, attributes);
            } else {
                currentTag = new BadTag(this, localName);
            }
        }

        @Override
        void endElement() {
            currentSection = null;
            isSectionPublic = false;
        }

        @Override
        boolean isNameRequired() {
            return true;
        }

        void addLabelData(String name, Object data) {
///CLOVER:OFF
            if (LOG_DUPLICATE_LABELS) {
                Object existingValue = GrammaticalLabelFileHandler.this.data.put(getName(), name, data);
                if (existingValue != null) { // TODO: Chinese is stored with a zh_TW being the default
                    HumanLanguage lang = LanguageProviderFactory.get().getProvider().getLanguage(GrammaticalLabelFileHandler.this.data.getLocale());
                    // TODO SLT: fix this.
                    if (!ImmutableSet.of(LanguageConstants.CHINESE_TW, LanguageConstants.SPANISH_MX).contains(lang.getLocaleString())) {
                        logger.log(getProblemLogLevel(), "Redundant label found: " + name + " in section: " + getName()
                                + ", Existing Value: " + existingValue);
                    }
                }
///CLOVER:ON
            } else {
                // Java compiler doesn't optimize away a dead local store if the user of the variable is inside a dead-code optimized block.
                GrammaticalLabelFileHandler.this.data.put(getName(), name, data);
            }
        }
    }

    /**
     * The param tag - &lt;param name="param1"&gt;. The param tag takes three attributes
     * <ul>
     * <li>name: parameter name. this is always required.
     * <li>alias: Use <i>alias</i> to make this param refers to the other label. e.g. &lt;param name="val1"
     * alias="sec1.val1"/&gt;
     * <li>entity: specify default entity name. This is used if modifier tag has no <i>entity</i> attribute specified,
     * and if there is no nouns defined.
     * </ul>
     */
    private class ParamTag extends BaseTag {
        private boolean isAlias;
        private StringBuilder sb;
        private final ArrayList<Object> values; // the type is specific (eg not List<>) to allow cloning

        @Override
        String getTagName() {
            return PARAM;
        }

        ParamTag(SectionTag parent, Attributes atts) throws SAXParseException {
            super(parent, atts);
            this.values = new ArrayList<Object>();
            this.isAlias = false;

            currentParam = this;

            // if alias is specified, ignore all remaining attributes/contents
            String alias = atts.getValue(ALIAS);
            if (alias != null) {
                int i = alias.indexOf('.');
                if (i <= 0)
                    logger.log(getProblemLogLevel(), "###\tBad alias name " + alias + " at " + parent.getName() + "." + getName() + " in " + getDictionary().getLanguage());
                else {
                    this.isAlias = true;
                    getParser().addAlias(parent.getName(), getName(), alias.substring(0, i),
                            alias.substring(i + 1), getFile(), getLocator().getLineNumber());
                }
            }

            if (!this.isAlias) {
                // make sure we no longer have alias on it. This is for the case that english label
                // has this param as an alias, but overriding file has not.
                getParser().removeAlias(parent.getName(), getName());
            }
        }

        private void addBufferedText() {
            String s = convertEscapedToUnicode(this.sb.toString());
            // If we ever wanted to keep large labels on disk/out of memory,
            // this would be great place to inject something. Perhaps storing
            // in sleepycat or somesuch thing.
            s = getParser().uniquefy(s);
            this.values.add(s);
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // if this is alias specified, ignore any internal tag
            if (this.isAlias)
                return;

            // push buffered text into array
            if (this.sb != null && this.sb.length() > 0) {
                addBufferedText();
                this.sb = null;
            }

            String lowerName = localName.toLowerCase();
            // <entity/> or <Entity/>
            if (ENTITY.equals(lowerName)) {
                currentTag = new NounTag(this, attributes, localName, true);
            } else if (getParser().getDictionary().isAdjective(lowerName)) {
                currentTag = new AdjTag(this, attributes, localName);
            } else if (getParser().getDictionary().isArticle(lowerName)) {
                currentTag = new ArtTag(this, attributes, localName);
            } else {
                currentTag = new NounTag(this, attributes, localName, false);
            }
        }

        @Override
        void characters(char[] ch, int start, int length) {
            if (!isAlias) {
                if (this.sb == null)
                    this.sb = new StringBuilder();
                this.sb.append(ch, start, length);
            }
        }

        @Override
        void endElement() {
            if (this.isAlias) {
                // register to name only - leave as null at this point
                ((SectionTag) getParent()).addLabelData(getName(), null);

            } else {
                // push any remaining string
                if (this.sb != null && this.sb.length() > 0) {
                    if (getParser().trackDupes()) {
                        getParser().trackLabel(this.sb.toString(), getFile().getPath() + ":" + ((SectionTag) getParent()).getName() + "." + getName());
                    }
                    addBufferedText();
                }

                if (values.isEmpty()) {
                    ((SectionTag) getParent()).addLabelData(getName(), "");

                } else {

                    if (this.values.size() == 1) {
                        ((SectionTag) getParent()).addLabelData(getName(), this.values.get(0));
                    } else {
                        List<Object> data;
                        data = finishParsingLabelsNew(this.values);
                        ((SectionTag) getParent()).addLabelData(getName(), data);
                    }
                }
            }
            currentParam = null;
        }

        protected List<Object> finishParsingLabelsNew(List<Object> values) {
            // The goal here is to determine noun phrases correctly.  We do that by assuming that noun phrases are of the following form
            // [article] [adjective]+ noun [adjective]+

            // We need to:
            // Fix up the articles based on the noun and the next term
            // Fix up the adjective based on the noun, the next term, and the article (whether from an article tag or from the noun)
            // Possibly fix up the noun based on the article tag for those languages that have a distinct article particle and a distinct form based on definitiveness

            // So there's a two pass algorithm.  In the first, we figure out the "noun" phrases
            List<NounPhrase> phrases = new ArrayList<NounPhrase>();
            NounPhrase curPhrase = new NounPhrase(values);
            for (int i = 0; i < values.size(); i++) {
                Object o = values.get(i);
                if (o instanceof ArticleRefTag) {
                    if (curPhrase.isNounSet()) {
                        curPhrase = new NounPhrase(values);  // Demarcate as a new phrase
                    }
                    curPhrase.setArticleLoc(i);
                } else if (o instanceof NounRefTag) {
                    if (curPhrase.isNounSet()) {
                        curPhrase = new NounPhrase(values);  // New noun phrase
                    }
                    phrases.add(curPhrase);  // Once there's a noun, there's a noun phrase
                    curPhrase.setNounLoc(i);
                } else if (o instanceof AdjectiveRefTag) {
                    AdjectiveRefTag refTag = (AdjectiveRefTag) o;
                    Adjective adj = getDictionary().getAdjective(refTag.getName());
                    LanguagePosition pos = refTag.getDeclensionOverrides().getPosition();  // Allow for position overrides in case it's "different"
                    if (pos == null) pos = adj.getPosition();
                    if (pos == LanguagePosition.PRE && curPhrase.isNounSet()) {
                        curPhrase = new NounPhrase(values);  // Demarcate as a new phrase
                    }
                    if (adj.isCopiedFromDefault()) {
                        logger.log(getSevereProblemLogLevel(), "Adjective copied from english for " + getFile().getPath() + ":" + ((SectionTag) getParent()).getName() + "." + getName());
                    }

                    // add to the last noun phrase
                    curPhrase.addAdjectiveLoc(i);
                }
            }
            if (!curPhrase.isNounSet()) {
                if (phrases.size() == 0) {
                    logger.log(getSevereProblemLogLevel(), "Adjective used without a noun for " + getFile().getPath() + ":" + ((SectionTag) getParent()).getName() + "." + getName());
                    return values;  // Don't deal
                } else {
                    // Chances are we have a dangling adjective (like <Tasks/> (<Open/>))
                    if (phrases.size() > 1) {
                        // Log only if there's an actual issue (multiple nouns)
                        logger.log(getSevereProblemLogLevel(), "Prepositional Adjective used in post-position for " + getFile().getPath() + ":" + ((SectionTag) getParent()).getName() + "." + getName());
                    }
                    // Add the adjectives to the last noun phrase and keep going.
                    NounPhrase lastPhrase = phrases.get(phrases.size() - 1);
                    for (Integer i : curPhrase.getAdjectiveLocs()) {
                        lastPhrase.addAdjectiveLoc(i);
                    }
                }
            }

            for (NounPhrase phrase : phrases) {
                Object o = values.get(phrase.nounLoc);
                assert o instanceof NounRefTag : "Invalid noun location " + phrase.nounLoc + " in " + phrase;
                NounRefTag nounRef = phrase.getNounRefTag();
                ArticleRefTag articleRef = phrase.getArticleRefTag();

                // Handle the article
                if (articleRef != null) {
                    // Fix up the article of the noun based on the particle if it's interesting.
                    if (getDictionary().getDeclension().shouldInferNounDefArticleFromParticle()) {
                        LanguageArticle articleType = ((Article) articleRef.resolveModifier(getDictionary())).getArticleType();
                        // Update the noun with the article
                        if (articleType == LanguageArticle.DEFINITE) {
                            nounRef = nounRef.makeArticled(getDictionary(), articleType);
                            values.set(phrase.getNounLoc(), nounRef);
                        }
                    }
                    // Figure out the "next thing"
                    if (phrase.hasAdjectives()) {
                        int nextAdjective = phrase.getAdjectiveLoc(0);
                        values.set(phrase.getArticleLoc(), articleRef.fixupModifier(nounRef, nextAdjective < phrase.getNounLoc() ? (TermRefTag) values.get(nextAdjective) : nounRef));
                    } else {
                        values.set(phrase.getArticleLoc(), articleRef.fixupModifier(nounRef, nounRef));
                    }
                }

                // Handle special case for bulgarian where all noun inflections should move to the first modifier automatically if there is a modifier
                // Modify the noun to have no inflection, and move the modifiers back to the adjective ref tag.
                if (getDictionary().getDeclension().moveNounInflectionToFirstModifier() && phrase.hasAdjectives()) {
                    NounRefTag nounTag = phrase.getNounRefTag();
                    NounForm nounForm = nounTag.getForm();
                    AdjectiveRefTag firstAdjectiveTag = (AdjectiveRefTag) values.get(phrase.getAdjectiveLoc(0));


                    if (nounForm.getArticle() != LanguageArticle.ZERO || nounForm.getCase() != LanguageCase.NOMINATIVE) {
                        nounRef = nounTag.makeUninflected(getDictionary());
                        values.set(phrase.getNounLoc(), nounRef);
                        values.set(phrase.getAdjectiveLoc(0), firstAdjectiveTag.fixupModifier(nounRef, nounRef, nounForm));
                    }
                }

                // Now go through the adjectives.
                LanguageArticle articleOverride = articleRef != null ? getDictionary().getArticle(articleRef.getName()).getArticleType() : null;
                for (int i = 0; i < phrase.getAdjectiveLocs().size(); i++) {
                    AdjectiveRefTag adjTag = (AdjectiveRefTag) values.get(phrase.getAdjectiveLoc(i));
                    // Figure out the "next" object
                    if (i < phrase.getAdjectiveLocs().size() - 1) {
                        int nextAdjective = phrase.getAdjectiveLoc(i + 1);
                        values.set(phrase.getAdjectiveLoc(i), adjTag.fixupModifier(nounRef, nextAdjective < phrase.getNounLoc() ? (TermRefTag) values.get(nextAdjective) : nounRef, articleOverride));
                    } else {
                        values.set(phrase.getAdjectiveLoc(i), adjTag.fixupModifier(nounRef, nounRef, articleOverride));
                    }
                }
            }
            return values;
        }


        @Override
        boolean isNameRequired() {
            return true;
        }

        void addTag(TermRefTag tag) {
            values.add(tag);
        }

    }

    /**
     * Represents the locations of various elements of a noun phrase in a label.
     *
     * @author stamm
     */
    static class NounPhrase {
        private final List<Object> values;
        private int nounLoc = -1;
        private int articleLoc = -1;
        private List<Integer> adjectiveLocs = new ArrayList<Integer>();

        public NounPhrase(List<Object> values) {
            this.values = values;
        }

        public NounRefTag getNounRefTag() {
            return nounLoc == -1 ? null : (NounRefTag) values.get(nounLoc);
        }

        public ArticleRefTag getArticleRefTag() {
            return articleLoc == -1 ? null : (ArticleRefTag) values.get(articleLoc);
        }

        public void setNounLoc(int i) {
            this.nounLoc = i;
            assert values.get(i) instanceof NounRefTag : "Illegal noun loc";
        }

        public void setArticleLoc(int i) {
            this.articleLoc = i;
            assert values.get(i) instanceof ArticleRefTag : "Illegal article loc";
        }

        public int getArticleLoc() {
            return this.articleLoc;
        }

        public int getNounLoc() {
            return this.nounLoc;
        }

        public void addAdjectiveLoc(int i) {
            this.adjectiveLocs.add(i);
            assert values.get(i) instanceof AdjectiveRefTag : "Illegal adjective loc";
        }

        public List<Integer> getAdjectiveLocs() {
            return this.adjectiveLocs;
        }

        public int getAdjectiveLoc(int offset) {
            return this.adjectiveLocs.get(offset);
        }

        public boolean isNounSet() {
            return nounLoc != -1;
        }

        public boolean hasAdjectives() {
            return this.adjectiveLocs.size() > 0;
        }

        public int getPhraseStart() {
            if (articleLoc != -1) return articleLoc;
            if (adjectiveLocs.size() == 0) return nounLoc;
            return Math.min(adjectiveLocs.get(0), nounLoc);
        }

        public int getPhraseEnd() {
            if (adjectiveLocs.size() == 0) return nounLoc;
            return Math.max(adjectiveLocs.get(adjectiveLocs.size() - 1), nounLoc);
        }

        @Override
        public String toString() {
            return values.subList(getPhraseStart(), getPhraseEnd() + 1).toString();
        }
    }


    /**
     * Entity or any noun tag - e.g. &lt;Accounts/&gt;, &lt;Account_Name/&gt;, &lt;Entity ref="0"/&gt;
     */
    private class NounTag extends BaseTag {
        private final NounRefTag nounTag;
        private final String tagName;

        @Override
        String getTagName() {
            return this.tagName;
        }

        NounTag(ParamTag parent, Attributes atts, String localName, boolean isEntityTag) throws SAXParseException {
            super(parent, atts);
            this.tagName = localName;

            String entityName = isEntityTag ? atts.getValue(ENTITY) : localName;
            String entityAttr = isEntityTag ? localName : null;

            this.nounTag = constructNounTag(entityName, atts, entityAttr);
            if (this.nounTag == null) {
                logger.log(getProblemLogLevel(), "###\tUnknown entity <" + entityName + "> at " + currentSection.getName() + "."
                        + parent.getName());
                parser.addInvalidLabel(currentSection.getName(), currentParam.getName());
            }
        }

        ///CLOVER:OFF
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // no inner tag is allowed
            currentTag = new BadTag(this, localName);
        }
///CLOVER:ON

        @Override
        void endElement() {
            if (this.nounTag != null)
                ((ParamTag) getParent()).addTag(this.nounTag);
        }
    }

    /**
     * Adjective tag. &lt;New entity="Accounts"/&gt;
     */
    class AdjTag extends BaseTag {
        AdjectiveRefTag modTag;
        private final String tagName;

        @Override
        String getTagName() {
            return this.tagName;
        }

        AdjTag(ParamTag parent, Attributes atts, String localName) throws SAXParseException {
            super(parent, atts);
            this.tagName = localName;

            String lowerName = tagName.toLowerCase();

            // handle <new entity="0"/> case
            String entityName = atts.getValue(ENTITY);
            NounRefTag refTag = constructNounTag(entityName, atts, entityName == null
                    || !Character.isDigit(entityName.charAt(0)) ? null : entityName);

            TermAttributes attributes = new TermAttributes(getDictionary().getDeclension(), atts, false);
            this.modTag = AdjectiveRefTag.getAdjectiveRefTag(lowerName, refTag, refTag,
                    Character.isUpperCase(tagName.charAt(0)), attributes);
        }

        ///CLOVER:OFF
        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // no inner tag is allowed
            currentTag = new BadTag(this, localName);
        }
///CLOVER:ON

        @Override
        void endElement() {
            if (this.modTag != null)
                ((ParamTag) getParent()).addTag(this.modTag);
        }
    }

    /**
     * Adjective tag. &lt;New entity="Accounts"/&gt;
     */
    class ArtTag extends BaseTag {
        ArticleRefTag modTag;
        private final String tagName;

        @Override
        String getTagName() {
            return this.tagName;
        }

        ArtTag(ParamTag parent, Attributes atts, String localName) throws SAXParseException {
            super(parent, atts);
            this.tagName = localName;

            String lowerName = tagName.toLowerCase();

            // handle <new entity="0"/> case
            String entityName = atts.getValue(ENTITY);
            NounRefTag refTag = constructNounTag(entityName, atts, entityName == null
                    || !Character.isDigit(entityName.charAt(0)) ? null : entityName);

            TermAttributes attributes = new TermAttributes(getDictionary().getDeclension(), atts, false);
            this.modTag = ArticleRefTag.getArticleRefTag(lowerName, refTag, refTag,
                    Character.isUpperCase(tagName.charAt(0)), attributes);
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // no inner tag is allowed
            currentTag = new BadTag(this, localName);
        }

        @Override
        void endElement() {
            if (this.modTag != null)
                ((ParamTag) getParent()).addTag(this.modTag);
        }
    }

    NounRefTag constructNounTag(String entityName, Attributes atts, String entityElement) {
        boolean isCapital = false;

        Integer ref = null;
        TermAttributes ta = new TermAttributes(getDictionary().getDeclension(), atts);

        String realEntityName = entityName;
        // entityName could be null if this called for constructing ModTag
        if (entityElement != null) {
            isCapital = Character.isUpperCase(entityElement.charAt(0));
            realEntityName = ENTITY;

        } else if (entityName != null) {
            isCapital = Character.isUpperCase(realEntityName.charAt(0));
        }

        String escapeHtmlStr = atts.getValue(ESCAPE_HTML);
        boolean escapeHtml = "true".equals(escapeHtmlStr) || "y".equals(escapeHtmlStr);

        if (entityName != null) {
            String lowerName = realEntityName.toLowerCase();

            // Try and get the noun
            Noun n = getParser().getDictionary().getNoun(lowerName, false);

            // Get the form for the noun.
            NounForm nid = ta.getExactNounForm();
            if (nid == null) {
                // Log a different error for articles vs non articles
                if (ta.getArticle() != LanguageArticle.ZERO) {
                    logger.finest("###\tNoun form " + ta + " at " + currentSection.getName() + "."
                            + currentParam.getName() + " uses antiquated article form.  Stop it.");
                } else {
                    logger.log(getProblemLogLevel(), "###\tNoun form " + ta + " at " + currentSection.getName() + "."
                            + currentParam.getName() + " not defined for this type of language");
                }
                nid = ta.getApproximateNounForm();
            }

            // first test if this is for custom entities.  They need to keep track of the reference id
            if (getParser().getDictionary().isCustom(lowerName)) {
                // takes entity="0", "1", ... in addition to entityName
                String testRef = atts.getValue(ENTITY);
                try {
                    ref = Integer.parseInt(testRef);
                } catch (Exception ex) {
                    // Yeah, this should be NPE || NumberFormatException.  This is simpler.
                }
                if (ref == null) {
                    logger.log(getProblemLogLevel(), "###\tCustom entity <" + entityName + "> at " + currentSection.getName() + "."
                            + currentParam.getName() + " must have entity attribute");
                    return null;
                }
                return NounRefTag.getNounTag(realEntityName, ref, isCapital, escapeHtml, nid);
            }

            // OK, we have a "real" noun for it.
            if (n != null) {
                // this is %entity/%compoundNouns - check plural="y" in case someone set
                return NounRefTag.getNounTag(realEntityName, null, isCapital, escapeHtml, nid);
            }

            // See if it's a lowercase alias.
            n = getParser().getDictionary().getNounByPluralAlias(lowerName, false);
            if (n != null) {
                // Get the "correct" term attribute based on plural overrides
                NounForm overrideForm = getDictionary().getDeclension().getExactNounForm(LanguageNumber.PLURAL, nid.getCase(), nid.getPossessive(), nid.getArticle());
                if (overrideForm == null) {
                    // Look for legacy article forms...
                    if (ta.getArticle() != LanguageArticle.ZERO && getDictionary().getDeclension().hasArticle() &&
                            !getDictionary().getDeclension().hasArticleInNounForm()) {
                        logger.finest("###\tNoun form " + ta + " at " + currentSection.getName() + "."
                                + currentParam.getName() + " uses antiquated article form.  Stop it.");
                        overrideForm = getDictionary().getDeclension().getApproximateNounForm(LanguageNumber.PLURAL, nid.getCase(), nid.getPossessive(), nid.getArticle());
                    }
                }
                return NounRefTag.getNounTag(n.getName(), null, isCapital, escapeHtml, overrideForm);
            }
        }
        return null;
    }

    /**
     * Convert a Unicode-escaped <code>String</code> (uXXXX) into Unicode.
     *
     * @throws IllegalArgumentException If there is a substring starting with <code>\\uXXXX</code> where the value of XXXX is an invalid one
     *                                  for unicode.
     */
    public static String convertEscapedToUnicode(String str) {
        int len = str.length();
        DeferredStringBuilder buf = new DeferredStringBuilder(str);

        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (c != '\\') {
                buf.append(c);
            } else {
                // found an escape char '\\' at the end of the string
                if (i >= len) {
                    buf.append('\\');
                    break;
                }
                c = str.charAt(++i); // look at next character
                switch (c) {
                    case 't':
                        buf.append('\t');
                        break;
                    case 'r':
                        buf.append('\r');
                        break;
                    case 'n':
                        buf.append('\n');
                        break;
                    case 'f':
                        buf.append('\f');
                        break;
                    case 'u':
                        try {
                            ++i; // need to look at XXXX portion of \\uXXXX escape sequence

                            buf.append((char) Integer.parseInt(str.substring(i, i + 4), 16));
                            i += 3;
                        } catch (NullPointerException x) {
                            throw new IllegalArgumentException("Malformed \\uxxxx encoding at position " + (i - 2) + " in "
                                    + str);
                        } catch (StringIndexOutOfBoundsException x) {
                            throw new IllegalArgumentException("Malformed \\uxxxx encoding at position " + (i - 2) + " in "
                                    + str);
                        } catch (NumberFormatException x) {
                            throw new IllegalArgumentException("Malformed \\uxxxx encoding at position " + (i - 2) + " in "
                                    + str);
                        }
                        break;
                    default:
                        buf.append(c);
                }
            }
        }
        return buf.toString();
    }


    // -----------------------------------------------------------
    // Label Debugger support
    // ----------------------------------------------------------
    private static String BASE_FILE = null;
    static final Map<String, String> SECTION_TO_FILENAME = new ConcurrentHashMap<String, String>();
}
