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
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.gradle.TaskDependencies.dependsOn;
import static io.spine.tools.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.tools.gradle.TaskName.ANNOTATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.CLEAN;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.FIND_ENRICHMENTS;
import static io.spine.tools.gradle.TaskName.FIND_TEST_ENRICHMENTS;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void apply_to_project() {
        Project project = newProject();
        project.getPluginManager()
               .apply(SPINE_PROTOBUF_PLUGIN_ID);
    }

    @Test
    @DisplayName("add preClean task")
    void add_task_preClean() {
        assertNotNull(task(PRE_CLEAN));
        assertTrue(dependsOn(task(CLEAN), task(PRE_CLEAN)));
    }

    @Test
    @DisplayName("add generateRejections task")
    void add_task_generateRejections() {
        Task genRejections = task(GENERATE_REJECTIONS);
        assertNotNull(genRejections);
        assertTrue(dependsOn(genRejections, MERGE_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(COMPILE_JAVA), genRejections));
    }

    @Test
    @DisplayName("add generateTestRejections task")
    void add_task_generateTestRejections() {
        Task genTestRejections = task(GENERATE_TEST_REJECTIONS);
        assertNotNull(genTestRejections);
        assertTrue(dependsOn(genTestRejections, MERGE_TEST_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(COMPILE_TEST_JAVA), genTestRejections));
    }

    @Test
    @DisplayName("add findEnrichments task")
    void add_task_findEnrichments() {
        Task find = task(FIND_ENRICHMENTS);
        assertNotNull(find);
        assertTrue(dependsOn(find, COMPILE_JAVA));
        assertTrue(dependsOn(task(PROCESS_RESOURCES), find));
    }

    @Test
    @DisplayName("add findTestEnrichments task")
    void add_task_findTestEnrichments() {
        Task find = task(FIND_TEST_ENRICHMENTS);
        assertNotNull(find);
        assertTrue(dependsOn(find, COMPILE_TEST_JAVA));
        assertTrue(dependsOn(task(PROCESS_TEST_RESOURCES), find));
    }

    @Test
    @DisplayName("add findValidationRules task")
    void add_task_findValidationRules() {
        Task find = task(FIND_VALIDATION_RULES);
        assertNotNull(find);
        assertTrue(dependsOn(find, MERGE_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(PROCESS_RESOURCES), find));
    }

    @Test
    @DisplayName("add findTestValidationRules task")
    void add_task_findTestValidationRules() {
        Task find = task(FIND_TEST_VALIDATION_RULES);
        assertNotNull(find);
        assertTrue(dependsOn(find, MERGE_TEST_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(PROCESS_TEST_RESOURCES), find));
    }

    @Test
    @DisplayName("add generateValidatingBuilders task")
    void add_task_generation_validating_builders() {
        Task genValidatingBuilders = task(GENERATE_VALIDATING_BUILDERS);
        assertNotNull(genValidatingBuilders);
        assertTrue(dependsOn(genValidatingBuilders, MERGE_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(COMPILE_JAVA), genValidatingBuilders));
    }

    @Test
    @DisplayName("add generateTestValidatingBuilders task")
    void add_task_generation_test_validating_builders() {
        Task genTestValidatingBuidlers = task(GENERATE_TEST_VALIDATING_BUILDERS);
        assertNotNull(genTestValidatingBuidlers);
        assertTrue(dependsOn(genTestValidatingBuidlers, MERGE_TEST_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(COMPILE_TEST_JAVA), genTestValidatingBuidlers));
    }

    @Test
    @DisplayName("add annotateProto task")
    void add_task_annotateProto() {
        Task annotateProto = task(ANNOTATE_PROTO);
        assertNotNull(annotateProto);
        assertTrue(dependsOn(annotateProto, MERGE_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(COMPILE_JAVA), annotateProto));
    }

    @Test
    @DisplayName("add annotateTestProto task")
    void add_task_annotateTestProto() {
        Task annotateTestProto = task(ANNOTATE_TEST_PROTO);
        assertNotNull(annotateTestProto);
        assertTrue(dependsOn(annotateTestProto, MERGE_TEST_DESCRIPTOR_SET));
        assertTrue(dependsOn(task(COMPILE_TEST_JAVA), annotateTestProto));
    }

    private Task task(TaskName taskName) {
        return tasks.getByName(taskName.getValue());
    }
}
