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

import com.google.common.collect.ImmutableList;
import io.spine.tools.gradle.GradleProject;
import io.spine.tools.gradle.TaskName;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;

@ExtendWith(TempDirectory.class)
@DisplayName("ValidatingBuilderGenPlugin should")
class ValidatingBuilderGenPluginTest {

    /**
     * The name of the directory under `test/resource` which will be used for creating
     * the test project.
     */
    private static final String PROJECT_NAME = "validators-gen-plugin-test";

    /**
     * Names of resource files under the resources "root".
     */
    private static final ImmutableList<String> PROTO_FILES =
            ImmutableList.of("identifiers.proto",
                             "attributes.proto",
                             "changes.proto",
                             "test_commands.proto");

    private File testProjectDir;

    @BeforeEach
    void setUp(@TempDirectory.TempDir Path tempDirPath) {
        testProjectDir = tempDirPath.toFile();
    }

    @Test
    @DisplayName("compile generated validators")
    void compileGeneratedValidators() {
        GradleProject project = newProject();
        project.executeTask(COMPILE_JAVA);
    }

    @Test
    @DisplayName("skip task if inputs and outputs stay the same")
    void incremental() {
        GradleProject project = newProject();

        TaskName taskName = GENERATE_VALIDATING_BUILDERS;
        BuildResult firstRun = project.executeTask(taskName);
        TaskOutcome firstOutcome = firstRun.task(taskName.path()).getOutcome();
        assertThat(firstOutcome).isEqualTo(SUCCESS);

        BuildResult secondRun = project.executeTask(taskName);
        TaskOutcome secondOutcome = secondRun.task(taskName.path()).getOutcome();
        assertThat(secondOutcome).isEqualTo(UP_TO_DATE);
    }

    private GradleProject newProject() {
        GradleProject project =
                GradleProject.newBuilder()
                             .setProjectName(PROJECT_NAME)
                             .setProjectFolder(testProjectDir)
                             .addProtoFiles(PROTO_FILES)
                             .enableDebug()
                             .build();
        return project;
    }
}
