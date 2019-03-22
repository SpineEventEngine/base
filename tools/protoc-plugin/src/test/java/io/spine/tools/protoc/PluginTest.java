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

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.code.java.ClassName;
import io.spine.option.OptionExtensionRegistry;
import io.spine.tools.gradle.compiler.protoc.GeneratedInterfaces;
import io.spine.tools.gradle.compiler.protoc.GeneratedMethods;
import io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory;
import io.spine.tools.gradle.compiler.protoc.PatternSelector;
import io.spine.tools.protoc.given.TestInterface;
import io.spine.tools.protoc.given.TestMethodFactory;
import io.spine.tools.protoc.given.UuidMethodFactory;
import io.spine.tools.protoc.method.TestMethodProtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory.prefix;
import static io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory.regex;
import static io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory.suffix;
import static io.spine.tools.protoc.given.CodeGeneratorRequestGiven.protocConfig;
import static io.spine.tools.protoc.given.CodeGeneratorRequestGiven.requestBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TempDirectory.class)
@DisplayName("Plugin should")
final class PluginTest {

    private static final String TEST_PROTO_SUFFIX = "_generators.proto";
    private static final String TEST_PROTO_PREFIX = "spine/tools/protoc/test_";
    private static final String TEST_PROTO_REGEX = ".*protoc/.*rators.pro.*";
    private static final String TEST_PROTO_FILE = "spine/tools/protoc/test_generators.proto";

    private Path testPluginConfig;

    @BeforeEach
    void setUp(@TempDirectory.TempDir Path tempDirPath) {
        testPluginConfig = tempDirPath.resolve("test-spine-protoc-plugin.pb");
    }

    @DisplayName("process suffix patterns")
    @Test
    void processSuffixPatterns() {
        GeneratedInterfaces interfaces = new GeneratedInterfaces();
        MessageSelectorFactory messages = interfaces.messages();
        PatternSelector suffixSelector = messages.inFiles(suffix(TEST_PROTO_SUFFIX));
        interfaces.mark(suffixSelector, ClassName.of(TestInterface.class));
        GeneratedMethods methods = new GeneratedMethods();
        methods.applyFactory(TestMethodFactory.class.getName(), suffixSelector);
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(interfaces, methods, testPluginConfig).toString())
                .build();

        CodeGeneratorResponse response = runPlugin(request);

        assertEquals(2, response.getFileCount());
        CodeGeneratorResponse.File messageInterface = response.getFile(0);
        CodeGeneratorResponse.File messageMethod = response.getFile(1);
        assertEquals(TestInterface.class.getName() + ',', messageInterface.getContent());
        assertEquals(TestMethodFactory.TEST_METHOD.value(), messageMethod.getContent());
    }

    @DisplayName("generate UUID message")
    @Test
    void generateUuidMethod() {
        GeneratedMethods methods = new GeneratedMethods();
        MessageSelectorFactory messages = methods.messages();
        methods.applyFactory(UuidMethodFactory.class.getName(), messages.uuid());

        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestMethodProtos.getDescriptor()
                                              .toProto())
                .addFileToGenerate("spine/tools/protoc/method/test_protos.proto")
                .setParameter(protocConfig(methods, testPluginConfig).toString())
                .build();
        CodeGeneratorResponse response = runPlugin(request);

        List<CodeGeneratorResponse.File> messageMethods =
                filterMethods(response, InsertionPoint.class_scope);
        assertEquals(1, messageMethods.size());
    }

    @Test
    @DisplayName("process prefix patterns")
    void processPrefixPatterns() {
        GeneratedInterfaces interfaces = new GeneratedInterfaces();
        MessageSelectorFactory messages = interfaces.messages();
        PatternSelector prefixSelector = messages.inFiles(prefix(TEST_PROTO_PREFIX));
        interfaces.mark(prefixSelector, ClassName.of(TestInterface.class));
        GeneratedMethods methods = new GeneratedMethods();
        methods.applyFactory(TestMethodFactory.class.getName(), prefixSelector);

        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(interfaces, methods, testPluginConfig).toString())
                .build();

        CodeGeneratorResponse response = runPlugin(request);

        assertEquals(2, response.getFileCount());
        CodeGeneratorResponse.File messageInterface = response.getFile(0);
        CodeGeneratorResponse.File messageMethod = response.getFile(1);
        assertEquals(TestInterface.class.getName() + ',', messageInterface.getContent());
        assertEquals(TestMethodFactory.TEST_METHOD.value(), messageMethod.getContent());
    }

    @Test
    @DisplayName("process regex patterns")
    void processRegexPatterns() {
        GeneratedInterfaces interfaces = new GeneratedInterfaces();
        MessageSelectorFactory messages = interfaces.messages();
        PatternSelector regexSelector = messages.inFiles(regex(TEST_PROTO_REGEX));
        interfaces.mark(regexSelector, ClassName.of(TestInterface.class));
        GeneratedMethods methods = new GeneratedMethods();
        methods.applyFactory(TestMethodFactory.class.getName(), regexSelector);
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestGeneratorsProto.getDescriptor()
                                                 .toProto())
                .addFileToGenerate(TEST_PROTO_FILE)
                .setParameter(protocConfig(interfaces, methods, testPluginConfig).toString())
                .build();

        CodeGeneratorResponse response = runPlugin(request);

        assertEquals(2, response.getFileCount());
        CodeGeneratorResponse.File messageInterface = response.getFile(0);
        CodeGeneratorResponse.File messageMethod = response.getFile(1);
        assertEquals(TestInterface.class.getName() + ',', messageInterface.getContent());
        assertEquals(TestMethodFactory.TEST_METHOD.value(), messageMethod.getContent());
    }

    private static List<CodeGeneratorResponse.File> filterMethods(CodeGeneratorResponse response,
                                                                  InsertionPoint insertionPoint) {
        return response
                .getFileList()
                .stream()
                .filter(file -> file.getInsertionPoint()
                                    .contains(insertionPoint.getDefinition()))
                .collect(Collectors.toList());
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
}
