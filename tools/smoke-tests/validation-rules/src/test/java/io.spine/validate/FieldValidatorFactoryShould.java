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

package io.spine.validate;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.spine.test.validate.msg.MessageWithMapBoolField;
import io.spine.test.validate.msg.MessageWithMapByteStringField;
import io.spine.test.validate.msg.MessageWithMapDoubleField;
import io.spine.test.validate.msg.MessageWithMapFloatField;
import io.spine.test.validate.msg.MessageWithMapIntField;
import io.spine.test.validate.msg.MessageWithMapLongField;
import io.spine.test.validate.msg.MessageWithMapMessageField;
import io.spine.test.validate.msg.MessageWithMapStringField;
import io.spine.test.validate.msg.RequiredByteStringFieldValue;
import io.spine.test.validate.msg.RequiredEnumFieldValue;
import io.spine.test.validate.msg.RequiredMsgFieldValue;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.protobuf.Descriptors.FieldDescriptor;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Alexander Litus
 */
public class FieldValidatorFactoryShould {

    @Test
    public void create_message_field_validator() {
        FieldDescriptor field = RequiredMsgFieldValue.getDescriptor()
                                                     .getFields()
                                                     .get(0);

        FieldValidator validator = create(field,
                                          StringValue.getDefaultInstance());

        assertThat(validator, instanceOf(MessageFieldValidator.class));
    }

    @Test
    public void create_integer_field_validator() {
        FieldDescriptor field = Int32Value.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(IntegerFieldValidator.class));
    }

    @Test
    public void create_long_field_validator() {
        FieldDescriptor field = Int64Value.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(LongFieldValidator.class));
    }

    @Test
    public void create_float_field_validator() {
        FieldDescriptor field = FloatValue.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(FloatFieldValidator.class));
    }

    @Test
    public void create_double_field_validator() {
        FieldDescriptor field = DoubleValue.getDescriptor()
                                           .getFields()
                                           .get(0);

        FieldValidator validator = create(field, 0);

        assertThat(validator, instanceOf(DoubleFieldValidator.class));
    }

    @Test
    public void create_String_field_validator() {
        FieldDescriptor field = StringValue.getDescriptor()
                                           .getFields()
                                           .get(0);

        FieldValidator validator = create(field, "");

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    public void create_ByteString_field_validator() {
        FieldDescriptor field = RequiredByteStringFieldValue.getDescriptor()
                                                            .getFields()
                                                            .get(0);

        FieldValidator validator = create(field, new Object());

        assertThat(validator, instanceOf(ByteStringFieldValidator.class));
    }

    @Test
    public void create_Enum_field_validator() {
        FieldDescriptor field = RequiredEnumFieldValue.getDescriptor()
                                                      .getFields()
                                                      .get(0);

        FieldValidator validator = create(field, new Object());

        assertThat(validator, instanceOf(EnumFieldValidator.class));
    }

    @Test
    public void create_Boolean_field_validator() {
        FieldDescriptor field = BoolValue.getDescriptor()
                                         .getFields()
                                         .get(0);

        FieldValidator validator = create(field, new Object());

        assertThat(validator, instanceOf(BooleanFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_repeated_field() {
        FieldDescriptor field = FieldMask.getDescriptor()
                                         .getFields()
                                         .get(0);

        FieldValidator<?> validator = create(field, emptyList());

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_map_String_field() {
        FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", "value"));

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_empty_map_field() {
        FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field, emptyMap());

        assertThat(validator, instanceOf(EmptyMapFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_bytes_map_field() {
        FieldDescriptor field = MessageWithMapByteStringField.getDescriptor()
                                                             .getFields()
                                                             .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", ByteString.EMPTY));

        assertThat(validator, instanceOf(ByteStringFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_Message_map_field() {
        FieldDescriptor field = MessageWithMapMessageField.getDescriptor()
                                                          .getFields()
                                                          .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", Any.getDefaultInstance()));

        assertThat(validator, instanceOf(MessageFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_int_map_field() {
        FieldDescriptor field = MessageWithMapIntField.getDescriptor()
                                                      .getFields()
                                                      .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", 0));

        assertThat(validator, instanceOf(IntegerFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_long_map_field() {
        FieldDescriptor field = MessageWithMapLongField.getDescriptor()
                                                       .getFields()
                                                       .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", 1L));

        assertThat(validator, instanceOf(LongFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_float_map_field() {
        FieldDescriptor field = MessageWithMapFloatField.getDescriptor()
                                                        .getFields()
                                                        .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", 0.0f));

        assertThat(validator, instanceOf(FloatFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_double_map_field() {
        FieldDescriptor field = MessageWithMapDoubleField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", 0.0));

        assertThat(validator, instanceOf(DoubleFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_bool_map_field() {
        FieldDescriptor field = MessageWithMapBoolField.getDescriptor()
                                                       .getFields()
                                                       .get(0);
        FieldValidator<?> validator = create(field,
                                             of("key", true));

        assertThat(validator, instanceOf(BooleanFieldValidator.class));
    }

    private static FieldValidator<?> create(FieldDescriptor fieldDescriptor, Object value) {
        FieldContext context = FieldContext.create(fieldDescriptor);
        return FieldValidatorFactory.create(context, value);
    }
}
