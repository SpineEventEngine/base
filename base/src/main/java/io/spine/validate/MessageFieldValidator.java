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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.option.IfInvalidOption;
import io.spine.option.OptionsProto;
import io.spine.option.Time;
import io.spine.option.TimeOption;

import java.util.List;

import static io.spine.option.Time.FUTURE;
import static io.spine.option.Time.TIME_UNDEFINED;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.time.Time.getCurrentTime;
import static io.spine.time.Timestamps2.isLaterThan;
import static io.spine.validate.Validate.isDefault;

/**
 * Validates fields of type {@link Message}.
 *
 * @author Alexander Litus
 */
class MessageFieldValidator extends FieldValidator<Message> {

    private final TimeOption timeConstraint;
    private final boolean isFieldTimestamp;

    /**
     * Creates a new validator instance.
     *
     * @param fieldContext the context of the field to validate
     * @param fieldValues  values to validate
     * @param strict       if {@code true} the validator would assume that the field
     *                     is required even if the corresponding field option is not present
     */
    MessageFieldValidator(FieldContext fieldContext,
                          Object fieldValues,
                          boolean strict) {
        super(fieldContext, FieldValidator.<Message>toValueList(fieldValues), strict);
        this.timeConstraint = getFieldOption(OptionsProto.when);
        this.isFieldTimestamp = isTimestamp();
    }

    @Override
    protected void validateOwnRules() {
        final boolean recursiveValidationRequired = getValidateOption();
        if (recursiveValidationRequired) {
            validateFields();
        }
        if (isFieldTimestamp) {
            validateTimestamps();
        }
    }

    @Override
    protected boolean isValueNotSet(Message value) {
        final boolean result = isDefault(value);
        return result;
    }

    private boolean isTimestamp() {
        final ImmutableList<Message> values = getValues();
        final Message value = values.isEmpty() ? null : values.get(0);
        final boolean isTimestamp = value instanceof Timestamp;
        return isTimestamp;
    }

    private void validateFields() {
        for (Message value : getValues()) {
            final MessageValidator validator = MessageValidator.newInstance(getFieldContext());
            final List<ConstraintViolation> violations = validator.validate(value);
            if (!violations.isEmpty()) {
                addViolation(newValidViolation(value, violations));
            }
        }
    }

    private void validateTimestamps() {
        final Time when = timeConstraint.getIn();
        if (when == TIME_UNDEFINED) {
            return;
        }
        final Timestamp now = getCurrentTime();
        for (Message value : getValues()) {
            final Timestamp time = (Timestamp) value;
            if (isTimeInvalid(time, when, now)) {
                addViolation(newTimeViolation(time));
                return; // return because one error message is enough for the "time" option
            }
        }
    }

    /**
     * Checks the time.
     *
     * @param timeToCheck  a timestamp to check
     * @param whenExpected the time when the checked timestamp should be
     * @param now          the current moment
     * @return {@code true} if the time is valid according to {@code whenExpected} parameter,
     * {@code false} otherwise
     */
    private static boolean isTimeInvalid(Timestamp timeToCheck, Time whenExpected, Timestamp now) {
        final boolean isValid = (whenExpected == FUTURE)
                                ? isLaterThan(timeToCheck, /*than*/ now)
                                : isLaterThan(now, /*than*/ timeToCheck);
        final boolean isInvalid = !isValid;
        return isInvalid;
    }

    private ConstraintViolation newTimeViolation(Timestamp fieldValue) {
        final String msg = getErrorMsgFormat(timeConstraint, timeConstraint.getMsgFormat());
        final String when = timeConstraint.getIn()
                                          .toString()
                                          .toLowerCase();
        final ConstraintViolation violation = ConstraintViolation.newBuilder()
                                                                 .setMsgFormat(msg)
                                                                 .addParam(when)
                                                                 .setFieldPath(getFieldPath())
                                                                 .setFieldValue(pack(fieldValue))
                                                                 .build();
        return violation;
    }

    private ConstraintViolation newValidViolation(Message fieldValue,
                                                  Iterable<ConstraintViolation> violations) {
        final IfInvalidOption ifInvalid = ifInvalid();
        final String msg = getErrorMsgFormat(ifInvalid, ifInvalid.getMsgFormat());
        final ConstraintViolation violation = ConstraintViolation.newBuilder()
                                                                 .setMsgFormat(msg)
                                                                 .setFieldPath(getFieldPath())
                                                                 .setFieldValue(pack(fieldValue))
                                                                 .addAllViolation(violations)
                                                                 .build();
        return violation;
    }
}
