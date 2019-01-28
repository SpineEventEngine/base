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
import io.spine.option.MaxOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.function.Predicate;

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
    boolean doesNotSatisfy(FieldValue<V> fieldValue) {
        return maxViolated(fieldValue);
    }

    private boolean maxViolated(FieldValue<V> fieldValue) {
        double maxValue = maxFrom(fieldValue);
        Predicate<V> exceeds = violates(fieldValue, maxValue);
        ImmutableList<V> nestedValues = fieldValue.asList();
        boolean violated = nestedValues.stream()
                                       .anyMatch(exceeds);
        return violated;
    }

    private Predicate<V> violates(FieldValue<V> fieldValue, double maxValue) {
        return isExclusive(fieldValue)
                                   ? value -> value.doubleValue() >= maxValue
                                   : value -> value.doubleValue() > maxValue;
    }

    @Override
    List<ConstraintViolation> constraintViolated(FieldValue<V> fieldValue) {
        String format = "Number must be less than %s %s.";
        FieldPath path = fieldValue.context()
                                   .getFieldPath();
        double maxValue = maxFrom(fieldValue);
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(format)
                .addParam(isExclusive(fieldValue) ? "" : "or equal to")
                .addParam(String.valueOf(maxValue))
                .setFieldPath(path)
                .setFieldValue(toAny(fieldValue.singleValue()))
                .build();
        return ImmutableList.of(violation);
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
