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

import com.google.common.annotations.VisibleForTesting;
import io.spine.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates {@link #BUILD_GRADLE build.gradle} or {@link #BUILD_GRADLE_KTS build.gradle.kts} file in
 * the root of the test project, copying it from resources.
 *
 * <p>If a {@code build.gradle} file is found, it is used for the build. Otherwise, if
 * a {@code build.gradle.kts} file is found, it is used for the build.
 */
final class BuildGradle {

    /**
     * The name of the build file.
     */
    @VisibleForTesting
    static final String BUILD_GRADLE = "build.gradle";
    private static final String BUILD_GRADLE_KTS = "build.gradle.kts";

    private final Path testProjectRoot;

    BuildGradle(Path root) {
        testProjectRoot = root;
    }

    /**
     * Copies a Gradle build script from the classpath into the test project directory.
     *
     * @throws IOException
     *         if the file cannot be written
     */
    void createFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Resource buildGradle = Resource.file(BUILD_GRADLE, classLoader);
        Resource buildGradleKts = Resource.file(BUILD_GRADLE_KTS, classLoader);
        Path resultingPath;
        Resource file;
        if (buildGradle.exists()) {
            resultingPath = testProjectRoot.resolve(BUILD_GRADLE);
            file = buildGradle;
        } else if (buildGradleKts.exists()) {
            resultingPath = testProjectRoot.resolve(BUILD_GRADLE_KTS);
            file = buildGradleKts;
        } else {
            throw new IllegalStateException("Build script is not found.");
        }

        try (InputStream fileContent = file.open()) {
            Files.createDirectories(resultingPath.getParent());
            checkNotNull(fileContent);
            Files.copy(fileContent, resultingPath);
        }
    }
}
