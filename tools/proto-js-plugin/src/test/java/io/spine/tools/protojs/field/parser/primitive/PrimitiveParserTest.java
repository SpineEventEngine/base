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

package io.spine.tools.protojs.field.parser.primitive;

import io.spine.tools.protojs.code.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.code.JsImportGenerator.rawNamedImport;
import static io.spine.tools.protojs.field.parser.primitive.BytesParser.BASE64_LIB;
import static io.spine.tools.protojs.field.parser.primitive.BytesParser.BASE64_VAR;
import static io.spine.tools.protojs.field.parser.primitive.given.PrimitiveParserTestEnv.bytesType;
import static io.spine.tools.protojs.field.parser.primitive.given.PrimitiveParserTestEnv.floatType;
import static io.spine.tools.protojs.field.parser.primitive.given.PrimitiveParserTestEnv.int32Type;
import static io.spine.tools.protojs.field.parser.primitive.given.PrimitiveParserTestEnv.int64Type;
import static io.spine.tools.protojs.given.Generators.assertContains;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("PrimitiveParser should")
class PrimitiveParserTest {

    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";

    private JsOutput jsOutput;

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
    }

    @Test
    @DisplayName("generate code for parsing value identically")
    void parseIdentically() {
        PrimitiveParser parser = PrimitiveParsers.createFor(int32Type(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = " + VALUE;
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing long value")
    void parseLong() {
        PrimitiveParser parser = PrimitiveParsers.createFor(int64Type(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing float value")
    void parseFloat() {
        PrimitiveParser parser = PrimitiveParsers.createFor(floatType(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseFloat(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing bytes value")
    void parseBytes() {
        PrimitiveParser parser = PrimitiveParsers.createFor(bytesType(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String base64Import = rawNamedImport(BASE64_LIB, BASE64_VAR);
        assertContains(jsOutput, base64Import);
        String parse = "let " + VARIABLE + " = " + BASE64_VAR + ".toByteArray(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }
}
