/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

/**
 * Represents the different form provided for the declension of modifiers.
 *
 * @author stamm
 */
public interface ModifierForm extends GrammaticalForm {
    /**
     * @return the startwith associated with this adjective form
     */
    LanguageStartsWith getStartsWith();

    /**
     * @return the gender associated with this adjective form
     */
    LanguageGender getGender();
}
