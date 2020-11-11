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
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Casts the primitive and built-in types to the corresponding {@link Message}s and back.
 *
 * @param <M>
 *         the type of the message
 * @param <T>
 *         the type to cast to and from the message
 * @implNote The arguments are checked during the conversion and an
 *         {@link IllegalArgumentException} is thrown in case of mismatch. The type name used in
 *         the error message is a simple {@link Class#getName() Class.getName()} call result.
 *         It's the best-performant solution among options, such as
 *         {@link Class#getCanonicalName() Class.getCanonicalName()}.
 */
final class PrimitiveTypeCaster<M extends Message, T> extends ProtoConverter<M, T> {

    private static final ImmutableMap<Class<?>, Converter<? extends Message, ?>>
            PROTO_WRAPPER_TO_HANDLER =
            ImmutableMap.<Class<?>, Converter<? extends Message, ?>>builder()
                    .put(Int32Value.class, new Int32Handler())
                    .put(Int64Value.class, new Int64Handler())
                    .put(UInt32Value.class, new UInt32Handler())
                    .put(UInt64Value.class, new UInt64Handler())
                    .put(FloatValue.class, new FloatHandler())
                    .put(DoubleValue.class, new DoubleHandler())
                    .put(BoolValue.class, new BoolHandler())
                    .put(StringValue.class, new StringHandler())
                    .build();
    private static final ImmutableMap<Class<?>, Converter<? extends Message, ?>>
            PRIMITIVE_TO_HANDLER =
            ImmutableMap.<Class<?>, Converter<? extends Message, ?>>builder()
                    .put(Integer.class, new Int32Handler())
                    .put(Long.class, new Int64Handler())
                    .put(Float.class, new FloatHandler())
                    .put(Double.class, new DoubleHandler())
                    .put(Boolean.class, new BoolHandler())
                    .put(String.class, new StringHandler())
                    .build();

    @Override
    protected T toObject(M input) {
        Class<?> boxedType = input.getClass();
        @SuppressWarnings("unchecked")
        Converter<M, T> typeUnpacker =
                (Converter<M, T>) PROTO_WRAPPER_TO_HANDLER.get(boxedType);
        checkArgument(typeUnpacker != null,
                      "Could not find a primitive type for %s.",
                      boxedType.getName());
        T result = typeUnpacker.convert(input);
        return result;
    }

    @Override
    protected M toMessage(T input) {
        Class<?> cls = input.getClass();
        @SuppressWarnings("unchecked")
        Converter<M, T> converter =
                (Converter<M, T>) PRIMITIVE_TO_HANDLER.get(cls);
        checkArgument(converter != null,
                      "Could not find a wrapper type for %s.",
                      cls.getName());
        M result = converter.reverse()
                            .convert(input);
        return result;
    }

    private static final class Int32Handler extends PrimitiveHandler<Int32Value, Integer> {

        @Override
        protected Integer unpack(Int32Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected Int32Value pack(Integer value) {
            return Int32Value
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class Int64Handler extends PrimitiveHandler<Int64Value, Long> {

        @Override
        protected Long unpack(Int64Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected Int64Value pack(Long value) {
            return Int64Value
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class UInt32Handler extends PrimitiveHandler<UInt32Value, Integer> {

        @Override
        protected Integer unpack(UInt32Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected UInt32Value pack(Integer value) {
            // Hidden by Int32Handler
            return UInt32Value
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class UInt64Handler extends PrimitiveHandler<UInt64Value, Long> {

        @Override
        protected Long unpack(UInt64Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected UInt64Value pack(Long value) {
            // Hidden by Int64Handler
            return UInt64Value
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class FloatHandler extends PrimitiveHandler<FloatValue, Float> {

        @Override
        protected Float unpack(FloatValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected FloatValue pack(Float value) {
            return FloatValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class DoubleHandler extends PrimitiveHandler<DoubleValue, Double> {

        @Override
        protected Double unpack(DoubleValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected DoubleValue pack(Double value) {
            return DoubleValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class BoolHandler extends PrimitiveHandler<BoolValue, Boolean> {

        @Override
        protected Boolean unpack(BoolValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected BoolValue pack(Boolean value) {
            return BoolValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class StringHandler extends PrimitiveHandler<StringValue, String> {

        @Override
        protected String unpack(StringValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected StringValue pack(String value) {
            return StringValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }
}
