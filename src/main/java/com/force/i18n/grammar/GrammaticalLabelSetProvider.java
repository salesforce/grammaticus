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

package com.force.i18n.grammar;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LabelSetProvider;

/**
 * A label set provider that returns a grammatical label set.
 * Uses fancy java 1.5 covariance
 *
 * @author stamm
 */
public interface GrammaticalLabelSetProvider extends LabelSetProvider {
    @Override
    GrammaticalLabelSet getSet(HumanLanguage language);
}
