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

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.resolve.given.Given.importWithPath;
import static io.spine.js.generate.resolve.given.Given.mainProtoRoot;
import static io.spine.js.generate.resolve.given.Given.testProtoRoot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("ResolveSpineImport should")
class ResolveSpineImportTest {

    private final SourceFile moduleMainFile = SourceFile.of(GenerationTask.class);
    private final Path importedFilePath = Paths.get("java")
                                               .resolve(moduleMainFile.getPath());
    private final Directory fakeProtoRoot = mainProtoRoot();
    private final FileName importInto = FileName.from(Any.getDescriptor()
                                                         .getFile());

    @Test
    @DisplayName("resolve Spine library import if it is present in the module")
    void resolveSpineImport() {
        ImportSnippet importLine = importLine("spine/" + importedFilePath);
        String expectedPathPrefix = ResolveSpineImport.fileRelativeToSources(fakeProtoRoot,
                                                                             importInto);
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
    @DisplayName("not skip the import of a file belonging to the module")
    void checkBelongsToModule() {
        ImportPath importPath = ImportPath.of("spine/" + importedFilePath);
        ResolveSpineImport action = new ResolveSpineImport(fakeProtoRoot);
        boolean shouldSkip = action.shouldNotResolve(importPath);
        assertFalse(shouldSkip);
    }

    @Test
    @DisplayName("resolve the sources directory")
    void resolveSourcesDirectory() {
        Path sourcesDirectoryPath = ResolveSpineImport.sourcesPath(fakeProtoRoot);
        Path expected = Paths.get("src/main")
                             .toAbsolutePath();
        assertEquals(expected, sourcesDirectoryPath);
    }

    @Test
    @DisplayName("compose the relative path to sources for a main file")
    void mainFileRelativeToSources() {
        String path = ResolveSpineImport.fileRelativeToSources(mainProtoRoot(), importInto);
        assertThat(path).isEqualTo("../../../");
    }

    @Test
    @DisplayName("compose the relative path to sources for a test file")
    void testFileRelativeToSources() {
        String path = ResolveSpineImport.fileRelativeToSources(testProtoRoot(), importInto);
        assertThat(path).isEqualTo("../../../../main/");
    }

    private void assertImportPath(ImportSnippet importLine, String expectedImportPath) {
        ImportSnippet resolved = resolveImport(importLine);
        ImportPath path = resolved.path();
        assertThat(path.value()).isEqualTo(expectedImportPath);
    }

    private ImportSnippet resolveImport(ImportSnippet importLine) {
        ResolveSpineImport action = new ResolveSpineImport(fakeProtoRoot);
        return action.attemptResolve(importLine);
    }

    private ImportSnippet importLine(String importPath) {
        return importWithPath(importPath, importInto);
    }
}
