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

package io.spine.tools.protojs.code.primitive;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.code.JsImportGenerator.rawNamedImport;
import static io.spine.tools.protojs.code.primitive.BytesParser.BASE64_LIB;
import static io.spine.tools.protojs.code.primitive.BytesParser.BASE64_VAR;
import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Given.bytesField;
import static io.spine.tools.protojs.given.Given.enumField;
import static io.spine.tools.protojs.given.Given.floatField;
import static io.spine.tools.protojs.given.Given.int32Field;
import static io.spine.tools.protojs.given.Given.int64Field;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

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
        PrimitiveParser parser = parserFor(int32Field());
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = " + VALUE;
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing long value")
    void parseLong() {
        PrimitiveParser parser = parserFor(int64Field());
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing float value")
    void parseFloat() {
        PrimitiveParser parser = parserFor(floatField());
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseFloat(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing bytes value")
    void parseBytes() {
        PrimitiveParser parser = parserFor(bytesField());
        parser.parseIntoVariable(VALUE, VARIABLE);
        String base64Import = rawNamedImport(BASE64_LIB, BASE64_VAR);
        assertContains(jsOutput, base64Import);
        String parse = "let " + VARIABLE + " = " + BASE64_VAR + ".toByteArray(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("generate code for parsing enum value")
    void parseEnum() {
        PrimitiveParser parser = parserFor(enumField());
        parser.parseIntoVariable(VALUE, VARIABLE);
        EnumDescriptor enumType = enumField().getEnumType();
        String typeName = typeWithProtoPrefix(enumType);
        String parse = "let " + VARIABLE + " = " + typeName + '[' + VALUE + ']';
        assertContains(jsOutput, parse);
    }

    private PrimitiveParser parserFor(FieldDescriptor field) {
        return PrimitiveParsers.createFor(field, jsOutput);
    }
}
