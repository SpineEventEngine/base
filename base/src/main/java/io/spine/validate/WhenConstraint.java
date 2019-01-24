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
import io.spine.option.OptionsProto;
import io.spine.option.Time;
import io.spine.option.TimeOption;

import java.util.List;

import static io.spine.base.Time.getCurrentTime;
import static io.spine.option.Time.FUTURE;
import static io.spine.option.Time.TIME_UNDEFINED;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.protobuf.Timestamps2.isLaterThan;
import static io.spine.validate.FieldValidator.getErrorMsgFormat;

/**
 * A constraint that, when applied to a {@link Timestamp} field value, checks for whether the
 * actual value is in the future or in the past, defined by the value of the field option.
 */
public class WhenConstraint implements Constraint<FieldValue<Timestamp>> {

    private static List<ConstraintViolation> validateTimestamps(FieldValue<Timestamp> fieldValue) {
        TimeOption timeConstraint = fieldValue.valueOf(OptionsProto.when);
        Time when = timeConstraint.getIn();
        if (when == TIME_UNDEFINED) {
            return ImmutableList.of();
        }
        Timestamp now = getCurrentTime();
        for (Message value : fieldValue.asList()) {
            Timestamp time = (Timestamp) value;
            if (isTimeInvalid(time, when, now)) {
                return ImmutableList.of(newTimeViolation(fieldValue, time, timeConstraint));
            }
        }
        return ImmutableList.of();
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

    private static ConstraintViolation newTimeViolation(FieldValue<Timestamp> fieldValue,
                                                        Timestamp value,
                                                        TimeOption timeConstraint) {
        String msg = getErrorMsgFormat(timeConstraint, timeConstraint.getMsgFormat());
        String when = timeConstraint.getIn()
                                    .toString()
                                    .toLowerCase();
        FieldPath fieldPath = fieldValue.context()
                                        .getFieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(when)
                .setFieldPath(fieldPath)
                .setFieldValue(pack(value))
                .build();
        return violation;
    }

    @Override
    public List<ConstraintViolation> check(FieldValue<Timestamp> value) {
        return validateTimestamps(value);
    }
}
