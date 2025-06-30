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

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.force.i18n.Renameable;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.LanguageDictionary;

/**
 * Base class of all Reference Tag elements in labels file. This keeps any elements
 * appears in contents of <CODE>param</CODE> element.
 * @author stamm
 */
public abstract class RefTag implements Serializable  {
    private static final long serialVersionUID = 1L;

    public RefTag() {
    }

    /**
     * @return a "key" that uniquely identifies all the attributes of the reference tag.
     * Used by toString to provide some usefulness
     */
    public abstract String getKey();

    /**
     * @return whether this GrammaticalTerm is "dynamic", i.e. it requires a
     * Renameable to be used.
     */
    public boolean isDynamic() {
        return false;
    }

    /**
     * Format this reference into a user-readable format based on the given dictionary
     * and dynamic entities
     * @param dictionary the dictionary of language particles.
     * @param entities the set of dynamic renameable entities
     * @param vals the values that
     * @param overrideForms if the form contained inside this term ref might be a different language than the dictionary provided.
     * In this case, the implementation must try to find the closest term to the one stored with this term ref.
     * @return a user-readable string for this reference
     */
    public abstract String toString(LanguageDictionary dictionary, boolean overrideForms, Object[] vals, Renameable... entities);

    /**
     * @return Get the current ref tag as a JSON object for use in offline mode
     * @param dictionary the dictionary with all the nouns
     * @param list the list of the current set of terms being processed (so that for modifiers it can find the associated term by index)
     */
    public abstract String toJson(LanguageDictionary dictionary, List<?> list);

    /**
     * Get all the terms referenced by this reference tag
     * @param dictionary the dictionary to use to look up terms
     * @return the set of terms references.  Will not be null
     */
    public abstract Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary);

    @Override
    public String toString() {
        return getKey();
    }

    /**
     * Given an object that may come from a grammatical label set (i.e. is a String, List, or RefTag), return the set of terms referenced by the label value
     * @param dictionary the dictionary to use to look up terms
     * @param labelValue the value from which to extract terms
     * @return a non-null set of grammatical terms
     */
    public static Set<GrammaticalTerm> getTermsFromLabelValue(LanguageDictionary dictionary, Object labelValue) {
        if (labelValue == null || labelValue instanceof String) return Collections.emptySet();

        Set<GrammaticalTerm> result = new HashSet<>();
        if (labelValue instanceof List) {
            for (Object part : (List<?>)labelValue) {
                if (part instanceof RefTag) {
                    result.addAll(((RefTag)part).getTermsInUse(dictionary));
                }
            }
        } else if (labelValue instanceof RefTag) {
            result.addAll(((RefTag)labelValue).getTermsInUse(dictionary));
        }
        return result;
    }

    /**
     * Given a collection of objects that may come from a grammatical label set (i.e. is a String, List, or RefTag), return the set of terms referenced by the label value
     * @param dictionary the dictionary to use to look up terms
     * @param labelValues the set of values from which to extract terms
     * @return a non-null set of grammatical terms
     */
    public static Set<GrammaticalTerm> getTermsFromLabels(LanguageDictionary dictionary, Iterable<Object> labelValues) {
        Set<GrammaticalTerm> result = null;
        for (Object labelValue : labelValues) {
            Set<GrammaticalTerm> val = getTermsFromLabelValue(dictionary, labelValue);
            if (!val.isEmpty()) {
                if (result == null) result = new HashSet<>();
                result.addAll(val);
            }
        }
        return result != null ? result : Collections.emptySet();
    }

    /**
     * Convert a component of a label to a String, either it's a string or a refTag
     * @param dictionary the current dictionary of nouns.
     * @param o the label value
     * @param list the list of *all* of the labels to look up, to make sure you get the right reference
     * @return o as a json value
     */
    static String refToJson(LanguageDictionary dictionary, Object o, List<?> list) {
        if (o instanceof RefTag) {
            return ((RefTag)o).toJson(dictionary, list);
        } else {
            return "\"" + TextUtil.escapeForJsonString(String.valueOf(o)) + "\"";
        }
    }

    /**
     * Append the label value as json to appendable.  Converts IOException to
     * @param dictionary the current dictionary of nouns.
     * @param out the thing to write to
     * @param value the Label value, a String, List, or RefTag
     * @param termsInUse if not null, add the set of terms referenced in value to it
     * @throws IOException if there is an issue writing to out.
     */
    public static void appendJsonLabelValue(LanguageDictionary dictionary, Appendable out, Object value, Set<GrammaticalTerm> termsInUse) throws IOException {
        if (value instanceof String) {
            out.append("\"").append(TextUtil.escapeForJsonString((String)value)).append("\"");
        } else if (value instanceof List) {
            // Stream out the list.
            out.append("[");
            out.append(((List<?>)value).stream().map(a -> refToJson(dictionary, a, (List<?>)value))
                    .collect(Collectors.joining(",")));
            out.append("]");
            if (termsInUse != null) termsInUse.addAll(getTermsFromLabelValue(dictionary, value));
        } else if (value instanceof RefTag) {
            // Simplification for offline, where we'll have only a single value.
            out.append("[");
            out.append(((RefTag)value).toJson(dictionary, null));
            out.append("]");
            if (termsInUse != null) termsInUse.addAll(((RefTag)value).getTermsInUse(dictionary));
        } else {
            out.append("\"\"");
        }
    }

    /**
     * NoThrow version of
     * @see #appendJsonLabelValue(LanguageDictionary, Appendable, Object, Set)
     * @param dictionary the current dictionary of nouns.
     * @param out the thing to write to
     * @param value the Label value, a String, List, or RefTag
     * @param termsInUse if not null, add the set of terms referenced in value to it
     */
    public static void appendJsonLabelValueNoThrow(LanguageDictionary dictionary, Appendable out, Object value, Set<GrammaticalTerm> termsInUse) {
        try {
            appendJsonLabelValue(dictionary, out, value, termsInUse);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
