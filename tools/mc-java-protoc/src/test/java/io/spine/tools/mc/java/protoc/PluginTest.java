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
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.code.java.ClassName;
import io.spine.code.proto.OptionExtensionRegistry;
import io.spine.tools.java.fs.SourceFile;
import io.spine.tools.java.protoc.Interfaces;
import io.spine.tools.java.protoc.MessageSelectorFactory;
import io.spine.tools.java.protoc.Methods;
import io.spine.tools.java.protoc.NestedClasses;
import io.spine.tools.java.protoc.PatternSelector;
import io.spine.tools.mc.java.protoc.given.TestInterface;
import io.spine.tools.mc.java.protoc.given.TestMethodFactory;
import io.spine.tools.mc.java.protoc.given.TestNestedClassFactory;
import io.spine.tools.mc.java.protoc.given.UuidMethodFactory;
import io.spine.tools.protoc.plugin.EnhancedWithCodeGeneration;
import io.spine.tools.protoc.plugin.TestGeneratorsProto;
import io.spine.tools.protoc.plugin.method.TestMethodProtos;
import io.spine.type.MessageType;
import io.spine.validate.ValidatingBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.java.protoc.MessageSelectorFactory.prefix;
import static io.spine.tools.java.protoc.MessageSelectorFactory.regex;
import static io.spine.tools.java.protoc.MessageSelectorFactory.suffix;
import static io.spine.tools.mc.java.protoc.given.CodeGeneratorRequestGiven.protocConfig;
import static io.spine.tools.mc.java.protoc.given.CodeGeneratorRequestGiven.requestBuilder;
import static io.spine.tools.mc.java.protoc.given.TestMethodFactory.TEST_METHOD;
import static io.spine.tools.mc.java.protoc.given.TestNestedClassFactory.NESTED_CLASS;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`Plugin` should")
final class PluginTest {

    private static final String TEST_PROTO_SUFFIX = "_generators.proto";
    private static final String TEST_PROTO_PREFIX = "spine/tools/protoc/test_";
    private static final String TEST_PROTO_REGEX = ".*protoc/.*rators.pro.*";
    private static final String TEST_PROTO_FILE = "spine/tools/protoc/test_generators.proto";
    private static final String TEST_MESSAGE_TYPE_PARAMETER = "<EnhancedWithCodeGeneration>";
    private static final String BUILDER_INTERFACE =
            ValidatingBuilder.class.getName() + TEST_MESSAGE_TYPE_PARAMETER + ',';

