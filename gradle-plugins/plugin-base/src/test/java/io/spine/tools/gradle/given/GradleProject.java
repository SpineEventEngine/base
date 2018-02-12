/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.gradle.given;

import io.spine.tools.gradle.TaskName;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.Files.exists;
import static java.util.Arrays.asList;

/**
 * {@code GradleProject} for the test needs.
 *
 * <p>{@code GradleProject} sets up {@linkplain TemporaryFolder test project directory}
 * and allows to execute Gradle tasks.
 *
 * @author Dmytro Grankin
 */
public class GradleProject {

    private static final String BUILD_GRADLE_NAME = "build.gradle";
    private static final String EXT_GRADLE_NAME = "ext.gradle";
    private static final String BASE_PROTO_LOCATION = "src/main/proto/";
    private static final String BASE_JAVA_LOCATION = "src/main/java/";

    private static final String STACKTRACE_CLI_OPTION = "--stacktrace";
    private static final String DEBUG_CLI_OPTION = "--debug";

    private final String name;
    private final GradleRunner gradleRunner;
    private final boolean debug;

    private GradleProject(Builder builder) throws IOException {
        this.name = builder.name;
        this.debug = builder.debug;
        this.gradleRunner = GradleRunner.create()
                                        .withProjectDir(builder.folder.getRoot())
                                        .withDebug(builder.debug);
        writeBuildGradle();
        writeProtoFiles(builder.protoFileNames);
        writeJavaFiles(builder.javaFileNames);
    }

    private void writeProtoFiles(Iterable<String> fileNames) throws IOException {
        for (String protoFile : fileNames) {
            writeProto(protoFile);
        }
    }

    private void writeJavaFiles(Iterable<String> fileNames) throws IOException {
        for (String javaFile : fileNames) {
            writeJava(javaFile);
        }
    }

    public BuildResult executeTask(TaskName taskName) {
        return prepareRun(taskName).build();
    }

    public BuildResult executeAndFail(TaskName taskName) {
        return prepareRun(taskName).buildAndFail();
    }

    private GradleRunner prepareRun(TaskName taskName) {
        final String[] args = debug
                ? new String[]{taskName.getValue(), STACKTRACE_CLI_OPTION, DEBUG_CLI_OPTION}
                : new String[]{taskName.getValue(), STACKTRACE_CLI_OPTION};
        return gradleRunner.withArguments(args);
    }

    private void writeProto(String fileName) throws IOException {
        writeFile(fileName, BASE_PROTO_LOCATION);
    }

    private void writeJava(String fileName) throws IOException {
        writeFile(fileName, BASE_JAVA_LOCATION);
    }

    private void writeFile(String fileName, String dir) throws IOException {
        final String filePath = dir + fileName;
        final Path resultingPath = gradleRunner.getProjectDir()
                                               .toPath()
                                               .resolve(filePath);
        final String fullyQualifiedPath = name + '/' + filePath;
        final InputStream fileContent = getClass().getClassLoader()
                                                  .getResourceAsStream(fullyQualifiedPath);
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
        // Find `ext.gradle` in the project dir or in the parent dirs.
        Path extGradleDirPath = workingFolderPath;
        while (extGradleDirPath != null
                && !exists(extGradleDirPath.resolve(EXT_GRADLE_NAME))) {
            extGradleDirPath = extGradleDirPath.getParent();
        }
        checkNotNull(extGradleDirPath,
                     "ext.gradle file not found in %s or parent directories.",
                     workingFolderPath);
        final Path extGradleSourcePath = extGradleDirPath.resolve(EXT_GRADLE_NAME);
        final Path extGradleResultingPath = gradleRunner.getProjectDir()
                                                        .toPath()
                                                        .resolve(EXT_GRADLE_NAME);
        Files.copy(extGradleSourcePath, extGradleResultingPath);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private TemporaryFolder folder;

        /**
         * Determines whether the code can be debugged.
         *
         * <p>Affects the code executed during a {@linkplain #executeTask(TaskName) Gradle task}.
         *
         * <p>NOTE: when the value is {@code true}, all code is executed in a single JVM.
         * This leads to a high consumption of a memory.
         */
        private boolean debug;
        private final List<String> protoFileNames = newLinkedList();
        private final List<String> javaFileNames = newLinkedList();

        private Builder() {
            // Prevent direct instantiation of this class.
        }

        public Builder setProjectName(String name) {
            this.name = checkNotNull(name);
            return this;
        }

        public Builder setProjectFolder(TemporaryFolder folder) {
            this.folder = checkNotNull(folder);
            return this;
        }

        public Builder addProtoFile(String protoFileName) {
            checkArgument(!isNullOrEmpty(protoFileName));
            protoFileNames.add(protoFileName);
            return this;
        }

        public Builder addJavaFiles(String... fileNames) {
            javaFileNames.addAll(asList(fileNames));
            return this;
        }

        /**
         * Enables the debug mode of the GradleRunner.
         *
         * <p>Use debug mode only for temporary debug purposes.
         */
        @SuppressWarnings("unused") // Used only for debug purposes. Should never get
                                    // to e.g. CLI server.
        public Builder enableDebug() {
            this.debug = true;
            return this;
        }

        public Builder createProto(String fileName, Iterable<String> lines) {
            final Path sourcePath = folder.getRoot()
                                          .toPath()
                                          .resolve(BASE_PROTO_LOCATION + fileName);
            try {
                Files.createDirectories(sourcePath.getParent());
                Files.write(sourcePath, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
            return this;
        }

        public Builder addProtoFiles(Collection<String> protoFileNames) {
            for (String protoFileName : protoFileNames) {
                checkArgument(!isNullOrEmpty(protoFileName));
            }

            this.protoFileNames.addAll(protoFileNames);
            return this;
        }

        public GradleProject build() {
            try {
                checkNotNull(name);
                checkNotNull(folder);
                return new GradleProject(this);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }
    }
}
