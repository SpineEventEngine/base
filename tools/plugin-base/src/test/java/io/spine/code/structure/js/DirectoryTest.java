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

package io.spine.code.structure.js;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.code.js.LibraryFile.INDEX;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Directory should")
class DirectoryTest {

    private static final Path DIRECTORY_PATH = Paths.get("/home/user/directory");

    private Directory directory;

    @BeforeEach
    void setUp() {
        directory = Directory.at(DIRECTORY_PATH);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(Directory.class);
        new NullPointerTester().testAllPublicInstanceMethods(directory);
    }

    @Test
    @DisplayName("resolve file name")
    void resolveFileName() {
        String rawName = "tasks_pb.js";
        FileName fileName = FileName.of(rawName);
        Path resolved = directory.resolve(fileName);
        Path expected = DIRECTORY_PATH.resolve(rawName);
        assertEquals(expected, resolved);
    }

    @Test
    @DisplayName("resolve LibraryFile")
    void resolveCommonFileName() {
        Path resolved = directory.resolve(INDEX);
        Path expected = DIRECTORY_PATH.resolve(INDEX.toString());
        assertEquals(expected, resolved);
    }
}
