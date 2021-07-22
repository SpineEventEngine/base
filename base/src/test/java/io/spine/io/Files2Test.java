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

import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.io.Files2.ensureFile;
import static io.spine.io.Files2.existsNonEmpty;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Files2` utility class should")
class Files2Test extends UtilityClassTest<Files2> {

    private File testFolder;

    Files2Test() {
        super(Files2.class);
    }

    @BeforeEach
    void setUp(@TempDir Path testFolderPath) {
        testFolder = testFolderPath.toFile();
    }

    @Nested
    @DisplayName("ensure a file exists")
    class EnsureThat {

        private File file;

        @BeforeEach
        void createFile() {
            String fileName = "ensure/exists/file" + TestValues.randomString() + ".txt";
            file = new File(testFolder.getAbsolutePath(), fileName);
        }

        @Test
        @DisplayName("with `File` argument")
        void fileExists() {
            boolean result = ensureFile(file);

            // Check that the file was created.
            assertTrue(result);
            assertTrue(file.exists());

            // When the file exists, the returned value is false.
            assertFalse(ensureFile(file));
        }

        @Test
        @DisplayName("with `Path` argument")
        void pathExists() {
            Path path = this.file.toPath();
            assertThat(ensureFile(path))
                    .isTrue();
            assertThat(file.exists())
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("verify that an existing file is not empty")
    class NonEmptyFile {

        @Test
        @DisplayName("returning `false` when existing file is empty")
        void whenNonEmpty() {
            File emptyFile = testFolder.toPath().resolve("empty file").toFile();

            assertThat(existsNonEmpty(emptyFile)).isFalse();
        }

        @Test
        @DisplayName("returning `false` when a file does not exist")
        void doesNotExist() {
            File doesNotExist = new File(TestValues.randomString());

            assertThat(existsNonEmpty(doesNotExist)).isFalse();
        }

        @Test
        @DisplayName("returning `true` if the existing file is not empty")
        void notEmpty() throws IOException {
            File nonEmptyFile = testFolder.toPath().resolve("non-empty file").toFile();
            String path = nonEmptyFile.getAbsolutePath();
            String charsetName = defaultCharset().name();
            try (PrintWriter out = new PrintWriter(path, charsetName)) {
                out.println(TestValues.randomString());
            }

            assertThat(existsNonEmpty(nonEmptyFile)).isTrue();
        }
    }
}
