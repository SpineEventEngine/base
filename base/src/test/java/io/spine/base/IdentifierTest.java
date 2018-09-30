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
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Identifier.Type;
import io.spine.protobuf.AnyPacker;
import io.spine.test.identifiers.NestedMessageId;
import io.spine.test.identifiers.SeveralFieldsId;
import io.spine.test.identifiers.TimestampFieldId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.EMPTY_ID;
import static io.spine.base.Identifier.NULL_ID;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.TestValues.newUuidValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Identifier should")
class IdentifierTest {

    private static final String TEST_ID = "someTestId 1234567890 !@#$%^&()[]{}-+=_";

    @SuppressWarnings("UnnecessaryBoxing") // We want to make the unsupported type obvious.
    @Test
    void reject_objects_of_unsupported_class_passed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Identifier.toString(Boolean.valueOf(true))
        );
    }

    @Test
    void reject_unsupported_classes() {
        assertThrows(
                IllegalArgumentException.class,
                () ->  Identifier.getType(Float.class)
        );
    }

    @SuppressWarnings("UnnecessaryBoxing") // OK as we want to show types clearly.
    @Test
    void convert_to_string_number_ids() {
        assertEquals("10", Identifier.toString(Integer.valueOf(10)));
        assertEquals("100", Identifier.toString(Long.valueOf(100)));
    }

    @Test
    void unpack_passed_Any() {
        StringValue id = newUuidValue();
        assertEquals(id.getValue(), Identifier.toString(AnyPacker.pack(id)));
    }

    @Test
    void generate_new_UUID() {
        // We have non-empty values.
        assertTrue(newUuid().length() > 0);

        // Values are random.
        assertNotEquals(newUuid(), newUuid());
    }

    @SuppressWarnings("UnnecessaryBoxing") // We want to make the unsupported type obvious.
    @Test
    void do_not_convert_unsupported_ID_type_to_Any() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Identifier.pack(Boolean.valueOf(false))
        );
    }

    @Test
    void return_NULL_ID_if_convert_null_to_string() {
        assertEquals(NULL_ID, Identifier.toString(null));
    }

    @Test
    void return_EMPTY_ID_if_convert_empty_string_to_string() {
        assertEquals(EMPTY_ID, Identifier.toString(""));
    }

    @Test
    void return_EMPTY_ID_if_result_of_Message_to_string_conversion_is_empty_string() {
        assertEquals(EMPTY_ID, Identifier.toString(TimestampFieldId.getDefaultInstance()));
    }

    @Test
    void return_EMPTY_ID_if_convert_empty_message_to_string() {
        assertEquals(EMPTY_ID, Identifier.toString(StringValue.getDefaultInstance()));
    }

    @Test
    void return_string_id_as_is() {
        assertEquals(TEST_ID, Identifier.toString(TEST_ID));
    }

    @Test
    void return_same_string_when_convert_string_wrapped_into_message() {

        StringValue id = toMessage(TEST_ID);

        String result = Identifier.toString(id);

        assertEquals(TEST_ID, result);
    }

    @Test
    void convert_to_string_integer_id_wrapped_into_message() {
        Integer value = 1024;
        Int32Value id = toMessage(value);
        String expected = value.toString();

        String actual = Identifier.toString(id);

        assertEquals(expected, actual);
    }

    @Test
    void convert_to_string_long_id_wrapped_into_message() {
        Long value = 100500L;
        Int64Value id = toMessage(value);
        String expected = value.toString();

        String actual = Identifier.toString(id);

        assertEquals(expected, actual);
    }

    @Test
    void convert_to_string_message_id_with_string_field() {
        StringValue id = toMessage(TEST_ID);

        String result = Identifier.toString(id);

        assertEquals(TEST_ID, result);
    }

    @Test
    void convert_to_string_message_id_with_message_field() {
        StringValue value = toMessage(TEST_ID);
        NestedMessageId idToConvert = NestedMessageId.newBuilder()
                                                     .setId(value)
                                                     .build();

        String result = Identifier.toString(idToConvert);

        assertEquals(TEST_ID, result);
    }

    @Test
    void have_default_to_string_conversion_of_message_id_with_several_fields() {
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
    void convert_to_string_message_id_wrapped_in_Any() {
        StringValue messageToWrap = toMessage(TEST_ID);
        Any any = AnyPacker.pack(messageToWrap);

        String result = Identifier.toString(any);

        assertEquals(TEST_ID, result);
    }

    @Test
    void pass_the_null_tolerance_check() {
        new NullPointerTester()
                .testAllPublicStaticMethods(Identifier.class);
    }

    @Test
    void getDefaultValue_by_class_id() {
        assertEquals(0L, Identifier.getDefaultValue(Long.class)
                                   .longValue());
        assertEquals(0, Identifier.getDefaultValue(Integer.class)
                                  .intValue());
        assertEquals("", Identifier.getDefaultValue(String.class));
        assertEquals(Timestamp.getDefaultInstance(), Identifier.getDefaultValue(Timestamp.class));
    }

    @Test
    void create_values_by_type() {
        assertTrue(Identifier.from("")
                             .isString());
        assertTrue(Identifier.from(0)
                             .isInteger());
        assertTrue(Identifier.from(0L)
                             .isLong());
        assertTrue(Identifier.from(toMessage(300))
                             .isMessage());
    }

    @Test
    void recognize_type_by_supported_message_type() {
        assertTrue(Type.INTEGER.matchMessage(toMessage(10)));
        assertTrue(Type.LONG.matchMessage(toMessage(1020L)));
        assertTrue(Type.STRING.matchMessage(toMessage("")));
        assertTrue(Type.MESSAGE.matchMessage(Timestamp.getDefaultInstance()));

        assertFalse(Type.MESSAGE.matchMessage(StringValue.getDefaultInstance()));
        assertFalse(Type.MESSAGE.matchMessage(Int32Value.getDefaultInstance()));
        assertFalse(Type.MESSAGE.matchMessage(Int64Value.getDefaultInstance()));
    }

    @Test
    void create_values_depending_on_wrapper_message_type() {
        assertEquals(10, Type.INTEGER.fromMessage(toMessage(10)));
        assertEquals(1024L, Type.LONG.fromMessage(toMessage(1024L)));

        String value = getClass().getSimpleName();
        assertEquals(value, Type.STRING.fromMessage(toMessage(value)));
    }

    @Test
    void not_unpack_empty_Any() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Identifier.unpack(Any.getDefaultInstance())        
        );
    }

    @Test
    void fail_to_unpack_ID_of_wrong_type() {
        String id = "abcdef";
        Any packed = Identifier.pack(id);

        assertThrows(
                ClassCastException.class,
                () -> {
                    @SuppressWarnings({"RedundantSuppression", "unused"})
                    // Required to invoke the cast.
                    Message wrong = Identifier.unpack(packed);            
                }
        );
    }
}
