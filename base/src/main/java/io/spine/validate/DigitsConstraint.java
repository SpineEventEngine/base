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
import io.spine.base.FieldPath;
import io.spine.option.DigitsOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.regex.Pattern;

import static io.spine.protobuf.TypeConverter.toAny;
import static io.spine.validate.FieldValidator.getErrorMsgFormat;

/**
 * A constraint that, when applied to a numeric field, limits the amount of digits that both a whole
 * and a decimal part of a number can have.
 *
 * @param <V>
 *         type of value that the field that is being validated holds
 */
final class DigitsConstraint<V extends Number> implements Constraint<FieldValue<V>> {

    private static final Pattern PATTERN_DOT = Pattern.compile("\\.");

    @Override
    public List<ConstraintViolation> check(FieldValue<V> fieldValue) {
        DigitsOption digitsOption = fieldValue.valueOf(OptionsProto.digits);
        int intDigitsMax = digitsOption.getIntegerMax();
        int fractionDigitsMax = digitsOption.getFractionMax();
        if (intDigitsMax < 1 || fractionDigitsMax < 1) {
            return ImmutableList.of();
        }
        for (V val : fieldValue.asList()) {
            double doubleValue = val.doubleValue();
            String[] parts = PATTERN_DOT.split(String.valueOf(doubleValue));
            int intDigitsCount = parts[0].length();
            int fractionDigitsCount = parts[1].length();
            boolean isInvalid = (intDigitsCount > intDigitsMax) ||
                    (fractionDigitsCount > fractionDigitsMax);
            if (isInvalid) {
                ConstraintViolation digits = digitsViolated(fieldValue, digitsOption, val);
                return ImmutableList.of(digits);
            }
        }
        return ImmutableList.of();
    }

    private ConstraintViolation digitsViolated(FieldValue<V> fieldValue,
                                               DigitsOption digitsOption,
                                               V actualValue) {
        String msg = getErrorMsgFormat(digitsOption, digitsOption.getMsgFormat());
        String intMax = String.valueOf(digitsOption.getIntegerMax());
        String fractionMax = String.valueOf(digitsOption.getFractionMax());
        FieldPath fieldPath = fieldValue.context()
                                        .getFieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(intMax)
                .addParam(fractionMax)
                .setFieldPath(fieldPath)
                .setFieldValue(toAny(actualValue))
                .build();
        return violation;
    }
}
