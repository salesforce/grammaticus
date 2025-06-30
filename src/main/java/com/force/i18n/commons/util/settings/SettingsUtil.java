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


/**
 * Class containing utilities useful for configuration classes.
 *
 * @author http://BenjaminTsai.com/
 * @author bzimmerman
 */
public class SettingsUtil {
    /**
     * Section keys that correspond to sensitive data which should be redacted in config.jsp
     */
    static final String[] SENSITIVE_KEYWORDS = {"password", "secret", "key", "authenticationid", "certificates", "token" };

    /**
     * @return true if the value is sensitive and should be censored, false otherwise.
     * @param section the section of the label
     * @param key the key of the label
     */
    public static boolean isSensitive(String section, String key) {
        key = key.toLowerCase();
        for (String s : SENSITIVE_KEYWORDS) {
            if (key.contains(s))
                return true;
        }
        return false;
    }

    /**
     * Blanks out the value if it determines the section/param is sensitive and should be censored
     *
     * @param section the section for the label
     * @param key the key for the label
     * @param value the value that should be returned if the value is sensitive
     * @return the value or some blanked out fake representation of the value if it is sensitive
     */
    public static String censorValue(String section, String key, String value) {
        boolean isPassword = isSensitive(section, key);

        if(isPassword) {
            return "xxxxxxxx";
        } else {
            return value;
        }
    }

    public static boolean isEnum(String paramName) {
        int underscoreIndex = paramName.lastIndexOf("_");
        if (underscoreIndex == -1) {
            return false;
        }
        try {
            Integer.parseInt(paramName.substring(underscoreIndex + 1));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
