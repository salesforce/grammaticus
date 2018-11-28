/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.*;
import java.util.*;

import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.commons.util.collection.LruCache;
import com.google.common.collect.ImmutableSet;

/**
 * An internationalization and localization utility class
 * Handles locale specific parsing and formatting
 *
 * @author pnakada
 * @author smawson
 */
public class BaseLocalizer {
    /**
     * Simple enum + convenience methods for asking Date questions in Local or GMT.
     */
    @SuppressWarnings("hiding")
    public enum LocalOrGmt {
        LOCAL, GMT
    }

    public static final LocalOrGmt LOCAL = LocalOrGmt.LOCAL;
    public static final LocalOrGmt GMT = LocalOrGmt.GMT;

    public static final TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");

    public static final Date EARLIEST;
    public static final Date LATEST;
    public static final Date OLD_EARLIEST;
    public static final Date OLD_LATEST;

    static {
        Calendar c = Calendar.getInstance();
        c.set(1700, Calendar.JANUARY, 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);

        EARLIEST = c.getTime();
        c.set(4000, Calendar.DECEMBER, 31, 0, 0, 0);
        LATEST = c.getTime();
        c.set(1700, 1, 1, 0, 0, 0);
        OLD_EARLIEST = c.getTime();
        c.set(4001, Calendar.JANUARY, 31, 0, 0, 0);
        OLD_LATEST = c.getTime();
    }

    private final Locale locale;
    private final Locale currencyLocale;
    private final HumanLanguage language;
    private final TimeZone timeZone;

    protected SharedLabelSet labelSet;

    /**
     * these elements aren't initialized on construction.  They are created once
     * the first time they are accessed
     */
    protected DateFormat gmtDateFormat;
    protected DateFormat localDateFormat;
    protected DateFormat localLongDateFormat;
    protected DateFormat gmtTimeFormat;
    protected DateFormat localTimeFormat;
    protected DateFormat dateTimeFormat;
    protected DateFormat longDateTimeFormat;
    protected DateFormat inputGmtDateFormat;
    protected DateFormat inputLocalDateFormat;
    protected DateFormat inputDateTimeFormat;
    protected NumberFormat numberFormat;
    protected NumberFormat currencyFormat;
    protected final LruCache<String, MessageFormat> messageFormatCache = new LruCache<>(10);

    public static final String ENGLISH_LANGUAGE = "en";
    public static final String KOREAN_LANGUAGE = "ko";
    public static final String JAPANESE_LANGUAGE = "ja";
    public static final String CHINESE_LANGUAGE = "zh";

    /**
     * OLD EXPLANATION
     * DVM - This is a workaround of Sun bug 4842276 (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4842276)
     * which incorrectly changed the date format for Danish.  Since it will be very hard to get Sun to undo this
     * "fix" and certainly won't happen in a timely fashion, we're going to hack instead.  Ugh.
     * <p>
     * NEW EXPLANATION:
     * The JDK fixed the Danish bug in JDK 6. (see above)
     * However, they have other bugs, mostly around the english date format.  One solution would be to implement
     * a DateFormatProvider to override the date format, and that's what will probably happen with Kyrgyz and Kazakh
     * but until then, it's easier to keep this hack around.
     * <p>
     * Singapore is the proximate cause of this; the FormatData_en_SG doesn't override the DateTimeElements.
     * http://www.java2s.com/Open-Source/Java-Document/6.0-JDK-Modules-sun/text/sun.text.resources.htm
     * http://www.java2s.com/Open-Source/Java-Document/6.0-JDK-Modules-sun/text/sun/text/resources/FormatData_en_SG.java.htm
     * <p>
     * The date format for Latvian locale is incorrect.  The format retuned by JDK is YYYY.dd.mm, the correct one is dd.mm.yyyy
     * <p>
     * Other unsupported JDK locales will use the english date format (which is a good way to figure out that they are unsupported)
     **/
    private static class JdkDateFormatFixer {

        private static final String BRITISH_DATE_SHORT_FORMAT = "dd/MM/yy";  // Much more proper, eh
        private static final String BRITISH_DATE_MEDIUM_FORMAT = "dd/MM/yyyy";
        private static final String LATVIA_DATE_SHORT_FORMAT = "dd.MM.yy";
        private static final String LATVIA_DATE_MEDIUM_FORMAT = "dd.MM.yyyy";
        private static final String LATVIA = "LV";

        private static Set<String> ENGLISH_OVERRIDE_COUNTRIES = ImmutableSet.of("SG", "NG", "MY", "BB", "BM", "GH", "ID");

