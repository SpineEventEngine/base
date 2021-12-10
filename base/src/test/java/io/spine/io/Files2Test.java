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
import static io.spine.io.Files2.existsNonEmpty;
import static java.nio.charset.Charset.defaultCharset;

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
    @DisplayName("verify that an existing file is not empty")
    class NonEmptyFile {

        @Test
        @DisplayName("returning `false` when existing file is empty")
        void whenNonEmpty() {
            var emptyFile = testFolder.toPath().resolve("empty file").toFile();

            assertThat(existsNonEmpty(emptyFile)).isFalse();
        }

        @Test
        @DisplayName("returning `false` when a file does not exist")
        void doesNotExist() {
            var doesNotExist = new File(TestValues.randomString());

            assertThat(existsNonEmpty(doesNotExist)).isFalse();
        }

        @Test
        @DisplayName("returning `true` if the existing file is not empty")
        void notEmpty() throws IOException {
            var nonEmptyFile = testFolder.toPath().resolve("non-empty file").toFile();
            var path = nonEmptyFile.getAbsolutePath();
            var charsetName = defaultCharset().name();
            try (var out = new PrintWriter(path, charsetName)) {
                out.println(TestValues.randomString());
            }

            assertThat(existsNonEmpty(nonEmptyFile)).isTrue();
        }
    }
}
