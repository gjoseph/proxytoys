/*
 * (c) 2003-2005, 2009, 2010 ThoughtWorks Ltd
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 24-Mar-2004
 */
package com.thoughtworks.proxy.toys.nullobject;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;


/**
 * Toy factory to create proxies acting as Null Objects.
 *
 * @author Dan North
 * @author Aslak Helles&oslash;y
 * @author Juan Li
 * @author J&ouml;rg Schaible
 * @see com.thoughtworks.proxy.toys.nullobject
 * @since 0.1
 */
public class Null<T> {

    /**
     * The Null {@link Object}.
     * @since 0.1
     */
    public static final Object NULL_OBJECT = new Object();

    /**
     * Immutable Null Object implementation of {@link SortedMap}.
     * @since 0.1
     */
    public static final SortedMap<Object, Object> NULL_SORTED_MAP = new TreeMap<Object, Object>() {
        private static final long serialVersionUID = -4388170961744587609L;

        @Override
        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            // nothing to do
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public Set<Object> keySet() {
            return Collections.emptySet();
        }

        @Override
        public Collection<Object> values() {
            return Collections.emptyList();
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return Collections.emptySet();
        }
    };

    /**
     * Immutable Null Object implementation of {@link SortedSet}.
     * @since 0.1
     */
    public static final SortedSet<Object> NULL_SORTED_SET = new TreeSet<Object>() {
        private static final long serialVersionUID = 809722154285517876L;

        @Override
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            // nothing to do
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }
    };
    private Class<T> type;

    private Null(Class<T> type) {
        this.type = type;
    }


    /**
     * Creates a factory for proxy instances that is nullable.
     *
     * @param type the type implemented by the proxy
     * @return the factory
     * @since 1.0
     */
    public static <T> NullBuild<T> proxy(Class<T> type) {
        return new NullBuild<T>(new Null<T>(type));
    }

    public static class NullBuild<T> {

        private final Null<T> nullObject;

        private NullBuild(Null<T> nullObject) {
            this.nullObject = nullObject;
        }

        /**
         * Generate a Null Object proxy for a specific type using the{@link StandardProxyFactory}.
         * <p>
         * Note that the method will only return a proxy if it cannot handle the type itself or <code>null</code> if the
         * type cannot be proxied.
         * </p>
         *
         * @return object, proxy or <code>null</code>
         * @see com.thoughtworks.proxy.toys.nullobject
         * @since 1.0
         */
        public T build() {
            return nullObject.build(new StandardProxyFactory());
        }

        /**
         * Generate a Null Object proxy for a specific type using a special {@link ProxyFactory}.
         * <p>
         * Note that the method will only return a proxy if it cannot handle the type itself or <code>null</code> if the
         * type cannot be proxied.
         * </p>
         *
         * @param factory the {@link ProxyFactory} in use
         * @return object, proxy or <code>null</code>
         * @see com.thoughtworks.proxy.toys.nullobject
         */
        public T build(ProxyFactory factory) {
            return nullObject.build(factory);
        }
        
    }
    
    private T build(ProxyFactory proxyFactory) {
        final Object result;

        // Primitives
        if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            result = Boolean.FALSE;
        } else if (byte.class.equals(type) || Byte.class.equals(type)) {
            result = (byte) 0;
        } else if (char.class.equals(type) || Character.class.equals(type)) {
            result = (char) 0;
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            result = 0;
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            result = (long) 0;
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            result = 0.0f;
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            result = 0.0;
        }

        // String
        else if (String.class.equals(type)) {
            result = "";
        }

        // Object
        else if (Object.class.equals(type)) {
            result = NULL_OBJECT;
        }

        // Arrays
        else if (type.isArray()) {
            result = Array.newInstance(type.getComponentType(), 0);
        }

        // Collections
        else if (Set.class == type) {
            result = Collections.EMPTY_SET;
        } else if (Map.class == type) {
            result = Collections.EMPTY_MAP;
        } else if (List.class == type) {
            result = Collections.EMPTY_LIST;
        } else if (SortedSet.class == type) {
            result = NULL_SORTED_SET;
        } else if (SortedMap.class == type) {
            result = NULL_SORTED_MAP;
        } else if (proxyFactory.canProxy(type)) {
            result = proxyFactory.createProxy(new NullInvoker(type, proxyFactory), type);
        } else {
            result = null;
        }
        @SuppressWarnings("unchecked")
        T typedResult = (T) result;
        return typedResult;
    }


    /**
     * Determine whether an object was created by {@link Null#proxy(Class)}.
     *
     * @param object the object to examine
     * @return <code>true</code> if the object is a Null proxy.
     * @since 0.1
     */
    public static boolean isNullObject(final Object object) {
        return isNullObject(object, new StandardProxyFactory());
    }

    /**
     * Determine whether an object was created by {@link Null#proxy(Class)} using a special ProxyFactory with the builder.
     *
     * @param object       the object to examine
     * @param proxyFactory the {@link ProxyFactory} to use
     * @return <code>true</code> if the object is a Null proxy.
     * @since 0.1
     */
    public static boolean isNullObject(final Object object, final ProxyFactory proxyFactory) {
        return isStandardNullObject(object) || isNullProxyObject(object, proxyFactory);
    }

    private static boolean isStandardNullObject(Object object) {
        return object == Collections.EMPTY_LIST
                || object == Collections.EMPTY_SET
                || object == Collections.EMPTY_MAP
                || object == NULL_SORTED_SET
                || object == NULL_SORTED_MAP
                || object == NULL_OBJECT;
    }

    private static boolean isNullProxyObject(final Object object, final ProxyFactory proxyFactory) {
        return proxyFactory.isProxyClass(object.getClass()) && proxyFactory.getInvoker(object) instanceof NullInvoker;
    }
}