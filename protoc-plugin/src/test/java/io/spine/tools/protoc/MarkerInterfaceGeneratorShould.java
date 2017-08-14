/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.spine.tools.protoc.MarkerInterfaceGenerator.INSERTION_POINT_IMPLEMENTS;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
public class MarkerInterfaceGeneratorShould {

    private static final String PROTO_PACKAGE = "spine.tools.protoc.";

    private static final String PACKAGE_PATH =
            MarkerInterfaceGeneratorShould.class.getPackage()
                                                .getName()
                                                .replace('.', '/');
    private static final Pattern CUSTOMER_EVENT_INTERFACE_PATTERN =
            compile("^\\s*io\\.spine\\.tools\\.protoc\\.ProtocPluginCustomerEvent\\s*,\\s*$");

    private static final Pattern CUSTOMER_EVENT_INTERFACE_DECL_PATTERN =
            compile("public\\s+interface\\s+ProtocPluginCustomerEvent\\s*extends\\s+Message\\s*\\{\\s*}");


    private static final Pattern CUSTOMER_EVENT_OR_COMMAND =
            compile("Customer(Command|Event)");

    private SpineProtoGenerator codeGenerator;

    @Before
    public void setUp() {
        codeGenerator = MarkerInterfaceGenerator.instance();
    }

    @Test
    public void not_accept_nulls() {
        new NullPointerTester()
                .setDefault(CodeGeneratorRequest.class, CodeGeneratorRequest.getDefaultInstance())
                .testAllPublicStaticMethods(MarkerInterfaceGenerator.class);
    }

    @Test
    public void generate_insertion_point_contents_for_EveryIs_option() {
        // Sample path; never resolved
        final String filePath = "/proto/spine/tools/protoc/every_is_test.proto";

        final FileDescriptorProto descriptor = EveryIsTestProto.getDescriptor().toProto();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version())
                                                                 .addFileToGenerate(filePath)
                                                                 .addProtoFile(descriptor)
                                                                 .build();
        final CodeGeneratorResponse response = codeGenerator.process(request);
        assertNotNull(response);
        final List<File> files = response.getFileList();
        assertEquals(3, files.size());
        for (File file : files) {
            final String name = file.getName();
            assertTrue(name.startsWith(PACKAGE_PATH));

            final String insertionPoint = file.getInsertionPoint();
            if (!insertionPoint.isEmpty()) {
                final String messageName = PROTO_PACKAGE + name.substring(name.lastIndexOf('/') + 1,
                                                                          name.lastIndexOf('.'));
                assertEquals(insertionPoint, format(INSERTION_POINT_IMPLEMENTS, messageName));

                final String content = file.getContent();
                final Matcher matcher = CUSTOMER_EVENT_INTERFACE_PATTERN.matcher(content);
                assertTrue(matcher.matches());
            } else {
                final String content = file.getContent();
                final Matcher matcher = CUSTOMER_EVENT_INTERFACE_DECL_PATTERN.matcher(content);
                assertTrue(matcher.find());
            }
        }
    }

    @Test
    public void generate_insertion_point_contents_for_Is_option() {
        // Sample path; never resolved
        final String filePath = "/proto/spine/tools/protoc/is_test.proto";

        final FileDescriptorProto descriptor = IsTestProto.getDescriptor().toProto();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version())
                                                                 .addFileToGenerate(filePath)
                                                                 .addProtoFile(descriptor)
                                                                 .build();
        final CodeGeneratorResponse response = codeGenerator.process(request);
        assertNotNull(response);
        final List<File> files = response.getFileList();
        assertEquals(4, files.size());
        for (File file : files) {
            final String name = file.getName();
            assertTrue(name.startsWith(PACKAGE_PATH));

            final String insertionPoint = file.getInsertionPoint();
            if (!insertionPoint.isEmpty()) {
                final String messageName = PROTO_PACKAGE + name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
                assertEquals(format(INSERTION_POINT_IMPLEMENTS, messageName), insertionPoint);
            }

            final String content = file.getContent();
            if (name.endsWith("ProtocPluginNameUpdated.java")) {
                assertTrue(content.contains("Event,"));
            } else if (name.endsWith("ProtocPluginUpdateName.java")) {
                assertTrue(content.contains("Command,"));
            } else {
                assertTrue(CUSTOMER_EVENT_OR_COMMAND.matcher(name).find());
            }
        }
    }

    @Test
    public void generate_insertion_point_contents_for_EveryIs_in_single_file() {
        // Sample path; never resolved
        final String filePath = "/proto/spine/tools/protoc/every_is_in_one_file.proto";

        final FileDescriptorProto descriptor = EveryIsInOneFileProto.getDescriptor().toProto();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version())
                                                                 .addFileToGenerate(filePath)
                                                                 .addProtoFile(descriptor)
                                                                 .build();
        final CodeGeneratorResponse response = codeGenerator.process(request);
        assertNotNull(response);
        final List<File> files = response.getFileList();
        assertEquals(3, files.size());
        for (File file : files) {
            if (!file.getName().equals("io/spine/tools/protoc/ProtocPluginCustomerEvent.java")) {
                final String name = file.getName();
                assertEquals(PACKAGE_PATH + "/EveryIsInOneFileProto.java", name);

                final String insertionPoint = file.getInsertionPoint();
                assertTrue(insertionPoint.startsWith(format(INSERTION_POINT_IMPLEMENTS,
                                                            PROTO_PACKAGE)));
                final String content = file.getContent();
                final Matcher matcher = CUSTOMER_EVENT_INTERFACE_PATTERN.matcher(content);
                assertTrue(content, matcher.matches());
            }
        }
    }

    @Test
    public void generate_insertion_point_contents_for_Is_in_single_file() {
        // Sample path; never resolved
        final String filePath = "/proto/spine/tools/protoc/is_in_one_file.proto";

        final FileDescriptorProto descriptor = IsInOneFileProto.getDescriptor().toProto();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version())
                                                                 .addFileToGenerate(filePath)
                                                                 .addProtoFile(descriptor)
                                                                 .build();
        final CodeGeneratorResponse response = codeGenerator.process(request);
        assertNotNull(response);
        final List<File> files = response.getFileList();
        assertEquals(3, files.size());
        for (File file : files) {
            if (file.getName().endsWith("Event.java")) {
                assertFalse(file.hasInsertionPoint());
            } else {
                final String name = file.getName();
                assertEquals(PACKAGE_PATH + "/IsInOneFileProto.java", name);

                final String insertionPoint = file.getInsertionPoint();
                assertTrue(insertionPoint.startsWith(format(INSERTION_POINT_IMPLEMENTS,
                                                            PROTO_PACKAGE)));
                final String content = file.getContent();
                final Matcher matcher = CUSTOMER_EVENT_INTERFACE_PATTERN.matcher(content);
                assertTrue(format("Unexpected inserted content: %s", content), matcher.matches());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_accept_requests_from_old_compiler() {
        final Version version = Version.newBuilder()
                                       .setMajor(2)
                                       .build();
        final FileDescriptorProto stubFile = FileDescriptorProto.getDefaultInstance();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version)
                                                                 .addProtoFile(stubFile)
                                                                 .build();
        codeGenerator.process(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_accept_empty_requests() {
        final Version version = Version.newBuilder()
                                       .setMajor(3)
                                       .build();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version)
                                                                 .build();
        codeGenerator.process(request);
    }

    private static Version version() {
        return Version.newBuilder()
                      .setMajor(3)
                      .setMinor(3)
                      .setPatch(0)
                      .build();
    }
}
