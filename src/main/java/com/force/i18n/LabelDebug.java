/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.Map;

import com.google.common.base.Objects;


/**
 * Represents a single "entry" in the set of label debugs generated for a request.
 * @see LabelDebugProvider
 * @author RChen
 */
public final class LabelDebug {
    private final String text;
    private final String section;
    private final String param;
    private final String stack;

    /**
     * This should only be used internally by makeLabelHintIfRequested(String, String, String).
     * @param text Text of the label
     * @param section Label section name
     * @param key Label param name
     */
    LabelDebug(String text, String section, String key, String stack) {
        this.text = text;
        this.section = section;
        this.param = key;
        this.stack = stack;
    }

    public String getParameter() {
        return param;
    }

    public String getSection() {
        return section;
    }

    public String getStack() {
        return stack;
    }

    public String getFilename() {
        if (!isLabelHintAllowed()) {
            throw new IllegalArgumentException("Only available when label hints are available.");
        }

        Map<String,String> fileMap = ((LabelSet)LocalizerFactory.get().getDefaultLocalizer().getLabelSet()).getLabelSectionToFilename();
        assert null != fileMap : "Didn't load label section to filename";
        return fileMap.get(getSection());
    }

    public String getText() {
        return this.text;
    }

    @Override
	public int hashCode() {
    	return Objects.hashCode(param, section, stack, text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		LabelDebug other = (LabelDebug) obj;
		return Objects.equal(param, other.param)
				&& Objects.equal(section, other.section)
				&& Objects.equal(stack, other.stack)
				&& Objects.equal(text, other.text);
	}

	@Override
	public String toString() {
		return "LabelDebug [" + section + "." + param + ", text=" + text + "]";
	}

	// Convenience method
    public static boolean isLabelHintAllowed() {
        return LabelDebugProvider.get().isAllowed();
    }
}
