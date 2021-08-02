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

package io.spine.tools.gradle.project;

import io.spine.tools.gradle.GeneratedSourceRoot;
import io.spine.tools.gradle.GeneratedSourceSet;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.Projects.sourceSet;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

@DisplayName("`ProjectSourceSuperset` should")
class ProjectSourceSupersetTest {

    private Project project;

    @BeforeEach
    void setUp(@TempDir Path projectPath) {
        project = ProjectBuilder
                .builder()
                .withProjectDir(projectPath.toFile())
                .build();
        project.getPluginManager()
               .apply(JavaPlugin.class);
    }

    @Test
    @DisplayName("mark a given generated code root as a source set in `main` scope")
    void addSourceSet() {
        checkMarks(MAIN_SOURCE_SET_NAME);
    }

    @Test
    @DisplayName("mark a given generated code root as a source set in `test` scope")
    void addTestSourceSet() {
        checkMarks(TEST_SOURCE_SET_NAME);
    }

    private void checkMarks(String sourceSetName) {
        GeneratedSourceRoot root = GeneratedSourceRoot.of(project);
        ProjectSourceSuperset structure = ProjectSourceSuperset.of(project);
        structure.register(root);

        SourceSet sourceSet = sourceSet(project, sourceSetName);
        SourceDirectorySet javaDirs = sourceSet.getJava();
        GeneratedSourceSet generatedSourceSet = root.sourceSet(sourceSetName);
        assertThat(javaDirs.getSrcDirs()).containsAtLeast(generatedSourceSet.java().toFile(),
                                                          generatedSourceSet.grpc().toFile(),
                                                          generatedSourceSet.spine().toFile());
        assertThat(sourceSet.getResources().getSrcDirs())
                .contains(generatedSourceSet.resources().toFile());
    }
}
