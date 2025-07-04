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

import java.util.*;
import java.util.Map.Entry;


/**
 * A facade interface to pull out the methods from IniFile which will not throw any
 * unchecked LabelNotFound or SectionNotFound exceptions.  This facade is useful as
 * there are some classes in Platform project which need to access some basic
 * IniFile functionality.
 *
 * @author http://BenjaminTsai.com/
 */
public interface BaseNonConfigIniFile {
    /**
     * Get the object stored in section/param in the IniFile.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the object to return if no value was found.
     * @return the object, or ifNull if none was found.
     */
    Object get(String section, String param, Object ifNull);

    /**
     * Get the list of values stored in section/param in the IniFile.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the list to return if no list was found.
     * @return the list, or ifNull if the list was empty or null.
     */
    List<String> getList(String section, String param, List<String> ifNull);

    /**
     * Get the value stored in section/param in the IniFile as a boolean.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the boolean to default to.
     * @return the boolean value that was found, or ifNull if none was found.
     */
    boolean getBoolean(String section, String param, boolean ifNull);

    /**
     * Gets the value stored in the section/param in the IniFile as a float
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the float to default to
     * @return the float value that was found, or ifNull if none was found
     */
    float getFloat(String section, String param, float ifNull);

    /**
     * Get the value stored in section/param in the IniFile as an int.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the int to default to.
     * @return the integer value from the section/param, or ifNull if it was not found
     */
    int getInt(String section, String param, int ifNull);

    /**
     * Get the value stored in section/param in the IniFile as a string - may censor the
     * value if necessary.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the default value to use.
     * @return the String value of the given section/param, or ifNull, or something else if should be censored
     */
    String getCensoredString(String section, String param, String ifNull);

    /**
     * Get the value stored in section/param in the IniFile as a string.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @param ifNull the default value to use.
     * @return the String value of the given section/param, or ifNull.
     */
    String getString(String section, String param, String ifNull);

    /**
     * @return all the values stored in all the section.
     */
    public Set<Entry<String, Map<String, Object>>> entrySet();
}
