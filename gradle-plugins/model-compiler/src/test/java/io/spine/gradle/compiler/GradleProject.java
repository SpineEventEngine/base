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

package io.spine.gradle.compiler;

import io.spine.gradle.TaskName;
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
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.jboss.forge.roaster.model.util.Strings.isBlank;

/**
 * {@code GradleProject} for the test needs.
 *
 * <p>{@code GradleProject} sets up {@linkplain TemporaryFolder test project directory}
 * and allows execute Gradle tasks.
 *
 * @author Dmytro Grankin
 */
public class GradleProject {

    private static final String BUILD_GRADLE_NAME = "build.gradle";
    private static final String EXT_GRADLE_NAME = "ext.gradle";
    protected static final String BASE_PROTO_LOCATION = "src/main/proto/";

    private final String name;
    private final GradleRunner gradleRunner;

    private GradleProject(Builder builder) throws IOException {
        this.name = builder.name;
        this.gradleRunner = GradleRunner.create()
                                        .withProjectDir(builder.folder.getRoot());
        writeBuildGradle();
        for (String protoFile : builder.protoFileNames) {
            writeProto(protoFile);
        }
    }

    public BuildResult executeTask(TaskName taskName) {
        return gradleRunner.withArguments(taskName.getValue())
                           .build();
    }

    private void writeProto(String protoFile) throws IOException {
        final String protoFilePath = BASE_PROTO_LOCATION + protoFile;
        final Path resultingPath = gradleRunner.getProjectDir()
                                               .toPath()
                                               .resolve(protoFilePath);
        final String fqnProtoPath = name + '/' + protoFilePath;
        final InputStream fileContent = getClass().getClassLoader()
                                                  .getResourceAsStream(fqnProtoPath);
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
                                                          .getParent()
                                                          .resolve(EXT_GRADLE_NAME);
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
        private final List<String> protoFileNames = newLinkedList();

        private Builder() {
            // Prevent instantiation of this class from outside.
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
            checkArgument(!isBlank(protoFileName));
            protoFileNames.add(protoFileName);
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
                checkArgument(!isBlank(protoFileName));
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
