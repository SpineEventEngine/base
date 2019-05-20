/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.js.gradle;

import com.google.protobuf.gradle.GenerateProtoTask;
import io.spine.code.fs.js.DefaultJsProject;
import io.spine.tools.gradle.ProtocConfigurationPlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;

/**
 * A Gradle plugin that performs additional {@code protoc} configurations relevant for JavaScript
 * projects.
 */
public final class ProtocConfigurationPluginForJs extends ProtocConfigurationPlugin {

    @Override
    protected void configureDescriptorSetGeneration(GenerateProtoTask task, File descriptor) {
        /* Nop, the parent configuration is sufficient. */
    }

    @Override
    protected void configureTaskPlugins(GenerateProtoTask protocTask, Task dependency) {
        /* Nop, the parent configuration is sufficient. */
    }

    @Override
    protected File getTestDescriptorSet(Project project) {
        return Extension.getTestDescriptorSet(project);
    }

    @Override
    protected Path generatedFilesBaseDir(Project project) {
        DefaultJsProject jsProject = DefaultJsProject.at(project.getProjectDir());
        DefaultJsProject.GeneratedProtoRoot generatedProtoRoot = jsProject.proto();
        return generatedProtoRoot.getPath();
    }

    @Override
    protected File getMainDescriptorSet(Project project) {
        return Extension.getMainDescriptorSet(project);
    }
}
