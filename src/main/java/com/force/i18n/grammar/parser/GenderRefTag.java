/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.parser;

import java.util.*;

import com.force.i18n.Renameable;
import com.force.i18n.grammar.*;
import com.google.common.collect.Sets;

/**
 * Represents a gender choice to allow translators to place gendered version of text without needing
 * agreement for use in languages where the verb should match the gender of the noun, such as in
 * Arabic and Hebrew
 * @author stamm
 * @since 1.1
 */
public class GenderRefTag extends AdnominalRefTag {
	private static final long serialVersionUID = 1L;
	private final Map<LanguageGender,Object> when;
	private final Object ifDefault;

	GenderRefTag(NounRefTag nounTag, Map<LanguageGender,Object> when, Object ifDefault) {
		super(nounTag);
		this.when = when;
		this.ifDefault = ifDefault;

    }

    @Override
    public String getKey() {
        return "Gender:"+when;
    }

	Object getData(LanguageDictionary dictionary, Renameable[] entities) {
		if (getAssociatedNounRef() == null) {
			return ifDefault;
		}
		Noun n = getAssociatedNounRef().resolveNoun(dictionary, entities);
		if (n == null) {
			return ifDefault;
		}
		Object val = when.get(n.getGender());
		return val != null ? val : ifDefault;
	}

    @Override
    public String toString(LanguageDictionary formatter, boolean overrideForms, Object[] vals, Renameable... entities) {
    	Object data = getData(formatter, entities);
    	if (data == null) {
    		return "";
    	}
		return formatter.format(getData(formatter, entities), entities, vals, overrideForms, false);  // Don't format for message format, since it'll do it again later.
    }

    @Override
    public GenderRefTag cloneWithResolvedNounTag(NounRefTag nounTag) {
        return new GenderRefTag(nounTag, this.when, this.ifDefault);
    }

	@Override
	public Set<GrammaticalTerm> getTermsInUse(LanguageDictionary dictionary) {
		Set<GrammaticalTerm> whens = RefTag.getTermsFromLabels(dictionary, when.values());
		return ifDefault != null ? Sets.union(whens, RefTag.getTermsFromLabelValue(dictionary, ifDefault)) : whens;
	}

	@Override
	public String toJson(LanguageDictionary dictionary, List<?> list) {
		// Get associated noun ref
		Integer associatedNounIndex = null;
		for (int i = 0; i < list.size(); i++) {
			Object term = list.get(i);
			if (term != null && term.equals(getAssociatedNounRef())) {
				associatedNounIndex = i;
			}
		}

		StringBuilder def = new StringBuilder();
		RefTag.appendJsonLabelValueNoThrow(dictionary, def, ifDefault, null);
		StringBuilder forms = new StringBuilder();
		when.entrySet().stream().forEach(e->{forms.append("\""+e.getKey().getDbValue()+"\":");RefTag.appendJsonLabelValueNoThrow(dictionary, forms, e.getValue(), null);});
		return "{\"t\":\"g\",\"an\":"+associatedNounIndex.intValue()+",\"def\":"+def.toString()+",\"v\":{"+forms.toString()+"}}";
	}

}
