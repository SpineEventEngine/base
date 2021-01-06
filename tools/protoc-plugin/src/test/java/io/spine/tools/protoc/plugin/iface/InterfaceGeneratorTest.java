/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.plugin.iface;

import com.google.common.testing.NullPointerTester;
import com.google.common.truth.IterableSubject;
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
import io.spine.tools.protoc.GeneratedInterfaces;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.given.SpineProtocConfigGiven;
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
final class InterfaceGeneratorTest {

    private static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";
    private static final String PROTO_PACKAGE = "spine.tools.protoc.iface.";

    private static final PackageName PACKAGE_NAME =
            PackageName.of(InterfaceGeneratorTest.class);
    private static final Pattern CUSTOMER_EVENT_INTERFACE_PATTERN =
            compile("^\\s*io\\.spine\\.tools\\.protoc\\.plugin\\.iface\\.ProtocCustomerEvent\\s*,\\s*$");
    private static final Pattern PROJECT_EVENT_INTERFACE_PATTERN =
            compile("^\\s*io\\.spine\\.tools\\.protoc\\.plugin\\.iface\\.ProtocProjectEvent\\s*,\\s*$");

    private static final Pattern PROJECT_EVENT_INTERFACE_DECL_PATTERN =
            compile("public\\s+interface\\s+ProtocProjectEvent\\s*extends\\s+Message\\s*\\{\\s*}");

    private static final Pattern CUSTOMER_EVENT_OR_COMMAND =
            compile("Customer(Command|Event)");

    private CodeGenerator codeGenerator;

    private static Version version() {
        return Version.newBuilder()
                      .setMajor(3)
                      .setMinor(6)
                      .setPatch(1)
                      .build();
    }

    @BeforeEach
    void setUp() {
        GeneratedInterfaces interfaces = SpineProtocConfigGiven.defaultInterfaces();
        SpineProtocConfig config = SpineProtocConfig
                .newBuilder()
                .setAddInterfaces(interfaces.asProtocConfig())
                .build();
        codeGenerator = InterfaceGenerator.instance(config);
    }

    @Test
    @DisplayName("not accept nulls")
    void notAcceptNulls() {
        new NullPointerTester()
                .setDefault(CodeGeneratorRequest.class, CodeGeneratorRequest.getDefaultInstance())
                .testAllPublicStaticMethods(InterfaceGenerator.class);
    }

    @Nested
    @DisplayName("generate insertion point contents for")
    class InsertionPoints {

        @Test
        @DisplayName("`EveryIs` option")
        void everyIsOption() {
            String filePath = "spine/tools/protoc/iface/every_is_test.proto";

            FileDescriptorProto descriptor = EveryIsTestProto.getDescriptor()
                                                             .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertPackage(file);

                String name = file.getName();
                String insertionPoint = file.getInsertionPoint();
                String messageName = PROTO_PACKAGE + name.substring(name.lastIndexOf('/') + 1,
                                                                    name.lastIndexOf('.'));
                assertEquals(insertionPoint, format(INSERTION_POINT_IMPLEMENTS, messageName));

                String content = file.getContent();
                Matcher matcher = CUSTOMER_EVENT_INTERFACE_PATTERN.matcher(content);
                assertTrue(matcher.matches());
            }
        }

