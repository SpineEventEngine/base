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

package io.spine.code.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertIllegalArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Indent` should")
class IndentTest {

    @Test
    @DisplayName("prohibit negative values")
    void prohibitNegativeValue() {
        assertIllegalArgument(() -> Indent.of(-1));
    }

    @Test
    @DisplayName("allow zero indent")
    void allowZeroIndent() {
        Indent zeroIndent = Indent.of(0);
        assertEquals(0, zeroIndent.getSize());
        assertTrue(zeroIndent.toString()
                             .isEmpty());
    }

    @Test
    @DisplayName("allow custom size")
    void allowCustomSize() {
        Indent ofThree = Indent.of(3);
        assertEquals(3, ofThree.getSize());
        assertEquals("   ", ofThree.toString());
    }

    @Test
    @DisplayName("provide convenient methods")
    void returnPopularConstants() {
        assertEquals(2, Indent.of2()
                              .getSize());
        assertEquals(4, Indent.of4()
                              .getSize());
    }

    @Test
    @DisplayName("return constant for popular values")
    void returnConstantsByPopularValues() {
        assertSame(Indent.of2(), Indent.of(2));
        assertSame(Indent.of4(), Indent.of(4));
    }
}
