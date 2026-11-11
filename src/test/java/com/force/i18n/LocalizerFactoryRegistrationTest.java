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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.force.i18n.LocalizerFactory.ReplacementAttempt;

/**
 * Registration contract of {@link LocalizerFactory}: legacy {@code set} until sealed, atomic {@code setIfAbsent},
 * validate-and-seal, and reporting of rejected replacements.
 */
class LocalizerFactoryRegistrationTest {

    private LocalizerFactoryRegistrationState saved;
    private final List<ReplacementAttempt> events = new CopyOnWriteArrayList<>();
    private final Consumer<ReplacementAttempt> recorder = this.events::add;

    @BeforeEach
    void captureState() {
        this.saved = LocalizerFactoryRegistrationState.capture();
        LocalizerFactoryRegistrationState.clear();
    }

    @AfterEach
    void restoreState() {
        this.saved.restore();
    }

    @Test
    void unsealedSetKeepsLegacyAssignment() {
        HostProvider a = new HostProvider();
        EmbeddedProvider b = new EmbeddedProvider();
        LocalizerFactory.set(a);
        assertSame(a, LocalizerFactory.get());
        LocalizerFactory.set(b);
        assertSame(b, LocalizerFactory.get());
        LocalizerFactory.set(null);
        assertNull(LocalizerFactory.get());
        assertFalse(LocalizerFactory.isSealed());
    }

    @Test
    void setIfAbsentInstallsOnlyWhenEmpty() {
        HostProvider a = new HostProvider();
        assertSame(a, LocalizerFactory.setIfAbsent(a));
        assertSame(a, LocalizerFactory.get());

        assertSame(a, LocalizerFactory.setIfAbsent(new EmbeddedProvider()));
        assertSame(a, LocalizerFactory.get());

        assertThrows(NullPointerException.class, () -> LocalizerFactory.setIfAbsent(null));
    }

    @Test
    void sealRequiresTheExpectedInstance() {
        assertThrows(NullPointerException.class, () -> LocalizerFactory.seal(null, this.recorder));
        assertThrows(IllegalStateException.class, () -> LocalizerFactory.seal(new HostProvider(), this.recorder));

        EmbeddedProvider first = new EmbeddedProvider();
        LocalizerFactory.set(first);
        HostProvider host = new HostProvider();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> LocalizerFactory.seal(host, this.recorder));
        assertTrue(e.getMessage().contains(EmbeddedProvider.class.getName()), e.getMessage());

