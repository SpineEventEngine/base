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

package io.spine.generate.dart;

import com.google.common.collect.ImmutableMap;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;

import java.io.File;

import static io.spine.tools.gradle.ProtoDartTaskName.copyGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.copyTestGeneratedDart;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.ProtocPluginName.dart;
import static io.spine.tools.gradle.SourceScope.main;
import static io.spine.tools.gradle.SourceScope.test;
import static org.gradle.api.Task.TASK_TYPE;

/**
 * A Gradle plugin which configures Protobuf Dart code generation.
 *
 * <p>Generates mapping between Protobuf type URLs and Dart types and reflective descriptors
 * (a.k.a. {@code BuilderInfo}s).
 *
 * @see DartProtocConfigurationPlugin
 */
public final class ProtoDartPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        Extension extension = new Extension(project);
        extension.register();

        Plugin<Project> protocConfig = new DartProtocConfigurationPlugin();
        protocConfig.apply(project);

        createMainCopyTask(project, extension);
        createTestCopyTask(project, extension);
    }

    private static void createMainCopyTask(Project project, Extension extension) {
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class),
                                        copyGeneratedDart.name());
        task.from(extension.getGeneratedBaseDir()
                           .dir(main.name() + File.separator + dart.name()));
        task.into(extension.getLibDir());
        task.dependsOn(generateProto.name());
    }

    private static void createTestCopyTask(Project project, Extension extension) {
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class),
                                        copyTestGeneratedDart.name());
        task.from(extension.getGeneratedBaseDir()
                           .dir(test.name() + File.separator + dart.name()));
        task.into(extension.getTestDir());
        task.dependsOn(generateTestProto.name());
    }
}
