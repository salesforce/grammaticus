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

import java.security.CodeSource;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  The real nitty-gritty of localization - every App should have 1 of these
 *  Note: this provides access to loaders, but not much else.
 */
public abstract class LocalizerFactory implements LocalizerProvider {

    private static final Logger logger = Logger.getLogger(LocalizerFactory.class.getName());

    // Guards INSTANCE writes, sealed and observer. Never call out (observer, logging) while holding it.
    private static final Object REGISTRATION_LOCK = new Object();
    private static final ThreadLocal<Boolean> REPORTING = new ThreadLocal<>();

	private static volatile LocalizerProvider INSTANCE;
    private static boolean sealed;
    private static Consumer<ReplacementAttempt> observer;

	// TODO: Put this someplace else
	public static LocalizerProvider get() {
		return INSTANCE;
	}

    /**
     * Replace the JVM-wide provider. Until {@link #seal(LocalizerProvider, Consumer)} is called this is a plain
     * assignment (including {@code null}). After sealing, setting the sealed provider again is a no-op, and any other
     * value is rejected: the sealed provider is kept, the attempt is reported, and an
     * {@link IllegalStateException} is thrown to the caller.
     * @param factory the provider to install
     * @throws IllegalStateException if the registration is sealed to a different provider
     */
	public static void set(LocalizerProvider factory) {
        Class<?> installedClass;
        Consumer<ReplacementAttempt> sink;
        synchronized (REGISTRATION_LOCK) {
            LocalizerProvider current = INSTANCE;
            if (!sealed) {
                INSTANCE = factory;
                return;
            }
            if (factory == current) {
                return;
            }
            installedClass = current.getClass();
            sink = observer;
        }
        ReplacementAttempt attempt = new ReplacementAttempt(installedClass, factory);
        report(attempt, sink);
        throw new IllegalStateException(attempt.describe());
	}

