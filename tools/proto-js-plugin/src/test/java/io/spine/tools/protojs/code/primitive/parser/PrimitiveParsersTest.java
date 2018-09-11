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

package io.spine.tools.protojs.code.primitive.parser;

import io.spine.tools.protojs.code.JsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Verify.assertInstanceOf;
import static io.spine.tools.protojs.given.Given.enumField;
import static io.spine.tools.protojs.given.Given.int64Field;

@DisplayName("PrimitiveParsers utility should")
class PrimitiveParsersTest {

    private JsGenerator jsGenerator;

    @BeforeEach
    void setUp() {
        jsGenerator = new JsGenerator();
    }

    @Test
    @DisplayName("create parser for primitive value")
    void createPrimitiveParser() {
        PrimitiveParser parser = PrimitiveParsers.createFor(int64Field(), jsGenerator);
        assertInstanceOf(LongParser.class, parser);
    }

    @Test
    @DisplayName("create parser for enum value")
    void createEnumParser() {
        PrimitiveParser parser = PrimitiveParsers.createFor(enumField(), jsGenerator);
        assertInstanceOf(EnumParser.class, parser);
    }
}
