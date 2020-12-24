/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Performs conversion of a {@linkplain Message Protobuf Message} to its Java counterpart and back.
 *
 * <p>The inheritors implement the actual conversion to a specific Message and Object.
 *
 * @param <M>
 *         the type of a message to convert
 * @param <T>
 *         target conversion type
 */
abstract class ProtoConverter<M extends Message, T> extends Converter<M, T> {

    /**
     * Returns a converter for the specified {@code type}.
     *
     * <p>Protobuf {@linkplain Message messages} are returned {@linkplain AsIs as is}.
     *
     * <p>{@link ByteString} instances are {@linkplain BytesConverter converted} to
     * {@link com.google.protobuf.BytesValue BytesValue}.
     *
     * <p>{@linkplain ProtocolMessageEnum Protobuf enums} are converted using a dedicated
     * {@link EnumConverter} which handles conversions by name or by number.
     *
     * <p>All other types are considered primitives and are {@linkplain PrimitiveConverter handled}
     * respectively.
     */
    static <M extends Message, T> Converter<M, T> forType(Class<T> type) {
        checkNotNull(type);
        ProtoConverter<?, ?> converter;
        if (Message.class.isAssignableFrom(type)) {
            converter = new AsIs();
        } else if (ByteString.class.isAssignableFrom(type)) {
            converter = new BytesConverter();
        } else if (isProtoEnum(type)) {
            converter = new EnumConverter(asProtoEnum(type));
        } else {
            converter = new PrimitiveConverter<>();
        }
        @SuppressWarnings("unchecked") // Logically checked.
        Converter<M, T> result = (Converter<M, T>) converter;
        return result;
    }

    private static <T> boolean isProtoEnum(Class<T> type) {
        return Enum.class.isAssignableFrom(type)
                && ProtocolMessageEnum.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked") // Checked at runtime.
    private static <T> Class<? extends Enum<? extends ProtocolMessageEnum>>
    asProtoEnum(Class<T> type) {
        return (Class<? extends Enum<? extends ProtocolMessageEnum>>) type;
    }

    @Override
    protected final T doForward(M input) {
        checkNotNull(input);
        return toObject(input);
    }

    @Override
    protected final M doBackward(T t) {
        checkNotNull(t);
        return toMessage(t);
    }

    /**
     * Converts supplied {@code input} message into a typed object.
     */
    protected abstract T toObject(M input);

    /**
     * Converts supplied {@code input} object into a Protobuf message.
     */
    protected abstract M toMessage(T input);

    /**
     * Returns the supplied {@code input} {@link Message} as is.
     */
    private static final class AsIs extends ProtoConverter<Message, Message> {

        @Override
        protected Message toObject(Message input) {
            return input;
        }

        @Override
        protected Message toMessage(Message input) {
            return input;
        }
    }
}
