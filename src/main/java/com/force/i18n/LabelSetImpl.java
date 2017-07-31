/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.force.i18n.settings.BasePropertyFile;
import com.force.i18n.settings.ParameterNotFoundException;
import com.force.i18n.settings.PropertyFileData;

/**
 * Set of label definitions that do not require grammatical correctness.  This can be used
 * for various localization files.
 * <p>
 * Labels are stored in a one or more XML files as defined by a LabelSetDescriptor.
 * <p>
 * A LabelSet is created/loaded by the {@link com.force.i18n.grammar.parser.GrammaticalLabelSetLoader}
 */
public class LabelSetImpl extends BasePropertyFile implements LabelSet {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(LabelSetImpl.class.getName());

    private Map<String, String> labelSectionToFilename = null;

    /**
     * Construct a label set from the given data and optional labelSectionToFileName
     * @param data the data
     * @param labelSectionToFilename the map from label section to filename (not super useful)
     */
    protected LabelSetImpl(PropertyFileData data, Map<String, String> labelSectionToFilename) {
        super(data);
        this.labelSectionToFilename = labelSectionToFilename;
    }

    public LabelSetImpl(BasePropertyFile.Parser p) throws IOException {
        super(p);
    }

    public LabelSetImpl(BasePropertyFile.Parser p, PropertyFileData data) throws IOException {
        super(p, data);
    }

    @Override
    public Map<String, String> getLabelSectionToFilename() {
        if (this.labelSectionToFilename == null) {
            return null;
        }
        return Collections.unmodifiableMap(this.labelSectionToFilename);
    }

    private String formatString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    @Override
    public boolean labelExists(String section, String param) {
        Object result = inner_get(section, param, false);
        if (result != null) {
            if (result instanceof LabelRef) {
                // Ensure that aliases can also be correctly resolved
                LabelRef ref = (LabelRef)result;
                return labelExists(ref.getSection(), ref.getKey());
            }
            return true;
        }
        return false;
    }

    @Override
    public String getString(String section, String param) {
        return formatString(this.get(section, param));
    }

    @Override
    public String getStringThrow(String section, String param) {
        return formatString(super.get(section, param, true));
    }

    @Override
    public String getString(String section, String param, String ifNull) {
        return formatString(this.get(section, param, ifNull));
    }

    public Object get(LabelReference reference) {
        return this.get(reference.getSection(), reference.getKey());
    }

    // raising visibility
    @Override
    public PropertyFileData getPropertyFileData() {
        return super.getPropertyFileData();
    }

    public String getFilenameFromLabelSection(String sectionName) {
        assert (null != getLabelSectionToFilename());
        return getLabelSectionToFilename().get(sectionName);
    }

    protected void setLabelSectionToFilename(Map<String, String> sectionMap) {
        this.labelSectionToFilename = sectionMap;
    }

    /**
     * Will either throw an exception or send an error depending on whether we
     * are in production / test running more or note.
     *
     * TODO : LabelUtils.java also has a processMissingLabel()
     * method used by other classes. We should have this method just depend
     * on Config.isProduction() and merge the two methods together
     *
     * @param message the message to return as the label
     * @param allowLabelException - true to always throw an exception for a missing label
     * @throws ParameterNotFoundException if label exception is allowed
     */
    public String processMissingLabel(String message, boolean allowLabelException) throws ParameterNotFoundException {
        if (!isProductionMode() || allowLabelException) {
            throw new ParameterNotFoundException(message);
        } else {
            logger.log(Level.WARNING, MISSING_LABEL + message,
                    new ParameterNotFoundException(message));
            return MISSING_LABEL + message;
        }
    }
}
