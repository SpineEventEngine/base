/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("Comparable number should")
class ComparableNumberTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void notAcceptNulls() {
        var tester = new NullPointerTester();
        tester.testAllPublicConstructors(ComparableNumber.class);
        tester.testAllPublicInstanceMethods(new ComparableNumber(42));
    }

    @Nested
    @DisplayName("have a consistent equality relationship")
    class EqualsTests {

        @Test
        @DisplayName("between instances")
        void testEquals() {
            var longMaxValue = String.valueOf(Long.MAX_VALUE);
            var doubleMinValue = String.valueOf(Double.MIN_VALUE);
            new EqualsTester()
                    .addEqualityGroup(new NumberText(1L).toNumber(), new NumberText("1").toNumber())
                    .addEqualityGroup(new NumberText(longMaxValue).toNumber(),
                                      new NumberText(Long.MAX_VALUE).toNumber())
                    .addEqualityGroup(new NumberText(doubleMinValue).toNumber(),
                                      new NumberText(Double.MIN_VALUE).toNumber())
                    .testEquals();
        }

        @Test
        @DisplayName("between instances and primitives")
        void testEqualsBetweenPrimitives() {
            var doubleValue = Double.MAX_VALUE;
            var intValue = Integer.MAX_VALUE;
            var floatValue = Float.MAX_VALUE;
            var longValue = Long.MAX_VALUE;

            new EqualsTester()
                    .addEqualityGroup(doubleValue, new ComparableNumber(doubleValue).doubleValue())
                    .addEqualityGroup(intValue, new ComparableNumber(intValue).intValue())
                    .addEqualityGroup(floatValue, new ComparableNumber(floatValue).floatValue())
                    .addEqualityGroup(longValue, new ComparableNumber(longValue).longValue())
                    .testEquals();
        }
    }
}
