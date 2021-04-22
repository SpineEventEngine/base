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

package io.spine.tools.compiler.gradle;

import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.gradle.TaskName;
import io.spine.tools.type.FileDescriptorSuperset;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import java.io.File;

import static io.spine.tools.gradle.ConfigurationName.runtimeClasspath;
import static io.spine.tools.gradle.ConfigurationName.testRuntimeClasspath;
import static io.spine.tools.gradle.JavaTaskName.processResources;
import static io.spine.tools.gradle.JavaTaskName.processTestResources;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeTestDescriptorSet;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;

/**
 * A Gradle plugin which merges the descriptor file with all the descriptor files from
 * the project runtime classpath.
 *
 * <p>The merge result is used to {@linkplain
 * io.spine.tools.type.MoreKnownTypes#extendWith(java.io.File) extend the known type registry}.
 */
public class DescriptorSetMergerPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        createTask(project, false);
        createTask(project, true);
    }

    private void createTask(Project project, boolean tests) {
        Configuration configuration = configuration(project, configurationName(tests));
        Buildable dependencies = configuration.getAllDependencies();
        GradleTask task = newTask(taskName(tests), createMergingAction(tests))
                .insertAfterTask(generateProtoTaskName(tests))
                .insertBeforeTask(processResourcesTaskName(tests))
                .applyNowTo(project);
        task.getTask().dependsOn(dependencies);
    }

    private static Action<Task> createMergingAction(boolean tests) {
        return task -> {
            Project project = task.getProject();
            Configuration configuration = configuration(project, configurationName(tests));
            File descriptorSet = descriptorSet(project, tests);
            FileDescriptorSuperset superset = new FileDescriptorSuperset();
            configuration.forEach(superset::addFromDependency);
            if (descriptorSet.exists()) {
                superset.addFromDependency(descriptorSet);
            }
            superset.merge()
                    .loadIntoKnownTypes();
        };
    }

    private static Configuration configuration(Project project, ConfigurationName name) {
        return project.getConfigurations()
                      .getByName(name.value());
    }

    private static ConfigurationName configurationName(boolean tests) {
        return tests
               ? testRuntimeClasspath
               : runtimeClasspath;
    }

    private static TaskName taskName(boolean tests) {
        return tests
               ? mergeTestDescriptorSet
               : mergeDescriptorSet;
    }

    private static TaskName generateProtoTaskName(boolean tests) {
        return tests
               ? generateTestProto
               : generateProto;
    }

    private static TaskName processResourcesTaskName(boolean tests) {
        return tests
               ? processTestResources
               : processResources;
    }

    private static File descriptorSet(Project project, boolean tests) {
        Extension extension = Extension.of(project);
        File descriptor = tests
                          ? extension.testDescriptorSetFile()
                          : extension.mainDescriptorSetFile();
        return descriptor;
    }
}
