/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.parser;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageConstants;
import com.force.i18n.LanguageProviderFactory;
import com.google.common.collect.ImmutableList;

public class GrammaticusLabelPerformanceTest extends BaseGrammaticalLabelTest {

    private static final Logger logger = Logger.getLogger(GrammaticusLabelPerformanceTest.class.getName());
    public GrammaticusLabelPerformanceTest(String name) {
        super(name);
    }

    /**
     * Test all uniquefied tag and verify it has minimum collision rate.
     * 
     * @throws Exception
     */
    public void testHashCollisionRateForUniquefiedTag() throws Exception {
        // build some data
        GrammaticalLabelSetLoader loader = getLoader();
        for (String languageStr : ImmutableList.of(LanguageConstants.FRENCH, LanguageConstants.GERMAN,
                LanguageConstants.KOREAN, LanguageConstants.SPANISH, LanguageConstants.JAPANESE,
                LanguageConstants.ENGLISH_US, LanguageConstants.ENGLISH_CA)) {
            HumanLanguage language = LanguageProviderFactory.get().getLanguage(languageStr);
            try {
                loader.getSet(language);
            } catch (Exception e) {
                continue;
            }
        }
        double rateForAdjective = runCollisionCheckOnHash(getHashMapFromUniquefy(AdjectiveRefTag.class));
        logger.info("AdjectiveRefTag hash uniqueness: " + rateForAdjective);
        double rateForArticle = runCollisionCheckOnHash(getHashMapFromUniquefy(ArticleRefTag.class));
        logger.info("ArticleRefTag hash uniqueness: " + rateForArticle);
        double rateForNoun = runCollisionCheckOnHash(getHashMapFromUniquefy(NounRefTag.class));
        logger.info("NounRefTag hash uniqueness: " + rateForArticle);

        assertTrue("AdjectiveRefTag should have 40% hash code uniqueness at least.", rateForAdjective > 0.4);
        assertTrue("ArticleRefTag should have 40% hash code uniqueness at least.", rateForArticle > 0.4);
        assertTrue("NounRefTag should have 40% hash code uniqueness at least.", rateForNoun > 0.4);
    }

    /**
     * Get private Map instance by reflection
     */
    private Map<?,?> getHashMapFromUniquefy(Class<?> c) throws Exception {
        Field f = c.getDeclaredField("tagMap");
        f.setAccessible(true);
        ConcurrentUniquefy<?> cu = (ConcurrentUniquefy<?>) f.get(null);
        Field f2 = ConcurrentUniquefy.class.getDeclaredField("pool");
        f2.setAccessible(true);
        return (Map<?,?>) f2.get(cu);
    }

    /**
     * Calcurate hash code collision rate.
     * 
     * @param map
     * @return
     */
    private double runCollisionCheckOnHash(Map<?,?> map) {
        double m = map.size();
        Set<Integer> s = new HashSet<Integer>(map.size() * 3);
        for (Object o : map.keySet()) {
            if (o != null)
                s.add(o.hashCode() ^ o.hashCode() >>> 16); // see HashMap#hash()
        }
        return s.size() / m;
    }
}