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

package io.spine.type;

import static java.lang.String.format;

/**
 * Exception thrown when the content of {@link com.google.protobuf.Any Any} does not
 * match one we expect when unpacking.
 *
 * <p>Typically this exception wraps
 * {@link com.google.protobuf.InvalidProtocolBufferException InvalidProtocolBufferException} thrown
 * in unsuccessful call of {@link com.google.protobuf.Any#unpack(Class) Any.unpack(Class)}.
 *
 * <p>Another usage scenario is a mismatch between
 * the {@linkplain com.google.protobuf.Descriptors.Descriptor#getFullName() descriptor full names}
 * of the instance wrapped by {@code Any} and the target message.
 */
public class UnexpectedTypeException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    /**
     * Creates an instance of {@code UnexpectedTypeException} by wrapping the root cause.
     */
    public UnexpectedTypeException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance {@code UnexpectedTypeException} with the expected and actual type names.
     */
    public UnexpectedTypeException(String expectedTypeName, String actualTypeName) {
        super(formatMsg(expectedTypeName, actualTypeName));
    }

    private static String formatMsg(String expectedTypeName, String actualTypeName) {
        return format(
                "Cannot unpack `Any` instance. Expected type name is `%s`, actual is `%s`.",
                expectedTypeName, actualTypeName);
    }
}
