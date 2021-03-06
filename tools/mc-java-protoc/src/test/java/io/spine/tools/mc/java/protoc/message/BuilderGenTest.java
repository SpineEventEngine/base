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

package io.spine.tools.mc.java.protoc.message;

import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.google.protobuf.compiler.PluginProtos.Version;
import io.spine.test.protoc.BuilderTestProto;
import io.spine.tools.mc.java.protoc.CodeGenerator;
import io.spine.tools.mc.java.protoc.NoOpGenerator;
import io.spine.tools.protoc.SpineProtocConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("BuilderGenerator should")
class BuilderGenTest {

    @Test
    @DisplayName("produce builder insertion points")
    void produceBuilderInsertionPoints() {
        CodeGenerator generator =
                BuilderGen.instance(SpineProtocConfig.getDefaultInstance());
        FileDescriptor file = BuilderTestProto.getDescriptor();
        CodeGeneratorRequest request = CodeGeneratorRequest
                .newBuilder()
                .addProtoFile(file.toProto())
                .addFileToGenerate(file.getFullName())
                .setCompilerVersion(Version.newBuilder().setMajor(3).build())
                .build();
        CodeGeneratorResponse response = generator.process(request);
        List<File> files = response.getFileList();
        assertThat(files).hasSize(1);
        assertThat(files.get(0).getInsertionPoint()).isNotEmpty();
    }

    @Test
    @DisplayName("do nothing if configured to skip validating builders")
    void ignoreIfConfigured() {
        SpineProtocConfig.Builder config = SpineProtocConfig.newBuilder();
        config.getValidationBuilder().setSkipBuilders(true);
        CodeGenerator generator = BuilderGen.instance(config.build());
        assertThat(generator).isInstanceOf(NoOpGenerator.class);
    }
}
