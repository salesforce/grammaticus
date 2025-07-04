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

import java.io.Serializable;

/**
 * A base interface for all the grammatical forms.
 * @author stamm
 */
public interface GrammaticalForm extends Serializable {
    /**
     * @return the number associated with this adjective form
     */
    LanguageNumber getNumber();
    /**
     * @return the grammatical case associated with this adjective form
     */
    LanguageCase getCase();

    /**
     * @return a HTML compatible screen that can be used to represent this grammatical
     * form uniquely when compared to all other forms.
     */
    String getKey();
}