        // This also applies to singapore and india.
        private static boolean shouldFixJdkDateBug(Locale locale) {
            if (locale == null) return false;
            String lang = locale.getLanguage();
            if (LATVIA.equals(locale.getCountry())) return true;
            if (ENGLISH_LANGUAGE.equals(lang) && ENGLISH_OVERRIDE_COUNTRIES.contains(locale.getCountry())) return true;
            return false;
        }

        public static DateFormat getDateInstance(int style, Locale aLocale) {
            if (shouldFixJdkDateBug(aLocale)) {
                String dateFormat;
                switch (style) {
                    case DateFormat.SHORT:
                        dateFormat = LATVIA.equals(aLocale.getCountry()) ? LATVIA_DATE_SHORT_FORMAT : BRITISH_DATE_SHORT_FORMAT;
                        return new SimpleDateFormat(dateFormat, aLocale);
                    case DateFormat.MEDIUM:
                        dateFormat = LATVIA.equals(aLocale.getCountry()) ? LATVIA_DATE_MEDIUM_FORMAT : BRITISH_DATE_MEDIUM_FORMAT;
                        return new SimpleDateFormat(dateFormat, aLocale);
                    default:
                        return DateFormat.getDateInstance(style, aLocale);
                }
            } else {
                return DateFormat.getDateInstance(style, aLocale);
            }
        }

