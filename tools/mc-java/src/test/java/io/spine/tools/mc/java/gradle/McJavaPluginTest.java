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
package io.spine.tools.mc.java.gradle;

import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.testing.TaskSubject;
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.tools.gradle.BaseTaskName.clean;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.gradle.testing.GradleTruth.assertThat;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.annotateProto;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.annotateTestProto;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.generateRejections;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.generateTestRejections;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.mergeDescriptorSet;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.mergeTestDescriptorSet;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.preClean;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.MC_JAVA_GRADLE_PLUGIN_ID;

@DisplayName("`McJavaPlugin` should")
class McJavaPluginTest {

    private TaskContainer tasks;

    @BeforeEach
    void createProjectWithPlugin() {
        Project project =
                StubProject.createFor(getClass())
                           .withMavenRepositories()
                           .get();
        project.getPluginManager()
               .apply(MC_JAVA_GRADLE_PLUGIN_ID);
        tasks = project.getTasks();
    }

    @Nested
    @DisplayName("should add a task")
    class AddTask {

        @Test
        void preClean() {
            assertThat(task(clean)).dependsOn(task(preClean))
                                   .isTrue();
        }

        @Test
        void generateRejections() {
            assertDependencies(
                    generateRejections, mergeDescriptorSet, compileJava
            );
        }

        @Test
        void generateTestRejections() {
            assertDependencies(
                    generateTestRejections, mergeTestDescriptorSet, compileTestJava
            );
        }

        @Test
        void annotateProto() {
            assertDependencies(
                    annotateProto, mergeDescriptorSet, compileJava
            );
        }

        @Test
        void annotateTestProto() {
            assertDependencies(
                    annotateTestProto, mergeTestDescriptorSet, compileTestJava
            );
        }

        /**
         * Asserts that the task depends on the second task and is the dependency of the third task.
         *
         * @param task
         *         the name of the task we assert
         * @param dependency
         *         the name of the task which is the dependency of the asserted task
         * @param dependantTask
         *         the name of the task which depends on the asserted task.
         */
        void assertDependencies(TaskName task, TaskName dependency, TaskName dependantTask) {
            TaskSubject assertTask = assertThat(task(task));

            assertTask.dependsOn(dependency)
                      .isTrue();
            assertTask.isDependencyOf(task(dependantTask))
                      .isTrue();
        }

        private Task task(TaskName taskName) {
            Task task = tasks.getByName(taskName.name());
            assertThat(task).isNotNull();
            return task;
        }
    }
}
