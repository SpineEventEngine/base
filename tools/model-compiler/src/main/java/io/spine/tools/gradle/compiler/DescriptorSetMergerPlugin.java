/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableSet;
import io.spine.code.proto.FileDescriptors;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.Collection;

import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.tools.gradle.ConfigurationName.RUNTIME;
import static io.spine.tools.gradle.ConfigurationName.TEST_RUNTIME;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static java.util.stream.Collectors.toList;

/**
 * A Gradle plugin which merges the descriptor file with all the descriptor files from
 * the project runtime classpath.
 *
 * @author Dmytro Dashenkov
 */
public class DescriptorSetMergerPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        createMainTask(project);
        createTestTask(project);
    }

    private void createMainTask(Project project) {
        logDependingTask(MERGE_DESCRIPTOR_SET,
                         GENERATE_VALIDATING_BUILDERS,
                         GENERATE_PROTO);
        newTask(MERGE_DESCRIPTOR_SET,
                createMergingAction(configuration(project, RUNTIME),
                                    getMainDescriptorSetPath(project)))
                .insertAfterTask(GENERATE_PROTO)
                .insertBeforeTask(GENERATE_VALIDATING_BUILDERS)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        logDependingTask(MERGE_TEST_DESCRIPTOR_SET,
                         GENERATE_TEST_VALIDATING_BUILDERS,
                         GENERATE_TEST_PROTO);
        newTask(MERGE_TEST_DESCRIPTOR_SET,
                createMergingAction(configuration(project, TEST_RUNTIME),
                                    getTestDescriptorSetPath(project)))
                .insertAfterTask(GENERATE_TEST_PROTO)
                .insertBeforeTask(GENERATE_TEST_VALIDATING_BUILDERS)
                .applyNowTo(project);
    }

    private Action<Task> createMergingAction(Configuration configuration,
                                             String descriptorSetPath) {
        return task -> {
            File descriptorSet = new File(descriptorSetPath);
            Collection<File> descriptors =
                    configuration.getFiles()
                                 .stream()
                                 .map(task.getProject()::zipTree)
                                 .flatMap(fileTree -> fileTree.getFiles().stream())
                                 .filter(file -> KNOWN_TYPES.equals(file.getName()))
                                 .peek(file -> log().debug("Merging descriptors from {}", file))
                                 .collect(toList());
            ImmutableSet.Builder<File> files = ImmutableSet
                    .<File>builder()
                    .addAll(descriptors);
            if (descriptorSet.exists()) {
                files.add(descriptorSet);
            }
            FileDescriptors.merge(files.build())
                           .writeTo(descriptorSet);
        };
    }

    private static Configuration configuration(Project project, ConfigurationName name) {
        return project.getConfigurations()
                      .getByName(name.getValue());
    }
}
