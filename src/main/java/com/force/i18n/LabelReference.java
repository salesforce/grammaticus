/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

/**
 * A generic reference to a label that can be passed into a call to LC
 * @author stamm
 */
public interface LabelReference {
    /** @return the section in which the label is defined */
    String getSection();
    /** @return the key for the label */
    String getKey();
    /** @return the arguments for this label, or null if there are none */
    Object[] getArguments();
}
