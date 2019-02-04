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

package io.spine.tools.gradle.compiler;

import io.spine.tools.compiler.descriptor.FileDescriptorSuperset;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import java.io.File;

import static io.spine.tools.gradle.ConfigurationName.RUNTIME_CLASSPATH;
import static io.spine.tools.gradle.ConfigurationName.TEST_RUNTIME_CLASSPATH;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;

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
        createMainTask(project);
        createTestTask(project);
    }

    private void createMainTask(Project project) {
        newTask(MERGE_DESCRIPTOR_SET, createMergingAction(false))
                .insertAfterTask(GENERATE_PROTO)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        newTask(MERGE_TEST_DESCRIPTOR_SET, createMergingAction(true))
                .insertAfterTask(GENERATE_TEST_PROTO)
                .applyNowTo(project);
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
            superset.loadIntoKnownTypes();
        };
    }

    private static Configuration configuration(Project project, ConfigurationName name) {
        return project.getConfigurations()
                      .getByName(name.getValue());
    }

    private static ConfigurationName configurationName(boolean tests) {
        return tests
               ? TEST_RUNTIME_CLASSPATH
               : RUNTIME_CLASSPATH;
    }

    private static File descriptorSet(Project project, boolean tests) {
        String path = tests
                      ? getTestDescriptorSetPath(project)
                      : getMainDescriptorSetPath(project);
        return new File(path);
    }
}
