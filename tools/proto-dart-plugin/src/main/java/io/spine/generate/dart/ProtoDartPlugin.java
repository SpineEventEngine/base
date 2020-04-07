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

package io.spine.generate.dart;

import com.google.common.collect.ImmutableMap;
import io.spine.tools.gradle.ProtoDartTaskName;
import io.spine.tools.gradle.SourceScope;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Copy;

import java.io.File;

import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.gradle.ProtoDartTaskName.copyGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.copyTestGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.resolveImports;
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
        createResolveImportTask(project, extension);
    }

    private static void createMainCopyTask(Project project, Extension extension) {
        createCopyTask(project, extension, main);
    }

    private static void createTestCopyTask(Project project, Extension extension) {
        createCopyTask(project, extension, test);
    }

    private static void createCopyTask(Project project, Extension extension, SourceScope scope) {
        ProtoDartTaskName taskName;
        DirectoryProperty targetDir;
        TaskName runAfter;
        if (scope == main) {
            taskName = copyGeneratedDart;
            targetDir = extension.getLibDir();
            runAfter = generateProto;
        } else {
            taskName = copyTestGeneratedDart;
            targetDir = extension.getTestDir();
            runAfter = generateTestProto;
        }
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class), taskName.name());
        task.from(extension.getGeneratedBaseDir()
                           .dir(scope.name() + File.separator + dart.name()));
        task.into(targetDir);
        task.dependsOn(runAfter.name());
        project.getTasks()
               .getByName(assemble.name())
               .dependsOn(taskName.name());
    }

    private void createResolveImportTask(Project project, Extension extension) {
        newTask(resolveImports, task -> {

        })
                .insertAfterTask(copyGeneratedDart)
                .insertBeforeTask(assemble)
                .applyNowTo(project);
    }
}
