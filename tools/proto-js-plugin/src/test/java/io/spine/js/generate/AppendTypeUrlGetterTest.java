/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.js.generate;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.Method;
import io.spine.js.generate.typeurl.OuterMessage;
import io.spine.js.generate.typeurl.OuterMessage.NestedEnum;
import io.spine.js.generate.typeurl.OuterMessage.NestedMessage;
import io.spine.js.generate.typeurl.TopLevelEnum;
import io.spine.type.MessageType;
import io.spine.type.ServiceType;
import io.spine.type.Type;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.AppendTypeUrlGetter.typeUrlMethod;
import static io.spine.js.generate.AppendTypeUrlGetter.typeUrlMethods;
import static io.spine.js.generate.given.Given.enumType;
import static io.spine.js.generate.given.Given.messageType;
import static java.lang.String.format;

@DisplayName("AppendTypeUrlGetter should")
class AppendTypeUrlGetterTest {

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
            assertTypeUrl(NestedMessage.getDescriptor());
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
            assertOutHasTypeUrl(NestedEnum.getDescriptor());
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
            Method method = typeUrlMethod(messageType());
            CodeLines codeLines = method.value();
            checkMethodForTypePresent(codeLines, messageType());
        }

        @Test
        @DisplayName("for an enum class")
        void forEnumClass() {
            Method method = typeUrlMethod(enumType());
            CodeLines codeLines = method.value();
            checkMethodForTypePresent(codeLines, enumType());
        }
    }

    @Test
    @DisplayName("skip service definitions")
    void skipServiceDefinitions() {
        FileDescriptor file = TaskServiceProto.getDescriptor();
        ServiceDescriptor service = file.findServiceByName("TaskService");
        CodeLines output = typeUrlMethods(file);

        ServiceType serviceType = ServiceType.of(service);
        checkMethodForTypeNotPresent(output, serviceType);

        MessageType messageType = new MessageType(TaskCount.getDescriptor());
        checkMethodForTypePresent(output, messageType);
    }

    private static void checkMethodForTypePresent(CodeLines codeLines, Type type) {
        String methodDeclaration = methodDeclaration(type);
        String returnStatement = format("return '%s';", type.url());
        String endOfMethod = "};";
        String code = codeLines.toString();
        assertThat(code).contains(methodDeclaration);
        assertThat(code).contains(returnStatement);
        assertThat(code).contains(endOfMethod);
    }

    private static void checkMethodForTypeNotPresent(CodeLines codeLines, Type type) {
        String declaration = methodDeclaration(type);
        String code = codeLines.toString();
        assertThat(code).doesNotContain(declaration);
    }

    private static String methodDeclaration(Type type) {
        String result = format("proto.%s.typeUrl = function() {", type.name());
        return result;
    }

    private void assertHasTypeUrl(TypeUrl typeUrl) {
        CodeLines out = AppendTypeUrlGetter.typeUrlMethods(file);
        assertThat(out.toString()).contains(typeUrl.value());
    }
}
