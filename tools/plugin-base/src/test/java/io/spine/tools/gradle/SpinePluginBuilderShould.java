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

package io.spine.tools.gradle;

import io.spine.tools.gradle.given.GradleProject;
import io.spine.tools.gradle.given.GradleProject.NoOp;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static io.spine.tools.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.tools.gradle.TaskName.CLASSES;
import static io.spine.tools.gradle.TaskName.CLEAN;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.PRE_CLEAN;
import static io.spine.tools.gradle.TaskName.VERIFY_MODEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
public class SpinePluginBuilderShould {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder()
                                .build();
        project.getPluginManager()
               .apply(GradleProject.JAVA_PLUGIN_ID);
    }

    @Test
    public void create_task_dependant_on_all_tasks_of_given_name() {
        final Project subProject = ProjectBuilder.builder()
                                                 .withParent(project)
                                                 .build();
        subProject.getPluginManager()
                  .apply(GradleProject.JAVA_PLUGIN_ID);
        final SpinePlugin plugin = TestPlugin.INSTANCE;
        final GradleTask task = plugin.newTask(ANNOTATE_PROTO, NoOp.action())
                                      .insertAfterAllTasks(COMPILE_JAVA)
                                      .applyNowTo(subProject);
        final TaskContainer subProjectTasks = subProject.getTasks();
        final Task newTask = subProjectTasks.findByName(task.getName()
                                                            .getValue());
        assertNotNull(newTask);
        final Collection<?> dependencies = newTask.getDependsOn();
        assertTrue(dependencies.contains(subProjectTasks.findByName(COMPILE_JAVA.getValue())));
        assertTrue(dependencies.contains(project.getTasks()
                                                .findByName(COMPILE_JAVA.getValue())));
    }

    @Test
    public void create_task_and_insert_before_other() {
        final SpinePlugin plugin = TestPlugin.INSTANCE;
        plugin.newTask(VERIFY_MODEL, NoOp.action())
              .insertBeforeTask(CLASSES)
              .applyNowTo(project);
        final TaskContainer tasks = project.getTasks();
        final Task classes = tasks.findByName(CLASSES.getValue());
        assertNotNull(classes);
        final Task verifyModel = tasks.findByName(VERIFY_MODEL.getValue());
        assertTrue(classes.getDependsOn()
                          .contains(verifyModel));
    }

    @Test
    public void create_task_and_insert_after_other() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        plugin.newTask(VERIFY_MODEL, NoOp.action())
              .insertAfterTask(COMPILE_JAVA)
              .applyNowTo(project);
        TaskContainer tasks = project.getTasks();
        Task compileJava = tasks.findByName(COMPILE_JAVA.getValue());
        assertNotNull(compileJava);
        Task verifyModel = tasks.findByName(VERIFY_MODEL.getValue());
        assertNotNull(verifyModel);
        assertTrue(verifyModel.getDependsOn()
                              .contains(compileJava.getName()));
    }

    @Test
    public void ignore_tasK_dependency_if_no_such_task_found() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        plugin.newTask(GENERATE_TEST_PROTO, NoOp.action())
              .insertAfterAllTasks(GENERATE_PROTO)
              .applyNowTo(project);
        TaskContainer tasks = project.getTasks();
        Task generateProto = tasks.findByName(GENERATE_PROTO.getValue());
        assertNull(generateProto);
        Task generateTestProto = tasks.findByName(GENERATE_TEST_PROTO.getValue());
        assertNotNull(generateTestProto);
    }

    @Test(expected = IllegalStateException.class)
    public void not_allow_tasks_without_any_connection_to_task_graph() {
        TestPlugin.INSTANCE.newTask(FIND_VALIDATION_RULES, NoOp.action())
                           .applyNowTo(project);
    }

    @Test
    public void return_build_task_description() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        GradleTask desc = plugin.newTask(PRE_CLEAN, NoOp.action())
                                .insertBeforeTask(CLEAN)
                                .applyNowTo(project);
        assertEquals(PRE_CLEAN, desc.getName());
        assertEquals(project, desc.getProject());
    }

    @Test
    public void create_task_with_given_inputs() throws IOException {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        File input = new File(".").getAbsoluteFile();
        plugin.newTask(PRE_CLEAN, NoOp.action())
              .insertBeforeTask(CLEAN)
              .withInputFiles(input.toPath())
              .applyNowTo(project);
        Task task = project.getTasks()
                           .findByPath(PRE_CLEAN.getValue());
        assertNotNull(task);
        File singleInput = task.getInputs()
                               .getFiles()
                               .getFiles()
                               .iterator()
                               .next();
        assertEquals(input.getCanonicalFile(), singleInput.getCanonicalFile());
    }

    /**
     * A NoOp implementation of {@link SpinePlugin} used for tests.
     *
     * <p>Applying this plugin to a project causes no result.
     */
    private static class TestPlugin extends SpinePlugin {

        private static final SpinePlugin INSTANCE = new TestPlugin();

        /** Prevent direct instantiation. */
        private TestPlugin() {
        }

        @Override
        public void apply(Project project) {
            // NoOp for tests.
        }
    }
}
