/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LabelSetProvider;

/**
 * A label set provider that returns a grammatical label set.
 * Uses fancy java 1.5 covariance
 *
 * @author stamm
 */
public interface GrammaticalLabelSetProvider extends LabelSetProvider {
    @Override
    GrammaticalLabelSet getSet(HumanLanguage language);
}
