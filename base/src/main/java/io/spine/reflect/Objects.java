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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A utility class for working reflectively with objects.
 */
public final class Objects {

    private Objects() {
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
    public static <C> C instantiateWithoutParameters(Class<C> type) {
        checkNotNull(type);
        Constructor<C> ctor = Constructors.ensureParameterlessCtor(type);
        boolean accessible = ctor.isAccessible();
        try {
            ctor.setAccessible(true);
            C result = ctor.newInstance();
            return result;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            String simpleName = type.getSimpleName();
            throw newIllegalStateException(e,
                                           "Could not instantiate the type `%s` using " +
                                                   "a parameterless constructor.",
                                           simpleName);
        } finally {
            ctor.setAccessible(accessible);
        }
    }
}
