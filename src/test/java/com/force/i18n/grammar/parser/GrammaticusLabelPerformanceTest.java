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