/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.io.IOException;
import java.util.Map;
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
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NounModifier(LanguageDeclension declension, String name) {
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
     * @return the defined string for this modifier for the given form, or <tt>null</tt> if there is no string for that form
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
	public void toJson(Appendable appendable) throws IOException {
    	appendable.append("{\"t\":\""+getTermType().getCharId()+"\",\"l\":\"");
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
