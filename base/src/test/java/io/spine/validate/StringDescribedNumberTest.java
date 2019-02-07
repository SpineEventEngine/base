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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("String described number should")
public class StringDescribedNumberTest {

    @Test
    @DisplayName("recognize that two numbers have different types")
    void differentTypeTest() {
        String plainNumber = "1";
        String numberWithDecimalPart = "1.0";
        StringDescribedNumber plain = new StringDescribedNumber(plainNumber);
        StringDescribedNumber withDecimal = new StringDescribedNumber(numberWithDecimalPart);
        assertFalse(plain.isOfSameType(withDecimal));
    }

    @Test
    @DisplayName("recognize that two numbers are of the same types")
    void sameTypeTest() {
        String fitsIntoByte = "4";
        String maxInteger = String.valueOf(Integer.MAX_VALUE);
        StringDescribedNumber small = new StringDescribedNumber(fitsIntoByte);
        StringDescribedNumber large = new StringDescribedNumber(maxInteger);
        assertTrue(small.isOfSameType(large));
    }

    @Test
    @DisplayName("correctly compare values")
    void comparisonTest() {
        String smallerValue = "0.1";
        String largerValue = "15";
        StringDescribedNumber smaller = new StringDescribedNumber(smallerValue);
        StringDescribedNumber larger = new StringDescribedNumber(largerValue);
        int comparison = smaller.compareTo(larger);
        assertTrue(comparison < 0);
    }
}
