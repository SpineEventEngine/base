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
import com.google.protobuf.StringValue;
import io.spine.code.proto.MessageType;
import io.spine.js.generate.JsOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.format;

@DisplayName("TypeUrlMethod should")
class TypeUrlMethodTest {

    private final MessageType type = MessageType.create(StringValue.getDescriptor());
    private final JsOutput output = new JsOutput();
    private final TypeUrlMethod generator = new TypeUrlMethod(type, output);

    @Test
    @DisplayName("provide TypeUrl for a message instance")
    void forInstance() {
        String methodDeclaration = format("proto.%s.typeUrl = function() {", type.name());
        String returnStatement = format("return '%s';", type.url());
        generator.generate();
        assertOutput().contains(methodDeclaration);
        assertOutput().contains(returnStatement);
    }

    @Test
    @DisplayName("provide TypeUrl for a message class")
    void forClass() {
        String methodDeclaration = format("proto.%s.prototype.typeUrl = function() {", type.name());
        String returnStatement = format("return proto.%s.typeUrl();", type.name());
        generator.generate();
        assertOutput().contains(methodDeclaration);
        assertOutput().contains(returnStatement);
    }

    private StringSubject assertOutput() {
        return assertThat(output.toString());
    }
}
