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

package io.spine.tools.gradle.compiler;

import io.spine.tools.DefaultProject;
import io.spine.tools.gradle.given.GradleProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static io.spine.tools.gradle.TaskName.BUILD;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@code spine-protoc.gradle} plugin.
 *
 * @author Dmytro Dashenkov
 */
public class SpineProtocShould {

    private static final String PROJECT_NAME = "empty-project";

    private GradleProject project;

    @Rule
    public TemporaryFolder projectDir = new TemporaryFolder();

    @Before
    public void setUp() {
        project = GradleProject.newBuilder()
                               .setProjectFolder(projectDir)
                               .setProjectName(PROJECT_NAME)
                               .build();
    }

    @Ignore(
        "Turned off because it tests the side effect. " +
        "In a project which does not have descriptor set file the directory should not be created."
    )
    @Test
    public void create_spine_directory() {
        project.executeTask(BUILD);
        final File spineDirPath = DefaultProject.at(projectDir.getRoot())
                                                .tempArtifacts();
        assertTrue(spineDirPath.exists());
    }
}
