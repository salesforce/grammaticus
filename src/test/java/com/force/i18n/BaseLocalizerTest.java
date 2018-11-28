/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.*;

import junit.framework.TestCase;

import org.junit.Assert;

import com.force.i18n.commons.util.settings.SimpleNonConfigIniFile;

/**
 * Very simple unit tests to make sure the BaseLocalizer is working correctly.
 *
 * @author smawson
 */
public class BaseLocalizerTest extends TestCase {

    private BaseLocalizer usLocalizer;
    private BaseLocalizer ukLocalizer;

    public BaseLocalizerTest(String name) throws Exception {
        super(name);

        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

        Locale locale = Locale.US;

        SharedLabelSet lSet = getIniFile();

        this.usLocalizer = new BaseLocalizer(locale, locale, tz, HumanLanguage.Helper.get(Locale.US), lSet);

        locale = Locale.UK;

        this.ukLocalizer = new BaseLocalizer(locale, locale, tz, HumanLanguage.Helper.get(Locale.US), lSet);
    }

    private SharedLabelSet getIniFile() {
        //SharedLabelSet ini = new SharedLabelSetImpl("config/labels/localizerLabels.xml");

        TestSimpleIniFile ini = new TestSimpleIniFile();

        ini.set("section1", "label1", "Section1, Label1");
        ini.set("section1", "label2", "Section1, Label2");
        ini.set("section1", "label3", "Section1, Label3");
        ini.set("section1", "format1", "At {0,time} on {0,date,short}, {1}.");

        return ini;
    }

    //************** Simple Test Cases *************

    public void testGetLabel() throws Exception {

        String label = this.usLocalizer.getLabel("section1", "label1");

        Assert.assertNotNull(label);
    }

    public void testFormatLabel() throws Exception {

        Object[] arguments = new Object[]{new Date(), "BaseLocalizerTest was run."};

        String label1 = this.usLocalizer.getLabel("section1", "format1", arguments);
        Assert.assertNotNull(label1);
        String label2 = this.ukLocalizer.getLabel("section1", "format1", arguments);
        Assert.assertNotNull(label2);
    }


    public void testJdkDateFormatFixer() throws Exception {
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

    }

    private static class TestSimpleIniFile extends SimpleNonConfigIniFile implements SharedLabelSet {
        @Override
        public boolean labelExists(String section, String param) {
            return getString(section, param, null) != null;
        }
    }
}
