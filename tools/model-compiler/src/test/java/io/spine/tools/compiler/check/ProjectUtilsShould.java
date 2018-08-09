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

import com.google.common.testing.NullPointerTester;
import org.gradle.api.Project;
import org.junit.Before;
import org.junit.Test;

import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.tools.compiler.check.given.ProjectTaskUtil.assertCompileTasksContain;
import static io.spine.tools.compiler.check.given.ProjectTaskUtil.assertCompileTasksEmpty;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;

/**
 * @author Dmytro Kuzmin
 */
public class ProjectUtilsShould {

    private Project project;

    @Before
    public void setUp() {
        project = newProject();
    }

    @Test
    public void have_utility_constructor() {
        assertHasPrivateParameterlessCtor(ProjectUtils.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicStaticMethods(ProjectUtils.class);
    }

    @Test
    public void add_args_to_java_compile_tasks_of_project() {
        String firstArg = "firstArg";
        String secondArg = "secondArg";
        ProjectUtils.addArgsToJavaCompile(project, firstArg, secondArg);
        assertCompileTasksContain(project, firstArg, secondArg);
    }

    @Test
    public void add_no_args_if_none_specified() {
        ProjectUtils.addArgsToJavaCompile(project);
        assertCompileTasksEmpty(project);
    }
}
