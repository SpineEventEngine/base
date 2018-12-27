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

package io.spine.js.generate.parse;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.js.TypeName;
import io.spine.js.generate.output.CodeLines;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_OBJECT;
import static io.spine.js.generate.parse.Parser.FROM_OBJECT_ARG;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("Parser should")
class ParserTest {

    private final Descriptor message = Any.getDescriptor();
    private final Parser parser = new Parser(message);

    @Test
    @DisplayName("generate `fromObject` method for message")
    void generateFromObject() {
        CodeLines snippet = parser.fromObjectMethod();
        String expectedName = expectedParserName(message) + ".prototype." + FROM_OBJECT;
        String methodDeclaration = expectedName + " = function(" + FROM_OBJECT_ARG;
        assertContains(snippet, methodDeclaration);
    }

    @Test
    @DisplayName("check parsed object for null in `fromObject` method")
    void checkJsObjectForNull() {
        CodeLines snippet = parser.fromObjectMethod();
        String check = "if (" + FROM_OBJECT_ARG + " === null) {";
        assertContains(snippet, check);
    }

    @SuppressWarnings("AccessStaticViaInstance") // For the testing purpose.
    @Test
    @DisplayName("handle message fields in `fromObject` method")
    void handleMessageFields() {
        Parser generator = spy(this.parser);
        CodeLines snippet = generator.fromObjectMethod();
        verify(generator, times(1))
                .handleMessageFields(new CodeLines(), message);
        assertNotNull(snippet);
    }

    @Test
    @DisplayName("generate whole snippet")
    void generateWholeSnippet() {
        Descriptor message = Any.getDescriptor();
        Parser parser = new Parser(message);
        CodeLines lines = parser.value();
        assertCtorDeclaration(lines, message);
        assertPrototypeInitialization(lines, message);
        assertCtorInitialization(lines, message);
        assertParseMethod(lines, message);
    }

    private static void assertCtorDeclaration(CodeLines lines, Descriptor message) {
        String expected = expectedParserName(message) + " = function() {" + lineSeparator()
                + "  ObjectParser.call(this);" + lineSeparator()
                + "};";
        assertThat(lines.toString()).contains(expected);
    }

    private static void assertPrototypeInitialization(CodeLines lines, Descriptor message) {
        assertThat(lines.toString()).contains(
                expectedParserName(message) + ".prototype = Object.create(ObjectParser.prototype);"
        );
    }

    private static void assertCtorInitialization(CodeLines lines, Descriptor message) {
        String expectedName = expectedParserName(message);
        assertThat(lines.toString()).contains(
                expectedName + ".prototype.constructor = " + expectedName + ';'
        );
    }

    private static void assertParseMethod(CodeLines lines, Descriptor message) {
        String expected = new Parser(message).fromObjectMethod()
                                             .toString();
        assertThat(lines.toString()).contains(expected);
    }

    private static String expectedParserName(Descriptor message) {
        return TypeName.from(message) + "Parser";
    }
}
