/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.query;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.testing.Tests.nullRef;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ComparisonOperator` should")
class ComparisonOperatorTest {

    private final ImmutableList<ComparisonOperator> orderingOperators =
            ImmutableList.of(GREATER_THAN, GREATER_OR_EQUALS, LESS_THAN, LESS_OR_EQUALS);

    @Nested
    @DisplayName("compare supported values A (smaller) and B (bigger):")
    @SuppressWarnings("DuplicateStringLiteralInspection")   // reusing the method sources.
    final class CompareSupported {

        @ParameterizedTest
        @DisplayName("(A = A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#equalValues")
        void equalToTrue(Object left, Object right) {
            assertThat(EQUALS.eval(left, right)).isTrue();
            assertThat(EQUALS.eval(right, left)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A = B) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#notEqualValues")
        void equalToFalse(Object left, Object right) {
            assertThat(EQUALS.eval(left, right)).isFalse();
            assertThat(EQUALS.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(B > A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void greaterThan(Object left, Object right) {
            assertThat(GREATER_THAN.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A > B) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void reverseGreaterThan(Object left, Object right) {
            assertThat(GREATER_THAN.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(B >= A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void greaterOrEqualsForDifferentValues(Object left, Object right) {
            assertThat(GREATER_OR_EQUALS.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A >= B) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void reverseGreaterOrEqualsForDifferentValues(Object left, Object right) {
            assertThat(GREATER_OR_EQUALS.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(B >= B) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#equalOrderableValues")
        void greaterOrEqualsForSame(Object left, Object right) {
            assertThat(GREATER_OR_EQUALS.eval(left, right)).isTrue();
            assertThat(GREATER_OR_EQUALS.eval(right, left)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A < B) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void lessThan(Object left, Object right) {
            assertThat(LESS_THAN.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(B < A) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void reverseLessThan(Object left, Object right) {
            assertThat(LESS_THAN.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(A <= B) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void lessOrEqualForDifferentValues(Object left, Object right) {
            assertThat(LESS_OR_EQUALS.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(B <= A) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void reverseLessOrEqualForDifferentValues(Object left, Object right) {
            assertThat(LESS_OR_EQUALS.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(A <= A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#equalOrderableValues")
        void lessOrEqualForSame(Object left, Object right) {
            assertThat(LESS_OR_EQUALS.eval(left, right)).isTrue();
            assertThat(LESS_OR_EQUALS.eval(right, left)).isTrue();
        }
    }

    @Nested
    @DisplayName("reject values in `>`, `>=`, `<` and `<=` operations")
    final class Reject {

        @Test
        @DisplayName("by throwing `IllegalArgumentException` " +
                "if the types of values are not the same")
        void notSame() {
            Object left = Timestamp.getDefaultInstance();
            Object right = 0;
            for (ComparisonOperator operator : orderingOperators) {
                assertThrows(IllegalArgumentException.class,
                             () -> operator.eval(left, right));
            }
        }

        @Test
        @DisplayName("by throwing an `UnsupportedOperationException` " +
                "if their types types are neither `Comparable` nor `Timestamp`")
        void unsupported() {
            Object left = Any.getDefaultInstance();
            Object right = Any.getDefaultInstance();
            for (ComparisonOperator operator : orderingOperators) {
                assertThrows(UnsupportedOperationException.class,
                             () -> operator.eval(left, right));
            }
        }
    }

    @Test
    @DisplayName("return `false` in `>`, `>=`, `<` and `<=` if any of the operands is `null`")
    void orderingOperatorsReturnFalseForTwoNulls() {
        Timestamp nullOperand = nullRef();
        Timestamp anotherOperand = Timestamp.getDefaultInstance();
        for (ComparisonOperator operator : orderingOperators) {
            assertThat(operator.eval(nullOperand, anotherOperand)).isFalse();
            assertThat(operator.eval(anotherOperand, nullOperand)).isFalse();
        }
    }

    @Test
    @DisplayName("return `true` for (`null` = `null`)")
    void nullEqualsNull() {
        assertThat(EQUALS.eval(nullRef(), nullRef())).isTrue();
    }
}
