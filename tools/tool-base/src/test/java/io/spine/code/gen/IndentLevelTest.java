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

package io.spine.code.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.code.gen.IndentLevel.zero;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`IndentationLevel` should")
class IndentLevelTest {

    @Test
    @DisplayName("not be negative")
    void notNegative() {
        assertIllegalArgument(() -> IndentLevel.of(-1));
    }

    @Test
    @DisplayName("provide incremented value")
    void provideIncremented() {
        IndentLevel zero = IndentLevel.zero();
        IndentLevel incremented = zero.incremented();
        assertEquals(0, zero.value());
        assertEquals(1, incremented.value());
    }

    @Test
    @DisplayName("provide decremented value")
    void provideDecremented() {
        IndentLevel five = IndentLevel.of(5);
        IndentLevel decremented = five.decremented();
        assertEquals(5, five.value());
        assertEquals(4, decremented.value());
    }

    @Test
    @DisplayName("not be decremented to a negative value")
    void notAllowDecrementOfZero() {
        assertIllegalArgument(() -> zero().decremented());
    }

    @Test
    @DisplayName("provide the total indent")
    void indentDepth() {
        Indent indent = Indent.of4();
        IndentLevel level = IndentLevel.of(2);
        Indent totalIndent = level.totalIndent(indent);
        assertEquals(8, totalIndent.getSize());
    }
}
