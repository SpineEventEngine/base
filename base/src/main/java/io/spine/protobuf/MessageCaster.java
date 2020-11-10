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
import com.google.common.base.Function;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@link Function} performing the described type conversion.
 *
 * @param <M>
 *         type of message to convert
 * @param <T>
 *         target conversion type
 */
abstract class MessageCaster<M extends Message, T> extends Converter<M, T> {

    /**
     * Returns a caster for the specified {@code type}.
     */
    static <M extends Message, T> MessageCaster<M, T> forType(Class<T> type) {
        checkNotNull(type);
        MessageCaster<?, ?> caster;
        if (Message.class.isAssignableFrom(type)) {
            caster = new MessageTypeCaster();
        } else if (ByteString.class.isAssignableFrom(type)) {
            caster = new BytesCaster();
        } else if (isProtoEnum(type)) {
            caster = new EnumCaster(asProtoEnum(type));
        } else {
            caster = new PrimitiveTypeCaster<>();
        }
        @SuppressWarnings("unchecked") // Logically checked.
        MessageCaster<M, T> result = (MessageCaster<M, T>) caster;
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
