/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.model;

import io.spine.gradle.TaskName;
import io.spine.gradle.compiler.GradleProject;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static io.spine.gradle.TaskName.GENERATE_MODEL;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Dmytro Dashenkov
 */
public class ModelVerifierPluginShould {

    private static final String PROJECT_NAME = "model-generator-test";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void pass_valid_model_classes() {
        newProjectWithJava("io/spine/tools/model/TestAggregate.java",
                           "io/spine/tools/model/TestProcMan.java",
                           "io/spine/tools/model/TestCommandHandler.java")
                .executeTask(GENERATE_MODEL);
    }

    @Test
    public void halt_build_on_duplicate_command_handling_methods() {
        final BuildResult result = newProjectWithJava(
                "io/spine/tools/model/DuplicateAggregate.java",
                "io/spine/tools/model/DuplicateCommandHandler.java")
                .executeAndFail(GENERATE_MODEL);
        final BuildTask task = result.task(toPath(GENERATE_MODEL));
        assertNotNull(task);
        final TaskOutcome generationResult = task.getOutcome();
        assertEquals(FAILED, generationResult);
    }

    @Ignore // TODO:2017-08-25:dmytro.dashenkov: Re-enable when Model is capable of checking the handler methods.
    @Test
    public void halt_build_on_malformed_command_handling_methods() {
        final BuildResult result =
                newProjectWithJava("io/spine/tools/model/MalformedAggregate.java")
                .executeAndFail(GENERATE_MODEL);
        final BuildTask task = result.task(toPath(GENERATE_MODEL));
        assertNotNull(task);
        final TaskOutcome generationResult = task.getOutcome();
        assertEquals(FAILED, generationResult);
    }

    private GradleProject newProjectWithJava(String... fileNames) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .addJavaFiles(fileNames)
                            .build();
    }

    private static String toPath(TaskName name) {
        return ':' + name.getValue();
    }
}
