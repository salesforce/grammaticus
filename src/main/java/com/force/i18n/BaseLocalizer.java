/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.commons.util.collection.LruCache;
import com.google.common.collect.ImmutableSet;
import com.ibm.icu.impl.jdkadapter.*;


/**
 * An internationalization and localization utility class Handles locale
 * specific parsing and formatting
 *
 * @author pnakada, smawson, pu.chen
 *
 */
public class BaseLocalizer {
    /**
     * Simple enum + convenience methods for asking Date questions in Local or GMT.
     */
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

    private static final FormatFixer JDK_FORMAT_FIXER = new JdkFormatFixer();
    private static Function<Locale, FormatFixer> LocaleFixerFunction;

    // controlls leniency of parsing date. using non-lenient mode for not allowing ambiguous date such as "2/30"
    // set true to avoid ICU-21301 that fails to parse the transition date for time zones with daylight saving time
    private static final boolean USE_LENIENT_MODE = false;

    static {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 1700);
        c.set(Calendar.MONTH, c.getActualMinimum(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
        EARLIEST = c.getTime();

        c.set(Calendar.YEAR, 4000);
        c.set(Calendar.MONTH, c.getActualMaximum(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        LATEST = c.getTime();

        c.set(Calendar.YEAR, 1700);
        c.set(Calendar.MONTH, c.getActualMinimum(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        OLD_EARLIEST = c.getTime();

        c.set(Calendar.YEAR, 4001);
        c.set(Calendar.MONTH, c.getActualMinimum(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        OLD_LATEST = c.getTime();

        LocaleFixerFunction = locale -> JDK_FORMAT_FIXER; // Use JDK locale data by default.
    }

    private final Locale locale;
    private final Locale currencyLocale;
    private final HumanLanguage language;
    private final TimeZone timeZone;

    protected SharedLabelSet labelSet;

    /**
     * these elements aren't initialized on construction. They are created once the
     * first time they are accessed
     */
    protected DateFormat gmtDateFormat;
    protected DateFormat localDateFormat;
    protected DateFormat localMediumDateFormat;
    protected DateFormat localLongDateFormat;
    protected DateFormat gmtTimeFormat;
    protected DateFormat localTimeFormat;
    protected DateFormat localMediumTimeFormat;
    protected DateFormat localLongTimeFormat;
    protected DateFormat dateTimeFormat;
    protected DateFormat mediumDateTimeFormat;
    protected DateFormat longDateTimeFormat;
    protected DateFormat inputGmtDateFormat;
    protected DateFormat inputLocalDateFormat;
    protected DateFormat inputLocalMediumDateFormat;
    protected DateFormat inputLocalLongDateFormat;
    protected DateFormat inputDateTimeFormat;
    protected DateFormat inputMediumDateTimeFormat;
    protected DateFormat inputLongDateTimeFormat;
    protected DateFormat inputLocalTimeFormat;
    protected DateFormat inputLocalMediumTimeFormat;
    protected DateFormat inputLocalLongTimeFormat;
    protected NumberFormat numberFormat;
    protected NumberFormat currencyFormat;
    protected NumberFormat accountingCurrencyFormat;
    protected final LruCache<String, MessageFormat> messageFormatCache = new LruCache<>(10);

    public static final String ENGLISH_LANGUAGE = "en";
    public static final String JAPANESE_LANGUAGE = "ja";


    protected static void setLocaleFormatFixer(Function<Locale, FormatFixer> predicate) {
        BaseLocalizer.LocaleFixerFunction = predicate;
    }

    protected static Function<Locale, FormatFixer> getLocaleFormatFixer() {
        return BaseLocalizer.LocaleFixerFunction;
    }

    private static final ThreadLocal<SimpleDateFormat> ISO8601_FORMATTER = new ThreadLocal<>() {
        @Override
        public SimpleDateFormat initialValue() {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            fmt.setTimeZone(BaseLocalizer.GMT_TZ);
            return fmt;
        }
    };

    private static final ThreadLocal<SimpleDateFormat> ISO8601_MILLISECOND_FORMATTER = new ThreadLocal<>() {
        @Override
        public SimpleDateFormat initialValue() {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            fmt.setTimeZone(BaseLocalizer.GMT_TZ);
            return fmt;
        }
    };


    /**
     * Sets two digit year start date for parsing 2 year dates to
     * 1959-12-23T23:00 local time or its DST equivalent.
     *
     * @param df DateFormat instance which should extend SimpleDateFormat
     * @param tz TimeZone instance
     */
    protected static void set2DigitYearStart(DateFormat df, TimeZone tz) {
        if (df instanceof SimpleDateFormat) {
            // If 1959-12-23T23:00 falls on a DST transition, the DST length is added.
            // e.g. Asia/Ho_Chi_Minh,Asia/Saigon,VST would be If 1960-01-01T00:00
            LocalDateTime ldt = LocalDateTime.of(1960, 1, 1, 0, 0);
            ZonedDateTime zdt = ldt.atZone(tz.toZoneId());
            Date twoDigitStart = Date.from(zdt.toInstant());
            ((SimpleDateFormat) df).set2DigitYearStart(twoDigitStart);
        }
    }

    /**
     * The Locale information in ICU and JDK changes often based on which libraries you use,
     * but often you need different formats based on customer requirements which may differ
     * from the CLDR information.  This
     *
     * The default implementations are whatever are in the JDK.  Use {@link JdkFormatFixer}
     * which does some helpful overridable methods for thinks like having ar_SA to
     * use the gregorian calendar.
     */
    public static interface FormatFixer {
        /**
         * Some locales have a different calendar than the business application would prefer.  This allows
         * overriding the locale for obtaining a different calendar.
         * @param tz the given time zone
         * @param locale the given locale
         * @return {@code Calender} instance represents given {@code tz} and {@code locale}
         */
        default Calendar getCalendar(TimeZone tz, Locale locale) {
            return Calendar.getInstance(tz, locale);
        }

        /**
         * @return the date formatter with the given formatting style for the given locale.
         * @param style the given formatting style
         * @param aLocale the given locale
         */
        default DateFormat getDateInstance(int style, Locale aLocale) {
            return DateFormat.getDateInstance(style, aLocale);
        }

        /**
         * @return the time formatter with the given formatting style for the given locale.
         * @param timeStyle the given formatting style
         * @param aLocale the given locale
         */
        default DateFormat getTimeInstance(int timeStyle, Locale aLocale) {
            return DateFormat.getTimeInstance(timeStyle, aLocale);
        }

        /**
         * @return the date and time formatter with the given formatting style for the given locale.
         * @param dateStyle the given date formatting style
         * @param timeStyle the given time formatting style
         * @param aLocale the given locale
         */
        default DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
            return DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale);
        }

        /**
         * Changes the SimpleDateFormat pattern for given DateFormat instance,
         * or create a new instance if DateFormat is null or not in the
         * expected implementation instance.
         * @param dateFormat the base format, or null
         * @param pattern the date pattern to apply
         * @param aLocale the given locale
         * @return the dateFormat with the given pattern applied
         */
        default DateFormat applyPattern(DateFormat dateFormat, String pattern, Locale aLocale) {
            if (dateFormat instanceof SimpleDateFormat) {
                ((SimpleDateFormat) dateFormat).applyPattern(pattern);
                return dateFormat;
            }
            return new SimpleDateFormat(pattern, aLocale);
        }

        /**
         * @return number format for given locale.
         * @param locale the given locale
         */
        default NumberFormat getNumberFormat(Locale locale) {
            return NumberFormat.getNumberInstance(locale);
        }

        /**
         * @return currency format for given locale.
         * @param locale the given locale
         */
        default NumberFormat getCurrencyFormat(Locale locale) {
            return NumberFormat.getCurrencyInstance(locale);

        }

        /**
         * @return currency in accounting format for given locale.
         * @param locale the given locale
         */
        default NumberFormat getAccountingCurrencyFormat(Locale locale) {
            return NumberFormat.getCurrencyInstance(locale); // JDK doesn't have dedicated accounting format. Default to regular format
        }

        /**
         * @return percentage format for given locale.
         * @param locale the given locale
         */
        default NumberFormat getPercentFormat(Locale locale) {
            return NumberFormat.getPercentInstance(locale);
        }

        /**
         * @param currencyIsoCode the iso code for the currency
         * @param currencyLocale the locale for the symbol to look up
         * @return the currency symbol for the given isocode locale
         */
        default String getCurrencySymbolFromCurrencyIsoCode(String currencyIsoCode, Locale currencyLocale) {
            return Currency.getInstance(currencyIsoCode).getSymbol(currencyLocale);
        }
    }

    /**
     * @return a format fixer that uses the built-in JDK data
     */
    protected static FormatFixer getJDKFormatFixer() {
        return JDK_FORMAT_FIXER;
    }

    /**
     * @return a format fixer that uses ICU4J instead of the JDK
     */
    protected static FormatFixer getICUFormatFixer() {
        return ICUFormatFixer.INSTANCE;
    }

    private static boolean isSaudiArabia(Locale aLocale) {
        return aLocale.getLanguage().equalsIgnoreCase("ar") && aLocale.getCountry().equalsIgnoreCase("sa");
    }

    /**
     * OLD EXPLANATION
     * DVM - This is a workaround of Sun bug 4842276 (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4842276)
     * which incorrectly changed the date format for Danish.  Since it will be very hard to get Sun to undo this
     * "fix" and certainly won't happen in a timely fashion, we're going to hack instead.  Ugh.
     *
     * NEW EXPLANATION:
     * The JDK fixed the Danish bug in JDK 6. (see above)
     * However, they have other bugs, mostly around the english date format.  One solution would be to implement
     * a DateFormatProvider to override the date format, and that's what will probably happen with Kyrgyz and Kazakh
     * but until then, it's easier to keep this hack around.
     *
     * Singapore is the proximate cause of this; the FormatData_en_SG doesn't override the DateTimeElements.
     * http://www.java2s.com/Open-Source/Java-Document/6.0-JDK-Modules-sun/text/sun.text.resources.htm
     * http://www.java2s.com/Open-Source/Java-Document/6.0-JDK-Modules-sun/text/sun/text/resources/FormatData_en_SG.java.htm
     *
     * The date format for Latvian locale is incorrect.  The format retuned by JDK is YYYY.dd.mm, the correct one is dd.mm.yyyy
     *
     * Other unsupported JDK locales will use the english date format (which is a good way to figure out that they are unsupported)
     *
     * In order to be back-compatible with legacy data, we have to keep these historic formats in place.
     **/
    protected static class JdkFormatFixer implements FormatFixer {
        private static final String BRITISH_DATE_SHORT_FORMAT = "dd/MM/yy";  // Much more proper, eh
        private static final String BRITISH_DATE_MEDIUM_FORMAT = "dd/MM/yyyy";
        private static final String LATVIA_DATE_SHORT_FORMAT = "dd.MM.yy";
        private static final String LATVIA_DATE_MEDIUM_FORMAT = "dd.MM.yyyy";
        private static final String LATVIA  = "LV";

        private static final Set<String> ENGLISH_OVERRIDE_COUNTRIES = ImmutableSet.of("SG", "NG", "MY", "BB", "BM", "GH", "ID");

        // This also applies to singapore and india.
        private static boolean shouldFixJdkDateBug(Locale locale) {
            if (locale == null) return false;
            if (LATVIA.equals(locale.getCountry())) return true;

            return ENGLISH_LANGUAGE.equals(locale.getLanguage())
                    && ENGLISH_OVERRIDE_COUNTRIES.contains(locale.getCountry());
        }

        // We have some issues with Saudi Arabia (ar-SA) calendar and
        // date / time formats due to the change in Calendar type to
        // IslamicCalendar in ICU4J 59.1. Work around these issues for
        // now by falling back to Arabic (ar) locale which uses
        // GregorianCalendar.  These changes were applied to the JDK as well.
        Locale overrideDateLocale(Locale aLocale) {
            if (isSaudiArabia(aLocale)) {
                return new Locale.Builder().setLanguage("ar").build();
            }
            return aLocale;
        }

        /**
         * The MALAYSIA (MY) (ms_MY) locale returns a time format of the form 'hh:mm' making it impossible to differentiate AM/PM
         * @return a dateformat with either a 24-hour clock or the necessary AM/PM identifier attached
         */
        private static DateFormat checkAM(DateFormat dateFormat, Locale l) {
            DateFormat df = dateFormat;
            String p = ((SimpleDateFormat)dateFormat).toPattern();
            if ((p.indexOf('a') == -1) && (p.indexOf('k') == -1) && (p.indexOf('H') == -1)) {
                df = new SimpleDateFormat(p + " a", l);
            }
            return df;
        }

        private String adjustDateFormat(int style, Locale aLocale) {
            checkArgument(style == DateFormat.SHORT || style == DateFormat.MEDIUM);
            if (LATVIA.equals(aLocale.getCountry())) {
                return style == DateFormat.SHORT ? LATVIA_DATE_SHORT_FORMAT : LATVIA_DATE_MEDIUM_FORMAT;
            }
            return style == DateFormat.SHORT ? BRITISH_DATE_SHORT_FORMAT : BRITISH_DATE_MEDIUM_FORMAT;
        }

        @Override
        public DateFormat getDateInstance(int style, Locale aLocale) {
            aLocale = overrideDateLocale(aLocale);
            if (shouldFixJdkDateBug(aLocale)) {
                switch (style) {
                case DateFormat.SHORT:
                case DateFormat.MEDIUM:
                    return new SimpleDateFormat(adjustDateFormat(style, aLocale), aLocale);
                default:
                    return DateFormat.getDateInstance(style, aLocale);
                }
            } else {
                return DateFormat.getDateInstance(style, aLocale);
            }
        }

        @Override
        public DateFormat getTimeInstance(int timeStyle, Locale aLocale) {
            aLocale = overrideDateLocale(aLocale);
            return checkAM(DateFormat.getTimeInstance(timeStyle, aLocale), aLocale);
        }

        @Override
        public DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
            aLocale = overrideDateLocale(aLocale);
            if (shouldFixJdkDateBug(aLocale)) {
                if (dateStyle == DateFormat.SHORT || dateStyle == DateFormat.MEDIUM) {
                    String dateFormat = adjustDateFormat(dateStyle, aLocale);
                    switch (timeStyle) {
                    case DateFormat.SHORT:
                        return new SimpleDateFormat(dateFormat + " HH:mm", aLocale);
                    case DateFormat.MEDIUM:
                        return new SimpleDateFormat(dateFormat + " HH:mm:ss", aLocale);
                    default:
                        return new SimpleDateFormat(dateFormat + " HH:mm:ss z", aLocale);
                    }
                }
                // #fixEnglishNumberFormat is done in BaseLocalizer#checkAM(DateFormat,Locale)
                // so we don't have to do it here
                return checkAM(DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale), aLocale);
            } else {
                // #fixEnglishNumberFormat is done in BaseLocalizer#checkAM(DateFormat,Locale)
                // so we don't have to do it here
                return checkAM(DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale), aLocale);
            }
        }

        @Override
        public DateFormat applyPattern(DateFormat dateFormat, String pattern, Locale aLocale) {
            if (dateFormat instanceof SimpleDateFormat) {
                ((SimpleDateFormat) dateFormat).applyPattern(pattern);
                return dateFormat;
            }
            return new SimpleDateFormat(pattern, overrideDateLocale(aLocale));
        }
    }

    /**
     * Some hot fixes to override locale data. This is ICU version.
     **/
    protected static class ICUFormatFixer implements FormatFixer {
        // Don't use the enum pattern here because we want to allow easy overriding.
        static final FormatFixer INSTANCE = new ICUFormatFixer();

        private static final Set<String> ENGLISH_OVERRIDE_COUNTRIES = ImmutableSet.of("ID");

        // Override locales.
        /**
         * Allow simple overriding of the date locale to use for the given locale.  Used for
         * ar-SA {@link #overrideCalendarLocale(Locale)} and also for en-ID.  Override this
         * if you want differing behaviors.
         * @param alocale the locale to look for
         * @return the locale to use for
         */
        protected Locale overrideDateLocaleFor(Locale alocale) {
            // Override some locales to en_GB.
            if (ENGLISH_LANGUAGE.equals(alocale.getLanguage()) && ENGLISH_OVERRIDE_COUNTRIES.contains(alocale.getCountry())) {
                return Locale.UK;
            }
            alocale = overrideCalendarLocale(alocale);

            return alocale;
        }

        /**
         * We have some issues with Saudi Arabia (ar-SA) calendar and
         * date / time formats due to the change in Calendar type to
         * IslamicCalendar in ICU4J 59.1. Work around these issues for
         * now by falling back to Arabic (ar) locale which uses
         * GregorianCalendar.
         * @param aLocale the locale to look for
         * @return the locale to use for date formats and calendar.
         */
        protected Locale overrideCalendarLocale(Locale aLocale) {
            if (isSaudiArabia(aLocale) && aLocale.getVariant().length() == 0) {
                // aLocale may contain other Unicode extension (e.g. "ar-SA-u-nu-latin") Use Locale.Builder to safely
                // add/overrite "ca" value.
                Locale.Builder builder = new Locale.Builder();
                builder.setLanguageTag(aLocale.toLanguageTag());
                builder.setUnicodeLocaleKeyword("ca", "gregory");
                return builder.build();
            }
            return aLocale;
        }

        @Override
        public Calendar getCalendar(TimeZone tz, Locale locale) {
            // TODO (G11n): CalenderICU#equals(Object), #compareTo(Object) does not work against JDK Calendar object.
            // - it works different around #set() (esp. Calendar.DAY_OF_WEEK) and caused unexpected error.
            // We still return JDK Calendar object here for now.
            // - JDK Calendar messed up with TimeZoneICU in computation (set/get methods). Replace with JDK TimeZone here
            TimeZone tzForJDK = (tz instanceof TimeZoneICU) ? TimeZone.getTimeZone(tz.getID()) : tz;
            return Calendar.getInstance(tzForJDK, overrideCalendarLocale(locale)); // Note, we don't override the English countries here
        }

        @Override
        public DateFormat getDateInstance(int style, Locale aLocale) {
            aLocale = overrideDateLocaleFor(aLocale);

            return SimpleDateFormatICU.wrap((com.ibm.icu.text.SimpleDateFormat)com.ibm.icu.text.DateFormat.getDateInstance(style, aLocale));
        }

        @Override
        public DateFormat getTimeInstance(int timeStyle, Locale aLocale) {
            aLocale = overrideDateLocaleFor(aLocale);

            return SimpleDateFormatICU.wrap((com.ibm.icu.text.SimpleDateFormat)com.ibm.icu.text.DateFormat.getTimeInstance(timeStyle, aLocale));
        }

        @Override
        public DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
            aLocale = overrideDateLocaleFor(aLocale);

            return SimpleDateFormatICU.wrap((com.ibm.icu.text.SimpleDateFormat)com.ibm.icu.text.DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale));
        }

        @Override
        public DateFormat applyPattern(DateFormat dateFormat, String pattern, Locale aLocale) {
            if (dateFormat instanceof SimpleDateFormatICU) {
                ((SimpleDateFormatICU) dateFormat).applyPattern(pattern);
                return dateFormat;
            }
            return SimpleDateFormatICU.wrap(new com.ibm.icu.text.SimpleDateFormat(pattern, overrideDateLocaleFor(aLocale)));
        }

        @Override
        public NumberFormat getNumberFormat(Locale locale) {
            com.ibm.icu.text.NumberFormat nf = com.ibm.icu.text.NumberFormat.getNumberInstance(locale);
            return NumberFormatICU.wrap(nf);
        }

        @Override
        public NumberFormat getCurrencyFormat(Locale locale) {
            com.ibm.icu.text.NumberFormat nf = com.ibm.icu.text.NumberFormat.getCurrencyInstance(locale);
            return NumberFormatICU.wrap(nf);
        }

        @Override
        public NumberFormat getAccountingCurrencyFormat(Locale locale) {
            com.ibm.icu.text.NumberFormat nf = com.ibm.icu.text.NumberFormat.getInstance(locale, com.ibm.icu.text.NumberFormat.ACCOUNTINGCURRENCYSTYLE);
            return NumberFormatICU.wrap(nf);
        }

        @Override
        public NumberFormat getPercentFormat(Locale locale) {
            com.ibm.icu.text.NumberFormat nf = com.ibm.icu.text.NumberFormat.getPercentInstance(locale);
            return NumberFormatICU.wrap(nf);
        }

        @Override
        public String getCurrencySymbolFromCurrencyIsoCode(String currencyIsoCode, Locale currencyLocale) {
            return com.ibm.icu.util.Currency.getInstance(currencyIsoCode).getSymbol(currencyLocale);
        }
    }

