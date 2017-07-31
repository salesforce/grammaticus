/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

/**
 * @author shansma, stamm
 */
public interface LabelSetProvider {
    LabelSet getSet(HumanLanguage language);
    void resetMap();

    /**
     * Used to initialize non english labels only. initEnglish should be called before this method
     */
    void init();

    /**
     * @author sgeneix
     * Used to initialize english labels only. To initialize other languages, use init method
     */
    void initEnglish();
}
