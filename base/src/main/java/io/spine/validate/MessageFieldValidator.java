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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.option.IfInvalidOption;
import io.spine.option.OptionsProto;
import io.spine.option.Time;
import io.spine.option.TimeOption;
import io.spine.protobuf.AnyPacker;

import java.util.List;

import static io.spine.base.Time.getCurrentTime;
import static io.spine.option.Time.FUTURE;
import static io.spine.option.Time.TIME_UNDEFINED;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.protobuf.Timestamps2.isLaterThan;
import static io.spine.validate.Validate.isDefault;

/**
 * Validates fields of type {@link Message}.
 */
class MessageFieldValidator extends FieldValidator<Message> {

    private final TimeOption timeConstraint;

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     * @param assumeRequired
     *         if {@code true} the validator would assume that the field is required even if
     *         such constraint is not explicitly set
     */
    MessageFieldValidator(FieldValue fieldValue, boolean assumeRequired) {
        super(fieldValue, assumeRequired, true);
        this.timeConstraint = fieldValue.valueOf(OptionsProto.when);
    }

    @Override
    protected void validateOwnRules() {
        boolean validateFields = shouldValidateFields();
        if (validateFields) {
            validateFields();
            BuiltInValidation.ANY.validateIfApplies(this);
        }
        BuiltInValidation.TIMESTAMP.validateIfApplies(this);
    }

    private boolean shouldValidateFields() {
        return getValidateOption() && !fieldValueNotSet();
    }

    @Override
    protected boolean isNotSet(Message value) {
        boolean result = isDefault(value);
        return result;
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass") // Proper encapsulation here.
    private boolean isOfType(Class<? extends Message> type) {
        ImmutableList<Message> values = getValues();
        Message value = values.isEmpty()
                        ? null
                        : values.get(0);
        boolean result = type.isInstance(value);
        return result;
    }

    private void validateFields() {
        for (Message value : getValues()) {
            validateSingle(value);
        }
    }

    private void validateAny() {
        for (Message value : getValues()) {
            Any any = (Any) value;
            Message unpacked = AnyPacker.unpack(any);
            validateSingle(unpacked);
        }
    }

    private void validateSingle(Message message) {
        MessageValidator validator = MessageValidator.newInstance(message, getFieldContext());
        List<ConstraintViolation> violations = validator.validate();
        if (!violations.isEmpty()) {
            addViolation(newValidViolation(message, violations));
        }
    }

    private void validateTimestamps() {
        Time when = timeConstraint.getIn();
        if (when == TIME_UNDEFINED) {
            return;
        }
        Timestamp now = getCurrentTime();
        for (Message value : getValues()) {
            Timestamp time = (Timestamp) value;
            if (isTimeInvalid(time, when, now)) {
                addViolation(newTimeViolation(time));
                return; // return because one error message is enough for the "time" option
            }
        }
    }

    /**
     * Checks the time.
     *
     * @param timeToCheck
     *         a timestamp to check
     * @param whenExpected
     *         the time when the checked timestamp should be
     * @param now
     *         the current moment
     * @return {@code true} if the time is valid according to {@code whenExpected} parameter,
     *         {@code false} otherwise
     */
    private static boolean isTimeInvalid(Timestamp timeToCheck, Time whenExpected, Timestamp now) {
        boolean isValid = (whenExpected == FUTURE)
                          ? isLaterThan(timeToCheck, /*than*/ now)
                          : isLaterThan(now, /*than*/ timeToCheck);
        boolean isInvalid = !isValid;
        return isInvalid;
    }

    private ConstraintViolation newTimeViolation(Timestamp fieldValue) {
        String msg = getErrorMsgFormat(timeConstraint, timeConstraint.getMsgFormat());
        String when = timeConstraint.getIn()
                                    .toString()
                                    .toLowerCase();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(when)
                .setFieldPath(getFieldPath())
                .setFieldValue(pack(fieldValue))
                .build();
        return violation;
    }

    private ConstraintViolation newValidViolation(Message fieldValue,
                                                  Iterable<ConstraintViolation> violations) {
        IfInvalidOption ifInvalid = ifInvalid();
        String msg = getErrorMsgFormat(ifInvalid, ifInvalid.getMsgFormat());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .setFieldPath(getFieldPath())
                .setFieldValue(pack(fieldValue))
                .addAllViolation(violations)
                .build();
        return violation;
    }

    /**
     * The enumeration of pre-defined custom validations for a message field.
     */
    private enum BuiltInValidation {

        /**
         * Custom validation strategy for a {@link Timestamp} field.
         */
        TIMESTAMP(Timestamp.class) {
            @Override
            void doValidate(MessageFieldValidator validator) {
                validator.validateTimestamps();
            }
        },

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
