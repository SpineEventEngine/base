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
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.option.IfInvalidOption;
import io.spine.protobuf.AnyPacker;
import io.spine.type.TypeName;
import io.spine.validate.option.FieldValidatingOption;
import io.spine.validate.option.Valid;
import io.spine.validate.option.ValidatingOptionFactory;

import java.util.List;
import java.util.Set;

import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.validate.Validate.isDefault;

/**
 * Validates fields of type {@link Message}, as opposed to primitive
 * Protobuf fields.
 */
final class MessageFieldValidator extends FieldValidator<Message> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     * @param assumeRequired
     *         if {@code true} the validator would assume that the field is required even if
     *         such constraint is not explicitly set
     */
    MessageFieldValidator(FieldValue<Message> fieldValue, boolean assumeRequired) {
        super(fieldValue, assumeRequired);
    }

    @Override
    public ImmutableList<ConstraintViolation> validate() {
        boolean validateFields = shouldValidateFields();
        if (validateFields) {
            validateFields();
            BuiltInValidation.ANY.validateIfApplies(this);
        }
        return super.validate();
    }

    private boolean shouldValidateFields() {
        return validOptionValue() && fieldValueIsSet();
    }

    private boolean validOptionValue() {
        Valid validOption = new Valid();
        boolean valid = validOption.valueFrom(descriptor())
                                   .orElse(false);
        return valid;
    }

    private boolean fieldValueIsSet() {
        return !fieldValueNotSet();
    }

    @Override
    protected boolean isNotSet(Message value) {
        boolean result = isDefault(value);
        return result;
    }

    @Override
    protected Set<FieldValidatingOption<?, Message>> createMoreOptions(
            ValidatingOptionFactory factory) {
        return factory.forMessage();
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass") // Proper encapsulation here.
    private boolean isOfType(Class<? extends Message> type) {
        ImmutableList<Message> values = values();
        Message value = values.isEmpty()
                        ? null
                        : values.get(0);
        boolean result = type.isInstance(value);
        return result;
    }

    private void validateFields() {
        for (Message value : values()) {
            validateSingle(value);
        }
    }

    private void validateAny() {
        for (Message value : values()) {
            Any any = (Any) value;
            Message unpacked = AnyPacker.unpack(any);
            validateSingle(unpacked);
        }
    }

    private void validateSingle(Message message) {
        MessageValidator validator = MessageValidator.newInstance(message, fieldContext());
        List<ConstraintViolation> violations = validator.validate();
        if (!violations.isEmpty()) {
            addViolation(newValidViolation(message, violations));
        }
    }

    private ConstraintViolation newValidViolation(Message fieldValue,
                                                  Iterable<ConstraintViolation> violations) {
        IfInvalidOption ifInvalid = ifInvalid();
        String msg = errorMsgFormat(ifInvalid, ifInvalid.getMsgFormat());
        TypeName validatedType = field().declaringType()
                                        .name();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .setFieldPath(fieldPath())
                .setFieldValue(pack(fieldValue))
                .addAllViolation(violations)
                .setTypeName(validatedType.value())
                .build();
        return violation;
    }

    /**
     * The enumeration of pre-defined custom validations for a message field.
     */
    private enum BuiltInValidation {

        /**
         * Custom validation strategy for an {@link Any} field.
         */
        ANY(Any.class) {
            @Override
            void doValidate(MessageFieldValidator validator) {
                validator.validateAny();
            }
        };

        private final Class<? extends Message> targetType;

        BuiltInValidation(Class<? extends Message> type) {
            this.targetType = type;
        }

        /**
         * Validates the field with the given {@code validator} if the field is of
         * the {@code targetType}.
         */
        private void validateIfApplies(MessageFieldValidator validator) {
            if (validator.isOfType(targetType)) {
                doValidate(validator);
            }
        }

        abstract void doValidate(MessageFieldValidator validator);
    }
}
