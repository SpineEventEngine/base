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

package org.spine3.tools.gcs;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.Before;
import org.junit.Test;
import org.spine3.gradle.TaskName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spine3.gradle.TaskDependencies.dependsOn;
import static org.spine3.gradle.TaskName.BUILD;
import static org.spine3.gradle.TaskName.CLEAN_GCS;
import static org.spine3.tools.gcs.Given.GCS_PLUGIN_ID;
import static org.spine3.tools.gcs.Given.newProject;

/**
 * @author Dmytro Grankin
 */
public class GcsPluginShould {

    private TaskContainer tasks;

    @Before
    public void setUp() {
        final Project project = newProject();
        project.getPluginManager()
               .apply(GCS_PLUGIN_ID);
        tasks = project.getTasks();
    }

    @Test
    public void apply_to_project() {
        final Project project = newProject();
        project.getPluginManager()
               .apply(GCS_PLUGIN_ID);
    }

    @Test
    public void add_task_cleanGCS() {
        assertNotNull(task(CLEAN_GCS));
        assertTrue(dependsOn(task(BUILD), task(CLEAN_GCS)));
    }

    @Test
    public void append_slash_to_folder_name_without_trailing_slash() {
        final Extension extension = new Extension();
        final String folderName = "just-folder-name";
        extension.setCleaningFolder(folderName);
        assertEquals(folderName + '/', extension.getCleaningFolder());
    }

    @Test
    public void not_append_slash_to_folder_name_with_trailing_slash() {
        final Extension extension = new Extension();
        final String folderName = "slash-in-the-end/";
        extension.setCleaningFolder(folderName);
        assertEquals(folderName, extension.getCleaningFolder());
    }

    private Task task(TaskName taskName) {
        return tasks.getByName(taskName.getValue());
    }
}
