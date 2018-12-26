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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.gradle.api.Action;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.util.Arrays.asList;

/**
 * {@code GradleProject} for the test needs.
 *
 * <p>{@code GradleProject} operates in the given {@linkplain File test project directory} and
 * allows to execute Gradle tasks.
 */
public final class GradleProject {

    public static final String JAVA_PLUGIN_ID = "java";

    private static final String BUILD_GRADLE_NAME = "build.gradle";
    private static final String VERSION_GRADLE_NAME = "version.gradle";
    private static final String TEST_ENV_GRADLE_NAME = "testEnv.gradle";
    private static final String BASE_PROTO_LOCATION = "src/main/proto/";
    private static final String BASE_JAVA_LOCATION = "src/main/java/";
    private static final String CONFIG_DIR_NAME = "config";

    private static final String STACKTRACE_CLI_OPTION = "--stacktrace";
    private static final String DEBUG_CLI_OPTION = "--debug";

    private final String name;
    private final GradleRunner gradleRunner;
    private final boolean debug;

    private GradleProject(Builder builder) throws IOException {
        this.name = builder.name;
        this.debug = builder.debug;
        this.gradleRunner = GradleRunner.create()
                                        .withProjectDir(builder.folder)
                                        .withDebug(builder.debug);
        writeGradleScripts();
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

    @CanIgnoreReturnValue
    public BuildResult executeTask(TaskName taskName) {
        return prepareRun(taskName).build();
    }

    @CanIgnoreReturnValue
    public BuildResult executeAndFail(TaskName taskName) {
        return prepareRun(taskName).buildAndFail();
    }

    private GradleRunner prepareRun(TaskName taskName) {
        String task = taskName.getValue();
        String[] args = debug
                        ? new String[]{task, STACKTRACE_CLI_OPTION, DEBUG_CLI_OPTION}
                        : new String[]{task, STACKTRACE_CLI_OPTION};
        return gradleRunner.withArguments(args);
    }

    private void writeProto(String fileName) throws IOException {
        writeFile(fileName, BASE_PROTO_LOCATION);
    }

    private void writeJava(String fileName) throws IOException {
        writeFile(fileName, BASE_JAVA_LOCATION);
    }

    private void writeFile(String fileName, String dir) throws IOException {
        String filePath = dir + fileName;
        Path resultingPath = testRoot().resolve(filePath);
        String fullyQualifiedPath = name + '/' + filePath;
        InputStream fileContent = getClass().getClassLoader()
                                            .getResourceAsStream(fullyQualifiedPath);
        Files.createDirectories(resultingPath.getParent());
        checkNotNull(fileContent);
        Files.copy(fileContent, resultingPath);
    }

    private void writeGradleScripts() throws IOException {
        writeBuildGradle();
        Path projectRoot = findRoot();
        createTestEnvExt(projectRoot);
        copyVersionGradle(projectRoot);
        copyConfig(projectRoot);
    }

    /**
     * Creates an extension file, which provides information about the project
     * running this test project.
     *
     * <p>In particular, it includes the root of the project running this test project.
     */
    private void createTestEnvExt(Path projectRoot) throws IOException {
        Path testEnvPath = testRoot().resolve(TEST_ENV_GRADLE_NAME);
        String unixLikeRootPath = projectRoot.toString()
                                             .replace('\\', '/');
        List<String> lines = ImmutableList.of(
                "ext {",
                format("    enclosingRootDir = '%s'", unixLikeRootPath),
                "}"
        );
        Files.write(testEnvPath, lines);
    }

    private void writeBuildGradle() throws IOException {
        Path resultingPath = testRoot().resolve(BUILD_GRADLE_NAME);
        InputStream fileContent = getClass().getClassLoader()
                                            .getResourceAsStream(BUILD_GRADLE_NAME);
        Files.createDirectories(resultingPath.getParent());
        checkNotNull(fileContent);
        Files.copy(fileContent, resultingPath);
    }

    /**
     * Copies the {@code version.gradle} file from the root of the project
     * into the root of the test project.
     */
    private void copyVersionGradle(Path projectRoot) throws IOException {
        Path sourcePath = projectRoot.resolve(VERSION_GRADLE_NAME);
        Path targetPath = testRoot().resolve(VERSION_GRADLE_NAME);
        Files.copy(sourcePath, targetPath);
    }

    /**
     * Copies the content of the {@code config} directory from the root of
     * this project into the root of the test project.
     */
    private void copyConfig(Path projectRoot) {
        Path sourcePath = projectRoot.resolve(CONFIG_DIR_NAME);
        Path targetPath = testRoot().resolve(CONFIG_DIR_NAME);
        copyFolder(sourcePath, targetPath);
    }

    /**
     * Copies the content of the {@code src} directory into {@code dest} directory.
     */
    private static void copyFolder(Path src, Path dest) {
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(sourcePath -> {
                try {
                    Path destPath = dest.resolve(src.relativize(sourcePath));
                    Files.copy(sourcePath, destPath);
                } catch (IOException e) {
                    throw illegalStateWithCauseOf(e);
                }

            });
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Path testRoot() {
        return gradleRunner.getProjectDir()
                           .toPath();
    }

    /**
     * Finds a root directory of the project by searching for the file
     * named {@link #VERSION_GRADLE_NAME ext.gradle}.
     *
     * <p>Starts from the current directory, climbing up, if the file is not found.
     *
     * @throws IllegalStateException if the file is not found
     */
    private static Path findRoot() {
        Path workingFolderPath = Paths.get(".")
                                      .toAbsolutePath();
        Path extGradleDirPath = workingFolderPath;
        while (extGradleDirPath != null
                && !exists(extGradleDirPath.resolve(VERSION_GRADLE_NAME))) {
            extGradleDirPath = extGradleDirPath.getParent();
        }
        checkState(extGradleDirPath != null,
                   "ext.gradle file not found in %s or parent directories.",
                   workingFolderPath);
        return extGradleDirPath;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public enum NoOp implements Action<Object> {
        ACTION;

        @Override
        public void execute(Object ignored) {
            // NoOp
        }

        @SuppressWarnings("unchecked") // No matter, as the action is NoOp.
        public static <T> Action<T> action() {
            return ((Action<T>) ACTION);
        }
    }

    public static class Builder {

        private String name;
        private File folder;

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

        public Builder setProjectFolder(File folder) {
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
        @SuppressWarnings({"unused", "RedundantSuppression"})
        // Used only for debug purposes. Should never get to e.g. CLI server.
        public Builder enableDebug() {
            this.debug = true;
            return this;
        }

        /**
         * Creates a {@code .proto} source file with the given name and content.
         *
         * @param fileName the name of the file
         * @param lines    the content of the file
         */
        public Builder createProto(String fileName, Iterable<String> lines) {
            String path = BASE_PROTO_LOCATION + fileName;
            return createFile(path, lines);
        }

        /**
         * Creates a file in the project directory under the given path and with the given content.
         *
         * @param path  the path to the file relative to the project dir
         * @param lines the content of the file
         */
        public Builder createFile(String path, Iterable<String> lines) {
            Path sourcePath = folder.toPath()
                                    .resolve(path);
            try {
                Files.createDirectories(sourcePath.getParent());
                Files.write(sourcePath, lines, Charsets.UTF_8);
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
                checkNotNull(name, "Project name");
                checkNotNull(folder, "Project folder");
                return new GradleProject(this);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }
    }

    private static IllegalStateException illegalStateWithCauseOf(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        throw new IllegalStateException(rootCause);
    }
}
