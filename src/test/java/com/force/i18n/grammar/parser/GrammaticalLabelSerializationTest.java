/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.*;
import java.util.logging.Logger;

import com.force.i18n.*;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.google.common.collect.ImmutableList;

/**
 * Test various issues around serialization of LabelSets along with invariant testing
 *
 * @author stamm
 */
public class GrammaticalLabelSerializationTest extends BaseGrammaticalLabelTest {
    private static final Logger logger = Logger.getLogger(GrammaticalLabelSerializationTest.class.getName());
    public GrammaticalLabelSerializationTest(String name) {
        super(name);
    }


    /**
     * Given a locale, retrieves the labels associated with that language.
     * This method was created to be used to retrieve the label set for end-user languages,
     * but can also be used to retrieve the label set for fully-supported languages.
     * NOTE that the label set for fully-supported languages can also be retrieved from the
     * Localizer
     */


    public void testSerializeDictionary() throws Exception {
        GrammaticalLabelSetLoader loader = getLoader();
        for (String languageStr : ImmutableList.of(LanguageConstants.JAPANESE, LanguageConstants.ENGLISH_US, LanguageConstants.ENGLISH_CA)) {
        	HumanLanguage language = LanguageProviderFactory.get().getLanguage(languageStr);
            GrammaticalLabelSet labelSet;
            try {
                labelSet = loader.getSet(language);
            } catch (Exception e) {
                continue;
            }
            File file = File.createTempFile("testSerialLabels" + language.getLocale(), "");
            try {
                long start = System.nanoTime();
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(labelSet);
                } catch (NotSerializableException ex) {
                    System.err.println("Failure to serialize " + language);
                    throw ex;
                }

                logger.info("Wrote " + language + " labels in " + (System.nanoTime() - start) / 1000000
                        + " msec of size " + file.length());

                start = System.nanoTime();
                GrammaticalLabelSet copy;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    copy = (GrammaticalLabelSet)ois.readObject();
                }
                logger.info("Read " + language + " labels in " + (System.nanoTime() - start) / 1000000 + " msec");
                // Make sure the noun forms are the same

                assertEquals("Serialized labels aren't the same", labelSet.getSection("ModStamp"), copy.getSection("ModStamp"));
            } finally {
                file.delete();
            }
        }
    }

    public void testSerializeDictionarySpeedTest() throws Exception {
        GrammaticalLabelSetLoader loader = getLoader();
        _testSerializeDictionarySpeedTest(loader, "InMemTest");

    }

    public void testSerializeDictionaryFileCacheSpeedTest() throws Exception {
        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(LanguageConstants.ENGLISH_US);
        GrammaticalLabelSetFileCacheLoader loader = new GrammaticalLabelSetFileCacheLoader(getDescriptor(ENGLISH), null);
        File f = loader.getCacheDir();
        if (f.exists()) {
            for (File labelFile : f.listFiles()) {
            	if (labelFile.getName().endsWith(".cache")) {
            		labelFile.delete();
            	}
            }
        }
        _testSerializeDictionarySpeedTest(loader, "CacheTestInit");
        // Wait for last language to be finished.
        _testSerializeDictionarySpeedTest(loader, "CacheTestPrimed");
    }

    protected void _testSerializeDictionarySpeedTest(GrammaticalLabelSetLoader loader, String loggerInfo) throws IOException {
        for (String languageStr : ImmutableList.of(LanguageConstants.JAPANESE, LanguageConstants.ENGLISH_US)) {
        	HumanLanguage language = LanguageProviderFactory.get().getLanguage(languageStr);
            GrammaticalLabelSet labelSet;
            try {
                labelSet = loader.getSet(language);
            } catch (Exception e) {
                continue;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000);
            long start = System.nanoTime();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(labelSet);
            }

            long writeTime = (System.nanoTime() - start)/1000 ;

            byte[] data = baos.toByteArray();
            start = System.nanoTime();
             try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                 ois.close();
             }
             logger.info(loggerInfo + ": " + language.getLocale() + " labels in " + writeTime + "/" + (System.nanoTime() - start)/1000 + " usec; size= " + data.length);
        }
   }
}
