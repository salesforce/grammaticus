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

/**
 * Implementation of noun form can be supported by a language that can be
 * used support the old incorrect grammar.
 *
 * @author stamm
 */
public final class LegacyArticledNounForm implements NounForm {
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
        return getArticle().getDbValue() + "~" + getBaseNounForm().getKey();
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
