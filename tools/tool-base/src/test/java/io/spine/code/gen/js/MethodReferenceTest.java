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

package io.spine.code.gen.js;

import com.google.protobuf.Any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MethodReference should")
class MethodReferenceTest {

    private static final String METHOD_NAME = "method";

    private final TypeName typeName = TypeName.from(Any.getDescriptor());

    @Test
    @DisplayName("provide reference to an instance method")
    void instanceMethod() {
        MethodReference reference = MethodReference.onPrototype(typeName, METHOD_NAME);
        String expectedName = "proto.google.protobuf.Any.prototype.method";
        assertEquals(expectedName, reference.value());
    }

    @Test
    @DisplayName("provide reference to a static method")
    void staticMethod() {
        MethodReference reference = MethodReference.onType(typeName, METHOD_NAME);
        String expectedName = "proto.google.protobuf.Any.method";
        assertEquals(expectedName, reference.value());
    }

    @Test
    @DisplayName("provide reference to a constructor")
    void constructor() {
        MethodReference reference = MethodReference.constructor(typeName);
        String expected = "proto.google.protobuf.Any";
        assertEquals(expected, reference.value());
    }
}
