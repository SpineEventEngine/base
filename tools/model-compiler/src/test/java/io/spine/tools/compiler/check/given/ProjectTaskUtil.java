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

package io.spine.tools.compiler.check.given;

import org.gradle.BuildListener;
import org.gradle.api.Project;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

import static com.google.common.collect.testing.Helpers.assertEmpty;
import static io.spine.testing.Verify.assertContains;

/**
 * The utility with several methods related to project tasks and required to properly test the
 * {@link io.spine.tools.gradle.compiler.ErrorProneChecksPlugin} functionality.
 *
 * @author Dmytro Kuzmin
 */
public class ProjectTaskUtil {

    /** Prevents instantiation of this utility class. */
    private ProjectTaskUtil() {
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

    private static TaskCollection<JavaCompile> acquireJavaCompileTasks(Project project) {
        GradleInternal gradle = (GradleInternal) project.getGradle();
        BuildListener buildListenerBroadcaster = gradle.getBuildListenerBroadcaster();
        buildListenerBroadcaster.projectsEvaluated(project.getGradle());
        TaskContainer tasks = project.getTasks();
        return tasks.withType(JavaCompile.class);
    }

    private static List<String> obtainCompilerArgs(JavaCompile task) {
        CompileOptions options = task.getOptions();
        return options.getCompilerArgs();
    }

    private static void assertHasAllArgs(List<String> compilerArgs, String[] args) {
        for (String arg : args) {
            assertContains(arg, compilerArgs);
        }
    }
}
