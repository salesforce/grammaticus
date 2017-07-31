/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.xml.sax.Locator;

import com.force.i18n.I18nJavaUtil;
import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.commons.util.settings.BaseNonConfigIniFile;
import com.force.i18n.commons.util.settings.SettingsUtil;

/**
 * This class parses XML configuration files and puts the data into a data
 * structure that can be used by the app to retrieve info.
 * <p>
 * Comments can be entered using standard XML comment syntax.
 * <h3> References </h3>
 * &lt;Value&gt;s may contain substitutions of other
 * &lt;ParamName&gt;,&lt;Value&gt; pairs. Substitutions are indicated by strings
 * of the form: When the parser sees such strings, they are replaced with the
 * selected &lt;SectionName&gt;.&lt;ParamName&gt; pair. References are in the
 * form <code> ${&lt;section&gt;.&lt;param&gt;} </code>
 * <p>
 * Forward and multi-level references are allowed, but circular are not.
 * <p>
 * Values may also contain references to system parameters.
 * <p>
 * <h3> Example </h3>
 * <pre><code>
 *                                     &lt;section name=&quot;sectionName&quot;&gt;
 *
 *                                         &lt;!--  Standard Parameter --&gt;
 *                                         &lt;param name=&quot;paramName1&quot;&gt; paramValue &lt;/param&gt;
 *
 *                                         &lt;!--  Parameter Reference --&gt;
 *                                         &lt;param name=&quot;paramName2&quot;&gt; ${sectionName.paramName1} &lt;/param&gt;
 *
 *                                         &lt;!--  Property Reference --&gt;
 *                                         &lt;param name=&quot;paramName3&quot;&gt; #{user.property} &lt;/param&gt;
 *
 *                                     &lt;/section&gt;
 * </code></pre>
 *
 * @author nveeser, btsai
 */
