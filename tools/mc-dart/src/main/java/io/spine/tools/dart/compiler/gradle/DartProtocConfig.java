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

package io.spine.tools.dart.compiler.gradle;

import com.google.protobuf.gradle.ExecutableLocator;
import io.spine.tools.dart.compiler.CachedDartProtocPlugin;
import io.spine.tools.gradle.ProtocConfigurationPlugin;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;

import static io.spine.tools.gradle.ProtocPluginName.dart;

/**
 * A Gradle plugin that performs additional {@code protoc} configurations relevant for Dart
 * projects.
 */
final class DartProtocConfig extends ProtocConfigurationPlugin {

    @Override
    protected Path generatedFilesBaseDir(Project project) {
        return Extension.findIn(project).generatedDirPath();
    }

    @Override
    protected File getMainDescriptorSet(Project project) {
        return Extension.findIn(project).mainDescriptorSetFile();
    }

    @Override
    protected File getTestDescriptorSet(Project project) {
        return Extension.findIn(project).testDescriptorSetFile();
    }

    @Override
    protected void configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins,
                                          Project project) {
        Path executable = CachedDartProtocPlugin.locate();
        plugins.create(dart.name(), locator -> locator.setPath(executable.toString()));
    }
}
