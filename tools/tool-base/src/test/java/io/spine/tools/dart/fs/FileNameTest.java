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

package io.spine.tools.dart.fs;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Empty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.testing.NullPointerTester.Visibility.PACKAGE;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`FileName` should")
class FileNameTest {

    private static final String GENERATED_FILE = "google/protobuf/empty.pb.dart";
    private static final FileDescriptor protoFile = Empty.getDescriptor()
                                                         .getFile();

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testStaticMethods(FileName.class, PACKAGE);
    }

    @Test
    @DisplayName("construct from a descriptor")
    void appendSuffix() {
        FileName fileName = FileName.relative(protoFile);
        assertThat(fileName.value()).isEqualTo(GENERATED_FILE);
    }

    @Test
    @DisplayName("construct from proto file name")
    void returnPathElements() {
        FileName fileName = FileName.relative(protoFileName());
        assertThat(fileName.value()).isEqualTo(GENERATED_FILE);
    }

    @Test
    @DisplayName("tell if the source code file is generated")
    void generatedSource() {
        Path sourceDir = Paths.get("main", "proto");
        assertTrue(FileName.isGenerated(
                sourceDir.resolve("msg" + FileName.GeneratedExtension.OF_MESSAGE))
        );
        assertTrue(FileName.isGenerated(
                sourceDir.resolve("enum" + FileName.GeneratedExtension.OF_ENUM))
        );
        assertTrue(FileName.isGenerated(
                sourceDir.resolve("srv" + FileName.GeneratedExtension.OF_SERVER))
        );
        assertTrue(FileName.isGenerated(
                sourceDir.resolve("json" + FileName.GeneratedExtension.OF_JSON))
        );
    }

    private static io.spine.code.proto.FileName protoFileName() {
        return io.spine.code.proto.FileName.from(protoFile);
    }
}
