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

import com.google.common.collect.ImmutableMap;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

import java.util.Collection;

import static io.spine.tools.gradle.TaskName.COPY_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.COPY_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.tools.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestTargetGenResourcesDir;
import static org.gradle.api.Task.TASK_TYPE;

/**
 * A {@link Copy} task which copies the descriptor set file to the project runtime classpath.
 *
 * @author Dmytro Dashenkov
 */
final class CopyDescriptorFile {

    /**
     * Prevents the utility class instantiation.
     */
    private CopyDescriptorFile() {
    }

    /**
     * Adds tasks {@code copyDescriptorSet} and {@code copyTestDescriptorSet} of type {@link Copy}
     * to the given project.
     *
     * <p>The tasks copy descriptor files for the main and test scopes respectively.
     *
     * <p>The tasks are executed before the {@code processResources} task (and
     * {@code processTestResources} in case of the test scope task).
     *
     * <p>The copied file is put into the {@link Extension#getMainTargetGenResourcesDir(Project)
     * target resources directories} of the respective scope.
     *
     * @param project the project to add the tasks to
     */
    static void addTo(Project project) {
        doAdd(project,
              COPY_DESCRIPTOR_SET,
              getMainDescriptorSetPath(project),
              getMainTargetGenResourcesDir(project),
              PROCESS_RESOURCES);
        doAdd(project,
              COPY_TEST_DESCRIPTOR_SET,
              getTestDescriptorSetPath(project),
              getTestTargetGenResourcesDir(project),
              PROCESS_TEST_RESOURCES);
    }

    private static void doAdd(Project project,
                              TaskName taskName,
                              String descriptorSet,
                              String targetDir,
                              TaskName executeBefore) {
        final Copy task = (Copy) project.task(
                ImmutableMap.<String, Object>of(TASK_TYPE, Copy.class),
                taskName.getValue());
        task.from(descriptorSet);
        task.into(targetDir);
        final Collection<Task> dependantTasks = project.getTasksByName(
                executeBefore.getValue(), false
        );
        for (Task dependantTask : dependantTasks) {
            dependantTask.dependsOn(task);
        }
    }
}
