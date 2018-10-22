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

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Preconditions2 utility class should")
class Preconditions2Test extends UtilityClassTest<Preconditions2> {

    Preconditions2Test() {
        super(Preconditions2.class);
    }

    @Nested
    @DisplayName("Check that a String is")
    class StringArg {

        private void assertThrowsOn(String arg) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> checkNotEmptyOrBlank(arg)
            );
        }

        @Test
        @DisplayName("empty")
        void emptyString() {
            assertThrowsOn("");
        }

        @Test
        @DisplayName("blank")
        void blankString() {
            assertThrowsOn(" ");
            assertThrowsOn("  ");
            assertThrowsOn("   ");
        }
    }
}
