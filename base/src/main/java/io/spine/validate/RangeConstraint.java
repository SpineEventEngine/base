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
import javafx.util.Pair;

import java.util.function.BiFunction;

/**
 * A constraint that checks whether a value fits the ranged described by expressions like:
 * {@code int32 value = 5 [(range) = "[3..5)]}, describing a value that is at least 3 and less
 * than 5.
 *
 * @param <V>
 *         numeric value that this constraint is applied to
 */
final class RangeConstraint<V extends Number & Comparable> extends RangedConstraint<V, String> {

    private static final String OPEN = "[]";
    private static final String CLOSED = "()";
    private static final String OPEN_CLOSED = "[)";
    private static final String CLOSED_OPEN = "(]";
    private static final Splitter RANGE_SPLITTER = Splitter.on("..");

    RangeConstraint(String optionValue) {
        super(optionValue, from(optionValue));
    }

    private static <V extends Number & Comparable> Range<V> from(String value) {
        String ranges = ranges(value);
        Pair<V, V> edgeValues = edgeValues(withoutBraces(value));
        V left = edgeValues.getKey();
        V right = edgeValues.getValue();
        @SuppressWarnings("unchecked") // Type parameters are the same across all of the static methods
        Range<V> result = (Range<V>) rangeFrom(ranges).apply(left, right);
        return result;
    }

    private static <V extends Number & Comparable> Pair<V, V> edgeValues(String value) {
        ImmutableList<String> edges = ImmutableList.copyOf(RANGE_SPLITTER.split(value));
        String leftEdge = edges.get(0);
        V left = fromStringValue(leftEdge);
        String rightEdge = edges.get(1);
        V right = fromStringValue(rightEdge);
        return new Pair<>(left, right);
    }

    private static String withoutBraces(String value){
        return value.substring(1, value.length()-1);
    }

    private static String ranges(String value) {
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        String result = String.valueOf(first) + last;
        return result;
    }

    private static <V extends Number & Comparable> BiFunction<V, V, Range<V>> rangeFrom(
            String edges) {
        switch (edges) {
            case OPEN:
                return Range::open;
            case CLOSED:
                return Range::closed;
            case OPEN_CLOSED:
                return Range::openClosed;
            case CLOSED_OPEN:
                return Range::openClosed;
            default:
                throw new IllegalStateException();
        }
    }
}
