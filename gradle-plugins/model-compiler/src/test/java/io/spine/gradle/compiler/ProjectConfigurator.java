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

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Abstract base for test project configurators.
 *
 * <p>{@code ProjectConfigurator} sets up {@linkplain TemporaryFolder test project directory}
 * and allows to receive {@link ProjectConnection} for it.
 *
 * @author Dmytro Grankin
 */
public abstract class ProjectConfigurator {

    private static final String BUILD_GRADLE_NAME = "build.gradle";
    private static final String EXT_GRADLE_NAME = "ext.gradle";
    protected static final String BASE_PROTO_LOCATION = "src/main/proto/";

    private final String projectName;
    private final TemporaryFolder projectDirectory;

    protected ProjectConfigurator(String projectName, TemporaryFolder projectDirectory) {
        this.projectDirectory = projectDirectory;
        this.projectName = projectName;
    }

    public abstract ProjectConnection configure() throws IOException;

    protected ProjectConnection createProjectConnection() {
        final GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(getProjectDirectory());
        return connector.connect();
    }

    protected void writeBuildGradle() throws IOException {
        final Path resultingPath = getProjectDirectory().toPath()
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
        final Path extGradleResultingPath = getProjectDirectory().toPath()
                                                                 .resolve(EXT_GRADLE_NAME);
        Files.copy(extGradleSourcePath, extGradleResultingPath);
    }

    protected void writeProto(String protoFile) throws IOException {
        final String protoFilePath = BASE_PROTO_LOCATION + protoFile;

        final Path resultingPath = getProjectDirectory().toPath()
                                                        .resolve(protoFilePath);
        final String fqnPath = projectName + '/' + protoFilePath;
        final InputStream fileContent = getClass().getClassLoader()
                                                  .getResourceAsStream(fqnPath);
        Files.createDirectories(resultingPath.getParent());
        Files.copy(fileContent, resultingPath);
    }

    protected File getProjectDirectory() {
        return projectDirectory.getRoot();
    }

    public static ResultHandler<Void> newEmptyResultHandler(CountDownLatch countDownLatch) {
        return new EmptyResultHandler(countDownLatch);
    }

    /**
     * Empty {@link ResultHandler} for tests, that just require successful
     * {@linkplain org.gradle.tooling.BuildLauncher#run(ResultHandler) Gradle build}.
     */
    private static class EmptyResultHandler implements ResultHandler<Void> {

        private final CountDownLatch countDownLatch;

        /**
         * Creates a new instance.
         *
         * @param countDownLatch the latch with the count value equal to {@code 1}
         */
        private EmptyResultHandler(CountDownLatch countDownLatch) {
            checkArgument(countDownLatch.getCount() == 1);
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onComplete(Void aVoid) {
            countDownLatch.countDown();
        }

        @Override
        public void onFailure(GradleConnectionException e) {
            throw e;
        }
    }
}
