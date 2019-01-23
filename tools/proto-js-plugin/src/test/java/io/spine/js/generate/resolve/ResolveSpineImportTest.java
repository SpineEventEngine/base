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
import io.spine.code.js.ImportPath;
import io.spine.js.generate.GenerationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.resolve.given.Given.importWithPath;
import static io.spine.js.generate.resolve.given.Given.mainProtoRoot;

@DisplayName("ResolveSpineImport should")
class ResolveSpineImportTest {

    private final SourceFile moduleMainFile = SourceFile.of(GenerationTask.class);
    private final Path importedFilePath = Paths.get("java")
                                               .resolve(moduleMainFile.getPath());
    private final Directory fakeProtoRoot = mainProtoRoot();
    private final FileName importOriginName = FileName.from(Any.getDescriptor()
                                                               .getFile());
    private final File importOrigin = fakeProtoRoot.resolve(importOriginName)
                                                   .toFile();

    @Test
    @DisplayName("resolve Spine library import if it is present in the module")
    void resolveSpineImport() {
        ImportSnippet importLine = importLine("spine/" + importedFilePath);
        //TODO:2019-01-23:dmytro.grankin: fix the test
        String expectedPathPrefix = "?";
        String expectedPath = expectedPathPrefix + importedFilePath;
        assertImportPath(importLine, expectedPath);
    }

    @Test
    @DisplayName("not resolve Spine library import if it present if the module")
    void notResolveSpineImport() {
        ImportSnippet importLine = importLine(importedFilePath.toString());
        assertImportPath(importLine, importedFilePath.toString());
    }

    private static void assertImportPath(ImportSnippet importLine, String expectedImportPath) {
        ImportSnippet resolved = resolveImport(importLine);
        ImportPath path = resolved.path();
        assertThat(path.value()).isEqualTo(expectedImportPath);
    }

    private static ImportSnippet resolveImport(ImportSnippet importLine) {
        ResolveSpineImport action = new ResolveSpineImport();
        return action.attemptResolve(importLine);
    }

    private ImportSnippet importLine(String importPath) {
        return importWithPath(importPath, importOrigin);
    }
}