    private Path testPluginConfig;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        testPluginConfig = tempDirPath.resolve("test-spine-protoc-plugin.pb");
    }

    @Test
    @DisplayName("process suffix patterns")
    void processSuffixPatterns() {
        Interfaces interfaces = new Interfaces();
        MessageSelectorFactory messages = interfaces.messages();
        PatternSelector suffixSelector = messages.inFiles(suffix(TEST_PROTO_SUFFIX));
        interfaces.mark(suffixSelector, ClassName.of(TestInterface.class));
        Methods methods = new Methods();
        methods.applyFactory(TestMethodFactory.class.getName(), suffixSelector);
        NestedClasses nestedClasses = new NestedClasses();
        nestedClasses.applyFactory(TestNestedClassFactory.class.getName(), suffixSelector);
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(interfaces, methods, nestedClasses, testPluginConfig))
                .build();

        CodeGeneratorResponse response = runPlugin(request);
        checkGenerated(response);
    }

    @Test
    @DisplayName("generate UUID message")
    void generateUuidMethod() {
        Methods methods = new Methods();
        MessageSelectorFactory messages = methods.messages();
        methods.applyFactory(UuidMethodFactory.class.getName(), messages.uuid());

        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestMethodProtos.getDescriptor()
                                              .toProto())
                .addFileToGenerate("spine/tools/protoc/method/test_protos.proto")
                .setParameter(protocConfig(methods, testPluginConfig))
                .build();
        CodeGeneratorResponse response = runPlugin(request);

        List<File> messageMethods =
                filterFiles(response, InsertionPoint.class_scope);
        assertEquals(1, messageMethods.size());
    }

    @Test
    @DisplayName("process prefix patterns")
    void processPrefixPatterns() {
        Interfaces interfaces = new Interfaces();
        MessageSelectorFactory messages = interfaces.messages();
        PatternSelector prefixSelector = messages.inFiles(prefix(TEST_PROTO_PREFIX));
        interfaces.mark(prefixSelector, ClassName.of(TestInterface.class));
        Methods methods = new Methods();
        methods.applyFactory(TestMethodFactory.class.getName(), prefixSelector);
        NestedClasses nestedClasses = new NestedClasses();
        nestedClasses.applyFactory(TestNestedClassFactory.class.getName(), prefixSelector);

        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(interfaces, methods, nestedClasses, testPluginConfig))
                .build();

        CodeGeneratorResponse response = runPlugin(request);
        checkGenerated(response);
    }

    @Test
    @DisplayName("process regex patterns")
    void processRegexPatterns() {
        Interfaces interfaces = new Interfaces();
        MessageSelectorFactory messages = interfaces.messages();
        PatternSelector regexSelector = messages.inFiles(regex(TEST_PROTO_REGEX));
        interfaces.mark(regexSelector, ClassName.of(TestInterface.class));
        Methods methods = new Methods();
        methods.applyFactory(TestMethodFactory.class.getName(), regexSelector);
        NestedClasses nestedClasses = new NestedClasses();
        nestedClasses.applyFactory(TestNestedClassFactory.class.getName(), regexSelector);

        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(interfaces, methods, nestedClasses, testPluginConfig))
                .build();

        CodeGeneratorResponse response = runPlugin(request);
        checkGenerated(response);
    }

    @Test
    @DisplayName("mark generated message builders with the `ValidatingBuilder` interface")
    void markBuildersWithInterface() {
        FileDescriptor testGeneratorsDescriptor = TestGeneratorsProto.getDescriptor();
        String protocConfigPath = protocConfig(new Interfaces(),
                                               new Methods(),
                                               new NestedClasses(),
                                               testPluginConfig);
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(testGeneratorsDescriptor.toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfigPath)
                .build();
        CodeGeneratorResponse response = runPlugin(request);

        MessageType type = new MessageType(EnhancedWithCodeGeneration.getDescriptor());
        String expectedPointName = InsertionPoint.builder_implements.forType(type);

        SourceFile expectedSourceFile = SourceFile.forType(type);
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
        String expectedFileName = expectedSourceFile.toString()
                                                    .replaceAll("\\\\", "/");
        File expectedFile = File
                .newBuilder()
                .setName(expectedFileName)
                .setInsertionPoint(expectedPointName)
                .setContent(BUILDER_INTERFACE)
                .build();
        assertThat(response.getFileList()).contains(expectedFile);
    }

    /**
     * Selects all files from the given response which contain the specified insertion point.
     */
    private static List<File> filterFiles(CodeGeneratorResponse response,
                                          InsertionPoint insertionPoint) {
        return response
                .getFileList()
                .stream()
                .filter(file -> file.getInsertionPoint()
                                    .contains(insertionPoint.getDefinition()))
                .collect(toList());
    }

    @SuppressWarnings("ZeroLengthArrayAllocation")
    private static CodeGeneratorResponse runPlugin(CodeGeneratorRequest request) {
        try (InputStream testInput = new ByteArrayInputStream(request.toByteArray());
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             PrintStream testOutput = new PrintStream(bos)
        ) {
            withSystemStreams(testInput, testOutput, () -> Plugin.main(new String[]{}));
            return CodeGeneratorResponse.parseFrom(bos.toByteArray(),
                                                   OptionExtensionRegistry.instance());
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

    private static void checkGenerated(CodeGeneratorResponse response) {
        List<File> responseFiles = response.getFileList();
        assertThat(responseFiles.size()).isAtLeast(3);
        List<String> fileContents = contentsOf(responseFiles);
        assertThat(fileContents).containsAtLeast(
                TestInterface.class.getName() + ',',
                BUILDER_INTERFACE
        );
        ImmutableList<String> possibleInsertions = ImmutableList.of(
                TEST_METHOD + NESTED_CLASS.toString(),
                NESTED_CLASS + TEST_METHOD.toString()
        );
        assertThat(fileContents).containsAnyIn(possibleInsertions);
    }

    private static List<String> contentsOf(List<File> files) {
        return files.stream()
                    .map(File::getContent)
                    .collect(toList());
    }
}
