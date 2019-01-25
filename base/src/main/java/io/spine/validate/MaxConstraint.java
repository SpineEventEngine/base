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

import io.spine.base.FieldPath;
import io.spine.option.MaxOption;
import io.spine.option.OptionsProto;

import static io.spine.protobuf.TypeConverter.toAny;
import static java.lang.Double.parseDouble;

/**
 * A constraint that, when applied, checks whether a numeric field value exceeds a max value.
 *
 * @param <V>
 *         value that the field validated by this constraint has
 */
final class MaxConstraint<V extends Number> extends NumericFieldConstraint<V> {

    @Override
    boolean doesNotSatisfy(V value, FieldValue<V> fieldValue) {
        double actualValue = value.doubleValue();
        double maxValue = maxFrom(fieldValue);
        return isExclusive(fieldValue)
               ? actualValue >= maxValue
               : actualValue > maxValue;
    }

    @Override
    ConstraintViolation constraintViolated(FieldValue<V> fieldValue,
                                           V actualValue) {
        String format = "Number must be less than %s %s.";
        FieldPath path = fieldValue.context()
                                   .getFieldPath();
        double maxValue= maxFrom(fieldValue);
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(format)
                .addParam(isExclusive(fieldValue) ? "" : "or equal to")
                .addParam(String.valueOf(maxFrom(fieldValue)))
                .setFieldPath(path)
                .setFieldValue(toAny(actualValue))
                .build();
        return violation;
    }

    private double maxFrom(FieldValue<V> fieldValue) {
        MaxOption optionValue = fieldValue.valueOf(OptionsProto.max);
        String stringValue = optionValue.getValue();
        return parseDouble(stringValue);
    }

    private boolean isExclusive(FieldValue<V> fieldValue) {
        return fieldValue.valueOf(OptionsProto.max)
                         .getExclusive();
    }
}
