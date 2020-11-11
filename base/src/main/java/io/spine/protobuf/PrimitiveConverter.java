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
final class PrimitiveConverter<M extends Message, T> extends ProtoConverter<M, T> {

    private static final ImmutableMap<Class<?>, Converter<? extends Message, ?>>
            PROTO_WRAPPER_TO_HANDLER =
            ImmutableMap.<Class<?>, Converter<? extends Message, ?>>builder()
                    .put(Int32Value.class, new Int32Converter())
                    .put(Int64Value.class, new Int64Converter())
                    .put(UInt32Value.class, new UInt32Converter())
                    .put(UInt64Value.class, new UInt64Converter())
                    .put(FloatValue.class, new FloatConverter())
                    .put(DoubleValue.class, new DoubleConverter())
                    .put(BoolValue.class, new BoolConverter())
                    .put(StringValue.class, new StringConverter())
                    .build();
    private static final ImmutableMap<Class<?>, Converter<? extends Message, ?>>
            PRIMITIVE_TO_HANDLER =
            ImmutableMap.<Class<?>, Converter<? extends Message, ?>>builder()
                    .put(Integer.class, new Int32Converter())
                    .put(Long.class, new Int64Converter())
                    .put(Float.class, new FloatConverter())
                    .put(Double.class, new DoubleConverter())
                    .put(Boolean.class, new BoolConverter())
                    .put(String.class, new StringConverter())
                    .build();

    @Override
    protected T toObject(M input) {
        Class<?> boxedType = input.getClass();
        @SuppressWarnings("unchecked")
        Converter<M, T> typeUnpacker = (Converter<M, T>) PROTO_WRAPPER_TO_HANDLER.get(boxedType);
        checkArgument(
                typeUnpacker != null,
                "Could not find a primitive type for `%s`.",
                boxedType.getName()
        );
        T result = typeUnpacker.convert(input);
        return result;
    }

    @Override
    protected M toMessage(T input) {
        Class<?> cls = input.getClass();
        @SuppressWarnings("unchecked")
        Converter<M, T> converter = (Converter<M, T>) PRIMITIVE_TO_HANDLER.get(cls);
        checkArgument(
                converter != null,
                "Could not find a wrapper type for `%s`.",
                cls.getName()
        );
        M result = converter.reverse()
                            .convert(input);
        return result;
    }

    private static final class Int32Converter extends WrappingConverter<Int32Value, Integer> {

        @Override
        protected Integer unwrap(Int32Value message) {
            return message.getValue();
        }

        @Override
        protected Int32Value wrap(Integer value) {
            return Int32Value
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class Int64Converter extends WrappingConverter<Int64Value, Long> {

        @Override
        protected Long unwrap(Int64Value message) {
            return message.getValue();
        }

        @Override
        protected Int64Value wrap(Long value) {
            return Int64Value
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class UInt32Converter extends WrappingConverter<UInt32Value, Integer> {

        @Override
        protected Integer unwrap(UInt32Value message) {
            return message.getValue();
        }

        @Override
        protected UInt32Value wrap(Integer value) {
            throw new UnsupportedOperationException(
                    "`Integer` value should be always mapped to a signed `Int32Value`. " +
                            "Conversion to `UInt32Value` is not supported."
            );
        }
    }

    private static final class UInt64Converter extends WrappingConverter<UInt64Value, Long> {

        @Override
        protected Long unwrap(UInt64Value message) {
            return message.getValue();
        }

        @Override
        protected UInt64Value wrap(Long value) {
            throw new UnsupportedOperationException(
                    "`Long` value should be always mapped to a signed `Int64Value`. " +
                            "Conversion to `UInt64Value` is not supported."
            );
        }
    }

    private static final class FloatConverter extends WrappingConverter<FloatValue, Float> {

        @Override
        protected Float unwrap(FloatValue message) {
            return message.getValue();
        }

        @Override
        protected FloatValue wrap(Float value) {
            return FloatValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class DoubleConverter extends WrappingConverter<DoubleValue, Double> {

        @Override
        protected Double unwrap(DoubleValue message) {
            return message.getValue();
        }

        @Override
        protected DoubleValue wrap(Double value) {
            return DoubleValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class BoolConverter extends WrappingConverter<BoolValue, Boolean> {

        @Override
        protected Boolean unwrap(BoolValue message) {
            return message.getValue();
        }

        @Override
        protected BoolValue wrap(Boolean value) {
            return BoolValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class StringConverter extends WrappingConverter<StringValue, String> {

        @Override
        protected String unwrap(StringValue message) {
            return message.getValue();
        }

        @Override
        protected StringValue wrap(String value) {
            return StringValue
                    .newBuilder()
                    .setValue(value)
                    .build();
        }
    }
}
