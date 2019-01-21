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

package io.spine.code.js;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImportPath should")
class ImportPathTest {

    @Test
    @DisplayName("obtain the file path skipping the library name")
    void importedFilePathSkippingLibrary() {
        ImportPath importPath = ImportPath.of("lib/main.js");
        ImportPath filePath = importPath.skipLibrary();
        assertThat(filePath.value()).isEqualTo("main.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the current directory")
    void importedFilePathRelativeToCurrentDir() {
        ImportPath fileImport = ImportPath.of("./file.js");
        ImportPath filePath = fileImport.skipLibrary();
        assertThat(filePath.value()).isEqualTo("file.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the parent directory")
    void importedFilePathRelativeToParentDir() {
        String sourcePath = "../file.js";
        ImportPath fileImport = ImportPath.of(sourcePath);
        ImportPath filePath = fileImport.skipLibrary();
        assertThat(filePath.value()).isEqualTo(sourcePath);
    }

    @Test
    @DisplayName("strip path relative to parent directory")
    void stripRelativeToParent() {
        ImportPath importPath = ImportPath.of("../../foo/nested.js");
        String stripped = importPath.stripRelativePath();
        assertThat(stripped).isEqualTo("foo/nested.js");
    }

    @Test
    @DisplayName("strip path relative to current directory")
    void stripRelativeToCurrent() {
        ImportPath importPath = ImportPath.of("./../foo/deep.js");
        String stripped = importPath.stripRelativePath();
        assertThat(stripped).isEqualTo("foo/deep.js");
    }

    @Test
    @DisplayName("recognize a Spine library")
    void recognizeSpine() {
        ImportPath importPath = ImportPath.of("spine/something");
        assertTrue(importPath.isSpine());
    }

    @Test
    @DisplayName("recognize not a Spine library")
    void recognizeNotSpine() {
        ImportPath importPath = ImportPath.of("not-spine");
        assertFalse(importPath.isSpine());
    }
}
