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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.spine.code.proto.FieldContext;
import io.spine.test.validate.MessageWithMapBoolField;
import io.spine.test.validate.MessageWithMapByteStringField;
import io.spine.test.validate.MessageWithMapDoubleField;
import io.spine.test.validate.MessageWithMapFloatField;
import io.spine.test.validate.MessageWithMapIntField;
import io.spine.test.validate.MessageWithMapLongField;
import io.spine.test.validate.MessageWithMapMessageField;
import io.spine.test.validate.MessageWithMapStringField;
import io.spine.test.validate.Planet;
import io.spine.test.validate.RequiredByteStringFieldValue;
import io.spine.test.validate.RequiredEnumFieldValue;
import io.spine.test.validate.RequiredMsgFieldValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.Descriptors.FieldDescriptor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

@DisplayName("FieldValidatorFactory should create field validator for a field of type")
class FieldValidatingOptionsTest {

    @Test
    @DisplayName("Message")
    void messageField() {
        FieldDescriptor field = RequiredMsgFieldValue.getDescriptor()
                                                     .getFields()
                                                     .get(0);

        FieldValidator validator = create(field, StringValue.getDefaultInstance());

        assertType(validator, MessageFieldValidator.class);
    }

    @Test
    @DisplayName("int")
    void intField() {
        FieldDescriptor field = Int32Value.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertType(validator, IntegerFieldValidator.class);
    }

    @Test
    @DisplayName("long")
    void longField() {
        FieldDescriptor field = Int64Value.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertType(validator, LongFieldValidator.class);
    }

    @Test
    @DisplayName("float")
    void floatField() {
        FieldDescriptor field = FloatValue.getDescriptor()
                                          .getFields()
                                          .get(0);

        FieldValidator validator = create(field, 0);

        assertType(validator, FloatFieldValidator.class);
    }

    @Test
    @DisplayName("double")
    void doubleField() {
        FieldDescriptor field = DoubleValue.getDescriptor()
                                           .getFields()
                                           .get(0);

        FieldValidator validator = create(field, 0);

        assertType(validator, DoubleFieldValidator.class);
    }

    @Test
    @DisplayName("String")
    void stringField() {
        FieldDescriptor field = StringValue.getDescriptor()
                                           .getFields()
                                           .get(0);

        FieldValidator validator = create(field, "");

        assertType(validator, StringFieldValidator.class);
    }

    @Test
    @DisplayName("ByteString")
    void byteStringField() {
        FieldDescriptor field = RequiredByteStringFieldValue.getDescriptor()
                                                            .getFields()
                                                            .get(0);

        FieldValidator validator = create(field, ByteString.EMPTY);

        assertType(validator, ByteStringFieldValidator.class);
    }

    @Test
    @DisplayName("enum")
    void enumField() {
        FieldDescriptor field = RequiredEnumFieldValue.getDescriptor()
                                                      .getFields()
                                                      .get(0);

        FieldValidator validator = create(field, Planet.MERCURY);

        assertType(validator, EnumFieldValidator.class);
    }

    @Test
    @DisplayName("boolean")
    void booleanField() {
        FieldDescriptor field = BoolValue.getDescriptor()
                                         .getFields()
                                         .get(0);

        FieldValidator validator = create(field, BoolValue.of(true));

        assertType(validator, BooleanFieldValidator.class);
    }

    @Test
    @DisplayName("repeated")
    void repeatedField() {
        FieldDescriptor field = FieldMask.getDescriptor()
                                         .getFields()
                                         .get(0);

        FieldValidator<?> validator = create(field, ImmutableList.<String>of());

        assertType(validator, StringFieldValidator.class);
    }

    @Test
    @DisplayName("map of String")
    void mapField() {
        FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", "value"));

        assertType(validator, StringFieldValidator.class);
    }

    @Test
    @DisplayName("empty map")
    void emptyMapField() {
        FieldDescriptor field = MessageWithMapStringField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.<String, String>of());

        assertType(validator, StringFieldValidator.class);
    }

    @Test
    @DisplayName("bytes map")
    void bytesMapField() {
        FieldDescriptor field = MessageWithMapByteStringField.getDescriptor()
                                                             .getFields()
                                                             .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", ByteString.EMPTY));

        assertType(validator, ByteStringFieldValidator.class);
    }

    @Test
    @DisplayName("Message map")
    void messageMapField() {
        FieldDescriptor field = MessageWithMapMessageField.getDescriptor()
                                                          .getFields()
                                                          .get(0);
        FieldValidator<?> validator = create(field,
                                             ImmutableMap.of("key", Any.getDefaultInstance()));

        assertType(validator, MessageFieldValidator.class);
    }

    @Test
    @DisplayName("int map")
    void integerMapField() {
        FieldDescriptor field = MessageWithMapIntField.getDescriptor()
                                                      .getFields()
                                                      .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", 0));

        assertType(validator, IntegerFieldValidator.class);
    }

    @Test
    @DisplayName("long map")
    void longMapField() {
        FieldDescriptor field = MessageWithMapLongField.getDescriptor()
                                                       .getFields()
                                                       .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", 1L));

        assertType(validator, LongFieldValidator.class);
    }

    @Test
    @DisplayName("float map")
    void floatMapField() {
        FieldDescriptor field = MessageWithMapFloatField.getDescriptor()
                                                        .getFields()
                                                        .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", 0.0f));

        assertType(validator, FloatFieldValidator.class);
    }

    @Test
    @DisplayName("double map")
    void doubleMapField() {
        FieldDescriptor field = MessageWithMapDoubleField.getDescriptor()
                                                         .getFields()
                                                         .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", 0.0));

        assertType(validator, DoubleFieldValidator.class);
    }

    @Test
    @DisplayName("bool map")
    void boolMap() {
        FieldDescriptor field = MessageWithMapBoolField.getDescriptor()
                                                       .getFields()
                                                       .get(0);
        FieldValidator<?> validator = create(field, ImmutableMap.of("key", true));

        assertThat(validator, instanceOf(BooleanFieldValidator.class));
    }

    @SuppressWarnings("Immutable")
    private static <@ImmutableTypeParameter T>
    FieldValidator<?> create(FieldDescriptor fieldDescriptor, T value) {
        FieldContext context = FieldContext.create(fieldDescriptor);
        FieldValue wrappedValue = FieldValue.of(value, context);
        return wrappedValue.createValidator();
    }

    private static
    void assertType(FieldValidator validator, Class<? extends FieldValidator> expectedClass) {
        Truth.assertThat(validator).isInstanceOf(expectedClass);
    }
}
