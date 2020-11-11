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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Performs two-way conversion of a specific Java target type to its
 * {@linkplain Message Protobuf Message} counterpart and back.
 *
 * <p>The inheritors implement the actual conversion to a specific Message and Object.
 *
 * @param <M>
 *         Protobuf Message to convert
 * @param <T>
 *         target conversion type
 */
abstract class ProtoConverter<M extends Message, T> extends Converter<M, T> {

    /**
     * Returns a converter for the specified {@code type}.
     *
     * <p>If a dedicated converter is not available returns {@link PrimitiveConverter}.
     */
    static <M extends Message, T> Converter<M, T> forType(Class<T> type) {
        checkNotNull(type);
        ProtoConverter<?, ?> converter;
        if (Message.class.isAssignableFrom(type)) {
            converter = new MessageConverter();
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
}
