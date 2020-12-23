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

package io.spine.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("GenericTypeIndex should")
class GenericTypeIndexTest {

    @Test
    @DisplayName("obtain generic argument assuming generic superclass")
    void obtain_generic_argument_assuming_generic_superclass() {
        Parametrized<Long, String> val = new Parametrized<Long, String>() {};
        assertEquals(Long.class, Types.argumentIn(val.getClass(), Base.class, 0));
        assertEquals(String.class, Types.argumentIn(val.getClass(), Base.class, 1));
    }

    @Test
    @DisplayName("obtain generic argument via superclass")
    void obtain_generic_argument_via_superclass() {
        assertEquals(String.class, Types.argumentIn(Leaf.class, Base.class, 0));
        assertEquals(Float.class, Types.argumentIn(Leaf.class, Base.class, 1));
    }

    @SuppressWarnings({"EmptyClass", "unused"})
    private static class Base<T, K> {}

    private static class Parametrized<T, K> extends Base<T, K> {}

    private static class Leaf extends Base<String, Float> {}
}
