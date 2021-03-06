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

package io.spine.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import io.spine.type.KnownTypes;
import io.spine.type.UnknownTypeException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.protobuf.util.JsonFormat.Parser;
import static com.google.protobuf.util.JsonFormat.Printer;
import static com.google.protobuf.util.JsonFormat.parser;
import static com.google.protobuf.util.JsonFormat.printer;
import static io.spine.protobuf.Messages.builderFor;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utilities for working with JSON.
 *
 * <p>Both {@linkplain #toJson(Message) parsing} and {@linkplain #fromJson(String, Class) printing}
 * functionality acknowledges presence of the custom Protobuf message types relying on
 * the {@link KnownTypes} for this.
 *
 * <p>The parsing functionality follows the default Protobuf ignorance strategy for unknown fields,
 * i.e. the unknown fields are {@linkplain Parser#ignoringUnknownFields() ignored} when a JSON
 * string is parsed.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#unknowns">
 *         Protobuf Unknown Fields</a>
 */
public final class Json {

    /** The type registry for all Proto types known in this class path. */
    private static final TypeRegistry typeRegistry = KnownTypes.instance().typeRegistry();

    /** The printer for JSON output which knows about the types. */
    private static final Printer printer = printer().usingTypeRegistry(typeRegistry);

    /** The compact version of the printer. */
    private static final Printer compactPrinter = printer.omittingInsignificantWhitespace();

    /** The parser which knows about all types. */
    private static final Parser parser =
            parser().ignoringUnknownFields().usingTypeRegistry(typeRegistry);

    /** Prevents instantiation of this utility class. */
    private Json() {
    }

    /**
     * Converts passed message into Json representation.
     *
     * @param message
     *         the message object
     * @return JSON string
     */
    public static String toJson(Message message) {
        checkNotNull(message);
        String result = toJson(message, printer);
        return result;
    }

    /**
     * Converts the passed message into compact JSON representation.
     *
     * <p>The resulted JSON does not contain the line separators.
     *
     * @param message
     *         the {@code Message} object
     * @return the converted message to JSON
     */
    public static String toCompactJson(Message message) {
        checkNotNull(message);
        String result = toJson(message, compactPrinter);
        return result;
    }

    private static String toJson(Message message, Printer printer) {
        String result;
        try {
            result = printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            Throwable rootCause = getRootCause(e);
            throw new UnknownTypeException(rootCause);
        }
        checkState(result != null);
        return result;
    }

    @SuppressWarnings("unchecked") // It is OK as the builder is obtained by the specified class.
    public static <T extends Message> T fromJson(String json, Class<T> messageClass) {
        checkNotNull(json);
        checkNotNull(messageClass);
        try {
            Message.Builder messageBuilder = builderFor(messageClass);
            parser.merge(json, messageBuilder);
            T result = (T) messageBuilder.build();
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw newIllegalArgumentException(
                    e,
                    "The JSON text (`%s`) cannot be parsed to an instance of the class `%s`.",
                    json, messageClass.getName()
            );
        }
    }

    @VisibleForTesting
    static TypeRegistry typeRegistry() {
        return typeRegistry;
    }
}
