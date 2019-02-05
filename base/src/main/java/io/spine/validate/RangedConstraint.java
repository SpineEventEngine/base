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

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import io.spine.base.FieldPath;

import static com.google.common.collect.BoundType.CLOSED;
import static io.spine.protobuf.TypeConverter.toAny;
import static java.lang.String.format;

public abstract class RangedConstraint<V extends Number & Comparable, T>
        extends NumericFieldConstraint<V, T> {

    static final String OR_EQUAL_TO = "or equal to";

    private final Range<V> range;

    RangedConstraint(T optionValue, Range<V> range) {
        super(optionValue);
        this.range = range;
    }

    @Override
    boolean satisfies(FieldValue<V> value) {
        return value.asList()
                    .stream()
                    .allMatch(range::contains);
    }

    @Override
    ImmutableList<ConstraintViolation> constraintViolated(FieldValue<V> value) {
        FieldPath path = value.context()
                              .getFieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(errorMsgFormat())
                .addAllParam(formatParams())
                .setFieldPath(path)
                .setFieldValue(toAny(value.singleValue()))
                .build();
        return ImmutableList.of(violation);
    }

    private String errorMsgFormat() {
        StringBuilder result = new StringBuilder("Number must be ");
        if (range.hasLowerBound() && range.hasUpperBound()) {
            result.append(forLowerBound());
            result.append("and ");
            result.append(forUpperBound());
            return result.toString();
        }
        if (range.hasLowerBound()) {
            result.append(forLowerBound());
        }
        if (range.hasUpperBound()) {
            result.append(forUpperBound());
        }
        return result.toString();
    }

    private ImmutableSet<String> formatParams() {
        ImmutableSet.Builder<String> result = ImmutableSet.builder();
        if (range.hasLowerBound()) {
            result.add(range.lowerEndpoint()
                            .toString());
        }
        if (range.hasUpperBound()) {
            result.add(range.upperEndpoint()
                            .toString());
        }
        return result.build();
    }

    private String forLowerBound() {
        String greaterThan = "greater than %s";
        String appendix = appendix(range.lowerBoundType());
        return format(greaterThan, appendix);
    }

    private String forUpperBound() {
        String lessThan = "less than %s";
        String appendix = appendix(range.upperBoundType());
        return format(lessThan, appendix);
    }

    private static String appendix(BoundType type) {
        return type == CLOSED
               ? OR_EQUAL_TO + " %s."
               : " %s.";
    }
}
