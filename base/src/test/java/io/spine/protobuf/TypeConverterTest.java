/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.base.Charsets;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.EnumValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import io.spine.test.protobuf.TaskStatus;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.test.protobuf.TaskStatus.EXECUTING;
import static io.spine.test.protobuf.TaskStatus.FAILED;
import static io.spine.test.protobuf.TaskStatus.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeConverter utility class should")
class TypeConverterTest extends UtilityClassTest<TypeConverter> {

    TypeConverterTest() {
        super(TypeConverter.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(Any.class, Any.getDefaultInstance());
    }

    @Nested
    @DisplayName("map")
    class Map {

        @Test
        @DisplayName("arbitrary message to itself")
        void map_arbitrary_message_to_itself() {
            Message message = StringValue.of(newUuid());
            checkMapping(message, message);
        }

        @Test
        @DisplayName("Int32Value to int")
        void map_Int32Value_to_int() {
            int rawValue = 42;
            Message value = Int32Value.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("Int64Value to int")
        void map_Int64Value_to_long() {
            long rawValue = 42;
            Message value = Int64Value.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("FloatValue to float")
        void map_FloatValue_to_float() {
            float rawValue = 42.0f;
            Message value = FloatValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("DoubleValue to double")
        void map_DoubleValue_to_double() {
            double rawValue = 42.0;
            Message value = DoubleValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("BoolValue to boolean")
        void map_BoolValue_to_boolean() {
            boolean rawValue = true;
            Message value = BoolValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("StringValue to String")
        void map_StringValue_to_String() {
            String rawValue = "Hello";
            Message value = StringValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("BytesValue to ByteString")
        void map_BytesValue_to_ByteString() {
            ByteString rawValue = ByteString.copyFrom("Hello!", Charsets.UTF_8);
            Message value = BytesValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("UInt32 to int")
        void map_uint32_to_int() {
            int value = 42;
            UInt32Value wrapped = UInt32Value.of(value);
            Any packed = AnyPacker.pack(wrapped);
            int mapped = TypeConverter.toObject(packed, Integer.class);
            assertEquals(value, mapped);
        }

        @Test
        @DisplayName("UInt64 to int")
        void map_uint64_to_long() {
            long value = 42L;
            UInt64Value wrapped = UInt64Value.of(value);
            Any packed = AnyPacker.pack(wrapped);
            long mapped = TypeConverter.toObject(packed, Long.class);
            assertEquals(value, mapped);
        }

        private void checkMapping(Object javaObject,
                                  Message protoObject) {
            Any wrapped = AnyPacker.pack(protoObject);
            Object mappedJavaObject = TypeConverter.toObject(wrapped, javaObject.getClass());
            assertEquals(javaObject, mappedJavaObject);
            Any restoredWrapped = TypeConverter.toAny(mappedJavaObject);
            Message restored = AnyPacker.unpack(restoredWrapped);
            assertEquals(protoObject, restored);
        }
    }

    @Nested
    @DisplayName("convert `EnumValue` to `Enum`")
    class ConvertEnumValueToEnum {

        @Test
        @DisplayName("if a `EnumValue` has the enum constant name specified")
        void ifHasName() {
            EnumValue value = EnumValue.newBuilder()
                                       .setName(SUCCESS.name())
                                       .build();
            checkConverts(value, SUCCESS);
        }

        @Test
        @DisplayName("if a `EnumValue` has the enum constant number specified")
        void ifHasNumber() {
            EnumValue value = EnumValue.newBuilder()
                                       .setNumber(EXECUTING.getNumber())
                                       .build();
            checkConverts(value, EXECUTING);
        }

        @Test
        @DisplayName("using the constant name if both the name and the number are specified")
        void preferringConversionWithName() {
            // Set the different name and number just for the sake of test.
            EnumValue value = EnumValue.newBuilder()
                                       .setName(SUCCESS.name())
                                       .setNumber(FAILED.getNumber())
                                       .build();
            checkConverts(value, SUCCESS);
        }

        private void checkConverts(EnumValue enumValue, Enum<?> expected) {
            Any wrapped = AnyPacker.pack(enumValue);
            Object mappedJavaObject =
                    TypeConverter.toObject(wrapped, expected.getDeclaringClass());
            assertEquals(expected, mappedJavaObject);
        }
    }

    @SuppressWarnings("CheckReturnValue") // The method is called to throw exception.
    @Test
    @DisplayName("throw an `IAE` when the `EnumValue` with an unknown number is specified")
    void throwOnInvalidNumber() {
        int unknownValue = 156;
        EnumValue value = EnumValue.newBuilder()
                                   .setNumber(unknownValue)
                                   .build();
        Any wrapped = AnyPacker.pack(value);
        assertThrows(IllegalArgumentException.class,
                     () -> TypeConverter.toObject(wrapped, TaskStatus.class));
    }

    @SuppressWarnings("CheckReturnValue") // The method is called to throw exception.
    @Test
    @DisplayName("throw an `IAE` when converting a non-`EnumValue` object to a `Enum`")
    void throwOnRawValuesForEnum() {
        Int32Value enumNumber = Int32Value
                .newBuilder()
                .setValue(SUCCESS.getNumber())
                .build();
        Any packed = AnyPacker.pack(enumNumber);
        assertThrows(IllegalArgumentException.class,
                     () -> TypeConverter.toObject(packed, TaskStatus.class));
    }

    @Test
    @DisplayName("convert `Enum` to `EnumValue`")
    void convertEnumToEnumValue() {
        Any restoredWrapped = TypeConverter.toAny(SUCCESS);
        Message restored = AnyPacker.unpack(restoredWrapped);
        EnumValue expected = EnumValue
                .newBuilder()
                .setName(SUCCESS.name())
                .setNumber(SUCCESS.getNumber())
                .build();
        assertEquals(expected, restored);
    }

    @Nested
    @DisplayName("convert")
    class Convert {

        @Test
        @DisplayName("a value to a particular message")
        void valueToParticularMessage() {
            String stringValue = "a string value";
            StringValue convertedValue = toMessage(stringValue, StringValue.class);
            assertEquals(stringValue, convertedValue.getValue());
        }
    }
}
