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
