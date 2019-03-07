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
import io.spine.option.OptionExtensionRegistry;
import io.spine.tools.gradle.compiler.protoc.FileSelectorFactory;
import io.spine.tools.gradle.compiler.protoc.GeneratedInterfaces;
import io.spine.tools.gradle.compiler.protoc.GeneratedMethods;
import io.spine.tools.protoc.given.TestInterface;
import io.spine.tools.protoc.given.TestMethodFactory;
import io.spine.tools.protoc.given.UuidMethodFactory;
import io.spine.tools.protoc.iface.TestEventsProto;
import io.spine.tools.protoc.method.TestMethodProtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import static io.spine.base.MessageFile.EVENTS;
import static io.spine.tools.protoc.given.CodeGeneratorRequestGiven.encodedProtocConfig;
import static io.spine.tools.protoc.given.CodeGeneratorRequestGiven.requestBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Plugin should")
final class PluginTest {

    private static final String TEST_PROTO_POSTFIX = "_generators.proto";
    private static final String TEST_PROTO_PREFIX = "spine/tools/protoc/test_";
    private static final String TEST_PROTO_REGEX = ".*protoc/.*rators.pro.*";
    private static final String TEST_PROTO_FILE = "spine/tools/protoc/test_generators.proto";

    @DisplayName("process postfix patterns")
    @Test
    void processPostfixPatterns() {
        GeneratedInterfaces interfaces = GeneratedInterfaces.withDefaults();
        FileSelectorFactory filePattern = interfaces.filePattern();
        interfaces.mark(filePattern.endsWith(TEST_PROTO_POSTFIX), TestInterface.class.getName());
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.useFactory(TestMethodFactory.class.getName(),
                           filePattern.endsWith(TEST_PROTO_POSTFIX));
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

    @DisplayName("skip generation of standard interfaces if they are `ignored`")
    @Test
    void skipStandardInterfacesIfIgnored() {
        GeneratedInterfaces interfaces = GeneratedInterfaces.withDefaults();
        FileSelectorFactory filePattern = interfaces.filePattern();
        interfaces.ignore(filePattern.endsWith(EVENTS.suffix()));
        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestEventsProto.getDescriptor()
                                             .toProto())
                .addFileToGenerate("spine/tools/protoc/iface/test_events.proto")
                .setParameter(encodedProtocConfig(interfaces))
                .build();
        CodeGeneratorResponse response = runPlugin(request);

        assertEquals(0, response.getFileCount());
    }

    @DisplayName("generate UUID message")
    @Test
    void generateUuidMethod() {
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.useFactory(UuidMethodFactory.class.getName(), methods.uuidMessage());

        CodeGeneratorRequest request = requestBuilder()
                .addProtoFile(TestMethodProtos.getDescriptor()
                                              .toProto())
                .addFileToGenerate("spine/tools/protoc/method/test_protos.proto")
                .setParameter(encodedProtocConfig(methods))
                .build();
        CodeGeneratorResponse response = runPlugin(request);

        List<CodeGeneratorResponse.File> messageMethods =
                filterMethods(response, InsertionPoint.CLASS_SCOPE);
        assertEquals(1, messageMethods.size());
    }

    @Test
    @DisplayName("process prefix patterns")
    void processPrefixPatterns() {
        GeneratedInterfaces interfaces = GeneratedInterfaces.withDefaults();
        FileSelectorFactory filePattern = interfaces.filePattern();
        interfaces.mark(filePattern.startsWith(TEST_PROTO_PREFIX), TestInterface.class.getName());
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.useFactory(TestMethodFactory.class.getName(),
                           filePattern.startsWith(TEST_PROTO_PREFIX));

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

    @Test
    @DisplayName("process matches patterns")
    void processRegexPatterns() {
        GeneratedInterfaces interfaces = GeneratedInterfaces.withDefaults();
        FileSelectorFactory filePattern = interfaces.filePattern();
        interfaces.mark(filePattern.matches(TEST_PROTO_REGEX), TestInterface.class.getName());
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.useFactory(TestMethodFactory.class.getName(),
                           filePattern.matches(TEST_PROTO_REGEX));
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
