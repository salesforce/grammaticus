/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.I18nJavaUtil;
import com.force.i18n.LabelDebug;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.GrammaticalLabelSetImpl;
import com.force.i18n.grammar.GrammaticalLabelSetProvider;
import com.force.i18n.settings.BasePropertyFile;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * An implementation of the grammatical label set loader that will cache the
 * results to a local file
 * @author nveeser,stamm
 */
public class GrammaticalLabelSetFileCacheLoader extends GrammaticalLabelSetLoader {
    private static final Logger logger = Logger.getLogger(GrammaticalLabelSetFileCacheLoader.class.getName());

    private final File cacheDir;

    private File calculateCacheDir(String setName) {
    	File result = new File(I18nJavaUtil.getCacheBaseDir(), I18nJavaUtil.getProperty("cacheDir") + "/" + setName);
    	try {
    	    result = result.getCanonicalFile();
    	    result.mkdirs();
    	} catch (IOException ex) {
            logger.log(Level.FINE, "Trouble with the cache dir", ex);
    	}
    	return result;
    }

    /**
     * Construct a FileCacheLoader that will cache the label set values
     * in the
     * @param desc the descriptor for the location of the label set
     * @param parent the optional parent provider for fallback labels
     */
    public GrammaticalLabelSetFileCacheLoader(GrammaticalLabelSetDescriptor desc, GrammaticalLabelSetProvider parent) {
        super(desc, parent);
        cacheDir = calculateCacheDir(desc.getLabelSetName());
    }

    // Used for testing
    File getCacheDir() {
        return cacheDir;
    }

    @Override
    public GrammaticalLabelSetImpl compute(GrammaticalLabelSetDescriptor desc) throws IOException {
        final FileCache cache = new FileCache(desc.getLanguage(), desc.getLabelSetName());

        GrammaticalLabelSetImpl labelSet = null;
        if (cache.exists()) {
            if (cache.expired()) {
                cache.delete();
            } else {
                labelSet = cache.read();
                if (labelSet != null) {
                    // Ensure if label hints are allowed and the cached labelset
                    // is missing the mapping of label sections to
                    // file names, recreate it
                    if (LabelDebug.isLabelHintAllowed() && (null == labelSet.getLabelSectionToFilename())) {
                        labelSet = null;
                        cache.delete();
                    } else {
                        ((BasePropertyFile)labelSet).attachSharedKeyMap(getSeedKeyMap());
                    }
                }
            }
        }

        if (labelSet == null) {
            long start = System.currentTimeMillis();

            labelSet = super.compute(desc);

            logger.info("Created LabelSet." + desc.getLanguage() + " in " + (System.currentTimeMillis() - start) + " ms");

            // Save as a cache file
            final GrammaticalLabelSetImpl writeMe = labelSet;
            // Wait to serialize english to prevent any funny business because we reload English very quickly afterwards.
            if (desc.getLanguage() == LanguageProviderFactory.get().getBaseLanguage()) {
                cache.write(labelSet);
            } else {
                // Do other languages in the background.
                ThreadFactory tf = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("LabelCache-" + desc.getLanguage() + "-%s").build();
                ExecutorService cacheWriter = Executors.newSingleThreadExecutor(tf);
                try {
                    cacheWriter.execute(new Runnable() {
                        @Override
                        public void run() {
                            cache.write(writeMe);
                        }
                    });
                } finally {
                    cacheWriter.shutdown();
                }
            }
        }
        return labelSet;
    }

    // TODO: This is copied from udd loader
    public static URL[] getAllUddXmlFiles() throws IOException {
        Enumeration<URL> udds = GrammaticalLabelSetFileCacheLoader.class.getClassLoader().getResources("udd/udd.xml");
        List<URL> uddUrls = Collections.list(udds);
        assert uddUrls.size() > 0 : "no udd.xml files found in classpath; classpath is probably misconfigured";
        return uddUrls.toArray(new URL[uddUrls.size()]);
    }

    /**
     * @return a set of URLs that should be used to check for
     * Note, this will look at all the files in the directory of the URL, if appropriate
     */
    protected Collection<URL> getFilesForModifiedDate() {
        return Collections.emptySet();
    }

