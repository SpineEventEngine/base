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

package io.spine.base;

import com.google.common.reflect.Invokable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A utility class for working with and verifying {@link EnvironmentType} extenders.
 */
final class EnvironmentTypes {

    private EnvironmentTypes() {
    }

    /**
     * Tries to instantiate the specified environment type.
     *
     * <p>It can only be instantiated if it has a parameterless constructor, an {@code
     * IllegalArgumentException} is thrown otherwise.
     *
     * @param type
     *         env type to instantiate
     * @return a new {@code EnvironmentType} instance
     */
    static EnvironmentType instantiate(Class<? extends EnvironmentType> type) {
        checkNotNull(type);
        Constructor<? extends EnvironmentType> ctor = ensureParameterlessCtor(type);
        Invokable<? extends EnvironmentType, ? extends EnvironmentType> invokable =
                Invokable.from(ctor);
        boolean isAccessible = invokable.isAccessible();
        try {
            EnvironmentType result = isAccessible
                                     ? ctor.newInstance()
                                     : instantiatePreservingAccessibility(ctor);
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            invokable.setAccessible(isAccessible);
            String message = "Could not instantiate `%s`.";
            throw newIllegalArgumentException(e, message, type.getSimpleName());
        }
    }

    /**
     * If the specified type has a constructor with 0 arguments, returns the type.
     *
     * <p>Otherwise, throws an {@code IllegalArgumentException}.
     *
     * @param type
     *         type to check
     * @return the specified instance, if it has a parameterless constructor
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked" /*
                                   * Casting from `Constructor<?>` to
                                   * `Constructor<? extends EnvironmentType)` is safe here, as
                                   * we extract this constructor from a
                                   * `Class<? extends EnvironmentType`.
                                   */)
    static Constructor<? extends EnvironmentType>
    ensureParameterlessCtor(Class<? extends EnvironmentType> type) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return (Constructor<? extends EnvironmentType>) constructor;
            }
        }

        throw newIllegalArgumentException("No parameterless ctor found in class `%s`.",
                                          type.getSimpleName());
    }

    private static EnvironmentType
    instantiatePreservingAccessibility(Constructor<? extends EnvironmentType> ctor)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        boolean accessible = ctor.isAccessible();
        ctor.setAccessible(true);
        EnvironmentType result = ctor.newInstance();
        ctor.setAccessible(accessible);
        return result;
    }
}