        public static DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
            if (shouldFixJdkDateBug(aLocale)) {
                if (dateStyle == DateFormat.SHORT || dateStyle == DateFormat.MEDIUM) {
                    String dateFormat = (dateStyle == DateFormat.SHORT) ? (LATVIA.equals(aLocale.getCountry()) ? LATVIA_DATE_SHORT_FORMAT : BRITISH_DATE_SHORT_FORMAT) :
                            (LATVIA.equals(aLocale.getCountry()) ? LATVIA_DATE_MEDIUM_FORMAT : BRITISH_DATE_MEDIUM_FORMAT);
                    switch (timeStyle) {
                        case DateFormat.SHORT:
                            return new SimpleDateFormat(dateFormat + " HH:mm", aLocale);
                        case DateFormat.MEDIUM:
                            return new SimpleDateFormat(dateFormat + " HH:mm:ss", aLocale);
                        default:
                            return new SimpleDateFormat(dateFormat + " HH:mm:ss z", aLocale);
                    }
                }
                return checkAM(DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale), aLocale);
            } else {
                return checkAM(DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale), aLocale);
            }
        }
    }

    /**
     * This constructor is used to create a BaseLocalizer
     *
     * @param locale         is the user's locale data
     * @param currencyLocale is org's currency locale ,
     */
    public BaseLocalizer(Locale locale, Locale currencyLocale, TimeZone timeZone, HumanLanguage language,
                         SharedLabelSet labelSet) {
        this.locale = locale;
        this.currencyLocale = currencyLocale != null ? currencyLocale : locale;
        this.timeZone = timeZone;
        this.language = language;

        this.labelSet = labelSet;
    }

    public SharedLabelSet getLabelSet() {
        return this.labelSet;
    }

    public Calendar getCalendar(LocalOrGmt tz) {
        if (tz == LOCAL) {
            return Calendar.getInstance(this.timeZone, this.locale);
        } else {
            return Calendar.getInstance(GMT_TZ, this.locale);
        }
    }

    public Calendar getCalendar(TimeZone tz) {
        return Calendar.getInstance(tz, this.locale);
    }

    public Calendar getCalendar(Locale l) {
        return Calendar.getInstance(this.timeZone, l);
    }

    /*
     * DATE INPUT
     */

    /**
     * Parses a date-only input string.  Caller specified time zone.
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     */
    public Date parseDate(String input, LocalOrGmt tz) throws ParseException {
        return BaseLocalizer.doParseDate(input, getInputDateFormat(tz));
    }

    /**
     * Parses a date-time input string.  Local time zone.
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     */
    public Date parseDateTime(String input) throws ParseException {
        return BaseLocalizer.doParseDate(input, getInputDateTimeFormat());
    }

    /**
     * Get date-only DateFormat for input.  Caller specified time zone.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.
     */
    public DateFormat getInputDateFormat(LocalOrGmt tz) {
        if (tz == LOCAL) {
            if (this.inputLocalDateFormat == null) {
                this.inputLocalDateFormat = BaseLocalizer.getLocaleInputDateFormat(this.locale, this.timeZone);
            }
            return this.inputLocalDateFormat;
        } else {
            if (this.inputGmtDateFormat == null) {
                this.inputGmtDateFormat = BaseLocalizer.getLocaleInputDateFormat(this.locale, GMT_TZ);
            }
            return this.inputGmtDateFormat;
        }
    }

    /**
     * Static method to get date-only DateFormat for input.  This is based on a 2 digit year
     * input mask, which also handles 4-digit year, but caller must use doParseDate() to
     * handle single-digit years, out-of-bounds years, and trailing garbage in input string.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     */
    public static DateFormat getLocaleInputDateFormat(Locale locale, TimeZone tz) {
        DateFormat df = JdkDateFormatFixer.getDateInstance(DateFormat.SHORT, locale);

        df.setLenient(false);
        df.setTimeZone(tz);
        Calendar calendar = df.getCalendar();
        calendar.set(1959, Calendar.DECEMBER, 31, 23, 0); // 60 means 1960, 59 means 2059; handle potential daylight saving difference
        ((SimpleDateFormat) df).set2DigitYearStart(calendar.getTime());
        return df;
    }

    public static boolean isLanguageEnglish(Locale locale) {
        return ENGLISH_LANGUAGE.equals(locale.getLanguage());
    }

    public static boolean isLanguageJapanese(Locale locale) {
        return JAPANESE_LANGUAGE.equals(locale.getLanguage());
    }

    /**
     * Get date-time DateFormat for input.  Locale time zone.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.
     */
    public DateFormat getInputDateTimeFormat() {
        if (this.inputDateTimeFormat == null) {
            this.inputDateTimeFormat = BaseLocalizer.getLocaleInputDateTimeFormat(this.locale, this.timeZone);
        }
        return this.inputDateTimeFormat;
    }

    /**
     * Static method to get date-time DateFormat for input.  This is based on a 2 digit year
     * input mask, which also handles 4-digit year, but caller must use doParseDate() to
     * handle single-digit years, out-of-bounds years, and trailing garbage in input string.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     */
    public static DateFormat getLocaleInputDateTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = JdkDateFormatFixer.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);

        df.setLenient(false);
        df.setTimeZone(tz);
        Calendar calendar = df.getCalendar();
        calendar.set(1959, Calendar.DECEMBER, 31, 23, 0); // 60 means 1960, 59 means 2059
        ((SimpleDateFormat) df).set2DigitYearStart(calendar.getTime());
        return df;
    }

    /**
     * Parse the given input string using the given format, and make sure the entire string
     * is used up during the parsing and garbage text at the end is not allowed.  Also
     * handles single-digit years and verify out-of-bounds years.  All date parsing in
     * the app must go through this.
     */
    public static Date doParseDate(String input, DateFormat df) throws ParseException {
        Date date = doParseTime(input, df);
        // Handles dates that are entered with one or two digit years
        Calendar cal = df.getCalendar();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        if (year >= 0 && year < 60) {
            cal.set(Calendar.YEAR, 2000 + year);
        } else if (year >= 60 && year < 100) {
            cal.set(Calendar.YEAR, 1900 + year);
        }

        // Verify out-of-bounds years
        date = cal.getTime();
        if (date.before(EARLIEST) || date.after(LATEST)) {
            throw new ParseException("Invalid year", 0);
        }
        return date;
    }

    /**
     * parse a time (or date really), but you should use doParseDate for dates as it does some extra stuff
     */
    public static Date doParseTime(String input, DateFormat tf) throws ParseException {
        if (input == null) {
            throw new ParseException("Input date is null", 0);
        }
        // Parse the input string
        ParsePosition pp = new ParsePosition(0);
        Date time = tf.parse(input, pp);
        if (time == null || pp.getIndex() == 0) {
            throw new ParseException("Unparseable date: \"" + input + "\"", pp.getErrorIndex());
        }
        // Make sure the entire string is used up
        if (pp.getIndex() < input.length()) {
            throw new ParseException("Unparseable date: \"" + input + "\"", pp.getIndex());
        }
        return time;
    }

    /*
     * DATE OUTPUT
     */

    /**
     * Formats a date-only Date.  Caller specified time zone.
     */
    public String formatDate(Date date, LocalOrGmt tz) {
        return (date == null) ? null : getDateFormat(tz).format(date);
    }

    /**
     * Formats a date-time Date.  Local time zone.
     */
    public String formatDateTime(Date date) {
        return (date == null) ? null : getDateTimeFormat().format(date);
    }

    /**
     * Get date-only DateFormat for output.  Uses short date format, 4-digit year.
     * Caller specified time zone.
     */
    public DateFormat getDateFormat(LocalOrGmt tz) {
        if (tz == LOCAL) {
            if (this.localDateFormat == null) {
                this.localDateFormat = BaseLocalizer.getLocaleDateFormat(this.locale, this.timeZone);
            }
            return this.localDateFormat;
        } else {
            if (this.gmtDateFormat == null) {
                this.gmtDateFormat = BaseLocalizer.getLocaleDateFormat(this.locale, GMT_TZ);
            }
            return this.gmtDateFormat;
        }
    }

    /**
     * Static method to get date-only DateFormat for output.  Uses short date format, 4-digit year.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     */
    public static DateFormat getLocaleDateFormat(Locale locale, TimeZone tz) {
        DateFormat df = BaseLocalizer.convertTo4DigitYear(JdkDateFormatFixer.getDateInstance(DateFormat.SHORT,
                locale), locale);
        df.setLenient(false);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * Get date-only DateFormat for output.  Uses long date format, 4-digit year.
     * Local time zone.
     */
    public DateFormat getLongDateFormat() {
        if (this.localLongDateFormat == null) {
            this.localLongDateFormat = BaseLocalizer.convertTo4DigitYear(JdkDateFormatFixer.getDateInstance(
                    DateFormat.LONG, this.locale), this.locale);
            this.localLongDateFormat.setLenient(false);
            this.localLongDateFormat.setTimeZone(this.timeZone);
        }
        return this.localLongDateFormat;
    }

    /**
     * Get time-only DateFormat for output.  Uses short time format.
     * Local time zone.
     */
    public DateFormat getTimeFormat() {
        return getTimeFormatHelper(this.localTimeFormat, this.timeZone);
    }

    /**
     * Get time-only DateFormat for output.  Uses short time format.
     * GMT time zone.
     */
    public DateFormat getGmtTimeFormat() {
        return getTimeFormatHelper(this.gmtTimeFormat, GMT_TZ);
    }

    private DateFormat getTimeFormatHelper(DateFormat timeFormat, TimeZone timeZone) {
        if (timeFormat != null)
            return timeFormat; // In case the protected variables are set, just used the cached values

        DateFormat newTimeFormat = checkAM(DateFormat.getTimeInstance(DateFormat.SHORT, this.locale));
        // Explicitly setting to true. This is a fix for Timezones where the certain time doesn't exist.
        // Example: America/Mazatlan, 12:00 AM. Try parsing this with a formatter of h:mm a.
        newTimeFormat.setLenient(true);
        newTimeFormat.setTimeZone(timeZone);
        return newTimeFormat;
    }

    /**
     * Get date-time DateFormat for output.  Uses short date and time formats, 4-digit year.
     * Local time zone.
     */
    public DateFormat getDateTimeFormat() {
        if (this.dateTimeFormat == null) {
            this.dateTimeFormat = BaseLocalizer.getLocaleDateTimeFormat(this.locale, this.timeZone);
        }
        return this.dateTimeFormat;
    }

    /**
     * Static method to get date-time DateFormat for output.  Uses short date and time formats, 4-digit year.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     */
    public static DateFormat getLocaleDateTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = BaseLocalizer.convertTo4DigitYear(JdkDateFormatFixer.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, locale), locale);
        df.setLenient(false);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * This form of getLocalDateTimeFormat is used to render times in a specific locale. Used to send event notification emails
     */
    public static DateFormat getLocaleTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = checkAM(BaseLocalizer.convertTo4DigitYear(DateFormat.getTimeInstance(DateFormat.SHORT, locale),
                locale), locale);
        df.setLenient(false);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * Get date-time DateFormat for output.  Uses short date and and long time formats, 4-digit year.
     * Local time zone.
     */
    public DateFormat getLongDateTimeFormat() {
        if (this.longDateTimeFormat == null) {
            this.longDateTimeFormat = BaseLocalizer.convertTo4DigitYear(JdkDateFormatFixer.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.LONG, this.locale), this.locale);
            this.longDateTimeFormat.setLenient(false);
            this.longDateTimeFormat.setTimeZone(this.timeZone);
        }
        return this.longDateTimeFormat;
    }

    /**
     * convertTo4DigitYear extracts the date format string from a dateformat
     * and substitutes a 4 digit year for any two digit year format strings.
     */
    private static DateFormat convertTo4DigitYear(DateFormat dateFormat, Locale locale) {
        String pattern = ((SimpleDateFormat) dateFormat).toPattern();

        // if there's a 4 digit year already, skip it
        if (pattern.indexOf("yyyy") == -1) {

            // find a 2 digit year string
            int loc = pattern.indexOf("yy");
            if (loc != -1) {
                pattern = pattern.substring(0, loc) + "yy" + pattern.substring(loc);
            }
        }
        return new SimpleDateFormat(pattern, locale);
    }

    /**
     * The South African localle returns a time format of the form 'hh:mm' making it impossible to differentiate AM/PM
     *
     * @return a dateformat with either a 24-hour clock or the necessary AM/PM identifier attached
     */
    private DateFormat checkAM(DateFormat dateFormat) {
        return checkAM(dateFormat, this.locale);
    }

    /**
     * The South African locale returns a time format of the form 'hh:mm' making it impossible to differentiate AM/PM
     *
     * @return a dateformat with either a 24-hour clock or the necessary AM/PM identifier attached
     */
    private static DateFormat checkAM(DateFormat dateFormat, Locale l) {
        DateFormat df = dateFormat;
        String p = ((SimpleDateFormat) dateFormat).toPattern();
        if ((p.indexOf('a') == -1) && (p.indexOf('k') == -1) && (p.indexOf('H') == -1)) {
            df = new SimpleDateFormat(p + " a", l);
        }
        return df;
    }

    /*
     * NUMBER INPUT/OUTPUT
     */

    /**
     * Parse the string as a number.
     *
     * @param s the string to be parsed
     * @return a number object representing the parsed number
     * @throws ParseException if the string cannot be parsed
     */
    public Number parseNumber(String s) throws ParseException {
        return BaseLocalizer.doParseNumber(s, getNumberFormat());
    }

    /**
     * Parse the string as percentage.
     *
     * @param s the string to be parsed
     * @return a number object representing the parsed percentage
     * @throws ParseException if the string cannot be parsed
     */
    public Number parsePercent(String s) throws ParseException {
        try {
            Number val = BaseLocalizer.doParseNumber(s, getPercentFormat());
            return BigDecimal.valueOf(val.doubleValue()).multiply(BigDecimal.valueOf(100));
        } catch (ParseException x) {
            return BaseLocalizer.doParseNumber(s, getNumberFormat());
        }
    }

    /**
     * Parse the string as currency.
     *
     * @param s the string to be parsed
     * @return a number object representing the parsed currency
     * @throws ParseException if the string cannot be parsed
     */
    public Number parseCurrency(String s) throws ParseException {
        try {
            return BaseLocalizer.doParseNumber(s, getCurrencyFormat());
        } catch (ParseException x) {
            return BaseLocalizer.doParseNumber(s, getNumberFormat());
        }
    }

    /**
     * Gets a NumberFormat, using the default settings for the Locale.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     */
    public NumberFormat getNumberFormat() {
        if (this.numberFormat == null) {
            this.numberFormat = NumberFormat.getNumberInstance(this.locale);
        }
        return numberFormat;
    }

    /**
     * Return the number format for the locale with the  given precision and scale
     *
     * @param precision the precision of the number to be displayed
     * @param scale     the scale to be displayed
     * @return a number format with the given scale
     */
    public NumberFormat getNumberFormat(int precision, int scale) {
        //TODO: anyone - the "precision" parameter is not used by this method => remove the parameter or implement it.
        return getNumberFormat(scale, false);
    }

    /**
     * Gets a NumberFormat, using the given precision and scale settings.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     *
     * @param scale number of digits to the right of the decimal that will be shown
     */
    public NumberFormat getNumberFormat(int scale, boolean scaleSpecified) {
        // don't use the cached NumberFormat because we are altering it
        NumberFormat nf = NumberFormat.getNumberInstance(this.locale);

        // We handle numbers longer than their precision with separate logic...
        // If we make the following call, it does truncation and rounding on the integer part which we do not desire.
        // But we DO want that effect on the fractional part of the number.
        // nf.setMaximumIntegerDigits(precision-scale);
        nf.setMinimumFractionDigits(scale);
        if (scaleSpecified)
            nf.setMaximumFractionDigits(scale);
        //Changing the rounding mode to HALF_UP for all number type
        nf.setRoundingMode(RoundingMode.HALF_UP);
        return nf;
    }

    /**
     * Gets a NumberFormat for currency, using the default settings for the Locale.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     */
    public NumberFormat getCurrencyFormat() {
        return getCurrencyFormat(this.currencyLocale);
    }

    protected NumberFormat getCurrencyFormat(Locale currLocale) {
        if (this.currencyFormat == null) {
            this.currencyFormat = NumberFormat.getCurrencyInstance(currLocale);
        }
        return currencyFormat;
    }

    /**
     * Gets a NumberFormat for currency, using the given precision and scale settings.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     *
     * @param scale number of digits to the right of the decimal that will be shown
     */
    public NumberFormat getCurrencyFormat(int scale) {
        return getCurrencyFormat(this.currencyLocale, scale);
    }

    protected NumberFormat getCurrencyFormat(Locale currLocale, int scale) {
        // don't use the cached NumberFormat because we are altering it
        NumberFormat cf = NumberFormat.getCurrencyInstance(currLocale);

        // We handle numbers longer than their precision with separate logic...
        // If we make the following call, it does truncation and rounding on the integer part which we do not desire.
        // But we DO want that effect on the fractional part of the number.
        // cf.setMaximumIntegerDigits(precision-scale);
        cf.setMinimumFractionDigits(scale);
        cf.setMaximumFractionDigits(scale);
        //Changing the rounding mode to Half_UP to be consistent throughout the app
        cf.setRoundingMode(RoundingMode.HALF_UP);
        return cf;
    }


    /**
     * Gets a NumberFormat for percentage, using the default settings for the Locale.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     */
    public NumberFormat getPercentFormat() {
        /* this was removed to work around a problem in the Decimal format
         object.

         http://developer.java.sun.com/developer/bugParade/bugs/4252562.html
         return (NumberFormat)data.percentFormat.clone();
         */
        return NumberFormat.getPercentInstance(locale);
    }

    /**
     * Parse the given input string using the given format, and make sure the entire string
     * is used up during the parsing and garbage text at the end is not allowed.
     */
    public static Number doParseNumber(String input, NumberFormat nf) throws ParseException {
        if (input == null) {
            throw new ParseException("Input number is null", 0);
        }

        // Parse the input string
        ParsePosition pp = new ParsePosition(0);
        Number number = nf.parse(input, pp);
        if (number == null || pp.getIndex() == 0) {
            throw new ParseException("Unparseable number: \"" + input + "\"", pp.getErrorIndex());
        }

        // Make sure the entire string is used up
        if (pp.getIndex() < input.length()) {
            throw new ParseException("Unparseable number: \"" + input + "\"", pp.getIndex());
        }

        return number;
    }

    public MessageFormat getMessageFormat(String pattern) {
        MessageFormat f = messageFormatCache.get(pattern);
        if (f == null) {
            f = new MessageFormat(pattern, this.locale);
            messageFormatCache.put(pattern, f);
        }
        return f;
    }

    /*
     * MISC
     */

    public String getLabel(String section, String name) {
        return this.labelSet.getString(section, name);
    }

    public String getLabelThrow(String section, String name) {
        return this.labelSet.getStringThrow(section, name);
    }

    public boolean labelExists(String section, String name) {
        return this.labelSet.labelExists(section, name);
    }

    /**
     * Returns a map containing the contents of an entire label section, or null if the section does not exist.
     *
     * @param section The section to retrieve
     */
    public Map<String, Object> getSection(String section) {
        return this.labelSet.getSection(section);
    }

    /**
     * Convenience function that calls <CODE>java.text.MessageFormat.format()</CODE> on the label
     * using the <CODE>Object[]</CODE> supplied.
     *
     * @param section same as getLabel(section, key)
     * @param key     same as getLabel(section, key)
     * @param args    for <CODE>java.text.MessageFormat.format()</CODE>
     */
    public String getLabel(String section, String key, Object... args) {
        String labelText = TextUtil.escapeForMessageFormat(getLabel(section, key)).toString();
        MessageFormat formatter = getMessageFormat(labelText);
        return formatter.format(args);
    }

    public List<String> getList(String section, String baseName) {
        return this.labelSet.getList(section, baseName);
    }

    /**
     * Retrieve the locale associated with this localizer. A setter shouldn't be added because it's
     * assumed the locale is set on instantiation; otherwise, we'd need to fix getMessageFormat()
     */
    public Locale getLocale() {
        return this.locale;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * @return the locale of the language associated with the localizer
     */
    // TODO: @Deprecated
    public Locale getLanguage() {
        return this.language.getLocale();
    }

    public HumanLanguage getUserLanguage() {
        return this.language;
    }

    public Locale getCurrencyLocale() {
        return this.currencyLocale;
    }
}
