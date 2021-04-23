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

package io.spine.tools.mc.java.gradle;

import io.spine.tools.java.fs.DefaultJavaPaths;
import io.spine.tools.gradle.testing.GradleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static io.spine.tools.gradle.BaseTaskName.build;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@code spine-protoc.gradle} plugin.
 */
@DisplayName("SpineProtoc should")
class SpineProtocTest {

    private static final String PROJECT_NAME = "empty-project";

    private GradleProject project;
    private File projectDir;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        projectDir = tempDirPath.toFile();
        project = GradleProject.newBuilder()
                               .setProjectFolder(projectDir)
                               .setProjectName(PROJECT_NAME)
                               .build();
    }

    @Disabled(
            "Turned off because it tests the side effect. " +
                    "In a project which does not have descriptor set file the directory should not be created."
    )
    @Test
    @DisplayName("create spine directory")
    void create_spine_directory() {
        project.executeTask(build);
        File spineDirPath =
                DefaultJavaPaths.at(projectDir)
                                .tempArtifacts()
                                .path()
                                .toFile();
        assertTrue(spineDirPath.exists());
    }
}
