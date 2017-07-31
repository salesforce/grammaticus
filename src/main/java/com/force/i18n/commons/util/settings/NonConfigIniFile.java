/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.settings;

import java.util.List;
import java.util.Map;

/**
 * A generic IniFile interface, usable by shared code, and implemented by the IniFile
 * on the app as well as the various IniFiles in shared.resource.  Methods in here
 * may throw unchecked LabelNotFoundException or SectionNotFoundExceptios.
 *
 * @author smawson
 */
public interface NonConfigIniFile extends BaseNonConfigIniFile {
    /**
     * Get the object stored in section/param in the IniFile.
     * Will return null if no object was found.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return the object, or null if none was found.
     */
    Object get(String section, String param);

    /**
     * Get a list of values stored in section/param in the IniFile.
     * Will return an empty list if no object was found.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return the list, possibly empty.
     */
    List<String> getList(String section, String param);

    /**
     * Get the value stored in section/param in the IniFile as a boolean.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return if the param was found, then: returns true if the text matches any of the true condition or
     *         false otherwise.  If no param was found, then an exception will potentially be thrown.
     */
    boolean getBoolean(String section, String param);

    /**
     * Gets the value stored in the section/param as a float.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return the float value
     */
    public float getFloat(String section, String param);

    /**
     * Get the value stored in section/param in the IniFile as an int.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return the integer value from the section/param
     * @throws NumberFormatException if the value wasn't a valid int.
     */
    int getInt(String section, String param);

    /**
     * Get the value stored in section/param in the IniFile as a string.
     * Will return null if it was not found.
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return the String value of the given section/param, or null.
     */
    String getString(String section, String param);

    /**
     * Get the value stored in section/param in the IniFile as a string.
     * Will throw exception if not found
     *
     * @param section the section to look in.
     * @param param the param name to look for.
     * @return the String value of the given section/param, or null.
     */
    String getStringThrow(String section, String param);

    /**
     * Returns all the values stored in the section.
     *
     * @param section the section to return.
     * @return A map of all the values in the section, or null if the section does not exist.
     */
    public Map<String, Object> getSection(String section);




}
