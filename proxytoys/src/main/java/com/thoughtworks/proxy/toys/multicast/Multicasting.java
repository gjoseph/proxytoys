/*
 * (c) 2003-2005, 2009, 2010 ThoughtWorks Ltd
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 14-May-2004
 */
package com.thoughtworks.proxy.toys.multicast;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;
import com.thoughtworks.proxy.kit.ReflectionUtils;

/**
 * Toy factory to create proxies delegating a call to multiple objects and managing the individual results.
 *
 * @author Dan North
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 * @author Juan Li
 * @author Paul Hammant
 * @see com.thoughtworks.proxy.toys.multicast
 * @since 0.1
 */
public class Multicasting<T> {
    private Class<?>[] types;
    private List<?> delegates;

    private Multicasting(List<Object> delegates) {
        this.delegates = delegates;
    }

    private Multicasting(Class<?> primaryType, Class<?>... types) {
        this.types = ReflectionUtils.makeTypesArray(primaryType, types);
    }

    /**
     * Creates a factory for proxy instances delegating a call to multiple objects and managing the individual results.
     *
     * @param primaryType the primary type implemented by the proxy
     * @param types other types that are implemented by the proxy
     * @return a factory that will proxy instances of the supplied type.
     * @since 1.0
     */
    public static <T> MulticastingWith<T> proxy(Class<T> primaryType, Class<?>... types) {
        return new MulticastingWith<T>(primaryType, types);
    }

    /**
     * Creates a factory for proxy instances delegating a call to multiple objects and managing the individual results.
     *
     * @param targets targets the target objects
     * @return a factory that will proxy instances of the supplied type.
     * @since 1.0
     * @see {@link #proxyList(java.util.List)}
     */
    public static MulticastingBuild<Multicast> proxy(Object... targets) {
        return proxyList(Arrays.asList(targets));
    }

    /**
     * Creates a factory for proxy instances delegating a call to multiple objects and managing the individual results.
     * The targets List can be modified after creation of the proxy to add or remove objects the proxy should delegate to.
     * If you do this in a multithreaded environment you should either make sure modification of the targets List and
     * invocations on the created proxy happens in the same thread, or use a synchronized List.
     *
     * @param targets targets the target objects
     * @return a factory that will proxy instances of the supplied type.
     * @since 1.1
     * @see {@link #proxy(Object...)}
     */
    public static MulticastingBuild<Multicast> proxyList(List<Object> targets) {
        return new MulticastingBuild<Multicast>(targets);
    }


    public static class MulticastingWith<T> {
        Multicasting<T> multicasting;

        private MulticastingWith(Class<T> primaryType, Class<?>[] types) {
            multicasting = new Multicasting<T>(primaryType, types);
        }

        /**
         * With these target Objects
         * @param targets targets the target objects
         * @return the factory
         * @since 1.0
         */
        public MulticastingBuild<T> with(Object... targets) {
            return withList(Arrays.asList(targets));
        }

        public MulticastingBuild<T> withList(List<?> targets) {
            multicasting.delegates = targets;
            return new MulticastingBuild<T>(multicasting);
        }
    }

    public static class MulticastingBuild<T> {
        private final Multicasting<T> multicasting;

        private MulticastingBuild(List<Object> targets) {
            multicasting = new Multicasting<T>(targets);
        }

        private MulticastingBuild(Multicasting<T> multicasting) {
            this.multicasting = multicasting;
        }

        /**
         * @return the proxy using StandardProxyFactory
         * @since 1.0
         */
        public T build() {
            return multicasting.build();
        }

        /**
         * Generate a proxy for the specified types calling the methods on the given targets using the {@link StandardProxyFactory}.
         * <p>
         * Note, that the method will only return a proxy if necessary. If there is only one target instance and this
         * instance implements all of the specified types, then there is no point in creating a proxy.
         * </p>
         *
         * @param factory the factory used to generate the proxy
         * @return the new proxy implementing {@link Multicast} or the only target
         * @since 1.0
         */
        public T build(ProxyFactory factory) {
            return multicasting.build(factory);
        }
    }

    private T build() {
        return build(new StandardProxyFactory());
    }

    private T build(ProxyFactory factory) {
        if (types == null) {
            return buildWithNoTypesInput(factory);
        }
        return new MulticastingInvoker<T>(types, factory, delegates).proxy();
    }

    private T buildWithNoTypesInput(ProxyFactory factory) {
        if (delegates.size() > 1) {
            Object[] delegateArray = delegates.toArray();
            final Class<?> superclass = ReflectionUtils.getMostCommonSuperclass(delegateArray);
            final Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(delegateArray);
            ReflectionUtils.addIfClassProxyingSupportedAndNotObject(superclass, interfaces, factory);
            this.types = interfaces.toArray(new Class<?>[interfaces.size()]);
            return new MulticastingInvoker<T>(types, factory, delegates).proxy();
        }
        @SuppressWarnings("unchecked")
        final T instance = (T) delegates.get(0);
        return instance;
    }
}
