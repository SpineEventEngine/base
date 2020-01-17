/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.output.snippet;

import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.js.generate.given.GivenMethod.methodReference;
import static java.lang.System.lineSeparator;

@DisplayName("Method should")
class MethodTest {

    @Test
    @DisplayName("assemble an empty no args method")
    void emptyNoArgs() {
        Method method = newMethod().build();
        String expectedRepresentation = expectedNoArgsDeclaration() + lineSeparator()
                + "};";
        assertThat(method).isEqualTo(expectedRepresentation);
    }

    @Test
    @DisplayName("assemble an empty method with arguments")
    void emptyWithArgs() {
        String argument = "methodArgument";
        Method method = newMethod()
                .withParameters(argument)
                .build();
        String expectedRepresentation = methodReference() + " = function(methodArgument) {"
                + lineSeparator()
                + "};";
        assertThat(method).isEqualTo(expectedRepresentation);
    }

    @Test
    @DisplayName("assemble a method with body")
    void nonEmpty() {
        Method method = newMethod()
                .appendToBody("statement1;")
                .appendToBody("statement2;")
                .build();
        String expectedRepresentation = expectedNoArgsDeclaration() + lineSeparator()
                + "  statement1;" + lineSeparator()
                + "  statement2;" + lineSeparator()
                + "};";
        assertThat(method).isEqualTo(expectedRepresentation);
    }

    private static String expectedNoArgsDeclaration() {
        return methodReference() + " = function() {";
    }

    private static Method.Builder newMethod() {
        return Method.newBuilder(methodReference());
    }

    private static StringSubject assertThat(Method method) {
        String rawMethod = method.value()
                                 .toString();
        return Truth.assertThat(rawMethod);
    }
}
