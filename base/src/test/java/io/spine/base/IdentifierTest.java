/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Identifier.Type;
import io.spine.protobuf.AnyPacker;
import io.spine.test.identifiers.NestedMessageId;
import io.spine.test.identifiers.SeveralFieldsId;
import io.spine.test.identifiers.TimestampFieldId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.EMPTY_ID;
import static io.spine.base.Identifier.NULL_ID;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.testing.TestValues.newUuidValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Identifier should")
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
        @DisplayName("String")
        void ofString() {
            assertTrue(Identifier.from("")
                                 .isString());
        }

        @Test
        @DisplayName("Integer")
        void ofInteger() {
            assertTrue(Identifier.from(0)
                                 .isInteger());
        }

        @Test
        @DisplayName("Long")
        void ofLong() {
            assertTrue(Identifier.from(0L)
                                 .isLong());
        }

        @Test
        @DisplayName("Message")
        void ofMessage() {
            assertTrue(Identifier.from(toMessage(300))
                                 .isMessage());
        }
    }

    @Nested
    @DisplayName("recognize type by supported Message type")
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
        @DisplayName("Integer")
        void ofInteger() {
            assertEquals(0, Identifier.getDefaultValue(Integer.class)
                                      .intValue());
        }

        @Test
        @DisplayName("Long")
        void ofLong() {
            assertEquals(0L, Identifier.getDefaultValue(Long.class)
                                       .longValue());
        }

        @Test
        @DisplayName("String")
        void ofString() {
            assertEquals("", Identifier.getDefaultValue(String.class));
        }

        @Test
        @DisplayName("Message")
        void ofMessage() {
            assertEquals(Timestamp.getDefaultInstance(),
                         Identifier.getDefaultValue(Timestamp.class));
        }
    }

    @Nested
    @DisplayName("return EMPTY_ID when converting")
    class EmptyId {

        @Test
        @DisplayName("empty string")
        void emptyString() {
            assertEquals(EMPTY_ID, Identifier.toString(""));
        }

        @Test
        @DisplayName("a Message with the default value")
        void return_EMPTY_ID_if_convert_empty_message_to_string() {
            assertEquals(EMPTY_ID, Identifier.toString(StringValue.getDefaultInstance()));
        }

        @Test
        @DisplayName("a Message with a field of Message type, which has the default value")
        void defaultMessage() {
            assertEquals(EMPTY_ID, Identifier.toString(TimestampFieldId.getDefaultInstance()));
        }
    }

    @Nested
    @DisplayName("convert to String an ID of type")
    class ConvertToString {

        @Test
        @DisplayName("Integer")
        @SuppressWarnings("UnnecessaryBoxing") // OK as we want to show types clearly.
        void ofInteger() {
            assertEquals("10", Identifier.toString(Integer.valueOf(10)));
        }

        @Test
        @DisplayName("Long")
        @SuppressWarnings("UnnecessaryBoxing") // OK as we want to show types clearly.
        void ofLong() {
            assertEquals("100000", Identifier.toString(Long.valueOf(100_000)));
        }

        @Test
        @DisplayName("wrapped Integer")
        void ofWrappedInteger() {
            Integer value = 1024;
            Int32Value id = toMessage(value);
            String expected = value.toString();

            String actual = Identifier.toString(id);

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("wrapped Long")
        void ofWrappedLong() {
            Long value = 100500L;
            Int64Value id = toMessage(value);
            String expected = value.toString();

            String actual = Identifier.toString(id);

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("String")
        void ofString() {
            assertEquals(TEST_ID, Identifier.toString(TEST_ID));
        }


        @Test
        @DisplayName("wrapped String")
        void ofWrappedString() {

            StringValue id = toMessage(TEST_ID);

            String result = Identifier.toString(id);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("Message with string field")
        void ofMessage() {
            StringValue id = toMessage(TEST_ID);

            String result = Identifier.toString(id);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("Message with nested Message")
        void ofNestedMessage() {
            StringValue value = toMessage(TEST_ID);
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
            StringValue messageToWrap = toMessage(TEST_ID);
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
        Integer number = 256;

        StringValue nestedMessageString = toMessage(nestedString);
        SeveralFieldsId idToConvert = SeveralFieldsId.newBuilder()
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
                .testAllPublicStaticMethods(Identifier.class);
    }

    @Test
    void create_values_depending_on_wrapper_message_type() {
        assertEquals(10, Type.INTEGER.fromMessage(toMessage(10)));
        assertEquals(1024L, Type.LONG.fromMessage(toMessage(1024L)));

        String value = getClass().getSimpleName();
        assertEquals(value, Type.STRING.fromMessage(toMessage(value)));
    }

    @Nested
    @DisplayName("reject unsupported")
    class RejectUnsupported {

        @Test
        @DisplayName("value")
        @SuppressWarnings("UnnecessaryBoxing") // We want to make the unsupported type obvious.
        void value() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Identifier.toString(Boolean.valueOf(true))
            );
        }

        @Test
        @DisplayName("class")
        void clazz() {
            assertThrows(
                    IllegalArgumentException.class,
                    () ->  Identifier.getType(Float.class)
            );
        }

    }
    @Test
    @DisplayName("unpack Any")
    void unpackAny() {
        StringValue id = newUuidValue();
        assertEquals(id.getValue(), Identifier.toString(AnyPacker.pack(id)));
    }

    @Test
    @DisplayName("do not unpack empty Any")
    void rejectUnpackingEmptyAny() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Identifier.unpack(Any.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("reject packing unsupported type")
    @SuppressWarnings("UnnecessaryBoxing") // We want to make the unsupported type obvious.
    void noPackingForUnsupported() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Identifier.pack(Boolean.valueOf(false))
        );
    }

    @Test
    @DisplayName("return NULL_ID when converting null")
    void nullId() {
        assertEquals(NULL_ID, Identifier.toString(null));
    }
}
