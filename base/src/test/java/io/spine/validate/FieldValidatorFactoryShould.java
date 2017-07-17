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

package io.spine.validate;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.spine.base.FieldPath;
import io.spine.test.validate.msg.MessageWithMapStringField;
import io.spine.test.validate.msg.MessageWithMapByteStringField;
import io.spine.test.validate.msg.RequiredByteStringFieldValue;
import io.spine.test.validate.msg.RequiredEnumFieldValue;
import io.spine.test.validate.msg.RequiredMsgFieldValue;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.protobuf.Descriptors.FieldDescriptor;
import static io.spine.validate.FieldValidatorFactory.create;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Alexander Litus
 */
public class FieldValidatorFactoryShould {

    private static final FieldPath FIELD_PATH = FieldPath.getDefaultInstance();

    @Test
    public void create_message_field_validator() {
        final FieldDescriptor field = RequiredMsgFieldValue.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field,
                                                StringValue.getDefaultInstance(),
                                                FIELD_PATH);

        assertThat(validator, instanceOf(MessageFieldValidator.class));
    }

    @Test
    public void create_integer_field_validator() {
        final FieldDescriptor field = Int32Value.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, 0, FIELD_PATH);

        assertThat(validator, instanceOf(IntegerFieldValidator.class));
    }

    @Test
    public void create_long_field_validator() {
        final FieldDescriptor field = Int64Value.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, 0, FIELD_PATH);

        assertThat(validator, instanceOf(LongFieldValidator.class));
    }

    @Test
    public void create_float_field_validator() {
        final FieldDescriptor field = FloatValue.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, 0, FIELD_PATH);

        assertThat(validator, instanceOf(FloatFieldValidator.class));
    }

    @Test
    public void create_double_field_validator() {
        final FieldDescriptor field = DoubleValue.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, 0, FIELD_PATH);

        assertThat(validator, instanceOf(DoubleFieldValidator.class));
    }

    @Test
    public void create_String_field_validator() {
        final FieldDescriptor field = StringValue.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, "", FIELD_PATH);

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    public void create_ByteString_field_validator() {
        final FieldDescriptor field = RequiredByteStringFieldValue.getDescriptor()
                                                                  .getFields()
                                                                  .get(0);

        final FieldValidator validator = create(field, new Object(), FIELD_PATH);

        assertThat(validator, instanceOf(ByteStringFieldValidator.class));
    }

    @Test
    public void create_Enum_field_validator() {
        final FieldDescriptor field = RequiredEnumFieldValue.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, new Object(), FIELD_PATH);

        assertThat(validator, instanceOf(EnumFieldValidator.class));
    }

    @Test
    public void create_Boolean_field_validator() {
        final FieldDescriptor field = BoolValue.getDescriptor().getFields().get(0);

        final FieldValidator validator = create(field, new Object(), FIELD_PATH);

        assertThat(validator, instanceOf(BooleanFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_repeated_field() {
        final FieldDescriptor field = FieldMask.getDescriptor().getFields().get(0);

        final FieldValidator<?> validator = create(field, emptyList(), FIELD_PATH);

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_map_String_field() {
        final FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                               .getFields()
                                                               .get(0);
        final FieldValidator<?> validator = create(field,
                                                   of("key", "value"),
                                                   FIELD_PATH);

        assertThat(validator, instanceOf(StringFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_empty_map_field() {
        final FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                               .getFields()
                                                               .get(0);
        final FieldValidator<?> validator = create(field, emptyMap(), FIELD_PATH);

        assertThat(validator, instanceOf(EmptyMapFieldValidator.class));
    }

    @Test
    public void create_field_validator_for_bytes_map_field() {
        final FieldDescriptor field = MessageWithMapByteStringField.getDescriptor()
                                                                   .getFields()
                                                                   .get(0);
        final FieldValidator<?> validator = create(field,
                                                   of("key", ByteString.EMPTY),
                                                   FIELD_PATH);

        assertThat(validator, instanceOf(ByteStringFieldValidator.class));
    }
}
