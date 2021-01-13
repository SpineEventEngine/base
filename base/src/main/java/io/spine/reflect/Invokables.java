/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.reflect;

import com.google.common.reflect.Invokable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * Utilities which streamline the usage of Java {@linkplain java.lang.reflect.Method methods} and
 * an instantiation of objects via reflectively-obtained {@linkplain Constructor constructors}.
 */
public final class Invokables {

    private static final MethodHandles.Lookup publicLookup = publicLookup();

    /** Prevents instantiation of this utility class. */
    private Invokables() {
    }

    /**
     * Converts the given {@link Method} into a {@link MethodHandle}.
     *
     * <p>The accessibility parameter of the input method, i.e. {@code method.isAccessible()}, is
     * preserved by this method. However, the attributes may be changes in a non-synchronized
     * manner, i.e. the {@code asHandle(..)} is not designed to operate concurrently.
     */
    public static MethodHandle asHandle(Method method) {
        checkNotNull(method);
        MethodHandle result = invokePreservingAccessibility(
                method,
                Invokable::from,
                publicLookup::unreflect,
                () -> format(
                        "Unable to obtain method handle for `%s`." +
                                " The method's accessibility was probably changed concurrently.",
                        method));
        return result;
    }

    /**
     * Attempts to create an instance of the specified type using a constructor without parameters.
     *
     * <p>If no such constructor exists, an {@code IllegalArgumentException} is thrown.
     *
     * <p>The access level does not matter: the constructor is made accessible during the method
     * execution. It is always restored after object instantiation or an error.
     *
     * @param type
     *         class to instantiate
     * @return the object created using a parameterless constructor
     * @throws IllegalStateException
     *         if the class is abstract, or an exception is thrown in the
     *         parameterless constructor
     * @throws IllegalArgumentException
     *         if the specified class does not have a parameterless
     *         constructors. Note that nested classes fall under this case
     */

    public static <C> C callParameterlessCtor(Class<C> type) {
        checkNotNull(type);
        Constructor<C> ctor = ensureParameterlessCtor(type);
        C result = invokePreservingAccessibility(
                ctor,
                Invokable::from,
                Constructor::newInstance,
                () -> format(
                        "Could not instantiate the type `%s` using " +
                                "a parameterless constructor.",
                        type.getSimpleName()));
        return result;
    }

    /**
     * Invokes the given argumentless method on the target ignoring the accessibility restrictions.
     *
     * <p>The target must be of the type that declares the given method, otherwise an
     * {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException
     *         if the target is not of the type that declares the given method or
     *         if an exception is thrown during the method invocation
     */
    public static Object setAccessibleAndInvoke(Method method, Object target) {
        checkNotNull(method);
        checkNotNull(target);

        Object result = invokePreservingAccessibility(
                method,
                Invokable::from,
                m -> m.invoke(target),
                () -> format(
                        "Method `%s` invocation on target `%s` of class `%s` failed.",
                        method.getName(), target,
                        target.getClass()
                              .getCanonicalName()));
        return result;
    }

    /**
     * Tries to find a constructor with no arguments in the specified class or its parents.
     *
     * <p>If no such constructor has been found, throws an {@code IllegalArgumentException}.
     *
     * @param type
     *         class to look for constructors in
     * @return a constructor with no parameters, if it exists
     * @throws IllegalArgumentException
     *         if the specified class does not declare a parameterless constructor
     */
    @CanIgnoreReturnValue
    private static <C> Constructor<C> ensureParameterlessCtor(Class<C> type) {
        checkNotNull(type);
        @SuppressWarnings("unchecked" /* safe, as `Class<C>` only declares `Constructor<C>`. */)
        Constructor<C>[] ctors = (Constructor<C>[]) type.getDeclaredConstructors();
        for (Constructor<C> ctor : ctors) {
            if (ctor.getParameterCount() == 0) {
                return ctor;
            }
        }

        throw newIllegalArgumentException("No parameterless ctor found in class `%s`.",
                                          type.getSimpleName());
    }

    /**
     * Performs a reflective operation regardless of its accessibility, returns its result.
     *
     * <p>Upon completion, whether successful or erroneous, returns the accessibility to its
     * initial state.
     *
     * @param reflectiveObject
     *         an object that can be used to make an {@code Invokable}
     * @param makeInvokable
     *         a function of {@code P} -> {@code Invokable}. {@code Invokable} is
     *         needed to manipulate the accessibility in a generic manner
     * @param fn
     *         a reflective function to perform
     * @param onError
     *         a supplier of the error message to include into the {@code
     *         IllegalStateException} should an error be thrown
     * @param <T>
     *         a type of reflection-related object to perform a function on
     * @param <R>
     *         a type of result of the reflective function
     * @return a result of the reflective function
     * @throws IllegalStateException
     *         if a {@code ReflectiveOperationException} is thrown
     *         by {@code fn}, or other error occurs during the reflective operation execution
     */
    @SuppressWarnings("OverlyBroadCatchBlock" /* catching any runtimes does not hurt here. */)
    private static <T, R> R invokePreservingAccessibility(T reflectiveObject,
                                                          Function<T, Invokable<?, ?>> makeInvokable,
                                                          ReflectiveFunction<T, R> fn,
                                                          Supplier<String> onError) {
        Invokable<?, ?> invokable = makeInvokable.apply(reflectiveObject);
        boolean accessible = invokable.isAccessible();
        try {
            invokable.setAccessible(true);
            R result = fn.apply(reflectiveObject);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            String message = onError.get();
            throw newIllegalStateException(e, message);
        } finally {
            invokable.setAccessible(accessible);
        }
    }

    /**
     * A function that may throw a reflection-related error on invocation.
     *
     * @param <T>
     *         function input type
     * @param <R>
     *         function output type
     */
    private interface ReflectiveFunction<T, R> {

        R apply(T t) throws ReflectiveOperationException;
    }
}
