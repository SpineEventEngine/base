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

package io.spine.tools.mc.dart.gradle;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.gradle.ProtoDartTaskName.copyGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.copyTestGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.resolveImports;

@DisplayName("`ProtoDartPlugin` should")
class McDartPluginTest {

    private Project project;

    @BeforeEach
    void setUp(@TempDir File dir) {
        project = ProjectBuilder
                .builder()
                .withName(McDartPluginTest.class.getName())
                .withProjectDir(dir)
                .build();
        project.apply(action -> action.plugin("java"));
    }

    @Test
    @DisplayName("create `copyGeneratedDart` task")
    void createMainTask() {
        McDartPlugin plugin = new McDartPlugin();
        plugin.apply(project);

        Task task = findTask(copyGeneratedDart);
        assertThat(task.getDependsOn()).isNotEmpty();

        Task assembleTask = findTask(assemble);
        assertThat(assembleTask.getDependsOn()).contains(task.getName());
    }

    @Test
    @DisplayName("create `copyTestGeneratedDart` task")
    void createTestTask() {
        McDartPlugin plugin = new McDartPlugin();
        plugin.apply(project);

        Task task = findTask(copyTestGeneratedDart);
        assertThat(task.getDependsOn()).isNotEmpty();

        Task assembleTask = findTask(assemble);
        assertThat(assembleTask.getDependsOn()).contains(task.getName());
    }

    @Test
    @DisplayName("create `resolveImports` task")
    void createResolveTask() {
        McDartPlugin plugin = new McDartPlugin();
        plugin.apply(project);

        findTask(resolveImports);
    }

    @CanIgnoreReturnValue
    private Task findTask(TaskName name) {
        Task task = project.getTasks()
                           .findByName(name.name());
        assertThat(task).isNotNull();
        return task;
    }
}
