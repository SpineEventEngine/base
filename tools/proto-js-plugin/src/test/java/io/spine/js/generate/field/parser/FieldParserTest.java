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

package io.spine.js.generate.field.parser;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.js.TypeName;
import io.spine.js.generate.output.CodeLines;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.field.given.Given.enumField;
import static io.spine.js.generate.field.given.Given.messageField;
import static io.spine.js.generate.field.given.Given.primitiveField;
import static io.spine.js.generate.field.given.Given.timestampField;
import static io.spine.js.generate.field.parser.FieldParser.parserFor;
import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_OBJECT;
import static io.spine.js.generate.parse.Parser.PARSE_METHOD;
import static java.lang.String.format;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("FieldParser should")
class FieldParserTest {

    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";

    private CodeLines jsOutput;

    @BeforeEach
    void setUp() {
        jsOutput = new CodeLines();
    }

    @Test
    @DisplayName("reject null passed to factory method")
    void nullCheck() {
        new NullPointerTester()
                .setDefault(FieldDescriptor.class, messageField())
                .testAllPublicStaticMethods(FieldParser.class);
    }

    @Test
    @DisplayName("create parser for primitive field")
    void createParserForPrimitive() {
        FieldParser parser = parserFor(primitiveField(), jsOutput);
        assertThat(parser).isInstanceOf(PrimitiveFieldParser.class);
    }

    @Test
    @DisplayName("create parser for enum field")
    void createParserForEnum() {
        FieldParser parser = parserFor(enumField(), jsOutput);
        assertThat(parser).isInstanceOf(EnumFieldParser.class);
    }

    @Test
    @DisplayName("create parser for message field with custom type")
    void createParserForMessage() {
        FieldParser parser = parserFor(messageField(), jsOutput);
        assertThat(parser).isInstanceOf(MessageFieldParser.class);
    }

    @Test
    @DisplayName("create parser for message field with standard type")
    void createParserForWellKnown() {
        FieldParser parser = parserFor(timestampField(), jsOutput);
        assertThat(parser).isInstanceOf(WellKnownFieldParser.class);
    }

    @Test
    @DisplayName("parse primitive field via predefined code")
    void parsePrimitive() {
        FieldParser parser = parserFor(primitiveField(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("parse enum field via JS enum object attribute")
    void parseEnum() {
        FieldParser parser = parserFor(enumField(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        EnumDescriptor enumType = enumField().getEnumType();
        TypeName typeName = TypeName.from(enumType);
        String parse = "let " + VARIABLE + " = " + typeName + '[' + VALUE + ']';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("parse message field with custom type via recursive call to `fromObject`")
    void parseMessage() {
        FieldParser parser = parserFor(messageField(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        Descriptor messageType = messageField().getMessageType();
        TypeName typeName = TypeName.from(messageType);
        String parse = "let " + VARIABLE + " = " + typeName + '.' + FROM_OBJECT + '(' + VALUE + ')';
        assertContains(jsOutput, parse);
    }

    @Test
    @DisplayName("parse message field with standard type via known type parser")
    void parseWellKnown() {
        FieldParser parser = parserFor(timestampField(), jsOutput);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String expectedStatement = format("let %s = parser.%s(%s);",
                                          VARIABLE, PARSE_METHOD, VALUE);
        assertContains(jsOutput, expectedStatement);
    }
}
