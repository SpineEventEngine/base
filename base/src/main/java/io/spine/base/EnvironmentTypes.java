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

import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;

/**
 * A utility class for working with and verifying {@link EnvironmentType} extenders.
 */
final class EnvironmentTypes {

    private EnvironmentTypes() {
    }

    /**
     * Checks whether the specified environment type may be registered using it's class.
     *
     * <p>To register the type by its class it must have a package-private parameterless
     * constructor.
     *
     * @param type
     *         environment to register
     */
    @CanIgnoreReturnValue
    static <C extends Class<? extends EnvironmentType>> C checkCanRegisterByClass(C type) {
        Constructor<? extends EnvironmentType> parameterlessCtor = checkHasParameterlessCtor(type);
        checkCtorAccessLevel(parameterlessCtor);
        return type;
    }

    private static void
    checkCtorAccessLevel(Constructor<? extends EnvironmentType> constructor) {
        Invokable<? extends EnvironmentType, ? extends EnvironmentType> ctor =
                Invokable.from(constructor);

        if (!ctor.isPackagePrivate()) {
            Class<? extends EnvironmentType> envType = constructor.getDeclaringClass();
            StringBuilder message = new StringBuilder();
            message.append(format(
                    "`%s` constructor must be package-private to be registered in `Environment`.",
                    envType.getSimpleName()));
            if (ctor.isPublic()) {
                message.append(format(
                        " As `%s` has a public constructor, you may use `Environment.register(envInstance)`.",
                        envType.getSimpleName()));
            }
            throw newIllegalArgumentException(message.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<? extends EnvironmentType>
    checkHasParameterlessCtor(Class<? extends EnvironmentType> type) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return (Constructor<? extends EnvironmentType>) constructor;
            }
        }

        throw newIllegalArgumentException("To register `%s` by class, it must " +
                                                  "have a parameterless package-private constructor.",
                                          type.getSimpleName());
    }
}
