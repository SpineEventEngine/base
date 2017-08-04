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
import com.google.protobuf.compiler.PluginProtos.Version;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Dmytro Dashenkov
 */
public class CodeGeneratorShould {

    @Test
    public void not_accept_nulls() {
        new NullPointerTester()
                .setDefault(CodeGeneratorRequest.class, CodeGeneratorRequest.getDefaultInstance())
                .testAllPublicStaticMethods(CodeGenerator.class);
    }

    @Test
    public void generate_insertion_point_contents_for_EveryIs_option() {
        // Sample path; never resolved
        final String filePath = "./proto/spine/tools/protoc/every_is_test.proto";

        final FileDescriptorProto descriptor = EveryIsTestProto.getDescriptor().toProto();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version())
                                                                 .addFileToGenerate(filePath)
                                                                 .addProtoFile(descriptor)
                                                                 .build();
        final CodeGeneratorResponse response = CodeGenerator.generate(request);
        assertNotNull(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_accept_requests_from_old_compiler() {
        final Version version = Version.newBuilder()
                                       .setMajor(2)
                                       .build();
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                                                                 .setCompilerVersion(version)
                                                                 .build();
        CodeGenerator.generate(request);
    }

    private static Version version() {
        return Version.newBuilder()
                      .setMajor(3)
                      .setMinor(3)
                      .setPatch(0)
                      .build();
    }
}
