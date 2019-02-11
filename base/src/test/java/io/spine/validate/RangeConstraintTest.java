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
import com.google.common.collect.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Range constraint should")
class RangeConstraintTest {

    @ParameterizedTest
    @MethodSource("validRanges")
    @DisplayName("be able to obtain adequate ranges")
    void acceptProperRanges(String range, BoundType expected) {
        Range<ComparableNumber> result = RangeConstraint.rangeFromOption(range);
        assertEquals(expected, result.upperBoundType());
    }

    private static Stream<Arguments> validRanges() {
        return Stream.of(
                Arguments.of("[1..2]", BoundType.CLOSED),
                Arguments.of("(1..2)", BoundType.OPEN),
                Arguments.of("[1..2)", BoundType.OPEN),
                Arguments.of("(1..2]", BoundType.CLOSED)
        );
    }

    @ParameterizedTest
    @MethodSource("badRanges")
    @DisplayName("throw on incorrectly defined ranges")
    void throwOnMalformedRanges(String badRange) {
        assertThrows(Exception.class, () -> RangeConstraint.rangeFromOption(badRange));
    }

    @ParameterizedTest
    @MethodSource("emptyRanges")
    @DisplayName("throw on empty ranges")
    void throwOnEmptyRanges(String emptyRange) {
        assertThrows(IllegalArgumentException.class,
                     () -> RangeConstraint.rangeFromOption(emptyRange));
    }

    private static Stream<Arguments> emptyRanges() {
        return Stream.concat(rangeCombinationsFor(1, 0),
                             Stream.of(Arguments.of("(0..0)")));
    }

    private static Stream<Arguments> badRanges() {
        return Stream.of(
                Arguments.of("{3..5]"),
                Arguments.of("[3..5}"),
                Arguments.of("{3..5}"),
                Arguments.of("(3..5"),
                Arguments.of("3..5)"),
                Arguments.of("((3..5]"),
                Arguments.of("(3..5]]"),
                Arguments.of("(3,5..5)"),
                Arguments.of("(3 5..5"),
                Arguments.of("[3..5 5]"),
                Arguments.of("[3..5,5]"),
                Arguments.of("[3;5]"),
                Arguments.of("[3.5..5)"),
                Arguments.of("[3..5.5)"),
                Arguments.of("[3...5]")
        );
    }

    private static Stream<Arguments> rangeCombinationsFor(Number left, Number right) {
        return Stream.of(
                Arguments.of(format("[%s..%s]", left, right)),
                Arguments.of(format("[%s..%s)", left, right)),
                Arguments.of(format("(%s..%s]", left, right)),
                Arguments.of(format("(%s..%s)", left, right))
        );
    }
}
