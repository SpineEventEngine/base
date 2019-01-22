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

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImportPath should")
class ImportPathTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(ImportPath.class);
    }

    @Test
    @DisplayName("not be an empty string")
    void notAcceptEmptyString() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ImportPath.of("")
        );
    }

    @Test
    @DisplayName("obtain the file path skipping the library name")
    void importedFilePathSkippingLibrary() {
        ImportPath importPath = ImportPath.of("lib/main.js");
        ImportPath filePath = importPath.stripLibrary();
        assertThat(filePath.value()).isEqualTo("main.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the current directory")
    void importedFilePathRelativeToCurrentDir() {
        ImportPath fileImport = ImportPath.of("./file.js");
        ImportPath filePath = fileImport.stripLibrary();
        assertThat(filePath.value()).isEqualTo("file.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the parent directory")
    void importedFilePathRelativeToParentDir() {
        String sourcePath = "../file.js";
        ImportPath fileImport = ImportPath.of(sourcePath);
        ImportPath filePath = fileImport.stripLibrary();
        assertThat(filePath.value()).isEqualTo(sourcePath);
    }

    @Test
    @DisplayName("strip path relative to parent directory")
    void stripRelativeToParent() {
        ImportPath importPath = ImportPath.of("../../foo/nested.js");
        FileName fileName = importPath.fileName();
        assertThat(fileName.value()).isEqualTo("foo/nested.js");
    }

    @Test
    @DisplayName("strip path relative to current directory")
    void stripRelativeToCurrent() {
        ImportPath importPath = ImportPath.of("./../foo/deep.js");
        FileName fileName = importPath.fileName();
        assertThat(fileName.value()).isEqualTo("foo/deep.js");
    }

    @Test
    @DisplayName("recognize a Spine library")
    void recognizeSpine() {
        ImportPath importPath = ImportPath.of("spine-lib/something");
        assertTrue(importPath.isSpine());
    }

    @Test
    @DisplayName("recognize not a Spine library")
    void recognizeNotSpine() {
        ImportPath importPath = ImportPath.of("not-spine");
        assertFalse(importPath.isSpine());
    }
}
