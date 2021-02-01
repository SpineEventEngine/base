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

package io.spine.tools.protoc.plugin.message;

import com.google.common.testing.NullPointerTester;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.StringSubject;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.google.protobuf.compiler.PluginProtos.Version;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.code.fs.java.Directory;
import io.spine.code.fs.java.FileName;
import io.spine.code.fs.java.SourceFile;
import io.spine.code.java.PackageName;
import io.spine.tools.protoc.plugin.CodeGenerator;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.given.SpineProtocConfigGiven;
import io.spine.tools.protoc.message.tests.EveryIsGeneratedProto;
import io.spine.tools.protoc.message.tests.EveryIsInOneFileProto;
import io.spine.tools.protoc.message.tests.EveryIsTestProto;
import io.spine.tools.protoc.message.tests.IsGeneratedProto;
import io.spine.tools.protoc.message.tests.IsInOneFileProto;
import io.spine.tools.protoc.message.tests.IsTestProto;
import io.spine.tools.protoc.message.tests.NonUuidValues;
import io.spine.tools.protoc.message.tests.Rejections;
import io.spine.tools.protoc.message.tests.TestCommandsProto;
import io.spine.tools.protoc.message.tests.TestEventsProto;
import io.spine.tools.protoc.message.tests.UserNameProto;
import io.spine.tools.protoc.message.tests.UserProto;
import io.spine.tools.protoc.message.tests.UuidValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InterfaceGenerator should")
final class InterfaceGenTest {

    private static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";

    private static final String PROTO_PACKAGE = "spine.tools.protoc.msg";
    private static final String PROTO_DIR = PROTO_PACKAGE.replace('.', '/');

    private static final PackageName JAVA_PACKAGE =
            PackageName.of(InterfaceGenTest.class).nested("tests");
    private static final String JAVA_DIR = JAVA_PACKAGE.value().replace('.', '/');

    private static final Pattern CUSTOMER_EVENT_INTERFACE_PATTERN =
            compile("^\\s*io\\.spine\\.tools\\.protoc\\.message\\.tests" +
                            "\\.ProtocCustomerEvent\\s*,\\s*$");
    private static final Pattern PROJECT_EVENT_INTERFACE_PATTERN =
            compile("^\\s*io\\.spine\\.tools\\.protoc\\.message\\.tests" +
                            "\\.ProtocProjectEvent\\s*,\\s*$");

    private static final Pattern PROJECT_EVENT_INTERFACE_DECL_PATTERN =
            compile("public\\s+interface\\s+ProtocProjectEvent\\s*extends\\s+Message\\s*\\{\\s*}");

    private static final Pattern CUSTOMER_EVENT_OR_COMMAND =
            compile("Customer(Command|Event)");

    private CodeGenerator codeGenerator;

    private static String protoFile(String shortName) {
        return PROTO_DIR + '/' + shortName;
    }

    private static String javaFile(String shortName) {
        return JAVA_DIR + '/' + shortName;
    }

    private static Version version() {
        return Version.newBuilder()
                      .setMajor(3)
                      .setMinor(6)
                      .setPatch(1)
                      .build();
    }

    @BeforeEach
    void setUp() {
        io.spine.tools.protoc.Interfaces interfaces = SpineProtocConfigGiven.defaultInterfaces();
        SpineProtocConfig config = SpineProtocConfig
                .newBuilder()
                .setAddInterfaces(interfaces.asProtocConfig())
                .build();
        codeGenerator = InterfaceGen.instance(config);
    }

    @Test
    @DisplayName("not accept nulls")
    void notAcceptNulls() {
        new NullPointerTester()
                .setDefault(CodeGeneratorRequest.class, CodeGeneratorRequest.getDefaultInstance())
                .testAllPublicStaticMethods(InterfaceGen.class);
    }

