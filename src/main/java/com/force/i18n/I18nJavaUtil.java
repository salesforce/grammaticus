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

package com.force.i18n;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import com.force.i18n.commons.text.TextUtil;

/**
 * A set of utilities used throughout the Grammaticus project.
 *
 * This should disappear eventually.
 * @author stamm
 */
public class I18nJavaUtil {
	// For production systems, this should be fine.
	// TODO: Use a jar properties file to initialize this.
	private static Level LOG_LEVEL = Level.FINE;
	private static File CACHE_BASE_DIR = new File(".");

	/**
	 * @return the log level to use for internal operations.  The intention is to allow
	 * different behaviors on production vs development; in development it will throw
	 * exceptions, on production it will produce log warnings but still function.
	 * If you set this to more severe than Level.CONFIG, it will have the production behavior.
	 */
	public static Level getLogLevel() {
		return LOG_LEVEL;
	}

	/**
	 * Set the log level to use for internal operations.
	 * @param level the level to set
	 */
	public static void setLogLevel(Level level) {
		LOG_LEVEL = level;
	}

	/**
	 * @return whether to throw exceptions for invalid labels instead of coping.
	 * Return true if the logLevel is &lt; Level.Config
	 */
	public static boolean isDebugging() {
		return LOG_LEVEL.intValue() < Level.CONFIG.intValue();
	}

    /**
     * Given a file (possibly a directory) return the last mod of the file or any recursive children files
     * @param includeSubDirs if false then only look in this directory itself, not recursive ones
     * @param dir the directory to test
     * @return the last modified file in the directory
     */
    public static long dirLastModified(File dir, boolean includeSubDirs) {
    	return dirLastModified(dir, includeSubDirs, false);
    }


    /**
     * Given a file (possibly a directory) return the last mod of the file or any recursive children files
     * @param includeSubDirs if false then only look in this directory itself, not recursive ones
     * @param dir the directory to test
     * @param checkDirectoryTimestampAsWell checks the directory's modification time itself.
     * @return the last modified file in the directory
     */
    public static long dirLastModified(File dir, boolean includeSubDirs, boolean checkDirectoryTimestampAsWell) {
        long lastModified = checkDirectoryTimestampAsWell ? dir.lastModified() : -1;
        File[] listOfFiles = dir.listFiles();
        if (listOfFiles != null) {  // listFiles is null if directory doesn't exist (i.e. not translated)
	        for (File child : listOfFiles) {
	            long childLastMod;
	            if (child.isDirectory()) {
	                if (!includeSubDirs)
	                    continue;
	                childLastMod = dirLastModified(child, includeSubDirs, checkDirectoryTimestampAsWell);
	            } else {
	                childLastMod = child.lastModified();
	            }
	            if (childLastMod > lastModified)
	                lastModified = childLastMod;
	        }
        }
        return lastModified;
    }

    /**
     * @param key the java-style property that is being loaded
     * @return the properties for this object
     */
    public static final String getProperty(String key) {
    	ResourceBundle defaultProps = getDefaultProperties();
    	try {
    		ResourceBundle overrideProps = getOverrideProperties();
    		if (overrideProps != null && overrideProps.containsKey(key)) {
    			return overrideProps.getString(key);
    		}
    	} catch (MissingResourceException ex) {
    		// IGNORE
    	}
    	return defaultProps.getString(key);
    }

    static final ResourceBundle getDefaultProperties() {
    	return ResourceBundle.getBundle("com.force.i18n.grammaticus");
    }

    static final ResourceBundle getOverrideProperties() throws MissingResourceException {
    	return ResourceBundle.getBundle("com.force.i18n.i18n");
    }

    /**
     * @return a jar protocol URL if the given URL is a bundleresource (i.e. osgi)
     */
    static URL osgiToJar(URL url) throws IOException {
        if ("bundleresource".equalsIgnoreCase(url.getProtocol())) {
            // Handle maveny things without referencing osgi directly.
            URLConnection connection = url.openConnection();
            try {
                return(URL) connection.getClass().getMethod("getLocalURL").invoke(connection);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return url;
    }

    public static File getFile(URL url) throws URISyntaxException, IOException {
        if ("jar".equalsIgnoreCase(url.getProtocol())) {
            return (new File(TextUtil.splitSimple("!", url.getFile()).get(0)).getCanonicalFile());
        }

        return new File(url.toURI()).getCanonicalFile();
    }


    public static long urlLastModified(URL url) throws URISyntaxException, IOException {
        url = osgiToJar(url);
        if ("jar".equalsIgnoreCase(url.getProtocol())) {
            JarURLConnection jarEntryConn = (JarURLConnection)url.openConnection();
            // Check that the jar file actually exists on the file system
            File file = new File(jarEntryConn.getJarFileURL().getPath());
            if (!file.exists()) throw new IOException("Unable to process JAR url. JAR file is missing: " + file);
            try (JarFile jarFile = new JarFile(file)) {
                // Automatically reject any JAR URL which does not contain a JAR entry
                String jarEntryName = jarEntryConn.getEntryName();
                if (jarEntryName == null) throw new IOException("Unsupported JAR url. Missing JAR entry: " + url);
                JarEntry jarEntry = jarFile.getJarEntry(jarEntryName);
                return jarEntry != null ? jarEntry.getTime() : -1;
            }
         }

        return new File(url.toURI()).lastModified();
    }

    public static long urlDirLastModified(URL url, boolean includeSubDirs, boolean checkDirectoryTimestampAsWell) throws URISyntaxException, IOException {
        if ("jar".equalsIgnoreCase(url.getProtocol())) {
            long result = -1;
            JarURLConnection jarEntryConn = (JarURLConnection)url.openConnection();
            // Check that the jar file actually exists on the file system
            File file = new File(jarEntryConn.getJarFileURL().getPath());
            if (!file.exists()) throw new IOException("Unable to process JAR url. JAR file is missing: " + file);
            // Automatically reject any JAR URL which does not contain a JAR entry
            String jarEntryName = jarEntryConn.getEntryName();
            try (JarFile jarFile = new JarFile(file)) {
                // See if the directory is there
                for (Enumeration<JarEntry> e =jarFile.entries(); e.hasMoreElements(); ) {
                    JarEntry entry = e.nextElement();
                    if (entry.getName().startsWith(jarEntryName)) {
                        if (!checkDirectoryTimestampAsWell && entry.getName().equals(jarEntryName)) continue;
                        // TODO: Ignore subdirs
                        result = Math.max(result, entry.getTime());
                    }
                }
            }
            return result;
        }
        assert "file".equalsIgnoreCase(url.getProtocol()) : "Invalid URL protocol: " + url;
        return dirLastModified(new File(url.toURI()), includeSubDirs, checkDirectoryTimestampAsWell);
    }

    /**
     * @return the base directory where to put the cache (using the value of cacheDir as a relative
     * directory to this).  Defaults to current directory.  You can also override cacheDir to be
     * absolute, and then this is irrelevant.
     */
    public static File getCacheBaseDir() {
        return CACHE_BASE_DIR;
    }

    /**
     * Set the base directory of where the cache may be stored.  This
     * way, if you restart the app, it won't need to recalculate the
     * cache
     * @param cacheDir the cache directory where the label cache will be stored
     */
    public static void setCacheBaseDir(File cacheDir) {
        CACHE_BASE_DIR = cacheDir;
    }
}
