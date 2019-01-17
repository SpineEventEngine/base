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

package io.spine.js.generate.output.snippet;

import com.google.protobuf.Any;
import io.spine.code.js.FileName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("A code generator should generate an import for")
class ImportTest {

    private final FileName anyFile = FileName.from(Any.getDescriptor()
                                                      .getFile());

    @Test
    @DisplayName("a file relative to a current directory")
    void fileRelativeToCurrentDir() {
        Import importLine = Import.fileRelativeToRoot(anyFile);
        String expected = "require('./google/protobuf/any_pb.js');";
        assertThat(importLine.content()).isEqualTo(expected);
    }

    @Test
    @DisplayName("a file relative to another file")
    void fileRelativeToAnotherFile() {
        Import importLine = Import.fileRelativeTo(anyFile, anyFile);
        String expected = "require('../../google/protobuf/any_pb.js');";
        assertThat(importLine.content()).isEqualTo(expected);
    }

    @Test
    @DisplayName("a library")
    void library() {
        Import importLine = Import.library("someJsLib");
        String expected = "require('someJsLib');";
        assertThat(importLine.content()).isEqualTo(expected);
    }

    @Test
    @DisplayName("a default library")
    void defaultLibrary() {
        Import importLine = Import.libraryDefault("someJsLib");
        String expected = "require('someJsLib').default;";
        assertThat(importLine.content()).isEqualTo(expected);
    }

    @Test
    @DisplayName("a file with an alias")
    void named() {
        Import importLine = Import.fileRelativeToRoot(anyFile);
        String expected = "let alias = require('./google/protobuf/any_pb.js');";
        assertThat(importLine.namedAs("alias")).isEqualTo(expected);
    }
}
