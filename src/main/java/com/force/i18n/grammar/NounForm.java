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
 * Represents the declension form of a noun.
 *
 * I.e. a collection of Number, Gender, Case, Article, and Possessive as a single
 * unit that forms the "key" in the String map.
 *
 * The implementation of a noun form is generally language specific.  It is *assumed*
 * that for simple languages, a Java enum will be used to represent the forms;
 * for more complex languages (more than 20 forms or so) a set of generated constants instead.
 *
 *
 *
 * @author stamm
 */
public interface NounForm extends GrammaticalForm {
    /**
     * @return the article (i.e. reference type) associated with this noun form
     *
     * TODO: This is usually wrong, but seems to be required for swedish
     */
    LanguageArticle getArticle();
    /**
     * @return the possessive associated with this noun form
     */
    LanguagePossessive getPossessive();
}
