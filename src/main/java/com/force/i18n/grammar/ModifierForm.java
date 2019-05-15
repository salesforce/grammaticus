/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import java.io.IOException;

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
    
    /**
     * Return a javascript string that will return the value of termFormVar with the gender or startsWith replaces
     * with the variable (if applicable for the declension).  This should return the form to use, so, by default it will
     * append termFormVar.  If you do anything it should probably use the appropriate substr
     * <tt>a.append(termFormVar).append(".substr(0,3)+").append(genderVar).append("+").append(termFormVar).append(".substr(4)");</tt>
     * 
     * NOTE: This should be a static method on the modifier form, but for simplicity of implementation we make it
     * an instance variable
     * 
     * @param termFormVar the variable containing the current form of the term (i.e. what's returned by {@link #getKey()}
     * @param genderVar the variable containing the dbvalue of gender of the noun if applicable in this declension 
     * @param startsWithVar the variable containing the dbvalue of the startsWith phoneme of the noun if applicable in this declension
     */
    default void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar) throws IOException {
    	// By default do nothing, since it doesn't need to be replaced.
    	a.append(termFormVar);
    }
}