    private static String messageNameFrom(File file) {
        String fileName = file.getName();
        String messageName = PROTO_PACKAGE + '.' +
                fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));
        return messageName;
    }

    @Nested
    @DisplayName("generate insertion point contents for")
    class InsertionPoints {

        @Test
        @DisplayName("`EveryIs` option")
        void everyIsOption() {
            String filePath = protoFile("every_is_test.proto");

            FileDescriptorProto fileDescr =
                    EveryIsTestProto.getDescriptor()
                                    .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertPackage(file);

                String messageName = messageNameFrom(file);
                String insertionPoint = file.getInsertionPoint();
                assertThat(insertionPoint)
                        .isEqualTo(format(INSERTION_POINT_IMPLEMENTS, messageName));
                String content = file.getContent();
                assertThat(content)
                        .matches(CUSTOMER_EVENT_INTERFACE_PATTERN);
            }
        }

        @Test
        @DisplayName("`Is` option")
        void generateInsertionPointContentsForIsOption() {
            String filePath = protoFile("is_test.proto");

            FileDescriptorProto fileDescr =
                    IsTestProto.getDescriptor()
                               .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertPackage(file);

                String name = file.getName();
                String insertionPoint = file.getInsertionPoint();
                assertFalse(insertionPoint.isEmpty());
                String content = file.getContent();
                StringSubject assertContent = assertThat(content);
                if (name.endsWith("ProtocNameUpdated.java")) {
                    assertContent.contains("Event,");
                } else if (name.endsWith("ProtocUpdateName.java")) {
                    assertContent.contains("Command,");
                }
            }
        }

        @Test
        @DisplayName("`EveryIs` in single file")
        void generateInsertionPointContentsForEveryIsInSingleFile() {
            String filePath = protoFile("every_is_in_one_file.proto");

            FileDescriptorProto fileDescr =
                    EveryIsInOneFileProto.getDescriptor()
                                         .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                if (!haveSamePath(file, sourceWithPackage("ProtocCustomerEvent"))) {
                    assertFilePath(file, sourceWithPackage("EveryIsInOneFileProto"));

                    String insertionPoint = file.getInsertionPoint();
                    assertThat(insertionPoint)
                            .startsWith(format(INSERTION_POINT_IMPLEMENTS, PROTO_PACKAGE));
                    String content = file.getContent();
                    assertThat(content)
                            .matches(CUSTOMER_EVENT_INTERFACE_PATTERN);
                }
            }
        }

        @Test
        @DisplayName("`Is` in single file")
        void isInSingleFile() {
            String filePath = protoFile("is_in_one_file.proto");

            FileDescriptorProto fileDescr =
                    IsInOneFileProto.getDescriptor()
                                    .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertFilePath(file, sourceWithPackage("IsInOneFileProto"));

                String insertionPoint = file.getInsertionPoint();
                assertThat(insertionPoint)
                        .startsWith(format(INSERTION_POINT_IMPLEMENTS, PROTO_PACKAGE));
                String content = file.getContent();
                assertThat(content)
                        .matches(CUSTOMER_EVENT_INTERFACE_PATTERN);
            }
        }
    }

    @Nested
    @DisplayName("generate insertion points for specific types")
    class TypeInsertionPoints {

        @Test
        @DisplayName("`EventMessage`")
        void eventMessage() {
            String filePath = protoFile("test_events.proto");

            FileDescriptorProto fileDescr =
                    TestEventsProto.getDescriptor()
                                   .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertGeneratedInterface(EventMessage.class, file);
            }
        }

        @Test
        @DisplayName("`CommandMessage`")
        void commandMessage() {
            String filePath = protoFile("test_commands.proto");

            FileDescriptorProto fileDescr =
                    TestCommandsProto.getDescriptor()
                                     .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertGeneratedInterface(CommandMessage.class, file);
            }
        }

        @Test
        @DisplayName("`RejectionMessage`")
        void generateRejectionMessageInsertionPoints() {
            String filePath = protoFile("test_rejections.proto");

            FileDescriptorProto fileDescr =
                    Rejections.getDescriptor()
                              .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(1, files.size());
            for (File file : files) {
                assertGeneratedInterface(RejectionMessage.class, file);
            }
        }

        @Test
        @DisplayName("`UuidValue`")
        void uuidValue() {
            String filePath = protoFile("uuid_values.proto");

            FileDescriptorProto fileDescr =
                    UuidValues.getDescriptor()
                              .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(1, files.size());
            for (File file : files) {
                assertTrue(file.hasInsertionPoint());
                assertTrue(file.hasName());
                assertThat(file.getContent())
                        .isEqualTo(UuidValue.class.getName() + ',');
            }
        }
    }

    @Test
    @DisplayName("not generate `UuidValue` insertion points for ineligible messages")
    void notGenerateUuidValueForNonEligible() {
        String filePath = protoFile("non_uuid_values.proto");

        FileDescriptorProto fileDescr =
                NonUuidValues.getDescriptor()
                             .toProto();
        CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
        assertNotNull(response);
        List<File> files = response.getFileList();
        assertThat(files).isEmpty();
    }

    @Test
    @DisplayName("not accept requests from old compiler")
    void notAcceptRequestsFromOldCompiler() {
        Version version = Version.newBuilder()
                .setMajor(2)
                .build();
        FileDescriptorProto stubFile = FileDescriptorProto.getDefaultInstance();
        CodeGeneratorRequest request =
                CodeGeneratorRequest.newBuilder()
                                    .setCompilerVersion(version)
                                    .addProtoFile(stubFile)
                                    .build();
        assertIllegalArgument(() -> codeGenerator.process(request));
    }

    @Test
    @DisplayName("not accept empty requests")
    void notAcceptEmptyRequests() {
        Version version = Version.newBuilder()
                .setMajor(3)
                .build();
        CodeGeneratorRequest request =
                CodeGeneratorRequest.newBuilder()
                                    .setCompilerVersion(version)
                                    .build();
        assertIllegalArgument(() -> codeGenerator.process(request));
    }

    @Nested
    @DisplayName("generate message interfaces for")
    class Interfaces {

        @Test
        @DisplayName("`(is)` if `generate = true`")
        void forIs() {
            String filePath = protoFile("is_generated.proto");

            FileDescriptorProto fileDescr =
                    IsGeneratedProto.getDescriptor()
                                    .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(4, files.size());
            for (File file : files) {
                assertPackage(file);

                String fileName = file.getName();
                String insertionPoint = file.getInsertionPoint();
                if (!insertionPoint.isEmpty()) {
                    String messageName = messageNameFrom(file);
                    assertThat(insertionPoint)
                            .isEqualTo(format(INSERTION_POINT_IMPLEMENTS, messageName));
                }

                String content = file.getContent();
                StringSubject assertContent = assertThat(content);
                if (fileName.endsWith("ProtocSurnameUpdated.java")) {
                    assertContent.contains("Event,");
                } else if (fileName.endsWith("ProtocUpdateSurname.java")) {
                    assertContent.contains("Command,");
                } else {
                    assertTrue(CUSTOMER_EVENT_OR_COMMAND.matcher(fileName)
                                                        .find());
                }
            }
        }

        @Test
        @DisplayName("`(every_is)` if `generate = true`")
        void forEveryIs() {
            String filePath = protoFile("every_is_generated.proto");

            FileDescriptorProto fileDescr =
                    EveryIsGeneratedProto.getDescriptor()
                                         .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, fileDescr);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(3, files.size());
            for (File file : files) {
                assertPackage(file);

                String content = file.getContent();
                String insertionPoint = file.getInsertionPoint();
                if (!insertionPoint.isEmpty()) {
                    String messageName = messageNameFrom(file);
                    assertThat(insertionPoint)
                            .isEqualTo(format(INSERTION_POINT_IMPLEMENTS, messageName));

                    Matcher matcher = PROJECT_EVENT_INTERFACE_PATTERN.matcher(content);
                    assertTrue(matcher.matches());
                } else {
                    Matcher matcher = PROJECT_EVENT_INTERFACE_DECL_PATTERN.matcher(content);
                    assertTrue(matcher.find());
                }
            }
        }
    }

    @Test
    @DisplayName("skip generation for types included in compilation but not requested to be generated")
    void skipIncluded() {
        FileDescriptorProto requestedTypes =
                UserProto.getDescriptor()
                         .toProto();
        FileDescriptorProto includedTypes =
                UserNameProto.getDescriptor()
                             .toProto();
        CodeGeneratorRequest request =
                CodeGeneratorRequest.newBuilder()
                                    .setCompilerVersion(version())
                                    .addFileToGenerate(protoFile("user.proto"))
                                    .addProtoFile(requestedTypes)
                                    .addProtoFile(includedTypes)
                                    .build();
        CodeGeneratorResponse response = codeGenerator.process(request);
        Set<String> generatedFiles = response.getFileList()
                                             .stream()
                                             .map(File::getName)
                                             .collect(toSet());

        IterableSubject assertFiles = assertThat(generatedFiles);
        assertFiles.doesNotContain(javaFile("UserName.java"));
        assertFiles.doesNotContain(javaFile("Name.java"));
        assertFiles.containsExactly(
                javaFile("User.java"),
                javaFile("LawSubject.java")
        );
    }

    private CodeGeneratorResponse
    processCodeGenRequest(String filePath, FileDescriptorProto descriptor) {
        CodeGeneratorRequest request =
                CodeGeneratorRequest.newBuilder()
                                    .setCompilerVersion(version())
                                    .addFileToGenerate(filePath)
                                    .addProtoFile(descriptor)
                                    .build();
        return codeGenerator.process(request);
    }

    private static SourceFile sourceWithPackage(String typeName) {
        FileName fileName = FileName.forType(typeName);
        return Directory.of(JAVA_PACKAGE).resolve(fileName);
    }

    private static boolean haveSamePath(File generatedFile, SourceFile anotherFile) {
        Path generatedFilePath = Paths.get(generatedFile.getName());
        return generatedFilePath.equals(anotherFile.path());
    }

    private static void assertFilePath(File generatedFile, SourceFile expectedFile) {
        assertTrue(haveSamePath(generatedFile, expectedFile));
    }

    private static void assertPackage(File generatedFile) {
        Path generatedFilePath = Paths.get(generatedFile.getName());
        Directory directory = Directory.of(JAVA_PACKAGE);
        assertThat(generatedFilePath.toString())
                .startsWith(directory.path().toString());
    }

    /**
     * Verifies that the file contains the name of the interface class suffixed with comma.
     *
     * <p>The trailing comma is needed because the interface name will be one of the several
     * interfaces in the {@code implements} clause of the generated class.
     */
    private static void assertGeneratedInterface(Class<?> interfaceClass, File file) {
        assertTrue(file.hasInsertionPoint());
        assertTrue(file.hasName());
        StringSubject assertContent = assertThat(file.getContent());
        assertContent.startsWith(interfaceClass.getName());
        assertContent.endsWith(",");
    }
}
