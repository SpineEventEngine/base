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

package io.spine.io;

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.io.Ensure.ensureDirectory;
import static io.spine.io.Ensure.ensureFile;
import static io.spine.testing.TestValues.randomString;
import static java.nio.file.Files.isDirectory;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Ensure` utilities class should")
class EnsureTest extends UtilityClassTest<Ensure> {

    EnsureTest() {
        super(Ensure.class);
    }


    @Nested
    @DisplayName("handle files via")
    class OnFiles {

        private File file;

        @BeforeEach
        void createFile(@TempDir Path tempDir) {
            File testFolder = tempDir.toFile();

            String fileName = "ensure/exists/file" + randomString() + ".txt";
            file = new File(testFolder.getAbsolutePath(), fileName);
        }

        @Test
        @DisplayName("`File` argument")
        void fileArg() {
            ensureFile(file);

            // Check that the file was created.
            assertTrue(file.exists());
            assertFalse(isDirectory(file.toPath()));
        }

        @Test
        @DisplayName("`Path` argument")
        void pathArg() {
            Path path = file.toPath();
            Object returnedValue = ensureFile(path);

            assertThat(file.exists())
                    .isTrue();
            assertThat(returnedValue)
                    .isEqualTo(path);
        }
    }

    @Nested
    @DisplayName("handle a directory creation")
    class OnDirectories {

        private Path tempDir;

        @BeforeEach
        void createTempDir(@TempDir Path tempDir) {
            this.tempDir = tempDir;
        }

        @Test
        @DisplayName("if it does not exist")
        void notExisting() {
            Path subDir = Paths.get("sub-1-" + randomString(), "sub-2-" + randomString());
            Path newDir = tempDir.resolve(subDir);

            // See that the directory does not exist.
            assertFalse(newDir.toFile().exists());

            ensureDirectory(newDir);

            assertTrue(isDirectory(newDir));
        }

        @Test
        @DisplayName("if it exists")
        void existing() {
            Path existingDir = tempDir.resolve(randomString());
            ensureDirectory(existingDir);

            // Now as we know that the directory exists, let's try it again.
            ensureDirectory(existingDir);

            assertTrue(isDirectory(existingDir));
        }

        @Test
        @DisplayName("rejecting existing file")
        void rejectFile() {
            Path filePath = tempDir.resolve("file" + randomString());
            File file = filePath.toFile();
            ensureFile(file);

            assertThrows(IllegalStateException.class, () -> ensureDirectory(filePath));
        }
    }
}
