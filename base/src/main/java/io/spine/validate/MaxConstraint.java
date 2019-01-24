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

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toList;

/**
 * A constraint that, when applied, checks whether a numeric field value exceeds a max value.
 *
 * @param <V>
 *         value that the field validated by this constraint has
 */
public class MaxConstraint<V extends Number> implements Constraint<FieldValue<V>> {

    @Override
    public List<ConstraintViolation> check(FieldValue<V> fieldValue) {
        MaxOption option = fieldValue.valueOf(OptionsProto.max);
        ImmutableList<V> actualValue = fieldValue.asList();
        double maxValue = parseDouble(option.getValue());
        boolean exclusive = option.getExclusive();
        Predicate<V> exceeds = exclusive
                               ? value -> value.doubleValue() >= maxValue
                               : value -> value.doubleValue() > maxValue;
        List<ConstraintViolation> violations =
                actualValue.stream()
                           .filter(exceeds)
                           .map(exceedingNumber -> maxViolated(fieldValue, maxValue, exclusive))
                           .collect(toList());
        return violations;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private ConstraintViolation maxViolated(FieldValue<V> fieldValue,
                                            double maxValue,
                                            boolean exclusive) {
        String format = "Number must be less than %s %s.";
        FieldPath path = fieldValue.context()
                                   .getFieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(format)
                .addParam(exclusive ? "" : "or equal to")
                .addParam(String.valueOf(maxValue))
                .setFieldPath(path)
                .build();
        return violation;
    }
}
