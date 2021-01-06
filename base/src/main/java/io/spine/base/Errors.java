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

package io.spine.base;

import io.spine.validate.ValidationException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.base.Throwables.getStackTraceAsString;

/**
 * Utility class for working with {@link Error}s.
 */
public final class Errors {

    /** Prevents instantiation of this utility class. */
    private Errors() {
    }

    /**
     * Creates new instance of {@link Error} by the passed {@code Throwable}.
     */
    public static Error fromThrowable(Throwable throwable) {
        Error.Builder result = toErrorBuilder(throwable);
        return result.build();
    }

    /**
     * Creates an instance by the root cause of the passed {@link Throwable}.
     *
     * @param throwable
     *         the {@code Throwable} to convert
     * @return new instance of {@link Error}
     */
    public static Error causeOf(Throwable throwable) {
        Error.Builder error = toBuilderCauseOf(throwable);
        return error.build();
    }

    /**
     * Creates an instance by the root cause of the given {@link Throwable} with
     * the given error code.
     *
     * <p>The error code may represent a number in an enum or a native error number.
     *
     * @param throwable
     *         the {@code Throwable} to convert
     * @param errorCode
     *         the error code to include in the resulting {@link Error}
     * @return new instance of {@link Error}
     * @see #causeOf(Throwable) as the recommended overload
     */
    public static Error causeOf(Throwable throwable, int errorCode) {
        Error.Builder error = toBuilderCauseOf(throwable).setCode(errorCode);
        return error.build();
    }

    private static Error.Builder toBuilderCauseOf(Throwable throwable) {
        return toErrorBuilder(getRootCause(throwable));
    }

    /**
     * Converts the given {@code Throwable} into an {@link Error} builder.
     *
     * <p>The class FQN of the {@code Throwable} becomes the {@code Error.type}.
     *
     * <p>The message of the {@code Throwable} becomes the {@code Error.message}.
     *
     * <p>The {@code Error.stacktrace} is populated by dumping the stacktrace of
     * the {@code Throwable} into a string.
     *
     * <p>If the {@code Throwable} is a {@link ValidationException},
     * the {@code Error.validation_error} is populated from the validation exception.
     *
     * @param throwable
     *         the {@code Throwable} to convert
     * @return new builder of {@link Error}
     */
    @SuppressWarnings("CheckReturnValue") // calling builder
    private static Error.Builder toErrorBuilder(Throwable throwable) {
        checkNotNull(throwable);
        String type = throwable.getClass()
                               .getCanonicalName();
        String message = nullToEmpty(throwable.getMessage());
        String stacktrace = getStackTraceAsString(throwable);
        Error.Builder result = Error
                .newBuilder()
                .setType(type)
                .setMessage(message)
                .setStacktrace(stacktrace);
        if (throwable instanceof ValidationException) {
            ValidationException validationException = (ValidationException) throwable;
            result.setValidationError(validationException.asValidationError());
        }
        return result;
    }
}
