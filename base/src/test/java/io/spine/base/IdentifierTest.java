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

package io.spine.base;

import com.google.common.testing.NullPointerTester;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.OptionalSubject;
import com.google.common.truth.Truth8;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import io.spine.base.Identifier.Type;
import io.spine.protobuf.AnyPacker;
import io.spine.test.identifiers.IdWithPrimitiveFields;
import io.spine.test.identifiers.NestedMessageId;
import io.spine.test.identifiers.SeveralFieldsId;
import io.spine.test.identifiers.TimestampFieldId;
import io.spine.testing.TestValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.EMPTY_ID;
import static io.spine.base.Identifier.NULL_ID;
import static io.spine.base.Identifier.checkSupported;
import static io.spine.base.Identifier.findField;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Identifier` should")
class IdentifierTest {

    private static final String TEST_ID = "someTestId 1234567890 !@#$%^&()[]{}-+=_";

    @Nested
    @DisplayName("generate a new ID value which is")
    class GenerateUuid {

        @Test
        @DisplayName("UUID-based")
        void uuidBased() {
            // We have non-empty values.
            assertTrue(newUuid().length() > 0);
        }

        @Test
        @DisplayName("unique")
        void unique() {
            assertNotEquals(newUuid(), newUuid());
        }
    }

    @Nested
    @DisplayName("create values by type")
    class CreateByType {

        @Test
        @DisplayName("`String`")
        void ofString() {
            assertTrue(Identifier.from("")
                                 .isString());
        }

        @Test
        @DisplayName("`Integer`")
        void ofInteger() {
            assertTrue(Identifier.from(0)
                                 .isInteger());
        }

        @Test
        @DisplayName("`Long`")
        void ofLong() {
            assertTrue(Identifier.from(0L)
                                 .isLong());
        }

        @Test
        @DisplayName("`Message`")
        void ofMessage() {
            assertTrue(Identifier.from(toMessage(300))
                                 .isMessage());
        }
    }

    @Nested
    @DisplayName("recognize type by supported Message type")
    @SuppressWarnings("BadImport") // OK to static-import `Identifier.Type` for brevity.
    class RecognizeType {

        @Test
        @DisplayName("INTEGER")
        void ofInteger() {
            assertTrue(Type.INTEGER.matchMessage(toMessage(10)));
        }

        @Test
        @DisplayName("LONG")
        void ofLong() {
            assertTrue(Type.LONG.matchMessage(toMessage(1020L)));
        }

        @Test
        @DisplayName("STRING")
        void ofString() {
            assertTrue(Type.STRING.matchMessage(toMessage("")));
        }

        @Test
        @DisplayName("MESSAGE")
        void ofMessage() {
            assertTrue(Type.MESSAGE.matchMessage(Timestamp.getDefaultInstance()));

            // Do not consider primitive type wrappers as message types.
            assertFalse(Type.MESSAGE.matchMessage(StringValue.getDefaultInstance()));
            assertFalse(Type.MESSAGE.matchMessage(Int32Value.getDefaultInstance()));
            assertFalse(Type.MESSAGE.matchMessage(Int64Value.getDefaultInstance()));
        }
    }

    @Nested
    @DisplayName("obtain default value for")
    class DefaultValue {

        @Test
        @DisplayName("`Integer`")
        void ofInteger() {
            assertThat(Identifier.defaultValue(Integer.class))
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("`Long`")
        void ofLong() {
            assertThat(Identifier.defaultValue(Long.class))
                    .isEqualTo(0L);
        }

        @Test
        @DisplayName("`String`")
        void ofString() {
            assertThat(Identifier.defaultValue(String.class))
                    .isEmpty();
        }

        @Test
        @DisplayName("`Message`")
        void ofMessage() {
            assertThat(Identifier.defaultValue(Timestamp.class))
                    .isEqualTo(Timestamp.getDefaultInstance());
        }
    }

    @Nested
    @DisplayName("return `EMPTY_ID` when converting")
    class EmptyId {

        @Test
        @DisplayName("empty string")
        void emptyString() {
            assertEmpty("");
        }

        @Test
        @DisplayName("a `Message` with the default value")
        void defaultInstance() {
            assertEmpty(StringValue.getDefaultInstance());
        }

        @Test
        @DisplayName("a Message with a field of Message type, which has the default value")
        void defaultNestedMessage() {
            assertEmpty(TimestampFieldId.getDefaultInstance());
        }

        private void assertEmpty(Object id) {
            String str = Identifier.toString(id);
            assertThat(str).isEqualTo(EMPTY_ID);
        }
    }

    @Nested
    @DisplayName("verify if ID is empty")
    class VerifyEmptyId {

        @Nested
        @DisplayName("returning always `false` for")
        class AlwaysNonEmpty {

            @Test
            @DisplayName("`Integer`")
            void intValue() {
                assertNotEmpty(0);
                assertNotEmpty(TestValues.random(Integer.MIN_VALUE, Integer.MAX_VALUE));
            }

            @Test
            @DisplayName("`Long`")
            void longValue() {
                assertNotEmpty(0L);
                assertNotEmpty(TestValues.longRandom(Long.MIN_VALUE, Long.MAX_VALUE));
            }
        }

        @Test
        @DisplayName("taking string value")
        void emptyString() {
            assertEmpty("");
            assertNotEmpty(TestValues.randomString());
        }

        @Test
        @DisplayName("taking message fields")
        void messageId() {
            assertEmpty(Struct.getDefaultInstance());
            assertNotEmpty(Time.currentTime());
        }

        <I> void assertNotEmpty(I value) {
            assertThatEmpty(value).isFalse();
        }

        <I> void assertEmpty(I value) {
            assertThatEmpty(value).isTrue();
        }

        private <I> BooleanSubject assertThatEmpty(I value) {
            return assertThat(Identifier.isEmpty(value));
        }
    }

    @Nested
    @DisplayName("convert to `String` an ID of type")
    class ConvertToString {

        @Test
        @DisplayName("`Integer`")
        @SuppressWarnings("UnnecessaryBoxing")
            // OK as we want to show types explicitly.
        void ofInteger() {
            assertEquals("10", Identifier.toString(Integer.valueOf(10)));
        }

        @Test
        @DisplayName("`Long`")
        @SuppressWarnings("UnnecessaryBoxing")
            // OK as we want to show types explicitly.
        void ofLong() {
            assertEquals("100000", Identifier.toString(Long.valueOf(100_000)));
        }

        @Test
        @DisplayName("wrapped `Integer`")
        void ofWrappedInteger() {
            int value = 1024;
            Int32Value id = Int32Value.of(value);
            String expected = Integer.toString(value);

            String actual = Identifier.toString(id);

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("wrapped `Long`")
        void ofWrappedLong() {
            long value = 100500L;
            Int64Value id = Int64Value.of(value);
            String expected = Long.toString(value);

            String actual = Identifier.toString(id);

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("`String`")
        void ofString() {
            assertEquals(TEST_ID, Identifier.toString(TEST_ID));
        }

        @Test
        @DisplayName("wrapped `String`")
        void ofWrappedString() {
            StringValue id = StringValue.of(TEST_ID);

            String result = Identifier.toString(id);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("`Message` with string field")
        void ofMessage() {
            StringValue id = StringValue.of(TEST_ID);

            String result = Identifier.toString(id);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("`Message` with nested `Message`")
        void ofNestedMessage() {
            StringValue value = StringValue.of(TEST_ID);
            NestedMessageId idToConvert = NestedMessageId
                    .newBuilder()
                    .setId(value)
                    .build();

            String result = Identifier.toString(idToConvert);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("Any")
        void ofAny() {
            StringValue messageToWrap = StringValue.of(TEST_ID);
            Any any = AnyPacker.pack(messageToWrap);

            String result = Identifier.toString(any);

            assertEquals(TEST_ID, result);
        }
    }

    @Test
    @DisplayName("provide string conversion of messages with several fields")
    void toStringSeveralFields() {
        String nestedString = "nested_string";
        String outerString = "outer_string";
        int number = 256;

        StringValue nestedMessageString = StringValue.of(nestedString);
        SeveralFieldsId idToConvert = SeveralFieldsId
                .newBuilder()
                .setString(outerString)
                .setNumber(number)
                .setMessage(nestedMessageString)
                .build();

        String expected =
                "string=\"" + outerString + '\"' +
                        " number=" + number +
                        " message { value=\"" + nestedString + "\" }";

        String actual = Identifier.toString(idToConvert);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester()
                .setDefault(Any.class, AnyPacker.pack(StringValue.of(TEST_ID)))
                .setDefault(Descriptor.class, Any.getDescriptor())
                .testAllPublicStaticMethods(Identifier.class);
    }

    @Nested
    @DisplayName("create values depend on the wrapper message type for")
    @SuppressWarnings("BadImport") // OK to static-import `Identifier.Type` for brevity.
    class CreateValues {

        @Test
        @DisplayName("`integer`")
        void intValue() {
            assertEquals(10, Type.INTEGER.fromMessage(toMessage(10)));
        }

        @Test
        @DisplayName("`long`")
        void longValue() {
            assertEquals(1024L, Type.LONG.fromMessage(toMessage(1024L)));
        }

        @Test
        @DisplayName("`string`")
        void stringValue() {
            String value = getClass().getSimpleName();
            assertEquals(value, Type.STRING.fromMessage(toMessage(value)));
        }
    }

    @Nested
    @DisplayName("check that a class of identifiers is supported for")
    class CheckSupported {

        @Test
        @DisplayName("`String`")
        void stringValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(String.class));
        }

        @Test
        @DisplayName("`Integer`")
        void integerValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(Integer.class));
        }

        @Test
        @DisplayName("`Long`")
        void longValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(Long.class));
        }

        @Test
        @DisplayName("a `Message` class")
        void messageValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(StringValue.class));
        }
    }

    @Test
    @DisplayName("throw `IllegalArgumentException` for unsupported class")
    void checkNotSupported() {
        assertIllegalArgument(() -> checkSupported(Boolean.class));
    }

    @Nested
    @DisplayName("reject unsupported")
    class RejectUnsupported {

        @Test
        @DisplayName("value")
        @SuppressWarnings("UnnecessaryBoxing")
            // We want to make the unsupported type obvious.
        void value() {
            assertIllegalArgument(() -> Identifier.toString(Boolean.valueOf(true)));
        }

        @Test
        @DisplayName("class")
        void clazz() {
            assertIllegalArgument(() -> Identifier.toType(Float.class));
        }
    }

    @Nested
    @DisplayName("unpack")
    class Unpack {

        @Test
        @DisplayName("Any with StringValue and cast")
        void anyWithStringValue() {
            StringValue testIdMessage = StringValue.of(TEST_ID);
            Any any = AnyPacker.pack(testIdMessage);
            String unpackedId = Identifier.unpack(any, String.class);
            assertEquals(testIdMessage.getValue(), unpackedId);
        }

        @Test
        @DisplayName("and throw if `Any` is empty")
        void rejectEmptyAny() {
            assertIllegalArgument(() -> Identifier.unpack(Any.getDefaultInstance()));
        }
    }

    @Test
    @DisplayName("reject packing unsupported type")
    @SuppressWarnings("UnnecessaryBoxing")
        // We want to make the unsupported type obvious.
    void noPackingForUnsupported() {
        assertIllegalArgument(() -> Identifier.pack(Boolean.valueOf(false)));
    }

    @Test
    @DisplayName("return `NULL_ID` when converting null")
    void nullId() {
        assertEquals(NULL_ID, Identifier.toString(null));
    }

    @Test
    @DisplayName("declare `ID_PROPERTY_SUFFIX`")
    void idPropSuffix() {
        assertThat(Identifier.ID_PROPERTY_SUFFIX).isEqualTo("id");
    }

    @Nested
    @DisplayName("recognize field descriptor")
    @SuppressWarnings("BadImport") // OK to static-import `Identifier.Type` for brefity.
    class FieldDescr {

        @Test
        @DisplayName("`Integer`")
        void intField() {
            assertTrue(Type.INTEGER.matchField(field(1)));
        }

        @Test
        @DisplayName("`Long`")
        void longField() {
            assertTrue(Type.LONG.matchField(field(3)));
        }

        @Test
        @DisplayName("`String`")
        void stringField() {
            assertTrue(Type.STRING.matchField(field(0)));
        }

        @Test
        @DisplayName("`Message`")
        void messgeField() {
            assertTrue(Type.MESSAGE.matchField(field(2)));
        }

        FieldDescriptor field(int index) {
            FieldDescriptor field =
                    SeveralFieldsId.getDescriptor()
                                   .getFields()
                                   .get(index);
            return field;
        }
    }

    @Nested
    @DisplayName("find field by type")
    class FindingField {

        @Test
        @DisplayName("`Integer`")
        void intType() {
            assertFound(Integer.class, SeveralFieldsId.getDescriptor());
            assertNotFound(Integer.class, TimestampFieldId.getDescriptor());
        }

        @Test
        @DisplayName("`Long`")
        void longType() {
            assertFound(Long.class, SeveralFieldsId.getDescriptor());
            assertNotFound(Long.class, TimestampFieldId.getDescriptor());
        }

        @Test
        @DisplayName("`String`")
        void stringType() {
            assertFound(String.class, SeveralFieldsId.getDescriptor());
            assertNotFound(String.class, NestedMessageId.getDescriptor());
        }

        @Test
        @DisplayName("`Message`")
        void messageType() {
            assertFound(StringValue.class, SeveralFieldsId.getDescriptor());
            assertNotFound(StringValue.class, IdWithPrimitiveFields.getDescriptor());
        }

        <I> void assertFound(Class<I> idClass, Descriptor message) {
            assertField(idClass, message).isPresent();
        }

        <I> void assertNotFound(Class<I> idClass, Descriptor message) {
            assertField(idClass, message).isEmpty();
        }

        private <I> OptionalSubject assertField(Class<I> idClass, Descriptor message) {
            return Truth8.assertThat(findField(idClass, message));
        }
    }
}
