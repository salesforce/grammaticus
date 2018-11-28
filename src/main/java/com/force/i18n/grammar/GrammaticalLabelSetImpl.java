/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.parser.*;
import com.force.i18n.settings.*;


/**
 * A label set that uses a LabelDictionary to provide the ability to
 * rename nouns and use more "generic" functionality.
 *
 * @author yoikawa, stamm
 */
public class GrammaticalLabelSetImpl extends LabelSetImpl implements GrammaticalLabelSet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(GrammaticalLabelSetImpl.class.getName());

    private final LanguageDictionary dictionary;
    private final Set<String> publicSections;

    /**
     * @param dictionary the dictionary that was filled in by the parser
     * @param p          the parser
     */
    public GrammaticalLabelSetImpl(LanguageDictionary dictionary, GrammaticalLabelFileParser p) throws IOException {
        super(p);
        assert dictionary != null : "You must provide a dictionary";
        this.dictionary = dictionary;
        this.publicSections = null;
        setLastModified(p.getLastModified());
        p.close(this);
    }

    /**
     * @param dictionary the dictionary that was filled in by the parser
     * @param p          the parser
     * @param data       the data that contains the public section names
     */
    public GrammaticalLabelSetImpl(LanguageDictionary dictionary, GrammaticalLabelFileParser p, PropertyFileData data) throws IOException {
        super(p, data);
        assert dictionary != null : "You must provide a dictionary";
        this.dictionary = dictionary;
        this.publicSections = data.getPublicSectionNames();
        setLastModified(p.getLastModified());
        p.close(this);
    }

    /**
     * Used by sleepy cat, or other implementations that already have the data specified preparsed
     *
     * @param dictionary             the dictionary associated with this label set
     * @param data                   the preloaded data
     * @param labelSectionToFilename the labelSectionToFilename
     * @param publicSections         The set of sections to be constructed. Usually comes from the prebuilt english labels.
     *                               We used to use data.getPublicSectionNames(), but when building the non english labels DB, that value is null.
     */
    protected GrammaticalLabelSetImpl(LanguageDictionary dictionary, PropertyFileData data, Map<String, String> labelSectionToFilename, Set<String> publicSections) {
        super(data, labelSectionToFilename);
        this.dictionary = dictionary;
        this.publicSections = publicSections;
    }

    /**
     * Use this only for testing or various values.  This doesn't do any caching, and it, ahem.  Evil.
     * However, we provide this capability to our localization team.
     *
     * @param descriptor the descriptor to use to create the test label set
     * @return a test label set that can be used
     */
    public static GrammaticalLabelSet getTestLabelSet(GrammaticalLabelSetDescriptor descriptor) throws IOException {
        return getTestLabelSet(descriptor, null);
    }

    public static GrammaticalLabelSet getTestLabelSet(GrammaticalLabelSetDescriptor descriptor, GrammaticalLabelSetProvider parent) throws IOException {
        LanguageDictionary dict = new LanguageDictionaryParser(descriptor, descriptor.getLanguage(), parent).getDictionary();
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dict, descriptor, parent, false);
        return new GrammaticalLabelSetImpl(dict, parser);
    }


    // new version of string formatter.
    private String formatString(Object obj, Renameable[] entities, boolean forMessageFormat) {
        if (obj == null) {
            return null;
        }

        // always test with String first to avoid more comparison.
        if (obj instanceof String) {
            String str = (String) obj;
            return forMessageFormat
                    ? TextUtil.escapeForMessageFormat(
                    str, new StringBuilder(str.length()), false).toString()
                    : str;
        }

        if (obj instanceof LabelReference) {
            return formatString(resolveLabelRef(obj), entities, forMessageFormat);
        }

        // possibly new complex label. let LabelInfo to handle
        return dictionary.format(obj, entities, allowOtherGrammaticalForms(), forMessageFormat);
    }

    /**
     * @return whether or not other grammatical forms should be allowed when formatting labels
     */
    protected boolean allowOtherGrammaticalForms() {
        return false;
    }

    @Override
    public String getString(String section, String param) {
        return formatString(this.get(section, param), null, false);
    }

    @Override
    public String getString(LabelReference ref) {
        return formatString(this.get(ref), null, false);
    }

    @Override
    public String getStringThrow(String section, String param) {
        return formatString(this.get(section, param, true), null, false);
    }

    @Override
    public String getString(String section, String param, String ifNull) {
        return formatString(this.get(section, param, ifNull), null, false);
    }

    @Override
    public String getString(String section, Renameable[] entities, String param) {
        return formatString(this.get(section, param), entities, false);
    }

    @Override
    public String getString(String section, Renameable[] entities, String param, String ifNull) {
        return formatString(this.get(section, param, ifNull), entities, false);
    }

    @Override
    public String getString(String section, String param, boolean forMessageFormat) {
        return formatString(this.get(section, param), null, forMessageFormat);
    }

    @Override
    public String getString(String section, Renameable[] entities, String param, boolean forMessageFormat) {
        return formatString(this.get(section, param), entities, forMessageFormat);
    }

    /**
     * Override so that missing labels don't throw gacks, but are logged instead
     */
    @Override
    public Object get(String section, String param, boolean allowLabelException) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        LabelDebugProvider.get().trackLabel(section, param);
        Object result = inner_get(section, param, true);
        if (result == null) {
            return processMissingLabel(
                    "PropertyFile - val " + param + " not found in section " + section, allowLabelException);
        }
        return result;
    }

    @Override
    public Object get(String section, String param, Object ifNull) throws SettingsSectionNotFoundException {
        LabelDebugProvider.get().trackLabel(section, param);
        return super.get(section, param, ifNull);
    }

    // This needs to be available to the parser
    @Override
    public void setLabelSectionToFilename(Map<String, String> sectionMap) {
        assert super.getLabelSectionToFilename() == null : "You cannot overwrite the section->filename map";
        super.setLabelSectionToFilename(sectionMap);
    }

    protected final Object resolveLabelRef(Object o) {
        return resolveLabelRef(o, null, null);
    }

    protected final Object resolveLabelRef(Object o, String section, String param) {
        if (o instanceof LabelReference) {
            try {
                // Bad aliases should return "" to satisfy the old requirements (see LabelParserTest.testRoot)
                return this.get((LabelReference) o);
            } catch (SettingsSectionNotFoundException ex) {
                logger.warning("Invalid label reference, section not found: " + o + " for " + section + "." + param);
                return "";
            } catch (ParameterNotFoundException e) {
                logger.warning("Invalid label reference, parameter not found: " + o + " for " + section + "." + param);
                return "";
            }
        } else {
            return o;
        }
    }

    @Override
    public GrammaticalTerm getGrammaticalTerm(String section, String param) {
        if (!labelExists(section, param)) {
            return null;
        }
        Object o = resolveLabelRef(this.get(section, param), section, param);
        if (o instanceof TermRefTag) {
            TermRefTag refTag = (TermRefTag) o;
            if (refTag.isNoun()) {
                return getDictionary().getNoun(refTag.getName(), false);
            } else if (refTag.isAdjective()) {
                return getDictionary().getAdjective(refTag.getName());
            } else if (refTag.isArticle()) {
                return getDictionary().getArticle(refTag.getName());
            }
        }
        return null;
    }

    @Override
    public Set<String> getPublicSectionNames() {
        return this.publicSections;
    }

    @Override
    public LanguageDictionary getDictionary() {
        return this.dictionary;
    }

}
