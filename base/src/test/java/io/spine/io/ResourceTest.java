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

import com.google.common.io.CharStreams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.io.Resource.file;
import static io.spine.testing.Assertions.assertIllegalState;
import static java.util.UUID.randomUUID;

@DisplayName("Resource should")
class ResourceTest {

    private static final String EXISTING_RESOURCE = "test_resource.txt";
    private static final ClassLoader CLASS_LOADER = ResourceTest.class.getClassLoader();

    @Test
    @DisplayName("throw ISE if queried for a non-existing file")
    void throwOnNonExisting(@TempDir Path path) {
        Path nonExistentFilePath = path.resolve(randomUUID()
                                                    .toString());
        File nonExistingFile = nonExistentFilePath.toFile();
        String name = nonExistingFile.getName();
        Resource file = file(name, CLASS_LOADER);
        assertThat(file.exists()).isFalse();
        assertIllegalState(file::locate);
    }

    @Test
    @DisplayName("correctly identify a file that is contained under the resources directory")
    void correctlyPickUrlsUp() throws IOException {
        Resource resource = Resource.file(EXISTING_RESOURCE, CLASS_LOADER);
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.locate()).isNotNull();
        assertThat(resource.locateAll()).hasSize(1);
        try (InputStream stream = resource.open()) {
            assertThat(stream.available()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("open as a byte stream")
    void openAsBytes() throws IOException {
        Resource resource = Resource.file(EXISTING_RESOURCE, CLASS_LOADER);
        try (InputStream stream = resource.open()) {
            assertThat(stream.available()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("open as a char stream")
    void openAsChars() throws IOException {
        Resource resource = Resource.file(EXISTING_RESOURCE, CLASS_LOADER);
        try (Reader reader = resource.openAsText()) {
            String content = CharStreams.toString(reader);
            assertThat(content).isNotEmpty();
        }
    }
}
