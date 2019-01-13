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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ResolvableImport should")
class ResolvableImportTest {

    private final FileName importedFile = FileName.from(Any.getDescriptor()
                                                           .getFile());
    private final ResolvableImport libraryFileImport = googleProtobufImport(importedFile);

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
        ResolvableImport updatedLine = libraryFileImport.replacePath(newPath);
        assertThat(updatedLine.path()).isEqualTo(newPath);
    }

    @Test
    @DisplayName("obtain the import path skipping the first part")
    void importPathWithSkippedLibrary() {
        String pureFilePath = libraryFileImport.pathSkipFirstPart();
        assertThat(pureFilePath).isEqualTo(importedFile.value());
    }

    @Test
    @DisplayName("recognize a Spine library")
    void recognizeSpine() {
        ResolvableImport spineImport = importWithPath("spine/something");
        assertTrue(spineImport.isSpine());
    }

    @Test
    @DisplayName("recognize not a Spine library")
    void recognizeNotSpine() {
        assertFalse(libraryFileImport.isSpine());
    }

    private static ResolvableImport googleProtobufImport(FileName file) {
        return importWithPath("google-protobuf/" + file);
    }

    private static ResolvableImport importWithPath(String path) {
        return new ResolvableImport(
                format("let hah = require('%s');", path));
    }
}
