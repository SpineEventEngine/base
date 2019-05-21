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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import io.spine.validate.ComparableNumber;
import io.spine.validate.NumberText;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A constraint that checks whether a value fits the ranged described by expressions such as
 * {@code int32 value = 5 [(range) = "[3..5)]}, describing a value that is at least 3 and less
 * than 5.
 *
 * @param <V>
 *         numeric value that this constraint is applied to
 */
final class RangeConstraint<V extends Number & Comparable> extends RangedConstraint<V, String> {

    private static final Splitter RANGE_SPLITTER = Splitter.on("..");

    RangeConstraint(String optionValue) {
        super(optionValue, rangeFromOption(optionValue));
    }

    @VisibleForTesting
    static Range<ComparableNumber> rangeFromOption(String value) {
        String trimmed = value.trim();
        RangeType range = RangeType.parse(trimmed);
        EdgeValues edgeValues = edgeValues(withoutBraces(trimmed));
        ComparableNumber left = edgeValues.leftEdge.toNumber();
        ComparableNumber right = edgeValues.rightEdge.toNumber();
        Range<ComparableNumber> result = range.create(left, right);
        return result;
    }

    private static EdgeValues edgeValues(String value) {
        String trimmed = value.trim();
        ImmutableList<String> edges = ImmutableList.copyOf(RANGE_SPLITTER.split(trimmed));
        String leftEdge = edges.get(0);
        String rightEdge = edges.get(1);
        return EdgeValues.of(leftEdge, rightEdge);
    }

    private static String withoutBraces(String value) {
        return value.substring(1, value.length() - 1);
    }

    /**
     * Edge values of a range.
     */
    private static class EdgeValues {

        private final NumberText leftEdge;
        private final NumberText rightEdge;

        private EdgeValues(NumberText leftEdge, NumberText rightEdge) {
            this.leftEdge = leftEdge;
            this.rightEdge = rightEdge;
        }

        /**
         * Creates a new instance of {@code EdgeValues} given their string representations.
         *
         * <p>If given strings represent numbers of different types, e.g.
         * {@code "0.0"} and {@code "2"}, which represent a {@code Double} and an {@code Integer},
         * an {@code IllegalStateException} is thrown.
         *
         * @param leftEdge
         *         left edge of the range
         * @param rightEdge
         *         right edge of the range
         * @return a new instance of {@code EdgeValues}
         */
        private static EdgeValues of(String leftEdge, String rightEdge)
                throws IllegalStateException {
            NumberText left = new NumberText(leftEdge);
            NumberText right = new NumberText(rightEdge);
            checkTypes(left, right);
            return new EdgeValues(left, right);
        }

        private static void checkTypes(NumberText left, NumberText right)
                throws IllegalStateException {
            if (!left.isOfSameType(right)) {
                String errorMessage =
                        "Could not construct edge values, since left and right" +
                                "edge values are of different type: %s and %s.";
                throw newIllegalStateException(errorMessage, left, right);
            }
        }
    }
}
