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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.Type;
import io.spine.js.generate.JsOutput;
import io.spine.js.generate.Method;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.given.GivenProject.mainProtoSources;
import static io.spine.js.generate.typeurl.TypeUrlMethods.typeUrlMethod;
import static io.spine.js.generate.typeurl.given.Given.enumType;
import static io.spine.js.generate.typeurl.given.Given.messageType;
import static java.lang.String.format;

@DisplayName("TypeUrlsInFile should")
class TypeUrlMethodsTest {

    private final FileDescriptor file = OuterMessage.getDescriptor()
                                                    .getFile();

    @Nested
    @DisplayName("look up messages")
    class LookUpMessages {

        @Test
        @DisplayName("declared at the top level")
        void topLevel() {
            assertTypeUrl(OuterMessage.getDescriptor());
        }

        @Test
        @DisplayName("nested in a message")
        void nested() {
            assertTypeUrl(OuterMessage.NestedMessage.getDescriptor());
        }

        private void assertTypeUrl(Descriptor message) {
            TypeUrl typeUrl = TypeUrl.from(message);
            assertHasTypeUrl(typeUrl);
        }
    }

    @Nested
    @DisplayName("look up enums")
    class LookUpEnums {

        @Test
        @DisplayName("declared at the top level")
        void topLevel() {
            assertOutHasTypeUrl(TopLevelEnum.getDescriptor());
        }

        @Test
        @DisplayName("nested in a message")
        void nested() {
            assertOutHasTypeUrl(OuterMessage.NestedEnum.getDescriptor());
        }

        private void assertOutHasTypeUrl(EnumDescriptor enumDescriptor) {
            TypeUrl typeUrl = TypeUrl.from(enumDescriptor);
            assertHasTypeUrl(typeUrl);
        }
    }

    @Nested
    @DisplayName("generate the method")
    class GenerateMethod {

        @Test
        @DisplayName("for a message class")
        void forMessageClass() {
            assertTypeUrlMethod(messageType());
        }

        @Test
        @DisplayName("for a enum class")
        void forEnumClass() {
            assertTypeUrlMethod(enumType());
        }

        private void assertTypeUrlMethod(Type type) {
            String methodDeclaration = format("proto.%s.typeUrl = function() {", type.name());
            String returnStatement = format("return '%s';", type.url());
            String endOfMethod = "};";
            Method method = typeUrlMethod(type);
            String methodLines = method.value()
                                       .toString();
            assertThat(methodLines).contains(methodDeclaration);
            assertThat(methodLines).contains(returnStatement);
            assertThat(methodLines).contains(endOfMethod);
        }
    }

    private void assertHasTypeUrl(TypeUrl typeUrl) {
        JsOutput out = typeUrlMethods().value();
        assertThat(out.toString()).contains(typeUrl.value());
    }

    private TypeUrlMethods typeUrlMethods() {
        return new TypeUrlMethods(file, mainProtoSources());
    }
}
