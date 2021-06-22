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

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with {@link JavaCompile} tasks.
 */
final class JavaCompileTasks {

    private final TaskCollection<JavaCompile> tasks;

    private JavaCompileTasks(Project project) {
        TaskContainer allTasks = project.getTasks();
        this.tasks = allTasks.withType(JavaCompile.class);
    }

    /**
     * Creates a new instance for the passed projects.
     */
    static JavaCompileTasks of(Project project) {
        checkNotNull(project);
        return new JavaCompileTasks(project);
    }

    /**
     * Adds specified arguments to all {@code JavaCompile} tasks of the project.
     */
    void addArgs(String... arguments) {
        checkNotNull(arguments);
        for (JavaCompile task : tasks) {
            CompileOptions taskOptions = task.getOptions();
            List<String> compilerArgs = taskOptions.getCompilerArgs();
            compilerArgs.addAll(Arrays.asList(arguments));
        }
    }
}
