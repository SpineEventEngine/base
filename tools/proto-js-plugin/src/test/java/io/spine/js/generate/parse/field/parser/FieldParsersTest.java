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

package io.spine.js.generate.parse.field.parser;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.js.generate.JsOutput;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.parse.field.parser.FieldParsers.parserFor;
import static io.spine.js.generate.given.Given.enumField;
import static io.spine.js.generate.given.Given.messageField;
import static io.spine.js.generate.given.Given.primitiveField;
import static io.spine.js.generate.given.Given.timestampField;

@DisplayName("FieldParsers utility should")
class FieldParsersTest extends UtilityClassTest<FieldParsers> {

    private JsOutput jsOutput;

    FieldParsersTest() {
        super(FieldParsers.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldDescriptor.class, messageField());
    }

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
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
}
