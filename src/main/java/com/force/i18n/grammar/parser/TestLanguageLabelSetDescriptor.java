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

import java.net.URL;
import java.util.List;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;

/**
 * A descriptor that includes some "text" that can be used in various label-related utilities
 * for testing labels.
 * @author stamm
 */
public class TestLanguageLabelSetDescriptor implements GrammaticalLabelSetDescriptor {
    private final GrammaticalLabelSetDescriptor delegate;
    private final String text;
    private final String grammar;

    /**
     * Construct the test language label set descriptor
     * @param delegate The delegate that represents the label set to which the test should be added
     * @param text the label XML file that is added to the set of the other labels
     * @param grammarOverrides an optional sfdcnames.xml style file that contains overrides for the grammar
     */
    public TestLanguageLabelSetDescriptor(GrammaticalLabelSetDescriptor delegate, String text, String grammarOverrides) {
        this.delegate = delegate;
        this.text = text;
        this.grammar = grammarOverrides;
    }

    @Override
    public URL getRootDir() {
        return delegate.getRootDir();
    }

    @Override
    public URL getRootFile() {
        return delegate.getRootFile();
    }

    @Override
    public boolean hasOverridingFiles() {
        return delegate.hasOverridingFiles();
    }

    @Override
    public List<URL> getOverridingFiles() {
        return delegate.getOverridingFiles();
    }

    @Override
    public HumanLanguage getLanguage() {
        return delegate.getLanguage();
    }

    @Override
    public GrammaticalLabelSetDescriptor getForOtherLanguage(HumanLanguage otherLanguage) {
        return delegate.getForOtherLanguage(otherLanguage);
    }

    @Override
    public URL getDictionaryFile() {
        return delegate.getDictionaryFile();
    }

    @Override
    public List<URL> getOverridingDictionaryFiles() {
        return delegate.getOverridingDictionaryFiles();
    }

    @Override
    public String getLabelSetName() {
        return delegate.getLabelSetName();
    }

	@Override
	public boolean hasModularizedFiles() {
		return delegate.hasModularizedFiles();
	}

	@Override
	public List<URL> getModularizedFiles() {
		return delegate.getModularizedFiles();
	}

    /**
     * @return the contents of the xml label file that should be added to those in the file system/
     */
    public String getText() {
        return this.text;
    }

    /**
     * @return an optional sfdcnames.xml style file that contains overrides for the grammar
     */
    public String getGrammar() {
        return this.grammar;
    }

///CLOVER:OFF
    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ((text == null) ? 0 : text.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        TestLanguageLabelSetDescriptor other = (TestLanguageLabelSetDescriptor)obj;
        return text == null ? other.text == null : text.equals(other.text);
    }
///CLOVER:ON
}
