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

package io.spine.json;

import com.google.errorprone.annotations.InlineMe;
import com.google.protobuf.Message;

/**
 * Utilities for working with JSON.
 *
 * @deprecated please use {@link io.spine.type.Json}
 */
@Deprecated
public final class Json {

    /** Prevents instantiation of this utility class. */
    private Json() {
    }

    /**
     * Converts passed message into Json representation.
     *
     * @param message
     *         the message object
     * @return JSON string
     * @deprecated
     *  Please use {@link io.spine.type.Json#toJson(com.google.protobuf.MessageOrBuilder)}.
     */
    @Deprecated
    @InlineMe(replacement = "io.spine.type.Json.toJson(message)")
    public static String toJson(Message message) {
        return io.spine.type.Json.toJson(message);
    }

    /**
     * Converts the passed message into compact JSON representation.
     *
     * <p>The resulted JSON does not contain the line separators.
     *
     * @param message
     *         the {@code Message} object
     * @return the converted message to JSON
     * @deprecated
     *  Please use {@link io.spine.type.Json#toCompactJson(com.google.protobuf.MessageOrBuilder)}.
     */
    @Deprecated
    @InlineMe(replacement = "io.spine.type.Json.toCompactJson(message)")
    public static String toCompactJson(Message message) {
        return io.spine.type.Json.toCompactJson(message);
    }

    /**
     * Parses a message from the given JSON string.
     *
     * @deprecated please use {@link io.spine.type.Json#fromJson(Class, String)}.
     */
    @Deprecated
    @InlineMe(replacement = "io.spine.type.Json.fromJson(messageClass, json)")
    public static <T extends Message> T fromJson(String json, Class<T> messageClass) {
        return io.spine.type.Json.fromJson(messageClass, json);
    }
}
