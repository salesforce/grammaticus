/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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
    static final String[] SENSITIVE_KEYWORDS = {"password", "secret", "key", "authenticationid", "certificates"};

    /**
     * @param section the section of the label
     * @param key     the key of the label
     * @return true if the value is sensitive and should be censored, false otherwise.
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
     * @param key     the key for the label
     * @param value   the value that should be returned if the value is sensitive
     * @return the value or some blanked out fake representation of the value if it is sensitive
     */
    public static String censorValue(String section, String key, String value) {
        boolean isPassword = isSensitive(section, key);

        if (isPassword) {
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
