/*
 * Copyright (c) 2026, Salesforce, Inc.
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

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Records which labels an application actually reads.
 * <p>
 * This is deliberately separate from {@link LabelDebugProvider}. Label debugging decides what a developer is
 * permitted to see; usage tracking is telemetry about what the application touched. They share no state and no
 * lifecycle, and a component that wants only telemetry should not have to displace the debug provider — nor
 * assert anything about label-hint permission — to register itself.
 * <p>
 * Register an implementation with {@link LabelUsage#set(LabelUsageTracking)}. Implementations must be
 * thread-safe: {@link #trackLabel(String, String)} is called on the label read path from every request thread.
 *
 * @see LabelUsage
 * @since 1.3.7
 */
public interface LabelUsageTracking {

    /**
     * Record that a label was read. Called on the hot path, so implementations should be cheap and must not
     * throw.
     *
     * @param section the section of the label
     * @param key the key (param) of the label
     */
    void trackLabel(String section, String key);

    /**
     * @return whether this tracker is currently recording usage
     */
    boolean isTrackingLabelUsage();

    /**
     * @return the labels recorded so far, as an unmodifiable multimap from section to key
     */
    SetMultimap<String, String> getUsedLabels();

    /**
     * The default: records nothing.
     */
    LabelUsageTracking NONE = new LabelUsageTracking() {
        @Override
        public void trackLabel(String section, String key) {
            // do nothing
        }

        @Override
        public boolean isTrackingLabelUsage() {
            return false;
        }

        @Override
        public SetMultimap<String, String> getUsedLabels() {
            return ImmutableSetMultimap.of();
        }
    };
}
