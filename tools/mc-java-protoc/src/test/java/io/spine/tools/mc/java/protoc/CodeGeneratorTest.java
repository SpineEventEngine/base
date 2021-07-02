/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.protoc;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.StringSubject;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.tools.mc.java.protoc.given.TestInterface;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.Uuids;
import io.spine.tools.protoc.plugin.EnhancedWithCodeGeneration;
import io.spine.tools.protoc.plugin.TestGeneratorsProto;
import io.spine.type.MessageType;
import io.spine.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertNpe;
import static io.spine.tools.mc.java.protoc.given.CodeGeneratorRequestGiven.addInterface;
import static io.spine.tools.mc.java.protoc.given.CodeGeneratorRequestGiven.protocConfig;
import static io.spine.tools.mc.java.protoc.given.CodeGeneratorRequestGiven.requestBuilder;

@DisplayName("`SpineProtoGenerator` should")
final class CodeGeneratorTest {

    private static final String TEST_PROTO_FILE = "spine/tools/protoc/test_generators.proto";

    private Path testPluginConfig;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        testPluginConfig = tempDirPath.resolve("test-spine-mc-java-protoc.pb");
    }

    @DisplayName("process valid `CodeGeneratorRequest`")
    @Test
    void processValidRequest() {
        Uuids uuids = Uuids.newBuilder()
                .addAddInterface(addInterface(TestInterface.class))
                .build();
        SpineProtocConfig config = SpineProtocConfig.newBuilder()
                .setUuids(uuids)
                .build();
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(config, testPluginConfig))
                .build();
        MessageType type = new MessageType(EnhancedWithCodeGeneration.getDescriptor());
        File firstFile = File
                .newBuilder()
                .setName("file.proto")
                .setContent(TestInterface.class.getName() + ',')
                .setInsertionPoint(InsertionPoint.message_implements.forType(type))
                .build();
        File secondFile = File
                .newBuilder()
                .setName("file.proto")
                .setContent("public void test(){}")
                .setInsertionPoint(InsertionPoint.class_scope.forType(type))
                .build();
        TestGenerator firstGenerator = new TestGenerator(new TestCompilerOutput(firstFile),
                                                         new TestCompilerOutput(secondFile));
        CodeGeneratorResponse result = firstGenerator.process(request);

        IterableSubject assertFiles = assertThat(result.getFileList());
        assertFiles
                .hasSize(2);
        assertFiles
                .containsExactly(firstFile, secondFile);
    }

    @DisplayName("concatenate code generated for the same insertion point")
    @Test
    void concatenateGeneratedCode() {
        SpineProtocConfig config = SpineProtocConfig.getDefaultInstance();
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(config, testPluginConfig))
                .build();
        MessageType type = new MessageType(EnhancedWithCodeGeneration.getDescriptor());
        String firstMethod = "public void test1(){}";
        String secondMethod = "public void test2(){}";
        File firstFile = File
                .newBuilder()
                .setName("file.proto")
                .setContent(firstMethod)
                .setInsertionPoint(InsertionPoint.class_scope.forType(type))
                .build();
        File secondFile = firstFile
                .toBuilder()
                .setContent(secondMethod)
                .build();
        ImmutableList<CompilerOutput> compilerOutputs = ImmutableList.of(
                new TestCompilerOutput(firstFile), new TestCompilerOutput(secondFile)
        );
        TestGenerator generator = new TestGenerator(compilerOutputs);

        CodeGeneratorResponse result = generator.process(request);
        assertThat(result.getFileList())
                .hasSize(1);
        File file = result.getFile(0);
        StringSubject assertFileContent = assertThat(file.getContent());
        assertFileContent
                .contains(firstMethod);
        assertFileContent
                .contains(secondMethod);
    }

    @DisplayName("drop duplicates in generated code for the same insertion point")
    @Test
    void dropCodeDuplicates() {
        SpineProtocConfig config = SpineProtocConfig.getDefaultInstance();
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(config, testPluginConfig))
                .build();
        MessageType type = new MessageType(EnhancedWithCodeGeneration.getDescriptor());
        String method = "public void test1(){}";
        File generated = File
                .newBuilder()
                .setName("file.proto")
                .setContent(method)
                .setInsertionPoint(InsertionPoint.class_scope.forType(type))
                .build();
        ImmutableList<CompilerOutput> compilerOutputs = ImmutableList.of(
                new TestCompilerOutput(generated), new TestCompilerOutput(generated)
        );
        TestGenerator generator = new TestGenerator(compilerOutputs);

        CodeGeneratorResponse result = generator.process(request);
        assertThat(result.getFileList())
                .hasSize(1);
        File file = result.getFile(0);
        StringSubject fileContent = assertThat(file.getContent());
        fileContent
                .isEqualTo(method);
    }

    @Nested
    @DisplayName("not process invalid `CodeGeneratorRequest` if passed")
    class Arguments {

        @Test
        @DisplayName("`null`")
        void nullArg() {
            assertNpe(() -> process(null));
        }

        @Test
        @DisplayName("unsupported version")
        void notProcessInvalidRequests() {
            assertIllegalArgument(() -> process(requestWithUnsupportedVersion()));
        }

        @Test
        @DisplayName("empty request")
        void emptyRequest() {
            assertIllegalArgument(() -> process(requestBuilder().build()));
        }

        private void process(CodeGeneratorRequest request) {
            new TestGenerator().process(request);
        }
    }

    private static CodeGeneratorRequest requestWithUnsupportedVersion() {
        CodeGeneratorRequest.Builder result = requestBuilder();
        result.setCompilerVersion(result.getCompilerVersionBuilder()
                                        .setMajor(2));
        return result.build();

    }

    private static class TestGenerator extends CodeGenerator {

        private final ImmutableList<CompilerOutput> compilerOutputs;

        private TestGenerator() {
            this(ImmutableList.of());
        }

        private TestGenerator(CompilerOutput... outputs) {
            this(ImmutableList.copyOf(outputs));
        }

        private TestGenerator(ImmutableList<CompilerOutput> outputs) {
            compilerOutputs = outputs;
        }

        @Override
        protected Collection<CompilerOutput> generate(Type<?, ?> type) {
            return compilerOutputs;
        }
    }

    private static class TestCompilerOutput extends AbstractCompilerOutput {

        private TestCompilerOutput(File file) {
            super(file);
        }
    }
}
