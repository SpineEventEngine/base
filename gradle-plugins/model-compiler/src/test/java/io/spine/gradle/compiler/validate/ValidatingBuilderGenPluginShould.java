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

package io.spine.gradle.compiler.validate;

import io.spine.gradle.compiler.ProjectConfigurator;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.ProjectConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.compiler.ProjectConfigurator.newEmptyResultHandler;

/**
 * @author Illia Shepilov
 */
public class ValidatingBuilderGenPluginShould {

    @SuppressWarnings("PublicField") // Rules should be public.
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @SuppressWarnings("UseOfSystemOutOrSystemErr")  // Required for a Gradle build is launched.
    @Test
    public void compile_generated_validators() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final ProjectConnection connection =
                new ValidatorsProjectConfigurator(testProjectDir).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(COMPILE_JAVA.getValue());
        try {
            launcher.run(newEmptyResultHandler(countDownLatch));
        } finally {
            connection.close();
        }

        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private static class ValidatorsProjectConfigurator extends ProjectConfigurator {

        private static final String PROJECT_NAME = "validators-gen-plugin-test/";
        private static final String[] TEST_PROTO_FILES = {
                "identifiers.proto",
                "attributes.proto",
                "changes.proto",
                "c/test_commands.proto"
        };

        private ValidatorsProjectConfigurator(TemporaryFolder projectDirectory) {
            super(projectDirectory);
        }

        @Override
        public ProjectConnection configure() throws IOException {
            writeBuildGradle();
            for (String protoFile : TEST_PROTO_FILES) {
                writeProto(PROJECT_NAME, protoFile);
            }

            return createProjectConnection();
        }
    }
}
