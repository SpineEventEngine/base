/*
 * Copyright 2022, TeamDev. All rights reserved.
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
package io.spine.type;

import java.io.Serial;

import static java.lang.String.format;

/**
 * Exception that is thrown when an unsupported message is obtained
 * or in case when there is no class for the given Protobuf message.
 */
public class UnknownTypeException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Creates a new instance with the type name.
     *
     * @param typeName the unknown type
     */
    public UnknownTypeException(String typeName) {
        super(makeMsg(typeName));
    }

    private static String makeMsg(String typeName) {
        return format("No Java class found for the Protobuf message of type: `%s`.", typeName);
    }

    /**
     * Creates a new instance with the type name and the cause.
     *
     * @param typeName the unknown type
     * @param cause    the exception cause
     */
    public UnknownTypeException(String typeName, Throwable cause) {
        super(makeMsg(typeName), cause);
    }

    /**
     * Creates a new instance when only the cause is known.
     *
     * <p>Use this constructor when propagating
     * {@link com.google.protobuf.InvalidProtocolBufferException InvalidProtocolBufferException}
     * without knowing which type caused the exception
     * (e.g., when calling {@code JsonFormat.print()}).
     */
    public UnknownTypeException(Throwable cause) {
        super(cause);
    }
}
