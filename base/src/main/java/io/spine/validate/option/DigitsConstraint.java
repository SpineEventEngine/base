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

package io.spine.validate.option;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.spine.base.FieldPath;
import io.spine.option.DigitsOption;
import io.spine.type.TypeName;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.FieldValue;

import static io.spine.protobuf.TypeConverter.toAny;
import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A numerical field constraint that limits the number of whole and decimal digits.
 *
 * @param <V>
 *         a numeric value the digits of which are being controlled for length
 */
final class DigitsConstraint<V extends Number & Comparable>
        extends NumericFieldConstraint<V, DigitsOption> {

    private static final Splitter DOT_SPLITTER = Splitter.on(".");

    DigitsConstraint(DigitsOption digitsOption) {
        super(digitsOption);
    }

    @Override
    boolean satisfies(FieldValue<V> value) {
        DigitsOption option = optionValue();
        int wholeDigitsMax = option.getIntegerMax();
        int fractionDigitsMax = option.getFractionMax();
        if (wholeDigitsMax < 1 || fractionDigitsMax < 1) {
            return true;
        }
        boolean constraintViolated =
                value.asList()
                     .stream()
                     .anyMatch(number -> violated(number, wholeDigitsMax, fractionDigitsMax));
        return !constraintViolated;
    }

    private boolean violated(V number, int wholeDigitsMax, int fractionDigitsMax) {
        double actualValue = number.doubleValue();
        ImmutableList<String> parts = splitOnPeriod(actualValue);
        int wholeDigitsCount = parts.get(0)
                                    .length();
        int fractionDigitsCount = parts.get(1)
                                       .length();
        boolean violated =
                wholeDigitsCount > wholeDigitsMax ||
                        fractionDigitsCount > fractionDigitsMax;
        return violated;
    }

    private static ImmutableList<String> splitOnPeriod(double value) {
        return ImmutableList.copyOf(DOT_SPLITTER.split(String.valueOf(value)));
    }

    @Override
    ImmutableList<ConstraintViolation> constraintViolated(FieldValue<V> value) {
        DigitsOption option = optionValue();
        String msg = errorMsgFormat(option, option.getMsgFormat());
        String intMax = String.valueOf(option.getIntegerMax());
        String fractionMax = String.valueOf(option.getFractionMax());
        FieldPath fieldPath = value.context()
                                   .fieldPath();
        TypeName declaringTypeName = value.declaration()
                                          .declaringType()
                                          .name();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(intMax)
                .addParam(fractionMax)
                .setFieldPath(fieldPath)
                .setFieldValue(toAny(value.singleValue()))
                .setTypeName(declaringTypeName.value())
                .build();
        return ImmutableList.of(violation);
    }
}
