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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.code.proto.MessageType;
import io.spine.option.Options;
import io.spine.option.OptionsProto;
import io.spine.protoc.MethodBody;
import io.spine.protoc.MethodFactory;
import io.spine.tools.gradle.compiler.protoc.GeneratedInterfaces;
import io.spine.tools.gradle.compiler.protoc.GeneratedMethods;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Plugin should")
final class PluginTest {

    private static final String TEST_PROTO_POSTFIX = "_generators.proto";
    private static final String TEST_PROTO_FILE = "spine/tools/protoc/test_generators.proto";

    @DisplayName("process code generation request")
    @Test
    void processCodeGenerationRequest() {
        GeneratedInterfaces interfaces = GeneratedInterfaces.withDefaults();
        interfaces.filePattern(interfaces.endsWith(TEST_PROTO_POSTFIX))
                  .markWith(TestInterface.class.getName());
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.filePattern(methods.endsWith(TEST_PROTO_POSTFIX))
               .withMethodFactory(TestMethodFactory.class.getName());
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(encodedProtocConfig(interfaces, methods))
                .build();

        CodeGeneratorResponse response = runPlugin(request);

        assertEquals(2, response.getFileCount());
        CodeGeneratorResponse.File messageInterface = response.getFile(0);
        CodeGeneratorResponse.File messageMethod = response.getFile(1);
        assertEquals(TestInterface.class.getName() + ',', messageInterface.getContent());
        assertEquals(TestMethodFactory.TEST_METHOD.value(), messageMethod.getContent());
    }

    private static String encodedProtocConfig(GeneratedInterfaces interfaces,
                                              GeneratedMethods methods) {
        SpineProtocConfig protocConfig = SpineProtocConfig
                .newBuilder()
                .setGeneratedInterfaces(interfaces.asProtocConfig())
                .setGeneratedMethods(methods.asProtocConfig())
                .build();
        return Base64.getEncoder()
                     .encodeToString(protocConfig.toByteArray());
    }

    private static CodeGeneratorResponse runPlugin(CodeGeneratorRequest request) {
        try (InputStream testInput = new ByteArrayInputStream(request.toByteArray());
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             PrintStream testOutput = new PrintStream(bos)
        ) {
            //noinspection ZeroLengthArrayAllocation
            withSystemStreams(testInput, testOutput, () -> Plugin.main(new String[]{}));
            return CodeGeneratorResponse.parseFrom(bos.toByteArray(), Options.registry());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr") // Required by the protoc API.
    private static void withSystemStreams(InputStream in, PrintStream os, Runnable action) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        try {
            System.setIn(in);
            System.setOut(os);
            action.run();
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    private static CodeGeneratorRequest.Builder requestBuilder() {
        return CodeGeneratorRequest
                .newBuilder()
                .addProtoFile(descriptorProto())
                .addProtoFile(spineOptionsProto())
                .setCompilerVersion(compilerVersion());
    }

    private static PluginProtos.Version compilerVersion() {
        return PluginProtos.Version.newBuilder()
                                   .setMajor(3)
                                   .setMajor(6)
                                   .setPatch(1)
                                   .setSuffix("")
                                   .build();
    }

    private static DescriptorProtos.FileDescriptorProto spineOptionsProto() {
        return OptionsProto.getDescriptor()
                           .toProto();
    }

    private static DescriptorProtos.FileDescriptorProto descriptorProto() {
        return DescriptorProtos.getDescriptor()
                               .toProto();
    }

    @SuppressWarnings("InterfaceNeverImplemented")
    public interface TestInterface {

    }

    @Immutable
    public static class TestMethodFactory implements MethodFactory {

        private static final MethodBody TEST_METHOD = MethodBody.of("public void test(){}");

        @Override
        public List<MethodBody> newMethodsFor(MessageType messageType) {
            return ImmutableList.of(TEST_METHOD);
        }
    }
}
