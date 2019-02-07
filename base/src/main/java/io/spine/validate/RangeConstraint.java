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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.util.function.BiFunction;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A constraint that checks whether a value fits the ranged described by expressions like:
 * {@code int32 value = 5 [(range) = "[3..5)]}, describing a value that is at least 3 and less
 * than 5.
 *
 * @param <V>
 *         numeric value that this constraint is applied to
 */
final class RangeConstraint<V extends Number & Comparable> extends RangedConstraint<V, String> {

    private static final Splitter RANGE_SPLITTER = Splitter.on("..");

    RangeConstraint(String optionValue) {
        super(optionValue, from(optionValue));
    }

    private static Range<StringDescribedNumber> from(String value) {
        RangeType range = rangeType(value);
        EdgeValues edgeValues = edgeValues(withoutBraces(value));
        Range<StringDescribedNumber> result = rangeFrom(range).apply(edgeValues.left(), edgeValues.right());
        return result;
    }

    private static EdgeValues edgeValues(String value) {
        ImmutableList<String> edges = ImmutableList.copyOf(RANGE_SPLITTER.split(value));
        String leftEdge = edges.get(0);
        String rightEdge = edges.get(1);
        return EdgeValues.of(leftEdge, rightEdge);
    }

    private static String withoutBraces(String value) {
        return value.substring(1, value.length() - 1);
    }

    private static RangeType rangeType(String value) {
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        String result = String.valueOf(first) + last;
        return RangeType.from(result);
    }

    private static BiFunction<StringDescribedNumber, StringDescribedNumber, Range<StringDescribedNumber>>
    rangeFrom(RangeType ranges) {
        switch (ranges) {
            case OPEN:
                return Range::open;
            case CLOSED:
                return Range::closed;
            case OPEN_CLOSED:
                return Range::openClosed;
            case CLOSED_OPEN:
                return Range::closedOpen;
            default:
                throw newIllegalStateException("Illegal range type instance %s", ranges);
        }
    }

    /**
     * Edge values of a range.
     */
    private static class EdgeValues {

        private final StringDescribedNumber leftEdge;
        private final StringDescribedNumber rightEdge;

        private EdgeValues(StringDescribedNumber leftEdge, StringDescribedNumber rightEdge) {
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
        static EdgeValues of(String leftEdge, String rightEdge) throws IllegalStateException {
            StringDescribedNumber left = new StringDescribedNumber(leftEdge);
            StringDescribedNumber right = new StringDescribedNumber(rightEdge);
            checkTypes(left, right);
            return new EdgeValues(left, right);
        }

        /** Returns the left edge of the range.*/
        private StringDescribedNumber left() {
            return leftEdge;
        }
        /** Returns the right edge of the range. */
        private StringDescribedNumber right() {
            return rightEdge;
        }

        private static void checkTypes(StringDescribedNumber left, StringDescribedNumber right)
                throws IllegalStateException {
            if (!left.isOfSameType(right)) {
                String errorMessage =
                        "Could not construct edge values, since left and right" +
                                "edge values are of different type: %s and %s.";
                throw newIllegalStateException(errorMessage, left, right);
            }
        }
    }

    /**
     * A type of range.
     *
     * <p>Can be {@code open} or {@code closed} from both sides, meaning that the edge values
     * is either excluded or included from the range.
     */
    private enum RangeType {
        CLOSED("[]"),
        OPEN("()"),
        OPEN_CLOSED("(]"),
        CLOSED_OPEN("[)");

        private final String edges;

        RangeType(String edges) {
            this.edges = edges;
        }

        /**
         * Find a {@code RangeType} instance that matches the given string.
         *
         * <p>If such instance could not be found, an {@code IllegalStateException} is thrown.
         *
         * @param edges
         *         the string representation of the edges, can be any combinations of
         *         {@code (}/{@code [} and {@code )}/{@code ]}
         * @return an instance of {@code RangeType}
         */
        static RangeType from(String edges) throws IllegalStateException {
            for (RangeType value : values()) {
                if (value.edges.equals(edges)) {
                    return value;
                }
            }
            throw newIllegalStateException("Could not construct edges from %s.", edges);
        }
    }
}
