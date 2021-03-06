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

package io.spine.tools.gradle;

import io.spine.tools.fs.DefaultPaths;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;

import static io.spine.code.proto.FileDescriptors.DESC_EXTENSION;

/**
 * An abstract base for creating Gradle extensions.
 */
public abstract class GradleExtension {

    protected final File defaultMainDescriptor(Project project) {
        Artifact artifact = newBuilderFrom(project).build();
        String fileName = descriptorSetFileOf(artifact);
        Path mainDescriptor = defaultPaths(project)
                .buildRoot()
                .descriptors()
                .mainDescriptors()
                .resolve(fileName);
        return mainDescriptor.toFile();
    }

    protected final File defaultTestDescriptor(Project project) {
        Artifact artifact = newBuilderFrom(project)
                .useTestClassifier()
                .build();
        String fileName = descriptorSetFileOf(artifact);
        Path testDescriptor = defaultPaths(project)
                .buildRoot()
                .descriptors()
                .testDescriptors()
                .resolve(fileName);
        return testDescriptor.toFile();
    }

    /**
     * Obtains a name of a descriptor set file for the passed artifact.
     */
    private static String descriptorSetFileOf(Artifact artifact) {
        String artifactId = artifact.fileSafeId();
        return artifactId + DESC_EXTENSION;
    }

    private static Artifact.Builder newBuilderFrom(Project project) {
        return Artifact.newBuilder()
                .setGroup(project.getGroup().toString())
                .setName(project.getName())
                .setVersion(project.getVersion().toString());
    }

    protected abstract DefaultPaths defaultPaths(Project project);
}