        @Test
        @DisplayName("`Is` option")
        void generateInsertionPointContentsForIsOption() {
            String filePath = "spine/tools/protoc/iface/is_test.proto";

            FileDescriptorProto descriptor = IsTestProto.getDescriptor()
                                                        .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertPackage(file);

                String name = file.getName();
                String insertionPoint = file.getInsertionPoint();
                assertFalse(insertionPoint.isEmpty());
                String content = file.getContent();
                if (name.endsWith("ProtocNameUpdated.java")) {
                    assertThat(content)
                            .contains("Event,");
                } else if (name.endsWith("ProtocUpdateName.java")) {
                    assertThat(content)
                            .contains("Command,");
                }
            }
        }

        @Test
        @DisplayName("`EveryIs` in single file")
        void generateInsertionPointContentsForEveryIsInSingleFile() {
            String filePath = "spine/tools/protoc/iface/every_is_in_one_file.proto";

            FileDescriptorProto descriptor = EveryIsInOneFileProto.getDescriptor()
                                                                  .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                if (!haveSamePath(file, sourceWithPackage("ProtocCustomerEvent"))) {
                    assertFilePath(file, sourceWithPackage("EveryIsInOneFileProto"));

                    String insertionPoint = file.getInsertionPoint();
                    assertTrue(insertionPoint.startsWith(format(INSERTION_POINT_IMPLEMENTS,
                                                                PROTO_PACKAGE)));
                    String content = file.getContent();
                    assertThat(content)
                            .matches(CUSTOMER_EVENT_INTERFACE_PATTERN);
                }
            }
        }

        @Test
        @DisplayName("`Is` in single file")
        void isInSingleFile() {
            String filePath = "spine/tools/protoc/iface/is_in_one_file.proto";

            FileDescriptorProto descriptor = IsInOneFileProto.getDescriptor()
                                                             .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(2, files.size());
            for (File file : files) {
                assertFilePath(file, sourceWithPackage("IsInOneFileProto"));

                String insertionPoint = file.getInsertionPoint();
                assertTrue(insertionPoint.startsWith(format(INSERTION_POINT_IMPLEMENTS,
                                                            PROTO_PACKAGE)));
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
            String filePath = "spine/tools/protoc/iface/test_events.proto";

            FileDescriptorProto descriptor = TestEventsProto.getDescriptor()
                                                            .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
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
            String filePath = "spine/tools/protoc/iface/test_commands.proto";

            FileDescriptorProto descriptor = TestCommandsProto.getDescriptor()
                                                              .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
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
            String filePath = "spine/tools/protoc/iface/test_rejections.proto";

            FileDescriptorProto descriptor = Rejections.getDescriptor()
                                                       .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
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
            String filePath = "spine/tools/protoc/iface/uuid_values.proto";

            FileDescriptorProto descriptor = UuidValues.getDescriptor()
                                                       .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(1, files.size());
            for (File file : files) {
                assertTrue(file.hasInsertionPoint());
                assertTrue(file.hasName());

                String genericParam = '<' + ProjectId.class.getSimpleName() + '>';
                assertEquals(UuidValue.class.getName() + genericParam + ',', file.getContent());
            }
        }
    }

    @Test
    @DisplayName("not generate `UuidValue` insertion points for ineligible messages")
    void notGenerateUuidValueForNonEligible() {
        String filePath = "spine/tools/protoc/iface/non_uuid_values.proto";

        FileDescriptorProto descriptor = NonUuidValues.getDescriptor()
                                                      .toProto();
        CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
        assertNotNull(response);
        List<File> files = response.getFileList();
        assertTrue(files.isEmpty());
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
    class MessageInterfaces {

        @Test
        @DisplayName("`(is)` if `generate = true`")
        void forIs() {
            String filePath = "spine/tools/protoc/iface/is_generated.proto";

            FileDescriptorProto descriptor = IsGeneratedProto.getDescriptor()
                                                             .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(4, files.size());
            for (File file : files) {
                assertPackage(file);

                String name = file.getName();
                String insertionPoint = file.getInsertionPoint();
                if (!insertionPoint.isEmpty()) {
                    String messageName = PROTO_PACKAGE +
                            name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
                    assertEquals(format(INSERTION_POINT_IMPLEMENTS, messageName), insertionPoint);
                }

                String content = file.getContent();
                if (name.endsWith("ProtocSurnameUpdated.java")) {
                    assertTrue(content.contains("Event,"));
                } else if (name.endsWith("ProtocUpdateSurname.java")) {
                    assertTrue(content.contains("Command,"));
                } else {
                    assertTrue(CUSTOMER_EVENT_OR_COMMAND.matcher(name)
                                                        .find());
                }
            }
        }

        @Test
        @DisplayName("`(every_is)` if `generate = true`")
        void forEveryIs() {
            String filePath = "spine/tools/protoc/iface/every_is_generated.proto";

            FileDescriptorProto descriptor = EveryIsGeneratedProto.getDescriptor()
                                                                  .toProto();
            CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
            assertNotNull(response);
            List<File> files = response.getFileList();
            assertEquals(3, files.size());
            for (File file : files) {
                assertPackage(file);

                String name = file.getName();
                String insertionPoint = file.getInsertionPoint();
                if (!insertionPoint.isEmpty()) {
                    String messageName = PROTO_PACKAGE + name.substring(name.lastIndexOf('/') + 1,
                                                                        name.lastIndexOf('.'));
                    assertEquals(insertionPoint, format(INSERTION_POINT_IMPLEMENTS, messageName));

                    String content = file.getContent();
                    Matcher matcher = PROJECT_EVENT_INTERFACE_PATTERN.matcher(content);
                    assertTrue(matcher.matches());
                } else {
                    String content = file.getContent();
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
                                    .addFileToGenerate(
                                            "spine/tools/protoc/iface/user.proto")
                                    .addProtoFile(requestedTypes)
                                    .addProtoFile(includedTypes)
                                    .build();
        CodeGeneratorResponse response = codeGenerator.process(request);
        Set<String> generatedFiles = response.getFileList()
                                             .stream()
                                             .map(File::getName)
                                             .collect(toSet());

        IterableSubject assertGeneratedFiles = assertThat(generatedFiles);
        assertGeneratedFiles.doesNotContain("io/spine/tools/protoc/iface/UserName.java");
        assertGeneratedFiles.doesNotContain("io/spine/tools/protoc/iface/Name.java");
        assertGeneratedFiles.containsExactly(
                "io/spine/tools/protoc/plugin/iface/User.java",
                "io/spine/tools/protoc/plugin/iface/LawSubject.java"
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
        return Directory.of(PACKAGE_NAME).resolve(fileName);
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
        Directory directory = Directory.of(PACKAGE_NAME);
        assertTrue(generatedFilePath.startsWith(directory.path()),
                   format("Invalid path `%s`.", generatedFilePath));
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
        assertThat(file.getContent()).isEqualTo(interfaceClass.getName() + ',');
    }
}
