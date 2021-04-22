/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.js.generate.imports;

import io.spine.tools.code.fs.FileReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.js.generate.imports.given.Given.importWithPath;
import static io.spine.tools.js.generate.imports.given.Given.relativeImportPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ImportStatement should")
class ImportStatementTest {

    private final File importOrigin = Paths.get("folder/nested/some-file.js")
                                           .toFile();
    private final ImportStatement statement = importWithPath(relativeImportPath(),
                                                             importOrigin);

    @Test
    @DisplayName("extract the import path")
    void extractImportPath() {
        FileReference fileReference = statement.path();
        assertThat(fileReference.value()).isEqualTo(relativeImportPath());
    }

    @Test
    @DisplayName("replace the import path")
    void replaceImportPath() {
        String newPath = "b";
        ImportStatement updatedStatement = statement.replacePath(newPath);
        FileReference updatedPath = updatedStatement.path();
        assertThat(updatedPath.value()).isEqualTo(newPath);
    }

    @Test
    @DisplayName("know about the absolute path to the imported file")
    void obtainImportedFilePath() {
        Path importedFilePath = statement.importedFilePath();
        Path expectedRoot = importOrigin.getParentFile()
                                        .toPath();
        Path expectedPath = expectedRoot.resolve(relativeImportPath())
                                        .normalize();
        assertEquals(expectedPath, importedFilePath);
    }
}
