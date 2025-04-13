/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.code.proto;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import io.spine.type.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.code.proto.CodeGeneratorRequestParsingSpecKt.constructRequest;
import static io.spine.string.Strings.toBase64Encoded;
import static kotlin.io.FilesKt.writeBytes;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("`CodeGeneratorRequest` Java API should")
class CodeGeneratorRequestsJavaSpec {

    private File requestFile;
    private CodeGeneratorRequest request;

    @BeforeEach
    void prepareFile(@TempDir Path dir) {
        requestFile = dir.resolve("request.binbp").toFile();
        var encodedPath = toBase64Encoded(requestFile.getAbsolutePath());
        request = constructRequest(encodedPath);
    }

    @Test
    @DisplayName("provide parsing from an input stream")
    void parsing() {
        writeBytes(requestFile, request.toByteArray());
        try (var input = new FileInputStream(requestFile)) {
            var parsed = Binary.parse(CodeGeneratorRequest.class, input);
            assertThat(parsed).isEqualTo(request);
        } catch (IOException e) {
            fail("Failed to parse the request file.", e);
        }
    }
}
