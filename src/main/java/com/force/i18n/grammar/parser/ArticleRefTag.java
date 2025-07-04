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

import java.util.List;

import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;

/**
 * Represents a fully qualified article reference.
 * @author stamm
 */
class ArticleRefTag extends ModifierRefTag {
    private static final long serialVersionUID = 1L;
    protected static final ConcurrentUniquefy<ArticleRefTag> tagMap = new ConcurrentUniquefy<>();

    private ArticleRefTag(String name, NounRefTag nounTag, TermRefTag nextTermTag, boolean isCapital, TermAttributes overrides) {
        super(name, nounTag, nextTermTag, isCapital, overrides);
    }

    @Override
    protected TermType getType() {
        return TermType.Article;
    }

    static ArticleRefTag getArticleRefTag(String name, NounRefTag entityTag, TermRefTag nextTermTag, boolean isCapital, TermAttributes overrides) {
        assert name != null;
        return tagMap.unique(new ArticleRefTag(name, entityTag, nextTermTag, isCapital, overrides));
    }

    @Override
    ArticleRefTag getNewModifierRef(NounRefTag entity, TermRefTag nextTermTag, LanguageArticle ignored) {
        return getArticleRefTag(getName(), entity, nextTermTag, isCapital(), getDeclensionOverrides());
    }


    @Override
    protected ModifierForm getForm(LanguageDictionary formatter, NounForm associatedNounForm, Noun noun, GrammaticalTerm term) {
        return getDeclensionOverrides().getArticleForm(formatter.getDeclension(), noun, associatedNounForm, term);
    }

    @Override
    protected NounModifier resolveModifier(LanguageDictionary dictionary) {
        return dictionary.getArticle(getName());
    }

    @Override
    public String toJson(LanguageDictionary dictionary, List<?> list) {
        // Fallback labels can have articles, but they should be ignored
        if (!dictionary.getDeclension().hasArticle()) {
            return "\"\"";
        }
        return super.toJson(dictionary, list);
    }

    @Override
    ArticleRefTag unique() {
        return tagMap.unique(this);
    }
}