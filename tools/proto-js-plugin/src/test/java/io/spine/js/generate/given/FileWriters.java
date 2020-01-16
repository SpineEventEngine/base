/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.given;

import com.google.common.base.Charsets;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A helper tool for checking file I/O operations result.
 */
public final class FileWriters {

    private FileWriters() {
    }

    public static void
    assertFileContains(Path filePath, CharSequence toSearch) throws IOException {
        assertTrue(exists(filePath));
        byte[] bytes = readAllBytes(filePath);
        String fileContent = new String(bytes, Charsets.UTF_8);
        assertThat(fileContent).contains(toSearch);
    }

    public static void
    assertFileNotContains(Path filePath, CharSequence toSearch) throws IOException {
        assertTrue(exists(filePath));
        byte[] bytes = readAllBytes(filePath);
        String fileContent = new String(bytes, Charsets.UTF_8);
        assertThat(fileContent).doesNotContain(toSearch);
    }
}