    private static FormatFixer getFormatProvider(Locale locale) {
        return LocaleFixerFunction.apply(locale);
    }

    /**
     * This constructor is used to create a BaseLocalizer
     *
     * @param locale is the user's locale data
     * @param currencyLocale is org's currency locale
     * @param timeZone the timeZone associated with this localizer
     * @param language the human language for this localizer
     * @param labelSet the set of labels for this language
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

    /**
     * Returns a calendar for local or GMT time zone. If it's local, localizer's time zone will be used.
     *
     * @param tz local or GMT time zone
     * @return a calendar for local or GMT time zone.
     */
    public Calendar getCalendar(LocalOrGmt tz) {
        if (tz == LOCAL) {
            return getCalendar(getTimeZone(), getLocale());
        } else {
            return getCalendar(GMT_TZ, getLocale());
        }
    }

    /**
     * Returns a calendar for given time zone with localizer's current locale.
     *
     * @param tz time zone
     * @return a calendar for given time zone.
     */
    public Calendar getCalendar(TimeZone tz) {
        return getCalendar(tz, getLocale());
    }

    /**
     * Returns a calendar for given locale with localizer's current time zone.
     *
     * @param l locale
     * @return a calendar for given locale.
     */
    public Calendar getCalendar(Locale l) {
        return getFormatProvider(l).getCalendar(getTimeZone(), l);
    }

