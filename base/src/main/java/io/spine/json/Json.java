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

package io.spine.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.spine.type.KnownTypes;
import io.spine.type.UnknownTypeException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.protobuf.util.JsonFormat.Parser;
import static com.google.protobuf.util.JsonFormat.Printer;
import static com.google.protobuf.util.JsonFormat.TypeRegistry;
import static com.google.protobuf.util.JsonFormat.parser;
import static com.google.protobuf.util.JsonFormat.printer;
import static io.spine.protobuf.Messages.builderFor;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utilities for working with JSON.
 */
public final class Json {

    private static final TypeRegistry typeRegistry = KnownTypes.instance()
                                                               .typeRegistry();
    private static final Printer PRINTER = printer().usingTypeRegistry(typeRegistry);
    private static final Printer COMPACT_PRINTER = PRINTER.omittingInsignificantWhitespace();
    private static final Parser PARSER = parser().usingTypeRegistry(typeRegistry);

    /**
     * Prevents the utility class instantiation.
     */
    private Json() {
    }

    /**
     * Converts passed message into Json representation.
     *
     * @param message the message object
     * @return JSON string
     */
    public static String toJson(Message message) {
        String result = toJson(message, PRINTER);
        return result;
    }

    /**
     * Converts the passed message into compact JSON representation.
     *
     * <p>The resulted JSON does not contain the line separators.
     *
     * @param message the {@code Message} object
     * @return the converted message to JSON
     */
    public static String toCompactJson(Message message) {
        String result = toJson(message, COMPACT_PRINTER);
        return result;
    }

    private static String toJson(Message message, Printer printer) {
        checkNotNull(message);
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
        try {
            Message.Builder messageBuilder = builderFor(messageClass);
            PARSER.merge(json, messageBuilder);
            T result = (T) messageBuilder.build();
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw newIllegalArgumentException(e,
                                              "%s cannot be parsed to the %s class.",
                                              json, messageClass);
        }
    }

    @VisibleForTesting
    static TypeRegistry typeRegistry() {
        return typeRegistry;
    }
}
