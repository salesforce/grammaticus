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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Usage tracking is registered independently of the label-debug provider, so registering a tracker must not
 * change what {@link LabelDebug#isLabelHintAllowed()} reports.
 */
class LabelUsageTest {

    private LabelDebugProvider originalDebugProvider;
    private LabelUsageTracking originalUsageTracker;

    @BeforeEach
    void captureState() {
        this.originalDebugProvider = LabelDebugProvider.get();
        this.originalUsageTracker = LabelUsage.get();
    }

    @AfterEach
    void restoreState() {
        // Restore the debug provider first -- that may itself register a tracker through the compatibility
        // bridge -- then put the usage registry back to exactly what it held before the test.
        LabelDebugProvider.setLabelDebugProviderEnabled(this.originalDebugProvider);
        LabelUsage.set(this.originalUsageTracker);
    }

    @Test
    @DisplayName("nothing is tracked until a tracker is registered")
    void defaultsToNone() {
        LabelUsage.set(LabelUsageTracking.NONE);

        assertSame(LabelUsageTracking.NONE, LabelUsage.get());
        assertFalse(LabelUsage.get().isTrackingLabelUsage());
        assertTrue(LabelUsage.get().getUsedLabels().isEmpty());
        LabelUsage.get().trackLabel("Sample", "anything");
    }

    @Test
    @DisplayName("a registered tracker receives label reads")
    void registeredTrackerReceivesReads() {
        RecordingTracker tracker = new RecordingTracker();
        LabelUsage.set(tracker);

        assertSame(tracker, LabelUsage.get());
        LabelUsage.get().trackLabel("Sample", "some_label");

        assertEquals(List.of("Sample.some_label"), tracker.recorded);
    }

    @Test
    @DisplayName("the registry rejects null")
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> LabelUsage.set(null));
    }

    @Test
    @DisplayName("clear(expected) resets to NONE only when the expected tracker is still registered")
    void clearIsConditional() {
        RecordingTracker tracker = new RecordingTracker();
        LabelUsage.set(tracker);

        assertTrue(LabelUsage.clear(tracker), "clear must succeed when the expected tracker is current");
        assertSame(LabelUsageTracking.NONE, LabelUsage.get());
    }

    @Test
    @DisplayName("clear(stale) does not erase a tracker another caller installed after the fact")
    void clearDoesNotEraseNewerRegistration() {
        RecordingTracker first = new RecordingTracker();
        RecordingTracker second = new RecordingTracker();
        LabelUsage.set(first);

        // Simulate the deregistration race: `first` decides to clear itself, but `second` has already taken the
        // slot in between. A check-then-set clear would wipe `second`; the conditional clear must leave it.
        LabelUsage.set(second);
        assertFalse(LabelUsage.clear(first), "clearing a stale tracker must not report success");

        assertSame(second, LabelUsage.get(), "a newer registration must survive a stale caller's clear");
    }

    @Test
    @DisplayName("registering a tracker does not grant label-hint permission")
    void trackerDoesNotGrantLabelHints() {
        LabelDebugProvider.setLabelDebugProviderEnabled(false);
        assertFalse(LabelDebug.isLabelHintAllowed());

        LabelUsage.set(new RecordingTracker());

        assertFalse(LabelDebug.isLabelHintAllowed(), "usage tracking must not widen label-debug permission");
        assertTrue(LabelUsage.get().isTrackingLabelUsage());
    }

    @Test
    @DisplayName("registering a tracker does not displace the debug provider")
    void trackerDoesNotDisplaceDebugProvider() {
        LabelDebugProvider.setLabelDebugProviderEnabled(true);
        LabelDebugProvider debugProvider = LabelDebugProvider.get();

        LabelUsage.set(new RecordingTracker());

        assertSame(debugProvider, LabelDebugProvider.get());
        assertTrue(LabelDebug.isLabelHintAllowed());
    }

    @Test
    @DisplayName("label hints render while a separate tracker collects usage")
    void debugHintsAndUsageTrackingCoexist() {
        LabelDebugProvider.setLabelDebugProviderEnabled(true);
        LabelDebugProvider debugProvider = LabelDebugProvider.get();
        RecordingTracker tracker = new RecordingTracker();
        LabelUsage.set(tracker);

        boolean wasHintRequest = debugProvider.setLabelHintRequest(true);
        try {
            String hinted = debugProvider.makeLabelHintIfRequested("Account", "Sample", "account");
            LabelUsage.get().trackLabel("Sample", "account");

            assertTrue(hinted.startsWith("Account[#"), () -> "label hint was not rendered: " + hinted);
            assertTrue(LabelDebug.isLabelHintAllowed());
            assertEquals(List.of("Sample.account"), tracker.recorded);
            assertSame(tracker, LabelUsage.get(), "the debug provider must not take the registry slot it never asked for");
        } finally {
            debugProvider.setLabelHintRequest(wasHintRequest);
        }
    }

    @Test
    @DisplayName("only one tracker collects: the debug provider's tracking flow takes over the slot")
    void debugProviderTrackingDisplacesAnotherTracker() {
        RecordingTracker tracker = new RecordingTracker();
        LabelUsage.set(tracker);
        LabelDebugProvider.setLabelDebugProviderEnabled(true);
        LabelDebugProvider debugProvider = LabelDebugProvider.get();

        debugProvider.setTrackingLabelUsage(true);

        assertSame(debugProvider, LabelUsage.get());
        LabelUsage.get().trackLabel("Sample", "account");
        assertTrue(tracker.recorded.isEmpty(), "the displaced tracker stops receiving reads");

        // and switching it back off does not restore the tracker it displaced
        debugProvider.setTrackingLabelUsage(false);
        assertSame(LabelUsageTracking.NONE, LabelUsage.get());
    }

    @Test
    @DisplayName("the debug provider's own tracking flow registers and deregisters itself")
    void debugProviderTrackingRegistersItself() {
        LabelDebugProvider.setLabelDebugProviderEnabled(true);
        LabelDebugProvider debugProvider = LabelDebugProvider.get();

        debugProvider.setTrackingLabelUsage(true);
        assertSame(debugProvider, LabelUsage.get());

        debugProvider.setTrackingLabelUsage(false);
        assertSame(LabelUsageTracking.NONE, LabelUsage.get());
    }

    @Test
    @DisplayName("replacing the debug provider clears a tracker it had registered")
    void replacingDebugProviderClearsItsTracker() {
        LabelDebugProvider.setLabelDebugProviderEnabled(true);
        LabelDebugProvider.get().setTrackingLabelUsage(true);

        LabelDebugProvider.setLabelDebugProviderEnabled(false);

        assertSame(LabelUsageTracking.NONE, LabelUsage.get());
    }

    @Test
    @DisplayName("replacing the debug provider leaves an unrelated tracker registered")
    void replacingDebugProviderKeepsUnrelatedTracker() {
        RecordingTracker tracker = new RecordingTracker();
        LabelUsage.set(tracker);

        LabelDebugProvider.setLabelDebugProviderEnabled(true);

        assertSame(tracker, LabelUsage.get());
    }

    @Test
    @DisplayName("installing a tracking debug provider registers it to receive reads (Core-tracker compat)")
    void installingTrackingProviderReceivesReads() {
        // Mirrors how Salesforce Core installs its LabelUsageTracker: a LabelDebugProvider subclass that always
        // tracks, registered through setLabelDebugProviderEnabled(provider) -- never through setTrackingLabelUsage.
        // Reads must reach it, otherwise production telemetry silently drops to LabelUsageTracking.NONE.
        RecordingDebugProvider provider = new RecordingDebugProvider();
        LabelDebugProvider.setLabelDebugProviderEnabled(provider);

        assertSame(provider, LabelUsage.get(), "a tracking provider must occupy the usage-registry slot on install");
        LabelUsage.get().trackLabel("Sample", "account");
        assertEquals(List.of("Sample.account"), provider.recorded);
    }

    @Test
    @DisplayName("installing a non-tracking debug provider does not claim the registry slot")
    void installingNonTrackingProviderLeavesRegistry() {
        RecordingTracker unrelated = new RecordingTracker();
        LabelUsage.set(unrelated);

        // A plain enabled provider is not a tracker; it must not seize the slot from an unrelated tracker.
        LabelDebugProvider.setLabelDebugProviderEnabled(true);

        assertSame(unrelated, LabelUsage.get(), "a non-tracking provider must not displace an unrelated tracker");
    }

    private static final class RecordingTracker implements LabelUsageTracking {
        private final List<String> recorded = new ArrayList<>();

        @Override
        public void trackLabel(String section, String key) {
            this.recorded.add(section + "." + key);
        }

        @Override
        public boolean isTrackingLabelUsage() {
            return true;
        }

        @Override
        public SetMultimap<String, String> getUsedLabels() {
            return ImmutableSetMultimap.of();
        }
    }

    /**
     * A {@link LabelDebugProvider} that is itself an always-on tracker, mirroring Salesforce Core's
     * {@code LabelUsageTracker}: it is installed through {@link LabelDebugProvider#setLabelDebugProviderEnabled}
     * and reports {@link #isTrackingLabelUsage()} == true, but never calls {@link #setTrackingLabelUsage}.
     */
    private static final class RecordingDebugProvider extends LabelDebugProvider {
        private final List<String> recorded = new ArrayList<>();

        @Override
        public boolean isAllowed() {
            return false;
        }

        @Override
        public String makeLabelHintIfRequested(String text, String section, String key) {
            return text;
        }

        @Override
        public boolean isTrackingLabelUsage() {
            return true;
        }

        @Override
        public void setTrackingLabelUsage(boolean trackUsage) {
            // always tracks; nothing to toggle
        }

        @Override
        public void trackLabel(String section, String key) {
            this.recorded.add(section + "." + key);
        }

        @Override
        public SetMultimap<String, String> getUsedLabels() {
            return ImmutableSetMultimap.of();
        }

        @Override
        public boolean setLabelHintRequest(boolean value) {
            return value;
        }

        @Override
        public void setLabelHintMode(String value) {
            // no label-hint rendering in this telemetry-only fixture
        }
    }
}
