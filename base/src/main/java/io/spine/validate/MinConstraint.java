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
import io.spine.option.MinOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.function.Predicate;

import static io.spine.protobuf.TypeConverter.toAny;
import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toList;

/**
 * A constraint that, when applied to a numeric field, checks whether the value of that field is
 * greater than (or equal to, if specified by the value of the respective option) a min value.
 *
 * @param <V>
 *         a type of value that is this constraint can be applied to
 */
final class MinConstraint<V extends Number> implements Constraint<FieldValue<V>> {

    @Override
    public List<ConstraintViolation> check(FieldValue<V> fieldValue) {
        MinOption option = fieldValue.valueOf(OptionsProto.min);
        ImmutableList<V> actualValue = fieldValue.asList();
        double minValue = parseDouble(option.getValue());
        boolean exclusive = option.getExclusive();
        Predicate<V> undershoots = exclusive
                                   ? value -> value.doubleValue() <= minValue
                                   : value -> value.doubleValue() < minValue;
        List<ConstraintViolation> violations =
                actualValue.stream()
                           .filter(undershoots)
                           .map(belowMin -> minConstraintViolated(fieldValue, option, belowMin))
                           .collect(toList());
        return violations;
    }

    private ConstraintViolation minConstraintViolated(FieldValue<V> fieldValue,
                                                      MinOption option,
                                                      V actualValue) {
        String format = "Number must be greater than %s %s.";
        FieldPath path = fieldValue.context()
                                   .getFieldPath();
        boolean exclusive = option.getExclusive();
        double minValue = Double.parseDouble(option.getValue());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(format)
                .addParam(exclusive ? "" : "or equal to")
                .addParam(String.valueOf(minValue))
                .setFieldPath(path)
                .setFieldValue(toAny(actualValue))
                .build();
        return violation;
    }
}
