/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Comparable number should")
class ComparableNumberTest {

    @Nested
    @DisplayName("have a consistent equality relationship")
    class EqualsTests {

        @Test
        @DisplayName("between instances")
        void testEquals() {
            String longMaxValue = String.valueOf(Long.MAX_VALUE);
            String doubleMinValue = String.valueOf(Double.MIN_VALUE);
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
            double doubleValue = Double.MAX_VALUE;
            int intValue = Integer.MAX_VALUE;
            float floatValue = Float.MAX_VALUE;
            long longValue = Long.MAX_VALUE;

            new EqualsTester()
                    .addEqualityGroup(doubleValue, new ComparableNumber(doubleValue).doubleValue())
                    .addEqualityGroup(intValue, new ComparableNumber(intValue).intValue())
                    .addEqualityGroup(floatValue, new ComparableNumber(floatValue).floatValue())
                    .addEqualityGroup(longValue, new ComparableNumber(longValue).longValue())
                    .testEquals();
        }
    }
}
