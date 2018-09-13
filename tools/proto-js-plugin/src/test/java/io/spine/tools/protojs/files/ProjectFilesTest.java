/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.files;

import io.spine.code.DefaultProject;
import io.spine.testing.UtilityClassTest;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.io.Files.createTempDir;
import static io.spine.tools.protojs.files.ProjectFiles.mainDescriptorSetFile;
import static io.spine.tools.protojs.files.ProjectFiles.mainProtoJsLocation;
import static io.spine.tools.protojs.files.ProjectFiles.testDescriptorSetFile;
import static io.spine.tools.protojs.files.ProjectFiles.testProtoJsLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("ProjectFiles utility should")
class ProjectFilesTest extends UtilityClassTest<ProjectFiles> {

    private Project project;

    ProjectFilesTest() {
        super(ProjectFiles.class);
    }

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                                .withProjectDir(createTempDir())
                                .build();
    }

    @Test
    @DisplayName("obtain generated `main` proto JS files location for project")
    void getMainProtoLocation() {
        Path location = mainProtoJsLocation(project);
        String projectDir = project.getProjectDir()
                                   .getAbsolutePath();
        Path expected = Paths.get(projectDir, "proto", "main", "js");
        assertEquals(expected, location);
    }

    @Test
    @DisplayName("obtain generated `test` proto JS files location for project")
    void getTestProtoLocation() {
        Path location = testProtoJsLocation(project);
        String projectDir = project.getProjectDir()
                                   .getAbsolutePath();
        Path expected = Paths.get(projectDir, "proto", "test", "js");
        assertEquals(expected, location);
    }

    @Test
    @DisplayName("obtain `main` descriptor set file location for project")
    void getMainDescriptorSetFile() {
        File file = mainDescriptorSetFile(project);
        DefaultProject defaultProject = DefaultProject.at(project.getProjectDir());
        File expected = defaultProject.mainDescriptors();
        assertEquals(expected, file);
    }

    @Test
    @DisplayName("obtain `test` descriptor set file location for project")
    void getTestDescriptorSetFile() {
        File file = testDescriptorSetFile(project);
        DefaultProject defaultProject = DefaultProject.at(project.getProjectDir());
        File expected = defaultProject.testDescriptors();
        assertEquals(expected, file);
    }
}
