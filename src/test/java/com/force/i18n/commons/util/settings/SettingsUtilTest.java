/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.settings;

import com.force.i18n.commons.util.settings.SettingsUtil;

import junit.framework.TestCase;

/**
 *
 * Strict Unit tests for {@link SettingsUtil}
 *
 * @author asridhar
 */
public class SettingsUtilTest extends TestCase {
    public SettingsUtilTest(String name){
        super(name);
    }
    /**
     * Test whether sensitive keywords are correctly filtered as so
     */
    public void testIsSensitive() {
        final String SECTION_NAME = "Section";
        assertTrue("Expected keyword password mixed case to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "DomainPassword"));
        assertTrue("Expected keyword password lower case  to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "Domainpassword"));
        assertTrue("Expected keyword password at start to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "PasswordEntry"));
        assertTrue("Expected keyword password by itself to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "password"));
        assertTrue("Expected keyword password by itself mixed case to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "Password"));

        assertTrue("Expected keyword secret to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "DomainSecret"));
        assertTrue("Expected keyword key to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "DomainKey"));
        assertTrue("Expected keyword authenticationid to be considered sensitive",SettingsUtil.isSensitive(SECTION_NAME, "authenticationid"));

        assertFalse("Testing a non-sensitive word",SettingsUtil.isSensitive(SECTION_NAME, "Parameter"));
    }
}
