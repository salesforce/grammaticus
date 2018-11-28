/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of property file data (e.g. label data) that
 * shares the section name keys and parameter name keys across
 * various languages.
 * <p>
 * Some care needs to be taken with the thread safetyness of this class,
 * as the internal SharedKeyMap can be mutated by multiple threads.
 *
 * @author koliver
 */
public class SharedKeyMapPropertyFileData implements PropertyFileData, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(SharedKeyMapPropertyFileData.class.getName());

    private static final class SerializableLock implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
    }

    private final Locale locale;

    private final boolean isPrimary;

    /**
     * Note that the section-level maps are thread-safe.
     */
    private SharedKeyMap<String, SharedKeyMap<String, Object>> seed;
    private final SerializableLock seedLock = new SerializableLock();

    private SharedKeyMap<String, SharedKeyMap<String, Object>> data;

    private final Set<String> publicSections;

    /**
     * @param locale    The locale id should correspond
     *                  to a value that came from the g11n.xml file
     * @param isPrimary this is the only label set that can define new sections.
     * @param seed      this is the shared key map that is shared across multiple locale's versions
     *                  of the data.
     */
    public SharedKeyMapPropertyFileData(Locale locale, boolean isPrimary, SharedKeyMap<String, SharedKeyMap<String, Object>> seed, Set<String> publicSections) {
        this.locale = locale;
        this.isPrimary = isPrimary;
        this.seed = seed;
        this.data = new SharedKeyMap<String, SharedKeyMap<String, Object>>(seed);
        this.publicSections = publicSections;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public Map<String, Object> getSection(String sectionName) {
        if (!this.data.containsKey(sectionName)) {
            return null;
        }

        return Collections.unmodifiableMap(this.data.get(sectionName));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Entry<String, Map<String, Object>>> entrySet() {
        // This wants to return Entry<String,SharedKeyMap<String,Object>> which is hard to get java to accept here
        Set entrySet = this.data.entrySet();
        return Collections.unmodifiableSet((Set<Entry<String, Map<String, Object>>>) entrySet);
    }

    @Override
    public Set<String> getSectionNames() {
        return Collections.unmodifiableSet(this.data.keySet());
    }

    @Override
    public boolean containsSection(String sectionName) {
        return this.data.containsKey(sectionName);
    }

    @Override
    public boolean contains(String sectionName, String paramName) {
        if (sectionName == null || paramName == null) {
            return false;
        }
        Map<String, Object> section = getSection(sectionName);
        if (section == null) {
            return false;
        }
        return section.containsKey(paramName);
    }

    @Override
    public Set<String> getPublicSectionNames() {
        return Collections.unmodifiableSet(this.publicSections);
    }

    @Override
    public void setSectionAsPublic(String section) {
        if (!section.toLowerCase().equals(section)) {
            throw new RuntimeException("Public sections must have a lowercase name");
        }
        this.publicSections.add(section);
    }

    @Override
    public Object put(String sectionName, String paramName, Object value) {
        SharedKeyMap<String, Object> section = this.data.get(sectionName);

        if (section == null) {
            // populate seed, lazily as needed
            SharedKeyMap<String, Object> seedSection;
            synchronized (this.seedLock) {
                seedSection = this.seed.get(sectionName);
                if (seedSection == null) {
                    if (!this.isPrimary) {
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("Only primary locale can create sections " + sectionName + "." + paramName);
                        }
                        return null;
                    }

                    // we need these to be thread safe as multiple threads could accessing
                    //
                    // ideally, we would just initialize all of these Maps once
                    // with the default/english language. Then our structures would be read-only
                    // from then on and we'd be okay sharing the read-only data structure from then on
                    // without the need for synchronization.
                    Map<String, Integer> sharedParamToValue = new ConcurrentHashMap<String, Integer>(1, 0.75f, 1);

                    seedSection = new SharedKeyMap<String, Object>(sharedParamToValue, -1);
                    this.seed.put(sectionName, seedSection);
                }
            }

            section = new SharedKeyMap<String, Object>(seedSection);
            this.data.put(sectionName, section);
        }

        return section.put(paramName, value);
    }

    public void compact() {
        synchronized (this.seedLock) {
            this.seed.trimToSize();
        }
        this.data.trimToSize();

        // we need to sync on seed here because iterating over this.data.values
        // ends up iterating over seed's keyset, which could have values added to it
        // in put, but in put we synchronize on seed to handle that.
        synchronized (this.seedLock) {
            for (SharedKeyMap<String, Object> section : this.data.values()) {
                section.trimToSize();
            }
        }
    }

    @Override
    public void removeSection(String sectionName) {
        this.data.remove(sectionName);
    }

    @Override
    public Object remove(String sectionName, String paramName) {
        SharedKeyMap<String, Object> section = this.data.get(sectionName);
        if (section == null) {
            return null;
        }
        return section.remove(paramName);
    }

    @Override
    public void shareKeys(SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap) {
        synchronized (this.seedLock) {
            // nothing for us to do if we're already sharing the same seeds
            if (this.seed == seedKeyMap) {
                return;
            }

            this.seed = seedKeyMap;
        }
        SharedKeyMap<String, SharedKeyMap<String, Object>> unshared = this.data;
        this.data = new SharedKeyMap<String, SharedKeyMap<String, Object>>(seedKeyMap);

        for (Map.Entry<String, SharedKeyMap<String, Object>> unsharedSection : unshared.entrySet()) {
            String sectionName = unsharedSection.getKey();
            for (Map.Entry<String, Object> entry : unsharedSection.getValue().entrySet()) {
                put(sectionName, entry.getKey(), entry.getValue());
            }
        }
    }

}
