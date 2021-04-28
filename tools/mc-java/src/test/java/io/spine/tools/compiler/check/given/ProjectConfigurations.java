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

package io.spine.tools.compiler.check.given;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

import static com.google.common.collect.testing.Helpers.assertEmpty;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.compiler.check.given.ProjectTasks.acquireJavaCompileTasks;
import static io.spine.tools.compiler.check.given.ProjectTasks.obtainCompilerArgs;

/**
 * A test helper providing various {@code assert...} methods related to the {@link Project}
 * configurations, tasks and their arguments.
 */
public class ProjectConfigurations {

    /** Prevents instantiation of this utility class. */
    private ProjectConfigurations() {
    }

    /**
     * Asserts that the given project has no arguments for its tasks of type {@link JavaCompile}.
     *
     * @param project the project to check
     * @throws AssertionError if the project contains any arguments for compile tasks
     */
    public static void assertCompileTasksEmpty(Project project) {
        TaskCollection<JavaCompile> javaCompileTasks = acquireJavaCompileTasks(project);
        for (JavaCompile task : javaCompileTasks) {
            List<String> compilerArgs = obtainCompilerArgs(task);
            assertEmpty(compilerArgs);
        }
    }

    /**
     * Asserts that the given project's {@link JavaCompile} tasks contain the specified arguments.
     *
     * @param project the project to check
     * @param args    the arguments
     * @throws AssertionError if the any of the project's {@link JavaCompile} tasks do not contain
     *                        any of the specified arguments
     */
    public static void assertCompileTasksContain(Project project, String... args) {
        TaskCollection<JavaCompile> javaCompileTasks = acquireJavaCompileTasks(project);
        for (JavaCompile task : javaCompileTasks) {
            List<String> compilerArgs = obtainCompilerArgs(task);
            assertHasAllArgs(compilerArgs, args);
        }
    }

    private static void assertHasAllArgs(List<String> compilerArgs, String[] args) {
        assertThat(compilerArgs).containsAtLeastElementsIn(args);
    }
}
