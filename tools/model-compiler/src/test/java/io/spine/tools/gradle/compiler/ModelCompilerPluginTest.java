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

import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.testing.TaskSubject;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.tools.gradle.TaskName.annotateProto;
import static io.spine.tools.gradle.TaskName.annotateTestProto;
import static io.spine.tools.gradle.TaskName.clean;
import static io.spine.tools.gradle.TaskName.compileJava;
import static io.spine.tools.gradle.TaskName.compileTestJava;
import static io.spine.tools.gradle.TaskName.generateRejections;
import static io.spine.tools.gradle.TaskName.generateTestRejections;
import static io.spine.tools.gradle.TaskName.generateTestValidatingBuilders;
import static io.spine.tools.gradle.TaskName.generateValidatingBuilders;
import static io.spine.tools.gradle.TaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.TaskName.mergeTestDescriptorSet;
import static io.spine.tools.gradle.TaskName.preClean;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.SPINE_PROTOBUF_PLUGIN_ID;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static io.spine.tools.gradle.testing.GradleTruth.assertThat;

@DisplayName("ModelCompilerPlugin should")
class ModelCompilerPluginTest {

    private TaskContainer tasks;

    @BeforeEach
    void setUp() {
        Project project = newProject();
        project.getPluginManager()
               .apply(SPINE_PROTOBUF_PLUGIN_ID);
        tasks = project.getTasks();
    }

    @Test
    @DisplayName("apply to project")
    void apply() {
        Project project = newProject();
        project.getPluginManager()
               .apply(SPINE_PROTOBUF_PLUGIN_ID);
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
        void generateValidatingBuilders() {
            assertDependencies(
                    generateValidatingBuilders, mergeDescriptorSet, compileJava
            );
        }

        @Test
        void generateTestValidatingBuilders() {
            assertDependencies(
                    generateTestValidatingBuilders, mergeTestDescriptorSet, compileTestJava
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
            Task task = tasks.getByName(taskName.value());
            assertThat(task).isNotNull();
            return task;
        }
    }
}
