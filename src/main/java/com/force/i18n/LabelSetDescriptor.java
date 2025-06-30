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
