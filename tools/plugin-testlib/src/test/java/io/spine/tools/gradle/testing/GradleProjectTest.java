/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.testing.NullPointerTester;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.io.TempDir;

@DisplayName("GradleProject should")
class GradleProjectTest {

    private static final String PROJECT_NAME = "gradle_project_test";

    private File temporaryFolder;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        temporaryFolder = tempDirPath.toFile();
    }

    @Test
    @DisplayName("build from project folder and project name")
    void buildFromProjectFolderAndProjectName() {
        GradleProject project = GradleProject
                .newBuilder()
                .setProjectFolder(temporaryFolder)
                .setProjectName(PROJECT_NAME)
                .build();
        assertNotNull(project);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    // OK for this test case; result of `build` it ignored.
    @Test
    @DisplayName("write given Java files")
    void writeGivenJavaFiles() {
        String[] files = {"Foo.java", "Bar.java"};
        GradleProject.newBuilder()
                     .setProjectFolder(temporaryFolder)
                     .setProjectName(PROJECT_NAME)
                     .addJavaFiles(files)
                     .build();
        Path root = temporaryFolder.toPath()
                                   .resolve("src")
                                   .resolve("main")
                                   .resolve("java");
        for (String fileName : files) {
            Path file = root.resolve(fileName);
            assertTrue(Files.exists(file));
        }
    }

    @Test
    @DisplayName("execute faulty build")
    void executeFaultyBuild() {
        GradleProject project = GradleProject.newBuilder()
                                             .setProjectName(PROJECT_NAME)
                                             .setProjectFolder(temporaryFolder)
                                             .addJavaFiles("Faulty.java")
                                             .build();
        BuildResult buildResult = project.executeAndFail(compileJava);
        assertNotNull(buildResult);
        BuildTask compileTask = buildResult.task(compileJava.path());
        assertNotNull(compileTask);
        assertEquals(FAILED, compileTask.getOutcome());
    }

    @Nested
    @DisplayName("have Builder which")
    class Builder {

        @Test
        @DisplayName("does not accept nulls")
        void nulls() {
            GradleProject.Builder instance = GradleProject.newBuilder();
            new NullPointerTester()
                    .testAllPublicInstanceMethods(instance);
        }
    }
}
