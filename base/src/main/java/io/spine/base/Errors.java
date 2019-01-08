/*
 * Copyright 2019, TeamDev. All rights reserved.
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

    private static Error.Builder toErrorBuilder(Throwable throwable) {
        checkNotNull(throwable);
        String type = throwable.getClass()
                               .getName();
        String message = nullToEmpty(throwable.getMessage());
        String stacktrace = getStackTraceAsString(throwable);
        return Error.newBuilder()
                    .setType(type)
                    .setMessage(message)
                    .setStacktrace(stacktrace);
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
     * @param throwable the {@code Throwable} to convert
     * @return new instance of {@link Error}
     */
    public static Error causeOf(Throwable throwable) {
        Error.Builder error = toBuilderCauseOf(throwable);
        return error.build();
    }

    private static Error.Builder toBuilderCauseOf(Throwable throwable) {
        return toErrorBuilder(getRootCause(throwable));
    }

    /**
     * Creates an instance by the root cause of the given {@link Throwable} with
     * the given error code.
     *
     * <p>The error code may represent a number in an enum or a native error number
     *
     * @param throwable the {@code Throwable} to convert
     * @param errorCode the error code to include in the resulting {@link Error}
     * @return new instance of {@link Error}
     * @see #causeOf(Throwable) as the recommended overload
     */
    public static Error causeOf(Throwable throwable, int errorCode) {
        Error.Builder error = toBuilderCauseOf(throwable).setCode(errorCode);
        return error.build();
    }
}
