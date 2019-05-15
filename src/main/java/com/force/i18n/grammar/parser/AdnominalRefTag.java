/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.parser;

/**
 * Admonimal = attached to or modifying a noun.  A noun-related tag that isn't a term stored in the
 * dictionary.
 * Not the best term, but this refers to both counter words and gender-related differences based on the
 * noun.
 * @author stamm
 * @since 1.1
 */
public abstract class AdnominalRefTag extends RefTag {
    private static final long serialVersionUID = 1L;
    private final NounRefTag associatedNounRef; // entity that this modifier associated with.

    public AdnominalRefTag(NounRefTag nounTag) {
        this.associatedNounRef = nounTag;
    }

    public NounRefTag getAssociatedNounRef() {
        return associatedNounRef;
    }

    public AdnominalRefTag getWithResolvedNounTag(NounRefTag nounTag) {
        if (this.getAssociatedNounRef() == null) {
            if (nounTag == null)
                return null; //error
            return cloneWithResolvedNounTag(nounTag);
        }
        return this;
    }

    protected abstract AdnominalRefTag cloneWithResolvedNounTag(NounRefTag nounTag);
}
