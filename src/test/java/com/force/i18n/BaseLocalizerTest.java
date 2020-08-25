/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;

import org.junit.Assert;

import com.force.i18n.BaseLocalizer.FormatFixer;
import com.force.i18n.commons.util.settings.SimpleNonConfigIniFile;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
    private final Function<Locale,FormatFixer> originalPredicate;
    
    public BaseLocalizerTest(String name) throws Exception {
        super(name);
        this.originalPredicate = BaseLocalizer.getLocalePredicate();

        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

        Locale locale = Locale.US;

        SharedLabelSet lSet = getIniFile();

        this.usLocalizer = new BaseLocalizer(locale, locale, tz, HumanLanguage.Helper.get(Locale.US), lSet);
        locale = Locale.UK;

        this.ukLocalizer = new BaseLocalizer(locale, locale, tz, HumanLanguage.Helper.get(Locale.US), lSet);
    }
    
    
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

    public void testFormatLabel() throws Exception {

        Object[] arguments = new Object[] { new Date(), "BaseLocalizerTest was run." };

        String label1 = this.usLocalizer.getLabel("section1", "format1", arguments);
        Assert.assertNotNull(label1);
        String label2 = this.ukLocalizer.getLabel("section1", "format1", arguments);
        Assert.assertNotNull(label2);
    }

    public void testJdkDateFormatFixer_JDK() throws Exception {
        
         // set to use JDK locale data.
        Function<Locale,FormatFixer> old_predicate = BaseLocalizer.getLocalePredicate();
        @SuppressWarnings("deprecation")
		Function<Locale,FormatFixer> predicate = loc -> BaseLocalizer.JdkFormatFixer.INSTANCE;
        BaseLocalizer.setLocaleFormatFixer(predicate);
        
        TimeZone tz = BaseLocalizer.GMT_TZ;
        Date sampleDate = I18nDateUtil.parseTimestamp("2008-03-13 12:00:00");
        // In JDK 6, they fixed danish.
        assertEquals("13-03-2008", BaseLocalizer.getLocaleDateFormat(new Locale("da"), tz).format(sampleDate));
        assertEquals("13-03-2008 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale("da"), tz).format(sampleDate));

        // Validate with US
        assertEquals("3/13/2008", BaseLocalizer.getLocaleDateFormat(Locale.US, tz).format(sampleDate));
        assertEquals("3/13/2008 12:00 PM", BaseLocalizer.getLocaleDateTimeFormat(Locale.US, tz).format(sampleDate));

        // Singapore didn't have the "right" time.
        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale("en", "SG"), tz).format(sampleDate));
        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale("en", "NG"), tz).format(sampleDate));
        assertEquals("13/03/2008", BaseLocalizer.getLocaleDateFormat(new Locale("en", "GB"), tz).format(sampleDate));
        assertEquals("13/03/2008 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale("en", "SG"), tz).format(sampleDate));
        assertEquals("13/03/2008 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale("en", "NG"), tz).format(sampleDate));
        assertEquals("13/03/2008 12:00", BaseLocalizer.getLocaleDateTimeFormat(new Locale("en", "GB"), tz).format(sampleDate));

        BaseLocalizer.setLocaleFormatFixer(old_predicate);
    }


    // List of overridden locales for date/time
    private static Locale[] DATE_FORMAT_INTERESTING_LOCALES = new Locale[] { //
            new Locale("en", "SG"), //
            new Locale("en", "NG"), //
            new Locale("en", "MY"), //
            new Locale("en", "BB"), //
            new Locale("en", "BM"), //
            new Locale("en", "GH"), //
            new Locale("en", "ID"), //
            new Locale("ar"), //
            new Locale("ar", "SA"), //
    };

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

    @SuppressWarnings("deprecation")
	public void testFormatAndParseDateTimeViaLocalizer() {
        BaseLocalizer.setLocaleFormatFixer(locale -> BaseLocalizer.JdkFormatFixer.INSTANCE);
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

    public void testUSLocalizerFormatDateTime() throws Exception {
         Date date = I18nDateUtil.parseTimestamp("2013-03-05 07:02:00"); // Time in GMT
    	    
          // Tests for formatting in US Localizer TimeZone (PST)
         //Test format date 
         assertEquals("3/4/2013", this.usLocalizer.formatDate(date, DateFormat.SHORT));
         assertEquals("Mar 4, 2013", this.usLocalizer.formatDate(date, DateFormat.MEDIUM));
         assertEquals("March 4, 2013", this.usLocalizer.formatDate(date, DateFormat.LONG));         

         //Test format date-time
         assertEquals("3/4/2013 11:02 PM", this.usLocalizer.formatDateTime(date, DateFormat.SHORT));
         assertEquals("Mar 4, 2013 11:02:00 PM", this.usLocalizer.formatDateTime(date, DateFormat.MEDIUM));
         assertEquals("3/4/2013 11:02:00 PM PST", this.usLocalizer.formatDateTime(date, DateFormat.LONG));   
         
         //Test format time
         assertEquals("11:02 PM", this.usLocalizer.formatTime(date, DateFormat.SHORT));
         assertEquals("11:02:00 PM", this.usLocalizer.formatTime(date, DateFormat.MEDIUM));
         assertEquals("11:02:00 PM PST", this.usLocalizer.formatTime(date, DateFormat.LONG));
             
    }
    
    public void testUSLocalizerParseAndFormatDateTime() throws Exception {	
    	
         // Test parse date in short,medium and long styles
         Date short_date = this.usLocalizer.parseDate("3/4/2013", DateFormat.SHORT);
         Date medium_date = this.usLocalizer.parseDate("Mar 4, 2013", DateFormat.MEDIUM);
         Date long_date = this.usLocalizer.parseDate("March 4, 2013", DateFormat.LONG);
          
         assertEquals("3/4/2013", this.usLocalizer.formatDate(short_date, DateFormat.SHORT));
         assertEquals("Mar 4, 2013", this.usLocalizer.formatDate(medium_date, DateFormat.MEDIUM));
         assertEquals("March 4, 2013", this.usLocalizer.formatDate(long_date, DateFormat.LONG));
         
         // Test parse date-time in short,medium and long styles
         Date short_date_time = this.usLocalizer.parseDateTime("3/4/2013 11:02 PM", DateFormat.SHORT);
         Date medium_date_time = this.usLocalizer.parseDateTime("Mar 4, 2013 11:02:00 PM", DateFormat.MEDIUM);
         Date long_date_time = this.usLocalizer.parseDateTime("3/4/2013 11:02:00 PM PST", DateFormat.LONG);
                 
         assertEquals("3/4/2013 11:02 PM", this.usLocalizer.formatDateTime(short_date_time, DateFormat.SHORT));
         assertEquals("Mar 4, 2013 11:02:00 PM", this.usLocalizer.formatDateTime(medium_date_time, DateFormat.MEDIUM));
         assertEquals("3/4/2013 11:02:00 PM PST", this.usLocalizer.formatDateTime(long_date_time, DateFormat.LONG));
         
         // Test parse time in short,medium and long styles
         Date short_time = this.usLocalizer.parseTime("11:02 PM", DateFormat.SHORT);
         Date medium_time = this.usLocalizer.parseTime("11:02:00 PM", DateFormat.MEDIUM);
         Date long_time = this.usLocalizer.parseTime("11:02:00 PM PST", DateFormat.LONG);
         
         assertEquals("11:02 PM", this.usLocalizer.formatTime(short_time, DateFormat.SHORT));
         assertEquals("11:02:00 PM", this.usLocalizer.formatTime(medium_time, DateFormat.MEDIUM));
         assertEquals("11:02:00 PM PST", this.usLocalizer.formatTime(long_time, DateFormat.LONG));

    }
    
    public void testFormatAndParseDateTimeVariations() throws Exception {
        TimeZone time_zone = TimeZone.getTimeZone("America/Los_Angeles");

        // Test static parse and format date in short,medium and long styles
        Date short_date = BaseLocalizer.parseDate("3/4/2013", DateFormat.SHORT, Locale.US, time_zone);
        Date medium_date = BaseLocalizer.parseDate("Mar 4, 2013", DateFormat.MEDIUM, Locale.US, time_zone);
        Date long_date = BaseLocalizer.parseDate("March 4, 2013", DateFormat.LONG, Locale.US, time_zone);
            
        assertEquals("3/4/2013", BaseLocalizer.formatDate(short_date, DateFormat.SHORT, Locale.US, time_zone));
        assertEquals("Mar 4, 2013", BaseLocalizer.formatDate(medium_date, DateFormat.MEDIUM, Locale.US, time_zone));
        assertEquals("March 4, 2013", BaseLocalizer.formatDate(long_date, DateFormat.LONG, Locale.US, time_zone));
        
        // Test static parse and format date-time in short,medium and long styles
        Date short_date_time = BaseLocalizer.parseDateTime("3/4/2013 11:02 PM", DateFormat.SHORT, Locale.US, time_zone);
        Date medium_date_time = BaseLocalizer.parseDateTime("Mar 4, 2013 11:02:00 PM", DateFormat.MEDIUM, Locale.US, time_zone);
        Date long_date_time = BaseLocalizer.parseDateTime("3/4/2013 11:02:00 PM PST", DateFormat.LONG, Locale.US, time_zone);
                
        assertEquals("3/4/2013 11:02 PM", BaseLocalizer.formatDateTime(short_date_time, DateFormat.SHORT, Locale.US, time_zone));
        assertEquals("Mar 4, 2013 11:02:00 PM", BaseLocalizer.formatDateTime(medium_date_time, DateFormat.MEDIUM, Locale.US, time_zone));
        assertEquals("3/4/2013 11:02:00 PM PST", BaseLocalizer.formatDateTime(long_date_time, DateFormat.LONG, Locale.US, time_zone));
        
        // Test static parse and format time in short,medium and long styles
        Date short_time = BaseLocalizer.parseTime("11:02 PM", DateFormat.SHORT, Locale.US, time_zone);
        Date medium_time = BaseLocalizer.parseTime("11:02:00 PM", DateFormat.MEDIUM, Locale.US, time_zone);
        Date long_time = BaseLocalizer.parseTime("11:02:00 PM PST", DateFormat.LONG, Locale.US, time_zone);
        
        assertEquals("11:02 PM", BaseLocalizer.formatTime(short_time, DateFormat.SHORT, Locale.US, time_zone));
        assertEquals("11:02:00 PM", BaseLocalizer.formatTime(medium_time, DateFormat.MEDIUM, Locale.US, time_zone));
        assertEquals("11:02:00 PM PST", BaseLocalizer.formatTime(long_time, DateFormat.LONG, Locale.US, time_zone));        	    
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
     * Test case to make sure all the JDK time zone can be mapped to ICU time zone.
     *
     */
    public void testJDKTZtoICU() {
        Set<String> jdk_set = new HashSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
        Set<String> icu_set = new HashSet<String>(Arrays.asList(com.ibm.icu.util.TimeZone.getAvailableIDs()));

        jdk_set.removeAll(icu_set);

        assertTrue("Following JDK time zone IDs are not supported by ICU, " + jdk_set, jdk_set.isEmpty());
    }

    /**
     * NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode() may through exception when ICU
     * has different currency code comparing to JDK. One example is, currency code for be_BY is BYR before JDK8_131
     * while ICU_59.1(and above) has BYN. This test case is used to capture all unmapped currency codes.
     *
     */
    public void testICUCurrencyCode2JDK() {
        List<String> res = new ArrayList<String>();
        ImmutableSet<String> exceptions = ImmutableSet.of("BYN", "STN", "MRU", "VES");
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

    private static class TestSimpleIniFile extends SimpleNonConfigIniFile implements SharedLabelSet {
        @Override
        public boolean labelExists(String section, String param) {
            return getString(section, param, null) != null;
        }
    }
}
