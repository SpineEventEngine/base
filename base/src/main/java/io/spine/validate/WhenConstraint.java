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
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.base.FieldPath;
import io.spine.option.Time;
import io.spine.option.TimeOption;
import io.spine.time.temporal.Temporal;

import static io.spine.option.Time.FUTURE;
import static io.spine.option.Time.TIME_UNDEFINED;
import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A constraint that, when applied to a {@link Timestamp} field value, checks for whether the
 * actual value is in the future or in the past, defined by the value of the field option.
 */
final class WhenConstraint<T extends Message> extends FieldValueConstraint<T, TimeOption> {

    WhenConstraint(TimeOption optionValue) {
        super(optionValue);
    }

    @Override
    public ImmutableList<ConstraintViolation> check(FieldValue<T> fieldValue) {
        Time when = optionValue().getIn();
        if (when == TIME_UNDEFINED) {
            return ImmutableList.of();
        }
        ImmutableList<ConstraintViolation> violations =
                fieldValue.asList()
                          .stream()
                          .map(Temporal::from)
                          .filter(temporalValue -> isTimeInvalid(temporalValue, when))
                          .findFirst()
                          .map(invalidValue -> ImmutableList.of(
                                  newTimeViolation(fieldValue, invalidValue)
                          ))
                          .orElse(ImmutableList.of());
        return violations;
    }

    /**
     * Checks the time.
     *
     * @param temporalValue
     *         a time point to check
     * @param whenExpected
     *         the time when the checked timestamp should be
     * @return {@code true} if the time is valid according to {@code whenExpected} parameter,
     *         {@code false} otherwise
     */
    private static boolean isTimeInvalid(Temporal<?> temporalValue, Time whenExpected) {
        boolean valid = (whenExpected == FUTURE)
                          ? temporalValue.isInFuture()
                          : temporalValue.isInPast();
        return !valid;
    }

    private ConstraintViolation newTimeViolation(FieldValue<?> fieldValue, Temporal<?> value) {
        String msg = errorMsgFormat(optionValue(), optionValue().getMsgFormat());
        String when = optionValue().getIn()
                                   .toString()
                                   .toLowerCase();
        FieldPath fieldPath = fieldValue.context()
                                        .fieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(when)
                .setFieldPath(fieldPath)
                .setFieldValue(value.toAny())
                .build();
        return violation;
    }
}
