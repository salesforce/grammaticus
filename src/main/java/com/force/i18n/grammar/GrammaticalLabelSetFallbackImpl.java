/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.force.i18n.I18nJavaUtil;
import com.force.i18n.LabelReference;
import com.force.i18n.grammar.GrammaticalLabelSet.GrammaticalLabelSetComposite;
import com.force.i18n.settings.*;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * A GrammaticalLabel set that contains two different sets, one "main"
 * set that, and a fallback set that can be used to retrieve labels that
 * don't exist in the "main" set.
 * @author stamm
 */
public final class GrammaticalLabelSetFallbackImpl extends GrammaticalLabelSetImpl implements GrammaticalLabelSetComposite {
    private static final long serialVersionUID = 1L;  // TODO: Prevent serialization

    private static final Logger logger = Logger.getLogger(GrammaticalLabelSetFallbackImpl.class.getName());

    private final GrammaticalLabelSet main;
    private final GrammaticalLabelSet fallback;
    private final boolean logFallback;
    private final boolean allowOtherGrammaticalForms;

    public GrammaticalLabelSetFallbackImpl(GrammaticalLabelSet main, GrammaticalLabelSet fallback) {
        super(main.getDictionary(),
                new CompositePropertyFileDataImpl(main.getPropertyFileData(), fallback.getPropertyFileData()),
                new ImmutableMapUnion<>(main.getLabelSectionToFilename(), fallback.getLabelSectionToFilename()),
                Sets.union(main.getPublicSectionNames(), fallback.getPublicSectionNames()));
        this.main = main;
        this.fallback = fallback;
        boolean sameLanguage = main.getDictionary().getLanguage() == fallback.getDictionary().getLanguage();
        this.logFallback = I18nJavaUtil.isDebugging() && !sameLanguage && main.getDictionary().getLanguage().shouldLogFallbackStrings();
        // If we're EN_US to EN_US, don't allow other forms.  If we're EN_GB to EN_US (or if the delegates are) then allow it.
        boolean _allowOtherForms = !sameLanguage;
        if (!_allowOtherForms && main instanceof GrammaticalLabelSetImpl) _allowOtherForms = ((GrammaticalLabelSetImpl)main).allowOtherGrammaticalForms();
        if (!_allowOtherForms && fallback instanceof GrammaticalLabelSetImpl) _allowOtherForms = ((GrammaticalLabelSetImpl)fallback).allowOtherGrammaticalForms();
        allowOtherGrammaticalForms = _allowOtherForms;
        setLastModified(Math.max(main.getLastModified(), fallback.getLastModified()));
    }

    /**
     * Set of allowed nontranslated section (where the logger warning won't be generated)
     * - UiSkin appears on every page, and is a product name and so is untranslated
     * - LanguageName is the name of the language translated into the language, hence is *already* translated
     */
    static final Set<String> ALLOWED_NONTRANSLATED_SECTIONS =
    		ImmutableSet.copyOf(Splitter.on(",").split(I18nJavaUtil.getProperty("nonTranslatedSections")));

    @Override
    public GrammaticalLabelSet getOverlay() {
        return this.main;
    }

    @Override
    public GrammaticalLabelSet getFallback() {
        return this.fallback;
    }

    /**
     * We need to resolve aliases here in the fallback, because delegating it won't allow overrides in the "main" set
     * to be seen by the "fallback" set
     */
    @Override
    protected Object inner_get(String section, String param, boolean throwSettingsSectionNotFoundException) throws SettingsSectionNotFoundException {
        Object result = ask_inner_get(section, param, throwSettingsSectionNotFoundException, logFallback);
        while (result instanceof LabelReference) {
            result = ask_inner_get(((LabelReference)result).getSection(), ((LabelReference)result).getKey(),
                    throwSettingsSectionNotFoundException, false); // If we fellback for an alias, don't keep doing it.
        }
        return result;
    }

    /**
     * @return the object for the section and param in either the main or fallback, logging bad label feedback if necessary
     * @param section the section name of the label
     * @param param the param key of the label
     * @param throwSettingsSectionNotFoundException throw SettingsSectionNotFoundException
     * @param doLogFallback create a warning to log for use of fallback string (which is bad for translated languages, but fine for language variants)
     * @throws SettingsSectionNotFoundException if the section isn't found and throwSettingsSectionNotFoundException is true.
     */
    protected Object ask_inner_get(String section, String param, boolean throwSettingsSectionNotFoundException, boolean doLogFallback) throws SettingsSectionNotFoundException {
        if (doLogFallback) {
            // See if it should be logged.
            boolean fromFallback = false;
            Object result = null;  // if we've already found it, then just go with that.
            Map<String, Object> theSect = this.main.getSection(section);
            if (theSect == null) {
                theSect = this.fallback.getSection(section);
                if (theSect != null && theSect.containsKey(param)) {
                    result = theSect.get(param);
                    fromFallback = true;
                }
            } else {
                if (!theSect.containsKey(param)) {
                    theSect = this.fallback.getSection(section);
                    if (theSect != null && theSect.containsKey(param)) {
                        result = theSect.get(param);
                        fromFallback = true;
                    }
                } else {
                    result = theSect.get(param);
                }
            }

            // Log it
            if (result == null) result = super.inner_get(section, param, throwSettingsSectionNotFoundException);
            if (fromFallback) {
                logFallbackWarning(result, section, param);
            }
            return result;
        } else {
            return super.inner_get(section, param, throwSettingsSectionNotFoundException);
        }
    }

    @Override
    protected boolean allowOtherGrammaticalForms() {
        return allowOtherGrammaticalForms;
    }

