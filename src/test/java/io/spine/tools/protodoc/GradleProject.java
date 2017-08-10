/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.protodoc;

import io.spine.gradle.TaskName;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code GradleProject} for the test needs.
 *
 * <p>{@code GradleProject} sets up {@linkplain TemporaryFolder test project directory}
 * and allows to execute Gradle tasks.
 *
 * @author Dmytro Grankin
 */
class GradleProject {

    /**
     * This value must be similar to the value in the test {@code build.gradle}.
     */
    private static final String MAIN_GEN_PROTO_LOCATION = "generated/main/java";
    private static final String TEST_SOURCE = "GeneratedJavaFile.java";
    private static final String BUILD_GRADLE_NAME = "build.gradle";
    private static final String EXT_GRADLE_NAME = "ext.gradle";

    private final GradleRunner gradleRunner;

    GradleProject(TemporaryFolder projectFolder) throws IOException {
        this.gradleRunner = GradleRunner.create()
                                        .withProjectDir(projectFolder.getRoot())
                                        .withDebug(true)
                                        .withPluginClasspath();
        writeBuildGradle();
        writeTestSource();
    }

    BuildResult executeTask(TaskName taskName) {
        return gradleRunner.withArguments(taskName.getValue())
                           .build();
    }

    private void writeTestSource() throws IOException {
        final String testSourceLocation = MAIN_GEN_PROTO_LOCATION + '/' + TEST_SOURCE;
        final Path resultingPath = gradleRunner.getProjectDir()
                                               .toPath()
                                               .resolve(testSourceLocation);
        final InputStream fileContent = getClass().getClassLoader()
                                                  .getResourceAsStream(TEST_SOURCE);
        Files.createDirectories(resultingPath.getParent());
        Files.copy(fileContent, resultingPath);
    }

    private void writeBuildGradle() throws IOException {
        final Path resultingPath = gradleRunner.getProjectDir()
                                               .toPath()
                                               .resolve(BUILD_GRADLE_NAME);
        final InputStream fileContent = getClass().getClassLoader()
                                                  .getResourceAsStream(BUILD_GRADLE_NAME);
        Files.createDirectories(resultingPath.getParent());
        Files.copy(fileContent, resultingPath);

        copyExtGradle();
    }

    private void copyExtGradle() throws IOException {
        final Path workingFolderPath = Paths.get(".")
                                            .toAbsolutePath();
        final Path extGradleSourcePath = workingFolderPath.getParent()
                                                          .getParent()
                                                          .resolve(EXT_GRADLE_NAME);
        final Path extGradleResultingPath = gradleRunner.getProjectDir()
                                                        .toPath()
                                                        .resolve(EXT_GRADLE_NAME);
        Files.copy(extGradleSourcePath, extGradleResultingPath);
    }
}
