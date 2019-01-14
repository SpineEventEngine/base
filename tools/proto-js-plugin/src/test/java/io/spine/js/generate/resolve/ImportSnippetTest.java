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

package io.spine.js.generate.resolve;

import com.google.protobuf.Any;
import io.spine.code.js.FileName;
import io.spine.js.generate.resolve.given.Given;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImportSnippet should")
class ImportSnippetTest {

    private final FileName importedFile = FileName.from(Any.getDescriptor()
                                                           .getFile());
    private final ImportSnippet libraryFileImport = Given.googleProtobufImport(importedFile);

    @Test
    @DisplayName("extract the import path")
    void extractImportPath() {
        String importPath = libraryFileImport.path();
        assertThat(importPath).isEqualTo("google-protobuf/" + importedFile);
    }

    @Test
    @DisplayName("replace the import path")
    void replaceImportPath() {
        String newPath = "b";
        ImportSnippet updatedLine = libraryFileImport.replacePath(newPath);
        assertThat(updatedLine.path()).isEqualTo(newPath);
    }

    @Test
    @DisplayName("obtain the file path skipping the library name")
    void importedFilePathSkippingLibrary() {
        String filePath = libraryFileImport.importedFilePath();
        assertThat(filePath).isEqualTo(importedFile.value());
    }

    @Test
    @DisplayName("obtain the file path relative to the current directory")
    void importedFilePathRelativeToCurrentDir() {
        ImportSnippet fileImport = Given.importWithPath("./file.js");
        String filePath = fileImport.importedFilePath();
        assertThat(filePath).isEqualTo("file.js");
    }

    @Test
    @DisplayName("obtain the file path relative to the parent directory")
    void importedFilePathRelativeToParentDir() {
        String filePath = "../file.js";
        ImportSnippet fileImport = Given.importWithPath(filePath);
        String parsedFilePath = fileImport.importedFilePath();
        assertThat(parsedFilePath).isEqualTo(filePath);
    }

    @Test
    @DisplayName("recognize a Spine library")
    void recognizeSpine() {
        ImportSnippet spineImport = Given.importWithPath("spine/something");
        assertTrue(spineImport.isSpine());
    }

    @Test
    @DisplayName("recognize not a Spine library")
    void recognizeNotSpine() {
        assertFalse(libraryFileImport.isSpine());
    }

}
