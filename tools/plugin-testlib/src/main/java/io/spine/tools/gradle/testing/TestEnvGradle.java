/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.testing;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.String.format;

/**
 * Creates a Gradle script which points to the root directory of the project.
 *
 * <p>The variable {@link #VAR_NAME} should be later used for referencing dependencies
 * in the test Gradle sub-projects.
 */
final class TestEnvGradle {

    private static final String VAR_NAME = "enclosingRootDir";
    private static final String FILE_NAME = "test-env.gradle";

    private final Path projectRoot;
    private final Path testProjectRoot;

    TestEnvGradle(Path projectRoot, Path testProjectRoot) {
        this.projectRoot = projectRoot;
        this.testProjectRoot = testProjectRoot;
    }

    void createFile() throws IOException {
        Path testEnvFile = testProjectRoot.resolve(FILE_NAME);
        List<String> lines = content();
        Files.write(testEnvFile, lines);
    }

    private List<String> content() {
        String unixLikeRootPath = projectRoot.toString()
                                             .replace('\\', '/');
        List<String> lines = ImmutableList.of(
                "ext {",
                format("    %s = '%s'", VAR_NAME, unixLikeRootPath),
                "}"
        );
        return lines;
    }
}