        // a failed seal changes nothing: still unsealed, still the first provider
        assertFalse(LocalizerFactory.isSealed());
        assertSame(first, LocalizerFactory.get());
        LocalizerFactory.set(host);
        assertSame(host, LocalizerFactory.get());
    }

    @Test
    void sealedSetOfSameInstanceIsNoOp() {
        HostProvider host = sealedHost();
        LocalizerFactory.set(host);
        assertSame(host, LocalizerFactory.get());
        assertTrue(this.events.isEmpty());
    }

    @Test
    void sealedSetOfOtherProviderIsReportedThenThrown() {
        HostProvider host = sealedHost();

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new EmbeddedProvider()));

        assertSame(host, LocalizerFactory.get());
        assertEquals(1, this.events.size());
        ReplacementAttempt attempt = this.events.get(0);
        assertEquals(HostProvider.class.getName(), attempt.getInstalledClassName());
        assertEquals(EmbeddedProvider.class.getName(), attempt.getAttemptedClassName());
        assertFalse(attempt.isNullCandidate());
        assertEquals(Thread.currentThread().getName(), attempt.getThreadName());
        assertNotNull(attempt.getAttemptedClassLoader());
        assertTrue(callerStackMentions(attempt, "sealedSetOfOtherProviderIsReportedThenThrown"));
        assertEquals(attempt.describe(), e.getMessage());
    }

    @Test
    void sealedSetOfNullIsRejected() {
        HostProvider host = sealedHost();
        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(null));
        assertSame(host, LocalizerFactory.get());
        assertEquals(1, this.events.size());
        assertTrue(this.events.get(0).isNullCandidate());
        assertNull(this.events.get(0).getAttemptedClassName());
    }

    @Test
    void sealedSetOfSameClassDifferentInstanceIsRejected() {
        HostProvider host = sealedHost();
        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new HostProvider()));
        assertSame(host, LocalizerFactory.get());
        assertEquals(1, this.events.size());
    }

    @Test
    void sealedSetIfAbsentReturnsSealedProviderWithoutEvent() {
        HostProvider host = sealedHost();
        assertSame(host, LocalizerFactory.setIfAbsent(new EmbeddedProvider()));
        assertSame(host, LocalizerFactory.get());
        assertTrue(this.events.isEmpty());
    }

    @Test
    void resealingKeepsTheOriginalObserver() {
        HostProvider host = sealedHost();
        List<ReplacementAttempt> other = new ArrayList<>();
        LocalizerFactory.seal(host, other::add);

        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new EmbeddedProvider()));
        assertEquals(1, this.events.size());
        assertTrue(other.isEmpty());

        // and a different provider still can't be sealed in
        assertThrows(IllegalStateException.class, () -> LocalizerFactory.seal(new HostProvider(), other::add));
        assertSame(host, LocalizerFactory.get());
    }

    @Test
    void throwingObserverDoesNotHideTheRejection() {
        HostProvider host = new HostProvider();
        LocalizerFactory.set(host);
        LocalizerFactory.seal(host, a -> { throw new UnsupportedOperationException("observer bug"); });

        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new EmbeddedProvider()));
        assertSame(host, LocalizerFactory.get());
    }

    @Test
    void nullObserverStillRejects() {
        HostProvider host = new HostProvider();
        LocalizerFactory.set(host);
        LocalizerFactory.seal(host, null);

        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new EmbeddedProvider()));
        assertSame(host, LocalizerFactory.get());
    }

    @Test
    void observerThatCallsSetIsNotReentered() {
        HostProvider host = new HostProvider();
        LocalizerFactory.set(host);
        LocalizerFactory.seal(host, a -> {
            this.events.add(a);
            LocalizerFactory.set(new EmbeddedProvider());
        });

        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new EmbeddedProvider()));
        assertEquals(1, this.events.size());
        assertSame(host, LocalizerFactory.get());
    }

    @Test
    void observerRunsWithoutHoldingTheRegistrationLock() {
        HostProvider host = new HostProvider();
        LocalizerFactory.set(host);
        List<Object> seenFromOtherThread = new CopyOnWriteArrayList<>();
        LocalizerFactory.seal(host, a -> {
            // If set() still held the monitor, these calls from another thread would block until the timeout.
            try {
                seenFromOtherThread.add(CompletableFuture.supplyAsync(LocalizerFactory::isSealed).get(5, TimeUnit.SECONDS));
                seenFromOtherThread.add(CompletableFuture.supplyAsync(() -> LocalizerFactory.setIfAbsent(new EmbeddedProvider()))
                    .get(5, TimeUnit.SECONDS));
            } catch (Exception e) {
                seenFromOtherThread.add(e);
            }
        });

        assertThrows(IllegalStateException.class, () -> LocalizerFactory.set(new EmbeddedProvider()));
        assertEquals(Arrays.asList(Boolean.TRUE, host), seenFromOtherThread);
    }

    @Test
    void concurrentSetIfAbsentSelectsExactlyOneProvider() throws Exception {
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (int round = 0; round < 50; round++) {
                LocalizerFactoryRegistrationState.clear();
                CountDownLatch start = new CountDownLatch(1);
                List<Future<LocalizerProvider>> results = new ArrayList<>();
                for (int i = 0; i < threads; i++) {
                    LocalizerProvider mine = new EmbeddedProvider();
                    results.add(pool.submit(() -> {
                        start.await();
                        return LocalizerFactory.setIfAbsent(mine);
                    }));
                }
                start.countDown();
                Set<LocalizerProvider> selected = ConcurrentHashMap.newKeySet();
                for (Future<LocalizerProvider> f : results) {
                    selected.add(f.get(5, TimeUnit.SECONDS));
                }
                assertEquals(1, selected.size());
                assertSame(selected.iterator().next(), LocalizerFactory.get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void racingSealAndSetHaveOnlyConsistentOutcomes() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < 200; round++) {
                LocalizerFactoryRegistrationState.clear();
                this.events.clear();
                HostProvider host = new HostProvider();
                EmbeddedProvider intruder = new EmbeddedProvider();
                LocalizerFactory.set(host);
                CountDownLatch start = new CountDownLatch(1);
                Future<Boolean> sealed = pool.submit(() -> {
                    start.await();
                    try {
                        LocalizerFactory.seal(host, this.recorder);
                        return true;
                    } catch (IllegalStateException e) {
                        return false;
                    }
                });
                Future<Boolean> replaced = pool.submit(() -> {
                    start.await();
                    try {
                        LocalizerFactory.set(intruder);
                        return true;
                    } catch (IllegalStateException e) {
                        return false;
                    }
                });
                start.countDown();
                boolean sealWon = sealed.get(5, TimeUnit.SECONDS);
                boolean setWon = replaced.get(5, TimeUnit.SECONDS);
                if (sealWon) {
                    // seal ran first: the set is rejected and reported
                    assertFalse(setWon);
                    assertSame(host, LocalizerFactory.get());
                    assertEquals(1, this.events.size());
                } else {
                    // set ran first: sealing the host then fails visibly instead of legitimizing the intruder
                    assertTrue(setWon);
                    assertSame(intruder, LocalizerFactory.get());
                    assertFalse(LocalizerFactory.isSealed());
                    assertTrue(this.events.isEmpty());
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private HostProvider sealedHost() {
        HostProvider host = new HostProvider();
        LocalizerFactory.set(host);
        LocalizerFactory.seal(host, this.recorder);
        assertTrue(LocalizerFactory.isSealed());
        return host;
    }

    private static boolean callerStackMentions(ReplacementAttempt attempt, String methodName) {
        return Arrays.stream(attempt.getCallerStack().getStackTrace()).anyMatch(f -> methodName.equals(f.getMethodName()));
    }

    /** Stand-in for the hosting application's provider. */
    static class HostProvider implements LocalizerProvider {
        @Override public BaseLocalizer getLabelLocalizer(HumanLanguage language) { return null; }
        @Override public BaseLocalizer getLocalizer(Locale locale, Locale currencyLocale, HumanLanguage language, TimeZone timeZone) { return null; }
        @Override public BaseLocalizer getLocalizer(Locale langLocale) { return null; }
        @Override public BaseLocalizer getLocalizer(HumanLanguage language) { return null; }
        @Override public LabelSet findLabelSet(HumanLanguage language) { return null; }
        @Override public BaseLocalizer getDefaultLocalizer() { return null; }
        @Override public BaseLocalizer getEnglishLocalizer() { return null; }
        @Override public URL getLabelsDirectory() { return null; }
    }

    /** Stand-in for an embedded library's provider. */
    static class EmbeddedProvider extends HostProvider {
    }
}
