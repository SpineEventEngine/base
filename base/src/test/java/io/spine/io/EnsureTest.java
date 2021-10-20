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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.io.Ensure.ensureFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Ensure` utilities class should")
class EnsureTest extends UtilityClassTest<Ensure> {

    EnsureTest() {
        super(Ensure.class);
    }

    private File file;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        File testFolder = tempDir.toFile();

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
