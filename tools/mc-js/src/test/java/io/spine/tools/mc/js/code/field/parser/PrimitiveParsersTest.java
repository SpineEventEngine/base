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

package io.spine.tools.mc.js.code.field.parser;

import io.spine.tools.mc.js.code.output.CodeLines;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;

@DisplayName("PrimitiveParsers utility should")
class PrimitiveParsersTest extends UtilityClassTest<PrimitiveParsers> {

    private CodeLines jsOutput;

    PrimitiveParsersTest() {
        super(PrimitiveParsers.class);
    }

    @BeforeEach
    void setUp() {
        jsOutput = new CodeLines();
    }

    @Test
    @DisplayName("create identity parser")
    void createIdentityParser() {
        PrimitiveParser parser = PrimitiveParsers.createFor(INT32, jsOutput);
        assertThat(parser).isInstanceOf(IdentityParser.class);
    }

    @Test
    @DisplayName("create parser for long value")
    void createLongParser() {
        PrimitiveParser parser = PrimitiveParsers.createFor(INT64, jsOutput);
        assertThat(parser).isInstanceOf(LongParser.class);
    }

    @Test
    @DisplayName("create parser for float value")
    void createFloatParser() {
        PrimitiveParser parser = PrimitiveParsers.createFor(FLOAT, jsOutput);
        assertThat(parser).isInstanceOf(FloatParser.class);
    }

    @Test
    @DisplayName("create parser for bytes value")
    void createBytesParser() {
        PrimitiveParser parser = PrimitiveParsers.createFor(BYTES, jsOutput);
        assertThat(parser).isInstanceOf(BytesParser.class);
    }
}
