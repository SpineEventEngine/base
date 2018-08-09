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

package io.spine.tools.compiler.check;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities related to working with Spine-custom Error Prone checks and
 * {@link io.spine.tools.gradle.compiler.ErrorProneChecksPlugin}.
 *
 * @author Dmytro Kuzmin
 */
class ProjectUtils {

    /** Prevents instantiation of this utility class. */
    private ProjectUtils() {
    }

    /**
     * Adds specified arguments to all {@code JavaCompile} tasks of the project.
     *
     * @param project   the project whose tasks are to be modified
     * @param arguments the arguments to add to the tasks
     */
    static void addArgsToJavaCompile(Project project, String... arguments) {
        checkNotNull(project);
        checkNotNull(arguments);

        TaskContainer tasks = project.getTasks();
        TaskCollection<JavaCompile> javaCompileTasks = tasks.withType(JavaCompile.class);
        for (JavaCompile task : javaCompileTasks) {
            CompileOptions taskOptions = task.getOptions();
            List<String> compilerArgs = taskOptions.getCompilerArgs();
            compilerArgs.addAll(Arrays.asList(arguments));
        }
    }
}
