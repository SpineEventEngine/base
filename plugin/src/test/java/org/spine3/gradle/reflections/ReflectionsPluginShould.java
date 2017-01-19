/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package org.spine3.gradle.reflections;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spine3.gradle.TaskDependencies.dependsOn;
import static org.spine3.gradle.TaskName.BUILD;
import static org.spine3.gradle.TaskName.CLASSES;
import static org.spine3.gradle.TaskName.SCAN_CLASS_PATH;
import static org.spine3.gradle.reflections.Given.REFLECTIONS_PLUGIN_ID;
import static org.spine3.gradle.reflections.Given.newProject;

/**
 * @author Alex Tymchenko
 */
public class ReflectionsPluginShould {

    @Test
    public void apply_to_project() {
        final Project project = newProject();
        project.getPluginManager()
               .apply(REFLECTIONS_PLUGIN_ID);
    }

    @Test
    public void add_task_scanClassPath() {
        final Project project = newProject();
        project.getPluginManager()
               .apply(REFLECTIONS_PLUGIN_ID);

        final TaskContainer tasks = project.getTasks();
        final Task scanClassPathTask = tasks.getByName(SCAN_CLASS_PATH.getValue());
        final Task buildTask = tasks.getByName(BUILD.getValue());

        assertNotNull(scanClassPathTask);
        assertTrue(dependsOn(scanClassPathTask, CLASSES));
        assertTrue(dependsOn(buildTask, scanClassPathTask));
    }
}
