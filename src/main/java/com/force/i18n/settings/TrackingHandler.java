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

package com.force.i18n.settings;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author bfry
 */
public abstract class TrackingHandler extends DefaultHandler {

    private URL fileName = null;
    private Locator locator; // used to locate xml errors

    private static final int DIRECTORY_DEPTH_FOR_DTD = 5;

    public TrackingHandler(URL file) {
        fileName = file;
    }

    public String getLineNumberString() {
        StringBuilder msg = new StringBuilder(this.fileName.getPath());
        if (this.locator != null) {
            msg.append("(").append(this.locator.getLineNumber()).append(")");
        }
        return msg.toString();
    }

    /**
     * Receives the locator for SAX parsing events
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    protected Locator getLocator() {
        return this.locator;
    }

    protected URL getFile() {
        return this.fileName;
    }

    public static boolean exists(URL url) {
        switch (url.getProtocol()) {
        case "file":
            try {
                File actualFile = new File(url.toURI());
                return actualFile.exists();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        case "jar":
            try {
                URLConnection conn = url.openConnection();
                conn.getInputStream().close();
                return true;
            } catch (IOException | StringIndexOutOfBoundsException ex) {
                return false;
            }
        default:
        }
        return false;
    }

    /**
     * Try and find the DTD by going up the directory chain a few times
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId != null) {
            try {
            	String dtdName = new File(systemId).getName();
                URL file = getFile() != null ? new URL(getFile(), dtdName)
                	: new File("config", new File(systemId).getName()).toURI().toURL();
                if (dtdName.endsWith(".dtd")) {  // The labels.dtd isn't copied to all directories (like the names.dtd), so go up the tree to find it
                	for (int i = DIRECTORY_DEPTH_FOR_DTD; i > 0 && !exists(file); i--) {
	                	// Import can occasionally get confused
	                	file = new URL(file, "../" + dtdName);
                	}
                }

                // Check to see if it's in the resource bundle (so you don't have to copy labels.dtd everywhere.
                if ("labels.dtd".equals(dtdName) && !exists(file)) {
                    file = TrackingHandler.class.getResource("/com/force/i18n/labels.dtd");
                }

                if (exists(file)) {
                    InputSource is = new InputSource(file.openStream());
                    is.setSystemId(file.getPath());
                    return is;
                }


            } catch (IOException e) {
                // probably a problem with the file, just switch over to standard processing.
                return null;
            }
        }

        return null;
    }
}
