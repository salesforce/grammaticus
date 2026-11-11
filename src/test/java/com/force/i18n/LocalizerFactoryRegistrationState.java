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

import java.lang.reflect.Field;

/**
 * Test-only snapshot of {@link LocalizerFactory}'s registration state (provider, sealed flag, observer).
 * Production code deliberately has no way to unseal, so tests that seal restore the snapshot afterwards.
 */
final class LocalizerFactoryRegistrationState {
    private static final Field LOCK = field("REGISTRATION_LOCK");
    private static final Field INSTANCE = field("INSTANCE");
    private static final Field SEALED = field("sealed");
    private static final Field OBSERVER = field("observer");

    private final Object instance;
    private final Object sealed;
    private final Object observer;

    private LocalizerFactoryRegistrationState(Object instance, Object sealed, Object observer) {
        this.instance = instance;
        this.sealed = sealed;
        this.observer = observer;
    }

    static LocalizerFactoryRegistrationState capture() {
        try {
            synchronized (LOCK.get(null)) {
                return new LocalizerFactoryRegistrationState(INSTANCE.get(null), SEALED.get(null), OBSERVER.get(null));
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /** Put the captured state back, including an unsealed flag if it was unsealed when captured. */
    void restore() {
        try {
            synchronized (LOCK.get(null)) {
                INSTANCE.set(null, this.instance);
                SEALED.set(null, this.sealed);
                OBSERVER.set(null, this.observer);
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /** Reset to an empty, unsealed registry. */
    static void clear() {
        new LocalizerFactoryRegistrationState(null, Boolean.FALSE, null).restore();
    }

    private static Field field(String name) {
        try {
            Field f = LocalizerFactory.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
}
