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

package io.spine.tools.validate.number;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.validate.number.NumberBoundaries.unbound;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`NumberBoundaries` should")
class NumberBoundariesTest {

    @Test
    @DisplayName("have consistent `equals()`")
    void equality() {
        EqualsTester tester = new EqualsTester()
                .addEqualityGroup(unbound(), unbound(), new NumberBoundaries(null, null))
                .addEqualityGroup(
                        new NumberBoundaries(new Boundary(42, true), new Boundary(314, false)),
                        new NumberBoundaries(new Boundary(42, true), new Boundary(314, false))
                );
        tester.testEquals();
    }

    @Test
    @DisplayName("not allow min value be greater than max value")
    void notAllowMinGtMax() {
        Boundary min = new Boundary(42, false);
        Boundary max = new Boundary(-1, false);
        assertThrows(IllegalArgumentException.class, () -> new NumberBoundaries(min, max));
    }
}
