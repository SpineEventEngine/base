/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import io.spine.testing.UtilityClassTest;
import org.gradle.api.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.compiler.check.given.ProjectConfigurations.assertCompileTasksContain;
import static io.spine.tools.compiler.check.given.ProjectConfigurations.assertCompileTasksEmpty;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;

@DisplayName("ProjectArguments utility class should")
class ProjectArgumentsTest extends UtilityClassTest<ProjectArguments> {

    private final Project project = newProject();

    ProjectArgumentsTest() {
        super(ProjectArguments.class);
    }

    @Test
    @DisplayName("add arguments to Java compile tasks")
    void add_args_to_java_compile_tasks_of_project() {
        String firstArg = "firstArg";
        String secondArg = "secondArg";
        ProjectArguments.addArgsToJavaCompile(project, firstArg, secondArg);
        assertCompileTasksContain(project, firstArg, secondArg);
    }

    @Test
    @DisplayName("not add arguments if none is specified")
    void add_no_args_if_none_specified() {
        ProjectArguments.addArgsToJavaCompile(project);
        assertCompileTasksEmpty(project);
    }
}
