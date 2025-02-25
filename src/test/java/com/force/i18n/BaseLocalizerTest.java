/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import static org.junit.Assert.assertThrows;

import java.text.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.force.i18n.BaseLocalizer.FormatFixer;
import com.force.i18n.BaseLocalizer.LocalOrGmt;
import com.force.i18n.commons.util.settings.SimpleNonConfigIniFile;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.ibm.icu.impl.jdkadapter.SimpleDateFormatICU;
import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

/**
 * Very simple unit tests to make sure the BaseLocalizer is working correctly.
 *
 * @author smawson
 */
public class BaseLocalizerTest extends TestCase {

    private BaseLocalizer usLocalizer;
    private BaseLocalizer ukLocalizer;
    private SharedLabelSet lSet;
    private final Function<Locale,FormatFixer> originalPredicate;

    public BaseLocalizerTest(String name) throws Exception {
        super(name);
        this.originalPredicate = BaseLocalizer.getLocaleFormatFixer();

        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

        Locale locale = Locale.US;

        this.lSet = getIniFile();

        this.usLocalizer = new BaseLocalizer(locale, locale, tz, HumanLanguage.Helper.get(Locale.US), lSet);
        locale = Locale.UK;

        this.ukLocalizer = new BaseLocalizer(locale, locale, tz, HumanLanguage.Helper.get(Locale.US), lSet);
    }

    @Override
    public void tearDown() {
        if (this.originalPredicate != null) {
            BaseLocalizer.setLocaleFormatFixer(this.originalPredicate);
        }
    }

    private SharedLabelSet getIniFile() {
        //SharedLabelSet ini = new SharedLabelSetImpl("config/labels/localizerLabels.xml");

        TestSimpleIniFile ini = new TestSimpleIniFile();

        ini.set("section1", "label1", "Section1, Label1");
        ini.set("section1", "label2", "Section1, Label2");
        ini.set("section1", "label3", "Section1, Label3");
        ini.set("section1", "test_0", "Foo");
        ini.set("section1", "test_1", "Bar");
        ini.set("section1", "format1", "At {0,time} on {0,date,short}, {1}.");
        ini.set("section2", "bool1", "1");
        ini.set("section2", "bool2", "0");
        ini.set("section2", "bool4", true);
        ini.set("section2", "int1", "1");
        ini.set("section2", "int2", "-1");
        ini.set("section2", "int3", 0);
        ini.set("section2", "real", 3.14f);
        ini.set("section3", "password", "UseModernCrypto!");

        return ini;
    }

    //************** Simple Test Cases *************

    public void testGetLabel() throws Exception {

        String label = this.usLocalizer.getLabel("section1", "label1");

        Assert.assertNotNull(label);
    }

    public void testLocaleInputDateFormat() throws Exception {
        List<TimeZone> failedTimeZones = new ArrayList<>();
        for (String tzStr : TimeZone.getAvailableIDs()) {
            TimeZone tz = TimeZone.getTimeZone(tzStr);
            try {
                BaseLocalizer.getLocaleInputDateFormat(Locale.US, tz);
            } catch(Exception e) {
                failedTimeZones.add(tz);
            }
        }
        if (!failedTimeZones.isEmpty()) {
            fail("Failed to getLocaleInputDateFormat for timezones: " + failedTimeZones.stream().map(tz -> tz.getID()).collect(Collectors.joining(",")));
        }
    }

    public void testFormatLabel() throws Exception {

        Object[] arguments = new Object[] { new Date(), "BaseLocalizerTest was run." };

        String label1 = this.usLocalizer.getLabel("section1", "format1", arguments);
        Assert.assertNotNull(label1);
        String label2 = this.ukLocalizer.getLabel("section1", "format1", arguments);
        Assert.assertNotNull(label2);
    }

