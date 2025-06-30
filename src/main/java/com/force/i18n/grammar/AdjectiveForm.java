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
 * Contains all the various forms of adjectives in a declension
 *
 * TODO: Article doesn't do the right thing right now.  In fact, article should be made it's own "form"
 * TODO: Adjective needs to have a "starts with" so that the article agreement is based on the starts
 * with of the adjective, but the number/gender of the noun.
 *
 * @author stamm
 */
public interface AdjectiveForm extends ModifierForm {
    /**
     * @return the article (i.e. reference type) associated with this adjective form
     */
    LanguageArticle getArticle();

    /**
     * @return the possessive associated with the adjective.
     */
    LanguagePossessive getPossessive();
}