    /**
     * Returns a JDK calendar type for given locale and time zone.
     *
     * @param tz time zone
     * @param locale locale
     * @return a calendar for given locale and time zone.
     */
    public Calendar getCalendar(TimeZone tz, Locale locale) {
        return getFormatProvider(locale).getCalendar(tz, locale);
    }

    /*
     * DATE INPUT
     */

    /**
     * Parses a date-only input string. Caller specified time zone.
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     *
     * @param input date string
     * @param tz time zone
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public Date parseDate(String input, LocalOrGmt tz) throws ParseException {
        return BaseLocalizer.doParseDate(input, getInputDateFormat(tz));
    }

    /**
     * Parses a date-only input string based on localizer's locale and time-zone.
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     *
     * @param input date string
     * @param style DateFormat style
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public Date parseDate(String input, int style) throws ParseException {
        return BaseLocalizer.doParseDate(input, getInputDateFormat(style));
    }


    /**
     * Parses a date-only input string
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     *
     * @param input date string
     * @param style DateFormat style
     * @param locale locale
     * @param tz TimeZone
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public static Date parseDate(String input, int style, Locale locale, TimeZone tz) throws ParseException {
        return BaseLocalizer.doParseDate(input, getLocaleInputDateFormat(locale, style, tz));
    }


    /**
     * Parses a date-time input string. Local time zone.
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     *
     * @param input date-time string
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public Date parseDateTime(String input) throws ParseException {
        return BaseLocalizer.doParseDate(input, getInputDateTimeFormat());
    }

    /**
     * Parses a date-time input string
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     *
     * @param input date string
     * @param style DateFormat style
     * @param locale Locale
     * @param tz TimeZone
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public static Date parseDateTime(String input, int style, Locale locale, TimeZone tz) throws ParseException {
        return BaseLocalizer.doParseDate(input, getLocaleInputDateTimeFormat(locale, style, tz));
    }

    /**
     * Parses a date-time input string. Local time zone.
     * Handles 2-digit, 4-digit, single-digit years, out-of-bounds years,
     * and trailing garbage in input string.
     *
     * @param input date-time string
     * @param style DateFormat style
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public Date parseDateTime(String input, int style) throws ParseException {
        return BaseLocalizer.doParseDate(input, getInputDateTimeFormat(style));
    }

    /**
     * Parses a time input string. Local time zone..
     * @param input time string
     * @param style DateFormat style
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public Date parseTime(String input, int style) throws ParseException {
        return BaseLocalizer.doParseTime(input, getInputTimeFormat(style));
    }

    /**
     * Parses a time input string
     *
     * @param input date string
     * @param style DateFormat style
     * @param locale Locale
     * @param tz TimeZone
     * @return a parsed date type.
     * @throws ParseException on an invalid date string
     */
    public static Date parseTime(String input, int style, Locale locale, TimeZone tz) throws ParseException {
        return BaseLocalizer.doParseTime(input, getLocaleInputTimeFormat(locale, style, tz));
    }

