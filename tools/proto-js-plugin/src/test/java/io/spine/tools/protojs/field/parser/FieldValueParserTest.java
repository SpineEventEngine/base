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

package io.spine.tools.protojs.field.parser;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.tools.protojs.code.JsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.field.parser.FieldValueParsers.parserFor;
import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Given.int64Field;
import static io.spine.tools.protojs.given.Given.messageField;
import static io.spine.tools.protojs.given.Given.timestampField;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

@DisplayName("FieldValueParser should")
class FieldValueParserTest {

    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";

    private JsGenerator jsGenerator;

    @BeforeEach
    void setUp() {
        jsGenerator = new JsGenerator();
    }

    @Test
    @DisplayName("parse primitive field via predefined code")
    void parsePrimitive() {
        FieldValueParser parser = parserFor(int64Field(), jsGenerator);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
        assertContains(jsGenerator, parse);
    }

    @Test
    @DisplayName("parse message field with custom type via recursive call to fromObject")
    void parseMessage() {
        FieldValueParser parser = parserFor(messageField(), jsGenerator);
        parser.parseIntoVariable(VALUE, VARIABLE);
        Descriptor messageType = messageField().getMessageType();
        String type = typeWithProtoPrefix(messageType);
        String parse = "let " + VARIABLE + " = " + type + ".fromObject(" + VALUE + ')';
        assertContains(jsGenerator, parse);
    }

    @Test
    @DisplayName("parse message field with standard type via known type parser")
    void parseWellKnown() {
        FieldValueParser parser = parserFor(timestampField(), jsGenerator);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parser.parse(" + VALUE + ')';
        assertContains(jsGenerator, parse);
    }
}