public class BasePropertyFile implements BaseNonConfigIniFile, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String MISSING_LABEL = I18nJavaUtil.getProperty("missingLabelPrefix");

    // Objects where we store the data and metadata
    protected PropertyFileData data;
    protected final Map<String, Map<String, MetaDataInfo>> metaData;

    // Who uses this?
    protected long lastModified = -1;

    public BasePropertyFile(int initialCapacity) {
        this(initialCapacity, new MapPropertyFileData());
    }

    public BasePropertyFile() {
        this(new MapPropertyFileData());
    }

    protected BasePropertyFile(PropertyFileData data) {
        this(16, data);
    }

    protected BasePropertyFile(int initialCapacity, PropertyFileData data) {
        this.data = data;
        this.metaData = new HashMap<String, Map<String, MetaDataInfo>>(initialCapacity);
    }

    public BasePropertyFile(Parser p) throws IOException {
        this();
        p.load(this.data, this.metaData);
    }

    protected BasePropertyFile(Parser p, PropertyFileData data) throws IOException {
        this(data);
        p.load(this.data, this.metaData);
    }

    @Override
    public Set<Entry<String, Map<String, Object>>> entrySet() {
        return this.data.entrySet();
    }

    public Set<String> sectionNames() {
        return this.data.getSectionNames();
    }

    public void remove(String sectionName, String paramName) {
        this.data.remove(sectionName, paramName);
    }

    public void removeSection(String sectionName) {
        this.data.removeSection(sectionName);
    }

    public boolean containsSection(String section) {
        return this.data.containsSection(section);
    }

    public boolean containsParam(String section, String param) {
        return this.data.contains(section, param);
    }

    /**
     * Whether the section contains an enum list with the baseParam.
     */
    public boolean containsListParam(String section, String baseParam) {
        // check if the list param exists by attempting to get the first one
        return inner_get(section, makeListEntryParam(baseParam, 0), false) != null;
    }

    /**
     * Gets all values of parameters that were in the section for a build config
     * file
     */
    public List<String> getListConfigValues(String section) throws SettingsSectionNotFoundException {
        return getList(section, section);
    }

    /**
     * Gets all values of parameters that begin with baseParam_xx, where xx is a
     * number starting from 0.
     */
    public List<String> getList(String section, String baseParam) throws SettingsSectionNotFoundException {
        // try to get the first one as a way to find whether the section exists
        inner_get(section, makeListEntryParam(baseParam, 0), true);
        return getParamList(section, baseParam);
    }

    /**
     * Gets all values of parameters that begin with baseParam_xx, where xx is a
     * number starting from 0.
     */
    @Override
    public List<String> getList(String section, String baseParam, List<String> ifNull) {
        // try to get the first one as a way to find whether the section exists
        try {
            if (inner_get(section, makeListEntryParam(baseParam, 0), false) == null) {
                return ifNull;
            }
        } catch (SettingsSectionNotFoundException x) {
            // won't happen
            throw new RuntimeException(x);
        }

        List<String> result = getParamList(section, baseParam);
        if (result.isEmpty()) {
            return ifNull;
        }
        return result;
    }

    /**
     * Gets a List of all values whose parameters start with
     * <code>baseParam_x</code>, where x is number starting from 0.
     */
    private List<String> getParamList(String section, String baseParam) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; true; ++index) {
            String result = getString(section, makeListEntryParam(baseParam, index), null);
            if (result == null) {
                break;
            }
            list.add(result);
        }
        return list;
    }

    /**
     * Get the number of enumItems within an enum list param.
     */
    public int getNumListEntryParams(String section, String baseParam) {
        for (int i = 0; true; i++) {
            String result = getString(section, makeListEntryParam(baseParam, i), null);
            if (result == null) {
                return i;
            }
        }
    }

    public static String makeListEntryParam(String baseParam, int index) {
        StringBuilder param = new StringBuilder(baseParam.length() + 5);
        param.append(baseParam);
        param.append("_");
        param.append(index);
        return param.toString();
    }

    /**
     * @return a set of all params in the specified section. You'd best be not
     *         messing with it.
     */
    public Set<String> getParams(String section) throws SettingsSectionNotFoundException {
        Set<String> result = getParams(section, null);
        if (result == null) {
            throw new SettingsSectionNotFoundException("PropertyFile - section " + section + " not found.");
        }
        return result;
    }

    protected Object inner_get(String section, String param, boolean throwSettingsSectionNotFoundException) throws SettingsSectionNotFoundException {
        Map<String, Object> theSect = this.data.getSection(section);
        if (theSect == null) {
            if (throwSettingsSectionNotFoundException) {
                throw new SettingsSectionNotFoundException("PropertyFile - section " + section + " not found.");
            }
            return null;
        }
        if (param == null) {
            throw new NullPointerException();
        }
        return theSect.get(param);
    }

    /**
     * @return a set of all params in the specified section. You'd best be not
     *         messing with it.
     */
    public Set<String> getParams(String section, Set<String> ifNull) {
        Map<String, Object> theSect = this.data.getSection(section);
        if (theSect == null) {
            return ifNull;
        }
        return theSect.keySet();
    }

    @Override
    public Object get(String section, String param, Object ifNull) {
        Object result;
        try {
            result = inner_get(section, param, false);
        } catch (SettingsSectionNotFoundException x) {
            // won't ever happen
            throw new RuntimeException(x);
        }
        if (result == null) {
            return ifNull;
        }
        return result;
    }

    public Object get(String section, String param, boolean allowLabelException) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        Object result = inner_get(section, param, true);
        if (result == null) {
            return processMissingLabel("PropertyFile - val " + param + " not found in section " + section, allowLabelException);
        }
        return result;
    }

    /**
     * Get the value for a given <b>SectName</b>.<b>ParamName</b> pair.
     *
     * @param section
     *            the section name of the desired variable
     * @param param
     *            the parameter name with the the given section
     * @return the value of the parameter
     */
    public Object get(String section, String param) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        return get(section, param, false);
    }

    /**
     * Will either throw an exception or send an error depending on whether we
     * are in production / test running more or note.
     *
     * @param message
     * @return
     * @throws ParameterNotFoundException
     */
    private String processMissingLabel(String message) throws ParameterNotFoundException {
        return processMissingLabel(message, false);
    }

    /**
     * Will either throw an exception or send an error depending on whether we
     * are in production / test running more or note.
     *
     * @param message
     * @return
     * @throws ParameterNotFoundException
     */
    private String processMissingLabel(String message, boolean allowLabelException) throws ParameterNotFoundException {
        if (!isProductionMode() || allowLabelException) {
            throw new ParameterNotFoundException(message);
        } else {
            return MISSING_LABEL + message;
        }
    }

    public String getString(String section, String param) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        return (String) this.get(section, param);
    }

    public String getStringThrow(String section, String param) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        return (String) this.get(section, param, true);
    }

    /**
     * Censors the value before returning it to the caller.  However, if the value is null, then we will not
     * run the censoring logic and just return the passed in ifNull value directly.
     *
     * Note: Do _NOT_ rely the censored value we return always being the same.  The exact censored output
     *       may change over time.
     *
     * @param section the section for the label
     * @param param the key for the label
     * @param ifNull default value to return if null (not set).
     * @return some number of 'x'ses if the value should be censored.
     */
    @Override
    public String getCensoredString(String section, String param, String ifNull) {
        String value = this.getString(section, param, null);
        if(value == null) {
            return ifNull;
        }

        return SettingsUtil.censorValue(section, param, value);
    }

    @Override
    public String getString(String section, String param, String ifNull) {
        return (String) this.get(section, param, ifNull);
    }

    public int getInt(String section, String param) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        return Integer.parseInt(getString(section, param));
    }

    @Override
    public int getInt(String section, String param, int ifNull) {
        try {
            return Integer.parseInt(getString(section, param, null));
        } catch (Exception x) {
            return ifNull;
        }
    }

    public float getFloat(String section, String param) throws SettingsSectionNotFoundException, ParameterNotFoundException {
        return Float.parseFloat(getString(section, param));
    }

    @Override
    public float getFloat(String section, String param, float ifNull) {
        try {
            return Float.parseFloat(getString(section, param, null));
        } catch (Exception x) {
            return ifNull;
        }
    }

    public boolean getBoolean(String section, String param) throws ParameterNotFoundException, SettingsSectionNotFoundException {
        return stringToBoolean(getString(section, param));
    }

    @Override
    public boolean getBoolean(String section, String param, boolean ifNull) {
        if (ifNull == true) {
            return stringToBoolean(getString(section, param, "true"));
        }
        return stringToBoolean(getString(section, param, "false"));
    }

    public Object put(String section, String param, Object value) {
        return this.data.put(section, param, value);
    }

    /**
     * Returns an unmodifiable <code>Map</code> of all the values for a
     * particular sectionName. If, that section does not exist, will return
     * <code>null</code>.
     */
    public Map<String, Object> getSection(String sectionName) {
        return this.data.getSection(sectionName);
    }

    /**
     * Output XML data to a stream output is for set.dtd. This will not give an
     * exact copy of what was read in.
     */
    public void outputXML(OutputStream os) throws IOException {
        outputXML(os, false);
    }

    /**
     * Outputs XML data to a stream output, either censored or uncensored version depending on
     * doCensor's value.
     *
     * Note: outputValueXML vs outputValueXMLCensored will go away in 152 when configuration
     *       is split out from labels / motifs.
     *
     * @param os the output stream to write to
     * @param doCensor will censor values like password if set to true
     */
    public void outputXML(OutputStream os, boolean doCensor) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(os, "utf-8");

        osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        osw.write("<iniFile>\n");

        for (String sectionName : new TreeSet<String>(this.data.getSectionNames())) {
            osw.write("\t<section name=\"" + sectionName + "\">\n");

            Map<String, Object> sectionMap = this.data.getSection(sectionName);

            for (String paramName : new TreeSet<String>(sectionMap.keySet())) {
                osw.write("\t\t<param name=\"" + paramName + "\">");
                if(doCensor) {
                    outputValueXMLCensored(osw, sectionName, paramName, sectionMap.get(paramName));
                } else {
                    outputValueXML(sectionMap.get(paramName), osw);
                }
                osw.write("</param>\n");
            }
            osw.write("\t</section>\n");
        }
        osw.write("</iniFile>\n");
        osw.flush();
    }

    protected void outputValueXML(Object value, OutputStreamWriter os) throws IOException {
        if (value != null) {
            if (value instanceof String) {
                os.write(TextUtil.escapeToXml((String)value, true, true));
            } else {
                os.write(value.getClass() + ":" + TextUtil.escapeToXml(value.toString(), true, true));
            }
        }
    }

    protected void outputValueXMLCensored(OutputStreamWriter os, String sectionName, String paramName, Object value) throws IOException {
        if (value != null) {
            if (value instanceof String) {
                os.write(TextUtil.escapeToXml(SettingsUtil.censorValue(sectionName, paramName, (String)value), true, true));
            } else {
                os.write(value.getClass() + ":" + TextUtil.escapeToXml(SettingsUtil.censorValue(sectionName, paramName, value.toString()), true, true));
            }
        }
    }

    public final long getLastModified() {
        return lastModified;
    }

    public final void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void doSubstitutions() throws ParameterNotFoundException, IOException {
        doSubstitutions(null);
    }
    public void doSubstitutions(Object referenceConfig) throws ParameterNotFoundException, IOException {
        // we need to queue up our modifications, because some implementations
        // don't support
        // swapping values on their Iterators (e.g. SharedKeyMap)
        Map<String, Map<String, Object>> modifications = new HashMap<String, Map<String, Object>>(128);

        for (Map.Entry<String, Map<String, Object>> sectionEntry : this.data.entrySet()) {
            String sectionKey = sectionEntry.getKey();

            for (Map.Entry<String, Object> parameterEntry : sectionEntry.getValue().entrySet()) {
                String parameterName = parameterEntry.getKey();
                Object originalValue = parameterEntry.getValue();

                Object newValue;
                try {
                    newValue = substitute(sectionKey, parameterName, originalValue, referenceConfig);
                } catch (SubstitutionException nfe) {
                    String sourceFile;
                    try {
                        MetaDataInfo info = metaData.get(sectionKey).get(parameterName);
                        sourceFile = info.getSourceFile().toString();
                    } catch (NullPointerException e) {
                        sourceFile = "(unknown)";
                    }
                    String message = "label substitution exception in file: " + sourceFile + ": section: "
                        + sectionKey + " label: " + parameterName + " on value: " + nfe.getValue() + " for value: "
                        + nfe.getParam();

                    newValue = processMissingLabel(message);
                }

                if (newValue != originalValue) {
                    Map<String, Object> sectionMods = modifications.get(sectionKey);
                    if (sectionMods == null) {
                        // lazy create
                        sectionMods = new HashMap<String, Object>(8);
                        modifications.put(sectionKey, sectionMods);
                    }
                    sectionMods.put(intern(parameterName), newValue);
                }
            }
        }

        for (Map.Entry<String, Map<String, Object>> sectionEntry : modifications.entrySet()) {
            String sectionName = sectionEntry.getKey();
            for (Map.Entry<String, Object> entry : sectionEntry.getValue().entrySet()) {
                this.data.put(sectionName, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Allow child implementations to substitute values based on an external set of params
     * @param sectionName the section of the param being replace
     * @param paramName the key of the param being replace
     * @param val the value being replaced
     * @param referenceConfig an object being passed in to parsing for 
     * @return the val with substrings substituted if need be
     * @throws IOException in case there are any IO Exceptions
     * @throws SubstitutionException in case the val has a malformed section/param
     */
    protected Object substitute(String sectionName, String paramName, Object val, Object referenceConfig) throws IOException, SubstitutionException {
    	return val;
    }

    public static class SubstitutionException extends Exception {
        private static final long serialVersionUID = 1L;
        private final String param;
        private final Object val;

        public SubstitutionException(String section, String param, Object val) {
            super("Could not find property: " + section + "." + param);
            this.param = section + "." + param;
            this.val = val;
        }

        public SubstitutionException(String property, Object val) {
            super("Could not find property: " + property);
            this.param = property;
            this.val = val;
        }

        public String getParam() { return this.param; }
        public Object getValue() { return this.val; }

    }

    protected PropertyFileData getPropertyFileData() {
        return this.data;
    }

    /**
     * Allows for some implementations to save a great deal of memory by sharing
     * maps across various language versions of the "same" properties/labels.
     */
    public void attachSharedKeyMap(SharedKeyMap<String, SharedKeyMap<String, Object>> seedKeyMap) {
        if (seedKeyMap == null) {
            return;
        }

        this.data.shareKeys(seedKeyMap);
    }

    public boolean isProductionMode() {
        return true;
    }

    public static class MetaDataInfo {

        private Locator locator;
        private File sourceFile;
        private boolean read = false;
        private boolean deprecated = false;

        public Locator getLocator() {
            return this.locator;
        }

        public void setLocator(Locator locator) {
            this.locator = locator;
        }

        public boolean isDeprecated() {
            return this.deprecated;
        }

        public void setDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
        }

        public boolean isRead() {
            return this.read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }

        public File getSourceFile() {
            return this.sourceFile;
        }

        public void setSourceFile(File sourceFile) {
            this.sourceFile = sourceFile;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof MetaDataInfo)) {
                return false;
            }
            MetaDataInfo mdi = (MetaDataInfo)o;
            return this.locator.equals(mdi.locator) && this.sourceFile.equals(mdi.sourceFile)
                && this.deprecated == mdi.deprecated;
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = 37 * hash + ((null != locator) ? locator.hashCode() : 0);
            hash = 37 * hash + ((null != sourceFile) ? sourceFile.hashCode() : 0);
            hash = 37 * hash + ((this.deprecated) ? 1 : 0);
            return hash;
        }
    }

    /**
     * Defaults to false if the string does not match one of the following
     *   - 1
     *   - true
     *   - yes
     *   - on
     */
    public static boolean stringToBoolean(String booleanValue) {
        if("1".equals(booleanValue)
                || "true".equals(booleanValue)
                || "yes".equals(booleanValue)
                || "on".equals(booleanValue)) {
            return true;
        }

        return false;
    }

    /**
     * Interface used by PropertyFile and its subclasses to parse and load its
     * internal map data structure (eg. from a file).
     *
     * @author nveeser
     */
    public interface Parser {
        void load(PropertyFileData data, Map<String, Map<String, MetaDataInfo>> metaData) throws IOException;
        // used for motif parsing - it helps determine the date that will be used in urls
        long getFileLastModified();
    }
}