    /**
     * Get date only DateFormat for input based on style.
     * Local time zone.
     *
     * @param style DateFormat style
     * @return a DateFormat based on localizer's locale and time zone
     */
    public DateFormat getInputDateFormat(int style) {
        switch (style) {
            case DateFormat.SHORT:
                return getInputDateFormat();
            case DateFormat.MEDIUM:
                return getInputMediumDateFormat();
            case DateFormat.LONG:
                return getInputLongDateFormat();
            default:
                return getInputDateFormat();
        }
    }

    /**
     * Get date-only DateFormat for input.  Caller specified time zone.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.
     *
     * @param tz time zone
     * @return a date-only DateFormat.
     */
    public DateFormat getInputDateFormat(LocalOrGmt tz) {
        if (tz == LOCAL) {
            if (this.inputLocalDateFormat == null) {
                this.inputLocalDateFormat = BaseLocalizer.getLocaleInputDateFormat(getLocale(), getTimeZone());
            }
            return this.inputLocalDateFormat;
        } else {
            if (this.inputGmtDateFormat == null) {
                this.inputGmtDateFormat = BaseLocalizer.getLocaleInputDateFormat(getLocale(), GMT_TZ);
            }
            return this.inputGmtDateFormat;
        }
    }

    /**
     * Get date-only short DateFormat for input.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.

     * @return a date-only DateFormat.
     */
    public DateFormat getInputDateFormat() {
        if (this.inputLocalDateFormat == null) {
            this.inputLocalDateFormat = BaseLocalizer.getLocaleInputDateFormat(getLocale(), getTimeZone());
        }
        return this.inputLocalDateFormat;
    }

    /**
     * Get date-only medium DateFormat for input.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.

     * @return a date-only DateFormat.
     */
    public DateFormat getInputMediumDateFormat() {
        if (this.inputLocalMediumDateFormat == null) {
            this.inputLocalMediumDateFormat = BaseLocalizer.getLocaleInputDateFormat(getLocale(), DateFormat.MEDIUM, getTimeZone());
        }
        return this.inputLocalMediumDateFormat;
    }

    /**
     * Get date-only long DateFormat for input.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.

     * @return a date-only DateFormat.
     */
    public DateFormat getInputLongDateFormat() {
        if (this.inputLocalLongDateFormat == null) {
            this.inputLocalLongDateFormat = BaseLocalizer.getLocaleInputDateFormat(getLocale(), DateFormat.LONG, getTimeZone());
        }
        return this.inputLocalLongDateFormat;
    }

    /**
     * Static method to get date-only DateFormat for input. This is based on a 2 digit year
     * input mask, which also handles 4-digit year, but caller must use doParseDate() to
     * handle single-digit years, out-of-bounds years, and trailing garbage in input string.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     *
     * @param locale locale
     * @param tz time zone
     * @return a date-only DateFormat.
     */
    public static DateFormat getLocaleInputDateFormat(Locale locale, TimeZone tz) {
        DateFormat df = getFormatProvider(locale).getDateInstance(DateFormat.SHORT, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        set2DigitYearStart(df, tz);
        return df;
    }

    /**
     * Static method to get date-only DateFormat for input based on style. This is based on a 2 digit year
     * input mask, which also handles 4-digit year, but caller must use doParseDate() to
     * handle single-digit years, out-of-bounds years, and trailing garbage in input string.
     *
     * @param locale locale
     * @param style DateFormat style
     * @param tz time zone
     * @return a date-only DateFormat.
     */
    public static DateFormat getLocaleInputDateFormat(Locale locale, int style, TimeZone tz) {
        int dateStyle;
        switch (style) {
        case DateFormat.SHORT:
        case DateFormat.MEDIUM:
        case DateFormat.LONG:
            dateStyle = style;
            break;
        default:
            dateStyle = DateFormat.SHORT;
        }
        DateFormat df = getFormatProvider(locale).getDateInstance(dateStyle, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        set2DigitYearStart(df, tz);
        return df;
    }

    public static boolean isLanguageEnglish(Locale locale) {
        return ENGLISH_LANGUAGE.equals(locale.getLanguage());
    }

    public static boolean isLanguageJapanese(Locale locale) {
        return JAPANESE_LANGUAGE.equals(locale.getLanguage());
    }

    /**
     * Get date-time DateFormat for input based on style.
     * Long date-time format uses short Date and long Time formats
     * Local time zone.
     *
     * @param style DateFormat style
     * @return a DateFormat based on localizer's locale and time zone
     */
    public DateFormat getInputDateTimeFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            return getInputDateTimeFormat();
        case DateFormat.MEDIUM:
            return getInputMediumDateTimeFormat();
        case DateFormat.LONG:
            //Uses short date and and long time formats
            return getInputLongDateTimeFormat();
        default:
            return getInputDateTimeFormat();
        }
    }

