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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.force.i18n.commons.text.TextUtil;


/**
 * Refers to a noun modifier; usually a pronoun, adjective, or article.
 *
 * Contains a "position" reference wrt a given noun.
 *
 * @author stamm
 */
public abstract class NounModifier extends GrammaticalTerm {
    private static final long serialVersionUID = 1L;

    protected NounModifier(LanguageDeclension declension, String name) {
        super(declension, name);
    }

    private boolean isCopiedFromDefault = false;

    @Override
    public boolean isCopiedFromDefault() {
        return isCopiedFromDefault;
    }

    void setInheritedFromDefault(boolean value) {
        this.isCopiedFromDefault = value;
    }

    public abstract Map<? extends ModifierForm, String> getAllValues();

    /**
     * @param form the form to return
     * @return the defined string for this modifier for the given form, or {@code null} if there is no string for that form
     */
    public abstract String getString(ModifierForm form);

    /**
     * @return the position of this noun modifier WRT the noun
     */
    public abstract LanguagePosition getPosition();

    /**
     * @return the starts with associated with this noun modifier
     */
    @Override
    public LanguageStartsWith getStartsWith() {
        return getDeclension().getDefaultStartsWith();
    }

    /**
     * @return an arbitrary but deterministic value for a specific form for this object.
     */
    public abstract String getDefaultValue();

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getStartsWith(), getPosition(), getAllValues());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof NounModifier) {
            NounModifier nm = (NounModifier)obj;
            return compareTo(nm) == 0
                    && getStartsWith() == nm.getStartsWith()
                    && getPosition() == nm.getPosition()
                    && nm.getAllValues().equals(getAllValues());
        }
        return false;
    }

    @Override
	public void toJson(Appendable appendable) throws IOException {
        appendable.append("{\"t\":\"" + getTermType().getCharId() + "\",\"l\":\"");
        appendable.append(getName());
        appendable.append("\",");
        if (getDeclension().hasStartsWith() && getStartsWith() != null) {
            appendable.append("\"s\":\"").append(getStartsWith().getDbValue()).append("\",");
        }
        appendable.append("\"v\":{");
        appendable.append(new TreeMap<>(getAllValues()).entrySet().stream().map(e->"\""+e.getKey().getKey()+"\":\""+TextUtil.escapeForJsonString(e.getValue())+"\"").collect(Collectors.joining(",")));
        appendable.append("}}");
    }
}
