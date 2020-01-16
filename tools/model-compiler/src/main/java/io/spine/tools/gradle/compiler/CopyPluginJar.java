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

package io.spine.tools.gradle.compiler;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.compiler.JavaProtocConfigurationPlugin.rootSpineDirectory;
import static io.spine.tools.gradle.compiler.JavaProtocConfigurationPlugin.spineDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Downloads and lays out the {@code protoc} plugin executable JAR.
 */
public final class CopyPluginJar implements Action<Task> {

    private final Project project;
    private final Dependency protocPluginDependency;
    private final Configuration fetch;

    CopyPluginJar(Project project, Dependency protocPlugin, Configuration fetch) {
        this.project = checkNotNull(project);
        this.protocPluginDependency = checkNotNull(protocPlugin);
        this.fetch = checkNotNull(fetch);
    }

    @Override
    public void execute(Task task) {
        File executableJar = fetch.fileCollection(protocPluginDependency)
                                  .getSingleFile();
        File spineDir = spineDirectory(project);
        File rootSpineDir = rootSpineDirectory(project);
        copy(executableJar, spineDir);
        copy(executableJar, rootSpineDir);
    }

    private static void copy(File file, File destinationDir) {
        try {
            destinationDir.mkdirs();
            Path destination = destinationDir.toPath()
                                             .resolve(file.getName());
            Files.copy(file.toPath(), destination, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new GradleException("Failed to copy Spine Protoc executable JAR.", e);
        }
    }
}
