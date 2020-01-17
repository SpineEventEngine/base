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

package io.spine.io;

import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import static io.spine.io.Files2.ensureFile;
import static io.spine.io.Files2.existsNonEmpty;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("Files2 utility class should")
class Files2Test extends UtilityClassTest<Files2> {

    private File testFolder;

    Files2Test() {
        super(Files2.class);
    }

    @BeforeEach
    void setUp(@TempDir Path testFolderPath) {
        testFolder = testFolderPath.toFile();
    }

    @Test
    @DisplayName("ensure file")
    void ensure_file() {
        File expected = new File(testFolder.getAbsolutePath(), "with/sub/dirs/file.txt");

        boolean result = ensureFile(expected);

        // Check that the file was created.
        assertTrue(result);
        assertTrue(expected.exists());

        // When the file exists, the returned value is false.
        assertFalse(ensureFile(expected));
    }

    @Test
    @DisplayName("verify non-empty file")
    void verify_non_empty_file() throws IOException {
        File empty = testFolder.toPath()
                               .resolve("empty file")
                               .toFile();
        assertFalse(existsNonEmpty(empty));

        File doesNotExist = new File(TestValues.randomString());
        assertFalse(existsNonEmpty(doesNotExist));

        File nonEmpty = testFolder.toPath()
                                  .resolve("non-empty file")
                                  .toFile();
        String path = nonEmpty.getAbsolutePath();
        String charsetName = defaultCharset().name();
        try (PrintWriter out = new PrintWriter(path, charsetName)) {
            out.println(TestValues.randomString());
        }
        assertTrue(existsNonEmpty(nonEmpty));
    }
}
