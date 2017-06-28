/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import io.spine.annotation.Internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A utility for converting the {@linkplain Message Protobuf Messages} (in form of {@link Any}) into
 * arbitrary {@linkplain Object Java Objects} and vice versa.
 *
 * <p>Currently, the supported types are:
 * <ul>
 *     <li>{@link Message} - converted via {@link AnyPacker};
 *     <li>Java primitives - the passed {@link Any} is unpacked into one of the types
 *         {@code Int32Value, Int64Value, UInt32Value, UInt64Value, FloatValue, DoubleValue,
 *         BoolValue, StringValue, BytesValue} and then transformed into the corresponding Java
 *         type, either a primitive value, or {@code String} of {@link ByteString}. For more info
 *         see <a href="https://developers.google.com/protocol-buffers/docs/proto3#scalar">
 *         the official doc</a>.
 * </ul>
 *
 * @author Dmytro Dashenkov
 */
@Internal
public final class TypeConverter {

    private TypeConverter() {
        // Prevent utility class initialization.
    }

    /**
     * Performs the {@link Any} to {@link Object} mapping.
     *
     * @param message the {@link Any} value to convert
     * @param target  the conversion target class
     * @param <T>     the conversion target type
     * @return the converted value
     */
    public static <T> T toObject(Any message, Class<T> target) {
        checkNotNull(message);
        checkNotNull(target);
        final AnyCaster<T> caster = AnyCaster.forType(target);
        final T result = caster.convert(message);
        return result;
    }

    /**
     * Performs the {@link Object} to {@link Any} mapping.
     *
     * @param value the {@link Object} value to convert
     * @param <T>   the converted object type
     * @return the packed value
     */
    public static <T> Any toAny(T value) {
        checkNotNull(value);
        @SuppressWarnings("unchecked") // Must be checked at runtime
        final Class<T> srcClass = (Class<T>) value.getClass();
        final AnyCaster<T> caster = AnyCaster.forType(srcClass);
        final Message message = caster.reverse()
                                      .convert(value);
        checkNotNull(message);
        final Any result = AnyPacker.pack(message);
        return result;
    }

    /**
     * The {@link Function} performing the described type conversion.
     */
    private abstract static class AnyCaster<T> extends Converter<Any, T> {

        private static <T> AnyCaster<T> forType(Class<T> cls) {
            checkNotNull(cls);
            if (Message.class.isAssignableFrom(cls)) {
                return new MessageTypeCaster<>();
            } else if (ByteString.class.isAssignableFrom(cls)) {
                @SuppressWarnings("unchecked") // Logically checked
                final AnyCaster<T> result = (AnyCaster<T>) new BytesCaster();
                return result;
            } else {
                return new PrimitiveTypeCaster<>();
            }
        }

        @Override
        protected T doForward(Any input) {
            return toObject(input);
        }

        @Override
        protected Any doBackward(T t) {
            final Message message = toMessage(t);
            return AnyPacker.pack(message);
        }

        protected abstract T toObject(Any input);

        protected abstract Message toMessage(T input);
    }

    private static class BytesCaster extends AnyCaster<ByteString> {

        @Override
        protected ByteString toObject(Any input) {
            final BytesValue bytes = AnyPacker.unpack(input);
            final ByteString result = bytes.getValue();
            return result;
        }

        @Override
        protected Message toMessage(ByteString input) {
            final BytesValue bytes = BytesValue.newBuilder()
                                               .setValue(input)
                                               .build();
            return bytes;
        }
    }

    private static class MessageTypeCaster<T> extends AnyCaster<T> {

        @Override
        protected T toObject(Any input) {
            final Message unpacked = AnyPacker.unpack(input);
            @SuppressWarnings("unchecked") final T result = (T) unpacked;
            return result;
        }

        @Override
        protected Message toMessage(T input) {
            checkState(input instanceof Message);
            final Message result = (Message) input;
            return result;
        }
    }

    private static class PrimitiveTypeCaster<T> extends AnyCaster<T> {

        private static final ImmutableMap<Class, Converter<? extends Message, ?>>
                PROTO_WRAPPER_TO_HANDLER =
                ImmutableMap.<Class, Converter<? extends Message, ?>>builder()
                        .put(Int32Value.class, new Int32Handler())
                        .put(Int64Value.class, new Int64Handler())
                        .put(UInt32Value.class, new UInt32Handler())
                        .put(UInt64Value.class, new UInt64Handler())
                        .put(FloatValue.class, new FloatHandler())
                        .put(DoubleValue.class, new DoubleHandler())
                        .put(BoolValue.class, new BoolHandler())
                        .put(StringValue.class, new StringHandler())
                        .build();
        private static final ImmutableMap<Class, Converter<? extends Message, ?>>
                PRIMITIVE_TO_HANDLER =
                ImmutableMap.<Class, Converter<? extends Message, ?>>builder()
                        .put(Integer.class, new Int32Handler())
                        .put(Long.class, new Int64Handler())
                        .put(Float.class, new FloatHandler())
                        .put(Double.class, new DoubleHandler())
                        .put(Boolean.class, new BoolHandler())
                        .put(String.class, new StringHandler())
                        .build();

