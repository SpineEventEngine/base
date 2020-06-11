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

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.lang.reflect.Constructor;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A utility for working with Java {@linkplain java.lang.reflect.Constructor constructors}.
 */
public final class Constructors {

    /** Prevents instantiation of this utility class. */
    private Constructors() {
    }

    /**
     * Tries to find a constructor with no arguments in the specified class or its parents.
     *
     * <p>If no such constructor has been found, throws an {@code IllegalArgumentException}.
     *
     * @param type
     *         class to look for constructors in
     * @return a constructor with no parameters, if it exists
     */
    @CanIgnoreReturnValue
    public static <C> Constructor<C> ensureParameterlessCtor(Class<C> type) {
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
}
