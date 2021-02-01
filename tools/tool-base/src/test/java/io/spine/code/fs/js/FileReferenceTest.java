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

package io.spine.code.fs.js;

import com.google.common.testing.NullPointerTester;
import io.spine.code.fs.DirectoryReference;
import io.spine.code.fs.FileReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`FileReference` should")
class FileReferenceTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(FileReference.class);
    }

    @Test
    @DisplayName("not be an empty string")
    void notAcceptEmptyString() {
        assertIllegalArgument(() -> FileReference.of(""));
    }

    @Test
    @DisplayName("obtain file name skipping the path")
    void obtainFileName() {
        FileReference fileReference = FileReference.of("./../../foo/nested.js");
        String fileName = fileReference.fileName();
        assertThat(fileName).isEqualTo("nested.js");
    }

    @Test
    @DisplayName("obtain the directory skipping relative path")
    void directorySkipRelative() {
        FileReference fileReference = FileReference.of("./../../foo/bar/f.js");
        assertEquals("foo/bar", fileReference.directory()
                                             .value());
    }

    @Test
    @DisplayName("obtain the empty directory path")
    void emptyDirectory() {
        FileReference file = FileReference.of("./neighbour.txt");
        DirectoryReference directory = file.directory();
        assertThat(directory.elements())
                .containsExactly("");
    }
}
