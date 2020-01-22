/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * A utility for working with Java {@linkplain Method methods}.
 */
public final class Methods {

    private static final Lookup publicLookup = publicLookup();

    /** Prevents instantiation of this utility class. */
    private Methods() {
    }

    /**
     * Invokes the given argumentless method on the target ignoring the accessibility restrictions.
     *
     * <p>The target must be of the type that declares the given method, otherwise an
     * {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     *         if the target is not of the type that declares the given method
     * @throws IllegalStateException
     *         if an exception is thrown during the method invocation
     */
    public static Object setAccessibleAndInvoke(Method method, Object target) {
        checkNotNull(method);
        boolean accessible = method.isAccessible();
        try {
            method.setAccessible(true);
            Object result = method.invoke(target);
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw newIllegalStateException(
                    e,
                    "Method `%s` invocation on target `%s` of class `%s` failed.",
                    method.getName(), target, target.getClass()
                                                    .getCanonicalName());
        } finally {
            method.setAccessible(accessible);
        }
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
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        MethodHandle handle;
        try {
            handle = publicLookup.unreflect(method);
        } catch (IllegalAccessException exception) {
            throw newIllegalArgumentException(exception,
                                              "Unable to obtain method handle for `%s`.",
                                              method);
        } finally {
            if (!accessible) {
                method.setAccessible(false);
            }
        }
        return handle;
    }
}