    /**
     * Get date-time DateFormat for input. Local time zone.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.
     *
     * @return a date and time DateFormat.
     */
    public DateFormat getInputDateTimeFormat() {
        if (this.inputDateTimeFormat == null) {
            this.inputDateTimeFormat = BaseLocalizer.getLocaleInputDateTimeFormat(getLocale(), getTimeZone());
        }
        return this.inputDateTimeFormat;
    }


    /**
     * Get date-time DateFormat for input. Local time zone.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.
     *
     * @return a date and time DateFormat.
     */
    public DateFormat getInputMediumDateTimeFormat() {
        if (this.inputMediumDateTimeFormat == null) {
            this.inputMediumDateTimeFormat = BaseLocalizer.getLocaleInputDateTimeFormat(getLocale(), DateFormat.MEDIUM, getTimeZone());
        }
        return this.inputMediumDateTimeFormat;
    }

    /**
     * Get date-time DateFormat for input. Local time zone.
     * This is based on a 2 digit year input mask, which also handles 4-digit year,
     * but caller must use doParseDate() to handle single-digit years, out-of-bounds
     * years, and trailing garbage in input string.
     *
     * @return a date and time DateFormat.
     */
    public DateFormat getInputLongDateTimeFormat() {
        if (this.inputLongDateTimeFormat == null) {
            this.inputLongDateTimeFormat = BaseLocalizer.getLocaleInputDateTimeFormat(getLocale(), DateFormat.LONG, getTimeZone());
        }
        return this.inputLongDateTimeFormat;
    }

