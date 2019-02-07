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
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * A constraint that puts a numeric field value into a range.
 *
 * <p>A field violates this constraint of its value is out of the range.
 *
 * @param <V>
 *         numeric value that the option is applied to
 * @param <T>
 *         value of the option
 */
abstract class RangedConstraint<V extends Number & Comparable, T>
        extends NumericFieldConstraint<V, T> {

    private static final String OR_EQUAL_TO = "or equal to";

    private final Range<StringDescribedNumber> range;

    RangedConstraint(T optionValue, Range<StringDescribedNumber> range) {
        super(optionValue);
        this.range = range;
    }

    @Override
    boolean satisfies(FieldValue<V> value) {
        checkTypeConsistency(value);
        return value.asList()
                    .stream()
                    .map(StringDescribedNumber::new)
                    .allMatch(range);
    }

    private void checkTypeConsistency(FieldValue<V> value) {
        if (hasBothBoundaries()) {
            StringDescribedNumber upper = range.upperEndpoint();
            StringDescribedNumber lower = range.lowerEndpoint();
            if (!upper.isOfSameType(lower)) {
                String errorMessage = "Boundaries have inconsistent types: lower %s, upper %s";
                throw newIllegalStateException(errorMessage, upper, lower);
            }
            checkBoundaryAndValue(upper, value);
        } else {
            checkSingleBoundary(value);
        }
    }

    private void checkBoundaryAndValue(StringDescribedNumber boundary, FieldValue<V> value) {
        StringDescribedNumber valueToCheck = new StringDescribedNumber(value.singleValue());
        if (!boundary.isOfSameType(valueToCheck)) {
            String errorMessage =
                    "Boundary values must have types consistent with values they bind: " +
                            "boundary %s, value %s";
            throw newIllegalStateException(errorMessage, boundary, valueToCheck);
        }
    }

    private void checkSingleBoundary(FieldValue<V> value) {
        StringDescribedNumber singleBoundary = range.hasLowerBound()
                                      ? range.lowerEndpoint()
                                      : range.upperEndpoint();
        checkBoundaryAndValue(singleBoundary, value);
    }

    private boolean hasBothBoundaries() {
        return range.hasLowerBound() && range.hasUpperBound();
    }

    private StringDescribedNumber existingBound() {
        return range.hasUpperBound()
               ? range.upperEndpoint()
               : range.lowerEndpoint();
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
