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
