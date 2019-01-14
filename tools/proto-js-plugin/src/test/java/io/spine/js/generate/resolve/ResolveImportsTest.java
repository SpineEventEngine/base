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
import io.spine.code.java.SourceFile;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.js.generate.GenerationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.resolve.ResolveImports.SRC_RELATIVE_TO_PROTO;
import static io.spine.js.generate.resolve.given.Given.importWithPath;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ResolveImports task should")
class ResolveImportsTest {

    private final SourceFile moduleMainFile = SourceFile.of(GenerationTask.class);
    private final Path importedFilePath = Paths.get("java")
                                               .resolve(moduleMainFile.getPath());
    private final Directory fakeProtoRoot = Directory.at(FileSystems.getDefault()
                                                                    .getPath("src/main/proto")
                                                                    .toAbsolutePath());
    private final FileName importInto = FileName.from(Any.getDescriptor()
                                                         .getFile());

    @Test
    @DisplayName("resolve Spine library import if it is present in the module")
    void resolveSpineImport() {
        ImportSnippet importLine = importLine("spine/" + importedFilePath);
        String expectedPathPrefix = SRC_RELATIVE_TO_PROTO + importInto.pathToRoot();
        String expectedPath = expectedPathPrefix + importedFilePath;
        assertImportPath(importLine, expectedPath);
    }

    @Test
    @DisplayName("not resolve Spine library import if it present if the module")
    void notResolveSpineImport() {
        ImportSnippet importLine = importLine(importedFilePath.toString());
        assertImportPath(importLine, importedFilePath.toString());
    }

    @Test
    @DisplayName("check imported file belongs to the module")
    void checkBelongsToModule() {
        String path = importedFilePath.toString();
        boolean belongs = ResolveImports.belongsToModule(path, fakeProtoRoot);
        assertTrue(belongs);
    }

    private void assertImportPath(ImportSnippet importLine, String expectedImportPath) {
        ImportSnippet resolved = resolveImport(importLine);
        assertThat(resolved.path()).isEqualTo(expectedImportPath);
    }

    private ImportSnippet resolveImport(ImportSnippet importLine) {
        return ResolveImports.resolveImport(importLine, fakeProtoRoot);
    }

    private ImportSnippet importLine(String importPath) {
        return importWithPath(importPath, importInto);
    }
}
