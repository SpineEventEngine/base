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
import com.google.protobuf.StringValue;
import io.spine.code.js.FileName;
import io.spine.code.js.ImportPath;
import io.spine.js.generate.resolve.given.Given;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("ImportSnippet should")
class ImportSnippetTest {

    private final FileName importedFile = FileName.from(Any.getDescriptor()
                                                           .getFile());
    private final FileName importSource = FileName.from(StringValue.getDescriptor()
                                                                   .getFile());
    private final ImportSnippet libraryFileImport = Given.googleProtobufImport(importedFile,
                                                                               importSource);

    @Test
    @DisplayName("extract the import path")
    void extractImportPath() {
        ImportPath importPath = libraryFileImport.path();
        ImportPath expected = ImportPath.of("google-protobuf/" + importedFile);
        assertThat(importPath).isEqualTo(expected);
    }

    @Test
    @DisplayName("replace the import path")
    void replaceImportPath() {
        String newPath = "b";
        ImportSnippet updatedLine = libraryFileImport.replacePath(newPath);
        ImportPath updatedPath = updatedLine.path();
        assertThat(updatedPath.value()).isEqualTo(newPath);
    }
}