        @Override
        protected T toObject(Any input) {
            final Message unpacked = AnyPacker.unpack(input);
            final Class boxedType = unpacked.getClass();
            @SuppressWarnings("unchecked") final Function<Message, T> typeUnpacker =
                    (Function<Message, T>) PROTO_WRAPPER_TO_HANDLER.get(boxedType);
            checkArgument(typeUnpacker != null,
                          "Could not find a primitive type for %s.",
                          boxedType.getCanonicalName());
            final T result = typeUnpacker.apply(unpacked);
            return result;
        }

        @Override
        protected Message toMessage(T input) {
            final Class<?> cls = input.getClass();
            @SuppressWarnings("unchecked") final Converter<Message, T> converter =
                    (Converter<Message, T>) PRIMITIVE_TO_HANDLER.get(cls);
            checkArgument(converter != null,
                          "Could not find a wrapper type for %s.",
                          cls.getCanonicalName());
            final Message result = converter.reverse()
                                            .convert(input);
            return result;
        }
    }

    /**
     * A converter handling the primitive types transformations.
     *
     * <p>It's sufficient to override methods {@link #pack(Object) pack(T)} and
     * {@link #unpack(Message) unpack(M)} when extending this class.
     *
     * <p>Since the Protobuf and Java primitives differ, there may be more then one
     * {@code PrimitiveHandler} for a Java primitive type. In this case, if the resulting Protobuf
     * value type is not specified explicitly, the closest type is selected as a target for
     * the conversion. The closeness of two types is determined by the lexicographic closeness.
     *
     * @param <M> the type of the Protobuf primitive wrapper
     * @param <T> the type of the Java primitive wrapper
     */
    private abstract static class PrimitiveHandler<M extends Message, T> extends Converter<M, T> {

        @Override
        protected T doForward(M input) {
            return unpack(input);
        }

        @Override
        protected M doBackward(T input) {
            return pack(input);
        }

        /**
         * Unpacks a primitive value of type {@code T} from the given wrapper value.
         *
         * @param message packed value
         * @return unpacked value
         */
        protected abstract T unpack(M message);

        /**
         * Packs the given primitive value into a Protobuf wrapper of type {@code M}.
         *
         * @param value primitive value
         * @return packed value
         */
        protected abstract M pack(T value);
    }

    private static class Int32Handler extends PrimitiveHandler<Int32Value, Integer> {

        @Override
        protected Integer unpack(Int32Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected Int32Value pack(Integer value) {
            return Int32Value.newBuilder()
                             .setValue(value)
                             .build();
        }
    }

    private static class Int64Handler extends PrimitiveHandler<Int64Value, Long> {

        @Override
        protected Long unpack(Int64Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected Int64Value pack(Long value) {
            return Int64Value.newBuilder()
                             .setValue(value)
                             .build();
        }
    }

    private static class UInt32Handler extends PrimitiveHandler<UInt32Value, Integer> {

        @Override
        protected Integer unpack(UInt32Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected UInt32Value pack(Integer value) {
            // Hidden by Int32Handler
            return UInt32Value.newBuilder()
                              .setValue(value)
                              .build();
        }
    }

    private static class UInt64Handler extends PrimitiveHandler<UInt64Value, Long> {

        @Override
        protected Long unpack(UInt64Value message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected UInt64Value pack(Long value) {
            // Hidden by Int64Handler
            return UInt64Value.newBuilder()
                              .setValue(value)
                              .build();
        }
    }

    private static class FloatHandler extends PrimitiveHandler<FloatValue, Float> {

        @Override
        protected Float unpack(FloatValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected FloatValue pack(Float value) {
            return FloatValue.newBuilder()
                             .setValue(value)
                             .build();
        }
    }

    private static class DoubleHandler extends PrimitiveHandler<DoubleValue, Double> {

        @Override
        protected Double unpack(DoubleValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected DoubleValue pack(Double value) {
            return DoubleValue.newBuilder()
                              .setValue(value)
                              .build();
        }
    }

    private static class BoolHandler extends PrimitiveHandler<BoolValue, Boolean> {

        @Override
        protected Boolean unpack(BoolValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected BoolValue pack(Boolean value) {
            return BoolValue.newBuilder()
                            .setValue(value)
                            .build();
        }
    }

    private static class StringHandler extends PrimitiveHandler<StringValue, String> {

        @Override
        protected String unpack(StringValue message) {
            checkNotNull(message);
            return message.getValue();
        }

        @Override
        protected StringValue pack(String value) {
            return StringValue.newBuilder()
                              .setValue(value)
                              .build();
        }
    }
}