    /**
     * Install {@code candidate} only if no provider is registered yet. Use this from libraries that need a provider
     * but must not replace the one owned by the hosting application.
     * @param candidate the provider to install when none is registered
     * @return the provider that is registered after the call: the existing one, or {@code candidate}
     * @throws NullPointerException if {@code candidate} is null
     */
    public static LocalizerProvider setIfAbsent(LocalizerProvider candidate) {
        Objects.requireNonNull(candidate, "candidate");
        synchronized (REGISTRATION_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = candidate;
            }
            return INSTANCE;
        }
    }

    /**
     * Protect the registered provider from later replacement. This validates, it does not install: the current
     * provider must be exactly (by reference) {@code expected}, which keeps a host from sealing a provider that some
     * other component installed first. Sealing the same provider again is a no-op and keeps the original observer.
     * There is intentionally no way to unseal.
     * <p>
     * {@code observer} receives every rejected {@link #set(LocalizerProvider)} after sealing. It is called on the
     * offending thread, outside of any lock, before the rejection is thrown; it must be cheap and must not call back
     * into the setters of this class. A {@code null} observer means the rejection is only logged through
     * {@code java.util.logging}.
     * @param expected the provider the caller owns and expects to be registered
     * @param observer receives rejected replacement attempts, or {@code null}
     * @throws NullPointerException if {@code expected} is null
     * @throws IllegalStateException if the registered provider is not {@code expected}
     */
    public static void seal(LocalizerProvider expected, Consumer<ReplacementAttempt> observer) {
        Objects.requireNonNull(expected, "expected");
        synchronized (REGISTRATION_LOCK) {
            LocalizerProvider current = INSTANCE;
            if (current != expected) {
                throw new IllegalStateException("Cannot seal LocalizerFactory to " + expected.getClass().getName()
                    + ": registered provider is " + (current == null ? "null" : current.getClass().getName()));
            }
            if (!sealed) {
                sealed = true;
                LocalizerFactory.observer = observer;
            }
        }
    }

    /**
     * @return whether {@link #seal(LocalizerProvider, Consumer)} has protected the registered provider
     */
    public static boolean isSealed() {
        synchronized (REGISTRATION_LOCK) {
            return sealed;
        }
    }

    private static void report(ReplacementAttempt attempt, Consumer<ReplacementAttempt> sink) {
        if (sink == null) {
            logger.log(Level.SEVERE, attempt.describe(), attempt.getCallerStack());
            return;
        }
        if (REPORTING.get() != null) {
            // The observer itself tried to replace the provider; don't recurse into it.
            logger.log(Level.SEVERE, "Rejected inside LocalizerFactory observer: " + attempt.describe(),
                attempt.getCallerStack());
            return;
        }
        REPORTING.set(Boolean.TRUE);
        try {
            sink.accept(attempt);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, attempt.describe(), attempt.getCallerStack());
            logger.log(Level.WARNING, "LocalizerFactory replacement observer failed", e);
        } finally {
            REPORTING.remove();
        }
    }

    /**
     * Immutable description of a {@link LocalizerFactory#set(LocalizerProvider)} call that was rejected because the
     * registration is sealed. It only holds names and the caller's stack, never the attempted provider itself.
     */
    public static final class ReplacementAttempt {
        private final String installedClassName;
        private final String attemptedClassName;
        private final String attemptedClassLoader;
        private final String attemptedCodeSource;
        private final String threadName;
        private final Throwable callerStack;

        ReplacementAttempt(Class<?> installedClass, LocalizerProvider attempted) {
            this.callerStack = new Throwable("LocalizerFactory.set caller");
            this.threadName = Thread.currentThread().getName();
            this.installedClassName = installedClass.getName();
            Class<?> attemptedClass = attempted == null ? null : attempted.getClass();
            this.attemptedClassName = attemptedClass == null ? null : attemptedClass.getName();
            this.attemptedClassLoader = attemptedClass == null ? null : String.valueOf(attemptedClass.getClassLoader());
            this.attemptedCodeSource = attemptedClass == null ? null : codeSourceOf(attemptedClass);
        }

        private static String codeSourceOf(Class<?> c) {
            try {
                CodeSource cs = c.getProtectionDomain().getCodeSource();
                return cs == null || cs.getLocation() == null ? null : cs.getLocation().toString();
            } catch (RuntimeException e) {
                return null;
            }
        }

        /** @return class name of the sealed provider that was kept */
        public String getInstalledClassName() {
            return installedClassName;
        }

        /** @return class name of the rejected provider, or {@code null} if {@link #isNullCandidate()} */
        public String getAttemptedClassName() {
            return attemptedClassName;
        }

        /** @return whether the rejected call was {@code set(null)} */
        public boolean isNullCandidate() {
            return attemptedClassName == null;
        }

        /** @return the class loader of the rejected provider's class, or {@code null} if unavailable */
        public String getAttemptedClassLoader() {
            return attemptedClassLoader;
        }

        /** @return the code source (jar or directory) of the rejected provider's class, or {@code null} if unavailable */
        public String getAttemptedCodeSource() {
            return attemptedCodeSource;
        }

        /** @return the name of the thread that called {@code set} */
        public String getThreadName() {
            return threadName;
        }

        /** @return a throwable whose stack trace is the {@code set} caller's stack; it is never thrown */
        public Throwable getCallerStack() {
            return callerStack;
        }

        /** @return a one-line summary suitable for logs and exception messages */
        public String describe() {
            return "LocalizerFactory is sealed to " + installedClassName + "; rejected set("
                + (isNullCandidate() ? "null" : attemptedClassName) + ") on thread " + threadName
                + (attemptedCodeSource == null ? "" : " from " + attemptedCodeSource);
        }

        @Override
        public String toString() {
            return describe();
        }
    }


    private static final AtomicReference<Locale> default_locale =
    		new AtomicReference<Locale>(Locale.US);  // TODO: Less 'mericun


	/* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLabelLocalizer(i18n.UserLanguage)
	 */
    @Override
	public BaseLocalizer getLabelLocalizer(HumanLanguage language) {
        return getLocalizer(language.getLocale(), language.getLocale(), language, TimeZone.getDefault());
    }



    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLocalizer(java.util.Locale)
	 */
    @Override
	public BaseLocalizer getLocalizer(Locale langLocale) {
        return getLocalizer(langLocale, null, // currency locale
            LanguageProviderFactory.get().getLanguageForLocale(langLocale), TimeZone.getDefault());
    }

    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getLocalizer(i18n.UserLanguage)
	 */
    @Override
	public BaseLocalizer getLocalizer(HumanLanguage language) {
        return getLocalizer(language.getLocale(), null, // currency locale
            language, TimeZone.getDefault());
    }

    /* (non-Javadoc)
	 * @see shared.i18n.LocalizerProvider#getDefaultLocalizer()
	 */
    @Override
	public BaseLocalizer getDefaultLocalizer() {
        return getLocalizer(LocalizerFactory.getDefaultLocale(),
            LocalizerFactory.getDefaultLocale(),
            LocalizerFactory.getDefaultLanguage(), TimeZone.getDefault());
    }


    public static Locale getDefaultLocale() {
        return default_locale.get();
    }

    public static HumanLanguage getDefaultLanguage() {
    	return LanguageProviderFactory.get().getLanguageForLocale(default_locale.get());
    }


    @Override
	public BaseLocalizer getEnglishLocalizer() {
        return getLocalizer(Locale.US, Locale.US, HumanLanguage.Helper.get(Locale.US), TimeZone.getDefault());
    }

    /**
     * Return the translated name of the language.  This will be the same in all languages, as retrieved from
     * public_i18n.xml (With ENGLISH using the special "en" label instead of en_US)
     * @param language the language for whom the language should be retrieved
     * @return the name of the human language in that language
     */
    public static String getTranslatedLanguageLabel(HumanLanguage language) {
        assert language != null : "You cannot provide a null language to this method";
        BaseLocalizer loc = get().getDefaultLocalizer();
        String label = loc.getLabel("translatedlanguagename", language.getLabelKey());
        if (language.getDirection() != loc.getUserLanguage().getDirection()) {  // Only embed for languages where the direction differs
            label = language.getDirection().addEmbeddingMarks(label);
        }
        return label;
     }

}
