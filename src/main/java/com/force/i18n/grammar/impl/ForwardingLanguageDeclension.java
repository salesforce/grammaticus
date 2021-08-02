/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.impl;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.force.i18n.HumanLanguage;
import com.force.i18n.commons.util.collection.IntHashMap;
import com.force.i18n.commons.util.collection.IntMap;
import com.force.i18n.grammar.LanguageDeclension;
import com.google.common.base.Objects;


/**
 * Forwarding proxy implementation for {@code LanguageDeclension}.
 *
 * @author yoikawa
 * @since 1.1.0
 */
public class ForwardingLanguageDeclension implements InvocationHandler {
    private final LanguageDeclension delegate;
    private final HumanLanguage language;

    // cached methods by name.  see invoke(Object, Method, Object[])
    private final IntMap<Method> methods = new IntHashMap<Method>();

    // methods to "override" in forwarding proxy. note that this only cares mehod's name, no args
    // TODO: if this is just for one method, maybe better to hard-code in invoke()
    private static final String[] overrideMethodNames = { "getLanguage"};
    private static final Map<String, Method> methodOverrides = new HashMap<>();
    static {
        for (String name : overrideMethodNames) {
            try {
                methodOverrides.put(name, ForwardingLanguageDeclension.class.getMethod(name));
            } catch (NoSuchMethodException | SecurityException e) {
                // this should not happen
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Construct a new forwarding proxy object for the given declension to override behavior.
     *
     * @param language the language for this declension
     * @param delegate the {@code LanguageDeclension} object forwarding to
     * @return {@link LanguageDeclension} object that proxies {@code delegate}
     */
    public static final LanguageDeclension newInstance(HumanLanguage language, LanguageDeclension delegate) {
        return (LanguageDeclension)Proxy.newProxyInstance(
            delegate.getClass().getClassLoader(),
                new Class[] { LanguageDeclension.WithClassifiers.class, LanguageDeclension.class },
                new ForwardingLanguageDeclension(language, delegate));
    }

    /**
     * Convenient method to determine if the desclension is forwarding proxy.
     *
     * @param declension the declension to test
     * @return {@code true} if the given declension is forwarding proxy, {@code false} otherwise.
     */
    static final boolean isForwardingProxy(LanguageDeclension declension) {
        return Proxy.isProxyClass(declension.getClass());
    }

    private Integer getKey(Method method) {
        return Objects.hashCode(method.getName(), Arrays.asList(method.getParameterTypes()));
    }

    private ForwardingLanguageDeclension(HumanLanguage language, LanguageDeclension delegate) {
        assert language != null && delegate != null;
        this.language = language;
        this.delegate = delegate;

        // cache all methods of given object. see invoke(Object, Method, Object[])
        for(Method method: delegate.getClass().getMethods()) {
            this.methods.put(getKey(method), method);
        }
    }

    /**
     * The implementation to override {@link com.force.i18n.grammar.LanguageDeclension#getLanguage()}
     * @return {@code HumanLanguage} this proxy associate with.
     */
    public final HumanLanguage getLanguage() {
        return this.language;
    }

    public final LanguageDeclension getDelegate() {
        return this.delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // test if we want to re-route to this class method otherwise, forward the call to the delegate object
        Method override = methodOverrides.get(method.getName());
        if (override != null) {
            return override.invoke(this, args);
        }

        try {
            return methods.get(getKey(method)).invoke(delegate, args);
        } catch (Exception ex) {
            // this should not happen
            throw new RuntimeException(ex);
        }
    }
}
