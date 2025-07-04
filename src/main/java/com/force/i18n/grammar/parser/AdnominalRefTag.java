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

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), associatedNounRef == null ? 0 : associatedNounRef.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return Objects.equals(associatedNounRef, ((AdnominalRefTag)obj).associatedNounRef);
    }

    protected abstract AdnominalRefTag cloneWithResolvedNounTag(NounRefTag nounTag);
}
