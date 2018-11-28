/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

/**
 * Contains all the various forms of adjectives in a declension
 * <p>
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
