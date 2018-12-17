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

package io.spine.js.generate.typeurl;

import com.google.common.truth.StringSubject;
import io.spine.code.proto.Type;
import io.spine.js.generate.JsOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.typeurl.given.Given.enumType;
import static io.spine.js.generate.typeurl.given.Given.messageType;
import static java.lang.String.format;

@DisplayName("TypeUrlMethod should")
class TypeUrlMethodTest {

    private final JsOutput output = new JsOutput();

    @Test
    @DisplayName("provide TypeUrl for a message class")
    void forMessageClass() {
        assertTypeUrlForClass(messageType());
    }

    @Test
    @DisplayName("provide TypeUrl for a enum class")
    void forEnumClass() {
        assertTypeUrlForClass(enumType());
    }

    @Test
    @DisplayName("not provide TypeUrl for a message instance")
    void forMessageInstance() {
        assertNoTypeUrlForInstance(messageType());
    }

    @Test
    @DisplayName("not provide TypeUrl for a enum instance")
    void forEnumInstance() {
        assertNoTypeUrlForInstance(enumType());
    }

    private StringSubject assertOutput() {
        return assertThat(output.toString());
    }

    private void assertTypeUrlForClass(Type type) {
        String methodDeclaration = format("proto.%s.typeUrl = function() {", type.name());
        String returnStatement = format("return '%s';", type.url());
        TypeUrlMethod method = newMethod(type);
        method.generate();
        assertOutput().contains(methodDeclaration);
        assertOutput().contains(returnStatement);
    }

    private void assertNoTypeUrlForInstance(Type type) {
        TypeUrlMethod method = newMethod(type);
        method.generate();
        assertOutput().doesNotContain(".prototype.");
    }

    private TypeUrlMethod newMethod(Type type) {
        return new TypeUrlMethod(type, output);
    }
}
