/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Reference to a specific label in labels.xml (section and key).  Also allows storage
 * of optional arguments.  This is nice if you want to store static references.
 * <p>
 * For smaller projects, you could auto-generate these from the label files kind of like
 * what eclipse does.  For larger projects you end up killing PermGen
 * <p>
 * Called LabelRef to save on typing
 *
 * @author nhorne
 */
public class LabelRef implements Comparable<LabelRef>, LabelReference, Serializable {
    private static final long serialVersionUID = 1L;
    private final String section;
    private final String key;
    private final Object[] args;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Construct a Labelref for the given section, key, and optional arguments
     *
     * @param section the section for the label
     * @param key     the key for the label
     * @param args    optional arguments that should be used in message format replacement
     */
    public LabelRef(String section, String key, Object... args) {
        assert section != null && key != null;
        this.section = section;
        this.key = key;
        this.args = args;
    }

    /**
     * Construct a Labelref for the given section and key
     *
     * @param section the section for the label
     * @param key     the key for the label
     */
    public LabelRef(String section, String key) {
        this(section, key, EMPTY_OBJECT_ARRAY);
    }

    @Override
    public String getSection() {
        return this.section;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Object[] getArguments() {
        return this.args;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (this.section == null ? 0 : this.section.hashCode());
        hashCode = 31 * hashCode + (this.key == null ? 0 : this.key.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LabelRef) {
            LabelRef l = (LabelRef) obj;
            return this.section.equals(l.section) && this.key.equals(l.key);
        }
        return false;
    }

    @Override
    public int compareTo(LabelRef o) {
        int sectionComparison = this.section.compareTo(o.section);
        if (sectionComparison == 0) {
            return this.key.compareTo(o.key);
        } else {
            return sectionComparison;
        }
    }

    @Override
    public String toString() {
        return this.section + "." + this.key;
    }

    public static LabelRef getInstance(String dotted) {
        List<String> split = Lists.newArrayList(Splitter.on('.').split(dotted));
        if (split.size() != 2) throw new IllegalArgumentException("Invalid label reference: " + dotted);
        return new LabelRef(split.get(0), split.get(1));
    }
}
