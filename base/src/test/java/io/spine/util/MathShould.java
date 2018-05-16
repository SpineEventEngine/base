/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import org.junit.Test;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.util.Math.floorDiv;
import static io.spine.util.Math.safeMultiply;
import static org.junit.Assert.assertEquals;

public class MathShould {

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(io.spine.util.Math.class);
    }

    @Test(expected = ArithmeticException.class)
    public void throw_ArithmethicException_multiply_MIN_VALUE_by_minus_one() {
        safeMultiply(Long.MIN_VALUE, -1);
    }

    @Test
    public void quickly_return_negative_number_on_multiply_by_minus_one() {
        assertEquals(-100, safeMultiply(100, -1));
    }

    @Test
    public void quickly_return_zero_when_multiplying_by_zero() {
        assertEquals(0, safeMultiply(100, 0));
    }

    @Test
    public void quickly_return_same_value_when_multiplying_by_one() {
        assertEquals(8, safeMultiply(8, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void not_allow_multiply_LongMinValue_by_negative_amount() {
        safeMultiply(Long.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void not_allow_multiply_LongMaxValue_by_any_amount_except_zero_one_and_minus_one() {
        safeMultiply(Long.MAX_VALUE, -2);
    }

    @Test
    public void provide_floor_division() {
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