    /**
     * Static method to get date-time DateFormat for input.  This is based on a 2 digit year
     * input mask, which also handles 4-digit year, but caller must use doParseDate() to
     * handle single-digit years, out-of-bounds years, and trailing garbage in input string.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     *
     * @param locale locale
     * @param tz time zone
     * @return a date and time DateFormat.
     */
    public static DateFormat getLocaleInputDateTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = getFormatProvider(locale).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        set2DigitYearStart(df, tz);
        return df;
    }

    /**
     * Static method to get date-time DateFormat for input.  This is based on a 2 digit year
     * input mask, which also handles 4-digit year, but caller must use doParseDate() to
     * handle single-digit years, out-of-bounds years, and trailing garbage in input string.
     * DateFormat.LONG uses short-date and long-time formats
     *
     * @param locale locale
     * @param tz time zone
     * @param style DateFormat style
     * @return a date and time DateFormat.
     */

    public static DateFormat getLocaleInputDateTimeFormat(Locale locale, int style, TimeZone tz) {
        int dateStyle;
        int timeStyle;
        switch (style) {
        case DateFormat.MEDIUM:
            dateStyle = timeStyle = DateFormat.MEDIUM;
            break;
        case DateFormat.LONG:
            dateStyle = DateFormat.SHORT;
            timeStyle = DateFormat.LONG;
            break;
        case DateFormat.SHORT:
        default:
            dateStyle = timeStyle = DateFormat.SHORT;
        }
        DateFormat df = getFormatProvider(locale).getDateTimeInstance(dateStyle, timeStyle, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        set2DigitYearStart(df, tz);
        return df;
    }

    /**
     * Get time only DateFormat for input based on style.
     * Local time zone.
     *
     * @param style DateFormat style
     * @return a DateFormat based on localizer's locale and time zone
     */
    public DateFormat getInputTimeFormat(int style) {
        switch (style) {
            case DateFormat.SHORT:
                return getInputTimeFormat();
            case DateFormat.MEDIUM:
                return getInputMediumTimeFormat();
            case DateFormat.LONG:
                return getInputLongTimeFormat();
            default:
                return getInputTimeFormat();
        }
    }

    public DateFormat getInputTimeFormat() {
        if (this.inputLocalTimeFormat == null) {
            this.inputLocalTimeFormat = BaseLocalizer.getLocaleInputTimeFormat(getLocale(), DateFormat.SHORT, getTimeZone());
        }
        return this.inputLocalTimeFormat;
    }

    public DateFormat getInputMediumTimeFormat() {
        if (this.inputLocalMediumTimeFormat == null) {
            this.inputLocalMediumTimeFormat = BaseLocalizer.getLocaleInputTimeFormat(getLocale(), DateFormat.MEDIUM, getTimeZone());
        }
        return this.inputLocalMediumTimeFormat;
    }

    public DateFormat getInputLongTimeFormat() {
        if (this.inputLocalLongTimeFormat == null) {
            this.inputLocalLongTimeFormat = BaseLocalizer.getLocaleInputTimeFormat(getLocale(), DateFormat.LONG, getTimeZone());
        }
        return this.inputLocalLongTimeFormat;
    }

    /**
     * Static method to get DateFormat for time input based on style
     * Caller must do doParseTime for parsing
     * @param locale locale
     * @param style DateFormat style
     * @param tz time zone
     * @return a date-only DateFormat.
     */
    public static DateFormat getLocaleInputTimeFormat(Locale locale, int style, TimeZone tz) {
        int dateStyle;

        switch (style) {
        case DateFormat.SHORT:
        case DateFormat.MEDIUM:
        case DateFormat.LONG:
            dateStyle = style;
            break;
        default:
            dateStyle = DateFormat.SHORT;
        }
        DateFormat df = getFormatProvider(locale).getTimeInstance(dateStyle, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        set2DigitYearStart(df, tz);
        return df;
    }

    /**
     * Parse the given input string using the given format, and make sure the entire string
     * is used up during the parsing and garbage text at the end is not allowed.  Also
     * handles single-digit years and verify out-of-bounds years.  All date parsing in
     * the app must go through this.
     *
     * @param input date string
     * @param df DateFormat used to parse the date
     * @return a parsed date.
     * @throws ParseException on an invalid input
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
     *
     * @param input date string
     * @param tf DateFormat used to parse the date
     * @return a parsed date.
     * @throws ParseException on an invalid input
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
     * Formats a date-only Date.
     *
     * @param date a Date
     * @param style tz local or GMT time zone
     * @return a formatted date string.
     */
    public String formatTime(Date date, int style) {
        return (date == null) ? null : getTimeFormat(style).format(date);
    }

    /**
     * @param date a Date
     * @param style DateFormat style
     * @param locale Locale
     * @param tz TimeZone
     * @return a formatted date string.
     */
    public static String formatTime(Date date, int style, Locale locale, TimeZone tz) {
        return (date == null) ? null : getLocaleTimeFormat(style, locale, tz).format(date);
    }

    /**
     * Formats a date-only Date.  Caller specified time zone.
     *
     * @param date a Date
     * @param tz local or GMT time zone
     * @return a formatted date string.
     */
    public String formatDate(Date date, LocalOrGmt tz) {
        return (date == null) ? null : getDateFormat(tz).format(date);
    }

    /**
     * Formats a date-only Date.
     *
     * @param date a Date
     * @param style tz local or GMT time zone
     * @return a formatted date string.
     */
    public String formatDate(Date date, int style) {
        return (date == null) ? null : getDateFormat(style).format(date);
    }

    /**
     * @param date a Date
     * @param style DateFormat style
     * @param locale Locale
     * @param tz TimeZone
     * @return a formatted date string.
     */
    public static String formatDate(Date date, int style, Locale locale, TimeZone tz) {
        return (date == null) ? null : getLocaleDateFormat(style, locale, tz).format(date);
    }

    /**
     * Formats a date-time Date.  Local time zone.
     *
     * @param date a Date
     * @return a formatted date string.
     */

    public String formatDateTime(Date date) {
        return (date == null) ? null : getDateTimeFormat().format(date);
    }

    /**
     * Formats a date-time Date.  Local time zone.
     *
     * @param date a Date
     * @param style DateFormat style
     * @return a formatted date string.
     */

    public String formatDateTime(Date date,int style) {
        return (date == null) ? null : getDateTimeFormat(style).format(date);
    }

    /**
     * @param date a Date
     * @param style DateFormat style
     * @param locale Locale
     * @param tz TimeZone
     * @return a formatted date string.
     */
    public static String formatDateTime(Date date, int style, Locale locale, TimeZone tz) {
        return (date == null) ? null : getLocaleDateTimeFormat(style, locale, tz).format(date);
    }

    /**
     * Static method to get date-only DateFormat for output.
     *
     * @param style DateFormat style
     * @param locale locale
     * @param tz time zone
     * @return a DateFormat.
     */
    public static DateFormat getLocaleDateFormat(int style, Locale locale, TimeZone tz) {
        DateFormat df;
        switch (style) {
        case DateFormat.SHORT:
            df = getFormatProvider(locale).getDateInstance(DateFormat.SHORT, locale);
            break;
        case DateFormat.MEDIUM:
            df = getFormatProvider(locale).getDateInstance(DateFormat.MEDIUM, locale);
            break;
        case DateFormat.LONG:
            df = getFormatProvider(locale).getDateInstance(DateFormat.LONG, locale);
            break;
        default:
            df = getFormatProvider(locale).getDateInstance(DateFormat.SHORT, locale);

        }
        df = BaseLocalizer.convertTo4DigitYear(df, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * Get date-only DateFormat for output based on style. 4-digit year
     *
     * @param style DateFormat style
     * @return a DateFormat based on localizer's locale and time zone
     */
    public DateFormat getDateFormat(int style) {
        switch (style) {
            case DateFormat.SHORT:
                return getDateFormat();
            case DateFormat.MEDIUM:
                return getMediumDateFormat();
            case DateFormat.LONG:
                return getLongDateFormat();
            default:
                return getDateFormat();
        }
    }


    /**
     * Get date-only DateFormat for output.  Uses short date format, 4-digit year.
     * Caller specified time zone.
     *
     * @param tz local or GMT time zone
     * @return a DateFormat.
     */
    public DateFormat getDateFormat(LocalOrGmt tz) {
        if (tz == LOCAL) {
            if (this.localDateFormat == null) {
                this.localDateFormat = BaseLocalizer.getLocaleDateFormat(getLocale(), getTimeZone());
            }
            return this.localDateFormat;
        } else {
            if (this.gmtDateFormat == null) {
                this.gmtDateFormat = BaseLocalizer.getLocaleDateFormat(getLocale(), GMT_TZ);
            }
            return this.gmtDateFormat;
        }
    }

    /**
     * Get date-only DateFormat for output.  Uses short date format, 4-digit year.
     * Caller specified time zone.
     *
     * @return a DateFormat.
     */
    public DateFormat getDateFormat() {
            if (this.localDateFormat == null) {
                this.localDateFormat = BaseLocalizer.getLocaleDateFormat(getLocale(), getTimeZone());
            }
            return this.localDateFormat;
    }


    /**
     * Static method to get date-only DateFormat for output.  Uses short date format, 4-digit year.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     *
     * @param locale lcoale
     * @param tz time zone
     * @return a DateFormat.
     */
    public static DateFormat getLocaleDateFormat(Locale locale, TimeZone tz) {
        DateFormat df = BaseLocalizer.convertTo4DigitYear(getFormatProvider(locale).getDateInstance(DateFormat.SHORT,
            locale), locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * Get date-only DateFormat for output.  Uses medium date format, 4-digit year.
     * Local time zone.
     *
     * @return a DateFormat.
     */
    public DateFormat getMediumDateFormat() {
        if (this.localMediumDateFormat == null) {
            Locale localeToUse = getLocale();
            this.localMediumDateFormat = BaseLocalizer.convertTo4DigitYear(getFormatProvider(localeToUse).getDateInstance(
                DateFormat.MEDIUM, localeToUse), localeToUse);
            this.localMediumDateFormat.setLenient(USE_LENIENT_MODE);
            this.localMediumDateFormat.setTimeZone(getTimeZone());
        }
        return this.localMediumDateFormat;
    }

    /**
     * Get date-only DateFormat for output.  Uses long date format, 4-digit year.
     * Local time zone.
     *
     * @return a DateFormat.
     */
    public DateFormat getLongDateFormat() {
        if (this.localLongDateFormat == null) {
            Locale localeToUse = getLocale();
            this.localLongDateFormat = BaseLocalizer.convertTo4DigitYear(getFormatProvider(localeToUse).getDateInstance(
                DateFormat.LONG, localeToUse), localeToUse);
            this.localLongDateFormat.setLenient(USE_LENIENT_MODE);
            this.localLongDateFormat.setTimeZone(getTimeZone());
        }
        return this.localLongDateFormat;
    }

    /**
     * Static method to get time-only DateFormat for output.
     * @param style DateFormat style
     * @param locale locale
     * @param tz time zone
     * @return a DateFormat.
     */
    public static DateFormat getLocaleTimeFormat(int style, Locale locale, TimeZone tz) {
        DateFormat df;
        switch (style) {
        case DateFormat.SHORT:
            df = getFormatProvider(locale).getTimeInstance(DateFormat.SHORT, locale);
            break;
        case DateFormat.MEDIUM:
            df = getFormatProvider(locale).getTimeInstance(DateFormat.MEDIUM, locale);
            break;
        case DateFormat.LONG:
            df = getFormatProvider(locale).getTimeInstance(DateFormat.LONG, locale);
            break;
        default:
            df = getFormatProvider(locale).getTimeInstance(DateFormat.SHORT, locale);

        }
        df = BaseLocalizer.convertTo4DigitYear(df, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * Get time-only DateFormat based on style for output.
     * Local time zone.
     *
     * @param style DateFormat style
     * @return a DateFormat based on localizer's time zone and cached
     */
    public DateFormat getTimeFormat(int style) {
        switch (style) {
            case DateFormat.SHORT:
                return getTimeFormat();
            case DateFormat.MEDIUM:
                return getMediumTimeFormat();
            case DateFormat.LONG:
                return getLongTimeFormat();
            default:
                return getTimeFormat();
        }
    }

    /**
     * Get time-only DateFormat for output.  Uses short time format.
     * Local time zone.
     *
     * @return a DateFormat based on localizer's time zone and cached
     */
    public DateFormat getTimeFormat() {
        return getTimeFormatHelper(this.localTimeFormat, DateFormat.SHORT, getTimeZone());
    }

    /**
     * Get time-only DateFormat for output.  Uses medium time format.
     * Local time zone.
     *
     * @return a DateFormat based on localizer's time zone and cached
     */
    public DateFormat getMediumTimeFormat() {
        return getTimeFormatHelper(this.localMediumTimeFormat, DateFormat.MEDIUM, getTimeZone());
    }

    /**
     * Get time-only DateFormat for output.  Uses long time format.
     * Local time zone.
     *
     * @return a DateFormat based on localizer's time zone and cached
     */
    public DateFormat getLongTimeFormat() {
        return getTimeFormatHelper(this.localLongTimeFormat, DateFormat.LONG, getTimeZone());
    }

    /**
     * Get date-time DateFormat for output.  Uses medium date and and medium time formats, 4-digit year.
     * Local time zone.
     *
     * @return a DateFormat instance with medium date and time format
     */
    public DateFormat getMediumDateTimeFormat() {
        if (this.mediumDateTimeFormat == null) {
            Locale localeToUse = getLocale();
            this.mediumDateTimeFormat = BaseLocalizer.convertTo4DigitYear(getFormatProvider(localeToUse).getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.MEDIUM, localeToUse), localeToUse);
            this.mediumDateTimeFormat.setLenient(USE_LENIENT_MODE);
            this.mediumDateTimeFormat.setTimeZone(getTimeZone());
        }
        return this.mediumDateTimeFormat;
    }

    /**
     * Get date-time DateFormat for output.  Uses short date and and long time formats, 4-digit year.
     * Local time zone.
     *
     * @return a DateFormat instance with short date and long time format
     */
    public DateFormat getLongDateTimeFormat() {
        if (this.longDateTimeFormat == null) {
            Locale localeToUse = getLocale();
            this.longDateTimeFormat = BaseLocalizer.convertTo4DigitYear(getFormatProvider(localeToUse).getDateTimeInstance(
                DateFormat.SHORT, DateFormat.LONG, localeToUse), localeToUse);
            this.longDateTimeFormat.setLenient(USE_LENIENT_MODE);
            this.longDateTimeFormat.setTimeZone(getTimeZone());
        }
        return this.longDateTimeFormat;
    }

    /**
     * Get time-only DateFormat for output.  Uses short time format.
     * GMT time zone.
     *
     * @return a GMT DateFormat with short time format
     */
    public DateFormat getGmtTimeFormat() {
        return getTimeFormatHelper(this.gmtTimeFormat, DateFormat.SHORT, GMT_TZ);
    }

    private DateFormat getTimeFormatHelper(DateFormat timeFormat, int style, TimeZone timeZone) {
        if (timeFormat != null) return timeFormat; // In case the protected variables are set, just used the cached values

        Locale localeToUse = getLocale();
        DateFormat newTimeFormat = getFormatProvider(localeToUse).getTimeInstance(style, localeToUse);;
        // Explicitly setting to true. This is a fix for Timezones where the certain time doesn't exist.
        // Example: America/Mazatlan, 12:00 AM. Try parsing this with a formatter of h:mm a.
        newTimeFormat.setLenient(true);
        newTimeFormat.setTimeZone(timeZone);
        return newTimeFormat;
    }

    /**
     * Get date-time DateFormat for output.  Uses short date and time formats, 4-digit year.
     * Local time zone.
     *
     * @return a DateFormat based on localizer's locale and time zone
     */
    public DateFormat getDateTimeFormat() {
        if (this.dateTimeFormat == null) {
            this.dateTimeFormat = BaseLocalizer.getLocaleDateTimeFormat(getLocale(), getTimeZone());
        }
        return this.dateTimeFormat;
    }


    /**
     * Static method to get date-time DateFormat for output.
     * @param style DateFormat style
     * @param locale locale
     * @param tz time zone
     * @return a DateFormat instance
     */
    public static DateFormat getLocaleDateTimeFormat(int style, Locale locale, TimeZone tz) {
        DateFormat df;
        switch (style) {
        case DateFormat.SHORT:
            df = getFormatProvider(locale).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
            break;
        case DateFormat.MEDIUM:
            df = getFormatProvider(locale).getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
            break;
        case DateFormat.LONG:
            df = getFormatProvider(locale).getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale);
            break;
        default:
            df = getFormatProvider(locale).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        }
        df = BaseLocalizer.convertTo4DigitYear(df, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * Get date-time DateFormat for output based on style. Uses 4-digit year.
     * Long date-time format uses short Date and long Time formats
     * Local time zone.
     *
     * @param style DateFormat style
     * @return a DateFormat based on localizer's locale and time zone
     */
    public DateFormat getDateTimeFormat(int style) {
        switch (style) {
            case DateFormat.SHORT:
                return getDateTimeFormat();
            case DateFormat.MEDIUM:
                return getMediumDateTimeFormat();
            case DateFormat.LONG:
                //Uses short date and and long time formats, 4-digit year.
                return getLongDateTimeFormat();
            default:
                return getDateTimeFormat();
        }
    }

    /**
     * Static method to get date-time DateFormat for output.  Uses short date and time formats, 4-digit year.
     * Also used by FilterItem to get DateFormat to store report dates in US locale.
     *
     * @param locale locale
     * @param tz time zone
     * @return a DateFormat instance with short date and time format
     */
    public static DateFormat getLocaleDateTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = BaseLocalizer.convertTo4DigitYear(getFormatProvider(locale).getDateTimeInstance(DateFormat.SHORT,
            DateFormat.SHORT, locale), locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * This form of getLocalDateTimeFormat is used to render times in a specific locale. Used to send event notification emails
     *
     * @param locale locale
     * @param tz time zone
     * @return a DateFormat instance with short time format
     */
    public static DateFormat getLocaleTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = getFormatProvider(locale).getTimeInstance(DateFormat.SHORT, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
    }

    /**
     * To render times in a specific locale. In medium format.
     *
     * @param locale locale
     * @param tz time zone
     * @return a DateFormat instance with medium time format
     */
    public static DateFormat getLocaleMediumTimeFormat(Locale locale, TimeZone tz) {
        DateFormat df = getFormatProvider(locale).getTimeInstance(DateFormat.MEDIUM, locale);
        df.setLenient(USE_LENIENT_MODE);
        df.setTimeZone(tz);
        return df;
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
            if (loc != -1) { // a 2 digit year string
                pattern = pattern.substring(0, loc) + "yy" + pattern.substring(loc);
            } else if (pattern.indexOf("y") != -1) { // a single 'y' year string
                loc = pattern.indexOf("y");
                pattern = pattern.substring(0, loc) + "yyy" + pattern.substring(loc);
            }
        }
        return getFormatProvider(locale).applyPattern(dateFormat, pattern, locale);
    }

    /**
     * Returns a string in ISO8601 format, with both date and time
     * e.g. 2011-01-31T22:59:48Z
     * @param date the date to format
     * @return the data in ISO8601 format
     */
    public static String formatISO8601(Date date) {
        return ISO8601_FORMATTER.get().format(date);
    }

    /**
     * Returns a string in ISO8601 format, with both date and time with millis
     * e.g. 2011-01-31T22:59:48.000Z
     * @param date the date to format
     * @return the data in ISO8601 format with milliseconds
     */
    public static String formatISO8601WithMilliSeconds(Date date) {
        return ISO8601_MILLISECOND_FORMATTER.get().format(date);
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
        }
        catch (ParseException x) {
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
        }
        catch (ParseException x) {
            return BaseLocalizer.doParseNumber(s, getNumberFormat());
        }
    }

    /**
     * Parse the accounting-formated string as currency.
     *
     * @param s the string to be parsed(in accounting currency format)
     * @return a number object representing the parsed currency
     * @throws ParseException if the string cannot be parsed
     */
    public Number parseAccountingCurrency(String s) throws ParseException {
        try {
            return BaseLocalizer.doParseNumber(s, getAccountingCurrencyFormat());
        }
        catch (ParseException x) {
            return BaseLocalizer.doParseNumber(s, getNumberFormat());
        }
    }

    /**
     * Gets a NumberFormat, using the default settings for the Locale.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     *
     * @return a NumberFormat with default settings
     */
    public NumberFormat getNumberFormat() {
        if (this.numberFormat == null) {
            Locale localeToUse = getLocale();
            this.numberFormat = getFormatProvider(localeToUse).getNumberFormat(localeToUse);
        }
        return numberFormat;
    }

    /**
     * Return the number format for the locale with the  given precision and scale
     *
     * @param precision the precision of the number to be displayed
     * @param scale the scale to be displayed
     * @return a number format with the given scale
     */
    public NumberFormat getNumberFormat(int precision, int scale) {
        //TODO: anyone - the "precision" parameter is not used by this method => remove the parameter or implement it.
        return getNumberFormat(scale, false);
    }

    /**
     * Gets a NumberFormat, using the given precision and scale settings.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     * @param scale number of digits to the right of the decimal that will be shown
     * @param scaleSpecified if you want the scale to be fixed (i.e. show exactly that number of trailing digits)
     * @return the NumberFormat for this localizer with the given scale.
     */
    public NumberFormat getNumberFormat(int scale, boolean scaleSpecified) {
        Locale localeToUse = getLocale();

        // don't use the cached NumberFormat because we are altering it
        NumberFormat nf = getFormatProvider(localeToUse).getNumberFormat(localeToUse);

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
     *
     * @return a NumberFormat instance to format currency
     */
    public NumberFormat getCurrencyFormat() {
        return getCurrencyFormat(getCurrencyLocale());
    }

    protected NumberFormat getCurrencyFormat(Locale currLocale) {
        if (this.currencyFormat == null) {
            this.currencyFormat = getFormatProvider(currLocale).getCurrencyFormat(currLocale);
        }
        return currencyFormat;
    }

    /**
     * Gets a NumberFormat for currency in accounting format, using the default settings for the Locale.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     *
     * @return a NumberFormat instance to format currency in accounting style
     */
    public NumberFormat getAccountingCurrencyFormat() {
        return getAccountingCurrencyFormat(getCurrencyLocale());
    }

    protected NumberFormat getAccountingCurrencyFormat(Locale currLocale) {
        if (this.accountingCurrencyFormat == null) {
            this.accountingCurrencyFormat = getFormatProvider(currLocale).getAccountingCurrencyFormat(currLocale);
        }
        return accountingCurrencyFormat;
    }

    /**
     * Gets a NumberFormat for currency, using the given precision and scale settings.
     * Caller must use doParseNumber() to handle trailing garbage in input string.
     * @param scale number of digits to the right of the decimal that will be shown
     *
     * @return a NumberFormat instance to format currency
     */
    public NumberFormat getCurrencyFormat(int scale) {
        return getCurrencyFormat(getCurrencyLocale(), scale);
    }

    protected NumberFormat getCurrencyFormat(Locale currLocale, int scale)  {
        // don't use the cached NumberFormat because we are altering it
        NumberFormat cf = getFormatProvider(currLocale).getCurrencyFormat(currLocale);

        return adjustCurrencyScale(cf, scale);
    }

    /**
     * Gets a NumberFormat for currency, in accounting format. Using the given precision and
     * scale settings. Caller must use doParseNumber() to handle trailing garbage in input string.
     * @param scale number of digits to the right of the decimal that will be shown
     *
     * @return a NumberFormat instance to format currency in accounting style
     */
    public NumberFormat getAccountingCurrencyFormat(int scale) {
        return getAccountingCurrencyFormat(getCurrencyLocale(), scale);
    }

    protected NumberFormat getAccountingCurrencyFormat(Locale currLocale, int scale)  {
        // don't use the cached NumberFormat because we are altering it
        NumberFormat cf = getFormatProvider(currLocale).getAccountingCurrencyFormat(currLocale);

        return adjustCurrencyScale(cf, scale);
    }

    private NumberFormat adjustCurrencyScale(NumberFormat cf, int scale) {
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
     *
     * @return a NumberFormat instance to format percent
     */
    public NumberFormat getPercentFormat() {
        /*
         * this was removed to work around a problem in the Decimal format object.
         * http://developer.java.sun.com/developer/bugParade/bugs/4252562.html
         * return NumberFormat)data.percentFormat.clone();
         */
        Locale localeToUse = getLocale();
        return getFormatProvider(localeToUse).getPercentFormat(localeToUse);
    }

    /**
     * Parse the given input string using the given format, and make sure the entire string
     * is used up during the parsing and garbage text at the end is not allowed.
     *
     * @param input String to be parsed
     * @param nf NumberFormat to parse the string
     * @return a parsed Number instance
     * @throws ParseException if the input isn't valid against the NumberFormat
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

    /**
     * Retrieve cached MessageFormat for given pattern; constructs and cache it if doesn't exist.
     *
     * @param pattern the pattern for this message format
     * @return a MessageFormat
     */
    public MessageFormat getMessageFormat(String pattern) {
        MessageFormat f = messageFormatCache.get(pattern);
        if (f == null) {
            f = new MessageFormat(pattern, getLocale());
            messageFormatCache.put(pattern, f);
        }
        return f;
    }

    /*
     * MISC
     */

    /**
     * Retrieve the message(label) for given section and parameter(key).
     *
     * @param section label section
     * @param name label parameter(key)
     * @return a format message
     */
    public String getLabel(String section, String name) {
        return this.labelSet.getString(section, name);
    }

    /**
     * Retrieve the message(label) for given section and parameter(key).
     * Will throw exception if not found.
     *
     * @param section label section
     * @param name label parameter(key)
     * @return a format message
     */
    public String getLabelThrow(String section, String name) {
        return this.labelSet.getStringThrow(section, name);
    }

    /**
     * If message exists for given section and parameter(key).
     *
     * @param section label section
     * @param name label parameter(key)
     * @return true if message exists; otherwise false.
     */
    public boolean labelExists(String section, String name) {
        return this.labelSet.labelExists(section, name);
    }

    /**
     * Returns a map containing the contents of an entire label section, or null if the section does not exist.
     *
     * @param section the section to retrieve
     * @return a map of all the values in the section, or null if the section does not exist.
     */
    public Map<String, Object> getSection(String section) {
        return this.labelSet.getSection(section);
    }


    /**
     * Convenience function that calls <CODE>java.text.MessageFormat.format()</CODE> on the label
     * using the <CODE>Object[]</CODE> supplied.
     *
     * @param    section    same as getLabel(section, key)
     * @param    key    same as getLabel(section, key)
     * @param    args    for <CODE>java.text.MessageFormat.format()</CODE>
     * @return the label in the label set at the given section and key
     */
    public String getLabel(String section, String key, Object... args) {
        String labelText = TextUtil.escapeForMessageFormat(getLabel(section, key));
        MessageFormat formatter = getMessageFormat(labelText);
        return formatter.format(args);
    }

    public String getLabelThrow(String section, String key, Object... args) {
        String labelText = TextUtil.escapeForMessageFormat(getLabelThrow(section, key));
        MessageFormat formatter = getMessageFormat(labelText);
        return formatter.format(args);
    }

    public List<String> getList(String section, String baseName) {
        return this.labelSet.getList(section, baseName);
    }

    /**
     * Retrieve the locale associated with this localizer. A setter shouldn't be added because it's
     * assumed the locale is set on instantiation; otherwise, we'd need to fix getMessageFormat()
     * @return the locale associated with this localizer
     */
    public Locale getLocale() {
        return this.locale;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * @return the locale of the language associated with the localizer
     * @deprecated use {@link #getUserLanguage()} instead
     */
    @Deprecated
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
