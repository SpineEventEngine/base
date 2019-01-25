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
import io.spine.option.MinOption;
import io.spine.option.OptionsProto;

import static io.spine.protobuf.TypeConverter.toAny;
import static java.lang.Double.parseDouble;

/**
 * A constraint that, when applied to a numeric field, checks whether the value of that field is
 * greater than (or equal to, if specified by the value of the respective option) a min value.
 *
 * @param <V>
 *         a type of value that is this constraint can be applied to
 */
final class MinConstraint<V extends Number> extends NumericFieldConstraint<V> {

    @Override
    boolean doesNotSatisfy(V value, FieldValue<V> fieldValue) {
        double actualValue = value.doubleValue();
        double minValue = minFrom(fieldValue);
        return isExclusive(fieldValue)
               ? actualValue <= minValue
               : actualValue < minValue;
    }

    @Override
    ConstraintViolation constraintViolated(FieldValue<V> fieldValue, V actualValue) {
        String format = "Number must be greater than %s %s.";
        FieldPath path = fieldValue.context()
                                   .getFieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(format)
                .addParam(isExclusive(fieldValue) ? "" : "or equal to")
                .addParam(String.valueOf(minFrom(fieldValue)))
                .setFieldPath(path)
                .setFieldValue(toAny(actualValue))
                .build();
        return violation;
    }

    private double minFrom(FieldValue<V> fieldValue) {
        MinOption optionValue = fieldValue.valueOf(OptionsProto.min);
        String stringValue = optionValue.getValue();
        return parseDouble(stringValue);
    }

    private boolean isExclusive(FieldValue<V> fieldValue) {
        return fieldValue.valueOf(OptionsProto.min)
                         .getExclusive();
    }
}
