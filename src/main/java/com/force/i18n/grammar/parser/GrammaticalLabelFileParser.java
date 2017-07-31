/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import com.force.i18n.*;
import com.force.i18n.commons.text.GenericUniquefy;
import com.force.i18n.commons.text.Uniquefy;
import com.force.i18n.grammar.*;
import com.force.i18n.settings.*;
import com.force.i18n.settings.BasePropertyFile.MetaDataInfo;
import com.google.common.base.Splitter;
import com.google.common.collect.*;

/**
 * Holder of information while parsing a set of Label files.
 *
 * @author stamm
 */
public class GrammaticalLabelFileParser implements BasePropertyFile.Parser {
    private static final Logger logger = Logger.getLogger(GrammaticalLabelFileParser.class.getName());

    private final LanguageDictionary dictionary;
    private final LabelSetDescriptor desc;
    private final GrammaticalLabelSetProvider parentProvider;
    private final boolean trackDupes;

    private final Map<String, AliasParam> aliasMap = new HashMap<String, AliasParam>();
    private final Multimap<String, String> allLabels = TreeMultimap.create();
    private Uniquefy uniquefy = new Uniquefy();
    private GenericUniquefy<LabelRef> aliasUniquefy = new GenericUniquefy<LabelRef>();

    private List<AliasParam> illegalAliases;
    private Set<LabelRef> invalidLabels;  // The set of labels that have a "problem" with them

    private long lastModified = -1;

    /**
     * Construct a label file parser
     * @param dictionary the dictionary to fill in with nouns and adjectives
     * @param labelDesc the descriptor of the location of the label set
     */
    public GrammaticalLabelFileParser(LanguageDictionary dictionary, LabelSetDescriptor labelDesc, GrammaticalLabelSetProvider parentProvider) throws IOException {
        this(dictionary, labelDesc, parentProvider,
        		dictionary.getLanguage() == LanguageProviderFactory.get().getBaseLanguage()
        		&& isDupeLabelTrackingEnabled());
    }

    public GrammaticalLabelFileParser(LanguageDictionary dictionary, LabelSetDescriptor labelDesc, GrammaticalLabelSetProvider parentProvider, boolean trackDupes) {
        this.dictionary = dictionary;
        this.desc = labelDesc;
        this.parentProvider = parentProvider;
        this.trackDupes = trackDupes;
    }

    @Override
    public void load(PropertyFileData data, Map<String, Map<String, MetaDataInfo>> metaData) throws IOException {
        // be nice if we removed the special condition on train.xml in LabelHandler.java
        boolean found = false;

        if (this.desc instanceof TestLanguageLabelSetDescriptor) {
            parseLabels(data, ((TestLanguageLabelSetDescriptor)this.desc).getText());
            found = true;
        } else if (this.desc.hasOverridingFiles()) {
            // Parse only non-english labels
            for (URL labelFile : this.desc.getOverridingFiles()) {
                if (TrackingHandler.exists(labelFile)) {
                    found = true;
                    parseLabels(data, labelFile);
                }
            }
        } else {
        	if(this.desc.hasModularizedFiles()) {
        		for(URL modularizedFile : this.desc.getModularizedFiles()) {
        			found = true;
        			parseLabels(data, modularizedFile);
        		}
        	} else if (TrackingHandler.exists(this.desc.getRootFile())) {
                // Parse english labels
                found = true;
                parseLabels(data, this.desc.getRootFile());
            }
        }
        if (!found) {
            // TODO: Vietnamese doesn't exist yet (new languages don't), so we pretty much just ignore it
            //throw new java.io.FileNotFoundException("can't read label file: " + this.desc.toString());
        }
    }

    @Override
    public long getFileLastModified() {
        // part of the Parser interface
        return this.lastModified;
    }

    public URL getRootDir() {
        return this.desc.getRootDir();
    }

