/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.net.URL;
import java.util.List;

/**
 * Defines a label set, including the path of the root file and the set and ordering of files that override the root
 * file.
 *
 * @author rchoi
 */
public interface LabelSetDescriptor {
    public static final String LABELS_FILENAME = I18nJavaUtil.getProperty("rootLabelFile");
    public static final String DICTIONARY_FILENAME = I18nJavaUtil.getProperty("rootDictionaryFile");

    /**
     * @return the root file for the label set.
     */
    URL getRootDir();

    /**
     * @return the root file for the label set.
     */
    URL getRootFile();

    /**
     * @return true if the label set has files that override the root file.
     */
    boolean hasOverridingFiles();

    /**
     * @return a list of files that override the root file in the order that they should be processed.
     */
    List<URL> getOverridingFiles();
    
    /** 
     * @return true if the label files should be loaded separately instead of through labels.xml.
     */
    boolean hasModularizedFiles();
    
    /**
     * @return a list of files which should be loaded separately instead of through labels.xml
     */
    List<URL> getModularizedFiles();

}
