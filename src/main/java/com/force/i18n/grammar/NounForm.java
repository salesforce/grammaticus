/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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

    /**
     * @return a HTML compatible screen that can be used to represent this noun
     * form uniquely when compared to all other noun forms.
     */
    @Override
    String getKey();
}
