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

import static io.spine.tools.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.tools.gradle.TaskName.ANNOTATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.CLEAN;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.FIND_TEST_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.GENERATE_REJECTIONS;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_REJECTIONS;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.PRE_CLEAN;
import static io.spine.tools.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.tools.gradle.TaskName.PROCESS_TEST_RESOURCES;
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
            assertThat(task(CLEAN)).dependsOn(task(PRE_CLEAN)).isTrue();
        }

        @Test
        void generateRejections() {
            assertDependencies(
                    GENERATE_REJECTIONS, MERGE_DESCRIPTOR_SET, COMPILE_JAVA
            );
        }

        @Test
        void generateTestRejections() {
            assertDependencies(
                    GENERATE_TEST_REJECTIONS, MERGE_TEST_DESCRIPTOR_SET, COMPILE_TEST_JAVA
            );
        }

        @Test
        void findValidationRules() {
            assertDependencies(
                    FIND_VALIDATION_RULES, MERGE_DESCRIPTOR_SET, PROCESS_RESOURCES
            );
        }

        @Test
        void findTestValidationRules() {
            assertDependencies(
                    FIND_TEST_VALIDATION_RULES, MERGE_TEST_DESCRIPTOR_SET, PROCESS_TEST_RESOURCES
            );
        }

        @Test
        void generateValidatingBuilders() {
            assertDependencies(
                    GENERATE_VALIDATING_BUILDERS, MERGE_DESCRIPTOR_SET, COMPILE_JAVA
            );
        }

        @Test
        void generateTestValidatingBuilders() {
            assertDependencies(
                    GENERATE_TEST_VALIDATING_BUILDERS, MERGE_TEST_DESCRIPTOR_SET, COMPILE_TEST_JAVA
            );
        }

        @Test
        void annotateProto() {
            assertDependencies(
                    ANNOTATE_PROTO, MERGE_DESCRIPTOR_SET, COMPILE_JAVA
            );
        }

        @Test
        void annotateTestProto() {
            assertDependencies(
                    ANNOTATE_TEST_PROTO, MERGE_TEST_DESCRIPTOR_SET, COMPILE_TEST_JAVA
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
            Task task = tasks.getByName(taskName.getValue());
            assertThat(task).isNotNull();
            return task;
        }
    }
}
