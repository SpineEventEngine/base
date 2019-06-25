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

package io.spine.validate.option;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.StringValue;
import io.spine.test.validate.option.ATestMessage;
import io.spine.test.validate.option.ATestMessageConstraint;
import io.spine.test.validate.option.ATestMessageWithConstraint;
import io.spine.test.validate.option.ATestMessageWithExternalConstraintOnly;
import io.spine.test.validate.option.NoValidationTestMessage;
import io.spine.test.validate.option.TestFieldOptionProto;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ExternalConstraints;
import io.spine.validate.FieldValue;
import io.spine.validate.MessageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth8.assertThat;
import static io.spine.testing.TestValues.randomString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
@DisplayName("FieldValidatingOption should")
final class FieldValidatingOptionTest {

    @BeforeAll
    static void beforeClass() {
        MessageType externalConstraint = new MessageType(ATestMessageConstraint.getDescriptor());
        ExternalConstraints.updateFrom(ImmutableSet.of(externalConstraint));
    }

    @DisplayName("return empty value if option is not present in external or field constraints")
    @Test
    void returnEmptyValueIfNotPresent() {
        ATestMessageWithConstraint msg = ATestMessageWithConstraint.getDefaultInstance();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue<StringValue> fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertThat(maxLength.valueFrom(fieldValue.descriptor(), fieldValue.context())).isEmpty();
    }

    @DisplayName("return value if option is present in external constraint only")
    @Test
    void returnValueIfOptionIsPresentInExternalConstraint() {
        FieldValue<StringValue> fieldValue = valueFieldWithExternalConstraint();
        MaxLength maxLength = new MaxLength();
        assertThat(maxLength.valueFrom(fieldValue.descriptor(), fieldValue.context())).isPresent();
    }

    @DisplayName("return value if option is present in field option")
    @Test
    void returnValueIfOptionIsPresentInFieldOption() {
        ATestMessage msg = ATestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue<StringValue> fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertThat(maxLength.valueFrom(fieldValue.descriptor(), fieldValue.context())).isPresent();
    }

    @DisplayName("throw IllegalStateException if a specified option is not as field option")
    @Test
    void throwISEIfOptionIsNotPresentInFieldOption() {
        ATestMessageWithConstraint msg = ATestMessageWithConstraint.getDefaultInstance();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue<StringValue> fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        Assertions.assertThrows(IllegalStateException.class, () -> {
            maxLength.optionValue(fieldValue);
        });
    }

    @DisplayName("not validate field if option is not present in external or field constraints")
    @Test
    void notValidateIfOptionNotPresent() {
        NoValidationTestMessage msg = NoValidationTestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue<StringValue> fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertFalse(maxLength.shouldValidate(fieldValue));
    }

    @DisplayName("validate field if option is present in external constraint")
    @Test
    void validateIfOptionIsPresentInExternalConstraint() {
        FieldValue<StringValue> fieldValue = valueFieldWithExternalConstraint();
        MaxLength maxLength = new MaxLength();
        assertTrue(maxLength.shouldValidate(fieldValue));
    }

    @DisplayName("validate field if option is present in field option")
    @Test
    void validateIfOptionIsPresentInFieldOption() {
        ATestMessage msg = ATestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue<StringValue> fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertTrue(maxLength.shouldValidate(fieldValue));
    }

    private static FieldValue<StringValue> valueFieldWithExternalConstraint() {
        NoValidationTestMessage testMessage = NoValidationTestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        ATestMessageWithExternalConstraintOnly msg = ATestMessageWithExternalConstraintOnly
                .newBuilder()
                .setMessage(testMessage)
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue<NoValidationTestMessage> messageValue = (FieldValue<NoValidationTestMessage>) value
                .valueOf("message")
                .get();
        MessageValue withExternalConstraints = MessageValue
                .nestedIn(messageValue.context(), messageValue.singleValue());
        return valueField(withExternalConstraints);
    }

    private static FieldValue<StringValue> valueField(MessageValue value) {
        return (FieldValue<StringValue>) value
                .valueOf("value")
                .get();
    }

    @Immutable
    private static final class MaxLength
            extends FieldValidatingOption<Integer, StringValue> {

        private MaxLength() {
            super(TestFieldOptionProto.maxLength);
        }

        @Override
        public Constraint<FieldValue<StringValue>> constraintFor(FieldValue<StringValue> value) {
            return new MaxLengthConstraint(optionValue(value));
        }
    }

    @Immutable
    private static final class MaxLengthConstraint
            extends FieldValueConstraint<StringValue, Integer> {

        /**
         * Creates a new instance of this constraint.
         *
         * @param optionValue
         *         a value that describes the field constraints
         */
        private MaxLengthConstraint(int optionValue) {
            super(optionValue);
        }

        @Override
        public ImmutableList<ConstraintViolation> check(FieldValue<StringValue> value) {
            return ImmutableList.of();
        }
    }
}
