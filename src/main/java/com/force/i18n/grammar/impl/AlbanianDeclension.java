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

package com.force.i18n.grammar.impl;

import java.util.EnumSet;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;

/**
 * Semisupported grammar for albanian.
 *
 * Albanian Grammar
 * Gender : M/F/N
 * Number: S/P
 * Case:
 * Nominative, Accusative, Genitive, Dative and Ablative. Ignoring Vocative
 * Articles: Yuck. We should ask for it declaratively like Swedish
 * Indefinite singular - një before the noun except in the genitive where there is a gender/number specific preposition i/e/të/së një before the një
 * Indefinite plural - standard case plural ending
 * Definite Singular - Definite article noun endings
 * Definite Plural - Definite article noun endings
 * Adjectives: Agree in gender and number but do not seem to need case. They are post positioned to the nouns.
 *
 * If upgraded to a non-platform language, you should simplify the modifiers by removing the case modifiers.
 *
 * @author stamm
 */
public class AlbanianDeclension extends GermanicDeclension {
    public AlbanianDeclension(HumanLanguage language) {
		super(language);
        assert language.getLocale().getLanguage().equals("sq") : "Initializing a language that isn't albanian";
	}

	@Override
    protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
        return ALL_ARTICLES;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);
    }


    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE, LanguageCase.GENITIVE, LanguageCase.DATIVE, LanguageCase.ABLATIVE);
    }

    @Override
    protected EnumSet<LanguageArticle> getRequiredNounArticles() {
        return ALL_ARTICLES;
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
    }

    @Override
    public LanguagePosition getDefaultAdjectivePosition() {
        return LanguagePosition.POST;
    }
}