    private void logFallbackWarning(Object label, String section, String key) {
        // Only bother logging if it's a "final" label, as opposed to a LabelRef or TermRef AND we're not in a setup page (i.e. !useRenamedNouns())
        if (((label instanceof String) || (label instanceof List<?>)) && RenamingProviderFactory.get().getProvider().useRenamedNouns()) {
            if (!ALLOWED_NONTRANSLATED_SECTIONS.contains(section)) {
                logger.warning("FallbackString-" + main.getDictionary().getLanguage().getLocaleString() + ":" + section + "." + key);
            }
        }
    }

    @Override
    public LanguageDictionary getDictionary() {
        return main.getDictionary();  // Use the "main" dictionary, never the fallback
    }

    @Override
    public Set<String> getPublicSectionNames() {
        // Often fallback will have a section like "translatedlanguagename" that won't be in main.  That's fine.
        return Sets.union(fallback.getPublicSectionNames(), main.getPublicSectionNames());  // Yes.  Use the fallback; using a union is expensive and this is only used in test code for now
    }

    @Override
    public boolean containsParam(String sectionName, String paramName) {
        return main.containsParam(sectionName, paramName) || fallback.containsParam(sectionName, paramName);
    }

    @Override
    public GrammaticalTerm getGrammaticalTerm(String section, String param) {
        GrammaticalTerm term = main.getGrammaticalTerm(section, param);
        if (term == null) term = fallback.getGrammaticalTerm(section, param);
        return term;
    }

    /**
     * An implementation of PropertyFileData that is a composition of two property files
     */
    static class CompositePropertyFileDataImpl implements PropertyFileData {
        private final PropertyFileData overlay;
        private final PropertyFileData fallback;
        public CompositePropertyFileDataImpl(PropertyFileData overlay, PropertyFileData fallback) {
            this.overlay = overlay;
            this.fallback = fallback;
        }
        @Override
        public Map<String, Object> getSection(String sectionName) {
            Map<String,Object> mainSection = overlay.getSection(sectionName);
            Map<String,Object> fallbackSection = fallback.getSection(sectionName);
            if (mainSection != null) {
                if (fallbackSection == null) {
                    return mainSection;
                } else {
                    return new ImmutableMapUnion<String,Object>(mainSection, fallbackSection);
                }
            } else {
                return fallbackSection;
            }
        }
        @Override
        public Object get(String sectionName, String paramName) {
            Object overlayValue =  overlay.get(sectionName, paramName);
            if (overlayValue != null) return overlayValue;

            return fallback.get(sectionName, paramName);
        }
        @Override
        public Set<Entry<String, Map<String, Object>>> entrySet() {
            throw new UnsupportedOperationException("You should not iterate through the entry set of a composite property file");
        }
        @Override
        public Object put(String sectionName, String paramName, Object value) {
            return this.overlay.put(sectionName, paramName, value);
        }
        @Override
        public Object remove(String sectionName, String paramName) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void removeSection(String sectionName) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<String> getSectionNames() {
            return Sets.union(overlay.getSectionNames(), fallback.getSectionNames());
        }
        @Override
        public Set<String> getPublicSectionNames() {
            return Sets.union(overlay.getPublicSectionNames(), fallback.getPublicSectionNames());
        }
        @Override
        public void setSectionAsPublic(String section) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsSection(String sectionName) {
            return overlay.containsSection(sectionName) || fallback.containsSection(sectionName);
        }
        @Override
        public boolean contains(String sectionName, String paramName) {
            return overlay.contains(sectionName, paramName) || fallback.contains(sectionName, paramName);
        }
        @Override
        public Locale getLocale() {
            return overlay.getLocale();
        }
        @Override
        public void shareKeys(SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap) {
            overlay.shareKeys(seedKeyMap);
        }
        private static final long serialVersionUID = 1L;
    }

    /**
     * Implementation of a MapUnion where there is a immutable overlay and a "backing" map.
     * Both must be non null.
     */
    public static final class ImmutableMapUnion<K, V> extends AbstractMap<K, V> {
        private final Map<K,V> overlay;
        private final Map<K,V> fallback;

        public ImmutableMapUnion(Map<K,V> overlay, Map<K,V> fallback) {
            this.overlay = overlay;
            this.fallback = fallback;
        }

        @Override
        public boolean containsKey(Object key) {
            return overlay.containsKey(key) || fallback.containsKey(key);
        }

        @Override
        public V get(Object key) {
            if (overlay.containsKey(key)) return overlay.get(key); else return fallback.get(key);
        }

        @Override
        public Set<K> keySet() {
            return Sets.union(overlay.keySet(), fallback.keySet());
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return Sets.union(overlay.entrySet(), Sets.filter(fallback.entrySet(), new Predicate<Entry<K,V>>() {
                @Override
                public boolean apply(Entry<K,V> input) {
                    return !overlay.containsKey(input.getKey());
                }
            }));
        }
    }

    /**
     * @return the serialization proxy for random noun forms (with a 7-ish byte cost)
     * If you have a map, using SerializeMap below is *much* better
     */
    protected final Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * The use of non-serializable unions means reconstructing from the labels themselves is much better
     */
    static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final GrammaticalLabelSet main;
        private final GrammaticalLabelSet fallback;
        public SerializationProxy(GrammaticalLabelSetFallbackImpl impl) {
            this.main = impl.getOverlay();
            this.fallback = impl.getFallback();
        }
        protected Object readResolve() {
            return new GrammaticalLabelSetFallbackImpl(this.main, this.fallback);
        }
    }
}
