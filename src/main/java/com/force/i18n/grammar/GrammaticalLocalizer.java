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

import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;

import com.force.i18n.*;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;

/**
 * A localizer that uses a grammatical label set for processing labels.
 *
 * @author stamm
 */
public class GrammaticalLocalizer extends BaseLocalizer {

    public GrammaticalLocalizer(Locale locale, Locale currencyLocale, TimeZone timeZone, HumanLanguage language,
            GrammaticalLabelSet labelSet) {
        super(locale, currencyLocale, timeZone, language, labelSet);
    }

    @Override
    public GrammaticalLabelSet getLabelSet() {
        return (GrammaticalLabelSet)this.labelSet;
    }

    /**
     * NOTE: This probably shouldn't be here, but for now it's a nice way to kill LabelInfo most places
     * @return the declension for the current language
     */
    public LanguageDeclension getDeclension() {
        return LanguageDeclensionFactory.get().getDeclension(getUserLanguage());
    }

    /**
     * @return email encoding based on the language for the system emails
     * This is not the same as User's personal email encoding
     */
    public String getEmailEncoding() {
        return getUserLanguage().getSystemEmailEncoding();
    }

    public String getFileEncoding() {
        return getUserLanguage().getDefaultFileEncoding();
    }

    // Label helpful stuff
    @Override
    public String getLabel(String section, String name) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(super.getLabel(section, name), section, name);
    }

    @Override
    public String getLabelThrow(String section, String name) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(super.getLabelThrow(section, name), section, name);
    }

    /**
     * The following getLabelNotHint(*) methods are special cased label lookups, so even while in
     * label debugging mode the current label request WILL NOT appended the label debug info.
     *
     * This is needed for certain labels which aren't truly displayed as text, but rather are
     * label keys to other labels.  Or other weird scenarios where putting the label debug info
     * into the returned String just breaks stuff.
     *
     * @param section the section name of the label
     * @param name the key of the label
     * @return the label without any hint, if hints are enabled.
     */
    public String getLabelNoHint(String section, String name) {
        return super.getLabel(section, name);
    }

    public String getLabelNoHint(String section, String name, Object... args) {
		MessageFormat formatter = getMessageFormat(getLabelNoHint(section, name, true));
        return formatter.format(args);
    }

    public String getLabelNoHint(String section, String name, boolean forMessageFormat) {
        return getLabelSet().getString(section, name, forMessageFormat);
    }

    /**
     * Convenience function that calls <CODE>java.text.MessageFormat.format()</CODE> on the label
     * using the <CODE>Object[]</CODE> supplied.
     * @param    section    same as getLabel(section, key)
     * @param    key    same as getLabel(section, key)
     * @param    args    for <CODE>java.text.MessageFormat.format()</CODE>
     * @return the formatted label
     */
    @Override
    public String getLabel(String section, String key, Object... args) {
        MessageFormat formatter = getMessageFormat(getLabel(section, key, true));
        return LabelDebugProvider.get().makeLabelHintIfRequested(formatter.format(args), section, key);
    }

    @Override
    public String getLabelThrow(String section, String key, Object... args) {
        MessageFormat formatter = getMessageFormat(getLabelThrow(section, key, true));
        return LabelDebugProvider.get().makeLabelHintIfRequested(formatter.format(args), section, key);
    }

    public String getLabel(String section, Renameable[] entities, String key) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(getLabelSet().getString(section, entities, key), section, key);
    }

    public String getLabel(String section, Renameable[] entities, String key, Object... args) {
        MessageFormat formatter = getMessageFormat(getLabel(section, entities, key, true));
        return LabelDebugProvider.get().makeLabelHintIfRequested(formatter.format(args), section, key);
    }

    public String getLabelNoThrow(String section, String name) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(getLabelSet().getString(section, name, null), section, name);
    }

    public String getLabelNoThrow(String section, String name, Object... args) {
        String labelText = getLabelSet().getString(section, name, null);
        if (labelText != null) {
            MessageFormat formatter = getMessageFormat(TextUtil.escapeForMessageFormat(labelText, new StringBuilder(labelText.length()), false).toString());
            labelText = LabelDebugProvider.get().makeLabelHintIfRequested(formatter.format(args), section, name);
        }
        return labelText;
    }

    public String getLabelNoThrow(String section, Renameable[] entities, String name) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(getLabelSet().getString(section, entities, name, null), section,
            name);
    }

    public String getLabel(String section, String name, boolean forMessageFormat) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(getLabelSet().getString(section, name, forMessageFormat), section,
            name);
    }

    public String getLabelThrow(String section, String name, boolean forMessageFormat) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(getLabelSet().getStringThrow(section, name, forMessageFormat), section,
            name);
    }


    public String getLabel(String section, Renameable[] entities, String key, boolean forMessageFormat) {
        return LabelDebugProvider.get().makeLabelHintIfRequested(getLabelSet().getString(section, entities, key, forMessageFormat),
            section, key);
    }

    public String getLabel(LabelReference ref) {
        Object[] args = ref.getArguments();
        if (args != null && args.length > 0) {
			MessageFormat formatter = getMessageFormat(getLabelSet().getString(ref.getSection(), null, ref.getKey(), true));
            return LabelDebugProvider.get().makeLabelHintIfRequested(formatter.format(ref.getArguments()),
                ref.getSection(), ref.getKey());
        }
        return getLabelSet().getString(ref.getSection(), null, ref.getKey(), false);
    }

    public String getLabelNoThrow(LabelReference ref) {
        return getLabel(ref);
    }

    // Temporary variables
    private Collator collator;
    private Comparator<String> comparator;

    /**
     * @return the Collator for the user's locale.
     * Collators are not threadsafe, so don't reuse this across threads
     */
    public Collator getCollator() {
        if (this.collator == null) {
            this.collator = makeCollator();
        }
        return this.collator;
    }

    /**
     * @return a cached instance of comparator
     */
    public Comparator<String> getComparator() {
        if (this.comparator == null) {
            this.comparator = makeComparator();
        }
        return this.comparator;
    }

    /**
     * @return the Collator for the user's locale
     * @param numElements the number of elements in the array
     */
    public Comparator<String> getComparator(int numElements) {
        return TextUtil.getComparator(getCollator(), numElements);
    }

    /**
     * @return an instance of a collator that isn't cached locally
     */
    protected Collator makeCollator() {
    	return Collator.getInstance(getLocale());
    }

    /**
     * @return a noncaching comparator that can be reused
     */
    protected Comparator<String> makeComparator() {
    	return getComparator(16);
    }

    public static final String CONTEXT_NAME = "Localizer";
}