    /**
     * Returns the most recent last modified date for the given language and other metadata files such as the udd.
     *
     * TODO the definition of expired may be wrong for any language other than English
     */
    public long getLastModifiedDate(Collection<HumanLanguage> languages) throws URISyntaxException, IOException {
    	long result = -1;

        // Allow
        for (URL overrides : getFilesForModifiedDate()) {
            File uddFile;
            try {
                uddFile = I18nJavaUtil.getFile(overrides);
                File uddDir = uddFile.getParentFile();
                result = Math.max(result, I18nJavaUtil.dirLastModified(uddDir, true));
            } catch (URISyntaxException | IOException e) {
                throw Throwables.propagate(e);
            }
        }


        Set<URL> rootDirSet = new HashSet<>(languages.size() * 3/2);
        for (HumanLanguage language : languages) {
            GrammaticalLabelSetDescriptor desc = language == this.getBaseDesc().getLanguage() ? this.getBaseDesc() : this.getBaseDesc().getForOtherLanguage(language);
            rootDirSet.add(new URL(desc.getRootDir(), language.getDefaultLabelDirectoryPath()));
        }

        for (URL rootDir : rootDirSet) {
            result = Math.max(result, I18nJavaUtil.urlDirLastModified(rootDir, false, true));
        }
        return result;
    }

    /**
     * Used only for debugging purposes.
     */
    @Override
    public void resetMap() {
    	// Invalidate in-memory cache.
    	super.resetMap();
    	
    	// Remove file cache.
    	// Dictionary is inherited and stored in file cache generated by child loader.
    	// When label change is made only in parent label files, file cache for child 
    	// label set won't be re-generated since the last modified date is current. 
    	if(cacheDir.isDirectory()) {
    		try {
    			Files.list(cacheDir.toPath())
	                 .filter(Files::isRegularFile)
                     .map(Path::toFile)
                     .forEach(File::delete);
    		} catch (IOException e) {
    			logger.log(Level.WARNING, "Could not delete cache file", e);
    		}
    	}
    }

    private class FileCache {

        private final HumanLanguage language;
        private final File cacheFile;
        private final String labelSetName;

        public FileCache(HumanLanguage language, String labelSetName) {
            this.language = language;
            this.cacheFile = new File(cacheDir, language + ".cache");
            this.labelSetName = "LabelSet." + labelSetName + "." + language;

            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
        }

        public boolean exists() {
            return this.cacheFile.exists();
        }

        public boolean expired() {
            try {
                return this.cacheFile.lastModified() < getLastModifiedDate(Collections.singleton(this.language));
            } catch (URISyntaxException | IOException e) {
                logger.log(Level.FINER, "Could not get last modified date form source", e);
                return false; // Oh well.
            }
        }

        /**
         * @return null if we were unable to load the cached version
         */
        public GrammaticalLabelSetImpl read() {
            // if we're tracking duplicated labels, we need to load the file directly in order to look at all the labels, so don't load from the cache
            if (this.language == LanguageProviderFactory.get().getBaseLanguage() && GrammaticalLabelFileParser.isDupeLabelTrackingEnabled()) {
                return null;
            }

            logger.info("Loading " + labelSetName + " from cache");
            long start = System.currentTimeMillis();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.cacheFile))) {
                GrammaticalLabelSetImpl labelSet = (GrammaticalLabelSetImpl)ois.readObject();
                logger.info("Loaded " + this.labelSetName + " from cache in " + (System.currentTimeMillis() - start)
                    + " ms");
                return labelSet;
            }
            catch (Exception e) {
                logger.log(Level.INFO, "Could not load " + labelSetName + " from cache: ", e);
                delete();
            }
            return null;
        }

        public void delete() {
            try {
                this.cacheFile.delete();
            }
            catch (Exception e) {
                logger.log(Level.INFO, "Could not delete old cache for " + this.labelSetName + ": ", e);
            }
        }

        public void write(GrammaticalLabelSetImpl labelSet) {
            long startAt = System.currentTimeMillis();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.cacheFile))){
                oos.writeObject(labelSet);
                logger.info("Wrote cache for " + this.labelSetName + " in " + (System.currentTimeMillis() - startAt)
                    + " ms");
            }
            catch (Exception e) {
                logger.log(Level.INFO, "Could not write cache for " + this.labelSetName + ": ", e);
            }
        }
    }

}
