/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.io.Serializable;



/**
 * Implementation of noun form can be supported by a language that can be
 * used support the old incorrect grammar.
 * 
 * @author stamm
 */
public final class LegacyArticledNounForm implements NounForm, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final LanguageArticle article;
    private final NounForm baseNounForm;
    
    public LegacyArticledNounForm(NounForm baseNounForm, LanguageArticle article) {
        this.baseNounForm = baseNounForm;
        this.article = article;
    }

    public NounForm getBaseNounForm() { return this.baseNounForm; }
    @Override public LanguageArticle getArticle() { return this.article; }
    @Override public LanguageCase getCase() {  return getBaseNounForm().getCase(); }
    @Override public LanguageNumber getNumber() {  return getBaseNounForm().getNumber(); }
    @Override public LanguagePossessive getPossessive() { return getBaseNounForm().getPossessive();}
    @Override
    public String getKey() {
        return "LegacyNounForm-" + getArticle() + "-" + getBaseNounForm().getKey();
    }
    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public int hashCode() {
        return 31 * article.hashCode() + baseNounForm.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LegacyArticledNounForm)) return false;
        LegacyArticledNounForm other = (LegacyArticledNounForm)obj;
        return article == other.article && baseNounForm.equals(other.baseNounForm);
    }


     
    
}
