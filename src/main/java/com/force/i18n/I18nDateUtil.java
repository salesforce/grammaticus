/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Set of date utils reused throughout the Grammaticus.
 * Mostly for parsing timestamps and the like.
 * @author stamm
 */
public class I18nDateUtil {
	// Dateformats aren't threadsafe.
    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat result = new SimpleDateFormat(NLS_DATE_FORMAT);
            result.setTimeZone(BaseLocalizer.GMT_TZ);  // GMT is the one format to rule them all...
            return result;
        }
    };

    public static String formatTimestamp(Date ts) {
        return dateFormat.get().format(ts);
    }

    public static Date parseTimestamp_NoStupidExceptionRemap(String ts) throws java.text.ParseException {
        return dateFormat.get().parse(ts);
    }

    /** The Oracle NLS_DATE_FORMAT */
    public static final String NLS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date parseTimestamp(String ts) throws SQLException {
        try {
            return parseTimestamp_NoStupidExceptionRemap(ts);
        } catch (java.text.ParseException x) {
            throw new SQLException(x.toString());
        }
    }
}
