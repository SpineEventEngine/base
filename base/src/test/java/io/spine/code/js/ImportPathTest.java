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

@DisplayName("ImportPath should")
class ImportPathTest {

    @Test
    @DisplayName("obtain the file path skipping the library name")
    void importedFilePathSkippingLibrary() {
        ImportPath importPath = ImportPath.of("lib/main.js");
        String filePath = importPath.filePath();
        assertThat(filePath).isEqualTo("main.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the current directory")
    void importedFilePathRelativeToCurrentDir() {
        ImportPath fileImport = ImportPath.of("./file.js");
        String filePath = fileImport.filePath();
        assertThat(filePath).isEqualTo("file.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the parent directory")
    void importedFilePathRelativeToParentDir() {
        String filePath = "../file.js";
        ImportPath fileImport = ImportPath.of(filePath);
        String parsedFilePath = fileImport.filePath();
        assertThat(parsedFilePath).isEqualTo(filePath);
    }
}
