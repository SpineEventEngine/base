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

import io.spine.tools.mc.js.code.CodeWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static io.spine.tools.mc.js.code.field.parser.BytesParser.BASE64_LIB;
import static io.spine.tools.mc.js.code.field.parser.BytesParser.BASE64_VAR;
import static io.spine.tools.mc.js.code.given.Generators.assertContains;

@DisplayName("`PrimitiveParser` should")
class PrimitiveParserTest {

    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";

    /** The parser under the test. */
    private Parser parser;

    /** The writer for the code output. */
    private CodeWriter writer;

    @BeforeEach
    void createWriter() {
        writer = new CodeWriter();
    }

    @Nested
    @DisplayName("create a parser for")
    class Creating {

        private void assertIsInstanceOf(Class<?> cls) {
            assertThat(parser).isInstanceOf(cls);
        }

        @Test
        @DisplayName("identity")
        void createIdentityParser() {
            parser = PrimitiveTypeParser.createFor(INT32, writer);
            assertIsInstanceOf(IdentityParser.class);
        }

        @Test
        @DisplayName("long value")
        void createLongParser() {
            parser = PrimitiveTypeParser.createFor(INT64, writer);
            assertIsInstanceOf(LongParser.class);
        }

        @Test
        @DisplayName("float value")
        void createFloatParser() {
            parser = PrimitiveTypeParser.createFor(FLOAT, writer);
            assertIsInstanceOf(FloatParser.class);
        }

        @Test
        @DisplayName("bytes value")
        void createBytesParser() {
            parser = PrimitiveTypeParser.createFor(BYTES, writer);
            assertIsInstanceOf(BytesParser.class);
        }
    }

    @Nested
    @DisplayName("generate code for parsing")
    class GeneratingCode {

        @Test
        @DisplayName("value to itself")
        void parseIdentically() {
            parser = PrimitiveTypeParser.createFor(INT32, writer);
            parser.parseIntoVariable(VALUE, VARIABLE);
            String parse = "let " + VARIABLE + " = " + VALUE;
            assertContains(writer, parse);
        }

        @Test
        @DisplayName("long value")
        void parseLong() {
            parser = PrimitiveTypeParser.createFor(INT64, writer);
            parser.parseIntoVariable(VALUE, VARIABLE);
            String parse = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
            assertContains(writer, parse);
        }

        @Test
        @DisplayName("float value")
        void parseFloat() {
            parser = PrimitiveTypeParser.createFor(FLOAT, writer);
            parser.parseIntoVariable(VALUE, VARIABLE);
            String parse = "let " + VARIABLE + " = parseFloat(" + VALUE + ')';
            assertContains(writer, parse);
        }

        @Test
        @DisplayName("bytes value")
        void parseBytes() {
            parser = PrimitiveTypeParser.createFor(BYTES, writer);
            parser.parseIntoVariable(VALUE, VARIABLE);
            String base64Import = "let " + BASE64_VAR + " = require('" + BASE64_LIB + "');";
            assertContains(writer, base64Import);
            String parse = "let " + VARIABLE + " = " + BASE64_VAR + ".toByteArray(" + VALUE + ')';
            assertContains(writer, parse);
        }
    }
}
