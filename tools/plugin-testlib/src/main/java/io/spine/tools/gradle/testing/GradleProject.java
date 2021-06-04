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

package io.spine.tools.gradle.testing;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.TaskName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.io.Files2.copyDir;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

/**
 * Allows to configure a Gradle project for testing needs.
 *
 * <p>The project operates in the given test project directory and allows to execute Gradle tasks.
 */
public final class GradleProject {

    private static final String mainProtoDir = "src/main/proto/";
    private static final String mainJavaDir = "src/main/java/";
    private static final String buildSrcDir = "buildSrc";

    private final String name;
    private final GradleRunner gradleRunner;
    private final boolean debug;
    private final ImmutableMap<String, String> gradleProperties;

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
        return "java";
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
        if (builder.environment != null) {
            gradleRunner.withEnvironment(builder.environment);
        }
        this.gradleProperties = ImmutableMap.copyOf(builder.gradleProperties);
        writeGradleScripts();
        writeBuildSrc();
        writeProtoFiles(builder.protoFileNames);
        writeJavaFiles(builder.javaFileNames);
    }

    private void writeGradleScripts() throws IOException {
        BuildGradle buildGradle = new BuildGradle(projectRoot());
        buildGradle.createFile();

        Path projectRoot = ProjectRoot.instance()
                                      .toPath();
        TestEnvGradle testEnvGradle = new TestEnvGradle(projectRoot, projectRoot());
        testEnvGradle.createFile();
    }

    private void writeBuildSrc() throws IOException {
        Path projectRoot = ProjectRoot.instance()
                                      .toPath();
        Path buildSrc = projectRoot.resolve(buildSrcDir);
        Path target = projectRoot();
        copyDir(buildSrc, target);
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
        String[] args = TaskArguments.mode(debug).of(taskName, gradleProperties);
        return gradleRunner.withArguments(args);
    }

    private void writeProto(String fileName) throws IOException {
        writeFile(fileName, mainProtoDir);
    }

    private void writeJava(String fileName) throws IOException {
        writeFile(fileName, mainJavaDir);
    }

    private void writeFile(String fileName, String dir) throws IOException {
        String filePath = dir + fileName;
        String resourcePath = name + '/' + filePath;
        InputStream fileContent = openResource(resourcePath);
        Path fileSystemPath = projectRoot().resolve(filePath);
        createDirectories(fileSystemPath.getParent());
        copy(fileContent, fileSystemPath);
    }

    private InputStream openResource(String fullPath) {
        InputStream stream = getClass().getClassLoader()
                                       .getResourceAsStream(fullPath);
        checkState(stream != null, "Unable to locate resource: `%s`.", fullPath);
        return stream;
    }

    private Path projectRoot() {
        return gradleRunner.getProjectDir()
                           .toPath();
    }

    /**
     * A builder for new {@code GradleProject}.
     */
    public static class Builder {

        private final List<String> protoFileNames = new ArrayList<>();
        private final List<String> javaFileNames = new ArrayList<>();
        private final Map<String, String> gradleProperties = new HashMap<>();

        private @Nullable ImmutableMap<String, String> environment;
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

        /**
         * Sets the name of the sub-directory under {@code resources} which contains files
         * for the project to be created.
         */
        public Builder setProjectName(String name) {
            this.name = checkNotNull(name);
            return this;
        }

        /**
         * Sets the name of the directory on the file system under which
         * the project will be created.
         */
        public Builder setProjectFolder(File folder) {
            this.folder = checkNotNull(folder);
            return this;
        }

        /**
         * Adds a {@code .proto} file to the project to be created.
         *
         * @param protoFileName
         *         a name of the proto file relative to {@code src/main/proto} sub-directory
         *         under the one specified in {@link #setProjectName(String)}
         */
        public Builder addProtoFile(String protoFileName) {
            checkNotNull(protoFileName);
            checkArgument(!protoFileName.isEmpty());
            protoFileNames.add(protoFileName);
            return this;
        }

        /**
         * Adds a collection of {@code .proto} files to the project to be created.
         *
         * @see #addProtoFile(String)
         */
        public Builder addProtoFiles(Collection<String> protoFileNames) {
            checkNotNull(protoFileNames);
            protoFileNames.forEach(this::addProtoFile);
            return this;
        }

        /**
         * Adds multiple {@code .proto} files to the project to be created.
         *
         * @see #addProtoFile(String)
         */
        public Builder addProtoFiles(String... fileNames) {
            checkNotNull(fileNames);
            return addProtoFiles(ImmutableList.copyOf(fileNames));
        }

        /**
         * Adds {@code .java} files to the project to be created.
         *
         * @param fileNames
         *         names of the Java files relative to {@code src/main/java} sub-directory
         *         under the one specified in {@link #setProjectName(String)}
         */
        public Builder addJavaFiles(String... fileNames) {
            checkNotNull(fileNames);
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
         * Adds a Gradle property to be passed to the Gradle build.
         *
         * @param name
         *         name of the property
         * @param value
         *         value of the property
         */
        public Builder withProperty(String name, String value) {
            checkNotNull(name);
            checkNotNull(value);
            this.gradleProperties.put(name, value);
            return this;
        }

        /**
         * Configures the environment variables available to the build.
         *
         * <p>If not set, the variables are inherited.
         */
        public Builder withEnvironment(ImmutableMap<String, String> environment) {
            checkNotNull(environment);
            this.environment = environment;
            return this;
        }

        /**
         * Creates a {@code .proto} source file with the given name and content.
         *
         * @param fileName
         *         the name of the file relative to {@code src/main/proto} directory
         * @param lines
         *         the content of the file
         */
        public Builder createProto(String fileName, Iterable<String> lines) {
            checkNotNull(fileName);
            checkNotNull(lines);

            String path = mainProtoDir + fileName;
            return createFile(path, lines);
        }

        /**
         * Creates a file in the project directory under the given path and with the given content.
         *
         * @param path
         *         the path to the file relative to the project root directory
         * @param lines
         *         the content of the file
         */
        public Builder createFile(String path, Iterable<String> lines) {
            checkNotNull(path);
            checkNotNull(lines);

            Path sourcePath = folder.toPath()
                                    .resolve(path);
            try {
                createDirectories(sourcePath.getParent());
                write(sourcePath, lines, Charsets.UTF_8);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
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
    }
}
