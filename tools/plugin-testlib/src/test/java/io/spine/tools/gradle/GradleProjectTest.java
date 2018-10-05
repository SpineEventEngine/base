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

package io.spine.tools.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GradleProjectTest {

    private static final String PROJECT_NAME = "gradle_project_test";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void build_from_project_folder_and_project_name() {
        GradleProject project = GradleProject.newBuilder()
                                             .setProjectFolder(temporaryFolder.getRoot())
                                             .setProjectName(PROJECT_NAME)
                                             .build();
        assertNotNull(project);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
        // OK for this test case; result of `build` it ignored.
    @Test
    public void write_given_java_files() {
        String[] files = {"Foo.java", "Bar.java"};
        GradleProject.newBuilder()
                     .setProjectFolder(temporaryFolder.getRoot())
                     .setProjectName(PROJECT_NAME)
                     .addJavaFiles(files)
                     .build();
        @SuppressWarnings("DuplicateStringLiteralInspection")
            // "java" literal is copied with different semantics.
        Path root = temporaryFolder.getRoot()
                                   .toPath()
                                   .resolve("src")
                                   .resolve("main")
                                   .resolve("java");
        for (String fileName : files) {
            assertTrue(Files.exists(root.resolve(fileName)));
        }
    }

    @Test
    public void execute_faulty_build() {
        GradleProject project = GradleProject.newBuilder()
                                             .setProjectName(PROJECT_NAME)
                                             .setProjectFolder(temporaryFolder.getRoot())
                                             .addJavaFiles("Faulty.java")
                                             .build();
        BuildResult buildResult = project.executeAndFail(COMPILE_JAVA);
        assertNotNull(buildResult);
        BuildTask compileTask = buildResult.task(':' + COMPILE_JAVA.getValue());
        assertNotNull(compileTask);
        assertEquals(FAILED, compileTask.getOutcome());
    }
}
