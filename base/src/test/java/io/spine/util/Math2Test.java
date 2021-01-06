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

package io.spine.util;

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.util.Math2.floorDiv;
import static io.spine.util.Math2.safeMultiply;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Math2 utility class should")
class Math2Test extends UtilityClassTest<Math2> {

    Math2Test() {
        super(Math2.class);
    }

    @Nested
    @DisplayName("throw ArithmeticException when")
    class ErrorOut {

        @Test
        @DisplayName("multiply MIN_VALUE by -1")
        void minusOneByMinValue() {
            assertThrows(
                    ArithmeticException.class,
                    () -> safeMultiply(Long.MIN_VALUE, -1)
            );
        }

        @Test
        @DisplayName("multiply MIN_VALUE by a negative amount")
        void minValueByNegativeAmount() {
            assertThrows(
                    ArithmeticException.class,
                    () -> safeMultiply(Long.MIN_VALUE, -10)
            );
        }
    }

    @Nested
    @DisplayName("quickly return")
    class QuickReturn {

        @Test
        @DisplayName("negative number when multiplying by -1")
        void multiplyByMinusOne() {
            assertEquals(-100, safeMultiply(100, -1));
        }

        @Test
        @DisplayName("zero when multiplied by zero")
        void multiplyByZero() {
            assertEquals(0, safeMultiply(100, 0));
        }

        @Test
        @DisplayName("same value when multiplying by 1")
        void sameValue() {
            assertEquals(8, safeMultiply(8, 1));
        }
    }

    @Test
    @DisplayName("not allow multiply Long.MAX_VALUE by any amount except 0, 1, and -1")
    void checkWithLongMaxValue() {
        assertThrows(
                ArithmeticException.class,
                () -> safeMultiply(Long.MAX_VALUE, -2)
        );

        assertEquals(Long.MAX_VALUE, safeMultiply(Long.MAX_VALUE, 1));
        assertEquals(-Long.MAX_VALUE, safeMultiply(Long.MAX_VALUE, -1));
        assertEquals(0, safeMultiply(Long.MAX_VALUE, 0));
    }

    @Test
    @DisplayName("provide floor division")
    void floorDivision() {
        assertFloorDiv(0, 0, 4);
        assertFloorDiv(-1, -1, 4);
        assertFloorDiv(-1, -2, 4);
        assertFloorDiv(-1, -3, 4);
        assertFloorDiv(-1, -4, 4);
        assertFloorDiv(-2, -5, 4);
    }

    private static void assertFloorDiv(long expected, long a, long b) {
        assertEquals(expected, floorDiv(a, b));
    }
}