    public void testJdkDateFormatFixer_JDK() throws Exception {

         // set to use JDK locale data.
        Function<Locale,FormatFixer> old_predicate = BaseLocalizer.getLocaleFormatFixer();
		Function<Locale,FormatFixer> predicate = loc -> BaseLocalizer.getJDKFormatFixer();
        BaseLocalizer.setLocaleFormatFixer(predicate);

        try {
	        TimeZone tz = BaseLocalizer.GMT_TZ;
	        Date sampleDate = I18nDateUtil.parseTimestamp("2008-03-13 12:00:00");

	        // In JDK 11, they fixed danish (again).  In JDK 17, they reverted it back to the correct ICU format...
	        //assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("da").build(), tz).format(sampleDate));
	        //assertEquals("13/03/2008 12.00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("da").build(), tz).format(sampleDate));

	        // Validate with US
	        assertEquals("3/13/2008", BaseLocalizer.getLocaleDateFormat(Locale.US, tz).format(sampleDate));
	        assertEquals("3/13/2008, 12:00 PM", BaseLocalizer.getLocaleDateTimeFormat(Locale.US, tz).format(sampleDate));

	        // Singapore didn't have the "right" time.
	        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("SG").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("NG").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("GB").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("SG").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("NG").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008, 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("GB").build(), tz).format(sampleDate));
        } finally {
        	BaseLocalizer.setLocaleFormatFixer(old_predicate);
        }
    }



    // List of overridden locales for date/time
    private static Locale[] DATE_FORMAT_INTERESTING_LOCALES = new Locale[] { //
            new Locale.Builder().setLanguage("en").setRegion("SG").build(), //
            new Locale.Builder().setLanguage("en").setRegion("NG").build(), //
            new Locale.Builder().setLanguage("en").setRegion("MY").build(), //
            new Locale.Builder().setLanguage("en").setRegion("BB").build(), //
            new Locale.Builder().setLanguage("en").setRegion("BM").build(), //
            new Locale.Builder().setLanguage("en").setRegion("GH").build(), //
            new Locale.Builder().setLanguage("en").setRegion("ID").build(), //
            new Locale.Builder().setLanguage("ar").build(), //
            new Locale.Builder().setLanguage("ar").setRegion("SA").build(), //
    };

    public void testJdkDateFormatFixer_ICU() throws Exception {
        // set to use ICU locale data.
       Function<Locale,FormatFixer> old_predicate = BaseLocalizer.getLocaleFormatFixer();
	   Function<Locale,FormatFixer> predicate = loc -> BaseLocalizer.getICUFormatFixer();
       BaseLocalizer.setLocaleFormatFixer(predicate);

       try {
	        TimeZone tz = BaseLocalizer.GMT_TZ;
	        Date sampleDate = I18nDateUtil.parseTimestamp("2008-03-13 13:00:00");
	        // In JDK 6, they fixed danish.
	        assertEquals("13.03.2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("da").build(), tz).format(sampleDate));
	        assertEquals("13.03.2008, 13.00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("da").build(), tz).format(sampleDate));

	        // Validate with US
	        assertEquals("3/13/2008", BaseLocalizer.getLocaleDateFormat(Locale.US, tz).format(sampleDate));
	        assertEquals("3/13/2008, 1:00 PM", BaseLocalizer.getLocaleDateTimeFormat(Locale.US, tz).format(sampleDate));

	        // Singapore, en_SG
	        assertEquals("13/3/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("SG").build(), tz).format(sampleDate));
	        assertEquals("13/3/2008, 1:00 pm", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("SG").build(), tz).format(sampleDate));

	        // Nigeria, en_NG
	        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("NG").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008, 13:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("NG").build(), tz).format(sampleDate));

	        // UK, en_GB
	        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("GB").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008, 13:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("GB").build(), tz).format(sampleDate));

	        // India, en_ID, override to en_GB
	        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale.Builder().setLanguage("en").setRegion("ID").build(), tz).format(sampleDate));
	        assertEquals("13/03/2008, 13:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale.Builder().setLanguage("en").setRegion("ID").build(), tz).format(sampleDate));
       } finally {
    	   BaseLocalizer.setLocaleFormatFixer(old_predicate);
       }
    }

    private static Date getDate(int year, int month, int date, int hourOfDay, int minute, int second, TimeZone tz) {
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.clear();
        cal.set(year, month, date, hourOfDay, minute, second);
        return cal.getTime();
    }

    public void testFormatAndParseDateTime() throws Exception {
        TimeZone tz = BaseLocalizer.GMT_TZ;
        Date date = getDate(2018, 8, 14, 10, 23, 0, tz);

        for (Locale locale : DATE_FORMAT_INTERESTING_LOCALES) {
            DateFormat format = BaseLocalizer.getLocaleDateTimeFormat(locale, tz);
            String formatted = format.format(date);
            Assert.assertNotNull(formatted);
            Date parsed = BaseLocalizer.doParseDate(formatted, format);
            Assert.assertEquals(date, parsed);
        }
    }

    public void testFormatAndParseDateTimeViaLocalizer() {
        BaseLocalizer.setLocaleFormatFixer(locale -> BaseLocalizer.getJDKFormatFixer());
        doTestFormatAndParseDateTimeViaLocalizer();
    }

    private void doTestFormatAndParseDateTimeViaLocalizer() {
        final HumanLanguage lang = HumanLanguage.Helper.get(Locale.US);
        final SharedLabelSet labelSet = getIniFile();
        final TimeZone tz = BaseLocalizer.GMT_TZ;
        final Date date = getDate(2018, 8, 14, 10, 23, 0, tz);
        final Map<Locale, Map.Entry<String ,Date>> failedLocales = new HashMap<>();
        for (Locale locale : DATE_FORMAT_INTERESTING_LOCALES) {
            BaseLocalizer localizer = new BaseLocalizer(locale, locale, tz, lang, labelSet);
            String formattedDate = localizer.formatDateTime(date);
            try {
                Date parsedDate = localizer.parseDateTime(formattedDate);
                if (!date.equals(parsedDate)) {
                    failedLocales.put(locale, new AbstractMap.SimpleEntry<String, Date>(formattedDate, parsedDate));
                }
            } catch (ParseException e) {
                failedLocales.put(locale, new AbstractMap.SimpleEntry<String, Date>(formattedDate, null));
            }
        }
        if (!failedLocales.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Test date: %s\n", BaseLocalizer.formatISO8601(date)));
            for (Locale locale : failedLocales.keySet()) {
                sb.append(String.format("Locale: %s Formatted date: %s Parsed date: %s\n",
                        locale.toString(), failedLocales.get(locale).getKey(),
                        failedLocales.get(locale).getValue() == null ? "(null)"
                                : BaseLocalizer.formatISO8601(failedLocales.get(locale).getValue())));
            }
            Assert.fail(sb.toString());
        }
    }

    public void testFormatAndParse2DigitYearDateTimeViaLocalizer() {
        BaseLocalizer.setLocaleFormatFixer(locale -> BaseLocalizer.getJDKFormatFixer());
        doTestFormatAndParse2DigitYearDateTimeViaLocalizer();
    }

    private void doTestFormatAndParse2DigitYearDateTimeViaLocalizer() {
        final HumanLanguage lang = HumanLanguage.Helper.get(Locale.US);
        final SharedLabelSet labelSet = getIniFile();
        final Map<TimeZone, Throwable> failedTimeZones = new HashMap<>();
        for (String tzId : TimeZone.getAvailableIDs()) {
            TimeZone tz = TimeZone.getTimeZone(tzId);
            BaseLocalizer localizer = new BaseLocalizer(Locale.US, Locale.US, tz, lang, labelSet);
            try {
                // Sanity check
                doFormatParseRoundTrip("12/30/2059", "12/30/2059", localizer);
                doFormatParseRoundTrip("12/30/1959", "12/30/1959", localizer);
                doFormatParseRoundTrip("12/31/2059", "12/31/2059", localizer);
                doFormatParseRoundTrip("12/31/1959", "12/31/1959", localizer);
                doFormatParseRoundTrip("1/1/1960", "1/1/1960", localizer);
                doFormatParseRoundTrip("1/1/2060", "1/1/2060", localizer);
                doFormatParseRoundTrip("1/2/1960", "1/2/1960", localizer);
                doFormatParseRoundTrip("1/2/2060", "1/2/2060", localizer);
                // Two digits
                doFormatParseRoundTrip("12/30/59", "12/30/2059", localizer);
                doFormatParseRoundTrip("12/31/59", "12/31/2059", localizer);
                doFormatParseRoundTrip("1/1/60", "1/1/1960", localizer);
                doFormatParseRoundTrip("1/2/60", "1/2/1960", localizer);
                doFormatParseRoundTrip("12/30/99", "12/30/1999", localizer);
                doFormatParseRoundTrip("12/31/99", "12/31/1999", localizer);
                doFormatParseRoundTrip("1/1/00", "1/1/2000", localizer);
                doFormatParseRoundTrip("1/2/00", "1/2/2000", localizer);
            } catch (Throwable t) {
                failedTimeZones.put(tz, t);
            }
        }
        if (!failedTimeZones.isEmpty()) {
            fail(failedTimeZones.entrySet().stream().map(x -> x.getKey().getID() + ": " + x.getValue().getMessage()).collect(Collectors.joining("\n")));
        }
    }

    private static void doFormatParseRoundTrip(String parseString, String formatString, BaseLocalizer localizer) throws ParseException {
        // Bug for 1960/1/1 for timezone inheritted from Africa/Nairobi on ICU.
        // ICU throws "Unparseable date" for 1/1/60 on these timezones.
        // There is a time gap (+0245 until 12/31/1959, but +0300 from 1/1/1960)
        // where there is no valid time from 00:00 to 00:15 on these timezones.
        // and ICU seems to not liking them.
        // # Zone	NAME		STDOFF	RULES	FORMAT	[UNTIL]
        // Zone	Africa/Nairobi	2:27:16	-	LMT	1928 Jul
        //             3:00	-	EAT	1930
        //             2:30	-	+0230	1940
        //             2:45	-	+0245	1960
        //             3:00	-	EAT
        // See https://data.iana.org/time-zones/tzdb/africa
        Set<String> nairobiTimeZones = ImmutableSet.of( //
                "Africa/Addis_Ababa", //
                "Africa/Asmara", //
                "Africa/Asmera", //
                "Africa/Dar_es_Salaam", //
                "Africa/Djibouti", //
                "Africa/Kampala", //
                "Africa/Mogadishu", //
                "Africa/Nairobi", //
                "EAT", //
                "Indian/Antananarivo", //
                "Indian/Comoro", //
                "Indian/Maldives", //
                "Indian/Mayotte" //
        );
        try {
            Date date = localizer.parseDate(parseString, LocalOrGmt.LOCAL);
            if (BaseLocalizer.getLocaleFormatFixer().apply(localizer.getLocale()) == BaseLocalizer.getICUFormatFixer() && //
                    ("1/1/1960".equals(parseString) || "1/1/60".equals(parseString)) && //
                    nairobiTimeZones.contains(localizer.getTimeZone().getID())) {
                fail("Africa/Nairobi timezone bug on ICU is fixed?");
            }
            assertEquals(formatString, localizer.formatDate(date, LocalOrGmt.LOCAL));
        } catch (ParseException e) {
            if (BaseLocalizer.getLocaleFormatFixer().apply(localizer.getLocale()) == BaseLocalizer.getICUFormatFixer() && //
                    ("1/1/1960".equals(parseString) || "1/1/60".equals(parseString)) && //
                    nairobiTimeZones.contains(localizer.getTimeZone().getID())) {
                assertTrue(e.getMessage().contains("Unparseable date"));
            } else {
                throw e;
            }
        }
    }

    public void testUSLocalizerFormatDateTime() throws Exception {
         BaseLocalizer.setLocaleFormatFixer(locale -> BaseLocalizer.getICUFormatFixer());
         Date date = I18nDateUtil.parseTimestamp("2013-03-05 07:02:00"); // Time in GMT

          // Tests for formatting in US Localizer TimeZone (PST)
         //Test format date
         assertEquals("3/4/2013", this.usLocalizer.formatDate(date, DateFormat.SHORT));
         assertEquals("Mar 4, 2013", this.usLocalizer.formatDate(date, DateFormat.MEDIUM));
         assertEquals("March 4, 2013", this.usLocalizer.formatDate(date, DateFormat.LONG));

         //Test format date-time
         assertEquals("3/4/2013, 11:02 PM", this.usLocalizer.formatDateTime(date, DateFormat.SHORT));
         assertEquals("Mar 4, 2013, 11:02:00 PM", this.usLocalizer.formatDateTime(date, DateFormat.MEDIUM));
         assertEquals("3/4/2013, 11:02:00 PM PST", this.usLocalizer.formatDateTime(date, DateFormat.LONG));

         //Test format time
         assertEquals("11:02 PM", this.usLocalizer.formatTime(date, DateFormat.SHORT));
         assertEquals("11:02:00 PM", this.usLocalizer.formatTime(date, DateFormat.MEDIUM));
         assertEquals("11:02:00 PM PST", this.usLocalizer.formatTime(date, DateFormat.LONG));

    }

    public void testUSLocalizerParseAndFormatDateTime() throws Exception {
        BaseLocalizer.setLocaleFormatFixer(locale -> BaseLocalizer.getICUFormatFixer());

         // Test parse date in short,medium and long styles
         Date short_date = this.usLocalizer.parseDate("3/4/2013", DateFormat.SHORT);
         Date medium_date = this.usLocalizer.parseDate("Mar 4, 2013", DateFormat.MEDIUM);
         Date long_date = this.usLocalizer.parseDate("March 4, 2013", DateFormat.LONG);

         assertEquals("3/4/2013", this.usLocalizer.formatDate(short_date, DateFormat.SHORT));
         assertEquals("Mar 4, 2013", this.usLocalizer.formatDate(medium_date, DateFormat.MEDIUM));
         assertEquals("March 4, 2013", this.usLocalizer.formatDate(long_date, DateFormat.LONG));

         // Test parse date-time in short,medium and long styles
         Date short_date_time = this.usLocalizer.parseDateTime("3/4/2013, 11:02 PM", DateFormat.SHORT);
         Date medium_date_time = this.usLocalizer.parseDateTime("Mar 4, 2013, 11:02:00 PM", DateFormat.MEDIUM);
         Date long_date_time = this.usLocalizer.parseDateTime("3/4/2013, 11:02:00 PM PST", DateFormat.LONG);

         assertEquals("3/4/2013, 11:02 PM", this.usLocalizer.formatDateTime(short_date_time, DateFormat.SHORT));
         assertEquals("Mar 4, 2013, 11:02:00 PM", this.usLocalizer.formatDateTime(medium_date_time, DateFormat.MEDIUM));
         assertEquals("3/4/2013, 11:02:00 PM PST", this.usLocalizer.formatDateTime(long_date_time, DateFormat.LONG));

         // Test parse time in short,medium and long styles
         Date short_time = this.usLocalizer.parseTime("11:02 PM", DateFormat.SHORT);
         Date medium_time = this.usLocalizer.parseTime("11:02:00 PM", DateFormat.MEDIUM);
         Date long_time = this.usLocalizer.parseTime("11:02:00 PM PST", DateFormat.LONG);

         assertEquals("11:02 PM", this.usLocalizer.formatTime(short_time, DateFormat.SHORT));
         assertEquals("11:02:00 PM", this.usLocalizer.formatTime(medium_time, DateFormat.MEDIUM));
         assertEquals("11:02:00 PM PST", this.usLocalizer.formatTime(long_time, DateFormat.LONG));

    }

    public void testFormatAndParseDateTimeVariations() throws Exception {
        BaseLocalizer.setLocaleFormatFixer(locale -> BaseLocalizer.getICUFormatFixer());
        TimeZone time_zone = TimeZone.getTimeZone("America/Los_Angeles");

        // Test static parse and format date in short,medium and long styles
        Date short_date = BaseLocalizer.parseDate("3/4/2013", DateFormat.SHORT, Locale.US, time_zone);
        Date medium_date = BaseLocalizer.parseDate("Mar 4, 2013", DateFormat.MEDIUM, Locale.US, time_zone);
        Date long_date = BaseLocalizer.parseDate("March 4, 2013", DateFormat.LONG, Locale.US, time_zone);

        assertEquals("3/4/2013", BaseLocalizer.formatDate(short_date, DateFormat.SHORT, Locale.US, time_zone));
        assertEquals("Mar 4, 2013", BaseLocalizer.formatDate(medium_date, DateFormat.MEDIUM, Locale.US, time_zone));
        assertEquals("March 4, 2013", BaseLocalizer.formatDate(long_date, DateFormat.LONG, Locale.US, time_zone));

        // Test static parse and format date-time in short,medium and long styles
        Date short_date_time = BaseLocalizer.parseDateTime("3/4/2013, 11:02 PM", DateFormat.SHORT, Locale.US, time_zone);
        Date medium_date_time = BaseLocalizer.parseDateTime("Mar 4, 2013, 11:02:00 PM", DateFormat.MEDIUM, Locale.US, time_zone);
        Date long_date_time = BaseLocalizer.parseDateTime("3/4/2013, 11:02:00 PM PST", DateFormat.LONG, Locale.US, time_zone);

        assertEquals("3/4/2013, 11:02 PM", BaseLocalizer.formatDateTime(short_date_time, DateFormat.SHORT, Locale.US, time_zone));
        assertEquals("Mar 4, 2013, 11:02:00 PM", BaseLocalizer.formatDateTime(medium_date_time, DateFormat.MEDIUM, Locale.US, time_zone));
        assertEquals("3/4/2013, 11:02:00 PM PST", BaseLocalizer.formatDateTime(long_date_time, DateFormat.LONG, Locale.US, time_zone));

        // Test static parse and format time in short,medium and long styles
        Date short_time = BaseLocalizer.parseTime("11:02 PM", DateFormat.SHORT, Locale.US, time_zone);
        Date medium_time = BaseLocalizer.parseTime("11:02:00 PM", DateFormat.MEDIUM, Locale.US, time_zone);
        Date long_time = BaseLocalizer.parseTime("11:02:00 PM PST", DateFormat.LONG, Locale.US, time_zone);

        assertEquals("11:02 PM", BaseLocalizer.formatTime(short_time, DateFormat.SHORT, Locale.US, time_zone));
        assertEquals("11:02:00 PM", BaseLocalizer.formatTime(medium_time, DateFormat.MEDIUM, Locale.US, time_zone));
        assertEquals("11:02:00 PM PST", BaseLocalizer.formatTime(long_time, DateFormat.LONG, Locale.US, time_zone));
    }

    public void testIniFile() throws Exception {
    	SimpleNonConfigIniFile iniFile = (SimpleNonConfigIniFile) this.usLocalizer.getLabelSet();
    	assertEquals(null, iniFile.getList("section1", "empty"));
    	assertEquals(Collections.emptyList(), iniFile.getList("section1", "empty", Collections.emptyList()));
    	assertEquals(ImmutableList.of("Foo", "Bar"), iniFile.getList("section1", "test"));
    	assertTrue(iniFile.getBoolean("section2", "bool1"));
    	assertFalse(iniFile.getBoolean("section2", "bool2"));
    	assertFalse(iniFile.getBoolean("section2", "bool3"));
    	assertTrue(iniFile.getBoolean("section2", "bool3", true));
    	assertTrue(iniFile.getBoolean("section2", "bool4"));
    	assertEquals(1, iniFile.getInt("section2", "int1"));
    	assertEquals(-1, iniFile.getInt("section2", "int2"));
    	assertEquals(0, iniFile.getInt("section2", "int3"));
    	assertEquals(5, iniFile.getInt("section2", "int4", 5));
    	assertEquals(1.0f, iniFile.getFloat("section2", "int1"));
    	assertEquals(-1.0f, iniFile.getFloat("section2", "int2"));
    	assertEquals(3.14f, iniFile.getFloat("section2", "real"));
    	assertEquals(2.7f, iniFile.getFloat("section2", "unreal", 2.7f));
    	assertEquals("xxxxxxxx", iniFile.getCensoredString("section3", "password", "donotuse"));
    }

    /**
     * Test case to make sure all ICU locale time formats have AM/PM set for 12-hour times.
     * If this case fails for certain locale, you may consider to have a hot fix.
     */
    public void test12HTimeWithAMPM() {
        Map<ULocale, String> invalid = new HashMap<>();
        for (ULocale u_loc : ULocale.getAvailableLocales()) {
        	if (u_loc.getLanguage().equals("mi")) continue;  // Ignore maori.
        	if (u_loc.getCountry().equals("AR")) continue;  // es_AR is wrong in ICU 46, but checkAM fixes it.
            for (int style : ImmutableSet.of(DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG)) {
                com.ibm.icu.text.DateFormat df = com.ibm.icu.text.DateFormat.getTimeInstance(style, u_loc);
                com.ibm.icu.text.SimpleDateFormat tf = (com.ibm.icu.text.SimpleDateFormat) df;
                String p = tf.toPattern();

                // H for 24-hours time format, h for 12-hours format.
                if ((p.indexOf('a') == -1) && (p.indexOf('k') == -1) && (p.indexOf('B') == -1)) {
                    // Locale has 12 hour time, but no am/pm marker.
                    if (-1 == p.indexOf('H')) {
                        invalid.put(u_loc, style + ":" + p);
                    }
                }
            }
        }
        assertEquals("Locales with incorrect time formats (12 hours with no am/pm marker).  Hot fix may be needed.", ImmutableMap.of(), invalid);
    }

    /**
     * NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode() may through exception when ICU
     * has different currency code comparing to JDK. One example is, currency code for be_BY is BYR before JDK8_131
     * while ICU_59.1(and above) has BYN. This test case is used to capture all unmapped currency codes.
     *
     */
    public void testICUCurrencyCode2JDK() {
        List<String> res = new ArrayList<String>();
        ImmutableSet<String> exceptions = ImmutableSet.of("MRU", "VES", "STN", "XCG", "ZWG");
        for (ULocale u_loc : ULocale.getAvailableLocales()) {
            com.ibm.icu.text.NumberFormat df = com.ibm.icu.text.NumberFormat.getCurrencyInstance(u_loc);
            com.ibm.icu.util.Currency cur = df.getCurrency();

            if (cur == null) continue; // not supported by ICU.

            String code = cur.getCurrencyCode();

            try {
                Currency.getInstance(code);
            } catch (IllegalArgumentException ex) {
                if (!exceptions.contains(code)) {
                    res.add(code);
                }
            }
        }
        assertTrue("Following currency codes are not supported by JDK:" + res, res.isEmpty());
    }

    /**
     * Test case to make sure years are in 4-digit pattern.
     *
     */
    public void test4DigitYears() {
        List<String> res = new ArrayList<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            DateFormat df = BaseLocalizer.getLocaleDateFormat(locale, TimeZone.getDefault());
            String pattern = ((SimpleDateFormat) df).toPattern();

            if (!pattern.contains("yyyy")) {
                res.add(res.toString());
            }
        }

        assertTrue("Following locales don't have 4-digit year patten:" + res, res.isEmpty());
    }

    /*
     * ICU-21301 failed to parse the transition date (e.g. 6/9/2020 for "America/Santiago")
     * https://unicode-org.atlassian.net/browse/ICU-21301
     */
    @Test
    public void testParseDateOnDST() {
        final Locale locale = Locale.GERMANY;
        final HumanLanguage language = HumanLanguage.Helper.get(Locale.US);

        // set to ICU mode
        BaseLocalizer.setLocaleFormatFixer(l -> BaseLocalizer.getICUFormatFixer());

        Calendar cal = Calendar.getInstance();
        cal.clear();

        final BaseLocalizer icuLocalizer = new BaseLocalizer(locale, locale, TimeZone.getTimeZone("America/Santiago"),
                language, lSet);
        assertTrue(icuLocalizer.getInputDateFormat() instanceof SimpleDateFormatICU);
        assertThrows(ParseException.class, () -> icuLocalizer.parseDate("06.09.2020", DateFormat.SHORT));
    }

    private static class TestSimpleIniFile extends SimpleNonConfigIniFile implements SharedLabelSet {
        @Override
        public boolean labelExists(String section, String param) {
            return getString(section, param, null) != null;
        }
    }

    /**
     * Test case for accounting currency format.
     */
    public void testAccoutingCurrencyFormat() {
        // set to use ICU locale data.
       Function<Locale,FormatFixer> old_predicate = BaseLocalizer.getLocaleFormatFixer();
       Function<Locale,FormatFixer> predicate = loc -> BaseLocalizer.getICUFormatFixer();
       BaseLocalizer.setLocaleFormatFixer(predicate);

       NumberFormat nf = usLocalizer.getCurrencyFormat();
       NumberFormat accounting_nf = usLocalizer.getAccountingCurrencyFormat();

       // standard format
       assertEquals("$12,345,678.57", nf.format(12345678.567));
       assertEquals("-$12,345,678.57", nf.format(-12345678.567));

       // accounting format
       assertEquals("$12,345,678.57", accounting_nf.format(12345678.567));
       assertEquals("($12,345,678.57)", accounting_nf.format(-12345678.567));

       BaseLocalizer.setLocaleFormatFixer(old_predicate);
    }

    public void testParseAccountingCurrencyFormat() throws ParseException {
        // set to use ICU locale data.
       Function<Locale,FormatFixer> old_predicate = BaseLocalizer.getLocaleFormatFixer();
       Function<Locale,FormatFixer> predicate = loc -> BaseLocalizer.getICUFormatFixer();
       BaseLocalizer.setLocaleFormatFixer(predicate);

       Number num = usLocalizer.parseAccountingCurrency("($12,345,678.57)");
       assertEquals(-12345678.57, num.doubleValue(), 0.001);

       BaseLocalizer.setLocaleFormatFixer(old_predicate);
    }

}
