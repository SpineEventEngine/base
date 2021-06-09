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

package io.spine.tools.gradle;

import io.spine.tools.gradle.testing.NoOp;
import io.spine.tools.mc.java.gradle.McJavaTaskName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.tools.gradle.BaseTaskName.clean;
import static io.spine.tools.gradle.GradleTask.Builder;
import static io.spine.tools.gradle.JavaTaskName.classes;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.annotateProto;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.preClean;
import static io.spine.tools.mc.java.gradle.ModelVerifierTaskName.verifyModel;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.testing.GradleProject.javaPlugin;
import static io.spine.tools.gradle.testing.NoOp.action;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SpinePlugin}.
 *
 * @apiNote This test suite is placed under the {@code mc-java} module, while the class
 * it tests ({@link SpinePlugin} is under {@code }) because the test suite uses
 * {@link McJavaTaskName}. Presumably, this test suite should be updated using
 * stub task names (which would be language-neutral) and returned back to the
 */
@DisplayName("`SpinePlugin` should")
class SpinePluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                                .build();
        project.getPluginManager()
               .apply(javaPlugin());
    }

    @Test
    @DisplayName("create task dependant on all tasks of given name")
    void createTaskDependantOnAllTasksOfGivenName() {
        Project subProject = ProjectBuilder.builder()
                                           .withParent(project)
                                           .build();
        subProject.getPluginManager()
                  .apply(javaPlugin());
        SpinePlugin plugin = TestPlugin.INSTANCE;
        GradleTask task = plugin.newTask(annotateProto, NoOp.action())
                                .insertAfterAllTasks(compileJava)
                                .applyNowTo(subProject);
        TaskContainer subProjectTasks = subProject.getTasks();
        Task newTask = subProjectTasks.findByName(task.getName().name());
        assertNotNull(newTask);
        Collection<?> dependencies = newTask.getDependsOn();
        assertTrue(dependencies.contains(subProjectTasks.findByName(compileJava.name())));
        assertTrue(dependencies.contains(project.getTasks()
                                                .findByName(compileJava.name())));
    }

    @Test
    @DisplayName("create task and insert before other")
    void createTaskAndInsertBeforeOther() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        plugin.newTask(verifyModel, NoOp.action())
              .insertBeforeTask(classes)
              .applyNowTo(project);
        TaskContainer tasks = project.getTasks();
        Task classes = tasks.findByName(JavaTaskName.classes.name());
        assertNotNull(classes);
        Task verifyModelTask = tasks.findByName(verifyModel.name());
        assertTrue(classes.getDependsOn()
                          .contains(verifyModelTask));
    }

    @Test
    @DisplayName("create task and insert after other")
    void createTaskAndInsertAfterOther() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        plugin.newTask(verifyModel, NoOp.action())
              .insertAfterTask(compileJava)
              .applyNowTo(project);
        TaskContainer tasks = project.getTasks();
        Task compileJavaTask = tasks.findByName(compileJava.name());
        assertNotNull(compileJavaTask);
        Task verifyModelTask = tasks.findByName(verifyModel.name());
        assertNotNull(verifyModelTask);
        assertTrue(verifyModelTask.getDependsOn()
                                  .contains(compileJavaTask.getName()));
    }

    @Test
    @DisplayName("ignore task dependency if no such task found")
    void ignoreTaskDependencyIfNoSuchTaskFound() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        plugin.newTask(generateTestProto, NoOp.action())
              .insertAfterAllTasks(generateProto)
              .applyNowTo(project);
        TaskContainer tasks = project.getTasks();
        Task generateProtoTask = tasks.findByName(generateProto.name());
        assertNull(generateProtoTask);
        Task generateTestProtoTask = tasks.findByName(generateTestProto.name());
        assertNotNull(generateTestProtoTask);
    }

    @Test
    @DisplayName("not allow tasks without any connection to task graph")
    void notAllowTasksWithoutAnyConnectionToTaskGraph() {
        Builder builder = TestPlugin.INSTANCE.newTask(verifyModel, action());
        assertIllegalState(() -> builder.applyNowTo(project));
    }

    @Test
    @DisplayName("return build task description")
    void returnBuildTaskDescription() {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        GradleTask desc = plugin.newTask(preClean, NoOp.action())
                                .insertBeforeTask(clean)
                                .applyNowTo(project);
        assertEquals(preClean, desc.getName());
        assertEquals(project, desc.getProject());
    }

    @Test
    @DisplayName("create task with given inputs")
    void createTaskWithGivenInputs() throws IOException {
        SpinePlugin plugin = TestPlugin.INSTANCE;
        File input = new File(".").getAbsoluteFile();
        FileCollection files = project.getLayout().files(input);
        plugin.newTask(preClean, NoOp.action())
              .insertBeforeTask(clean)
              .withInputFiles(files)
              .applyNowTo(project);
        Task task = project.getTasks()
                           .findByPath(preClean.name());
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
