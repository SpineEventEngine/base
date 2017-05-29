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

package io.spine.tools.gcs;

import com.google.api.client.http.HttpTransport;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.Before;
import org.junit.Test;
import io.spine.gradle.TaskName;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static io.spine.gradle.TaskName.CLEAN_GCS;

/**
 * @author Dmytro Grankin
 */
public class GcsPluginShould {

    private TaskContainer tasks;

    @Before
    public void setUp() {
        final Project project = Given.newProject();
        project.getPluginManager()
               .apply(Given.GCS_PLUGIN_ID);
        tasks = project.getTasks();
    }

    @Test
    public void apply_to_project() {
        final Project project = Given.newProject();
        project.getPluginManager()
               .apply(Given.GCS_PLUGIN_ID);
    }

    @Test
    public void add_task_cleanGCS() {
        assertNotNull(task(CLEAN_GCS));
    }

    @Test
    public void limit_excessive_logging_from_HttpTransport() {
        final Project project = Given.newProject();
        project.getPluginManager()
               .apply(Given.GCS_PLUGIN_ID);

        final String name = HttpTransport.class.getName();
        final Logger log = Logger.getLogger(name);
        assertEquals(Level.WARNING, log.getLevel());
    }

    private Task task(TaskName taskName) {
        return tasks.getByName(taskName.getValue());
    }
}
