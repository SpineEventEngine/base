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

import com.google.common.base.Joiner;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.spine.tools.gradle.TaskName.FORMAT_PROTO_DOC;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * A helper class for the {@link ProtoJavadocPlugin} testing.
 *
 * @author Dmytro Grankin
 */
class Given {

    private static final ClassLoader CLASS_LOADER = Given.class.getClassLoader();

    /**
     * The {@code protoJavadoc.mainGenProtoDir} value from the plugin configuration.
     *
     * <p>This value is located in the test {@code build.gradle}.
     */
    private static final String MAIN_GEN_PROTO_LOCATION = "generated/main/java";
    private static final String BUILD_GRADLE_NAME = "build.gradle";
    private static final String EXT_GRADLE_NAME = "ext.gradle";

    private Given() {
        // Prevent instantiation of this utility class.
    }

    static void formatAndAssert(String expectedContent, String contentToFormat,
                                TemporaryFolder folder) throws IOException {
        final Path formattedFilePath = format(contentToFormat, folder);
        final List<String> formattedLines = Files.readAllLines(formattedFilePath, UTF_8);
        final String mergedLines = Joiner.on(lineSeparator())
                                         .join(formattedLines);
        assertEquals(expectedContent, mergedLines);
    }

    private static Path format(String fileContent, TemporaryFolder folder) throws IOException {
        final GradleRunner runner = GradleRunner.create()
                                                .withProjectDir(folder.getRoot())
                                                .withPluginClasspath();
        writeBuildGradle(runner);
        final Path testSourcePath = writeTestSource(fileContent, runner);
        runner.withArguments(FORMAT_PROTO_DOC.getValue(), "--stacktrace")
              .build();
        return testSourcePath;
    }

    private static Path writeTestSource(String content,
                                        GradleRunner gradleRunner) throws IOException {
        final String sourceRelativePath = MAIN_GEN_PROTO_LOCATION + '/' + "TestSource.java";
        final Path resultingPath = gradleRunner.getProjectDir()
                                               .toPath()
                                               .resolve(sourceRelativePath);
        Files.createDirectories(resultingPath.getParent());
        Files.write(resultingPath, content.getBytes());
        return resultingPath;
    }

    private static void writeBuildGradle(GradleRunner gradleRunner) throws IOException {
        final Path resultingPath = gradleRunner.getProjectDir()
                                               .toPath()
                                               .resolve(BUILD_GRADLE_NAME);
        final InputStream fileContent = CLASS_LOADER.getResourceAsStream(BUILD_GRADLE_NAME);
        Files.createDirectories(resultingPath.getParent());
        Files.copy(fileContent, resultingPath);

        copyExtGradle(gradleRunner);
    }

    private static void copyExtGradle(GradleRunner gradleRunner) throws IOException {
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
