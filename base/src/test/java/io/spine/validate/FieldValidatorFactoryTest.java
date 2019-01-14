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

package io.spine.validate;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.spine.test.validate.MessageWithMapBoolField;
import io.spine.test.validate.MessageWithMapByteStringField;
import io.spine.test.validate.MessageWithMapDoubleField;
import io.spine.test.validate.MessageWithMapFloatField;
import io.spine.test.validate.MessageWithMapIntField;
import io.spine.test.validate.MessageWithMapLongField;
import io.spine.test.validate.MessageWithMapMessageField;
import io.spine.test.validate.MessageWithMapStringField;
import io.spine.test.validate.RequiredByteStringFieldValue;
import io.spine.test.validate.RequiredEnumFieldValue;
import io.spine.test.validate.RequiredMsgFieldValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.Descriptors.FieldDescriptor;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

@DisplayName("FieldValidatorFactory should")
class FieldValidatorFactoryTest {

    @Test
    @DisplayName("create message field validator")
    void create_message_field_validator() {
        FieldDescriptor field = RequiredMsgFieldValue.getDescriptor()
                                                     .getFields()
                                                     .get(0);

        FieldValidator validator = create(field,
                                          StringValue.getDefaultInstance());

        assertThat(validator, instanceOf(MessageFieldValidator.class));
    }

    @Test
    @DisplayName("create integer field validator")
    void create_integer_field_validator() {
        FieldDescriptor field = Int32Value.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(IntegerFieldValidator.class));
    }

    @Test
    @DisplayName("create long field validator")
    void create_long_field_validator() {
        FieldDescriptor field = Int64Value.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(LongFieldValidator.class));
    }

    @Test
    @DisplayName("create float field validator")
    void create_float_field_validator() {
        FieldDescriptor field = FloatValue.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(FloatFieldValidator.class));
    }

    @Test
    @DisplayName("create double field validator")
    void create_double_field_validator() {
        FieldDescriptor field = DoubleValue.getDescriptor()
                                           .getFields()
                                           .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(DoubleFieldValidator.class));
    }

    @Test
    @DisplayName("create String field validator")
    void create_String_field_validator() {
        FieldDescriptor field = StringValue.getDescriptor()
                                           .getFields()
                                           .get(0);

        FieldValidator validator = create(field, "");

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    @DisplayName("create ByteString field validator")
    void create_ByteString_field_validator() {
        FieldDescriptor field = RequiredByteStringFieldValue.getDescriptor()
                                                            .getFields()
                                                            .get(0);

        FieldValidator validator = create(field, new Object());

        assertThat(validator, instanceOf(ByteStringFieldValidator.class));
    }

    @Test
    @DisplayName("create Enum field validator")
    void create_Enum_field_validator() {
        FieldDescriptor field = RequiredEnumFieldValue.getDescriptor()
                                                      .getFields()
                                                      .get(0);

        FieldValidator validator = create(field, new Object());

        assertThat(validator, instanceOf(EnumFieldValidator.class));
    }

    @Test
    @DisplayName("create Boolean field validator")
    void create_Boolean_field_validator() {
        FieldDescriptor field = BoolValue.getDescriptor()
                                         .getFields()
                                         .get(0);

        FieldValidator validator = create(field, new Object());

        assertThat(validator, instanceOf(BooleanFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for repeated field")
    void create_field_validator_for_repeated_field() {
        FieldDescriptor field = FieldMask.getDescriptor()
                                         .getFields()
                                         .get(0);

        FieldValidator<?> validator = create(field, emptyList());

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for map String field")
    void create_field_validator_for_map_String_field() {
        FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", "value"));

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for empty map field")
    void create_field_validator_for_empty_map_field() {
        FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field, emptyMap());

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for bytes map field")
    void create_field_validator_for_bytes_map_field() {
        FieldDescriptor field = MessageWithMapByteStringField.getDescriptor()
                                                             .getFields()
                                                             .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", ByteString.EMPTY));

        assertThat(validator, instanceOf(ByteStringFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for Message map field")
    void create_field_validator_for_Message_map_field() {
        FieldDescriptor field = MessageWithMapMessageField.getDescriptor()
                                                          .getFields()
                                                          .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", Any.getDefaultInstance()));

        assertThat(validator, instanceOf(MessageFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for int map field")
    void create_field_validator_for_int_map_field() {
        FieldDescriptor field = MessageWithMapIntField.getDescriptor()
                                                      .getFields()
                                                      .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", 0));

        assertThat(validator, instanceOf(IntegerFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for long map field")
    void create_field_validator_for_long_map_field() {
        FieldDescriptor field = MessageWithMapLongField.getDescriptor()
                                                       .getFields()
                                                       .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", 1L));

        assertThat(validator, instanceOf(LongFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for float map field")
    void create_field_validator_for_float_map_field() {
        FieldDescriptor field = MessageWithMapFloatField.getDescriptor()
                                                        .getFields()
                                                        .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", 0.0f));

        assertThat(validator, instanceOf(FloatFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for double map field")
    void create_field_validator_for_double_map_field() {
        FieldDescriptor field = MessageWithMapDoubleField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", 0.0));

        assertThat(validator, instanceOf(DoubleFieldValidator.class));
    }

    @Test
    @DisplayName("create field validator for bool map field")
    void create_field_validator_for_bool_map_field() {
        FieldDescriptor field = MessageWithMapBoolField.getDescriptor()
                                                       .getFields()
                                                       .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", true));

        assertThat(validator, instanceOf(BooleanFieldValidator.class));
    }

    private static FieldValidator<?> create(FieldDescriptor fieldDescriptor, Object value) {
        FieldContext context = FieldContext.create(fieldDescriptor);
        FieldValue wrappedValue = FieldValue.of(value, context);
        return wrappedValue.createValidator();
    }
}
