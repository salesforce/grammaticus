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

package com.force.i18n.commons.util.settings;

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
        assertTrue("Expected keyword token to be considered sensitive", SettingsUtil.isSensitive(SECTION_NAME, "token"));
        assertTrue("Expected keyword token to be considered sensitive", SettingsUtil.isSensitive(SECTION_NAME, "AccessToken"));
        assertTrue("Expected keyword token to be considered sensitive", SettingsUtil.isSensitive(SECTION_NAME, "DomainAccounttoken"));

        assertFalse("Testing a non-sensitive word",SettingsUtil.isSensitive(SECTION_NAME, "Parameter"));
        assertFalse("Testing a non-sensitive word",SettingsUtil.isSensitive(SECTION_NAME, "StatusTokeep"));
    }
}
