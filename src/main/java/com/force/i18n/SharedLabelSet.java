/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import com.force.i18n.commons.util.settings.NonConfigIniFile;

/**
 * This interface describes a set of labels available through localization.
 */
public interface SharedLabelSet extends NonConfigIniFile {

    // Pulled out of property file because all LabelSets (not just extensions of property file) need this.
    public boolean labelExists(String section, String param);
}
