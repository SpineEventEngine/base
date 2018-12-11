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

package io.spine.tools.protoc;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.google.protobuf.compiler.PluginProtos.Version;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.code.java.FileName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SourceFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.spine.tools.protoc.InsertionPoint.INSERTION_POINT_IMPLEMENTS;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MarkerInterfaceGenerator should")
class MarkerInterfaceGeneratorTest {

    private static final String PROTO_PACKAGE = "spine.tools.protoc.";

    private static final PackageName PACKAGE_NAME =
            PackageName.of(MarkerInterfaceGeneratorTest.class);
    private static final Pattern CUSTOMER_EVENT_INTERFACE_PATTERN =
            compile("^\\s*io\\.spine\\.tools\\.protoc\\.ProtocCustomerEvent\\s*,\\s*$");

    private static final Pattern CUSTOMER_EVENT_INTERFACE_DECL_PATTERN =
            compile("public\\s+interface\\s+ProtocCustomerEvent\\s*extends\\s+Message\\s*\\{\\s*}");

    private static final Pattern CUSTOMER_EVENT_OR_COMMAND =
            compile("Customer(Command|Event)");

    private SpineProtoGenerator codeGenerator;

    private static Version version() {
        return Version.newBuilder()
                      .setMajor(3)
                      .setMinor(3)
                      .setPatch(0)
                      .build();
    }

    @BeforeEach
    void setUp() {
        codeGenerator = MarkerInterfaceGenerator.instance();
    }

    @Test
    @DisplayName("not accept nulls")
    void notAcceptNulls() {
        new NullPointerTester()
                .setDefault(CodeGeneratorRequest.class, CodeGeneratorRequest.getDefaultInstance())
                .testAllPublicStaticMethods(MarkerInterfaceGenerator.class);
    }

    @Test
    @DisplayName("generate insertion point contents for EveryIs option")
    void generateInsertionPointContentsForEveryIsOption() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/every_is_test.proto";

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
    @DisplayName("generate insertion point contents for Is option")
    void generateInsertionPointContentsForIsOption() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/is_test.proto";

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
                assertTrue(content.contains("Event,"));
            } else if (name.endsWith("ProtocUpdateName.java")) {
                assertTrue(content.contains("Command,"));
            }
        }
    }

    @Test
    @DisplayName("generate insertion point contents for EveryIs in singe file")
    void generateInsertionPointContentsForEveryIsInSingleFile() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/every_is_in_one_file.proto";

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
                Matcher matcher = CUSTOMER_EVENT_INTERFACE_PATTERN.matcher(content);
                assertTrue(matcher.matches(), content);
            }
        }
    }

    @Test
    @DisplayName("generate insertion point contents for Is in single file")
    void generateInsertionPointContentsForIsInSingleFile() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/is_in_one_file.proto";

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
            Matcher matcher = CUSTOMER_EVENT_INTERFACE_PATTERN.matcher(content);
            assertTrue(matcher.matches(), format("Unexpected inserted content: %s", content));
        }
    }

    @Test
    @DisplayName("generate EventMessage insertion points")
    void generateEventMessageInsertionPoints() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/test_events.proto";

        FileDescriptorProto descriptor = TestEventsProto.getDescriptor()
                                                        .toProto();
        CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
        assertNotNull(response);
        List<File> files = response.getFileList();
        assertEquals(2, files.size());
        for (File file : files) {
            assertTrue(file.hasInsertionPoint());
            assertTrue(file.hasName());

            assertEquals(EventMessage.class.getName() + ',', file.getContent());
        }
    }

    @Test
    @DisplayName("generate CommandMessage insertion points")
    void generateCommandMessageInsertionPoints() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/test_commands.proto";

        FileDescriptorProto descriptor = TestCommandsProto.getDescriptor()
                                                          .toProto();
        CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
        assertNotNull(response);
        List<File> files = response.getFileList();
        assertEquals(2, files.size());
        for (File file : files) {
            assertTrue(file.hasInsertionPoint());
            assertTrue(file.hasName());

            assertEquals(CommandMessage.class.getName() + ',', file.getContent());
        }
    }

    @Test
    @DisplayName("generate RejectionMessage insertion points")
    void generateRejectionMessageInsertionPoints() {
        // Sample path; never resolved
        String filePath = "/proto/spine/tools/protoc/test_rejections.proto";

        FileDescriptorProto descriptor = Rejections.getDescriptor()
                                                   .toProto();
        CodeGeneratorResponse response = processCodeGenRequest(filePath, descriptor);
        assertNotNull(response);
        List<File> files = response.getFileList();
        assertEquals(1, files.size());
        for (File file : files) {
            assertTrue(file.hasInsertionPoint());
            assertTrue(file.hasName());

            assertEquals(RejectionMessage.class.getName() + ',', file.getContent());
        }
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
        assertThrows(IllegalArgumentException.class,
                     () -> codeGenerator.process(request));
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
        assertThrows(IllegalArgumentException.class,
                     () -> codeGenerator.process(request));
    }

    private CodeGeneratorResponse processCodeGenRequest(String filePath,
                                                        FileDescriptorProto descriptor) {
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
        return PACKAGE_NAME.toDirectory()
                           .resolve(fileName);
    }

    private static boolean haveSamePath(File generatedFile, SourceFile anotherFile) {
        Path generatedFilePath = Paths.get(generatedFile.getName());
        return generatedFilePath.equals(anotherFile.getPath());
    }

    private static void assertFilePath(File generatedFile, SourceFile expectedFile) {
        assertTrue(haveSamePath(generatedFile, expectedFile));
    }

    private static void assertPackage(File generatedFile) {
        Path generatedFilePath = Paths.get(generatedFile.getName());
        assertTrue(generatedFilePath.startsWith(PACKAGE_NAME.toDirectory()
                                                            .getPath()));
    }
}
