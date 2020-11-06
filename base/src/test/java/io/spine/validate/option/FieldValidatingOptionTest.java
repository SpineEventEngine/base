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

package io.spine.validate.option;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Message;
import io.spine.code.proto.FieldContext;
import io.spine.test.validate.option.ATestMessage;
import io.spine.test.validate.option.ATestMessageConstraint;
import io.spine.test.validate.option.ATestMessageWithConstraint;
import io.spine.test.validate.option.ATestMessageWithExternalConstraintOnly;
import io.spine.test.validate.option.NoValidationTestMessage;
import io.spine.test.validate.option.TestFieldOptionProto;
import io.spine.type.MessageType;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.CustomConstraint;
import io.spine.validate.ExternalConstraints;
import io.spine.validate.FieldValue;
import io.spine.validate.MessageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth8.assertThat;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.TestValues.randomString;
import static io.spine.validate.MessageValue.atTopLevel;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`FieldValidatingOption` should")
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
        FieldValue fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertThat(maxLength.valueFrom(fieldValue.context())).isEmpty();
    }

    @DisplayName("return value if option is present in external constraint only")
    @Test
    void returnValueIfOptionIsPresentInExternalConstraint() {
        FieldValue fieldValue = valueFieldWithExternalConstraint();
        MaxLength maxLength = new MaxLength();
        assertThat(maxLength.valueFrom(fieldValue.context())).isPresent();
    }

    @DisplayName("return value if option is present in field option")
    @Test
    void returnValueIfOptionIsPresentInFieldOption() {
        ATestMessage msg = ATestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertThat(maxLength.valueFrom(fieldValue.context())).isPresent();
    }

    @DisplayName("throw `IllegalStateException` if a specified option is not a field option")
    @Test
    void throwISEIfOptionIsNotPresentInFieldOption() {
        ATestMessageWithConstraint msg = ATestMessageWithConstraint.getDefaultInstance();
        MessageValue value = atTopLevel(msg);
        FieldValue fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertIllegalState(() -> maxLength.optionValue(fieldValue.context()));
    }

    @DisplayName("not validate field if option is not present in external or field constraints")
    @Test
    void notValidateIfOptionNotPresent() {
        NoValidationTestMessage msg = NoValidationTestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertFalse(maxLength.shouldValidate(fieldValue.context()));
    }

    @DisplayName("validate field if option is present in external constraint")
    @Test
    void validateIfOptionIsPresentInExternalConstraint() {
        FieldValue fieldValue = valueFieldWithExternalConstraint();
        MaxLength maxLength = new MaxLength();
        assertTrue(maxLength.shouldValidate(fieldValue.context()));
    }

    @DisplayName("validate field if option is present in field option")
    @Test
    void validateIfOptionIsPresentInFieldOption() {
        ATestMessage msg = ATestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue fieldValue = valueField(value);
        MaxLength maxLength = new MaxLength();
        assertTrue(maxLength.shouldValidate(fieldValue.context()));
    }

    private static FieldValue valueFieldWithExternalConstraint() {
        NoValidationTestMessage testMessage = NoValidationTestMessage
                .newBuilder()
                .setValue(randomString())
                .build();
        ATestMessageWithExternalConstraintOnly msg = ATestMessageWithExternalConstraintOnly
                .newBuilder()
                .setMessage(testMessage)
                .build();
        MessageValue value = MessageValue.atTopLevel(msg);
        FieldValue messageValue = value
                .valueOf("message")
                .orElseGet(Assertions::fail);
        MessageValue withExternalConstraints = MessageValue
                .nestedIn(messageValue.context(), (Message) messageValue.singleValue());
        return valueField(withExternalConstraints);
    }

    private static FieldValue valueField(MessageValue value) {
        return value
                .valueOf("value")
                .orElseGet(Assertions::fail);
    }

    @Immutable
    private static final class MaxLength
            extends FieldValidatingOption<Integer> {

        private MaxLength() {
            super(TestFieldOptionProto.maxLength);
        }

        @Override
        public Constraint constraintFor(FieldContext field) {
            return new MaxLengthConstraint(optionValue(field), field);
        }
    }

    @Immutable
    private static final class MaxLengthConstraint
            extends FieldConstraint<Integer> implements CustomConstraint {

        /**
         * Creates a new instance of this constraint.
         *
         * @param optionValue
         *         a value that describes the field constraints
         */
        private MaxLengthConstraint(int optionValue, FieldContext field) {
            super(optionValue, field.targetDeclaration());
        }

        @Override
        public String errorMessage(FieldContext field) {
            return format("Value of `%s` must not be longer than `%d`.",
                          field.targetDeclaration(),
                          optionValue());
        }

        @Override
        public ImmutableList<ConstraintViolation> validate(MessageValue containingMessage) {
            FieldValue value = containingMessage.valueOf(field());
            int maxLength = optionValue();
            FieldContext context = value.context();
            return value.nonDefault()
                        .filter(val -> val.toString().length() > maxLength)
                        .map(val -> ConstraintViolation.newBuilder()
                                                       .setFieldPath(context.fieldPath())
                                                       .setTypeName(containingMessage.declaration()
                                                                                     .name()
                                                                                     .value())
                                                       .setMsgFormat(errorMessage(context))
                                                       .build())
                        .collect(toImmutableList());
        }
    }
}
