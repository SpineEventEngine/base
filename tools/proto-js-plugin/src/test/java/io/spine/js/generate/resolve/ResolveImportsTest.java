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

import com.google.common.collect.ImmutableList;
import com.google.common.truth.IterableSubject;
import io.spine.code.js.Directory;
import io.spine.js.generate.given.GivenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.resolve.given.Given.newModule;
import static java.util.Arrays.asList;

@ExtendWith(TempDirectory.class)
@DisplayName("ResolveImports task should")
class ResolveImportsTest {

    private final Directory generatedProtoDir = GivenProject.mainProtoSources();
    private final ExternalModule module = newModule("test-module", "root-dir");
    private final ResolveImports task = new ResolveImports(generatedProtoDir,
                                                           ImmutableList.of(module));
    private Path tempDirectory;
    private Path testFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        tempDirectory = tempDir;
        testFile = tempDirectory.resolve("with-imports.js");
    }

    @DisplayName("replace a relative import of a missing file")
    @Test
    void resolveMissingFileImport() throws IOException {
        writeTestFile("require('./root-dir/missing.js');");
        fileAfterResolve().containsExactly("require('test-module/root-dir/missing.js');");
    }

    @DisplayName("not replace a relative import of an existing file")
    @Test
    void notResolveExistingFile() throws IOException {
        String originalImport = "require('./root-dir/not-missing.js');";
        createFile("root-dir/not-missing.js");
        writeTestFile(originalImport);
        fileAfterResolve().containsExactly(originalImport);
    }

    @DisplayName("not replace a relative import if not matches patterns")
    @Test
    void notReplaceIfNotProvided() throws IOException {
        String originalImport = "require('./abcdef/missing.js');";
        writeTestFile(originalImport);
        fileAfterResolve().containsExactly(originalImport);
    }

    private void createFile(String name) throws IOException {
        Path filePath = tempDirectory.resolve(name);
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
    }

    private void writeTestFile(String... lines) throws IOException {
        Files.write(testFile, asList(lines));
    }

    private IterableSubject fileAfterResolve() throws IOException {
        task.resolveInFile(testFile);
        List<String> lines = Files.readAllLines(testFile);
        return assertThat(lines);
    }
}
