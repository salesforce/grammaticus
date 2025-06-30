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

import com.force.i18n.commons.util.settings.NonConfigIniFile;

/**
 * This interface describes a set of labels available through localization.
 * */
public interface SharedLabelSet extends NonConfigIniFile {

    // Pulled out of property file because all LabelSets (not just extensions of property file) need this.
    public boolean labelExists(String section, String param);
}
