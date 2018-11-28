/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

/**
 * Thrown when BaseParameterFile cannot find a section in the configuration system.
 *
 * @author btsai
 */
public class SettingsSectionNotFoundException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SettingsSectionNotFoundException(String msg) {
        super(msg);
    }
}
