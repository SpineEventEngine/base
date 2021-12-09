/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.spine.code.proto.FieldDeclaration;
import io.spine.test.type.Url;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.validate.option.RangeConstraint.rangeFromOption;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Range constraint should")
class RangeConstraintTest {

    @ParameterizedTest
    @MethodSource("validRanges")
    @DisplayName("be able to parse valid range strings")
    void acceptProperRanges(String range, BoundType expected) {
        var result = RangeConstraint.rangeFromOption(range, aDeclaration());
        assertEquals(expected, result.upperBoundType());
    }

    @SuppressWarnings("unused") /* Serves as a method source. */
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
        assertThrows(Exception.class,
                     () -> RangeConstraint.rangeFromOption(badRange, aDeclaration()));
    }

    @SuppressWarnings("unused") /* Serves as a method source. */
    private static ImmutableSet<Arguments> badRanges() {
        return argumentsFrom(
                "{3..5]",
                "[3..5}",
                "{3..5}",
                "(3..5",
                "3..5)",
                "((3..5]",
                "(3..5]]",
                "(3,5..5)",
                "(3 5..5",
                "[3..5 5]",
                "[3..5,5]",
                "[3;5]",
                "[3...5]"
        );
    }

    @ParameterizedTest
    @MethodSource("emptyRanges")
    @DisplayName("throw on empty ranges")
    void throwOnEmptyRanges(String emptyRange) {
        assertIllegalArgument(() -> rangeFromOption(emptyRange, aDeclaration()));
    }

    @SuppressWarnings("unused") /* Serves as a method source. */
    private static Set<Arguments> emptyRanges() {
        var right = 0;
        var left = right + 1;
        var leftGreaterThanRight =
                rangeCombinationsFor(left,
                                     right,
                                     ImmutableSet.of('[', '('),
                                     ImmutableSet.of(']', ')'));
        var closedWithSameNumber = arguments("(0..0)");
        return Sets.union(leftGreaterThanRight, ImmutableSet.of(closedWithSameNumber));
    }

    private static ImmutableSet<Arguments>
    rangeCombinationsFor(Number left,
                         Number right,
                         ImmutableSet<Character> leftBoundary,
                         ImmutableSet<Character> rightBoundary) {
        var lefts = leftBoundary.stream()
                .map(boundary -> String.valueOf(boundary) + left + "..")
                .collect(toImmutableSet());
        var rights = rightBoundary.stream()
                .map(boundary -> String.valueOf(right) + boundary)
                .collect(toImmutableSet());
        var result = Sets.cartesianProduct(lefts, rights).stream()
                .flatMap(product -> Stream.of(product.get(0) + product.get(1)))
                .map(Arguments::of)
                .collect(toImmutableSet());
        return result;
    }

    private static ImmutableSet<Arguments> argumentsFrom(Object... elements) {
        ImmutableSet.Builder<Arguments> builder = ImmutableSet.builder();
        for (var element : elements) {
            builder.add(Arguments.of(element));
        }
        return builder.build();
    }

    private static FieldDeclaration aDeclaration() {
        return new FieldDeclaration(
                Url.getDescriptor()
                   .getFields()
                   .get(0)
        );
    }
}