    public LanguageDictionary getDictionary() {
        return this.dictionary;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    void parseLabels(PropertyFileData data, URL file) {
        GrammaticalLabelFileHandler handler = new GrammaticalLabelFileHandler(file, data, this);
        parse(file, handler);
    }

    void parseLabels(PropertyFileData data, String labelText) {
        GrammaticalLabelFileHandler handler = new GrammaticalLabelFileHandler(this.desc.getRootFile(), data, this);
        parse(new InputSource(new StringReader(labelText)), handler);
    }

    private void parse(URL file, TrackingHandler handler) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            URLConnection connection = file.openConnection();
            connection.connect();
            this.lastModified = Math.max(this.lastModified, connection.getLastModified());
            saxParser.parse(connection.getInputStream(), handler);

        }
        catch (Exception ex) {
            throw new RuntimeException("Error parsing XML file " + handler.getLineNumberString(), ex);
        }
    }

    private void parse(InputSource source, TrackingHandler handler) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);
            SAXParser saxParser = spf.newSAXParser();
            saxParser.getXMLReader().setEntityResolver(handler);
            saxParser.parse(source, handler);
        }
        catch (Exception ex) {
            throw new RuntimeException("Error parsing XML file " + handler.getLineNumberString(), ex);
        }
    }

    boolean trackDupes() {
        return this.trackDupes;
    }

    static boolean isDupeLabelTrackingEnabled() {
    	return false;
    }

    String uniquefy(String label) {
        return this.uniquefy.unique(label);
    }

    void trackLabel(String label, String location) {
        if (this.trackDupes) allLabels.put(label, location);
    }

    List<String> writeDuplicateLabelsFile(Writer out) throws IOException {
        List<String> result = new ArrayList<>();
        try {
            long totalWords = 0;
            long dupeWords = 0;
            long totalLabels = 0;
            long dupeLabels = 0;

            for (Map.Entry<String, Collection<String>> entry : this.allLabels.asMap().entrySet()) {
                int count = entry.getValue().size();
                totalLabels += count;
                dupeLabels += (count - 1);

                int words = Iterables.size(Splitter.on(' ').split(entry.getKey()));
                totalWords += words * count;
                dupeWords += words * (count - 1);

                if (count > 1) {
                    String msg = entry.getKey() + ": " + entry.getValue().size() + " dupes: " + entry.getValue();
                    result.add(msg);
                    if (out != null) {
                        out.write(msg);
                        out.write('\n');
                    }
                }
            }
            if (out != null) {
                out.write("\n\n************ dupe labels: " + dupeLabels + "\n");
                out.write("************ total labels: " + totalLabels + "\n");
                out.write("************ dupe words: " + dupeWords + "\n");
                out.write("************ total words: " + totalWords + "\n");
            }
            this.allLabels.clear();
            return result;
        } finally {
            if (out != null) out.close();
        }
    }

    List<AliasParam> getIllegalAliases() { return this.illegalAliases; }
    public Set<? extends LabelReference> getInvalidLabels() { return this.invalidLabels; }

    // ====================================================================
    // Param alias handler: <param name="nnn" alias="xxx"/>
    // ====================================================================
    private static final String BAD_ALIAS = "###\tBad alias: ";

    class AliasParam implements Comparable<AliasParam> {
        final URL file;
        final int lineNumber;
        final String srcSection, srcParam, dstSection, dstParam;
        final String srcKey; // Memoize since it's used so often
        final String dstKey; // Memoize since it's used so often
        boolean ignore = false;

        AliasParam(String srcSection, String srcParam, String dstSection, String dstParam, URL file, int lineNumber) {
            this.file = file;
            this.lineNumber = lineNumber;

            this.srcSection = srcSection;
            this.srcParam = srcParam;
            this.dstSection = dstSection;
            this.dstParam = dstParam;
            this.srcKey = GrammaticalLabelFileParser.getKey(this.srcSection, this.srcParam);
            this.dstKey = GrammaticalLabelFileParser.getKey(this.dstSection, this.dstParam);

            // ignore self-reference
            this.ignore = srcSection.equals(dstSection) && srcParam.equals(dstParam);
            if (this.ignore)
                error("Circular reference at ", getKey());
        }

        String getKey() {
            return this.srcKey;
        }

        String getTargetKey() {
            return this.dstKey;
        }

        void error(String msg, String key) {
            String fileMsg = "";
            if (file != null) {
                fileMsg = this.file.getPath() + "(" + lineNumber + "): ";
            }
            String message = BAD_ALIAS + fileMsg + msg + (key == null ? "" : key);
            // if we are loading English labels in dev-mode throw an exception here
            if (I18nJavaUtil.isDebugging() && LanguageProviderFactory.get().getBaseLanguage() == GrammaticalLabelFileParser.this.getDictionary().getLanguage()
                    && !(GrammaticalLabelFileParser.this.desc instanceof TestLanguageLabelSetDescriptor)) {
                throw new IllegalStateException(message);
            } else {
                if (illegalAliases == null) illegalAliases = new ArrayList<AliasParam>(10);
                illegalAliases.add(this);
                // Oh, just keep going
                logger.fine(message);
            }
        }

        private Object validateDst(LabelSet label) {
            Map<String, Object> sectionMap = label.getSection(dstSection);
            if (sectionMap == null) {
                error("Section " + dstSection + " does not exist.", null);
                return makeLabelRef(dstSection, dstParam);  // Let it die later
            }

            Object val = sectionMap.get(dstParam);
            if (val == null) {
                error("Param does not exist: ", getTargetKey());
            }
            return val != null ? makeLabelRef(dstSection, dstParam) : null;
        }

        LabelRef makeLabelRef(String section, String param) {
            return aliasUniquefy.unique(new LabelRef(uniquefy(section), uniquefy(param)));
        }

        @Override
        public String toString() {
            return this.file.getPath() + "(" + lineNumber + "): " + srcKey + "->" + dstKey;
        }

        @Override
        public int compareTo(AliasParam o) {
            int fileCompare = this.file.getPath().compareTo(o.file.getPath());
            if (fileCompare == 0) {
                if (this.lineNumber < o.lineNumber) {
                    return -1;
                } else if (this.lineNumber == o.lineNumber) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return fileCompare;
            }
        }
    }

    static final String getKey(String sec, String param) {
        return sec + "." + param;
    }

    void addInvalidLabel(String section, String key) {
        if (this.invalidLabels == null) this.invalidLabels = new TreeSet<LabelRef>();
        this.invalidLabels.add(new LabelRef(section, key));
    }

    void addAlias(String srcSection, String srcParam, String dstSection, String dstParam, URL file, int lineNumber) {
        AliasParam alias = new AliasParam(srcSection, srcParam, dstSection, dstParam, file, lineNumber);
        this.aliasMap.put(alias.getKey(), alias);
    }

    void removeAlias(String srcSection, String srcParam) {
        if (this.aliasMap != null) {
            String k = getKey(srcSection, srcParam);
            if (this.aliasMap.containsKey(k)) {
                this.aliasMap.remove(k);
            }
        }
    }

    Map<String,AliasParam> getAliasMap() {
        return Collections.unmodifiableMap(this.aliasMap);
    }

    // Provide the set you are writing to
    public void close(GrammaticalLabelSetImpl writeSet) {
        GrammaticalLabelSet parentSet = parentProvider != null ? parentProvider.getSet(dictionary.getLanguage()) : null;
        resolveAliases(parentSet != null ? new GrammaticalLabelSetFallbackImpl(writeSet, parentSet) : writeSet, writeSet);
    }

    /**
     * Resolve all alias value in &lt;param&gt; tag. This param tag suppose to exists in <tt>label</tt>, with null
     * value.
     *
     * @param labelSet
     *            LabelSet to resolve alias values
     * @param writeSet the label set to write to
     */
    void resolveAliases(GrammaticalLabelSet labelSet, GrammaticalLabelSetImpl writeSet) {
        if (this.aliasMap.isEmpty())
            return;

        // Use array to get all values instead of using Iterator. This is because resolveAlias
        // internally removes resolved value from aliasMap. If multiple alias(es) are recursively resolved,
        // it may remove multiple values from the map.
        for (AliasParam ap : new HashSet<AliasParam>(this.aliasMap.values())) {
            if (this.aliasMap.containsKey(ap.getKey())) {
                resolveAlias(labelSet, writeSet, ap, null);
            }
        }
        assert this.aliasMap.isEmpty() : "Unresolved aliases: " + this.aliasMap;
        this.uniquefy = null;
        this.aliasUniquefy = null;
    }

    /**
     * resolve single alias value
     *
     * @param labelSet
     * @param ap
     *            alias to evaluate. must NOT null
     * @param refSet
     *            null if this is top level of alias chain.
     * @return resolved value or null otherwise
     */
    private Object resolveAlias(GrammaticalLabelSet labelSet, GrammaticalLabelSetImpl writeSet, AliasParam ap, Set<String> refSet) {

        Object retValue = null;
        if (ap.ignore) {
            retValue = "";

        } else {
            // dst could be either valid value, or another alias
            AliasParam t = this.aliasMap.get(ap.getTargetKey());
            if (t == null) {
                // target is not an alias. Set dst to the current value
                retValue = ap.validateDst(labelSet);

            } else if (refSet != null && refSet.contains(t.getKey())) {
                // target key exists in the recursive alias chain, that means this is
                // circular reference.
                ap.error("Circular reference at ", t.getKey());
                addInvalidLabel(ap.srcSection, ap.srcParam);
            } else {
                // recursive reference. Keep tracking down
                Set<String> localRefSet = refSet;
                if (refSet == null) {
                    // means this is the top level (or maybe middle) of alias chain
                    localRefSet = new HashSet<String>();
                } else {
                    // recursively called from alias chain. add to the chain list
                    refSet.add(ap.getKey());
                }

                retValue = resolveAlias(labelSet, writeSet, t, localRefSet);
                if (retValue == null)
                    ap.error("Param does not exist: ", ap.getKey());

                // if the current stack level is the top of the chain, remove resolved values from the map.
                // localRefSet contains only descendants.
                if (refSet == null && !localRefSet.isEmpty()) {
                    for (String string : localRefSet) {
                        this.aliasMap.remove(string);
                    }
                }
            }

        }

        writeSet.put(ap.srcSection, ap.srcParam, (retValue == null ? "" : retValue));
        this.aliasMap.remove(ap.getKey());
        return retValue;
    }

}
