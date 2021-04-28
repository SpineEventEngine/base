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

import org.gradle.BuildListener;
import org.gradle.api.Project;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

/**
 * An extractor for the {@link Project} {@linkplain org.gradle.api.Task tasks} and their arguments.
 */
class ProjectTasks {

    /** Prevents instantiation of this utility class. */
    private ProjectTasks() {
    }

    /**
     * Returns a list of project's tasks of type {@link JavaCompile}.
     *
     * <p>Evaluates the project in a process to trigger all tasks' arguments modifications.
     *
     * @param project the project to obtain tasks from
     * @return the project {@link JavaCompile} tasks
     */
    static TaskCollection<JavaCompile> acquireJavaCompileTasks(Project project) {
        GradleInternal gradle = (GradleInternal) project.getGradle();
        BuildListener buildListenerBroadcaster = gradle.getBuildListenerBroadcaster();
        buildListenerBroadcaster.projectsEvaluated(project.getGradle());
        TaskContainer tasks = project.getTasks();
        TaskCollection<JavaCompile> javaCompileTasks = tasks.withType(JavaCompile.class);
        return javaCompileTasks;
    }

    /**
     * Returns compiler arguments from the given {@code JavaCompile} task.
     *
     * @param task the task to obtain the arguments from
     * @return the {@code List} of the compiler arguments
     */
    static List<String> obtainCompilerArgs(JavaCompile task) {
        CompileOptions options = task.getOptions();
        List<String> compilerArgs = options.getCompilerArgs();
        return compilerArgs;
    }
}
