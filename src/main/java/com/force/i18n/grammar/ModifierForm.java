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
     * {@code a.append(termFormVar).append(".substr(0,3)+").append(genderVar).append("+").append(termFormVar).append(".substr(4)");}
     *
     * NOTE: This should be a static method on the modifier form, but for simplicity of implementation we make it
     * an instance variable
     *
     * @param a where the form should be written.
     * @param termFormVar the variable containing the current form of the term (i.e. what's returned by {@link #getKey()}
     * @param genderVar the variable containing the dbvalue of gender of the noun if applicable in this declension
     * @param startsWithVar the variable containing the dbvalue of the startsWith phoneme of the noun if applicable in this declension
     * @throws IOException if there is an issue writing to a
     */
    default void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar) throws IOException {
    	// By default do nothing, since it doesn't need to be replaced.
    	a.append(termFormVar);
    }
}
