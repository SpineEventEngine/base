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

import com.google.common.base.Charsets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.TaskName;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;

/**
 * Allows to configure a Gradle project for testing needs.
 *
 * <p>The project operates in the given test project directory and allows to execute Gradle tasks.
 */
public final class GradleProject {

    private static final String BASE_PROTO_LOCATION = "src/main/proto/";
    private static final String BASE_JAVA_LOCATION = "src/main/java/";
    private static final String JAVA_PLUGIN_NAME = "java";

    private final String name;
    private final GradleRunner gradleRunner;
    private final boolean debug;

    /**
     * Creates new builder for the project.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Obtains the name of the Java Gradle plugin.
     */
    public static String javaPlugin() {
        return JAVA_PLUGIN_NAME;
    }

    private GradleProject(Builder builder) throws IOException {
        this.name = builder.name;
        this.debug = builder.debug;
        this.gradleRunner = GradleRunner.create()
                                        .withProjectDir(builder.folder)
                                        .withDebug(builder.debug);
        if (builder.addPluginUnderTestClasspath) {
            gradleRunner.withPluginClasspath();
        }
        writeGradleScripts();
        writeProtoFiles(builder.protoFileNames);
        writeJavaFiles(builder.javaFileNames);
    }

    private void writeGradleScripts() throws IOException {
        BuildGradle buildGradle = new BuildGradle(testProjectRoot());
        buildGradle.createFile();

        Path projectRoot = ProjectRoot.instance()
                                      .toPath();
        TestEnvGradle testEnvGradle = new TestEnvGradle(projectRoot, testProjectRoot());
        testEnvGradle.createFile();
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
        String[] args = TaskArguments.mode(debug)
                                     .of(taskName);
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
        Path resultingPath = testProjectRoot().resolve(filePath);
        String fullyQualifiedPath = name + '/' + filePath;
        InputStream fileContent = getClass().getClassLoader()
                                            .getResourceAsStream(fullyQualifiedPath);
        Files.createDirectories(resultingPath.getParent());
        checkNotNull(fileContent);
        Files.copy(fileContent, resultingPath);
    }

    private Path testProjectRoot() {
        return gradleRunner.getProjectDir()
                           .toPath();
    }

    /**
     * A builder for new {@code GradleProject}.
     */
    public static class Builder {

        private String name;
        private File folder;

        /**
         * Determines whether the code can be debugged.
         *
         * <p>Affects the code executed during a {@linkplain #executeTask Gradle task}.
         *
         * <p>NOTE: when the value is {@code true}, all code is executed in a single JVM.
         * This leads to a high consumption of a memory.
         */
        private boolean debug;
        private final List<String> protoFileNames = newLinkedList();
        private final List<String> javaFileNames = newLinkedList();

        /**
         * Determines whether the plugin under test classpath is defined and should be added to
         * the Gradle execution classpath.
         *
         * <p>The {@code plugin-under-test-metadata.properties} resource must be present in
         * the current classpath. The file defines the {@code implementation-classpath} property,
         * which contains the classpath to be added to the Gradle run.
         *
         * <p>Whenever the added classpath contains a Gradle plugin, the executed Gradle scripts may
         * apply it via the {@code plugins} block.
         *
         * @see GradleRunner#withPluginClasspath
         */
        private boolean addPluginUnderTestClasspath;

        /** Prevents direct instantiation of this class. */
        private Builder() {
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
            checkNotNull(protoFileName);
            checkArgument(!protoFileName.isEmpty());
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
         * Configures this runner to include the plugin under development into the classpath.
         *
         * @see GradleRunner#withPluginClasspath()
         */
        @SuppressWarnings("unused") // Used in downstream repositories.
        public Builder withPluginClasspath() {
            this.addPluginUnderTestClasspath = true;
            return this;
        }

        /**
         * Creates a {@code .proto} source file with the given name and content.
         *
         * @param fileName
         *         the name of the file
         * @param lines
         *         the content of the file
         */
        public Builder createProto(String fileName, Iterable<String> lines) {
            checkNotNull(fileName);
            checkNotNull(lines);

            String path = BASE_PROTO_LOCATION + fileName;
            return createFile(path, lines);
        }

        /**
         * Creates a file in the project directory under the given path and with the given content.
         *
         * @param path
         *         the path to the file relative to the project dir
         * @param lines
         *         the content of the file
         */
        public Builder createFile(String path, Iterable<String> lines) {
            checkNotNull(path);
            checkNotNull(lines);

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
            checkNotNull(protoFileNames);

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
                GradleProject result = new GradleProject(this);
                return result;
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private static IllegalStateException illegalStateWithCauseOf(Throwable throwable) {
            Throwable rootCause = getRootCause(throwable);
            throw new IllegalStateException(rootCause);
        }
    }
}
